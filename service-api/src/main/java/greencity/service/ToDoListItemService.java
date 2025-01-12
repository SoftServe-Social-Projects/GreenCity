package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.todolistitem.*;
import greencity.dto.habit.HabitVO;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.dto.user.UserVO;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ToDoListItemService {
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
    ToDoListItemResponseWithTranslationDto findToDoListItemById(Long id);

    /**
     * Method to filter to-do list items.
     *
     * @param dto data of to-do list item you need to find
     * @author Dmytro Khonko
     */
    PageableAdvancedDto<ToDoListItemManagementDto> getFilteredDataForManagementByPage(Pageable pageable,
        ToDoListItemViewDto dto);

    /**
     * Method returns list of to-do list for habit in specific language.
     *
     * @param habitId  id of the {@link HabitVO} habit.
     * @param language needed language code.
     * @return List of {@link ToDoListItemResponseDto}.
     */
    List<ToDoListItemResponseDto> findAllHabitToDoList(Long habitId, String language);

    /**
     * Method returns list of to-do list items user not added to habit assign in
     * specific language.
     *
     * @param userId        id of the {@link UserVO} current user.
     * @param habitAssignId id of the {@link greencity.dto.habit.HabitAssignVO}
     *                      habit assign.
     * @param language      needed language code.
     * @return List of {@link ToDoListItemResponseDto}.
     */
    List<ToDoListItemResponseDto> findAvailableToDoListForHabitAssign(Long userId, Long habitAssignId,
        String language);

    /**
     * Method returns to-do list items by habitAssignId for specific language.
     *
     * @param userId        id of the {@link UserVO} current user.
     * @param habitAssignId {@link greencity.dto.habit.HabitAssignVO} id.
     * @param language      needed language code.
     * @return List of {@link ToDoListItemResponseDto}.
     */
    List<ToDoListItemResponseDto> getToDoListByHabitAssignId(Long userId, Long habitAssignId,
        String language);

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
}
