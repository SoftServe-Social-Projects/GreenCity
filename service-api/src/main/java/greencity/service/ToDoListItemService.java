package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.todolistitem.*;
import greencity.dto.habit.HabitVO;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.dto.user.UserToDoListItemResponseDto;
import greencity.dto.user.UserToDoListItemVO;
import greencity.dto.user.UserVO;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ToDoListItemService {
    /**
     * Method returns to-do list, available for tracking for specific language.
     *
     * @param language needed language code
     * @return List of {@link ToDoListItemDto}.
     */
    List<ToDoListItemDto> findAll(String language);

    /**
     * Method for saving to-do list item from {@link ToDoListItemPostDto}.
     *
     * @param toDoListItemPostDto needed text
     * @author Dmytro Khonko
     */
    List<LanguageTranslationDTO> saveToDoListItem(ToDoListItemPostDto toDoListItemPostDto);

    /**
     * Method to update to-do list item translations from
     * {@link ToDoListItemPostDto}.
     *
     * @param toDoListItemPostDto new text
     * @author Dmytro Khonko
     */
    List<LanguageTranslationDTO> update(ToDoListItemPostDto toDoListItemPostDto);

    /**
     * Method delete to-do list item.
     *
     * @param id id of to-do list item you need to delete
     * @author Dmytro Khonko
     */
    Long delete(Long id);

    /**
     * Method to find to-do list items.
     *
     * @param pageable our page
     * @author Dmytro Khonko
     */
    PageableAdvancedDto<ToDoListItemManagementDto> findToDoListItemsForManagementByPage(Pageable pageable);

    /**
     * Method search to-do list items.
     *
     * @param paging our page.
     * @param query  search request
     * @author Dmytro Khonko
     */
    PageableAdvancedDto<ToDoListItemManagementDto> searchBy(Pageable paging, String query);

    /**
     * Method delete few to-do list items.
     *
     * @param listId ids of to-do list items you need to delete
     * @author Dmytro Khonko
     */
    List<Long> deleteAllToDoListItemsByListOfId(List<Long> listId);

    /**
     * Method to find to-do list item by id.
     *
     * @param id id of to-do list item you need to find
     * @author Dmytro Khonko
     */
    ToDoListItemResponseDto findToDoListItemById(Long id);

    /**
     * Method to filter to-do list items.
     *
     * @param dto data of to-do list item you need to find
     * @author Dmytro Khonko
     */
    PageableAdvancedDto<ToDoListItemManagementDto> getFilteredDataForManagementByPage(Pageable pageable,
        ToDoListItemViewDto dto);

    /**
     * Method assign to user list of user to-do list items available for habit.
     *
     * @param userId   id of the {@link UserVO} current user.
     * @param language needed language code.
     * @param habitId  id of the {@link HabitVO}.
     * @return List of saved {@link UserToDoListItemResponseDto} with specific
     *         language.
     */
    List<UserToDoListItemResponseDto> saveUserToDoListItems(Long userId, Long habitId,
        List<ToDoListItemRequestDto> dto, String language);

    /**
     * Method returns list of user to-do list for specific language.
     *
     * @param userId   id of the {@link UserVO} current user.
     * @param language needed language code.
     * @return List of {@link UserToDoListItemResponseDto}.
     */
    List<UserToDoListItemResponseDto> getUserToDoList(Long userId, Long habitId, String language);

    /**
     * Method returns list of user to-do list items by habitAssignId, specific
     * language and INPROGRESS status.
     *
     * @param habitAssignId id of the {@link Long} current user.
     * @param language      needed language code.
     * @return List of {@link UserToDoListItemResponseDto}.
     */
    List<UserToDoListItemResponseDto> getUserToDoListItemsByHabitAssignIdAndStatusInProgress(
        Long habitAssignId, String language);

    /**
     * Method returns user to-do list by habitAssignId for specific language.
     *
     * @param userId        id of the {@link UserVO} current user.
     * @param habitAssignId {@link greencity.dto.habit.HabitAssignVO} id.
     * @param language      needed language code.
     * @return List of {@link UserToDoListItemResponseDto}.
     */
    List<UserToDoListItemResponseDto> getUserToDoListByHabitAssignId(Long userId, Long habitAssignId,
        String language);

    /**
     * Method for deleting to-do list item from user`s to-do list.
     *
     * @param userId  id of the {@link UserVO} current user.
     * @param habitId id of the {@link HabitVO}.
     * @param itemId  id of the {@link ToDoListItemVO}.
     */
    void deleteUserToDoListItemByItemIdAndUserIdAndHabitId(Long itemId, Long userId, Long habitId);

    /**
     * Method update status of user to-do list item to done.
     *
     * @param userId   id of the {@link UserVO} current user.
     * @param itemId   - {@link UserToDoListItemVO}'s id that should be updated.
     * @param language needed language code.
     * @return {@link UserToDoListItemResponseDto} with specific language.
     */
    UserToDoListItemResponseDto updateUserToDoListItemStatus(Long userId, Long itemId, String language);

    /**
     * Method update status of user to-do list item to do.
     *
     * @param userId             id of the {@link UserVO} current user.
     * @param userToDoListItemId - {@link UserToDoListItemVO}'s id that should be
     *                           updated.
     * @param language           needed language code.
     * @param status             needed language code.
     * @return {@link UserToDoListItemResponseDto} with specific language.
     */
    List<UserToDoListItemResponseDto> updateUserToDoListItemStatus(Long userId,
        Long userToDoListItemId,
        String language,
        String status);

    /**
     * Method for deleted list of user to-do list items.
     *
     * @param ids string with ids object for deleting.
     * @return list ids of deleted
     * @author Bogdan Kuzenko
     */
    List<Long> deleteUserToDoListItems(String ids);

    /**
     * Method returns list of hopping list items for habit.
     *
     * @param habitId id of the {@link HabitVO}.
     * @return List of {@link ToDoListItemManagementDto}.
     * @author Marian Diakiv
     */
    List<ToDoListItemManagementDto> getToDoListByHabitId(Long habitId);

    /**
     * Method returns to-do list items that are not in the habit.
     *
     * @param habitId  id of the {@link HabitVO}.
     * @param pageable - instance of {@link Pageable}.
     * @return Pageable of {@link ToDoListItemManagementDto}.
     * @author Marian Diakiv
     */
    PageableAdvancedDto<ToDoListItemManagementDto> findAllToDoListItemsForManagementPageNotContained(
        Long habitId, Pageable pageable);

    /**
     * Method returns user's to-do list for active items and habits in progress.
     *
     * @param userId id of the {@link Long} current user
     * @param code   language code {@link String}
     * @return {@link ToDoListItemDto}
     */
    List<ToDoListItemDto> findInProgressByUserIdAndLanguageCode(Long userId, String code);
}
