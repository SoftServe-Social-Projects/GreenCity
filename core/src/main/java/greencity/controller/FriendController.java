package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.annotations.CurrentUser;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableDto;
import greencity.dto.friends.UserAsFriendDto;
import greencity.dto.friends.UserFriendDto;
import greencity.dto.user.UserManagementDto;
import greencity.dto.user.UserVO;
import greencity.enums.RecommendedFriendsType;
import greencity.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/friends")
public class FriendController {
    private final FriendService friendService;

    /**
     * Method for deleting user's friend.
     *
     * @param friendId id user friend.
     * @param userVO   {@link UserVO} user.
     * @author Marian Datsko
     */
    @Operation(summary = "Delete user's friend")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @DeleteMapping("/{friendId}")
    public ResponseEntity<ResponseEntity.BodyBuilder> deleteUserFriend(
        @Parameter(description = "Id friend of current user. Cannot be empty.") @PathVariable long friendId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        friendService.deleteUserFriendById(userVO.getId(), friendId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Exception for deleting friend with null id.
     *
     * @author Oleksandr Sokil
     */
    @DeleteMapping({"", "/"})
    @Operation(hidden = true)
    public ResponseEntity<ResponseEntity.BodyBuilder> deleteUserFriendWithoutParams() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    /**
     * Method for add new user friend.
     *
     * @param friendId id user friend.
     * @param userVO   {@link UserVO} user.
     * @author Marian Datsko
     */
    @Operation(summary = "Add new user friend")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND))),
    })
    @PostMapping("/{friendId}")
    public ResponseEntity<ResponseEntity.BodyBuilder> addNewFriend(
        @Parameter(description = "Id friend of current user. Cannot be empty.") @PathVariable long friendId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        friendService.addNewFriend(userVO.getId(), friendId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for accepting friend request from user.
     *
     * @param friendId id user friend.
     * @param userVO   {@link UserVO} user.
     */
    @Operation(summary = "Accept friend request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND))),
    })
    @PatchMapping("/{friendId}/acceptFriend")
    public ResponseEntity<ResponseEntity.BodyBuilder> acceptFriendRequest(
        @Parameter(description = "Friend's id. Cannot be empty.") @PathVariable long friendId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        friendService.acceptFriendRequest(userVO.getId(), friendId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for declining friend request from user. Change status from REQUEST to
     * REJECTED.
     *
     * @param friendId id user friend.
     * @param userVO   {@link UserVO} user.
     */
    @Operation(summary = "Decline friend request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND))),
    })
    @PatchMapping("/{friendId}/declineFriend")
    public ResponseEntity<Object> declineFriendRequest(
        @Parameter(description = "Friend's id. Cannot be empty.") @PathVariable long friendId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        friendService.declineFriendRequest(userVO.getId(), friendId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method to find all user friends by userId.
     *
     * @param userId user id.
     *
     * @return {@link PageableDto} of {@link UserManagementDto}.
     * @author Orest Mamchuk
     */
    @Operation(summary = "Get all user friends")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/user/{userId}")
    @ApiPageable
    public ResponseEntity<PageableDto<UserManagementDto>> findUserFriendsByUserId(
        @Parameter(hidden = true) @PageableDefault Pageable page,
        @PathVariable long userId) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(friendService.findUserFriendsByUserId(page, userId));
    }

    /**
     * Method to find all user friends by userId and to set FriendStatus related to
     * current user.
     *
     * @param userId user id.
     *
     * @return {@link PageableDto} of {@link UserManagementDto}.
     * @author Lilia Mokhnatska
     */
    @Operation(summary = "Get all user friends and set FriendStatus related to current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/{userId}/all-user-friends")
    @ApiPageable
    public ResponseEntity<PageableDto<UserFriendDto>> findUserFriendsByUserIAndShowFriendStatusRelatedToCurrentUser(
        @Parameter(hidden = true) @PageableDefault Pageable page,
        @PathVariable long userId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(friendService.findUserFriendsByUserIAndShowFriendStatusRelatedToCurrentUser(page, userId,
                userVO.getId()));
    }

    /**
     * Method to find {@link UserFriendDto}s that are not friend for current
     * user(except current user).
     *
     * @param userVO user.
     * @param name   filtering name.
     *
     * @return {@link PageableDto} of {@link UserFriendDto}.
     */
    @Operation(
        summary = "Find all users except current user and his friends and users who send request to current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/not-friends-yet")
    @ApiPageable
    public ResponseEntity<PageableDto<UserFriendDto>> findAllUsersExceptMainUserAndUsersFriendAndRequestersToMainUser(
        @Parameter(hidden = true) @PageableDefault Pageable page,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @RequestParam(required = false, defaultValue = "") String name,
        @RequestParam(required = false, defaultValue = "false") boolean filterByFriendsOfFriends,
        @RequestParam(required = false, defaultValue = "false") boolean filterByCity) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(friendService.findAllUsersExceptMainUserAndUsersFriendAndRequestersToMainUser(
                userVO.getId(),
                name,
                filterByFriendsOfFriends,
                filterByCity,
                page));
    }

    /**
     * Method to find recommended friends for current user by type.
     *
     * @param userVO user.
     * @param type   type to find recommended friends Supported values:
     *               "FRIENDS_OF_FRIENDS", "HABITS", "CITY".
     *
     * @return {@link PageableDto} of {@link UserFriendDto}.
     */
    @Operation(summary = "Find recommended friends by type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
    })
    @GetMapping("/recommended-friends")
    @ApiPageable
    public ResponseEntity<PageableDto<UserFriendDto>> findRecommendedFriends(
        @Parameter(hidden = true) Pageable page,
        @RequestParam(required = false) RecommendedFriendsType type,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(friendService.findRecommendedFriends(userVO.getId(), type, page));
    }

    /**
     * Method to find {@link UserFriendDto}s which sent request to current user.
     *
     * @param userVO user.
     *
     * @return {@link PageableDto} of {@link UserFriendDto}.
     */
    @Operation(summary = "Find user's requests")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/friendRequests")
    @ApiPageable
    public ResponseEntity<PageableDto<UserFriendDto>> getAllUserFriendsRequests(
        @RequestParam(required = false, defaultValue = "") String name,
        @RequestParam(required = false, defaultValue = "false") boolean filterByCity,
        @Parameter(hidden = true) Pageable page,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(friendService.getAllUserFriendRequests(userVO.getId(), name, filterByCity, page));
    }

    /**
     * The method returns friends for the current user.
     *
     * @param userVO user.
     * @param name   filtering name.
     *
     * @return {@link PageableDto} of {@link UserFriendDto}.
     */
    @Operation(summary = "Find all friends")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @GetMapping
    @ApiPageable
    public ResponseEntity<PageableDto<UserFriendDto>> findAllFriendsOfUser(
        @RequestParam(required = false, defaultValue = "") String name,
        @RequestParam(required = false, defaultValue = "false") boolean filterByCity,
        @Parameter(hidden = true) Pageable page,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(friendService.findAllFriendsOfUser(userVO.getId(),
                name,
                filterByCity,
                page));
    }

    /**
     * The method returns mutual friends for the current user.
     *
     * @param friendId The id of the friend for whom you want to find mutual
     *                 friends.
     * @param userVO   current user.
     *
     * @return {@link PageableDto} of {@link UserFriendDto}.
     */
    @Operation(summary = "Get all mutual friends for current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/mutual-friends")
    @ApiPageable
    public ResponseEntity<PageableDto<UserFriendDto>> getMutualFriends(
        @RequestParam Long friendId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) Pageable page) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(friendService.getMutualFriends(userVO.getId(), friendId, page));
    }

    /**
     * Method for canceling a request that the current user has sent before to user
     * with friendId.
     *
     * @param friendId id user friend.
     * @param userVO   {@link UserVO} user.
     * @author Lilia Mokhnatska
     */
    @Operation(summary = "Delete user's request to friend")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @DeleteMapping("/{friendId}/cancelRequest")
    public ResponseEntity<ResponseEntity.BodyBuilder> cancelRequest(
        @Parameter(description = "Id friend of current user. Cannot be empty.") @PathVariable long friendId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        friendService.deleteRequestOfCurrentUserToFriend(userVO.getId(), friendId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for getting common data of users.
     *
     * @param friendId id of user.
     * @param userVO   {@link UserVO} user.
     *
     * @return {@link UserAsFriendDto}
     * @author Denys Ryhal.
     */
    @Operation(summary = "Get user as friend")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/user-data-as-friend/{friendId}")
    public ResponseEntity<UserAsFriendDto> getUserAsFriend(
        @PathVariable Long friendId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(friendService.getUserAsFriend(userVO.getId(), friendId));
    }
}
