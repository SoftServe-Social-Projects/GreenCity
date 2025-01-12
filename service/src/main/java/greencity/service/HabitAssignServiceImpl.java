package greencity.service;

import greencity.achievement.AchievementCalculation;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.habit.HabitAssignCustomPropertiesDto;
import greencity.dto.habit.HabitAssignDto;
import greencity.dto.habit.HabitAssignManagementDto;
import greencity.dto.habit.HabitAssignPreviewDto;
import greencity.dto.habit.HabitAssignPropertiesDto;
import greencity.dto.habit.HabitAssignStatDto;
import greencity.dto.habit.HabitAssignUserDurationDto;
import greencity.dto.habit.HabitAssignVO;
import greencity.dto.habit.HabitDto;
import greencity.dto.habit.HabitEnrollDto;
import greencity.dto.habit.HabitVO;
import greencity.dto.habit.HabitWorkingDaysDto;
import greencity.dto.habit.HabitsDateEnrollmentDto;
import greencity.dto.habitstatuscalendar.HabitStatusCalendarVO;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.todolistitem.CustomToDoListItemSaveRequestDto;
import greencity.dto.todolistitem.ToDoListItemResponseDto;
import greencity.dto.user.UserToDoListItemAdvanceDto;
import greencity.dto.user.UserToDoListItemResponseDto;
import greencity.dto.user.UserVO;
import greencity.entity.CustomToDoListItem;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.HabitInvitation;
import greencity.entity.HabitStatusCalendar;
import greencity.entity.HabitTranslation;
import greencity.entity.ToDoListItem;
import greencity.entity.User;
import greencity.entity.UserToDoListItem;
import greencity.entity.localization.ToDoListItemTranslation;
import greencity.enums.AchievementAction;
import greencity.enums.AchievementCategoryType;
import greencity.enums.HabitAssignStatus;
import greencity.enums.HabitInvitationStatus;
import greencity.enums.Role;
import greencity.enums.UserToDoListItemStatus;
import greencity.enums.NotificationType;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.CustomToDoListItemNotSavedException;
import greencity.exception.exceptions.InvalidStatusException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserAlreadyHasEnrolledHabitAssign;
import greencity.exception.exceptions.UserAlreadyHasHabitAssignedException;
import greencity.exception.exceptions.UserAlreadyHasMaxNumberOfActiveHabitAssigns;
import greencity.exception.exceptions.UserHasNoFriendWithIdException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.exception.exceptions.UserHasReachedOutOfEnrollRange;
import greencity.rating.RatingCalculation;
import greencity.repository.CustomToDoListItemRepo;
import greencity.repository.HabitAssignRepo;
import greencity.repository.HabitInvitationRepo;
import greencity.repository.HabitRepo;
import greencity.repository.HabitStatusCalendarRepo;
import greencity.repository.ToDoListItemRepo;
import greencity.repository.ToDoListItemTranslationRepo;
import greencity.repository.UserRepo;
import greencity.repository.UserToDoListItemRepo;
import greencity.repository.RatingPointsRepo;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Implementation of {@link HabitAssignService}.
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class HabitAssignServiceImpl implements HabitAssignService {
    private final HabitAssignRepo habitAssignRepo;
    private final HabitRepo habitRepo;
    private final UserRepo userRepo;
    private final ToDoListItemRepo toDoListItemRepo;
    private final UserToDoListItemRepo userToDoListItemRepo;
    private final CustomToDoListItemRepo customToDoListItemRepo;
    private final ToDoListItemTranslationRepo toDoListItemTranslationRepo;
    private final HabitStatusCalendarRepo habitStatusCalendarRepo;
    private final HabitStatisticService habitStatisticService;
    private final HabitStatusCalendarService habitStatusCalendarService;
    private final AchievementCalculation achievementCalculation;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final RatingCalculation ratingCalculation;
    private final UserNotificationService userNotificationService;
    private final RatingPointsRepo ratingPointsRepo;
    private final HabitInvitationRepo habitInvitationRepo;
    private final HabitInvitationService habitInvitationService;

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitAssignDto getByHabitAssignIdAndUserId(Long habitAssignId, Long userId, String language) {
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId));

        if (!habitAssign.getUser().getId().equals(userId)) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }

        HabitAssignDto habitAssignDto = buildHabitAssignDto(habitAssign, language);
        HabitDto habitDto = habitAssignDto.getHabit();
        Long amountAcquiredUsers = habitAssignRepo.findAmountOfUsersAcquired(habitDto.getId());
        habitDto.setAmountAcquiredUsers(amountAcquiredUsers);
        habitDto.setUsersIdWhoCreatedCustomHabit(habitAssign.getHabit().getUserId());
        habitAssignDto.setHabit(habitDto);
        habitAssignDto.setProgressNotificationHasDisplayed(habitAssign.getProgressNotificationHasDisplayed());

        return habitAssignDto;
    }

    /**
     * {@inheritDoc}
     */

    @Transactional
    @Override
    public HabitAssignManagementDto assignDefaultHabitForUser(Long habitId, UserVO userVO) {
        Habit habit = habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId));
        HabitAssignCustomPropertiesDto habitAssignCustomPropertiesDto = buildDefaultHabitAssignPropertiesDto(habit);
        HabitAssign habitAssign = assignHabitForUser(habit, userVO, habitAssignCustomPropertiesDto);

        HabitAssignManagementDto habitAssignManagementDto =
            modelMapper.map(habitAssign, HabitAssignManagementDto.class);
        habitAssignManagementDto.setProgressNotificationHasDisplayed(habitAssign.getProgressNotificationHasDisplayed());

        return habitAssignManagementDto;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public List<HabitAssignManagementDto> assignCustomHabitForUser(Long habitId, UserVO userVO,
        HabitAssignCustomPropertiesDto habitAssignCustomPropertiesDto) {
        Habit habit = habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId));
        HabitAssign habitAssign = assignHabitForUser(habit, userVO, habitAssignCustomPropertiesDto);

        List<HabitAssignManagementDto> habitAssignManagementDtoList = new ArrayList<>();
        habitAssignManagementDtoList.add(modelMapper.map(habitAssign, HabitAssignManagementDto.class));

        if (!CollectionUtils.isEmpty(habitAssignCustomPropertiesDto.getFriendsIdsList())) {
            assignFriendsForCustomHabit(habit, userVO.getId(), habitAssignCustomPropertiesDto,
                habitAssignManagementDtoList);
        }

        return habitAssignManagementDtoList;
    }

    private void saveCustomToDoListItemsForCurrentUser(List<CustomToDoListItemSaveRequestDto> saveList,
        User user, Habit habit, HabitAssign habitAssign) {
        if (!CollectionUtils.isEmpty(saveList)) {
            saveList.forEach(item -> {
                CustomToDoListItem customToDoListItem = modelMapper.map(item, CustomToDoListItem.class);
                List<CustomToDoListItem> duplicates = user.getCustomToDoListItems().stream()
                    .filter(userItem -> userItem.getText().equals(customToDoListItem.getText())
                        && userItem.getHabit().getId().equals(habit.getId()))
                    .toList();
                if (duplicates.isEmpty()) {
                    customToDoListItem.setUser(user);
                    customToDoListItem.setHabit(habit);
                    customToDoListItem.setIsDefault(false);
                    user.getCustomToDoListItems().add(customToDoListItem);
                    customToDoListItemRepo.save(customToDoListItem);
                } else {
                    throw new CustomToDoListItemNotSavedException(String.format(
                        ErrorMessage.CUSTOM_TO_DO_LIST_ITEM_EXISTS, customToDoListItem.getText()));
                }
            });
            List<Long> customToDoListByUser = customToDoListItemRepo
                .getAllCustomToDoListItemIdByUserIdAndByHabitIdAndNotDefault(habit.getId(), user.getId());
            saveUserToDoListItems(customToDoListByUser, habitAssign, true);
        }
    }

    private void assignFriendsForCustomHabit(Habit habit,
        Long userId,
        HabitAssignCustomPropertiesDto habitAssignCustomPropertiesDto,
        List<HabitAssignManagementDto> habitAssignManagementDtoList) {
        List<User> usersWhoShouldBeFriendList = getUsersByIds(habitAssignCustomPropertiesDto.getFriendsIdsList());

        for (User friendOfUser : usersWhoShouldBeFriendList) {
            if (!userRepo.isFriend(userId, friendOfUser.getId())) {
                throw new UserHasNoFriendWithIdException(
                    ErrorMessage.USER_HAS_NO_FRIEND_WITH_ID + friendOfUser.getId());
            }

            HabitAssign habitAssign =
                createHabitAssign(friendOfUser, habit, habitAssignCustomPropertiesDto, HabitAssignStatus.REQUESTED);
            habitAssignManagementDtoList.add(modelMapper.map(habitAssign, HabitAssignManagementDto.class));
        }
    }

    private void checkStatusInProgressExists(Long habitId, Long userId) {
        List<HabitAssign> habits = habitAssignRepo.findAllByUserId(userId);
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
            .map(habitAssign -> buildHabitAssignDtoContent(habitAssign, language)).toList();
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
        List<UserToDoListItem> toDoListItems = habitAssign.getUserToDoListItems();
        return !duration.equals(defaultDuration) && !toDoListItems.isEmpty();
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
     * Method which updates duration of habit assigned for user.
     *
     * @param habitAssignId {@code AssignHabit} id.
     * @param userId        {@link Long} id.
     * @param duration      {@link Integer} with needed duration.
     * @return {@link HabitAssignUserDurationDto}.
     */
    @Transactional
    @Override
    public HabitAssignUserDurationDto updateUserHabitInfoDuration(Long habitAssignId, Long userId, Integer duration) {
        if (!habitAssignRepo.existsById(habitAssignId)) {
            throw new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitAssignId);
        }
        HabitAssign habitAssign = habitAssignRepo.findByHabitAssignIdUserIdAndStatusIsInProgress(habitAssignId, userId)
            .orElseThrow(() -> new InvalidStatusException(
                ErrorMessage.HABIT_ASSIGN_STATUS_IS_NOT_INPROGRESS_OR_USER_HAS_NOT_ANY_ASSIGNED_HABITS));
        if (duration < habitAssign.getWorkingDays()) {
            throw new BadRequestException(ErrorMessage.INVALID_DURATION);
        }

        habitAssign.setDuration(duration);
        return modelMapper.map(habitAssignRepo.save(habitAssign), HabitAssignUserDurationDto.class);
    }

    private void saveUserToDoListItems(List<Long> toDoListIds, HabitAssign habitAssign, boolean isCustomItems) {
        if (!toDoListIds.isEmpty()) {
            List<UserToDoListItem> userToDoList = new ArrayList<>();
            for (Long itemId : toDoListIds) {
                userToDoList.add(UserToDoListItem.builder()
                    .habitAssign(habitAssign)
                    .targetId(itemId)
                    .status(UserToDoListItemStatus.INPROGRESS)
                    .isCustomItem(isCustomItems)
                    .build());
            }
            userToDoListItemRepo.saveAll(userToDoList);
        }
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
        habitAssign.setIsPrivate(props.getIsPrivate());
    }

    /**
     * Method builds {@link HabitAssign} with main props.
     *
     * @param habit        {@link Habit} instance.
     * @param user         {@link User} instance.
     * @param assignStatus {@link HabitAssignStatus} instance.
     * @return {@link HabitAssign} instance.
     */
    private HabitAssign buildHabitAssign(Habit habit, User user, HabitAssignStatus assignStatus) {
        return HabitAssign.builder()
            .habit(habit)
            .status(assignStatus)
            .createDate(ZonedDateTime.now())
            .user(user)
            .duration(habit.getDefaultDuration())
            .habitStreak(0)
            .workingDays(0)
            .lastEnrollmentDate(ZonedDateTime.now())
            .progressNotificationHasDisplayed(false)
            .build();
    }

    /**
     * Method builds {@link HabitAssignDto} with one habit translation.
     *
     * @param habitAssign {@link HabitAssign} instance.
     * @param language    code of language.
     * @return {@link HabitAssign} instance.
     */
    private HabitAssignDto buildHabitAssignDto(HabitAssign habitAssign, String language) {
        HabitTranslation habitTranslation = getHabitTranslation(habitAssign, language);
        HabitAssignDto habitAssignDto = modelMapper.map(habitAssign, HabitAssignDto.class);
        habitAssignDto.setHabit(modelMapper.map(habitTranslation, HabitDto.class));
        habitAssignDto.setFriendsIdsTrackingHabit(getFriendsIdsTrackingHabitList(habitAssign));
        setToDoListItems(habitAssignDto, habitAssign, language);
        setCustomToDoListItems(habitAssignDto, habitAssign);
        setUserToDoListItems(habitAssignDto, habitAssign, language);
        setLikes(habitAssignDto, habitAssign);
        setDislikes(habitAssignDto, habitAssign);
        return habitAssignDto;
    }

    /**
     * Method builds {@link HabitAssignDto} with one habit translation.
     *
     * @param habitAssign {@link HabitAssign} instance.
     * @param language    code of language.
     * @return {@link HabitAssign} instance.
     */
    private HabitAssignDto buildHabitAssignDto(HabitAssign habitAssign, String language, Long userId) {
        HabitTranslation habitTranslation = getHabitTranslation(habitAssign, language);
        HabitAssignDto habitAssignDto = modelMapper.map(habitAssign, HabitAssignDto.class);
        habitAssignDto.setHabit(modelMapper.map(habitTranslation, HabitDto.class));
        habitAssignDto.setFriendsIdsTrackingHabit(
            habitInvitationService.getInvitedFriendsIdsTrackingHabitList(userId, habitAssign.getHabit().getId()));
        setToDoListItems(habitAssignDto, habitAssign, language);
        setCustomToDoListItems(habitAssignDto, habitAssign);
        setUserToDoListItems(habitAssignDto, habitAssign, language);
        return habitAssignDto;
    }

    private List<Long> getFriendsIdsTrackingHabitList(HabitAssign habitAssign) {
        return habitAssignRepo
            .findFriendsIdsTrackingHabit(habitAssign.getHabit().getId(), habitAssign.getUser().getId());
    }

    private void setToDoListItems(HabitAssignDto habitAssignDto, HabitAssign habitAssign, String language) {
        habitAssignDto.getHabit()
            .setToDoListItems(toDoListItemRepo.getAllToDoListItemIdByHabitIdIsContained(habitAssign.getHabit().getId())
                .stream()
                .map(toDoListItemId -> {
                    ToDoListItem toDoListItem =
                        toDoListItemRepo.getReferenceById(toDoListItemId);
                    return ToDoListItemResponseDto.builder()
                        .id(toDoListItem.getId())
                        .text(toDoListItem.getTranslations().stream()
                            .filter(toDoItem -> toDoItem.getLanguage().getCode().equals(language)).findFirst()
                            .orElseThrow(
                                () -> new NotFoundException(
                                    ErrorMessage.TO_DO_LIST_ITEM_TRANSLATION_NOT_FOUND
                                        + habitAssignDto.getHabit().getId()))
                            .getContent())
                        .build();
                })
                .toList());
    }

    private void setCustomToDoListItems(HabitAssignDto habitAssignDto, HabitAssign habitAssign) {
        habitAssignDto.getHabit().setCustomToDoListItems(
            customToDoListItemRepo.getAllCustomToDoListItemIdByHabitIdIsContained(habitAssign.getHabit().getId())
                .stream()
                .map(customToDoListItemId -> {
                    CustomToDoListItem customToDoListItem =
                        customToDoListItemRepo.getReferenceById(customToDoListItemId);
                    return CustomToDoListItemResponseDto.builder()
                        .id(customToDoListItem.getId())
                        .text(customToDoListItem.getText())
                        .build();
                })
                .toList());
    }

    private void setUserToDoListItems(HabitAssignDto habitAssignDto, HabitAssign habitAssign, String language) {
        List<UserToDoListItem> userToDoListItems = userToDoListItemRepo.findAllByHabitAssingId(habitAssign.getId());
        List<UserToDoListItemAdvanceDto> userToDoListItemDtos = userToDoListItems.stream()
            .map(userToDoListItem -> {
                UserToDoListItemAdvanceDto itemDto = UserToDoListItemAdvanceDto.builder()
                    .id(userToDoListItem.getId())
                    .targetId(userToDoListItem.getTargetId())
                    .isCustomItem(userToDoListItem.getIsCustomItem())
                    .status(userToDoListItem.getStatus())
                    .dateCompleted(userToDoListItem.getDateCompleted())
                    .build();
                boolean isCustomItem = userToDoListItem.getIsCustomItem();
                if (isCustomItem) {
                    CustomToDoListItem customItem =
                        customToDoListItemRepo.getReferenceById(userToDoListItem.getTargetId());
                    itemDto.setContent(customItem.getText());
                } else {
                    ToDoListItemTranslation itemTranslation =
                        toDoListItemTranslationRepo.findByLangAndToDoListItemId(language, itemDto.getTargetId());
                    itemDto.setContent(itemTranslation.getContent());
                }
                return itemDto;
            })
            .toList();
        habitAssignDto.setUserToDoListItems(userToDoListItemDtos);
    }

    private HabitAssignDto buildHabitAssignDtoContent(HabitAssign habitAssign, String language) {
        HabitAssignDto habitAssignDto = buildHabitAssignDto(habitAssign, language);
        habitAssignDto.setUserToDoListItems(buildUserToDoListItemAdvanceDto(habitAssign, language));
        return habitAssignDto;
    }

    private List<UserToDoListItemAdvanceDto> buildUserToDoListItemAdvanceDto(HabitAssign habitAssign,
        String language) {
        List<UserToDoListItemAdvanceDto> userItemsDTO = new ArrayList<>();
        userItemsDTO.addAll(buildUserToDoListItemAdvanceDtoForToDoList(habitAssign, language));
        userItemsDTO.addAll(buildUserToDoListItemAdvanceDtoForCustomToDoList(habitAssign));
        return userItemsDTO;
    }

    private List<UserToDoListItemAdvanceDto> buildUserToDoListItemAdvanceDtoForToDoList(HabitAssign habitAssign,
        String language) {
        List<UserToDoListItemAdvanceDto> userItemList = new ArrayList<>();
        List<ToDoListItemTranslation> listItemTranslations = toDoListItemTranslationRepo
            .findToDoListByHabitIdAndByLanguageCode(language, habitAssign.getHabit().getId());
        for (ToDoListItemTranslation translationItem : listItemTranslations) {
            boolean isContains = false;
            for (UserToDoListItem userItem : habitAssign.getUserToDoListItems()) {
                boolean isCustomItem = userItem.getIsCustomItem();
                if (translationItem.getToDoListItem().getId().equals(userItem.getTargetId())
                    && !isCustomItem) {
                    userItemList.add(UserToDoListItemAdvanceDto.builder()
                        .id(userItem.getId())
                        .targetId(userItem.getTargetId())
                        .isCustomItem(false)
                        .status(userItem.getStatus())
                        .dateCompleted(userItem.getDateCompleted())
                        .content(translationItem.getContent())
                        .build());
                    isContains = true;
                    break;
                }
            }
            if (!isContains) {
                userItemList.add(UserToDoListItemAdvanceDto.builder()
                    .targetId(translationItem.getToDoListItem().getId())
                    .isCustomItem(false)
                    .status(UserToDoListItemStatus.DISABLED)
                    .content(translationItem.getContent())
                    .build());
            }
        }
        return userItemList;
    }

    private List<UserToDoListItemAdvanceDto> buildUserToDoListItemAdvanceDtoForCustomToDoList(HabitAssign habitAssign) {
        List<UserToDoListItemAdvanceDto> userItemList = new ArrayList<>();
        List<Long> allAvailableForCurrentAssign =
            customToDoListItemRepo.getAllCustomToDoListItemIdByHabitIdIsContained(habitAssign.getHabit().getId());
        for (Long customItemId : allAvailableForCurrentAssign) {
            boolean isContains = false;
            CustomToDoListItem customItem = customToDoListItemRepo.findById(customItemId)
                .orElseThrow(
                    () -> new NotFoundException(ErrorMessage.CUSTOM_TO_DO_LIST_ITEM_NOT_FOUND_BY_ID + customItemId));
            for (UserToDoListItem userItem : habitAssign.getUserToDoListItems()) {
                boolean isCustomItem = userItem.getIsCustomItem();
                if (customItemId.equals(userItem.getTargetId()) && isCustomItem) {
                    userItemList.add(UserToDoListItemAdvanceDto.builder()
                        .id(userItem.getId())
                        .targetId(userItem.getTargetId())
                        .isCustomItem(true)
                        .status(userItem.getStatus())
                        .dateCompleted(userItem.getDateCompleted())
                        .content(customItem.getText())
                        .build());
                    isContains = true;
                    break;
                }
            }
            if (!isContains) {
                userItemList.add(UserToDoListItemAdvanceDto.builder()
                    .targetId(customItem.getId())
                    .isCustomItem(true)
                    .status(UserToDoListItemStatus.DISABLED)
                    .content(customItem.getText())
                    .build());
            }
        }
        return userItemList;
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
    public HabitDto findHabitByUserIdAndHabitAssignId(Long userId, Long habitAssignId, String language) {
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId));

        if (!habitAssign.getUser().getId().equals(userId)) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }

        var habitAssignDto = buildHabitAssignDto(habitAssign, language);
        HabitDto habit = habitAssignDto.getHabit();
        habit.setDefaultDuration(habitAssignDto.getDuration());
        habit.setAmountAcquiredUsers(habitAssignRepo.findAmountOfUsersAcquired(habit.getId()));
        habit.setHabitAssignStatus(habitAssign.getStatus());
        return habit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HabitAssignDto> getAllHabitAssignsByUserIdAndStatusNotCancelled(Long userId, String language) {
        return habitAssignRepo.findAllByUserId(userId)
            .stream().map(habitAssign -> buildHabitAssignDto(habitAssign, language, userId))
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableAdvancedDto<HabitAssignPreviewDto> getAllMutualHabitAssignsWithUserAndStatusNotCancelled(
        Long userId, Long currentUserId, Pageable pageable) {
        Page<HabitAssign> returnedPage = habitAssignRepo.findAllMutual(userId, currentUserId, pageable);
        return mapHabitAssignPageToPageableAdvancedDtoOfMutualHabitAssignDto(returnedPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableAdvancedDto<HabitAssignPreviewDto> getMyHabitsOfCurrentUserAndStatusNotCancelled(
        Long userId, Long currentUserId, Pageable pageable) {
        Page<HabitAssign> returnedPage = habitAssignRepo.findAllOfCurrentUser(userId, currentUserId, pageable);
        return mapHabitAssignPageToPageableAdvancedDtoOfMutualHabitAssignDto(returnedPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HabitAssignDto> getAllHabitAssignsByHabitIdAndStatusNotCancelled(Long habitId,
        String language) {
        return habitAssignRepo.findAllByHabitId(habitId)
            .stream().map(habitAssign -> buildHabitAssignDto(habitAssign, language)).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableAdvancedDto<HabitAssignPreviewDto> getAllByUserIdAndStatusNotCancelled(Long userId,
        Pageable pageable) {
        Page<HabitAssign> returnedPage = habitAssignRepo.findAllByUserId(userId, pageable);
        return mapHabitAssignPageToPageableAdvancedDtoOfMutualHabitAssignDto(returnedPage);
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
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserToDoListItemResponseDto> getToDoAndCustomToDoLists(
        Long userId, Long habitAssignId, String language) {
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId));

        UserVO userVO = userService.findById(userId);
        if (!habitAssign.getUser().getId().equals(userId) && !userVO.getRole().equals(Role.ROLE_ADMIN)) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }
        return userToDoListItemRepo.findAllByHabitAssingId(habitAssignId)
            .stream()
            .map(userToDoListItem -> {
                UserToDoListItemResponseDto itemDto =
                    modelMapper.map(userToDoListItem, UserToDoListItemResponseDto.class);
                boolean isCustomItem = userToDoListItem.getIsCustomItem();
                if (isCustomItem) {
                    CustomToDoListItem customItem =
                        customToDoListItemRepo.getReferenceById(userToDoListItem.getTargetId());
                    itemDto.setText(customItem.getText());
                } else {
                    ToDoListItemTranslation itemTranslation = toDoListItemTranslationRepo
                        .findByLangAndToDoListItemId(language, userToDoListItem.getTargetId());
                    itemDto.setText(itemTranslation.getContent());
                }
                return itemDto;
            }).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public List<UserToDoListItemResponseDto> getListOfUserToDoListsWithStatusInprogress(
        Long userId, String language) {
        List<HabitAssign> habitAssignList = habitAssignRepo.findAllByUserIdAndStatusIsInProgress(userId);
        if (habitAssignList.isEmpty()) {
            throw new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_WITH_CURRENT_USER_ID_AND_INPROGRESS_STATUS + userId);
        }
        return habitAssignList.stream()
            .map(HabitAssign::getUserToDoListItems)
            .map(userToDoListItem -> modelMapper.map(userToDoListItem, UserToDoListItemResponseDto.class))
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HabitAssignDto> getAllHabitAssignsByUserIdAndCancelledStatus(Long userId,
        String language) {
        return habitAssignRepo.findAllByUserIdAndStatusIsCancelled(userId)
            .stream().map(habitAssign -> buildHabitAssignDtoContent(habitAssign, language))
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public HabitAssignManagementDto updateStatusByHabitAssignId(Long habitAssignId,
        HabitAssignStatDto dto) {
        HabitAssign updatable = habitAssignRepo.findById(habitAssignId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_WITH_CURRENT_USER_ID_AND_HABIT_ASSIGN_ID + habitAssignId));

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
    @Transactional
    @Override
    public HabitAssignDto enrollHabit(Long habitAssignId, Long userId, LocalDate date, String language) {
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId));

        if (!habitAssign.getUser().getId().equals(userId)) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }

        validateForEnroll(date, habitAssign);

        HabitStatusCalendar habitCalendar = HabitStatusCalendar.builder()
            .enrollDate(date).habitAssign(habitAssign).build();

        updateHabitAssignAfterEnroll(habitAssign, habitCalendar);
        UserVO userVO = userService.findById(userId);
        achievementCalculation.calculateAchievement(userVO,
            AchievementCategoryType.HABIT, AchievementAction.ASSIGN, habitAssign.getHabit().getId());
        ratingCalculation.ratingCalculation(ratingPointsRepo.findByNameOrThrow("DAYS_OF_HABIT_IN_PROGRESS"), userVO);

        return buildHabitAssignDto(habitAssign, language);
    }

    /**
     * Method validates existed enrolls of {@link HabitAssign} for creating new one.
     *
     * @param habitAssign {@link HabitAssign} instance.
     * @param date        {@link LocalDate} date.
     */
    private void validateForEnroll(LocalDate date, HabitAssign habitAssign) {
        HabitAssignVO habitAssignVO = modelMapper.map(habitAssign, HabitAssignVO.class);
        HabitStatusCalendarVO habitCalendarVO =
            habitStatusCalendarService.findHabitStatusCalendarByEnrollDateAndHabitAssign(
                date, habitAssignVO);
        if (habitCalendarVO != null) {
            throw new UserAlreadyHasEnrolledHabitAssign(ErrorMessage.HABIT_HAS_BEEN_ALREADY_ENROLLED);
        }

        LocalDate today = LocalDate.now();
        LocalDate lastDayToEnroll = today.minusDays(AppConstant.MAX_PASSED_DAYS_OF_ABILITY_TO_ENROLL);
        if (!(date.isBefore(today.plusDays(1)) && date.isAfter(lastDayToEnroll))) {
            throw new UserHasReachedOutOfEnrollRange(
                ErrorMessage.HABIT_STATUS_CALENDAR_OUT_OF_ENROLL_RANGE);
        }
        if (habitAssign.getWorkingDays() >= habitAssign.getDuration()) {
            throw new UserHasReachedOutOfEnrollRange(ErrorMessage.HABIT_ASSIGN_ENROLL_RANGE_REACHED);
        }
    }

    /**
     * Method updates {@link HabitAssign} after enroll.
     *
     * @param habitAssign {@link HabitAssign} instance.
     */
    private void updateHabitAssignAfterEnroll(HabitAssign habitAssign,
        HabitStatusCalendar habitCalendar) {
        habitAssign.setWorkingDays(habitAssign.getWorkingDays() + 1);
        habitAssign.setLastEnrollmentDate(ZonedDateTime.now());

        List<HabitStatusCalendar> habitStatusCalendars =
            new ArrayList<>(habitAssign.getHabitStatusCalendars());
        habitStatusCalendars.add(habitCalendar);
        habitAssign.setHabitStatusCalendars(habitStatusCalendars);

        int habitStreak = countNewHabitStreak(habitAssign.getHabitStatusCalendars());
        habitAssign.setHabitStreak(habitStreak);
        if (isHabitAcquired(habitAssign)) {
            habitAssign.setStatus(HabitAssignStatus.ACQUIRED);
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
    @Transactional
    @Override
    public HabitAssignDto unenrollHabit(Long habitAssignId, Long userId, LocalDate date) {
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId));

        if (!habitAssign.getUser().getId().equals(userId)) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }

        deleteHabitStatusCalendar(date, habitAssign);
        updateHabitAssignAfterUnenroll(habitAssign);
        UserVO userVO = userService.findById(userId);
        ratingCalculation.ratingCalculation(ratingPointsRepo.findByNameOrThrow("UNDO_DAYS_OF_HABIT_IN_PROGRESS"),
            userVO);
        achievementCalculation.calculateAchievement(userVO,
            AchievementCategoryType.HABIT, AchievementAction.DELETE, habitAssign.getHabit().getId());
        return modelMapper.map(habitAssign, HabitAssignDto.class);
    }

    /**
     * Method delete {@link HabitStatusCalendar}.
     *
     * @param date        {@link LocalDate} date.
     * @param habitAssign {@link HabitAssign} instance.
     */
    private void deleteHabitStatusCalendar(LocalDate date, HabitAssign habitAssign) {
        HabitStatusCalendar habitStatusCalendar = habitStatusCalendarRepo
            .findHabitStatusCalendarByEnrollDateAndHabitAssign(date, habitAssign);

        if (habitStatusCalendar == null) {
            throw new NotFoundException(ErrorMessage.HABIT_IS_NOT_ENROLLED_ON_CURRENT_DATE + date);
        }

        habitStatusCalendarRepo.delete(habitStatusCalendar);
        habitAssign.getHabitStatusCalendars().remove(habitStatusCalendar);
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
            habitAssign -> buildHabitAssignDto(habitAssign, language)).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HabitAssignDto> findInprogressHabitAssignsOnDateContent(Long userId, LocalDate date, String language) {
        List<HabitAssign> list = habitAssignRepo.findAllInprogressHabitAssignsOnDate(userId, date);
        return list.stream().map(
            habitAssign -> buildHabitAssignDtoContent(habitAssign, language)).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HabitsDateEnrollmentDto> findHabitAssignsBetweenDates(Long userId, LocalDate from, LocalDate to,
        String language) {
        if (from.isAfter(to)) {
            throw new BadRequestException(ErrorMessage.INVALID_DATE_RANGE);
        }

        List<HabitAssign> allHabitAssigns = habitAssignRepo
            .findAllInProgressHabitAssignsRelatedToUser(userId);

        List<HabitAssign> habitAssignsBetweenDates = allHabitAssigns.stream()
            .filter(ha -> isWithinDateRange(ha, from, to)).toList();

        List<LocalDate> dates = Stream.iterate(from, date -> date.plusDays(1))
            .limit(ChronoUnit.DAYS.between(from, to.plusDays(1)))
            .toList();

        List<HabitsDateEnrollmentDto> dtos = dates.stream()
            .map(date -> HabitsDateEnrollmentDto.builder().enrollDate(date)
                .habitAssigns(new ArrayList<>())
                .build())
            .toList();

        habitAssignsBetweenDates.forEach(habitAssign -> buildHabitsDateEnrollmentDto(habitAssign, language, dtos));
        return dtos;
    }

    private boolean isWithinDateRange(HabitAssign habitAssign, LocalDate from, LocalDate to) {
        LocalDate createDate = habitAssign.getCreateDate().toLocalDate();
        LocalDate endDate = createDate.plusDays(habitAssign.getDuration());

        boolean createDateWithinRange = !createDate.isBefore(from) && !createDate.isAfter(to);
        boolean endDateWithinRange = !endDate.isBefore(from) && !endDate.isAfter(to);
        boolean rangeEncompassesDates = createDate.isBefore(from) && endDate.isAfter(to);

        return createDateWithinRange || endDateWithinRange || rangeEncompassesDates;
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

        list.stream().filter(dto -> checkIfHabitIsActiveOnDay(dto, habitAssign))
            .forEach(dto -> markHabitOnHabitsEnrollmentDto(dto, checkIfHabitIsEnrolledOnDay(dto, habitAssign),
                habitTranslation, habitAssign));
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
            .isEnrolled(isEnrolled).habitAssignId(habitAssign.getId()).build());
    }

    /**
     * Method to check if {@code HabitAssign} was enrolled on concrete date.
     *
     * @param dto         {@link HabitsDateEnrollmentDto} which contains date.
     * @param habitAssign {@link HabitAssign} contains enroll dates.
     * @return boolean.
     */
    private boolean checkIfHabitIsEnrolledOnDay(HabitsDateEnrollmentDto dto, HabitAssign habitAssign) {
        return habitAssign.getHabitStatusCalendars().stream()
            .anyMatch(habitStatusCalendar -> habitStatusCalendar.getEnrollDate().equals(dto.getEnrollDate()));
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
    public void deleteHabitAssign(Long habitAssignId, Long userId) {
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId));

        if (!habitAssign.getUser().getId().equals(userId)) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }
        UserVO userVO = userService.findById(userId);

        for (int i = 0; i < habitAssign.getWorkingDays(); i++) {
            ratingCalculation.ratingCalculation(ratingPointsRepo.findByNameOrThrow("UNDO_DAYS_OF_HABIT_IN_PROGRESS"),
                userVO);
            achievementCalculation.calculateAchievement(userVO,
                AchievementCategoryType.HABIT, AchievementAction.DELETE, habitAssign.getHabit().getId());
        }
        userToDoListItemRepo.deleteToDoListItemsByHabitAssignId(habitAssign.getId());
        customToDoListItemRepo.deleteNotDefaultCustomToDoListItemsByHabitIdAndUserId(habitAssign.getHabit().getId(),
            userId);
        habitAssignRepo.delete(habitAssign);
    }

    /**
     * Method update to-do item by habitAssign id and toDoListItem id.
     *
     * @param habitAssignId  {@link Long} habit id.
     * @param toDoListItemId {@link Long} item id.
     */
    @Transactional
    public void updateToDoItem(Long habitAssignId, Long toDoListItemId) {
        Optional<UserToDoListItem> optionalUserToDoListItem =
            userToDoListItemRepo.getAllAssignedToDoListItemsFull(habitAssignId).stream()
                .filter(f -> f.getId().equals(toDoListItemId)).findAny();
        if (optionalUserToDoListItem.isPresent()) {
            UserToDoListItem utdli = optionalUserToDoListItem.get();
            if (utdli.getStatus().equals(UserToDoListItemStatus.INPROGRESS)) {
                utdli.setStatus(UserToDoListItemStatus.DONE);
            } else if (utdli.getStatus().equals(UserToDoListItemStatus.DONE)) {
                utdli.setStatus(UserToDoListItemStatus.INPROGRESS);
            }
            userToDoListItemRepo.save(utdli);
        }
    }

    @Transactional
    @Override
    public void updateProgressNotificationHasDisplayed(Long habitAssignId, Long userId) {
        if (habitAssignRepo.findById(habitAssignId).isEmpty()) {
            throw new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId);
        }
        habitAssignRepo.updateProgressNotificationHasDisplayed(habitAssignId, userId);
    }

    @Transactional
    @Override
    public HabitAssignUserDurationDto updateStatusAndDurationOfHabitAssign(Long habitAssignId, Long userId,
        Integer duration) {
        Optional<HabitAssign> habitAssignOptional = habitAssignRepo.findById(habitAssignId);
        HabitAssign habitAssign;

        if (habitAssignOptional.isPresent()) {
            habitAssign = habitAssignRepo.findByHabitAssignIdUserIdAndStatusIsRequested(habitAssignId, userId)
                .orElseThrow(() -> new InvalidStatusException(
                    ErrorMessage.HABIT_ASSIGN_STATUS_IS_NOT_REQUESTED_OR_USER_HAS_NOT_ANY_ASSIGNED_HABITS));
        } else {
            throw new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId);
        }
        habitAssign.setDuration(duration);
        habitAssign.setStatus(HabitAssignStatus.INPROGRESS);
        return modelMapper.map(habitAssignRepo.save(habitAssign), HabitAssignUserDurationDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void inviteFriendForYourHabitWithEmailNotification(UserVO userVO, List<Long> friendsIds, Long habitId,
        Locale locale) {
        friendsIds.stream()
            .map(friendId -> getValidatedFriend(userVO, friendId, habitId))
            .forEach(friend -> processHabitInvite(userVO, friend, habitId, locale));
    }

    private User getValidatedFriend(UserVO userVO, Long friendId, Long habitId) {
        User friend = getUserById(friendId);
        checkIfUserIsAFriend(userVO.getId(), friendId);
        checkHabitAssignmentValidity(habitId, friend);
        return friend;
    }

    private void processHabitInvite(UserVO userVO, User friend, Long habitId, Locale locale) {
        Habit habit = getHabitById(habitId);
        HabitAssign habitAssign = assignHabitToUser(habit, friend);
        List<Long> toDoListItemIds = toDoListItemRepo.getAllToDoListItemIdByHabitIdIsContained(habitId);
        List<Long> customToDoListItemIds =
            customToDoListItemRepo.getAllCustomToDoListItemIdByHabitIdIsContained(habitId);
        saveUserToDoListItems(toDoListItemIds, habitAssign, false);
        saveUserToDoListItems(customToDoListItemIds, habitAssign, true);

        HabitAssign inviterHabitAssign = getOrAssignHabitToUser(userVO, habit);
        saveUserToDoListItems(toDoListItemIds, inviterHabitAssign, false);
        saveUserToDoListItems(customToDoListItemIds, inviterHabitAssign, true);

        boolean invitationExists = habitInvitationRepo.existsByInviterHabitAssignAndInviteeHabitAssign(
            inviterHabitAssign, habitAssign);

        if (invitationExists) {
            throw new IllegalArgumentException(ErrorMessage.INVITATION_ALREADY_EXIST);
        }

        HabitInvitation habitInvitation =
            habitInvitationRepo.save(createHabitInvitation(habitAssign, inviterHabitAssign));

        String habitName = getHabitTranslation(habitAssign, locale.getLanguage()).getName();
        UserVO friendVO = mapToUserVO(friend);

        userNotificationService.createNotification(friendVO, userVO, NotificationType.HABIT_INVITE, habitId, habitName,
            habitInvitation.getId(), habitName);
    }

    private HabitAssign assignHabitToUser(Habit habit, User user) {
        HabitAssign habitAssign = updateOrCreateHabitAssignWithStatus(habit, user, HabitAssignStatus.REQUESTED);
        return habitAssignRepo.save(habitAssign);
    }

    private HabitAssign getOrAssignHabitToUser(UserVO userVO, Habit habit) {
        return habitAssignRepo.findByHabitIdAndUserId(habit.getId(), userVO.getId())
            .orElseGet(() -> {
                HabitAssign habitAssign = assignHabitToUser(habit, getUserById(userVO.getId()));
                List<Long> toDoListItemIds = toDoListItemRepo.getAllToDoListItemIdByHabitIdIsContained(habit.getId());
                List<Long> customToDoListItemIds =
                    customToDoListItemRepo.getAllCustomToDoListItemIdByHabitIdIsContained(habit.getId());
                saveUserToDoListItems(toDoListItemIds, habitAssign, false);
                saveUserToDoListItems(customToDoListItemIds, habitAssign, true);
                return habitAssign;
            });
    }

    private void checkIfUserIsAFriend(Long userId, Long friendId) {
        if (!userRepo.isFriend(userId, friendId)) {
            throw new UserHasNoFriendWithIdException(ErrorMessage.USER_HAS_NO_FRIEND_WITH_ID + friendId);
        }
    }

    private User getUserById(Long userId) {
        return userRepo.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + userId));
    }

    private UserVO mapToUserVO(User user) {
        return modelMapper.map(user, UserVO.class);
    }

    private Habit getHabitById(Long habitId) {
        return habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId));
    }

    private HabitAssign getHabitAssignById(Long habitId, Long userId) {
        return habitAssignRepo
            .findByHabitIdAndUserIdAndStatusIsCancelledOrRequested(habitId, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void confirmHabitInvitation(Long habitAssignId) {
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId));
        if (!habitAssign.getStatus().equals(HabitAssignStatus.REQUESTED)) {
            throw new BadRequestException(
                ErrorMessage.HABIT_ASSIGN_STATUS_IS_NOT_REQUESTED_OR_USER_HAS_NOT_ANY_ASSIGNED_HABITS);
        }
        habitAssign.setStatus(HabitAssignStatus.INPROGRESS);
        habitAssign.setCreateDate(ZonedDateTime.now());
        habitAssignRepo.save(habitAssign);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<HabitWorkingDaysDto> getAllHabitsWorkingDaysInfoForCurrentUserFriends(Long userId, Long habitAssignId) {
        List<Long> friendsIdsTrackingHabit =
            habitInvitationService.getInvitedFriendsIdsTrackingHabitList(userId, habitAssignId);
        if (friendsIdsTrackingHabit.isEmpty()) {
            throw new NotFoundException(ErrorMessage.NO_FRIENDS_ASSIGNED_ON_CURRENT_HABIT_ASSIGN + habitAssignId);
        }

        List<HabitAssignDto> habitAssigns =
            habitInvitationService.getHabitAssignsTrackingHabitList(userId, habitAssignId);

        return habitAssigns.stream()
            .map(this::convert)
            .toList();
    }

    private HabitWorkingDaysDto convert(HabitAssignDto habitAssign) {
        return HabitWorkingDaysDto.builder()
            .userId(habitAssign.getUserId())
            .duration(habitAssign.getDuration())
            .workingDays(habitAssign.getWorkingDays())
            .build();
    }

    @NotNull
    private PageableAdvancedDto<HabitAssignPreviewDto> mapHabitAssignPageToPageableAdvancedDtoOfMutualHabitAssignDto(
        Page<HabitAssign> returnedPage) {
        List<HabitAssignPreviewDto> habitAssignPreviewDtos = returnedPage.getContent().stream()
            .map(habitAssign -> modelMapper.map(habitAssign, HabitAssignPreviewDto.class)).toList();
        return new PageableAdvancedDto<>(habitAssignPreviewDtos, returnedPage.getTotalElements(),
            returnedPage.getPageable().getPageNumber(), returnedPage.getTotalPages(), returnedPage.getNumber(),
            returnedPage.hasPrevious(), returnedPage.hasNext(), returnedPage.isFirst(), returnedPage.isLast());
    }

    private HabitAssign createHabitAssign(User user, Habit habit,
        HabitAssignCustomPropertiesDto habitAssignCustomPropertiesDto, HabitAssignStatus status) {
        HabitAssign habitAssign = updateOrCreateHabitAssignWithStatus(habit, user, status);

        HabitAssignPropertiesDto customAssignProperties = habitAssignCustomPropertiesDto.getHabitAssignPropertiesDto();
        enhanceAssignWithCustomProperties(habitAssign, customAssignProperties);
        habitAssign = habitAssignRepo.save(habitAssign);

        List<Long> defaultToDoList = customAssignProperties.getDefaultToDoListItemIds();
        List<Long> customDefaultToDoList = customAssignProperties.getDefaultCustomToDoListItemIds();
        if (defaultToDoList != null) {
            saveUserToDoListItems(defaultToDoList, habitAssign, false);
        }
        if (customDefaultToDoList != null) {
            saveUserToDoListItems(customDefaultToDoList, habitAssign, true);
        }
        saveCustomToDoListItemsForCurrentUser(habitAssignCustomPropertiesDto.getCustomToDoListItemList(), user, habit,
            habitAssign);

        return habitAssign;
    }

    private HabitAssign updateOrCreateHabitAssignWithStatus(Habit habit, User user, HabitAssignStatus status) {
        HabitAssign habitAssign =
            getHabitAssignById(habit.getId(), user.getId());
        if (habitAssign != null) {
            habitAssign.setStatus(status);
            habitAssign.setCreateDate(ZonedDateTime.now());
        } else {
            habitAssign = buildHabitAssign(habit, user, status);
        }
        return habitAssign;
    }

    private void checkHabitAssignmentValidity(Long habitId, User user) {
        checkStatusInProgressExists(habitId, user.getId());
        validateHabitForAssign(habitId, user);
    }

    private HabitAssign assignHabitForUser(Habit habit, UserVO userVO,
        HabitAssignCustomPropertiesDto habitAssignCustomPropertiesDto) {
        User user = modelMapper.map(userVO, User.class);
        checkHabitAssignmentValidity(habit.getId(), user);

        return createHabitAssign(user, habit, habitAssignCustomPropertiesDto, HabitAssignStatus.INPROGRESS);
    }

    private HabitAssignCustomPropertiesDto buildDefaultHabitAssignPropertiesDto(Habit habit) {
        HabitAssignPropertiesDto habitAssignPropertiesDto = HabitAssignPropertiesDto.builder()
            .defaultToDoListItemIds(toDoListItemRepo.getAllToDoListItemIdByHabitIdIsContained(habit.getId()))
            .defaultCustomToDoListItemIds(
                customToDoListItemRepo.getAllCustomToDoListItemIdByHabitIdIsContained(habit.getId()))
            .duration(habit.getDefaultDuration())
            .isPrivate(false)
            .build();

        return HabitAssignCustomPropertiesDto.builder()
            .habitAssignPropertiesDto(habitAssignPropertiesDto)
            .customToDoListItemList(null)
            .friendsIdsList(null)
            .build();
    }

    private List<User> getUsersByIds(List<Long> ids) {
        return ids.stream()
            .map(id -> userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + id)))
            .toList();
    }

    private HabitInvitation createHabitInvitation(HabitAssign inviteeHabitAssign, HabitAssign inviterHabitAssign) {
        return HabitInvitation.builder()
            .inviteeHabitAssign(inviteeHabitAssign)
            .inviterHabitAssign(inviterHabitAssign)
            .status(HabitInvitationStatus.PENDING)
            .build();
    }

    private void setLikes(HabitAssignDto habitAssignDto, HabitAssign habitAssign) {
        int likes = habitAssign.getHabit().getUsersLiked().size();
        habitAssignDto.getHabit().setLikes(likes);
    }

    private void setDislikes(HabitAssignDto habitAssignDto, HabitAssign habitAssign) {
        int dislikes = habitAssign.getHabit().getUsersDisliked().size();
        habitAssignDto.getHabit().setDislikes(dislikes);
    }
}
