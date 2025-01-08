package greencity.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EmailNotificationMessagesConstants {
    public static final String ECONEWS_CREATION_SUBJECT = "You have created eco news";
    public static final String ECONEWS_CREATION_MESSAGE = "You successfully created eco news %s";
    public static final String ECONEWS_COMMENTED_SUBJECT = "You received a comment";
    public static final String ECONEWS_COMMENTED_MESSAGE = "You received a comment on your econews: %s";
    public static final String EVENT_CREATION_SUBJECT = "You have created an event";
    public static final String EVENT_CREATION_MESSAGE = "You have created an event: %s";
    public static final String EVENT_CANCELED_SUBJECT = "Event you have joined was canceled";
    public static final String EVENT_CANCELED_MESSAGE = "This event was canceled: %s";
    public static final String EVENT_UPDATED_SUBJECT = "Event you have joined was updated";
    public static final String EVENT_UPDATED_MESSAGE = "This event was updated: %s";
    public static final String EVENT_JOINED_SUBJECT = "New people joined your event";
    public static final String EVENT_JOINED_MESSAGE = "%s has joined your event";
    public static final String EVENT_COMMENTED_SUBJECT = "You received a comment";
    public static final String EVENT_COMMENTED_MESSAGE = "You received a comment on your event: %s";
    public static final String ARTICLE_TAGGED_SUBJECT = "You were tagged in the comments by %s";
    public static final String REPLY_SUBJECT = "You received a reply";
    public static final String REPLY_MESSAGE = "%s replied to you";
    public static final String COMMENT_LIKE_SUBJECT = "You receive a like on your comment";
    public static final String COMMENT_LIKE_MESSAGE = "%s liked your comment";
    public static final String FRIEND_REQUEST_RECEIVED_SUBJECT = "You have received a friend request";
    public static final String FRIEND_REQUEST_RECEIVED_MESSAGE = "%s sent you a friend request";
    public static final String FRIEND_REQUEST_ACCEPTED_SUBJECT = "Your friend request was accepted";
    public static final String FRIEND_REQUEST_ACCEPTED_MESSAGE = "Now you are friends with %s";
}
