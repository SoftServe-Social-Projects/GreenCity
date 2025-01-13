package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.CurrentUser;
import greencity.annotations.ValidLanguage;
import greencity.constant.HttpStatuses;
import greencity.dto.todolistitem.ToDoListItemResponseDto;
import greencity.dto.user.UserVO;
import greencity.service.ToDoListItemService;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import java.util.List;
import java.util.Locale;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/habits/to-do-list-items")
public class ToDoListItemController {
    private final ToDoListItemService toDoListItemService;

    /**
     * Method finds all available to-do list items for habit in specific language.
     *
     * @param locale  {@link Locale} with needed language code.
     * @param habitId {@link Long} with needed habit id.
     * @return list of {@link ToDoListItemResponseDto}.
     */
    @Operation(description = "Get all available to-do list for habit.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/{habitId}")
    @ApiLocale
    public ResponseEntity<List<ToDoListItemResponseDto>> getAllToDoListItemsForHabit(
        @Parameter @PathVariable @Min(1) Long habitId,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(toDoListItemService.findAllHabitToDoList(habitId, locale.getLanguage()));
    }

    /**
     * Method finds all available to-do list items for adding to habit assign in
     * specific language.
     *
     * @param locale        {@link Locale} with needed language code.
     * @param habitAssignId {@link Long} with needed habit assign id.
     * @return List of {@link ToDoListItemResponseDto}.
     */
    @Operation(description = "Get all not added to-do list for habit assign.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/assign/{habitAssignId}")
    @ApiLocale
    public ResponseEntity<List<ToDoListItemResponseDto>> getAllNotAddedToDoListItemsForHabitAssign(
        @Parameter(hidden = true) @CurrentUser UserVO user,
        @Parameter @PathVariable @Min(1) Long habitAssignId,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(toDoListItemService.findAvailableToDoListForHabitAssign(user.getId(), habitAssignId,
                locale.getLanguage()));
    }
}
