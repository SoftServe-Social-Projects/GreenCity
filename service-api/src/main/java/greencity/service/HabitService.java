package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.friends.UserFriendHabitInviteDto;
import greencity.dto.habit.CustomHabitDtoRequest;
import greencity.dto.habit.CustomHabitDtoResponse;
import greencity.dto.habit.HabitVO;
import greencity.dto.habit.HabitDto;
import greencity.dto.user.UserProfilePictureDto;
import greencity.dto.user.UserVO;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

public interface HabitService {
    /**
     * Method finds {@code Habit} by id and language code.
     *
     * @param id           {@code Habit} id.
     * @param languageCode - language code.
     * @return {@link HabitDto}.
     */
    HabitDto getByIdAndLanguageCode(Long id, String languageCode);

    /**
     * Method returns all default and custom which created by current user his
     * friends {@code Habit}'s.
     *
     * @param pageable - instance of {@link Pageable}.
     * @return Pageable of {@link HabitDto}.
     */
    PageableDto<HabitDto> getAllHabitsByLanguageCode(UserVO userVO, Pageable pageable, String languageCode);

    /**
     * Method returns all habits of the current user.
     *
     * @param pageable - instance of {@link Pageable}.
     * @return Pageable of {@link HabitDto}.
     */
    PageableDto<HabitDto> getMyHabits(Long userId, Pageable pageable, String languageCode);

    /**
     * Method returns all habits of a friend, both default and custom, for the
     * current user and the specified friend.
     *
     * @param userId   - ID of the current user.
     * @param friendId - ID of the friend whose habits are to be retrieved.
     * @param pageable - instance of {@link Pageable}.
     * @return Pageable of {@link HabitDto}.
     */
    PageableDto<HabitDto> getAllHabitsOfFriend(Long userId, Long friendId, Pageable pageable, String languageCode);

    /**
     * Method returns all mutual habits, both default and custom, that are shared
     * between the current user and a specified friend.
     *
     * @param userId   - ID of the current user.
     * @param friendId - ID of the friend whose mutual habits with the user are to
     *                 be retrieved.
     * @param pageable - instance of {@link Pageable}.
     * @return Pageable of {@link HabitDto}.
     */
    PageableDto<HabitDto> getAllMutualHabitsWithFriend(Long userId, Long friendId, Pageable pageable,
        String languageCode);

    /**
     * Method that find all habit's translations by language code and tags.
     *
     * @param pageable        {@link Pageable}
     * @param tags            {@link List} of {@link String} tags
     * @param languageCode    language code {@link String}
     * @param excludeAssigned {@link boolean} flag to determine whether to exclude
     *                        habits already assigned to the specified user.
     * @param userId          {@link Long} representing the ID of the user for whom
     *                        assigned habits should be excluded (if applicable).
     *
     * @return {@link PageableDto} of {@link HabitDto}.
     * @author Markiyan Derevetskyi
     */
    PageableDto<HabitDto> getAllByTagsAndLanguageCode(Pageable pageable, List<String> tags, String languageCode,
        boolean excludeAssigned, Long userId);

    /**
     * Method that return all PageableDto of HabitDto by tags, isCustomHabit,
     * complexities, habitAssignStatus and language code.
     *
     * @param pageable      {@link Pageable}.
     * @param tags          {@link List} of {@link String}.
     * @param isCustomHabit {@link Boolean} value.
     * @param complexities  {@link List} of {@link Integer}.
     * @param languageCode  language code {@link String}.
     *
     * @return {@link PageableDto} of {@link HabitDto}.
     * @author Lilia Mokhnatska
     */
    PageableDto<HabitDto> getAllByDifferentParameters(UserVO userVO, Pageable pageable, Optional<List<String>> tags,
        Optional<Boolean> isCustomHabit, Optional<List<Integer>> complexities, String languageCode);

    /**
     * Method that add to-do list item To Habit by habit id and to-do list item id.
     *
     * @author Marian Diakiv
     */
    void addToDoListItemToHabit(Long habitId, Long itemId);

    /**
     * Method for deleting the {@link greencity.dto.todolistitem.ToDoListItemVO}
     * instance by its id.
     *
     * @param itemId  - {@link greencity.dto.todolistitem.ToDoListItemVO} instance
     *                id which will be deleted.
     * @param habitId - {@link HabitDto} the id of the instance from which it will
     *                be deleted.
     * @author Marian Diakiv
     */
    void deleteToDoListItem(Long habitId, Long itemId);

