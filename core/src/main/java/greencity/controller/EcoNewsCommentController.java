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
import greencity.dto.econews.EcoNewsVO;
import greencity.dto.comment.AmountCommentLikesDto;
import greencity.dto.habit.HabitVO;
import greencity.dto.user.UserSearchDto;
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
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.Locale;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/eco-news")
public class EcoNewsCommentController {
    private final CommentService commentService;

    /**
     * Method for creating {@link CommentVO}.
     *
     * @param ecoNewsId id of {@link EcoNewsVO} to add comment to.
     * @param request   - dto for {@link CommentVO} entity.
     * @return dto {@link AddCommentDtoResponse}
     */
    @Operation(summary = "Add comment.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = AddCommentDtoResponse.class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND))),
    })
    @PostMapping(path = "/{ecoNewsId}/comments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AddCommentDtoResponse> save(@PathVariable Long ecoNewsId,
        @Valid @RequestPart AddCommentDtoRequest request,
        @RequestPart(value = "images", required = false) @Nullable @Size(max = 5,
            message = "Download up to 5 images") @ImageArrayValidation MultipartFile[] images,
        @Parameter(hidden = true) @ValidLanguage Locale locale,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(commentService.save(ArticleType.ECO_NEWS, ecoNewsId, request, images, user, locale));
    }

    /**
     * Method to count not deleted comments to certain {@link EcoNewsVO}.
     *
     * @param ecoNewsId to specify {@link EcoNewsVO}
     * @return amount of comments
     */
    @Operation(summary = "Count comments.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND))),
    })
    @GetMapping("/{ecoNewsId}/comments/count")
    public int getCountOfComments(@PathVariable Long ecoNewsId) {
        return commentService.countCommentsForEcoNews(ecoNewsId);
    }

    /**
     * Method to get all active replies to {@link CommentDto} specified by parent
     * comment id.
     *
     * @param parentCommentId id of parent comment {@link CommentDto}
     * @param userVO          {@link UserVO} user who want to get replies
     * @return Pageable of {@link CommentDto} replies
     */
    @Operation(summary = "Get all replies to comment.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND))),
    })
    @ApiPageable(dtoClass = CommentDto.class)
    @GetMapping("/comments/{parentCommentId}/replies/active")
    public ResponseEntity<PageableDto<CommentDto>> getAllActiveReplies(
        @Parameter(hidden = true) Pageable pageable,
        @PathVariable Long parentCommentId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(commentService.getAllActiveReplies(pageable, parentCommentId, userVO));
    }

    /**
     * Method to count of all active replies to certain {@link CommentDto} comment.
     *
     * @param parentCommentId to specify {@link CommentDto}
     * @return amount of all active comments for certain @{@link CommentDto}
     */
    @Operation(summary = "Count replies for comment.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND))),
    })
    @GetMapping("/comments/{parentCommentId}/replies/active/count")
    public int getCountOfActiveReplies(@PathVariable Long parentCommentId) {
        return commentService.countAllActiveReplies(parentCommentId);
    }

    /**
     * Method to mark comment as deleted.
     *
     * @param id comment id
     */
    @Operation(summary = "Mark comment as deleted.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND))),
    })
    @DeleteMapping("comments/{id}")
    public ResponseEntity<Object> delete(
        @PathVariable Long id,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        commentService.delete(id, user);
        return ResponseEntity.ok().build();
    }

    /**
     * Method to update certain {@link CommentVO} specified by id.
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
    @PatchMapping("comments")
    public void update(
        @RequestParam Long commentId,
        @RequestBody @Valid @Size(min = 1, max = 8000) String commentText,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        commentService.update(commentText, commentId, user);
    }

    /**
     * Method to like/dislike certain {@link CommentDto} specified by id.
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
    @PostMapping("/comments/like")
    public void like(
        @RequestParam("commentId") Long commentId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        commentService.like(commentId, userVO, null);
    }

    /**
     * Method to dislike certain {@link CommentDto} specified by id.
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
    public void dislike(
        @RequestParam("commentId") Long commentId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        commentService.dislike(commentId, userVO, null);
    }

    /**
     * Method to get count of likes for a specific comment.
     *
     * @param amountCommentLikesDto dto with id and count likes for comments.
     */
    @MessageMapping("/likeAndCount")
    public void getCountOfLike(@Payload AmountCommentLikesDto amountCommentLikesDto) {
        commentService.countLikes(amountCommentLikesDto);
    }

    /**
     * Method to get all active comments to {@link HabitVO} specified by habitId.
     *
     * @param ecoNewsId id of {@link EcoNewsVO}
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
    @GetMapping("{ecoNewsId}/comments/active")
    @ApiPageableWithoutSort
    public ResponseEntity<PageableDto<CommentDto>> getAllActiveComments(
        @Parameter(hidden = true) Pageable pageable,
        @PathVariable Long ecoNewsId,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(commentService.getAllActiveComments(pageable, user, ecoNewsId, ArticleType.ECO_NEWS));
    }

    /**
     * Method to get certain comment for {@link EcoNewsVO} specified by commentId.
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
            .body(commentService.getCommentById(ArticleType.ECO_NEWS, id, userVO));
    }

    /**
     * Method for getting all users available to tag in comment by search query.
     *
     * @param searchUsers dto with current user ID and search query
     *                    {@link UserSearchDto}.
     * @author Anton Bondar
     */
    @MessageMapping("/getUsersToTagInComment")
    public void getUsersToTagInComment(@Payload UserSearchDto searchUsers) {
        commentService.searchUsers(searchUsers);
    }
}
