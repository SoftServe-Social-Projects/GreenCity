package greencity.controller;

import greencity.annotations.CurrentUserId;
import greencity.constant.HttpStatuses;
import greencity.dto.todolistitem.BulkSaveCustomToDoListItemDto;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.todolistitem.CustomToDoListItemVO;
import greencity.dto.user.UserVO;
import greencity.service.CustomToDoListItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/custom/to-do-list-items")
public class CustomToDoListItemController {
    private final CustomToDoListItemService customToDoListItemService;

    /**
     * Method for finding all custom to-do list items.
     *
     * @param userId user id.
     * @return list of {@link CustomToDoListItemVO}
     */
    @Operation(summary = "Get all available custom to-do-list-items")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
    })
    @GetMapping("/{userId}/{habitId}")
    public ResponseEntity<List<CustomToDoListItemResponseDto>> getAllAvailableCustomToDoListItems(
        @PathVariable Long userId, @PathVariable Long habitId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(customToDoListItemService.findAllAvailableCustomToDoListItems(userId, habitId));
    }

    /**
     * Method saves custom to-do list items for user.
     *
     * @param dto    {@link BulkSaveCustomToDoListItemDto} with list objects to save
     * @param userId {@link UserVO} id
     * @return new {@link ResponseEntity}
     * @author Bogdan Kuzenko
     */
    @Operation(summary = "Save one or multiple custom To-Do list item for current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
    })
    @PostMapping("/{userId}/{habitAssignId}/custom-to-do-list-items")
    public ResponseEntity<List<CustomToDoListItemResponseDto>> saveUserCustomToDoListItems(
        @Valid @RequestBody BulkSaveCustomToDoListItemDto dto,
        @Parameter(description = "Id of current user. Cannot be empty.") @PathVariable @CurrentUserId Long userId,
        @PathVariable Long habitAssignId) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(customToDoListItemService.save(dto, userId, habitAssignId));
    }

    /**
     * Method updated user custom to-do list items to status DONE.
     *
     * @param userId     {@link UserVO} id
     * @param itemId     {@link Long} with needed item id.
     * @param itemStatus {@link String} with needed item status.
     * @return new {@link ResponseEntity}
     * @author Bogdan Kuzenko
     */
    @Operation(summary = "Update custom to-do list items status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PatchMapping("/{userId}/custom-to-do-list-items")
    public ResponseEntity<CustomToDoListItemResponseDto> updateItemStatus(@PathVariable @CurrentUserId Long userId,
        @RequestParam("itemId") Long itemId,
        @RequestParam("status") String itemStatus) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(customToDoListItemService.updateItemStatus(userId, itemId, itemStatus));
    }

    /**
     * Method updates user's to-do list items to status DONE.
     *
     * @param userId {@link UserVO} id
     * @param itemId {@link Long} with needed item id.
     * @author Volodia Lesko
     */
    @Operation(summary = "Update to-do list item status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @PatchMapping("/{userId}/done")
    public void updateItemStatusToDone(@PathVariable @CurrentUserId Long userId,
        @RequestParam("itemId") Long itemId) {
        customToDoListItemService.updateItemStatusToDone(userId, itemId);
    }

    /**
     * Method for delete user custom to-do list items.
     *
     * @param ids    string with objects id for deleting.
     * @param userId {@link UserVO} id
     * @return new {@link ResponseEntity}
     */
    @Operation(summary = "Delete user custom to-do list items")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
    })
    @DeleteMapping("/{userId}/custom-to-do-list-items")
    public ResponseEntity<List<Long>> bulkDeleteCustomToDoListItems(
        @Parameter(description = "Ids of custom to-do-list-items separated by a comma \n e.g. 1,2",
            required = true) @RequestParam String ids,
        @PathVariable @CurrentUserId Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(customToDoListItemService.bulkDelete(ids));
    }

    /**
     * Method returns all user's custom to-do items by status if is defined.
     *
     * @param userId {@link Long} id
     * @param status {@link String} status
     * @return list of {@link ResponseEntity}
     * @author Max Bohonko
     */
    @Operation(summary = "Get all user's custom to-do items")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @GetMapping("/{userId}/custom-to-do-list-items")
    public ResponseEntity<List<CustomToDoListItemResponseDto>> getAllCustomToDoItemsByStatus(
        @PathVariable @CurrentUserId Long userId,
        @Parameter(description = "Available values : ACTIVE, DONE, DISABLED, INPROGRESS."
            + " Leave this field empty if you need items with any status") @RequestParam(
                required = false) String status) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(customToDoListItemService.findAllUsersCustomToDoListItemsByStatus(userId, status));
    }
}
