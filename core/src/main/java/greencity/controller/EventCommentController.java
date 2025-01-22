package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.annotations.ApiPageableWithoutSort;
import greencity.annotations.CurrentUser;
import greencity.annotations.ImageArrayValidation;
import greencity.annotations.ValidLanguage;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableDto;
import greencity.dto.comment.AddCommentDtoRequest;
import greencity.dto.comment.AddCommentDtoResponse;
import greencity.dto.comment.CommentDto;
import greencity.dto.user.UserVO;
import greencity.enums.ArticleType;
import greencity.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import greencity.dto.comment.CommentVO;
import greencity.dto.event.EventVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.Locale;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/events")
public class EventCommentController {
    private final CommentService commentService;

    /**
     * Method for creating {@link CommentVO}.
     *
     * @param request dto for {@link CommentVO} entity.
     * @return dto {@link AddCommentDtoResponse}
     */
    @Operation(summary = "Add comment.")
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = AddCommentDtoResponse.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND))),
    })
    @PostMapping(path = "/{eventId}/comments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AddCommentDtoResponse> save(
        @PathVariable Long eventId,
        @Valid @RequestPart AddCommentDtoRequest request,
        @Parameter(hidden = true) @CurrentUser UserVO user,
        @RequestPart(value = "images", required = false) @Nullable @Size(max = 5,
            message = "Download up to 5 images") @ImageArrayValidation MultipartFile[] images,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(commentService.save(ArticleType.EVENT, eventId, request, images, user, locale));
    }

    /**
     * Method to get certain comment to {@link EventVO} specified by commentId.
     *
     * @param commentId specifies {@link CommentDto} to which we search for comments
     * @return comment to certain event specified by commentId.
     */
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND))),
    })
    @GetMapping("/comments/{commentId}")
    public ResponseEntity<CommentDto> getCommentById(@PathVariable Long commentId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity.ok()
            .body(commentService.getCommentById(ArticleType.EVENT, commentId, userVO));
    }

    /**
     * Method to count all active comments for certain {@link EventVO}.
     *
     * @param eventId to specify {@link EventVO}
     * @return amount of all active comments for certain {@link EventVO}
     */
    @Operation(summary = "Count comments.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND))),
    })
    @GetMapping("/{eventId}/comments/count")
    public int getCountOfComments(@PathVariable Long eventId) {
        return commentService.countCommentsForEvent(eventId);
    }

    /**
     * Method to get all comments to {@link EventVO} specified by eventId.
     *
     * @param eventId id of {@link EventVO}
     * @return Pageable of {@link CommentDto}
     */
    @Operation(summary = "Get all active comments (without replies).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/{eventId}/comments")
    @ApiPageableWithoutSort
    public ResponseEntity<PageableDto<CommentDto>> getAllActiveComments(
        @Parameter(hidden = true) Pageable pageable,
        @PathVariable Long eventId,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        return ResponseEntity.ok()
            .body(commentService.getAllActiveComments(pageable, user, eventId, ArticleType.EVENT));
    }

    /**
     * Method to update certain {@link CommentVO} specified by eventId.
     *
     * @param commentId   of {@link CommentVO} to update
     * @param commentText edited text of {@link CommentVO}
     */
    @Operation(summary = "Update comment.")
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
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<Object> update(
        @PathVariable Long commentId,
        @RequestBody @Valid @Size(min = 1, max = 8000) String commentText,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        commentService.update(commentText, commentId, user);
        return ResponseEntity.ok().build();
    }

    /**
     * Method for deleting {@link CommentVO} by its id.
     *
     * @param commentId {@link CommentVO} id which will be deleted.
     * @return id of deleted {@link CommentVO}.
     */
    @Operation(summary = "Mark comment as deleted.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Object> delete(
        @PathVariable Long commentId,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        commentService.delete(commentId, user);
        return ResponseEntity.ok().build();
    }

    /**
     * Method to get all active replies to {@link CommentDto} specified by parent
     * comment id.
     *
     * @param parentCommentId id of parent comment {@link CommentDto}
     * @param userVO          {@link UserVO} user who want to get replies.
     * @return Pageable of {@link CommentDto}
     */
    @Operation(description = "Get all active replies to comment.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @ApiPageable(dtoClass = CommentDto.class)
    @GetMapping("/comments/{parentCommentId}/replies/active")
    public ResponseEntity<PageableDto<CommentDto>> findAllActiveReplies(
        @Parameter(hidden = true) Pageable pageable,
        @PathVariable Long parentCommentId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity.ok()
            .body(commentService.getAllActiveReplies(pageable, parentCommentId, userVO));
    }

    /**
     * Method to count all active replies to certain {@link CommentDto} comment.
     *
     * @param parentCommentId to specify {@link CommentDto}
     * @return amount of all active comments for certain {@link CommentDto}
     */
    @Operation(summary = "Count replies for comment.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/comments/{parentCommentId}/replies/count")
    public int getCountOfActiveReplies(@PathVariable Long parentCommentId) {
        return commentService.countAllActiveReplies(parentCommentId);
    }

    /**
     * Method to like/unlike certain {@link CommentDto} specified by id.
     *
     * @param commentId of {@link CommentDto} to like/dislike
     */
    @Operation(summary = "Like/unlike comment.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/comments/like/{commentId}")
    public void like(
        @PathVariable Long commentId,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        commentService.like(commentId, user, null);
    }

    /**
     * Method to dislike certain {@link CommentDto} specified by id.
     *
     * @param commentId of {@link CommentDto} to like/dislike
     */
    @Operation(summary = "Like/unlike comment.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/comments/dislike/{commentId}")
    public void dislike(
        @PathVariable Long commentId,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        commentService.dislike(commentId, user, null);
    }
}
