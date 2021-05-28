package greencity.controller;

import greencity.annotations.CurrentUserId;
import greencity.constant.HttpStatuses;
import greencity.dto.shoppinglistitem.BulkSaveCustomShoppingListItemDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemResponseDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemVO;
import greencity.dto.shoppinglistitem.ShoppingListItemDto;
import greencity.dto.user.BulkSaveUserShoppingListItemDto;
import greencity.dto.user.UserVO;
import greencity.service.CustomShoppingListItemService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/custom/shopping-list-items")
public class CustomShoppingListItemController {
    private final CustomShoppingListItemService customShoppingListItemService;

    /**
     * Method for finding all custom shopping list items.
     *
     * @param userId user id.
     * @return list of {@link CustomShoppingListItemVO}
     */
    @ApiOperation(value = "Get all available custom shopping-list-items")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
    })
    @GetMapping("/{userId}/{habitId}")
    public ResponseEntity<List<CustomShoppingListItemResponseDto>> getAllAvailableCustomShoppingListItems(
        @PathVariable Long userId, @PathVariable Long habitId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(customShoppingListItemService.findAllAvailableCustomShoppingListItems(userId, habitId));
    }

    /**
     * Method returns list user custom shopping-list.
     *
     * @param userId {@link UserVO} id
     * @return list of {@link ResponseEntity}
     * @author Bogdan Kuzenko
     */
    @ApiOperation(value = "Get all user custom shopping-list-items.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
    })
    @GetMapping("/{userId}/custom-shopping-list-items")
    public ResponseEntity<List<ShoppingListItemDto>> findAllByUser(
        @PathVariable @CurrentUserId Long userId, @RequestParam(name = "lang") String code) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(customShoppingListItemService.findInProgressByUserIdAndLanguageCode(userId, code));
    }

    /**
     * Method saves custom shopping list items for user.
     *
     * @param dto    {@link BulkSaveUserShoppingListItemDto} with list objects to
     *               save
     * @param userId {@link UserVO} id
     * @return new {@link ResponseEntity}
     * @author Bogdan Kuzenko
     */
    @ApiOperation(value = "Save one or multiple custom Shopping list item for current user.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
    })
    @PostMapping("/{userId}/{habitId}/custom-shopping-list-items")
    public ResponseEntity<List<CustomShoppingListItemResponseDto>> saveUserCustomShoppingListItems(
        @Valid @RequestBody BulkSaveCustomShoppingListItemDto dto,
        @ApiParam("Id of current user. Cannot be empty.") @PathVariable @CurrentUserId Long userId,
        @PathVariable Long habitId) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(customShoppingListItemService.save(dto, userId, habitId));
    }

    /**
     * Method updated user custom shopping list items to status DONE.
     *
     * @param userId     {@link UserVO} id
     * @param itemId     {@link Long} with needed item id.
     * @param itemStatus {@link String} with needed item status.
     * @return new {@link ResponseEntity}
     * @author Bogdan Kuzenko
     */
    @ApiOperation(value = "Update custom shopping list items status")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PatchMapping("/{userId}/custom-shopping-list-items")
    public ResponseEntity<CustomShoppingListItemResponseDto> updateItemStatus(@PathVariable @CurrentUserId Long userId,
        @RequestParam("itemId") Long itemId,
        @RequestParam("status") String itemStatus) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(customShoppingListItemService.updateItemStatus(userId, itemId, itemStatus));
    }

    /**
     * Method updates user's shopping list items to status DONE.
     *
     * @param userId {@link UserVO} id
     * @param itemId {@link Long} with needed item id.
     * @author Volodia Lesko
     */
    @ApiOperation(value = "Update shopping list item status")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PatchMapping("/{userId}/done")
    public void updateItemStatusToDone(@PathVariable @CurrentUserId Long userId,
        @RequestParam("itemId") Long itemId) {
        customShoppingListItemService.updateItemStatusToDone(userId, itemId);
    }

    /**
     * Method for delete user custom shopping list items.
     *
     * @param ids    string with objects id for deleting.
     * @param userId {@link UserVO} id
     * @return new {@link ResponseEntity}
     */
    @ApiOperation(value = "Delete user custom shopping list items")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = Long.class),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
    })
    @DeleteMapping("/{userId}/custom-shopping-list-items")
    public ResponseEntity<List<Long>> bulkDeleteCustomShoppingListItems(
        @ApiParam(value = "Ids of custom shopping-list-items separated by a comma \n e.g. 1,2",
            required = true) @RequestParam String ids,
        @PathVariable @CurrentUserId Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(customShoppingListItemService.bulkDelete(ids));
    }
}
