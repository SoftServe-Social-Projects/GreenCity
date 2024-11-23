package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.ValidLanguage;
import greencity.constant.HttpStatuses;
import greencity.dto.user.UserToDoListItemResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/habits/assign/user-to-do-list-items")
public class UserToDoListItemController {

    /**
     * Method finds user to-do list items for habit assign in specific language.
     *
     * @param locale {@link Locale} with needed language code.
     * @param habitAssignId {@link Long} with needed habit assign id.
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
    @GetMapping("/{habitAssignId}")
    @ApiLocale
    public ResponseEntity<List<UserToDoListItemResponseDto>> getUserToDoListItemsForHabitAssign(
            @PathVariable Long habitAssignId,
            @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return null;
    }

    /**
     * Method save list of user to-do list items for habit assign.
     *
     * @param habitAssignId {@link Long} with needed habit assign id.
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
    @PostMapping("/{habitAssignId}")
    @ApiLocale
    public ResponseEntity<List<UserToDoListItemResponseDto>> saveUserToDoListItemsForHabitAssign(
            @PathVariable Long habitAssignId,
            @RequestBody List<UserToDoListItemRequestDto> userToDoListItems) {
        return null;
    }

    /**
     * Method delete list of user to-do list items for habit assign by items ids.
     *
     * @param habitAssignId {@link Long} with needed habit assign id.
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
    @DeleteMapping("/{habitAssignId}")
    @ApiLocale
    public ResponseEntity<List<UserToDoListItemResponseDto>> deleteUserToDoListItemsForHabitAssign(
            @PathVariable Long habitAssignId,
            @RequestBody List<Long> userToDoListItemsIds) {
        return null;
    }

    /**
     * Method change status of user to-do list item for habit assign by item id.
     *
     * @param habitAssignId {@link Long} with needed habit assign id.
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
    @DeleteMapping("/{habitAssignId}")
    @ApiLocale
    public ResponseEntity<List<UserToDoListItemResponseDto>> changeStatusUserToDoListItems(
            @PathVariable Long habitAssignId,
            @RequestBody List<UserToDoListItemRequestWithStatusDto> userToDoListItemsIds) {
        return null;
    }
    //add list to habit assign, delete list from habit assign, change status

    private class UserToDoListItemRequestWithStatusDto {}
    private class UserToDoListItemRequestDto {}
}
