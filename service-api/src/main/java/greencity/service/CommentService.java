package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.comment.AddCommentDtoRequest;
import greencity.dto.comment.AddCommentDtoResponse;
import greencity.dto.comment.CommentDto;
import greencity.dto.comment.CommentVO;
import greencity.dto.comment.AmountCommentLikesDto;
import greencity.dto.user.UserSearchDto;
import greencity.dto.user.UserVO;
import greencity.enums.ArticleType;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.Locale;

public interface CommentService {
    /**
     * Method to save {@link CommentVO}.
     *
     * @param articleType          type {@link ArticleType} of article to which we
     *                             save comment.
     * @param articleId            id of article to which we save comment.
     * @param addCommentDtoRequest dto with {@link CommentVO} text, parentCommentId.
     * @param userVO               {@link UserVO} that saves the comment.
     * @return {@link AddCommentDtoResponse} instance.
     * @author Dmytro Fedotov
     */
    AddCommentDtoResponse save(ArticleType articleType, Long articleId, AddCommentDtoRequest addCommentDtoRequest,
        MultipartFile[] images, UserVO userVO, Locale locale);

    /**
     * Method to get certain comment specified by commentId.
     *
     * @param id   specifies {@link CommentDto} to which we search for comments
     * @param type specifies {@link ArticleType} to which we search for comments
     * @return comment to certain article specified by commentId.
     */
    CommentDto getCommentById(ArticleType type, Long id, UserVO userVO);

    /**
     * Method to get all not deleted replies for to certain {@link CommentVO}
     * specified by id.
     *
     * @param parentCommentId to specify {@link CommentVO}.
     * @param userVO          {@link UserVO} that want to get replies.
     * @return replies for comment
     */
    PageableDto<CommentDto> getAllActiveReplies(Pageable pageable, Long parentCommentId, UserVO userVO);

    /**
     * Method to count not deleted comments for habit.
     *
     * @param habitId to specify article
     * @return amount of comments
     */
    int countCommentsForHabit(Long habitId);

    /**
     * Method to count not deleted comments for eco-news.
     *
     * @param ecoNewsId to specify article
     * @return {@code int} amount of comments
     */
    int countCommentsForEcoNews(Long ecoNewsId);

    /**
     * Method to count not deleted replies for to certain {@link CommentVO}
     * specified by id.
     *
     * @param parentCommentId to specify {@link CommentVO}.
     * @return amount of replies.
     */
    int countAllActiveReplies(Long parentCommentId);

    /**
     * Method to get all active comments for article specified by articleId.
     *
     * @param pageable  page of article.
     * @param articleId specifies article to which we search comments
     * @return all active comments to certain article specified by articleId.
     */
    PageableDto<CommentDto> getAllActiveComments(Pageable pageable, UserVO userVO, Long articleId, ArticleType type);

    /**
     * Method to like or dislike {@link CommentVO} specified by id.
     *
     * @param commentId id of {@link CommentVO} to like/dislike.
     * @param userVO    current {@link UserVO} that wants to like/dislike.
     */
    void like(Long commentId, UserVO userVO);

    /**
     * Method returns count of likes to certain {@link CommentVO} specified by id.
     *
     * @param commentId id of {@link CommentVO} must be counted.
     * @param userVO    {@link UserVO} user who want to get amount of likes for
     *                  comment.
     *
     * @return amountCommentLikesDto dto with id and count likes for comments.
     */
    AmountCommentLikesDto countLikes(Long commentId, UserVO userVO);

    /**
     * Method to change the existing {@link CommentVO}.
     *
     * @param commentText new text of {@link CommentVO}.
     * @param id          to specify {@link CommentVO} that user wants to change.
     * @param userVO      current {@link UserVO} that wants to change comment.
     */
    void update(String commentText, Long id, UserVO userVO);

    /**
     * Method for deleting the {@link CommentVO} instance by its id.
     *
     * @param id     - {@link CommentVO} instance id which will be deleted.
     * @param userVO current {@link CommentVO} that wants to delete.
     */
    void delete(Long id, UserVO userVO);

    /**
     * Method that allow you to search users by name.
     *
     * @param searchUsers dto with current user ID and search query
     *                    {@link UserSearchDto}.
     * @author Anton Bondar
     */
    void searchUsers(UserSearchDto searchUsers);
}
