package greencity.service;

import greencity.dto.habit.HabitVO;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.todolistitem.CustomToDoListItemVO;
import greencity.dto.user.UserVO;
import java.util.List;

/**
 * Provides the interface to manage {@code CustomToDoList} entity.
 */
public interface CustomToDoListItemService {
    /**
     * Method returns list of custom to-do list items for habit.
     *
     * @param userId  id of the {@link UserVO} current user.
     * @param habitId id of the {@link HabitVO} habit.
     * @return list of {@link CustomToDoListItemResponseDto}
     */
    List<CustomToDoListItemResponseDto> findAllHabitCustomToDoList(Long userId, Long habitId);

    /**
     * Method for finding all custom to-do list items user not added for habit
     * assign.
     *
     * @param habitAssignId id of the {@link greencity.dto.habit.HabitAssignVO}
     *                      habit assign.
     * @return list of {@link CustomToDoListItemResponseDto}
     */
    List<CustomToDoListItemResponseDto> findAvailableCustomToDoListForHabitAssign(Long userId, Long habitAssignId);

    /**
     * Method for finding all custom to-do list items with not DISABLED status by
     * habitAssignId.
     *
     * @param userId        id of the {@link UserVO} current user.
     * @param habitAssignId id of the {@link greencity.dto.habit.HabitAssignVO}
     *                      habit assign.
     * @return list of {@link CustomToDoListItemVO}
     */
    List<CustomToDoListItemResponseDto> getCustomToDoListByHabitAssignId(Long userId,
        Long habitAssignId);
}