    /**
     * Method deletes all {@link greencity.dto.todolistitem.ToDoListItemVO} by list
     * of ids.
     *
     * @param listId  list of id {@link greencity.dto.todolistitem.ToDoListItemVO}
     * @param habitId - {@link HabitDto} the id of the instance from which it will
     *                be deleted. return list of id
     *                {@link greencity.dto.todolistitem.ToDoListItemVO}
     * @author Marian Diakiv
     */
    List<Long> deleteAllToDoListItemsByListOfId(Long habitId, List<Long> listId);

    /**
     * Method add all {@link greencity.dto.todolistitem.ToDoListItemVO} by list of
     * ids.
     *
     * @param listId  list of id {@link greencity.dto.todolistitem.ToDoListItemVO}
     * @param habitId - {@link HabitDto} the id of the instance to which it will be
     *                added return list of id
     *                {@link greencity.dto.todolistitem.ToDoListItemVO}
     * @author Marian Diakiv
     */
    List<Long> addAllToDoListItemsByListOfId(Long habitId, List<Long> listId);

    /**
     * Method to save {@link CustomHabitDtoResponse}.
     *
     * @param addCustomHabitDtoRequest dto with {@link CustomHabitDtoRequest}
     *                                 entered info about field that need to edit.
     * @param userEmail                {@link String} - user email.
     * @return {@link CustomHabitDtoResponse} instance.
     * @author Lilia Mokhnatska
     */
    CustomHabitDtoResponse addCustomHabit(CustomHabitDtoRequest addCustomHabitDtoRequest, MultipartFile image,
        String userEmail);

    /**
     * Retrieves a list of profile pictures of the user's friends who are associated
     * with a specified habit assignment through invitations. This includes both
     * friends who invited the user and friends whom the user has invited for this
     * habit assignment.
     *
     * @param habitAssignId The ID of the habit assignment.
     * @param userId        The ID of the user.
     * @return A list of {@link UserProfilePictureDto} representing the friends'
     *         profile pictures.
     */
    List<UserProfilePictureDto> getFriendsAssignedToHabitProfilePictures(Long habitAssignId, Long userId);

    /**
     * Method to update {@link CustomHabitDtoResponse}.
     *
     * @param customHabitDtoRequest dto with {@link CustomHabitDtoRequest} entered
     *                              info about field that need to edit.
     * @param userEmail             {@link String} - user email.
     * @return {@link CustomHabitDtoResponse} instance.
     * @author Olena Sotnik.
     */
    CustomHabitDtoResponse updateCustomHabit(CustomHabitDtoRequest customHabitDtoRequest, Long habitId,
        String userEmail, MultipartFile image);

    /**
     * Method for deleting of custom habit by its id.
     *
     * @param customHabitId - id of custom habit to be deleted.
     * @param ownerEmail    - email of user who owns the habit.
     *
     * @author Olena Sotnik.
     */
    void deleteCustomHabit(Long customHabitId, String ownerEmail);

    /**
     * Method to like or unlike {@link HabitVO} specified by id.
     *
     * @param habitId id of {@link HabitVO} to like/unlike.
     * @param userVO  current {@link UserVO} that wants to like/unlike.
     */
    void like(Long habitId, UserVO userVO);

    /**
     * Method to dislike or remove dislike {@link HabitVO} specified by id.
     *
     * @param habitId id of {@link HabitVO} to like/dislike.
     * @param userVO  current {@link UserVO} that wants to like/dislike.
     */
    void dislike(Long habitId, UserVO userVO);

    /**
     * Method for adding a habit to favorites by habitId.
     *
     * @param habitId - habit id
     * @param email   - email of user
     */
    void addToFavorites(Long habitId, String email);

    /**
     * Method for removing a habit from favorites by habitId.
     *
     * @param habitId - habit id.
     * @param email   - user email.
     */
    void removeFromFavorites(Long habitId, String email);

    /**
     * Method returns all favorite habits.
     *
     * @param pageable - instance of {@link Pageable}.
     * @return Pageable of {@link HabitDto}.
     */
    PageableDto<HabitDto> getAllFavoriteHabitsByLanguageCode(UserVO userVO, Pageable pageable, String languageCode);

    /**
     * Retrieves a paginated list of friends of a user with has invitation status.
     * Optionally filters by friend name.
     *
     * @param userVO   The current user's details.
     * @param name     Optional name filter for friends.
     * @param pageable .
     * @param habitId  The ID of the habit.
     * @return A paginated list of friends (UserFriendHabitInviteDto) who can be
     *         invited to the habit.
     */
    PageableDto<UserFriendHabitInviteDto> findAllFriendsOfUser(UserVO userVO, @Nullable String name,
        Pageable pageable, Long habitId);
}