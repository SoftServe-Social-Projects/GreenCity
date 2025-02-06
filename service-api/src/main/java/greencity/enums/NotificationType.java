package greencity.enums;

import java.util.EnumSet;

public enum NotificationType {
    ECONEWS_COMMENT_REPLY,
    ECONEWS_COMMENT_LIKE,
    ECONEWS_LIKE,
    ECONEWS_CREATED,
    ECONEWS_COMMENT,
    ECONEWS_COMMENT_USER_TAG,
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
    PLACE_ADDED,
    EVENT_REQUEST_ACCEPTED,
    EVENT_REQUEST_DECLINED,
    EVENT_INVITE;

    private static final EnumSet<NotificationType> COMMENT_LIKE_TYPES = EnumSet.of(
        ECONEWS_COMMENT_LIKE, EVENT_COMMENT_LIKE, HABIT_COMMENT_LIKE);

    public static boolean isCommentLike(final NotificationType notificationType) {
        return COMMENT_LIKE_TYPES.contains(notificationType);
    }

    private static final EnumSet<NotificationType> INVITE_REQUEST_TYPES = EnumSet.of(
        FRIEND_REQUEST_RECEIVED, HABIT_INVITE);

    public static boolean isInviteOrRequest(final NotificationType notificationType) {
        return INVITE_REQUEST_TYPES.contains(notificationType);
    }
}
