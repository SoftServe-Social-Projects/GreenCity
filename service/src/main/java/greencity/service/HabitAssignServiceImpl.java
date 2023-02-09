package greencity.service;

import greencity.achievement.AchievementCalculation;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.habit.*;
import greencity.dto.habitstatuscalendar.HabitStatusCalendarVO;
import greencity.dto.shoppinglistitem.BulkSaveCustomShoppingListItemDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemResponseDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemSaveRequestDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemWithStatusSaveRequestDto;
import greencity.dto.shoppinglistitem.ShoppingListItemDto;
import greencity.dto.shoppinglistitem.ShoppingListItemRequestDto;
import greencity.dto.shoppinglistitem.ShoppingListItemWithStatusRequestDto;
import greencity.dto.user.UserShoppingListItemAdvanceDto;
import greencity.dto.user.UserShoppingListItemResponseDto;
import greencity.dto.user.UserVO;
import greencity.entity.*;
import greencity.entity.localization.ShoppingListItemTranslation;
import greencity.enums.AchievementCategoryType;
import greencity.enums.AchievementType;
import greencity.enums.HabitAssignStatus;
import greencity.enums.ShoppingListItemStatus;
import greencity.exception.exceptions.*;
import greencity.repository.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link HabitAssignService}.
 */
@Service
@AllArgsConstructor
public class HabitAssignServiceImpl implements HabitAssignService {
    private final HabitAssignRepo habitAssignRepo;
    private final HabitRepo habitRepo;
    private final ShoppingListItemRepo shoppingListItemRepo;
    private final UserShoppingListItemRepo userShoppingListItemRepo;
    private final CustomShoppingListItemRepo customShoppingListItemRepo;
    private final ShoppingListItemTranslationRepo shoppingListItemTranslationRepo;
    private final ShoppingListItemService shoppingListItemService;
    private final CustomShoppingListItemService customShoppingListItemService;
    private final HabitStatisticService habitStatisticService;
    private final HabitStatusCalendarService habitStatusCalendarService;
    private final AchievementCalculation achievementCalculation;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitAssignDto getById(Long habitAssignId, String language) {
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId));
        return buildHabitAssignDto(habitAssign, language);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public HabitAssignManagementDto assignDefaultHabitForUser(Long habitId, UserVO userVO) {
        checkStatusInProgressExists(habitId, userVO);

        User user = modelMapper.map(userVO, User.class);

        Habit habit = habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId));
        validateHabitForAssign(habitId, user);
        HabitAssign habitAssign =
            habitAssignRepo.findByHabitIdAndUserIdAndStatusIsCancelled(habitId, user.getId());

        if (habitAssign != null) {
            habitAssign.setStatus(HabitAssignStatus.INPROGRESS);
            habitAssign.setCreateDate(ZonedDateTime.now());
        } else {
            List<ShoppingListItem> shoppingList =
                shoppingListItemRepo.getShoppingListByListOfId(
                    shoppingListItemRepo.getAllShoppingListItemIdByHabitIdISContained(habitId));
            habitAssign = buildHabitAssign(habit, user);
            saveUserShoppingListItems(shoppingList, habitAssign);
        }

        enhanceAssignWithDefaultProperties(habitAssign);

        return modelMapper.map(habitAssign, HabitAssignManagementDto.class);
    }

    /**
     * Method updates {@link HabitAssign} with default properties.
     *
     * @param habitAssign {@link HabitAssign} instance.
     */
    private void enhanceAssignWithDefaultProperties(HabitAssign habitAssign) {
        habitAssign.setDuration(habitAssign.getHabit().getDefaultDuration());
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public HabitAssignManagementDto assignCustomHabitForUser(Long habitId, UserVO userVO,
        HabitAssignPropertiesDto habitAssignPropertiesDto) {
        User user = modelMapper.map(userVO, User.class);

        checkStatusInProgressExists(habitId, userVO);

        Habit habit = habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId));
        validateHabitForAssign(habitId, user);
        HabitAssign habitAssign =
            habitAssignRepo.findByHabitIdAndUserIdAndStatusIsCancelled(habitId, user.getId());
        if (habitAssign != null) {
            habitAssign.setStatus(HabitAssignStatus.INPROGRESS);
            habitAssign.setCreateDate(ZonedDateTime.now());
        } else {
            habitAssign = buildHabitAssign(habit, user);
        }
        enhanceAssignWithCustomProperties(habitAssign, habitAssignPropertiesDto);

        if (!habitAssignPropertiesDto.getDefaultShoppingListItems().isEmpty()) {
            List<ShoppingListItem> shoppingList =
                shoppingListItemRepo.getShoppingListByListOfId(habitAssignPropertiesDto
                    .getDefaultShoppingListItems());
            saveUserShoppingListItems(shoppingList, habitAssign);
        }

        habitAssignRepo.save(habitAssign);

        return modelMapper.map(habitAssign, HabitAssignManagementDto.class);
    }

    private void checkStatusInProgressExists(Long habitId, UserVO userVO) {
        List<HabitAssign> habits = habitAssignRepo.findAllByUserId(userVO.getId());
        boolean habitInProgress = habits.stream()
            .filter(h -> h.getHabit().getId().equals(habitId))
            .anyMatch(h -> h.getStatus().equals(HabitAssignStatus.INPROGRESS));

        if (habitInProgress) {
            throw new UserAlreadyHasHabitAssignedException(
                ErrorMessage.USER_ALREADY_HAS_ASSIGNED_HABIT + habitId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HabitAssignDto> getAllCustomHabitAssignsByUserId(Long userId, String language) {
        return habitAssignRepo.findAllByUserId(userId)
            .stream()
            .filter(this::isHabitCustom)
            .map(habitAssign -> buildHabitAssignDtoContent(habitAssign, language)).collect(Collectors.toList());
    }

    /**
     * Method checks if {@link HabitAssign} is custom.
     *
     * @param habitAssign {@link HabitAssign} instance.
     * @return boolean.
     */
    private boolean isHabitCustom(HabitAssign habitAssign) {
        Integer duration = habitAssign.getDuration();
        Integer defaultDuration = habitAssign.getHabit().getDefaultDuration();
        List<UserShoppingListItem> shoppingListItems = habitAssign.getUserShoppingListItems();
        return !duration.equals(defaultDuration) && !shoppingListItems.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTime getEndDate(HabitAssignDto habitAssign) {
        return habitAssign.getCreateDateTime().plusDays(habitAssign.getDuration());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getReadinessPercent(HabitAssignDto habitAssign) {
        return habitAssign.getWorkingDays() * 100 / habitAssign.getDuration();
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public HabitAssignUserShoppingListItemDto updateUserShoppingItemListAndDuration(Long habitId, Long userId,
        HabitAssignPropertiesDto habitAssignPropertiesDto) {
        if (!habitRepo.existsById(habitId)) {
            throw new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId);
        }
        HabitAssign habitAssign = habitAssignRepo.findByHabitIdAndUserIdAndStatusIsInprogress(habitId, userId)
            .orElseThrow(() -> new InvalidStatusException(ErrorMessage.HABIT_ASSIGN_STATUS_IS_NOT_INPROGRESS));
        enhanceAssignWithCustomProperties(habitAssign, habitAssignPropertiesDto);
        if (habitAssignPropertiesDto.getDefaultShoppingListItems() != null
            && !habitAssignPropertiesDto.getDefaultShoppingListItems().isEmpty()) {
            List<ShoppingListItem> shoppingListItems =
                shoppingListItemRepo.getShoppingListByListOfId(habitAssignPropertiesDto
                    .getDefaultShoppingListItems());
            List<UserShoppingListItem> userShoppingListItems = shoppingListItems.stream()
                .map(s -> buildUserShoppingListItems(s, habitAssign))
                .collect(Collectors.toList());
            Hibernate.initialize(habitAssign.getUserShoppingListItems());
            userShoppingListItemRepo.deleteAll(habitAssign.getUserShoppingListItems());
            habitAssign.setUserShoppingListItems(userShoppingListItems);
        }
        return modelMapper.map(habitAssignRepo.save(habitAssign), HabitAssignUserShoppingListItemDto.class);
    }

    private UserShoppingListItem buildUserShoppingListItems(ShoppingListItem shoppingListItem,
        HabitAssign habitAssign) {
        return UserShoppingListItem.builder()
            .habitAssign(habitAssign)
            .shoppingListItem(shoppingListItem)
            .status(ShoppingListItemStatus.INPROGRESS)
            .build();
    }

    private void saveUserShoppingListItems(List<ShoppingListItem> shoppingList, HabitAssign habitAssign) {
        List<UserShoppingListItem> userShoppingList = new ArrayList<>();
        for (ShoppingListItem shoppingItem : shoppingList) {
            userShoppingList.add(UserShoppingListItem.builder()
                .habitAssign(habitAssign)
                .shoppingListItem(shoppingItem)
                .status(ShoppingListItemStatus.ACTIVE)
                .build());
        }
        userShoppingListItemRepo.saveAll(userShoppingList);
    }

    /**
     * Method updates {@link HabitAssign} with custom properties from
     * {@link HabitAssignPropertiesDto} instance.
     *
     * @param habitAssign {@link HabitAssign} instance.
     * @param props       {@link HabitAssignPropertiesDto} instance.
     */
    private void enhanceAssignWithCustomProperties(HabitAssign habitAssign,
        HabitAssignPropertiesDto props) {
        habitAssign.setDuration(props.getDuration());
    }

    /**
     * Method builds {@link HabitAssign} with main props.
     *
     * @param habit {@link Habit} instance.
     * @param user  {@link User} instance.
     * @return {@link HabitAssign} instance.
     */
    private HabitAssign buildHabitAssign(Habit habit, User user) {
        return habitAssignRepo.save(
            HabitAssign.builder()
                .habit(habit)
                .status(HabitAssignStatus.INPROGRESS)
                .createDate(ZonedDateTime.now())
                .user(user)
                .duration(habit.getDefaultDuration())
                .habitStreak(0)
                .workingDays(0)
                .lastEnrollmentDate(ZonedDateTime.now())
                .build());
    }

    /**
     * Method builds {@link HabitAssignDto} with one habit translation.
     *
     * @param habitAssign {@link HabitAssign} instance.
     * @param language    code of {@link Language}.
     * @return {@link HabitAssign} instance.
     */
    private HabitAssignDto buildHabitAssignDto(HabitAssign habitAssign, String language) {
        HabitTranslation habitTranslation = getHabitTranslation(habitAssign, language);
        HabitAssignDto habitAssignDto = modelMapper.map(habitAssign, HabitAssignDto.class);
        habitAssignDto.setHabit(modelMapper.map(habitTranslation, HabitDto.class));
        setShoppingListItems(habitAssignDto, habitAssign, language);
        return habitAssignDto;
    }

    private void setShoppingListItems(HabitAssignDto habitAssignDto, HabitAssign habitAssign, String language) {
        habitAssignDto.getHabit().setShoppingListItems(userShoppingListItemRepo
            .getAllAssignedShoppingListItemsFull(habitAssign.getId()).stream()
            .map(shoppingItem -> ShoppingListItemDto.builder()
                .id(shoppingItem.getId())
                .status(shoppingItem.getStatus().toString())
                .text(shoppingItem.getShoppingListItem().getTranslations().stream()
                    .filter(shopItem -> shopItem.getLanguage().getCode().equals(language)).findFirst()
                    .orElseThrow(
                        () -> new NotFoundException(
                            ErrorMessage.SHOPPING_LIST_ITEM_TRANSLATION_NOT_FOUND + habitAssignDto.getHabit().getId()))
                    .getContent())
                .build())
            .collect(Collectors.toList()));
    }

    private HabitAssignDto buildHabitAssignDtoContent(HabitAssign habitAssign, String language) {
        HabitAssignDto habitAssignDto = buildHabitAssignDto(habitAssign, language);
        habitAssignDto.setUserShoppingListItems(buildUserShoppingListItemAdvanceDto(habitAssign, language));
        return habitAssignDto;
    }

    private List<UserShoppingListItemAdvanceDto> buildUserShoppingListItemAdvanceDto(HabitAssign habitAssign,
        String language) {
        List<UserShoppingListItemAdvanceDto> userItemsDTO = new ArrayList<>();
        boolean isContains;
        List<ShoppingListItemTranslation> listItemTranslations = shoppingListItemTranslationRepo
            .findShoppingListByHabitIdAndByLanguageCode(language, habitAssign.getHabit().getId());
        for (ShoppingListItemTranslation translationItem : listItemTranslations) {
            isContains = false;
            for (UserShoppingListItem userItem : habitAssign.getUserShoppingListItems()) {
                if (translationItem.getShoppingListItem().getId().equals(userItem.getShoppingListItem().getId())) {
                    userItemsDTO.add(UserShoppingListItemAdvanceDto.builder()
                        .id(userItem.getId())
                        .shoppingListItemId(translationItem.getId())
                        .status(userItem.getStatus())
                        .dateCompleted(userItem.getDateCompleted())
                        .content(translationItem.getContent())
                        .build());
                    isContains = true;
                    break;
                }
            }
            if (!isContains) {
                userItemsDTO.add(UserShoppingListItemAdvanceDto.builder()
                    .shoppingListItemId(translationItem.getId())
                    .status(ShoppingListItemStatus.ACTIVE)
                    .content(translationItem.getContent())
                    .build());
            }
        }
        return userItemsDTO;
    }

    @Override
    @Transactional
    public void updateUserShoppingListItem(UpdateUserShoppingListDto updateUserShoppingListDto) {
        userShoppingListItemRepo.saveAll(buildUserShoppingListItem(updateUserShoppingListDto));
    }

    private List<UserShoppingListItem> buildUserShoppingListItem(UpdateUserShoppingListDto updateUserShoppingListDto) {
        HabitAssign habitAssign = habitAssignRepo.findById(updateUserShoppingListDto.getHabitAssignId())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID));
        List<UserShoppingListItem> userShoppingListItemList = new ArrayList<>();
        for (UserShoppingListItemAdvanceDto item : updateUserShoppingListDto.getUserShoppingListAdvanceDto()) {
            ShoppingListItem shoppingListItem = shoppingListItemRepo.findById(item.getShoppingListItemId())
                .orElseThrow(
                    () -> new ShoppingListItemNotFoundException(ErrorMessage.SHOPPING_LIST_ITEM_NOT_FOUND_BY_ID));
            userShoppingListItemList.add(UserShoppingListItem.builder()
                .habitAssign(habitAssign)
                .shoppingListItem(shoppingListItem)
                .status(item.getStatus())
                .id(updateUserShoppingListDto.getUserShoppingListItemId())
                .build());
        }
        return userShoppingListItemList;
    }

    /**
     * Method to get {@link HabitTranslation} for current habit assign and language.
     *
     * @param habitAssign {@link HabitAssign} habit assign.
     * @param language    {@link String} language code.
     */
    private HabitTranslation getHabitTranslation(HabitAssign habitAssign, String language) {
        return habitAssign.getHabit().getHabitTranslations().stream()
            .filter(ht -> ht.getLanguage().getCode().equals(language)).findFirst()
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_TRANSLATION_NOT_FOUND + habitAssign.getHabit().getId()));
    }

    /**
     * Method validates new {@link HabitAssign} to be created for current user.
     *
     * @param habitId {@link Habit} id.
     * @param user    {@link User} instance.
     */
    private void validateHabitForAssign(Long habitId, User user) {
        if (habitAssignRepo.countHabitAssignsByUserIdAndAcquiredFalseAndCancelledFalse(
            user.getId()) >= AppConstant.MAX_NUMBER_OF_HABIT_ASSIGNS_FOR_USER) {
            throw new UserAlreadyHasMaxNumberOfActiveHabitAssigns(
                ErrorMessage.USER_ALREADY_HAS_MAX_NUMBER_OF_HABIT_ASSIGNS
                    + AppConstant.MAX_NUMBER_OF_HABIT_ASSIGNS_FOR_USER);
        }
        if (habitAssignRepo.findByHabitIdAndUserIdAndCreateDate(
            habitId, user.getId(), ZonedDateTime.now()).isPresent()) {
            throw new UserAlreadyHasHabitAssignedException(
                ErrorMessage.USER_SUSPENDED_ASSIGNED_HABIT_FOR_CURRENT_DAY_ALREADY + habitId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitAssignDto findHabitAssignByUserIdAndHabitId(Long userId, Long habitId, String language) {
        HabitAssign habitAssign =
            habitAssignRepo.findByHabitIdAndUserId(habitId, userId)
                .orElseThrow(
                    () -> new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_WITH_CURRENT_USER_ID_AND_HABIT_ID
                        + habitId));
        return buildHabitAssignDto(habitAssign, language);
    }

    @Override
    public HabitDto findHabitByUserIdAndHabitId(Long userId, Long habitId, String language) {
        var habitAssign =
            habitAssignRepo.findByHabitIdAndUserId(habitId, userId)
                .orElseThrow(
                    () -> new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_WITH_CURRENT_USER_ID_AND_HABIT_ID
                        + habitId));
        var habitAssignDto = buildHabitAssignDto(habitAssign, language);
        HabitDto habit = habitAssignDto.getHabit();
        habit.setDefaultDuration(habitAssignDto.getDuration());
        List<ShoppingListItemDto> shoppingListItems = new ArrayList<>();
        shoppingListItemTranslationRepo
            .findShoppingListByHabitIdAndByLanguageCode(language, habitId)
            .forEach(x -> shoppingListItems.add(modelMapper.map(x, ShoppingListItemDto.class)));
        changeStatuses(ShoppingListItemStatus.INPROGRESS.toString(),
            habitAssign.getId(), shoppingListItems);
        changeStatuses(ShoppingListItemStatus.DONE.toString(),
            habitAssign.getId(), shoppingListItems);
        habit.setShoppingListItems(shoppingListItems);
        return habit;
    }

    /**
     * Method changes statuses in shoppingListItems.
     *
     * @param status            String status to set.
     * @param habitAssignId     Long id.
     * @param shoppingListItems list with habit's items.
     */
    private void changeStatuses(String status, Long habitAssignId,
        List<ShoppingListItemDto> shoppingListItems) {
        List<Long> otherStatusItems = userShoppingListItemRepo
            .getShoppingListItemsByHabitAssignIdAndStatus(habitAssignId, status);
        if (!otherStatusItems.isEmpty()) {
            for (Long otherStatusItemId : otherStatusItems) {
                for (ShoppingListItemDto slid : shoppingListItems) {
                    if (slid.getId().equals(otherStatusItemId)) {
                        slid.setStatus(status);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HabitAssignDto> getAllHabitAssignsByUserIdAndStatusNotCancelled(Long userId, String language) {
        return habitAssignRepo.findAllByUserId(userId)
            .stream().map(habitAssign -> buildHabitAssignDto(habitAssign, language)).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HabitAssignDto> getAllHabitAssignsByHabitIdAndStatusNotCancelled(Long habitId,
        String language) {
        return habitAssignRepo.findAllByHabitId(habitId)
            .stream().map(habitAssign -> buildHabitAssignDto(habitAssign, language)).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getNumberHabitAssignsByHabitIdAndStatus(Long habitId, HabitAssignStatus status) {
        List<HabitAssign> habitAssigns =
            habitAssignRepo.findAllHabitAssignsByStatusAndHabitId(status, habitId);
        return (long) habitAssigns.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HabitAssignDto> getAllHabitAssignsByUserIdAndStatusAcquired(Long userId, String language) {
        return habitAssignRepo.findAllByUserIdAndStatusAcquired(userId)
            .stream().map(habitAssign -> buildHabitAssignDtoContent(habitAssign, language))
            .collect(Collectors.toList());
    }

    /**
     * Method that return list of user shopping list and custom shopping list for
     * habit.
     *
     * @param userId   {@code User} id.
     * @param habitId  {@code Habit} id.
     * @param language {@link String} of language code value.
     * @return @{link UserShoppingAndCustomShoppingListsDto} instance.
     */
    @Override
    public UserShoppingAndCustomShoppingListsDto getUserShoppingListItemAndUserCustomShoppingList(
        Long userId, Long habitId, String language) {
        return UserShoppingAndCustomShoppingListsDto
            .builder()
            .userShoppingListItemDto(shoppingListItemService.getUserShoppingList(userId, habitId, language))
            .customShoppingListItemDto(customShoppingListItemService
                .findAllAvailableCustomShoppingListItems(userId, habitId))
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HabitAssignDto> getAllHabitAssignsByUserIdAndCancelledStatus(Long userId,
        String language) {
        return habitAssignRepo.findAllByUserIdAndStatusIsCancelled(userId)
            .stream().map(habitAssign -> buildHabitAssignDtoContent(habitAssign, language))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public HabitAssignManagementDto updateStatusByHabitIdAndUserId(Long habitId, Long userId,
        HabitAssignStatDto dto) {
        HabitAssign updatable = habitAssignRepo.findByHabitIdAndUserId(habitId, userId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_WITH_CURRENT_USER_ID_AND_HABIT_ID + habitId));

        updatable.setStatus(dto.getStatus());

        return modelMapper.map(habitAssignRepo.save(updatable), HabitAssignManagementDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void deleteAllHabitAssignsByHabit(HabitVO habit) {
        habitAssignRepo.findAllByHabitId(habit.getId())
            .forEach(habitAssign -> {
                HabitAssignVO habitAssignVO = modelMapper.map(habitAssign, HabitAssignVO.class);
                habitStatisticService.deleteAllStatsByHabitAssign(habitAssignVO);
                habitAssignRepo.delete(habitAssign);
            });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitAssignDto enrollHabit(Long habitId, Long userId, LocalDate dateTime, String language) {
        HabitAssign habitAssign = habitAssignRepo.findByHabitIdAndUserId(habitId, userId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_WITH_CURRENT_USER_ID_AND_HABIT_ID + habitId));

        validateForEnroll(dateTime, habitAssign);

        HabitStatusCalendar habitCalendar = HabitStatusCalendar.builder()
            .enrollDate(dateTime).habitAssign(habitAssign).build();

        updateHabitAssignAfterEnroll(habitAssign, habitCalendar, userId);
        return buildHabitAssignDto(habitAssign, "en");
    }

    /**
     * Method validates existed enrolls of {@link HabitAssign} for creating new one.
     *
     * @param habitAssign {@link HabitAssign} instance.
     * @param dateTime    {@link LocalDate} date.
     */
    private void validateForEnroll(LocalDate dateTime, HabitAssign habitAssign) {
        HabitAssignVO habitAssignVO = modelMapper.map(habitAssign, HabitAssignVO.class);
        HabitStatusCalendarVO habitCalendarVO =
            habitStatusCalendarService.findHabitStatusCalendarByEnrollDateAndHabitAssign(
                dateTime, habitAssignVO);
        if (habitCalendarVO != null) {
            throw new UserAlreadyHasEnrolledHabitAssign(ErrorMessage.HABIT_HAS_BEEN_ALREADY_ENROLLED);
        }

        LocalDate today = LocalDate.now();
        LocalDate lastDayToEnroll = today.minusDays(AppConstant.MAX_PASSED_DAYS_OF_ABILITY_TO_ENROLL);
        if (!(dateTime.isBefore(today.plusDays(1)) && dateTime.isAfter(lastDayToEnroll))) {
            throw new UserHasReachedOutOfEnrollRange(
                ErrorMessage.HABIT_STATUS_CALENDAR_OUT_OF_ENROLL_RANGE);
        }
    }

    /**
     * Method updates {@link HabitAssign} after enroll.
     *
     * @param habitAssign {@link HabitAssign} instance.
     */
    private void updateHabitAssignAfterEnroll(HabitAssign habitAssign,
        HabitStatusCalendar habitCalendar, Long userId) {
        habitAssign.setWorkingDays(habitAssign.getWorkingDays() + 1);
        habitAssign.setLastEnrollmentDate(ZonedDateTime.now());

        List<HabitStatusCalendar> habitStatusCalendars =
            new ArrayList<>(habitAssign.getHabitStatusCalendars());
        habitStatusCalendars.add(habitCalendar);
        habitAssign.setHabitStatusCalendars(habitStatusCalendars);

        int habitStreak = countNewHabitStreak(habitAssign.getHabitStatusCalendars());
        habitAssign.setHabitStreak(habitStreak);
        CompletableFuture.runAsync(() -> achievementCalculation
            .calculateAchievement(userId, AchievementType.COMPARISON,
                AchievementCategoryType.HABIT_STREAK, habitStreak));

        if (isHabitAcquired(habitAssign)) {
            habitAssign.setStatus(HabitAssignStatus.ACQUIRED);
            CompletableFuture.runAsync(() -> achievementCalculation
                .calculateAchievement(userId, AchievementType.INCREMENT, AchievementCategoryType.HABIT_STREAK, 0));
        }
        habitAssignRepo.save(habitAssign);
    }

    /**
     * Method checks if {@link HabitAssign} is completed.
     *
     * @param habitAssign {@link HabitAssign} instance.
     * @return boolean.
     */
    private boolean isHabitAcquired(HabitAssign habitAssign) {
        int workingDays = habitAssign.getWorkingDays();
        int habitDuration = habitAssign.getDuration();
        if (workingDays == habitDuration) {
            if (HabitAssignStatus.ACQUIRED.equals(habitAssign.getStatus())) {
                throw new BadRequestException(
                    ErrorMessage.HABIT_ALREADY_ACQUIRED + habitAssign.getHabit().getId());
            }
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitAssignDto unenrollHabit(Long habitId, Long userId, LocalDate date) {
        HabitAssign habitAssign = habitAssignRepo.findByHabitIdAndUserId(habitId, userId)
            .orElseThrow(
                () -> new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_WITH_CURRENT_USER_ID_AND_HABIT_ID
                    + userId + ", " + habitId));

        deleteHabitStatusCalendarIfExists(date, habitAssign);
        updateHabitAssignAfterUnenroll(habitAssign);

        return modelMapper.map(habitAssign, HabitAssignDto.class);
    }

    /**
     * Method checks and calls method for delete if enroll of {@link HabitAssign}
     * exists.
     *
     * @param date        {@link LocalDate} date.
     * @param habitAssign {@link HabitAssign} instance.
     */
    private void deleteHabitStatusCalendarIfExists(LocalDate date, HabitAssign habitAssign) {
        HabitStatusCalendarVO habitCalendarVO =
            habitStatusCalendarService
                .findHabitStatusCalendarByEnrollDateAndHabitAssign(
                    date, modelMapper.map(habitAssign, HabitAssignVO.class));
        deleteHabitStatusCalendar(habitAssign, habitCalendarVO);
    }

    /**
     * Method deletes enroll of {@link HabitAssign}.
     *
     * @param habitCalendarVO {@link HabitStatusCalendarVO} date.
     * @param habitAssign     {@link HabitAssign} instance.
     */
    private void deleteHabitStatusCalendar(HabitAssign habitAssign, HabitStatusCalendarVO habitCalendarVO) {
        if (habitCalendarVO != null) {
            List<HabitStatusCalendar> habitCalendars =
                new ArrayList<>(habitAssign.getHabitStatusCalendars());
            habitCalendars.removeIf(hc -> hc.getEnrollDate().isEqual(habitCalendarVO.getEnrollDate()));
            habitAssign.setHabitStatusCalendars(habitCalendars);
            habitStatusCalendarService.delete(habitCalendarVO);
        } else {
            throw new BadRequestException(ErrorMessage.HABIT_IS_NOT_ENROLLED);
        }
    }

    /**
     * Method updates {@link HabitAssign} after unenroll.
     *
     * @param habitAssign {@link HabitAssign} instance.
     */
    private void updateHabitAssignAfterUnenroll(HabitAssign habitAssign) {
        habitAssign.setWorkingDays(habitAssign.getWorkingDays() - 1);
        habitAssign.setHabitStreak(countNewHabitStreak(habitAssign.getHabitStatusCalendars()));

        habitAssignRepo.save(habitAssign);
    }

    /**
     * Method counts new habit streak for {@link HabitAssign}.
     *
     * @param habitCalendars {@link List} of {@link HabitStatusCalendar}'s.
     * @return int of habit days streak.
     */
    private int countNewHabitStreak(List<HabitStatusCalendar> habitCalendars) {
        habitCalendars.sort(Comparator.comparing(HabitStatusCalendar::getEnrollDate).reversed());

        LocalDate today = LocalDate.now();
        int daysStreak = 0;
        int daysPast = 0;
        for (HabitStatusCalendar hc : habitCalendars) {
            if (today.minusDays(daysPast++).equals(hc.getEnrollDate())) {
                daysStreak++;
            } else {
                return daysStreak;
            }
        }
        return daysStreak;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HabitAssignDto> findInprogressHabitAssignsOnDate(Long userId, LocalDate date, String language) {
        List<HabitAssign> list = habitAssignRepo.findAllInprogressHabitAssignsOnDate(userId, date);
        return list.stream().map(
            habitAssign -> buildHabitAssignDto(habitAssign, language)).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HabitAssignDto> findInprogressHabitAssignsOnDateContent(Long userId, LocalDate date, String language) {
        List<HabitAssign> list = habitAssignRepo.findAllInprogressHabitAssignsOnDate(userId, date);
        return list.stream().map(
            habitAssign -> buildHabitAssignDtoContent(habitAssign, language)).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HabitsDateEnrollmentDto> findHabitAssignsBetweenDates(Long userId, LocalDate from, LocalDate to,
        String language) {
        List<HabitAssign> habitAssignsBetweenDates = habitAssignRepo
            .findAllHabitAssignsBetweenDates(userId, from, to);
        List<LocalDate> dates = Stream.iterate(from, date -> date.plusDays(1))
            .limit(ChronoUnit.DAYS.between(from, to.plusDays(1)))
            .collect(Collectors.toList());
        List<HabitsDateEnrollmentDto> dtos = new ArrayList<>();
        for (LocalDate date : dates) {
            dtos.add(HabitsDateEnrollmentDto.builder().enrollDate(date)
                .habitAssigns(new ArrayList<>())
                .build());
        }
        habitAssignsBetweenDates.forEach(habitAssign -> buildHabitsDateEnrollmentDto(habitAssign, language, dtos));

        return dtos;
    }

    /**
     * Method to fill in all user enrollment activity in the list of
     * {@code HabitsDateEnrollmentDto}'s by {@code HabitAssign}'s list of habit
     * status calendar.
     *
     * @param habitAssign {@code HabitAssign} habit assign.
     * @param language    {@link String} of language code value.
     * @param list        of {@link HabitsDateEnrollmentDto} instances.
     */
    private void buildHabitsDateEnrollmentDto(HabitAssign habitAssign, String language,
        List<HabitsDateEnrollmentDto> list) {
        HabitTranslation habitTranslation = getHabitTranslation(habitAssign, language);

        for (HabitsDateEnrollmentDto dto : list) {
            if (checkIfHabitIsActiveOnDay(dto, habitAssign)) {
                markHabitOnHabitsEnrollmentDto(dto, checkIfHabitIsEnrolledOnDay(dto, habitAssign),
                    habitTranslation, habitAssign);
            }
        }
    }

    /**
     * Method to mark if habit was enrolled on concrete date.
     *
     * @param dto              {@link HabitsDateEnrollmentDto}.
     * @param isEnrolled       {@link boolean} shows if habit was enrolled.
     * @param habitTranslation {@link HabitTranslation} contains content.
     * @param habitAssign      {@link HabitAssign} contains habit id.
     */
    private void markHabitOnHabitsEnrollmentDto(HabitsDateEnrollmentDto dto, boolean isEnrolled,
        HabitTranslation habitTranslation, HabitAssign habitAssign) {
        dto.getHabitAssigns().add(HabitEnrollDto.builder()
            .habitDescription(habitTranslation.getDescription()).habitName(habitTranslation.getName())
            .isEnrolled(isEnrolled).habitId(habitAssign.getHabit().getId()).build());
    }

    /**
     * Method to check if {@code HabitAssign} was enrolled on concrete date.
     *
     * @param dto         {@link HabitsDateEnrollmentDto} which contains date.
     * @param habitAssign {@link HabitAssign} contains enroll dates.
     * @return boolean.
     */
    private boolean checkIfHabitIsEnrolledOnDay(HabitsDateEnrollmentDto dto, HabitAssign habitAssign) {
        for (int i = 0; i < habitAssign.getHabitStatusCalendars().size(); i++) {
            if (habitAssign.getHabitStatusCalendars().get(i).getEnrollDate()
                .equals(dto.getEnrollDate())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to check if {@code HabitAssign} is active on concrete date.
     *
     * @param dto         {@link HabitsDateEnrollmentDto} which contains date.
     * @param habitAssign {@link HabitAssign} contains habit date borders.
     * @return boolean.
     */
    private boolean checkIfHabitIsActiveOnDay(HabitsDateEnrollmentDto dto, HabitAssign habitAssign) {
        return dto.getEnrollDate()
            .isBefore(habitAssign.getCreateDate().toLocalDate().plusDays(habitAssign.getDuration() + 1L))
            && dto.getEnrollDate()
                .isAfter(habitAssign.getCreateDate().toLocalDate().minusDays(1L));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void addDefaultHabit(UserVO user, String language) {
        if (habitAssignRepo.findAllByUserId(user.getId()).isEmpty()) {
            UserVO userVO = modelMapper.map(user, UserVO.class);
            assignDefaultHabitForUser(1L, userVO);
        }
    }

    /**
     * Method to set {@link HabitAssign} status from inprogress to cancelled.
     *
     * @param habitId - id of {@link HabitVO}.
     * @param userId  - id of {@link UserVO}.
     * @return {@link HabitAssignDto}.
     */
    @Transactional
    @Override
    public HabitAssignDto cancelHabitAssign(Long habitId, Long userId) {
        HabitAssign habitAssignToCancel = habitAssignRepo.findByHabitIdAndUserIdAndStatusIsInprogress(habitId, userId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_WITH_CURRENT_USER_ID_AND_HABIT_ID_AND_INPROGRESS_STATUS + habitId));
        habitAssignToCancel.setStatus(HabitAssignStatus.CANCELLED);
        habitAssignRepo.save(habitAssignToCancel);
        return buildHabitAssignDto(habitAssignToCancel, "en");
    }

    /**
     * {@inheritDoc}
     */
    public void deleteHabitAssign(Long habitId, Long userId) {
        HabitAssign habitAssign = habitAssignRepo.findByUserIdAndHabitId(habitId, userId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_WITH_CURRENT_USER_ID_AND_HABIT_ID));
        userShoppingListItemRepo.deleteByShoppingListItemsByHabitAssignId(habitAssign.getId());
        habitAssignRepo.delete(habitAssign);
    }

    /**
     * Method update shopping item by habitAssign id and shoppingListItem id.
     *
     * @param habitAssignId      {@link Long} habit id.
     * @param shoppingListItemId {@link Long} item id.
     */
    public void updateShoppingItem(Long habitAssignId, Long shoppingListItemId) {
        Optional<UserShoppingListItem> optionalUserShoppingListItem =
            userShoppingListItemRepo.getAllAssignedShoppingListItemsFull(habitAssignId).stream()
                .filter(f -> f.getId().equals(shoppingListItemId)).findAny();
        if (optionalUserShoppingListItem.isPresent()) {
            UserShoppingListItem usli = optionalUserShoppingListItem.get();
            if (usli.getStatus().equals(ShoppingListItemStatus.INPROGRESS)) {
                usli.setStatus(ShoppingListItemStatus.ACTIVE);
            } else if (usli.getStatus().equals(ShoppingListItemStatus.ACTIVE)) {
                usli.setStatus(ShoppingListItemStatus.INPROGRESS);
            }
            userShoppingListItemRepo.save(usli);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void fullUpdateUserAndCustomShoppingLists(
        Long userId,
        Long habitId,
        UserShoppingAndCustomShoppingListsDto listsDto,
        String language) {
        fullUpdateUserShoppingList(userId, habitId, listsDto.getUserShoppingListItemDto(), language);
        fullUpdateCustomShoppingList(userId, habitId, listsDto.getCustomShoppingListItemDto());
    }

    /**
     * Method that update UserShoppingList.
     *
     * <ul>
     * <li>If items are present in the db, method update them;</li>
     * <li>If items don't present in the db and id is null, method try to add it to
     * user;</li>
     * <li>If some items from db don't present in the lists, method delete
     * them(Except items with DISABLED status).</li>
     * </ul>
     *
     * @param userId   {@code User} id.
     * @param habitId  {@code Habit} id.
     * @param list     {@link UserShoppingListItemResponseDto} User Shopping lists.
     * @param language {@link String} of language code value.
     */
    private void fullUpdateUserShoppingList(
        Long userId,
        Long habitId,
        List<UserShoppingListItemResponseDto> list,
        String language) {
        updateAndDeleteUserShoppingListWithStatuses(userId, habitId, list);
        saveUserShoppingListWithStatuses(userId, habitId, list, language);
    }

    /**
     * Method that save {@link UserShoppingListItemResponseDto} for item with id =
     * null.
     *
     * @param userId           {@code User} id.
     * @param habitId          {@code Habit} id.
     * @param userShoppingList {@link UserShoppingListItemResponseDto} User shopping
     *                         lists.
     * @param language         {@link String} of language code value.
     */
    private void saveUserShoppingListWithStatuses(
        Long userId,
        Long habitId,
        List<UserShoppingListItemResponseDto> userShoppingList,
        String language) {
        List<UserShoppingListItemResponseDto> listToSave = userShoppingList.stream()
            .filter(shoppingItem -> shoppingItem.getId() == null)
            .collect(Collectors.toList());
        checkDuplicationForUserShoppingListByName(listToSave);

        List<ShoppingListItem> shoppingListItems = findRelatedShoppingListItem(habitId, language, listToSave);

        Map<Long, ShoppingListItemStatus> shoppingItemIdToStatusMap =
            getShoppingItemIdToStatusMap(shoppingListItems, listToSave, language);

        List<ShoppingListItemRequestDto> listToSaveParam = shoppingListItems.stream()
            .map(shoppingItem -> ShoppingListItemWithStatusRequestDto.builder()
                .id(shoppingItem.getId())
                .status(shoppingItemIdToStatusMap.get(shoppingItem.getId()))
                .build())
            .collect(Collectors.toList());

        shoppingListItemService.saveUserShoppingListItems(userId, habitId, listToSaveParam, language);
    }

    private void checkDuplicationForUserShoppingListByName(List<UserShoppingListItemResponseDto> listToSave) {
        long countOfUnique = listToSave.stream()
            .map(UserShoppingListItemResponseDto::getText)
            .distinct()
            .count();
        if (listToSave.size() != countOfUnique) {
            throw new BadRequestException(ErrorMessage.DUPLICATED_USER_SHOPPING_LIST_ITEM);
        }
    }

    private List<ShoppingListItem> findRelatedShoppingListItem(
        Long habitId,
        String language,
        List<UserShoppingListItemResponseDto> listToSave) {
        if (listToSave.isEmpty()) {
            return List.of();
        }

        List<String> listToSaveNames = listToSave.stream()
            .map(UserShoppingListItemResponseDto::getText)
            .collect(Collectors.toList());

        List<ShoppingListItem> relatedShoppingListItems =
            shoppingListItemRepo.findByNames(habitId, listToSaveNames, language);

        if (listToSaveNames.size() != relatedShoppingListItems.size()) {
            List<String> relatedShoppingListItemNames = relatedShoppingListItems.stream()
                .map(x -> getShoppingItemNameByLanguageCode(x, language))
                .collect(Collectors.toList());

            listToSaveNames.removeAll(relatedShoppingListItemNames);

            String notFoundItems = String.join(", ", listToSaveNames);

            throw new NotFoundException(ErrorMessage.SHOPPING_LIST_ITEM_NOT_FOUND_BY_NAMES + notFoundItems);
        }
        return relatedShoppingListItems;
    }

    private Map<Long, ShoppingListItemStatus> getShoppingItemIdToStatusMap(
        List<ShoppingListItem> shoppingListItems,
        List<UserShoppingListItemResponseDto> listToSave,
        String language) {
        Map<String, ShoppingListItemStatus> shoppingItemNameToStatusMap =
            listToSave.stream()
                .collect(Collectors.toMap(
                    UserShoppingListItemResponseDto::getText,
                    UserShoppingListItemResponseDto::getStatus));

        return shoppingListItems.stream()
            .collect(Collectors.toMap(
                ShoppingListItem::getId,
                shoppingListItem -> shoppingItemNameToStatusMap
                    .get(getShoppingItemNameByLanguageCode(shoppingListItem, language))));
    }

    private String getShoppingItemNameByLanguageCode(ShoppingListItem shoppingItem, String language) {
        return shoppingItem.getTranslations()
            .stream()
            .filter(x -> x.getLanguage().getCode().equals(language))
            .findFirst()
            .orElseThrow()
            .getContent();
    }

    /**
     * Method that update or delete {@link UserShoppingListItem}. Not founded items,
     * except DISABLED, will be deleted.
     *
     * @param userId           {@code User} id.
     * @param habitId          {@code Habit} id.
     * @param userShoppingList {@link UserShoppingListItemResponseDto} User shopping
     *                         lists.
     */
    private void updateAndDeleteUserShoppingListWithStatuses(
        Long userId,
        Long habitId,
        List<UserShoppingListItemResponseDto> userShoppingList) {
        List<UserShoppingListItemResponseDto> listToUpdate = userShoppingList.stream()
            .filter(item -> item.getId() != null)
            .collect(Collectors.toList());

        checkDuplicationForUserShoppingListById(listToUpdate);

        HabitAssign habitAssign = habitAssignRepo.findByHabitIdAndUserId(habitId, userId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_WITH_CURRENT_USER_ID_AND_HABIT_ID + habitId));

        List<UserShoppingListItem> currentList = habitAssign.getUserShoppingListItems();

        checkIfUserShoppingItemsExist(listToUpdate, currentList);

        Map<Long, ShoppingListItemStatus> mapIdToStatus =
            listToUpdate.stream()
                .collect(Collectors.toMap(
                    UserShoppingListItemResponseDto::getId,
                    UserShoppingListItemResponseDto::getStatus));

        List<UserShoppingListItem> listToSave = new ArrayList<>();
        List<UserShoppingListItem> listToDelete = new ArrayList<>();
        for (var currentItem : currentList) {
            ShoppingListItemStatus newStatus = mapIdToStatus.get(currentItem.getId());
            if (newStatus != null) {
                currentItem.setStatus(newStatus);
                listToSave.add(currentItem);
            } else {
                if (!currentItem.getStatus().equals(ShoppingListItemStatus.DISABLED)) {
                    listToDelete.add(currentItem);
                }
            }
        }
        userShoppingListItemRepo.saveAll(listToSave);
        userShoppingListItemRepo.deleteAll(listToDelete);
    }

    private void checkDuplicationForUserShoppingListById(List<UserShoppingListItemResponseDto> listToUpdate) {
        long countOfUnique = listToUpdate.stream()
            .map(UserShoppingListItemResponseDto::getId)
            .distinct()
            .count();
        if (listToUpdate.size() != countOfUnique) {
            throw new BadRequestException(ErrorMessage.DUPLICATED_USER_SHOPPING_LIST_ITEM);
        }
    }

    private void checkIfUserShoppingItemsExist(
        List<UserShoppingListItemResponseDto> listToUpdate,
        List<UserShoppingListItem> currentList) {
        List<Long> updateIds =
            listToUpdate.stream().map(UserShoppingListItemResponseDto::getId).collect(Collectors.toList());
        List<Long> currentIds = currentList.stream().map(UserShoppingListItem::getId).collect(Collectors.toList());

        updateIds.removeAll(currentIds);

        if (!updateIds.isEmpty()) {
            String notFoundedIds = updateIds.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
            throw new NotFoundException(ErrorMessage.USER_SHOPPING_LIST_ITEM_NOT_FOUND + notFoundedIds);
        }
    }

    /**
     * Method that update CustomShopping List.
     *
     * <ul>
     * <li>If items are present in the db, method update them;</li>
     * <li>If items don't present in the db and id is null, method try to add it to
     * user;</li>
     * <li>If some items from db don't present in the lists, method delete
     * them(Except items with DISABLED status).</li>
     * </ul>
     *
     * @param userId  {@code User} id.
     * @param habitId {@code Habit} id.
     * @param list    {@link CustomShoppingListItemResponseDto} Custom Shopping
     *                lists.
     */
    private void fullUpdateCustomShoppingList(
        Long userId,
        Long habitId,
        List<CustomShoppingListItemResponseDto> list) {
        updateAndDeleteCustomShoppingListWithStatuses(userId, habitId, list);
        saveCustomShoppingListWithStatuses(userId, habitId, list);
    }

    /**
     * Method that save {@link CustomShoppingListItemResponseDto} for item with id =
     * null.
     *
     * @param userId             {@code User} id.
     * @param habitId            {@code Habit} id.
     * @param customShoppingList {@link CustomShoppingListItemResponseDto} Custom
     *                           shopping lists.
     */
    private void saveCustomShoppingListWithStatuses(
        Long userId,
        Long habitId,
        List<CustomShoppingListItemResponseDto> customShoppingList) {
        List<CustomShoppingListItemResponseDto> listToSave = customShoppingList.stream()
            .filter(shoppingItem -> shoppingItem.getId() == null)
            .collect(Collectors.toList());

        checkDuplicationForCustomShoppingListByName(listToSave);

        List<CustomShoppingListItemSaveRequestDto> listToSaveParam = listToSave.stream()
            .map(item -> CustomShoppingListItemWithStatusSaveRequestDto.builder()
                .text(item.getText())
                .status(item.getStatus())
                .build())
            .collect(Collectors.toList());

        customShoppingListItemService.save(new BulkSaveCustomShoppingListItemDto(listToSaveParam), userId, habitId);
    }

    private void checkDuplicationForCustomShoppingListByName(List<CustomShoppingListItemResponseDto> listToSave) {
        long countOfUnique = listToSave.stream()
            .map(CustomShoppingListItemResponseDto::getText)
            .distinct()
            .count();
        if (listToSave.size() != countOfUnique) {
            throw new BadRequestException(ErrorMessage.DUPLICATED_CUSTOM_SHOPPING_LIST_ITEM);
        }
    }

    /**
     * Method that update or delete {@link CustomShoppingListItem}. Not founded
     * items, except DISABLED, will be deleted.
     *
     * @param userId             {@code User} id.
     * @param habitId            {@code Habit} id.
     * @param customShoppingList {@link CustomShoppingListItemResponseDto} Custom
     *                           shopping lists.
     */
    private void updateAndDeleteCustomShoppingListWithStatuses(
        Long userId,
        Long habitId,
        List<CustomShoppingListItemResponseDto> customShoppingList) {
        List<CustomShoppingListItemResponseDto> listToUpdate = customShoppingList.stream()
            .filter(shoppingItem -> shoppingItem.getId() != null)
            .collect(Collectors.toList());

        checkDuplicationForCustomShoppingListById(listToUpdate);

        List<CustomShoppingListItem> currentList =
            customShoppingListItemRepo.findAllByUserIdAndHabitId(userId, habitId);

        checkIfCustomShoppingItemsExist(listToUpdate, currentList);

        Map<Long, ShoppingListItemStatus> mapIdToStatus =
            listToUpdate.stream()
                .collect(Collectors.toMap(
                    CustomShoppingListItemResponseDto::getId,
                    CustomShoppingListItemResponseDto::getStatus));

        List<CustomShoppingListItem> listToSave = new ArrayList<>();
        List<CustomShoppingListItem> listToDelete = new ArrayList<>();
        for (var currentItem : currentList) {
            ShoppingListItemStatus newStatus = mapIdToStatus.get(currentItem.getId());
            if (newStatus != null) {
                currentItem.setStatus(newStatus);
                listToSave.add(currentItem);
            } else {
                if (!currentItem.getStatus().equals(ShoppingListItemStatus.DISABLED)) {
                    listToDelete.add(currentItem);
                }
            }
        }
        customShoppingListItemRepo.saveAll(listToSave);
        customShoppingListItemRepo.deleteAll(listToDelete);
    }

    private void checkDuplicationForCustomShoppingListById(List<CustomShoppingListItemResponseDto> listToUpdate) {
        long countOfUnique = listToUpdate.stream()
            .map(CustomShoppingListItemResponseDto::getId)
            .distinct()
            .count();
        if (listToUpdate.size() != countOfUnique) {
            throw new BadRequestException(ErrorMessage.DUPLICATED_CUSTOM_SHOPPING_LIST_ITEM);
        }
    }

    private void checkIfCustomShoppingItemsExist(
        List<CustomShoppingListItemResponseDto> listToUpdate,
        List<CustomShoppingListItem> currentList) {
        List<Long> updateIds =
            listToUpdate.stream().map(CustomShoppingListItemResponseDto::getId).collect(Collectors.toList());
        List<Long> currentIds = currentList.stream().map(CustomShoppingListItem::getId).collect(Collectors.toList());

        updateIds.removeAll(currentIds);

        if (!updateIds.isEmpty()) {
            String notFoundedIds = updateIds.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
            throw new NotFoundException(ErrorMessage.CUSTOM_SHOPPING_LIST_ITEM_WITH_THIS_ID_NOT_FOUND + notFoundedIds);
        }
    }
}
