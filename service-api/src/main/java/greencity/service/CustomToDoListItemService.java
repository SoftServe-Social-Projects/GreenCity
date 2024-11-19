package greencity.service;

import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.todolistitem.CustomToDoListItemSaveRequestDto;
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
     * @param dtoList list of {@link CustomToDoListItemSaveRequestDto}
     *                                      with objects list for saving.
     * @param userId                        {@link UserVO} current user id
     * @return list of saved {@link CustomToDoListItemResponseDto}
     */
    List<CustomToDoListItemResponseDto> save(List<CustomToDoListItemSaveRequestDto> dtoList,
        Long userId, Long habitAssignId);

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
     * @param ids list of id for deleting.
     * @return list ids of deleted custom to-do list items
     */
    List<Long> bulkDelete(List<Long> ids);

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
     * Method returns all user's custom to-do items by status(if is defined).
     *
     * @param userId user id.
     * @return list of {@link CustomToDoListItemVO}
     */
    List<CustomToDoListItemResponseDto> findAllUsersCustomToDoListItemsByStatus(Long userId, String status);
}
