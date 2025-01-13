package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.CurrentUser;
import greencity.annotations.ValidLanguage;
import greencity.constant.HttpStatuses;
import greencity.dto.user.UserToDoListItemRequestDto;
import greencity.dto.user.UserToDoListItemRequestWithStatusDto;
import greencity.dto.user.UserToDoListItemResponseDto;
import greencity.dto.user.UserVO;
import greencity.service.UserToDoListItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/habits/assign/user-to-do-list-items/{habitAssignId}")
public class UserToDoListItemController {
    private final UserToDoListItemService userToDoListItemService;

    /**
     * Method finds user to-do list items for habit assign in specific language.
     *
     * @param habitAssignId {@link Long} with needed habit assign id.
     * @param userVO        {@link UserVO} current user.
     * @param locale        {@link Locale} with needed language code.
     * @return List of {@link UserToDoListItemResponseDto}.
     */
    @Operation(summary = "Get user to-do list for habit assign.")
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
    @GetMapping
    @ApiLocale
    public ResponseEntity<List<UserToDoListItemResponseDto>> getUserToDoListItemsForHabitAssign(
        @PathVariable @Min(1) Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(userToDoListItemService.findAllForHabitAssign(habitAssignId, userVO.getId(), locale.getLanguage()));
    }

    /**
     * Method save list of user to-do list items for habit assign.
     *
     * @param habitAssignId     {@link Long} with needed habit assign id.
     * @param userToDoListItems list of {@link UserToDoListItemRequestWithStatusDto}
     *                          user to-do items to save.
     * @param userVO            {@link UserVO} current user.
     * @param locale            {@link Locale} with needed language code.
     * @return List of {@link UserToDoListItemResponseDto}.
     */
    @Operation(summary = "Save user to-do list items for habit assign.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping
    @ApiLocale
    public ResponseEntity<List<UserToDoListItemResponseDto>> saveUserToDoListItemsForHabitAssign(
        @PathVariable @Min(1) Long habitAssignId,
        @RequestBody List<@Valid UserToDoListItemRequestDto> userToDoListItems,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userToDoListItemService.saveUserToDoListItems(habitAssignId, userToDoListItems, userVO.getId(),
                locale.getLanguage()));
    }

    /**
     * Method delete list of user to-do list items for habit assign by items ids.
     *
     * @param habitAssignId        {@link Long} with needed habit assign id.
     * @param userToDoListItemsIds list of {@link Long} user to-do item id to
     *                             delete.
     * @param userVO               {@link UserVO} current user.
     * @return List of {@link UserToDoListItemResponseDto}.
     */
    @Operation(summary = "Save user to-do list items for habit assign.")
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
    @DeleteMapping
    public ResponseEntity<Object> deleteUserToDoListItemsForHabitAssign(
        @PathVariable @Min(1) Long habitAssignId,
        @RequestBody List<@Min(1) Long> userToDoListItemsIds,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        userToDoListItemService.deleteUserToDoListItems(habitAssignId, userToDoListItemsIds, userVO.getId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method change statuses of user to-do list items for habit assign.
     *
     * @param habitAssignId     {@link Long} with needed habit assign id.
     * @param userToDoListItems list of {@link UserToDoListItemRequestWithStatusDto}
     *                          user to-do items with status.
     * @param userVO            {@link UserVO} current user.
     * @param locale            {@link Locale} with needed language code.
     * @return List of {@link UserToDoListItemResponseDto}.
     */
    @Operation(summary = "Save new statuses for user to-do list items for habit assign.")
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
    @PatchMapping
    @ApiLocale
    public ResponseEntity<List<UserToDoListItemResponseDto>> changeStatusUserToDoListItems(
        @PathVariable @Min(1) Long habitAssignId,
        @RequestBody List<@Valid UserToDoListItemRequestWithStatusDto> userToDoListItems,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK).body(userToDoListItemService
            .changeStatusesUserToDoListItems(habitAssignId, userToDoListItems, userVO.getId(), locale.getLanguage()));
    }
}
