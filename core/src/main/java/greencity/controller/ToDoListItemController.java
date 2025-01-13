package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.CurrentUser;
import greencity.annotations.CurrentUserId;
import greencity.annotations.ValidLanguage;
import greencity.constant.HttpStatuses;
import greencity.constant.ValidationConstants;
import greencity.dto.todolistitem.ToDoListItemDto;
import greencity.dto.todolistitem.ToDoListItemRequestDto;
import greencity.dto.user.UserToDoListItemResponseDto;
import greencity.dto.user.UserVO;
import greencity.service.ToDoListItemService;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import java.util.List;
import java.util.Locale;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PatchMapping;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/to-do-list-items")
public class ToDoListItemController {
    private final ToDoListItemService toDoListItemService;

    /**
     * Method saves to-do list items, chosen by user.
     *
     * @param dto    - dto with Items, chosen by user.
     * @param locale - needed language code
     * @return new {@link ResponseEntity}.
     * @author Vitalii Skolozdra
     */
    @Operation(summary = "Save one or multiple to-do list items for current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN)))
    })
    @PostMapping
    @ApiLocale
    public ResponseEntity<List<UserToDoListItemResponseDto>> saveUserToDoListItems(
        @Valid @RequestBody List<ToDoListItemRequestDto> dto,
        @Parameter(hidden = true) @CurrentUser UserVO user,
        Long habitId,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(toDoListItemService.saveUserToDoListItems(user.getId(), habitId, dto, locale.getLanguage()));
    }

    /**
     * Method finds toDoList saved by user in specific language.
     *
     * @param locale  {@link Locale} with needed language code.
     * @param habitId {@link Long} with needed habit id.
     * @return List of {@link UserToDoListItemResponseDto}.
     * @author Dmytro Khonko
     */
    @Operation(description = "Get user`s to-do list.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN)))
    })
    @GetMapping("/habits/{habitId}/to-do-list")
    @ApiLocale
    public ResponseEntity<List<UserToDoListItemResponseDto>> getToDoListItemsAssignedToUser(
        @Parameter(hidden = true) @CurrentUser UserVO user,
        @Parameter(
            description = "Id of the Habit that belongs to current user. Cannot be empty.") @PathVariable Long habitId,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(toDoListItemService.getUserToDoList(user.getId(), habitId, locale.getLanguage()));
    }

    /**
     * Method deletes from toDoList item saved by user.
     *
     * @param toDoListItemId {@link Long} with needed to-do list item id.
     * @param habitId        {@link Long} with needed habit id.
     * @author Dmytro Khonko
     */

    @Operation(summary = "Delete from to-do list")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN)))
    })
    @DeleteMapping
    public void delete(
        @Parameter(hidden = true) @CurrentUser UserVO user, Long habitId, Long toDoListItemId) {
        toDoListItemService.deleteUserToDoListItemByItemIdAndUserIdAndHabitId(toDoListItemId, user.getId(),
            habitId);
    }

    /**
     * Method updates to-do list item status to 'DONE'.
     *
     * @param locale - needed language code
     * @return new {@link ResponseEntity}.
     * @author Vitalii Skolozdra
     */
    @Operation(summary = "Change status of one of the to-do list item for current user to DONE.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN)))
    })
    @PatchMapping("/{userToDoListItemId}")
    @ApiLocale
    public ResponseEntity<UserToDoListItemResponseDto> updateUserToDoListItemStatus(
        @Parameter(hidden = true) @CurrentUser UserVO user,
        @Parameter(description = "Id of the UserToDoListItems that belongs to current user."
            + " Cannot be empty.") @PathVariable Long userToDoListItemId,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(toDoListItemService.updateUserToDoListItemStatus(user.getId(), userToDoListItemId,
                locale.getLanguage()));
    }

    /**
     * Method updates user to-do list item status.
     *
     * @param locale - needed language code
     * @return new {@link ResponseEntity}.
     * @author Mykola Danylko
     */
    @Operation(summary = "Change status of one of the user to-do list item for current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = UserToDoListItemResponseDto.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND))),
    })
    @PatchMapping("/{userToDoListItemId}/status/{status}")
    @ApiLocale
    public ResponseEntity<List<UserToDoListItemResponseDto>> updateUserToDoListItemStatus(
        @Parameter(hidden = true) @CurrentUser UserVO user,
        @Parameter(description = "Id of the userToDoListItem that belongs to current user."
            + " Cannot be empty.") @PathVariable(value = "userToDoListItemId") Long userToDoListItemId,
        @PathVariable(value = "status") String status,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK).body(toDoListItemService
            .updateUserToDoListItemStatus(user.getId(), userToDoListItemId, locale.getLanguage(), status));
    }

    /**
     * Method for deleting user to-do list item.
     *
     * @param ids string with objects id for deleting.
     * @return new {@link ResponseEntity}
     * @author Bogdan Kuzenko
     */
    @Operation(summary = "Delete user to-do list item")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = Long.class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
    })
    @DeleteMapping("/user-to-do-list-items")
    public ResponseEntity<List<Long>> bulkDeleteUserToDoListItems(
        @Parameter(description = "Ids of user to-do list items separated by a comma \n e.g. 1,2",
            required = true) @Pattern(
                regexp = "^\\d+(,\\d+)++$",
                message = ValidationConstants.BAD_COMMA_SEPARATED_NUMBERS) @RequestParam String ids,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        return ResponseEntity.status(HttpStatus.OK).body(toDoListItemService
            .deleteUserToDoListItems(ids));
    }

    /**
     * Method returns list user custom to-do list.
     *
     * @param userId {@link UserVO} id
     * @return list of {@link ResponseEntity}
     * @author Bogdan Kuzenko
     */
    @Operation(summary = "Get all user to-do-list-items with 'INPROGRESS' status.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
    })
    @GetMapping("/{userId}/get-all-inprogress")
    public ResponseEntity<List<ToDoListItemDto>> findInProgressByUserId(
        @PathVariable @CurrentUserId Long userId, @RequestParam(name = "lang") String code) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(toDoListItemService.findInProgressByUserIdAndLanguageCode(userId, code));
    }
}
