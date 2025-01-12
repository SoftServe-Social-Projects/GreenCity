package greencity.service;

import greencity.dto.user.UserToDoListItemRequestDto;
import greencity.dto.user.UserToDoListItemRequestWithStatusDto;
import greencity.dto.user.UserToDoListItemResponseDto;
import java.util.List;

/**
 * Provides the interface to manage {@code UserToDoListItem} entity.
 */
public interface UserToDoListItemService {
    /**
     * Method returns list of user to-do list for habit assign in specific language.
     *
     * @param habitAssignId id of the {@link greencity.dto.habit.HabitAssignVO}
     *                      habit assign.
     * @param userId        id of {@link greencity.dto.user.UserVO} current user.
     * @param language      needed language code.
     * @return List of {@link UserToDoListItemResponseDto}.
     */
    List<UserToDoListItemResponseDto> findAllForHabitAssign(Long habitAssignId, Long userId, String language);

    /**
     * Method for saving user to-do list items from
     * {@link UserToDoListItemRequestDto} for habit assign.
     *
     * @param habitAssignId     id of the {@link greencity.dto.habit.HabitAssignVO}
     *                          habit assign.
     * @param userToDoListItems list of items to save.
     * @param userId            id of {@link greencity.dto.user.UserVO} current
     *                          user.
     * @param language          needed language code.
     */
    List<UserToDoListItemResponseDto> saveUserToDoListItems(Long habitAssignId,
        List<UserToDoListItemRequestDto> userToDoListItems, Long userId, String language);

    /**
     * Method delete user to-do list items.
     *
     * @param habitAssignId id of the {@link greencity.dto.habit.HabitAssignVO}
     *                      habit assign.
     * @param itemIds       list of {@link greencity.dto.user.UserToDoListItemVO}
     *                      ids to delete.
     * @param userId        id of {@link greencity.dto.user.UserVO} current user.
     */
    void deleteUserToDoListItems(Long habitAssignId, List<Long> itemIds, Long userId);

    /**
     * Method to change user to-do list items statuses.
     *
     * @param habitAssignId     id of the {@link greencity.dto.habit.HabitAssignVO}
     *                          habit assign.
     * @param userToDoListItems list of
     *                          {@link greencity.dto.user.UserToDoListItemVO} user
     *                          to-do list items with statuses
     * @param userId            id of {@link greencity.dto.user.UserVO} current
     *                          user.
     * @param language          needed language code.
     */
    List<UserToDoListItemResponseDto> changeStatusesUserToDoListItems(Long habitAssignId,
        List<UserToDoListItemRequestWithStatusDto> userToDoListItems, Long userId, String language);
}
