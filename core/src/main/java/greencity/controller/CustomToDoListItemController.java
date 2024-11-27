package greencity.controller;

import greencity.annotations.CurrentUser;
import greencity.constant.HttpStatuses;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.user.UserVO;
import greencity.service.CustomToDoListItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/habits/custom-to-do-list-items")
public class CustomToDoListItemController {
    private final CustomToDoListItemService customToDoListItemService;

    /**
     * Method finds all available custom to-do list items for habit.
     *
     * @param habitId {@link Long} with needed habit id.
     * @return list of {@link CustomToDoListItemResponseDto}
     */
    @Operation(summary = "Get all custom to-do-list-items for habit.")
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
    public ResponseEntity<List<CustomToDoListItemResponseDto>> getAllCustomToDoListItemsForHabit(
        @Parameter(hidden = true) @CurrentUser UserVO user,
        @Parameter @PathVariable @Min(1) Long habitId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(customToDoListItemService.findAllHabitCustomToDoList(user.getId(), habitId));
    }

    /**
     * Method finds all available custom to-do list items for adding to habit
     * assign.
     *
     * @param habitAssignId {@link Long} with needed habit assign id.
     * @return list of {@link CustomToDoListItemResponseDto}
     */
    @Operation(summary = "Get all not added custom to-do-list-items for habit assign.")
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
    public ResponseEntity<List<CustomToDoListItemResponseDto>> getAllNotAddedCustomToDoListItemsForHabitAssign(
        @Parameter(hidden = true) @CurrentUser UserVO user,
        @Parameter @PathVariable @Min(1) Long habitAssignId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(customToDoListItemService.findAvailableCustomToDoListForHabitAssign(user.getId(), habitAssignId));
    }
}
