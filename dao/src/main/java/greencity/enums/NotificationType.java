package greencity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NotificationType {
    ECONEWS_COMMENT_REPLY,
    EVENT_COMMENT_REPLY,
    ECONEWS_COMMENT_LIKE,
    ECONEWS_LIKE,
    ECONEWS_CREATED,
    EVENT_CREATED,
    ECONEWS_COMMENT,
    EVENT_COMMENT_LIKE,
    EVENT_CANCELED,
    EVENT_UPDATED,
    EVENT_JOINED,
    EVENT_COMMENT,
    FRIEND_REQUEST_ACCEPTED,
    FRIEND_REQUEST_RECEIVED
}
