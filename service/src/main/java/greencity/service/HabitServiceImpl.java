package greencity.service;

import greencity.achievement.AchievementCalculation;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableDto;
import greencity.dto.filter.HabitTranslationFilterDto;
import greencity.dto.friends.UserFriendDto;
import greencity.dto.friends.UserFriendHabitInviteDto;
import greencity.dto.habit.CustomHabitDtoRequest;
import greencity.dto.habit.CustomHabitDtoResponse;
import greencity.dto.habit.HabitDto;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.dto.notification.LikeNotificationDto;
import greencity.dto.todolistitem.ToDoListItemDto;
import greencity.dto.user.UserProfilePictureDto;
import greencity.dto.user.UserVO;
import greencity.entity.CustomToDoListItem;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.HabitTranslation;
import greencity.entity.Tag;
import greencity.entity.User;
import greencity.enums.HabitAssignStatus;
import greencity.enums.Role;
import greencity.enums.AchievementCategoryType;
import greencity.enums.AchievementAction;
import greencity.enums.NotificationType;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoFriendWithIdException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.exception.exceptions.WrongEmailException;
import greencity.mapping.CustomHabitMapper;
import greencity.mapping.CustomToDoListMapper;
import greencity.mapping.CustomToDoListResponseDtoMapper;
import greencity.mapping.HabitTranslationDtoMapper;
import greencity.mapping.HabitTranslationMapper;
import greencity.rating.RatingCalculation;
import greencity.repository.HabitInvitationRepo;
import greencity.repository.HabitRepo;
import greencity.repository.HabitTranslationRepo;
import greencity.repository.ToDoListItemTranslationRepo;
import greencity.repository.HabitAssignRepo;
import greencity.repository.RatingPointsRepo;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import greencity.repository.CustomToDoListItemRepo;
import greencity.repository.LanguageRepo;
import greencity.repository.TagsRepo;
import greencity.repository.UserRepo;
import greencity.repository.options.HabitTranslationFilter;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.Transactional;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * Implementation of {@link HabitService}.
 */
@Service
@AllArgsConstructor
public class HabitServiceImpl implements HabitService {
    private final HabitRepo habitRepo;
    private final HabitTranslationRepo habitTranslationRepo;
    private final ModelMapper modelMapper;
    private final CustomToDoListResponseDtoMapper customToDoListResponseDtoMapper;
    private final HabitTranslationDtoMapper habitTranslationDtoMapper;
    private final CustomToDoListMapper customToDoListMapper;
    private final HabitTranslationMapper habitTranslationMapper;
    private final CustomHabitMapper customHabitMapper;
    private final ToDoListItemTranslationRepo toDoListItemTranslationRepo;
    private final CustomToDoListItemRepo customToDoListItemRepo;
    private final LanguageRepo languageRepo;
    private final UserRepo userRepo;
    private final TagsRepo tagsRepo;
    private final FileService fileService;
    private final HabitAssignRepo habitAssignRepo;
    private final HabitAssignService habitAssignService;
    private static final String DEFAULT_TITLE_IMAGE_PATH = AppConstant.DEFAULT_HABIT_IMAGE;
    private final UserNotificationService userNotificationService;
    private final RatingCalculation ratingCalculation;
    private final AchievementCalculation achievementCalculation;
    private final RatingPointsRepo ratingPointsRepo;
    private final HabitInvitationService habitInvitationService;
    private final FriendService friendService;
    private final HabitInvitationRepo habitInvitationRepo;

