package greencity.webcontroller;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.genericresponse.GenericResponseDto;
import greencity.dto.todolistitem.*;
import greencity.service.HabitToDoListItemService;
import greencity.service.ToDoListItemService;
import greencity.service.LanguageService;
import java.util.List;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PutMapping;
import static greencity.dto.genericresponse.GenericResponseDto.buildGenericResponseDto;

@Controller
@RequiredArgsConstructor
@RequestMapping("/management/to-do-list-items")
public class ManagementToDoListItemsController {
    private final ToDoListItemService toDoListItemService;
    private final LanguageService languageService;
    private final HabitToDoListItemService habitToDoListItemService;

    /**
     * Method that returns management page with all {@link ToDoListItemVO}.
     *
     * @param query    Query for searching related data
     * @param model    Model that will be configured and returned to user.
     * @param pageable {@link Pageable}.
     * @return View template path {@link String}.
     */
    @GetMapping
    public String getAllToDoListItems(@RequestParam(required = false, name = "query") String query,
        Pageable pageable,
        Model model) {
        Pageable paging = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").ascending());
        PageableAdvancedDto<ToDoListItemManagementDto> pageableDto = query == null || query.isEmpty()
            ? toDoListItemService.findToDoListItemsForManagementByPage(paging)
            : toDoListItemService.searchBy(paging, query);
        model.addAttribute("toDoListItems", pageableDto);
        model.addAttribute("languages", languageService.getAllLanguages());
        return "core/management_to_do_list_items";
    }

    /**
     * The method which save To-Do list item {@link ToDoListItemVO}.
     *
     * @param toDoListItemPostDto {@link ToDoListItemPostDto}
     * @return {@link ResponseEntity}
     * @author Dmytro Khonko
     */
    @PostMapping
    @ResponseBody
    public GenericResponseDto save(@Valid @RequestBody ToDoListItemPostDto toDoListItemPostDto,
        BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            toDoListItemService.saveToDoListItem(toDoListItemPostDto);
        }
        return GenericResponseDto.buildGenericResponseDto(bindingResult);
    }

    /**
     * The method which update {@link ToDoListItemTranslationVO}.
     *
     * @param toDoListItemPostDto {@link ToDoListItemPostDto}
     * @return {@link ResponseEntity}
     * @author Dmytro Khonko
     */
    @PutMapping("/{id}")
    @ResponseBody
    public GenericResponseDto update(
        @Valid @RequestBody ToDoListItemPostDto toDoListItemPostDto, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            toDoListItemService.update(toDoListItemPostDto);
        }
        return buildGenericResponseDto(bindingResult);
    }

    /**
     * Method to find to-do list item by id.
     *
     * @return {@link ToDoListItemVO} instance.
     * @author Dmytro Khonko
     */
    @GetMapping("/{id}")
    public ResponseEntity<ToDoListItemResponseWithTranslationDto> getToDoListItemById(
        @PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(toDoListItemService.findToDoListItemById(id));
    }

    /**
     * The method which delete {@link ToDoListItemVO}.
     *
     * @param id of {@link ToDoListItemVO}
     * @return {@link ResponseEntity}
     * @author Dmytro Khonko
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(toDoListItemService.delete(id));
    }

    /**
     * Method which deletes {@link ToDoListItemVO} and
     * {@link ToDoListItemTranslationVO} by given id.
     *
     * @param listId list of IDs
     * @return {@link ResponseEntity}
     * @author Dmytro Khonko
     */
    @DeleteMapping("/deleteAll")
    @ResponseBody
    public ResponseEntity<List<Long>> deleteAll(@RequestBody List<Long> listId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(toDoListItemService.deleteAllToDoListItemsByListOfId(listId));
    }

    /**
     * Method which deletes HabitToDoListItem.
     *
     * @param toDoIds list of IDs
     * @author Vira Maksymets
     */
    @DeleteMapping("/unlink/{habitId}")
    @ResponseBody
    public ResponseEntity<Long> unlinkToDoListItems(@RequestBody List<Long> toDoIds, @PathVariable Long habitId) {
        habitToDoListItemService.unlinkToDoListItems(toDoIds, habitId);
        return ResponseEntity.status(HttpStatus.OK).body(habitId);
    }

    /**
     * Method that returns management page with filtered {@link ToDoListItemVO}.
     *
     * @param model    Model that will be configured and returned to user.
     * @param pageable {@link Pageable}.
     * @return View template path {@link String}.
     */
    @PostMapping(value = "/filter")
    public String filterData(Model model, @PageableDefault(value = 20) @Parameter(hidden = true) Pageable pageable,
        ToDoListItemViewDto goal) {
        PageableAdvancedDto<ToDoListItemManagementDto> pageableDto =
            toDoListItemService.getFilteredDataForManagementByPage(
                pageable,
                goal);
        model.addAttribute("toDoListItems", pageableDto);
        model.addAttribute("languages", languageService.getAllLanguages());
        model.addAttribute("fields", goal);
        return "core/management_to_do_list_items";
    }
}
