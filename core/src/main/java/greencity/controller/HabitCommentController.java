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
import greencity.dto.comment.CommentVO;
import greencity.dto.habit.HabitVO;
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
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Locale;

@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/habits")
public class HabitCommentController {
    private final CommentService commentService;

    /**
     * Method for creating {@link CommentVO}.
     *
     * @param habitId id of {@link HabitVO} to add comment to.
     * @param request dto for {@link CommentVO} entity.
     * @return dto {@link AddCommentDtoResponse}
     */
    @Operation(summary = "Add comment")
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
    @PostMapping(path = "/{habitId}/comments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AddCommentDtoResponse> save(@PathVariable Long habitId,
        @Valid @RequestPart AddCommentDtoRequest request,
        @RequestPart(value = "images", required = false) @Nullable @Size(max = 5,
            message = "Download up to 5 images") @ImageArrayValidation MultipartFile[] images,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(commentService.save(ArticleType.HABIT, habitId, request, images, userVO, locale));
    }

    /**
     * Method to get certain comment for {@link HabitVO} specified by commentId.
     *
     * @param id specifies {@link CommentDto} to which we search for comments
     * @return comment to certain habit specified by commentId.
     */
    @Operation(summary = "Get comment by id")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = AddCommentDtoResponse.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND))),
    })
    @GetMapping("/comments/{id}")
    public ResponseEntity<CommentDto> getCommentById(@PathVariable Long id,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(commentService.getCommentById(ArticleType.HABIT, id, userVO));
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
    @GetMapping("/comments/{parentCommentId}/replies/active")
    @ApiPageable
    public ResponseEntity<PageableDto<CommentDto>> getAllActiveReplies(
        @Parameter(hidden = true) Pageable pageable,
        @PathVariable Long parentCommentId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(commentService.getAllActiveReplies(pageable, parentCommentId, userVO));
    }

    /**
     * Method to count all active comments for certain {@link HabitVO}.
     *
     * @param habitId to specify {@link HabitVO}
     * @return amount of all active comments for certain {@link HabitVO}
     */
    @Operation(summary = "Count comments.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND))),
    })
    @GetMapping("/{habitId}/comments/count")
    public int getCountOfComments(@PathVariable Long habitId) {
        return commentService.countCommentsForHabit(habitId);
    }

    /**
     * Method to count all active replies for {@link CommentDto} comment.
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
    @GetMapping("/comments/{parentCommentId}/replies/active/count")
    public int getCountOfActiveReplies(@PathVariable Long parentCommentId) {
        return commentService.countAllActiveReplies(parentCommentId);
    }

    /**
     * Method to get all active comments to {@link HabitVO} specified by habitId.
     *
     * @param habitId id of {@link HabitVO}
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
    @GetMapping("/comments/active")
    @ApiPageableWithoutSort
    public ResponseEntity<PageableDto<CommentDto>> getAllActiveComments(
        @Parameter(hidden = true) Pageable pageable,
        Long habitId,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(commentService.getAllActiveComments(pageable, user, habitId, ArticleType.HABIT));
    }

    /**
     * Method to like/dislike certain {@link CommentDto} comment specified by id.
     *
     * @param commentId of {@link CommentDto} to like/unlike
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
    @PostMapping("/comments/like")
    public void like(@RequestParam("commentId") Long commentId,
        @Parameter(hidden = true) @CurrentUser UserVO user,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        commentService.like(commentId, user, locale);
    }

    /**
     * Method to dislike certain {@link CommentDto} comment specified by id.
     *
     * @param commentId of {@link CommentDto} to like/dislike
     */
    @Operation(summary = "Dislike comment.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/comments/dislike")
    public void dislike(@RequestParam("commentId") Long commentId,
        @Parameter(hidden = true) @CurrentUser UserVO user,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        commentService.dislike(commentId, user, locale);
    }

    /**
     * Method to update certain {@link CommentVO} specified by id.
     *
     * @param id          of {@link CommentVO} to update
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
    @PatchMapping("/comments")
    public void update(@RequestParam Long id,
        @RequestBody @Valid @Size(min = 1, max = 8000) String commentText,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        commentService.update(commentText, id, user);
    }

    /**
     * Method for deleting {@link CommentVO} by its id.
     *
     * @param id {@link CommentVO} id which will be deleted.
     * @return id of deleted {@link CommentVO}.
     */
    @Operation(summary = "Mark comment as deleted.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        commentService.delete(id, user);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
