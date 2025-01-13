package greencity.repository;

import greencity.entity.Comment;
import greencity.entity.EcoNews;
import greencity.entity.Habit;
import greencity.enums.ArticleType;
import greencity.enums.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface CommentRepo extends JpaRepository<Comment, Long> {
    /**
     * Method returns all {@link Comment} not deleted replies to the comment by
     * page.
     *
     * @param pageable        page of news.
     * @param parentCommentId id of {@link Comment} parent comment
     * @return all replies to comment, specified by parentCommentId and page.
     */
    Page<Comment> findAllByParentCommentIdAndStatusNotOrderByCreatedDateDesc(Pageable pageable, Long parentCommentId,
        CommentStatus commentStatus);

    /**
     * The method returns the count of not deleted comments, specified by.
     *
     * @param habitId {@link Long} - id of habit.
     * @return count of not deleted comments, specified by {@link Habit}
     */
    @Query(value = "select count(c.id) from comments c "
        + "join habits h on h.id = c.article_id "
        + "where h.id = :habitId and c.status <>'DELETED' "
        + "and c.article_type = 'HABIT'", nativeQuery = true)
    int countNotDeletedCommentsByHabit(Long habitId);

    /**
     * The method returns the count of not deleted comments, specified by.
     *
     * @param ecoNewsId {@link Long} - id of eco-news
     * @return count of not deleted comments, specified by {@link EcoNews}
     */
    @Query(value = "SELECT COUNT(c.id) from comments c "
        + "join eco_news e on e.id = c.article_id "
        + "where e.id =:ecoNewsId and c.status <> 'DELETED' "
        + "and c.article_type = 'ECO_NEWS'", nativeQuery = true)
    int countNotDeletedCommentsByEcoNews(Long ecoNewsId);

    /**
     * The method returns the count of not deleted comments, specified by.
     *
     * @param eventId {@link Long} - id of eco-news
     * @return count of not deleted comments, specified by {@link EcoNews}
     */
    @Query(value = "SELECT COUNT(c.id) from comments c "
        + "join events e on e.id = c.article_id "
        + "where e.id =:eventId and c.status <> 'DELETED' "
        + "and c.article_type = 'EVENT'", nativeQuery = true)
    int countNotDeletedCommentsByEvent(Long eventId);

    /**
     * The method returns not deleted comment {@link Comment}, specified by id.
     *
     * @param id id of {@link Comment} parent comment
     * @return not deleted comment by it id
     */
    Optional<Comment> findByIdAndStatusNot(Long id, CommentStatus status);

    /**
     * Method returns the count of not deleted replies to the comment, specified by
     * parent comment {@link Comment} id.
     *
     * @param id id of {@link Comment} parent comment
     * @return count of comments, specified by {@link Comment}
     */
    int countByParentCommentIdAndStatusNot(Long id, CommentStatus status);

    /**
     * Method returns all {@link Comment} by page.
     *
     * @param pageable    page of news.
     * @param articleId   id of article for which we search comments.
     * @param articleType type {@link ArticleType} of article for which we search
     *                    comments.
     * @return all active {@link Comment} by page.
     */
    Page<Comment> findAllByParentCommentIdIsNullAndArticleIdAndArticleTypeAndStatusNotOrderByCreatedDateDesc(
        Pageable pageable, Long articleId, ArticleType articleType, CommentStatus commentStatus);

    /**
     * Method returns count of replies to comment, specified by parentCommentId.
     *
     * @param parentCommentId id of comment, count of replies to which we get.
     * @return count of replies to comment, specified by parentCommentId.
     */
    @Query("SELECT count(c) from Comment c where c.parentComment.id = :parentCommentId AND c.status <> 'DELETED'")
    int countByParentCommentId(Long parentCommentId);
}
