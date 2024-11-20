package greencity.enums;

import java.util.EnumSet;

public enum NotificationType {
    ECONEWS_COMMENT_REPLY,
    ECONEWS_COMMENT_LIKE,
    ECONEWS_LIKE,
    ECONEWS_CREATED,
    ECONEWS_COMMENT_USER_TAG,
    ECONEWS_COMMENT,
    EVENT_COMMENT_REPLY,
    EVENT_COMMENT_LIKE,
    EVENT_COMMENT_USER_TAG,
    EVENT_CREATED,
    EVENT_CANCELED,
    EVENT_NAME_UPDATED,
    EVENT_UPDATED,
    EVENT_JOINED,
    EVENT_COMMENT,
    EVENT_LIKE,
    FRIEND_REQUEST_ACCEPTED,
    FRIEND_REQUEST_RECEIVED,
    HABIT_LIKE,
    HABIT_INVITE,
    HABIT_COMMENT,
    HABIT_COMMENT_LIKE,
    HABIT_COMMENT_REPLY,
    HABIT_COMMENT_USER_TAG,
    HABIT_LAST_DAY_OF_PRIMARY_DURATION,
    PLACE_STATUS,
    PLACE_ADDED;

    private static final EnumSet<NotificationType> COMMENT_LIKE_TYPES = EnumSet.of(
        ECONEWS_COMMENT_LIKE, EVENT_COMMENT_LIKE, HABIT_COMMENT_LIKE);
    private static final EnumSet<NotificationType> COMMENT_REPLY_TYPES = EnumSet.of(
        ECONEWS_COMMENT_REPLY, EVENT_COMMENT_REPLY, HABIT_COMMENT_REPLY);
    private static final EnumSet<NotificationType> TAG_TYPES = EnumSet.of(
        ECONEWS_COMMENT_USER_TAG, EVENT_COMMENT_USER_TAG, HABIT_COMMENT_USER_TAG);

    public static boolean isCommentLike(final NotificationType notificationType) {
        return COMMENT_LIKE_TYPES.contains(notificationType);
    }

    public static boolean isCommentReply(final NotificationType notificationType) {
        return COMMENT_REPLY_TYPES.contains(notificationType);
    }

    public static boolean isTag(final NotificationType notificationType) {
        return TAG_TYPES.contains(notificationType);
    }
}
