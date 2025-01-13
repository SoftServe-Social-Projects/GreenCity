package greencity.webcontroller;

import greencity.dto.todolistitem.ToDoListItemManagementDto;
import greencity.dto.habit.HabitDto;
import greencity.service.ToDoListItemService;
import greencity.service.HabitService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/management/habit-to-do-list")
public class ManagementHabitToDoListItemController {
    private final ToDoListItemService toDoListItemService;
    private final HabitService habitService;

    /**
     * Returns management page with {@link ToDoListItemManagementDto}.
     *
     * @param model Model that will be configured.
     * @return View template path {@link String}.
     * @author Marian Diakiv.
     */
    @GetMapping("")
    public String getAllToDoListItems(@RequestParam("habitId") Long id, Model model, Pageable pageable) {
        model.addAttribute("toDoListItems",
            toDoListItemService.findAllToDoListItemsForManagementPageNotContained(id, pageable));
        model.addAttribute("habitId", id);
        model.addAttribute("currentToDoListItems", toDoListItemService.getToDoListByHabitId(id));
        return "core/management_habit_to_do_list_item";
    }

    /**
     * Controller for deleting {@link greencity.dto.todolistitem.ToDoListItemVO} by
     * given id.
     *
     * @param listId  list of IDs.
     * @param habitId - {@link HabitDto} the id of the instance from which it will
     *                be deleted.
     * @return {@link ResponseEntity}
     */
    @DeleteMapping("/deleteAll/")
    public ResponseEntity<List<Long>> deleteAllToDoListItem(@RequestParam("habitId") Long habitId,
        @RequestBody List<Long> listId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitService.deleteAllToDoListItemsByListOfId(habitId, listId));
    }

    /**
     * Controller add all {@link greencity.dto.todolistitem.ToDoListItemVO} by list
     * of ids.
     *
     * @param listId  list of id {@link greencity.dto.todolistitem.ToDoListItemVO}
     * @param habitId - {@link HabitDto} the id of the instance to which it will be
     *                added.
     * @return {@link ResponseEntity}
     * @author Marian Diakiv.
     */
    @PostMapping("/addAll/")
    public ResponseEntity<List<Long>> addAllToDoListItem(@RequestParam("habitId") Long habitId,
        @RequestBody List<Long> listId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitService.addAllToDoListItemsByListOfId(habitId, listId));
    }

    /**
     * Controller for c the {@link greencity.dto.todolistitem.ToDoListItemVO}
     * instance by its id.
     *
     * @param itemId  - {@link greencity.dto.todolistitem.ToDoListItemVO} instance
     *                id which will be deleted.
     * @param habitId - {@link HabitDto} the id of the instance from which it will
     *                be deleted.
     * @return {@link ResponseEntity}
     * @author Marian Diakiv.
     */
    @DeleteMapping("/delete/")
    public ResponseEntity<Long> deleteToDoListItem(@RequestParam("habitId") Long habitId,
        @RequestParam("itemId") Long itemId) {
        habitService.deleteToDoListItem(habitId, itemId);
        return ResponseEntity.status(HttpStatus.OK).body(itemId);
    }

    /**
     * Controller add {@link greencity.dto.todolistitem.ToDoListItemVO} by id.
     *
     * @param habitId - {@link HabitDto} the id of the instance to which it will be
     *                added.
     * @return {@link ResponseEntity}
     * @author Marian Diakiv.
     */
    @PostMapping("/add/")
    public ResponseEntity<Long> addToDoListItemToHabit(@RequestParam("habitId") Long habitId,
        @RequestParam("itemId") Long itemId) {
        habitService.addToDoListItemToHabit(habitId, itemId);
        return ResponseEntity.status(HttpStatus.OK).body(itemId);
    }
}
