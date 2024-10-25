package greencity.service;

import greencity.achievement.AchievementCalculation;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableDto;
import greencity.dto.filter.HabitTranslationFilterDto;
import greencity.dto.habit.CustomHabitDtoRequest;
import greencity.dto.habit.CustomHabitDtoResponse;
import greencity.dto.habit.HabitDto;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.dto.shoppinglistitem.ShoppingListItemDto;
import greencity.dto.user.UserProfilePictureDto;
import greencity.dto.user.UserVO;
import greencity.entity.CustomShoppingListItem;
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
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoFriendWithIdException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.exception.exceptions.WrongEmailException;
import greencity.mapping.CustomHabitMapper;
import greencity.mapping.CustomShoppingListMapper;
import greencity.mapping.CustomShoppingListResponseDtoMapper;
import greencity.mapping.HabitTranslationDtoMapper;
import greencity.mapping.HabitTranslationMapper;
import greencity.rating.RatingCalculation;
import greencity.repository.HabitRepo;
import greencity.repository.HabitTranslationRepo;
import greencity.repository.ShoppingListItemTranslationRepo;
import greencity.repository.HabitAssignRepo;
import greencity.repository.RatingPointsRepo;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import greencity.repository.CustomShoppingListItemRepo;
import greencity.repository.LanguageRepo;
import greencity.repository.TagsRepo;
import greencity.repository.UserRepo;
import greencity.repository.options.HabitTranslationFilter;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
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
    private final CustomShoppingListResponseDtoMapper customShoppingListResponseDtoMapper;
    private final HabitTranslationDtoMapper habitTranslationDtoMapper;
    private final CustomShoppingListMapper customShoppingListMapper;
    private final HabitTranslationMapper habitTranslationMapper;
    private final CustomHabitMapper customHabitMapper;
    private final ShoppingListItemTranslationRepo shoppingListItemTranslationRepo;
    private final CustomShoppingListItemRepo customShoppingListItemRepo;
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
        List<ShoppingListItemDto> shoppingListItems = new ArrayList<>();
        shoppingListItemTranslationRepo
            .findShoppingListByHabitIdAndByLanguageCode(languageCode, id)
            .forEach(x -> shoppingListItems.add(modelMapper.map(x, ShoppingListItemDto.class)));
        habitDto.setShoppingListItems(shoppingListItems);
        habitDto.setAmountAcquiredUsers(habitAssignRepo.findAmountOfUsersAcquired(habitDto.getId()));
        boolean isCustomHabit = habit.getIsCustomHabit();
        habitDto.setIsCustomHabit(isCustomHabit);
        habitDto.setUsersIdWhoCreatedCustomHabit(habit.getUserId());
        if (isCustomHabit) {
            habitDto.setCustomShoppingListItems(
                customShoppingListResponseDtoMapper.mapAllToList(habit.getCustomShoppingListItems()));
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
            habitDto.setCustomShoppingListItems(
                customShoppingListResponseDtoMapper.mapAllToList(habit.getCustomShoppingListItems()));
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
    public List<ShoppingListItemDto> getShoppingListForHabit(Long habitId, String lang) {
        return shoppingListItemTranslationRepo.findShoppingListByHabitIdAndByLanguageCode(lang, habitId)
            .stream()
            .map(g -> modelMapper.map(g, ShoppingListItemDto.class))
            .collect(Collectors.toList());
    }

    @Override
    public void addShoppingListItemToHabit(Long habitId, Long itemId) {
        habitRepo.addShopingListItemToHabit(habitId, itemId);
    }

    @Override
    public void deleteShoppingListItem(Long habitId, Long itemId) {
        habitRepo.upadateShopingListItemInHabit(habitId, itemId);
    }

    @Override
    public List<Long> deleteAllShoppingListItemsByListOfId(Long habitId, List<Long> listId) {
        listId.forEach(id -> deleteShoppingListItem(habitId, id));
        return listId;
    }

    @Override
    public List<Long> addAllShoppingListItemsByListOfId(Long habitId, List<Long> listId) {
        listId.forEach(id -> addShoppingListItemToHabit(habitId, id));
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
        setCustomShoppingListItemToHabit(addCustomHabitDtoRequest, habit, user);
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

        response.setCustomShoppingListItemDto(customShoppingListResponseDtoMapper
            .mapAllToList(customShoppingListItemRepo.findAllByUserIdAndHabitId(userId, habit.getId())));
        response.setTagIds(habit.getTags().stream().map(Tag::getId).collect(Collectors.toSet()));
        response
            .setHabitTranslations(habitTranslationDtoMapper.mapAllToList(habitTranslationRepo.findAllByHabit(habit)));
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserProfilePictureDto> getFriendsAssignedToHabitProfilePictures(Long habitId, Long userId) {
        if (!userRepo.existsById(userId)) {
            throw new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + userId);
        }
        if (!habitRepo.existsById(habitId)) {
            throw new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId);
        }
        List<User> users = userRepo.getFriendsAssignedToHabit(userId, habitId);
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
        if (isNotEmpty(habitDto.getCustomShoppingListItemDto())) {
            updateExistingCustomShoppingListItems(habitDto, toUpdate, user);
            saveNewCustomShoppingListItemsToUpdate(habitDto, toUpdate, user);
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

    private void saveNewCustomShoppingListItemsToUpdate(CustomHabitDtoRequest habitDto, Habit habit, User user) {
        List<CustomShoppingListItem> customShoppingListItems = customShoppingListMapper
            .mapAllToList(habitDto.getCustomShoppingListItemDto());

        customShoppingListItems.stream()
            .filter(item -> Objects.isNull(item.getId()))
            .forEach(customShoppingListItem -> {
                customShoppingListItem.setHabit(habit);
                customShoppingListItem.setUser(user);
                customShoppingListItemRepo.save(customShoppingListItem);
            });
    }

    private void updateExistingCustomShoppingListItems(CustomHabitDtoRequest habitDto, Habit habit, User user) {
        List<CustomShoppingListItem> customShoppingListItems = customShoppingListItemRepo
            .findAllByUserIdAndHabitId(user.getId(), habit.getId());

        customShoppingListItems
            .forEach(item -> habitDto.getCustomShoppingListItemDto().stream()
                .filter(itemToUpdate -> item.getId().equals(itemToUpdate.getId()))
                .forEach(itemToUpdate -> {
                    item.setStatus(itemToUpdate.getStatus());
                    item.setText(itemToUpdate.getText());
                }));

        customShoppingListItemRepo.deleteAll(customShoppingListItems.stream()
            .filter(item -> habitDto.getCustomShoppingListItemDto().stream()
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
        List<HabitTranslation> habitTranslationListForUa = mapHabitTranslationFromAddCustomHabitDtoRequest(habitDto);
        habitTranslationListForUa.forEach(habitTranslation -> habitTranslation.setHabit(habit));
        habitTranslationListForUa.forEach(habitTranslation -> habitTranslation.setLanguage(
            languageRepo.findByCode("ua").orElseThrow(NoSuchElementException::new)));
        habitTranslationRepo.saveAll(habitTranslationListForUa);

        List<HabitTranslation> habitTranslationListForEn = mapHabitTranslationFromAddCustomHabitDtoRequest(habitDto);
        habitTranslationListForEn.forEach(habitTranslation -> habitTranslation.setHabit(habit));
        habitTranslationListForEn.forEach(habitTranslation -> habitTranslation.setLanguage(
            languageRepo.findByCode("en").orElseThrow(NoSuchElementException::new)));
        habitTranslationRepo.saveAll(habitTranslationListForEn);
    }

    private List<HabitTranslation> mapHabitTranslationFromAddCustomHabitDtoRequest(CustomHabitDtoRequest habitDto) {
        return habitTranslationMapper.mapAllToList(habitDto.getHabitTranslations());
    }

    private void setTagsIdsToHabit(CustomHabitDtoRequest habitDto, Habit habit) {
        habit.setTags(habitDto.getTagIds().stream().map(tagId -> tagsRepo.findById(tagId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.TAG_NOT_FOUND + tagId)))
            .collect(Collectors.toSet()));
    }

    private void setCustomShoppingListItemToHabit(CustomHabitDtoRequest habitDto, Habit habit, User user) {
        List<CustomShoppingListItem> customShoppingListItems =
            customShoppingListMapper.mapAllToList(habitDto.getCustomShoppingListItemDto());
        customShoppingListItems.forEach(customShoppingListItem -> customShoppingListItem.setHabit(habit));
        customShoppingListItems.forEach(customShoppingListItem -> customShoppingListItem.setUser(user));
        customShoppingListItemRepo.saveAll(customShoppingListItems);
        habit.setCustomShoppingListItems(customShoppingListItems);
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
        Habit habit = habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId));

        User habitAuthor = null;

        if (habit.getUserId() != null) {
            habitAuthor = userRepo.findById(habit.getUserId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + habitId));
        }
        if (habit.getUsersLiked().stream().anyMatch(user -> user.getId().equals(userVO.getId()))) {
            habit.getUsersLiked().removeIf(user -> user.getId().equals(userVO.getId()));
            ratingCalculation.ratingCalculation(ratingPointsRepo.findByNameOrThrow("UNDO_LIKE_HABIT"), userVO);
            achievementCalculation.calculateAchievement(userVO, AchievementCategoryType.LIKE_HABIT,
                AchievementAction.DELETE);
            if (habitAuthor != null) {
                userNotificationService.removeActionUserFromNotification(modelMapper.map(habitAuthor, UserVO.class),
                    userVO, habitId, NotificationType.HABIT_LIKE);
            }
        } else {
            habit.getUsersLiked().add(modelMapper.map(userVO, User.class));
            ratingCalculation.ratingCalculation(ratingPointsRepo.findByNameOrThrow("LIKE_HABIT"), userVO);
            achievementCalculation.calculateAchievement(userVO, AchievementCategoryType.LIKE_HABIT,
                AchievementAction.ASSIGN);
            if (habitAuthor != null) {
                sendHabitLikeNotification(habitAuthor, userVO, habitId, habit);
            }
        }
        habitRepo.save(habit);
    }

    private void sendHabitLikeNotification(User targetUser, UserVO actionUser, Long habitId, Habit habit) {
        userNotificationService.createOrUpdateLikeNotification(modelMapper.map(targetUser, UserVO.class),
            actionUser, habitId, habit.getHabitTranslations().getFirst().getName(),
            NotificationType.HABIT_LIKE, true);
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
        return assignHabitStatus(habitAssigns) == HabitAssignStatus.INPROGRESS;
    }
}
