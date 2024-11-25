package greencity.enums;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationTypeTest {
    @ParameterizedTest
    @EnumSource(names = {
        "ECONEWS_COMMENT_LIKE",
        "EVENT_COMMENT_LIKE",
        "HABIT_COMMENT_LIKE"
    })
    void isCommentLike_ShouldReturnTrueForCommentLikeType(final NotificationType notificationType) {
        assertTrue(NotificationType.isCommentLike(notificationType));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = {
        "ECONEWS_COMMENT_LIKE",
        "EVENT_COMMENT_LIKE",
        "HABIT_COMMENT_LIKE"
    })
    void isCommentLike_ShouldReturnFalseForCommentLikeType(final NotificationType notificationType) {
        assertFalse(NotificationType.isCommentLike(notificationType));
    }
}