    /**
     * Method returns Habit by its id.
     *
     * @param id           - id of the {@link Long} habit
     * @param languageCode - language code {@link String}
     * @return {@link HabitDto}
     */
    @Override
    public HabitDto getByIdAndLanguageCode(Long id, String languageCode) {
        Habit habit = habitRepo.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + id));
        HabitTranslation habitTranslation = habitTranslationRepo.findByHabitAndLanguageCode(habit, languageCode)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_TRANSLATION_NOT_FOUND + id));
        var habitDto = modelMapper.map(habitTranslation, HabitDto.class);
        List<ToDoListItemDto> toDoListItems = new ArrayList<>();
        toDoListItemTranslationRepo
            .findToDoListByHabitIdAndByLanguageCode(languageCode, id)
            .forEach(x -> toDoListItems.add(modelMapper.map(x, ToDoListItemDto.class)));
        habitDto.setToDoListItems(toDoListItems);
        habitDto.setAmountAcquiredUsers(habitAssignRepo.findAmountOfUsersAcquired(habitDto.getId()));
        boolean isCustomHabit = habit.getIsCustomHabit();
        habitDto.setIsCustomHabit(isCustomHabit);
        habitDto.setUsersIdWhoCreatedCustomHabit(habit.getUserId());
        if (isCustomHabit) {
            habitDto.setCustomToDoListItems(
                customToDoListResponseDtoMapper.mapAllToList(habit.getCustomToDoListItems()));
        }
        return habitDto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<HabitDto> getAllHabitsByLanguageCode(UserVO userVO, Pageable pageable, String languageCode) {
        long userId = userVO.getId();
        List<Long> requestedCustomHabitIds = habitAssignRepo.findAllHabitIdsByUserIdAndStatusIsRequested(userId);
        checkAndAddToEmptyCollectionValueNull(requestedCustomHabitIds);

        Page<HabitTranslation> habitTranslationPage =
            habitTranslationRepo.findAllByLanguageCodeAndHabitAssignIdsRequestedAndUserId(pageable,
                requestedCustomHabitIds, userId, languageCode);
        return buildPageableDtoForDifferentParameters(habitTranslationPage, userVO.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<HabitDto> getMyHabits(Long userId, Pageable pageable, String languageCode) {
        Page<HabitTranslation> habitTranslationPage = habitTranslationRepo.findMyHabits(pageable, userId, languageCode);
        return buildPageableDtoForDifferentParameters(habitTranslationPage, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<HabitDto> getAllHabitsOfFriend(Long userId, Long friendId, Pageable pageable,
        String languageCode) {
        if (!userRepo.isFriend(userId, friendId)) {
            throw new UserHasNoFriendWithIdException(
                ErrorMessage.USER_HAS_NO_FRIEND_WITH_ID + friendId);
        }
        Page<HabitTranslation> habitTranslationPage =
            habitTranslationRepo.findAllHabitsOfFriend(pageable, friendId, languageCode);

        return buildPageableDtoForDifferentParameters(habitTranslationPage, userId, friendId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<HabitDto> getAllMutualHabitsWithFriend(Long userId, Long friendId, Pageable pageable,
        String languageCode) {
        if (!userRepo.isFriend(userId, friendId)) {
            throw new UserHasNoFriendWithIdException(
                ErrorMessage.USER_HAS_NO_FRIEND_WITH_ID + friendId);
        }
        Page<HabitTranslation> habitTranslationPage =
            habitTranslationRepo.findAllMutualHabitsWithFriend(pageable, userId, friendId, languageCode);

        return buildPageableDtoForDifferentParameters(habitTranslationPage, userId, friendId);
    }

    /**
     * Method to check and add null value to {@link List} when it is empty in order
     * to prevent exception in repository method's queries.
     *
     * @param emptyCollection - list of objects {@link List} that will be checked
     *                        for emptiness.
     * @author Olena Sotnik
     */
    private void checkAndAddToEmptyCollectionValueNull(List<?> emptyCollection) {
        if (emptyCollection.isEmpty()) {
            emptyCollection.add(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<HabitDto> getAllByTagsAndLanguageCode(Pageable pageable, List<String> tags,
        String languageCode, boolean excludeAssigned, Long userId) {
        List<String> lowerCaseTags = tags.stream().map(String::toLowerCase).collect(Collectors.toList());
        Page<HabitTranslation> habitTranslationsPage = (excludeAssigned)
            ? habitTranslationRepo.findUnassignedHabitTranslationsByLanguageAndTags(pageable, lowerCaseTags,
                languageCode, userId)
            : habitTranslationRepo.findAllByTagsAndLanguageCode(pageable, lowerCaseTags, languageCode);

        return buildPageableDto(habitTranslationsPage);
    }

    /**
     * Method that build {@link PageableDto} of {@link HabitDto} from {@link Page}
     * of {@link HabitTranslation}.
     *
     * @param habitTranslationsPage {@link Page} of {@link HabitTranslation}
     * @return {@link PageableDto} of {@link HabitDto}
     * @author Markiyan Derevetskyi
     */
    private PageableDto<HabitDto> buildPageableDto(Page<HabitTranslation> habitTranslationsPage) {
        List<HabitDto> habits =
            habitTranslationsPage.stream()
                .map(habitTranslation -> modelMapper.map(habitTranslation, HabitDto.class))
                .collect(Collectors.toList());
        habits.forEach(
            habitDto -> habitDto.setAmountAcquiredUsers(habitAssignRepo.findAmountOfUsersAcquired(habitDto.getId())));
        return new PageableDto<>(habits, habitTranslationsPage.getTotalElements(),
            habitTranslationsPage.getPageable().getPageNumber(),
            habitTranslationsPage.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<HabitDto> getAllByDifferentParameters(UserVO userVO, Pageable pageable,
        Optional<List<String>> tags, Optional<Boolean> isCustomHabit, Optional<List<Integer>> complexities,
        String languageCode) {
        Long userId = userVO.getId();
        HabitTranslationFilterDto filterDto = HabitTranslationFilterDto.builder()
            .userId(userId)
            .languageCode(languageCode)
            .tags(tags.orElse(new ArrayList<>()))
            .complexities(complexities.orElse(new ArrayList<>()))
            .isCustom(isCustomHabit.orElse(null))
            .build();

        Specification<HabitTranslation> specification = new HabitTranslationFilter(filterDto);
        Page<HabitTranslation> habitTranslationsPage = habitTranslationRepo.findAll(specification, pageable);

        return buildPageableDtoForDifferentParameters(habitTranslationsPage, userVO.getId());
    }

    private PageableDto<HabitDto> buildPageableDtoForDifferentParameters(Page<HabitTranslation> habitTranslationsPage,
        Long userId) {
        return buildPageableDtoForDifferentParameters(habitTranslationsPage, userId, userId);
    }

    /**
     * Method that build {@link PageableDto} of {@link HabitDto} from {@link Page}
     * of {@link HabitTranslation}.
     *
     * @param habitTranslationsPage {@link Page} of {@link HabitTranslation}
     * @return {@link PageableDto} of {@link HabitDto}
     * @author Lilia Mokhnatska
     */
    private PageableDto<HabitDto> buildPageableDtoForDifferentParameters(Page<HabitTranslation> habitTranslationsPage,
        Long userId, Long friendId) {
        List<HabitDto> habits = habitTranslationsPage.stream()
            .map(habitTranslation -> {
                HabitDto habitDto = modelMapper.map(habitTranslation, HabitDto.class);
                HabitTranslation habitTranslationByUaLanguage =
                    habitTranslationRepo.getHabitTranslationByUaLanguage(habitTranslation.getHabit().getId());
                habitDto.getHabitTranslation()
                    .setDescriptionUa(habitTranslationByUaLanguage.getDescription() != null
                        ? habitTranslationByUaLanguage.getDescription()
                        : "");
                habitDto.getHabitTranslation().setNameUa(
                    habitTranslationByUaLanguage.getName() != null ? habitTranslationByUaLanguage.getName() : "");
                habitDto.getHabitTranslation()
                    .setHabitItemUa(habitTranslationByUaLanguage.getHabitItem() != null
                        ? habitTranslationByUaLanguage.getHabitItem()
                        : "");
                boolean isFavorite = isCurrentUserFollower(habitTranslation.getHabit(), userId);
                habitDto.setIsFavorite(isFavorite);
                return habitDto;
            })
            .collect(Collectors.toList());
        habits.forEach(
            habitDto -> habitDto.setAmountAcquiredUsers(habitAssignRepo.findAmountOfUsersAcquired(habitDto.getId())));

        for (HabitDto habitDto : habits) {
            Habit habit = habitRepo.findById(habitDto.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitDto.getId()));
            List<HabitAssign> habitAssigns =
                habitAssignRepo.findHabitsByHabitIdAndUserId(habitDto.getId(), friendId);
            if (!habitAssigns.isEmpty()) {
                habitDto.setHabitAssignStatus(assignHabitStatus(habitAssigns));
            }
            boolean isCustomHabit = habit.getIsCustomHabit();
            habitDto.setIsCustomHabit(isCustomHabit);
            if (isCustomHabit) {
                habitDto.setUsersIdWhoCreatedCustomHabit(habit.getUserId());
            }

            if (userId.equals(friendId)) {
                boolean isAssigned = habitDto.getHabitAssignStatus() == HabitAssignStatus.INPROGRESS;
                habitDto.setIsAssigned(isAssigned);
            } else {
                habitDto.setIsAssigned(isHabitAssign(userId, habit.getId()));
            }
            habitDto.setCustomToDoListItems(
                customToDoListResponseDtoMapper.mapAllToList(habit.getCustomToDoListItems()));
        }
        return new PageableDto<>(habits,
            habitTranslationsPage.getTotalElements(),
            habitTranslationsPage.getPageable().getPageNumber(),
            habitTranslationsPage.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ToDoListItemDto> getToDoListForHabit(Long habitId, String lang) {
        return toDoListItemTranslationRepo.findToDoListByHabitIdAndByLanguageCode(lang, habitId)
            .stream()
            .map(g -> modelMapper.map(g, ToDoListItemDto.class))
            .collect(Collectors.toList());
    }

    @Override
    public void addToDoListItemToHabit(Long habitId, Long itemId) {
        habitRepo.addToDoListItemToHabit(habitId, itemId);
    }

    @Override
    public void deleteToDoListItem(Long habitId, Long itemId) {
        habitRepo.upadateToDoListItemInHabit(habitId, itemId);
    }

    @Override
    public List<Long> deleteAllToDoListItemsByListOfId(Long habitId, List<Long> listId) {
        listId.forEach(id -> deleteToDoListItem(habitId, id));
        return listId;
    }

    @Override
    public List<Long> addAllToDoListItemsByListOfId(Long habitId, List<Long> listId) {
        listId.forEach(id -> addToDoListItemToHabit(habitId, id));
        return listId;
    }

    @Transactional
    @Override
    public CustomHabitDtoResponse addCustomHabit(
        CustomHabitDtoRequest addCustomHabitDtoRequest, MultipartFile image, String userEmail) {
        User user = userRepo.findByEmail(userEmail)
            .orElseThrow(() -> new WrongEmailException(ErrorMessage.USER_NOT_FOUND_BY_EMAIL + userEmail));

        if (StringUtils.isNotBlank(addCustomHabitDtoRequest.getImage())) {
            image = fileService.convertToMultipartImage(addCustomHabitDtoRequest.getImage());
        }
        if (image != null) {
            addCustomHabitDtoRequest.setImage(fileService.upload(image));
        } else {
            addCustomHabitDtoRequest.setImage(DEFAULT_TITLE_IMAGE_PATH);
        }
        Habit habit = habitRepo.save(customHabitMapper.convert(addCustomHabitDtoRequest));
        habit.setUserId(user.getId());
        habit.setIsDeleted(false);
        setTagsIdsToHabit(addCustomHabitDtoRequest, habit);
        saveHabitTranslationListsToHabitTranslationRepo(addCustomHabitDtoRequest, habit);
        setCustomToDoListItemToHabit(addCustomHabitDtoRequest, habit, user);
        inviteFriendsToJoinHabit(addCustomHabitDtoRequest, user, habit);

        return buildAddCustomHabitDtoResponse(habit, user.getId());
    }

    /**
     * Method that build {@link CustomHabitDtoResponse} from {@link Habit}.
     *
     * @param habit  {@link Habit}
     * @param userId {@link Long}
     * @return {@link CustomHabitDtoResponse}
     * @author Lilia Mokhnatska
     */
    private CustomHabitDtoResponse buildAddCustomHabitDtoResponse(Habit habit, Long userId) {
        CustomHabitDtoResponse response = modelMapper.map(habit, CustomHabitDtoResponse.class);

        response.setCustomToDoListItemDto(customToDoListResponseDtoMapper
            .mapAllToList(customToDoListItemRepo.findAllByUserIdAndHabitId(userId, habit.getId())));
        response.setTagIds(habit.getTags().stream().map(Tag::getId).collect(Collectors.toSet()));
        response
            .setHabitTranslations(habitTranslationDtoMapper.mapAllToList(habitTranslationRepo.findAllByHabit(habit)));
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserProfilePictureDto> getFriendsAssignedToHabitProfilePictures(Long habitAssignId, Long userId) {
        if (!userRepo.existsById(userId)) {
            throw new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + userId);
        }
        if (!habitAssignRepo.existsById(habitAssignId)) {
            throw new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId);
        }
        List<Long> ids = habitInvitationService.getInvitedFriendsIdsTrackingHabitList(userId, habitAssignId);
        List<User> users = userRepo.findAllById(ids);
        return users.stream().map(user -> modelMapper.map(user, UserProfilePictureDto.class))
            .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CustomHabitDtoResponse updateCustomHabit(CustomHabitDtoRequest habitDto, Long habitId,
        String userEmail, MultipartFile image) {
        User user = userRepo.findByEmail(userEmail)
            .orElseThrow(() -> new WrongEmailException(ErrorMessage.USER_NOT_FOUND_BY_EMAIL + userEmail));
        Habit toUpdate = habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.CUSTOM_HABIT_NOT_FOUND + habitId));
        checkAccessForAdminAndModeratorAndByUserId(user, toUpdate);
        enhanceHabitWithNewData(toUpdate, habitDto, user, image);
        Habit updatedHabit = habitRepo.save(toUpdate);
        return buildAddCustomHabitDtoResponse(updatedHabit, user.getId());
    }

    private void enhanceHabitWithNewData(Habit toUpdate, CustomHabitDtoRequest habitDto,
        User user, MultipartFile image) {
        if (Objects.nonNull(habitDto.getComplexity())) {
            toUpdate.setComplexity(habitDto.getComplexity());
        }
        if (Objects.nonNull(habitDto.getDefaultDuration())) {
            toUpdate.setDefaultDuration(habitDto.getDefaultDuration());
        }
        if (isNotEmpty(habitDto.getHabitTranslations())) {
            updateHabitTranslationsForCustomHabit(habitDto, toUpdate);
        }
        if (isNotEmpty(habitDto.getCustomToDoListItemDto())) {
            updateExistingCustomToDoListItems(habitDto, toUpdate, user);
            saveNewCustomToDoListItemsToUpdate(habitDto, toUpdate, user);
        }
        if (StringUtils.isNotBlank(habitDto.getImage())) {
            image = fileService.convertToMultipartImage(habitDto.getImage());
        }
        if (image != null) {
            toUpdate.setImage(fileService.upload(image));
        }
        if (isNotEmpty(habitDto.getTagIds())) {
            setTagsIdsToHabit(habitDto, toUpdate);
        }
    }

    private void saveNewCustomToDoListItemsToUpdate(CustomHabitDtoRequest habitDto, Habit habit, User user) {
        List<CustomToDoListItem> customToDoListItems = customToDoListMapper
            .mapAllToList(habitDto.getCustomToDoListItemDto());

        customToDoListItems.stream()
            .filter(item -> Objects.isNull(item.getId()))
            .forEach(customToDoListItem -> {
                customToDoListItem.setHabit(habit);
                customToDoListItem.setUser(user);
                customToDoListItemRepo.save(customToDoListItem);
            });
    }

    private void updateExistingCustomToDoListItems(CustomHabitDtoRequest habitDto, Habit habit, User user) {
        List<CustomToDoListItem> customToDoListItems = customToDoListItemRepo
            .findAllByUserIdAndHabitId(user.getId(), habit.getId());

        customToDoListItems
            .forEach(item -> habitDto.getCustomToDoListItemDto().stream()
                .filter(itemToUpdate -> item.getId().equals(itemToUpdate.getId()))
                .forEach(itemToUpdate -> {
                    item.setStatus(itemToUpdate.getStatus());
                    item.setText(itemToUpdate.getText());
                }));

        customToDoListItemRepo.deleteAll(customToDoListItems.stream()
            .filter(item -> habitDto.getCustomToDoListItemDto().stream()
                .noneMatch(itemToUpdate -> item.getId().equals(itemToUpdate.getId())))
            .collect(Collectors.toList()));
    }

    private void updateHabitTranslationsForCustomHabit(CustomHabitDtoRequest habitDto, Habit habit) {
        Optional<HabitTranslationDto> habitTranslationDtoOptional = habitDto.getHabitTranslations().stream()
            .findFirst();
        habitTranslationDtoOptional.ifPresent(habitTranslationDto -> habitTranslationRepo.findAllByHabit(habit)
            .forEach(habitTranslation -> {
                habitTranslation.setName(habitTranslationDto.getName());
                habitTranslation.setDescription(habitTranslationDto.getDescription());
                habitTranslation.setHabitItem(habitTranslationDto.getHabitItem());
            }));
    }

    private void saveHabitTranslationListsToHabitTranslationRepo(CustomHabitDtoRequest habitDto, Habit habit) {
        List<HabitTranslation> habitTranslationListForUa =
            mapHabitTranslationFromAddCustomHabitDtoRequest(habitDto, AppConstant.LANGUAGE_CODE_UA);
        habitTranslationListForUa.forEach(habitTranslation -> habitTranslation.setHabit(habit));
        habitTranslationListForUa.forEach(habitTranslation -> habitTranslation.setLanguage(
            languageRepo.findByCode(AppConstant.LANGUAGE_CODE_UA).orElseThrow(NoSuchElementException::new)));

        List<HabitTranslation> habitTranslationListForEn =
            mapHabitTranslationFromAddCustomHabitDtoRequest(habitDto, AppConstant.DEFAULT_LANGUAGE_CODE);
        habitTranslationListForEn.forEach(habitTranslation -> habitTranslation.setHabit(habit));
        habitTranslationListForEn.forEach(habitTranslation -> habitTranslation.setLanguage(
            languageRepo.findByCode(AppConstant.DEFAULT_LANGUAGE_CODE).orElseThrow(NoSuchElementException::new)));

        habit.setHabitTranslations(Stream.concat(habitTranslationListForUa.stream(), habitTranslationListForEn.stream())
            .collect(Collectors.toList()));
    }

    private List<HabitTranslation> mapHabitTranslationFromAddCustomHabitDtoRequest(CustomHabitDtoRequest habitDto,
        String language) {
        return habitTranslationMapper.mapAllToList(habitDto.getHabitTranslations(), language);
    }

    private void setTagsIdsToHabit(CustomHabitDtoRequest habitDto, Habit habit) {
        habit.setTags(habitDto.getTagIds().stream().map(tagId -> tagsRepo.findById(tagId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.TAG_NOT_FOUND + tagId)))
            .collect(Collectors.toSet()));
    }

    private void setCustomToDoListItemToHabit(CustomHabitDtoRequest habitDto, Habit habit, User user) {
        List<CustomToDoListItem> customToDoListItems =
            customToDoListMapper.mapAllToList(habitDto.getCustomToDoListItemDto());
        customToDoListItems.forEach(customToDoListItem -> customToDoListItem.setHabit(habit));
        customToDoListItems.forEach(customToDoListItem -> customToDoListItem.setUser(user));
        customToDoListItemRepo.saveAll(customToDoListItems);
        habit.setCustomToDoListItems(customToDoListItems);
    }

    private void inviteFriendsToJoinHabit(CustomHabitDtoRequest addCustomHabitDtoRequest, User user, Habit habit) {
        if (!addCustomHabitDtoRequest.getFriendsToInvite().isEmpty()) {
            List<Long> friendsIds = addCustomHabitDtoRequest.getFriendsToInvite().stream()
                .map(UserFriendDto::getId)
                .collect(Collectors.toList());
            habitAssignService.inviteFriendForYourHabitWithEmailNotification(
                modelMapper.map(user, UserVO.class), friendsIds, habit.getId(),
                Locale.of(user.getLanguage().getCode()));
        }
    }

    private void checkAccessForAdminAndModeratorAndByUserId(User user, Habit habit) {
        if (user.getRole() != Role.ROLE_ADMIN && user.getRole() != Role.ROLE_MODERATOR
            && !user.getId().equals(habit.getUserId())) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteCustomHabit(Long customHabitId, String ownerEmail) {
        Habit toDelete = habitRepo.findByIdAndIsCustomHabitIsTrue(customHabitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.CUSTOM_HABIT_NOT_FOUND + customHabitId));
        User owner = userRepo.findByEmail(ownerEmail)
            .orElseThrow(() -> new WrongEmailException(ErrorMessage.USER_NOT_FOUND_BY_EMAIL + ownerEmail));
        unAssignOwnerFromCustomHabit(toDelete, owner.getId());
        toDelete.setIsDeleted(true);
        habitRepo.save(toDelete);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void like(Long habitId, UserVO userVO) {
        Habit habit = findHabitById(habitId);
        User author = getHabitAuthor(habit);
        boolean isAuthor = habit.getUserId().equals(userVO.getId());

        if (isAuthor) {
            throw new BadRequestException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }

        if (removeLikeIfExists(habit, userVO, author)) {
            return;
        }

        removeDislikeIfExists(habit, userVO);

        habit.getUsersLiked().add(modelMapper.map(userVO, User.class));
        ratingCalculation.ratingCalculation(ratingPointsRepo.findByNameOrThrow("LIKE_HABIT"), userVO);
        achievementCalculation.calculateAchievement(userVO, AchievementCategoryType.LIKE_HABIT,
            AchievementAction.ASSIGN);

        sendHabitLikeNotification(author, userVO, habitId, habit);

        habitRepo.save(habit);
    }

    @Override
    public void dislike(Long habitId, UserVO userVO) {
        Habit habit = findHabitById(habitId);
        boolean isAuthor = habit.getUserId().equals(userVO.getId());

        if (isAuthor) {
            throw new BadRequestException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }

        removeLikeIfExists(habit, userVO, getHabitAuthor(habit));

        if (removeDislikeIfExists(habit, userVO)) {
            return;
        }

        habit.getUsersDisliked().add(modelMapper.map(userVO, User.class));

        habitRepo.save(habit);
    }

    private void sendHabitLikeNotification(User targetUser, UserVO actionUser, Long habitId, Habit habit) {
        final LikeNotificationDto likeNotificationDto = LikeNotificationDto.builder()
            .targetUserVO(modelMapper.map(targetUser, UserVO.class))
            .actionUserVO(actionUser)
            .newsId(habitId)
            .newsTitle(habit.getHabitTranslations().getFirst().getName())
            .notificationType(NotificationType.HABIT_LIKE)
            .isLike(true)
            .build();
        userNotificationService.createOrUpdateLikeNotification(likeNotificationDto);
    }

    private void unAssignOwnerFromCustomHabit(Habit habit, Long userId) {
        if (!userId.equals(habit.getUserId())) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }
        habitRepo.findHabitAssignByHabitIdAndHabitOwnerId(habit.getId(), userId)
            .forEach(haId -> habitAssignService.deleteHabitAssign(haId, userId));
    }

    private HabitAssignStatus assignHabitStatus(List<HabitAssign> habitAssigns) {
        if (habitAssigns.isEmpty()) {
            throw new IllegalArgumentException(ErrorMessage.EMPTY_HABIT_ASSIGN_LIST);
        }

        for (HabitAssign habitAssign : habitAssigns) {
            HabitAssignStatus status = habitAssign.getStatus();
            if (status == HabitAssignStatus.INPROGRESS || status == HabitAssignStatus.REQUESTED) {
                return status;
            }
        }
        return HabitAssignStatus.ACQUIRED;
    }

    private boolean isHabitAssign(Long userId, Long habitId) {
        List<HabitAssign> habitAssigns = habitAssignRepo.findHabitsByHabitIdAndUserId(habitId, userId);
        if (habitAssigns.isEmpty()) {
            return false;
        }
        return assignHabitStatus(habitAssigns) == HabitAssignStatus.INPROGRESS;
    }

    @Override
    public void addToFavorites(Long habitId, String email) {
        Habit habit = habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId));

        User currentUser = userRepo.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_EMAIL + email));

        if (habit.getFollowers().contains(currentUser)) {
            throw new BadRequestException(ErrorMessage.USER_HAS_ALREADY_ADDED_HABIT_TO_FAVORITES);
        }

        habit.getFollowers().add(currentUser);

        habitRepo.save(habit);
    }

    @Override
    public void removeFromFavorites(Long habitId, String email) {
        Habit habit = habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId));

        User currentUser = userRepo.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_EMAIL + email));

        if (!habit.getFollowers().contains(currentUser)) {
            throw new BadRequestException(ErrorMessage.HABIT_NOT_IN_FAVORITES);
        }

        habit.getFollowers().remove(currentUser);
        habitRepo.save(habit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<HabitDto> getAllFavoriteHabitsByLanguageCode(UserVO userVO, Pageable pageable,
        String languageCode) {
        Long userId = userVO.getId();
        Page<HabitTranslation> habitTranslationPage =
            habitTranslationRepo.findMyFavoriteHabits(pageable, userId, languageCode);
        return buildPageableDtoForDifferentParameters(habitTranslationPage, userVO.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<UserFriendHabitInviteDto> findAllFriendsOfUser(UserVO userVO, String name, Pageable pageable,
        Long habitId) {
        Long userId = userVO.getId();
        name = Optional.ofNullable(name).orElse("");
        Page<UserFriendHabitInviteDto> friendsWithIsInvitedStatus =
            findUserFriendsWithHabitInvitesMapped(userId, name, habitId, pageable);
        return new PageableDto<>(
            friendsWithIsInvitedStatus.getContent(),
            friendsWithIsInvitedStatus.getTotalElements(),
            friendsWithIsInvitedStatus.getNumber(),
            friendsWithIsInvitedStatus.getTotalPages());
    }

    private boolean isCurrentUserFollower(Habit habit, Long currentUserId) {
        return habit.getFollowers().stream()
            .anyMatch(user -> user.getId().equals(currentUserId));
    }

    private Habit findHabitById(Long habitId) {
        return habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId));
    }

    private User getHabitAuthor(Habit habit) {
        return userRepo.findById(habit.getUserId())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + habit.getId()));
    }

    /**
     * Removes a like from the habit if the user has already liked it. Returns true
     * if a like was removed, false otherwise.
     */
    private boolean removeLikeIfExists(Habit habit, UserVO userVO, User habitAuthor) {
        boolean userLiked = habit.getUsersLiked().stream()
            .anyMatch(user -> user.getId().equals(userVO.getId()));

        Long habitId = habit.getId();
        if (userLiked) {
            habit.getUsersLiked().removeIf(user -> user.getId().equals(userVO.getId()));
            achievementCalculation.calculateAchievement(userVO, AchievementCategoryType.LIKE_HABIT,
                AchievementAction.DELETE);
            ratingCalculation.ratingCalculation(ratingPointsRepo.findByNameOrThrow("UNDO_LIKE_HABIT"), userVO);

            if (habitAuthor != null) {
                userNotificationService.removeActionUserFromNotification(modelMapper.map(habitAuthor, UserVO.class),
                    userVO, habitId, NotificationType.HABIT_LIKE);
            }
            return true;
        }
        return false;
    }

    /**
     * Removes a dislike from the habit if the user has already disliked it. Returns
     * true if a dislike was removed, false otherwise.
     */
    private boolean removeDislikeIfExists(Habit habit, UserVO userVO) {
        boolean userDisliked = habit.getUsersDisliked().stream()
            .anyMatch(user -> user.getId().equals(userVO.getId()));

        if (userDisliked) {
            habit.getUsersDisliked().removeIf(user -> user.getId().equals(userVO.getId()));
            return true;
        }
        return false;
    }

    private Page<UserFriendHabitInviteDto> findUserFriendsWithHabitInvitesMapped(
        Long userId, String name, Long habitId, Pageable pageable) {
        List<Tuple> tuples = habitInvitationRepo.findUserFriendsWithHabitInvites(userId, name, habitId, pageable);
        List<UserFriendHabitInviteDto> dtoList = tuples.stream()
            .map(tuple -> UserFriendHabitInviteDto.builder()
                .id(tuple.get("id", Long.class))
                .name(tuple.get("name", String.class))
                .email(tuple.get("email", String.class))
                .profilePicturePath(tuple.get("profile_picture", String.class))
                .hasInvitation(tuple.get("has_invitation", Boolean.class))
                .hasAcceptedInvitation(tuple.get("has_accepted_invitation", Boolean.class))
                .build())
            .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, dtoList.size());
    }
}
