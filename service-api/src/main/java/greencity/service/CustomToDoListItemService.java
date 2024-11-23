package greencity.service;

import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.todolistitem.CustomToDoListItemVO;
import java.util.List;

/**
 * Provides the interface to manage {@code CustomToDoList} entity.
 *
 * @author Bogdan Kuzenko
 */
public interface CustomToDoListItemService {
    /**
     * Method for finding all custom to-do list items for habit.
     *
     * @param userId user id.
     * @param habitId habit id.
     * @return list of {@link CustomToDoListItemResponseDto}
     */
    List<CustomToDoListItemResponseDto> findAllCustomToDoListItemsForHabit(Long userId, Long habitId);

    /**
     * Method for finding all custom to-do list items user not added for habit assign.
     *
     * @param userId user id.
     * @param habitAssignId habit assign id.
     * @return list of {@link CustomToDoListItemResponseDto}
     */
    List<CustomToDoListItemResponseDto> findAvailableCustomToDoListItemsForHabitAssign(Long userId, Long habitAssignId);

    /**
     * Method for finding all custom to-do list items with not DISABLED status by
     * habitAssignId.
     *
     * @param userId        user id.
     * @param habitAssignId habitAssign id.
     * @return list of {@link CustomToDoListItemVO}
     */
    List<CustomToDoListItemResponseDto> findAllAvailableCustomToDoListItemsByHabitAssignId(Long userId,
        Long habitAssignId);
}
