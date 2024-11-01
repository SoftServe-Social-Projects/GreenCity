package greencity.service;

import greencity.dto.todolistitem.BulkSaveCustomToDoListItemDto;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.todolistitem.CustomToDoListItemVO;
import greencity.dto.user.UserVO;
import java.util.List;

/**
 * Provides the interface to manage {@code CustomToDoList} entity.
 *
 * @author Bogdan Kuzenko
 */
public interface CustomToDoListItemService {
    /**
     * Method saves list of custom to-do list items for user.
     *
     * @param bulkSaveCustomToDoListItemDto {@link BulkSaveCustomToDoListItemDto}
     *                                      with objects list for saving.
     * @param userId                        {@link UserVO} current user id
     * @return list of saved {@link CustomToDoListItemResponseDto}
     */
    List<CustomToDoListItemResponseDto> save(BulkSaveCustomToDoListItemDto bulkSaveCustomToDoListItemDto,
        Long userId, Long habitAssignId);

    /**
     * Method for finding all custom to-do list items.
     *
     * @return list of {@link CustomToDoListItemResponseDto}
     */
    List<CustomToDoListItemResponseDto> findAll();

    /**
     * Method for finding all custom to-do list item for one user.
     *
     * @param userId user id.
     * @return list of {@link CustomToDoListItemResponseDto}
     */
    List<CustomToDoListItemResponseDto> findAllByUserAndHabit(Long userId, Long habitId);

    /**
     * Method for finding one custom to-do list item by id.
     *
     * @param id - custom to-do list item id.
     * @return {@link CustomToDoListItemResponseDto}
     */
    CustomToDoListItemResponseDto findById(Long id);

    /**
     * Method update custom to-do items status.
     *
     * @param userId     {@link Long} user id.
     * @param itemId     {@link Long} custom to-do list item id.
     * @param itemStatus {@link String} custom to-do list item status.
     * @return {@link CustomToDoListItemResponseDto}
     */
    CustomToDoListItemResponseDto updateItemStatus(Long userId, Long itemId, String itemStatus);

    /**
     * Method updates user's to-do item status to DONE.
     *
     * @param userId {@link Long} user id.
     * @param itemId {@link Long} custom to-do list item id.
     */
    void updateItemStatusToDone(Long userId, Long itemId);

    /**
     * Method for deleted list of custom to-do list items.
     *
     * @param ids string with objects id for deleting.
     * @return list ids of deleted custom to-do list items
     */
    List<Long> bulkDelete(String ids);

    /**
     * Method for finding all custom to-do list items.
     *
     * @param userId user id.
     * @return list of {@link CustomToDoListItemVO}
     */
    List<CustomToDoListItemResponseDto> findAllAvailableCustomToDoListItems(Long userId, Long habitId);

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

    /**
     * Method for finding custom to-do list items by userId and habitId and
     * INPROGRESS status.
     *
     * @param userId  user id.
     * @param habitId habit id.
     * @return list of {@link CustomToDoListItemResponseDto}
     */
    List<CustomToDoListItemResponseDto> findAllCustomToDoListItemsWithStatusInProgress(Long userId,
        Long habitId);

    /**
     * Method returns all user's custom to-do items by status(if is defined).
     *
     * @param userId user id.
     * @return list of {@link CustomToDoListItemVO}
     */
    List<CustomToDoListItemResponseDto> findAllUsersCustomToDoListItemsByStatus(Long userId, String status);
}
