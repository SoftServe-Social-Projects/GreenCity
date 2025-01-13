package greencity.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.ResourceBundle;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationUtils {
    private static final String REPLIES = "REPLIES";
    private static final String REPLY = "REPLY";
    private static final String COMMENTS = "COMMENTS";
    private static final String COMMENT = "COMMENT";

    public static String resolveTimesInEnglish(final int number) {
        return switch (number) {
            case 1 -> "";
            case 2 -> "twice";
            default -> number + " times";
        };
    }

    public static String resolveTimesInUkrainian(int number) {
        number = Math.abs(number);
        final int lastTwoDigits = number % 100;
        final int lastDigit = number % 10;

        if (number == 1) {
            return "";
        }

        if (lastTwoDigits >= 11 && lastTwoDigits <= 19) {
            return number + " разів";
        }

        return switch (lastDigit) {
            case 1 -> number + " раз";
            case 2, 3, 4 -> number + " рази";
            default -> number + " разів";
        };
    }

    public static boolean isMessageLocalizationRequired(String notificationType) {
        return switch (notificationType) {
            case "ECONEWS_COMMENT_REPLY", "ECONEWS_COMMENT",
                "EVENT_COMMENT_REPLY", "EVENT_COMMENT",
                "HABIT_COMMENT", "HABIT_COMMENT_REPLY" -> true;
            default -> false;
        };
    }

    public static String localizeMessage(String message, ResourceBundle bundle) {
        if (message.contains(REPLIES)) {
            message = message.replace(REPLIES, bundle.getString(REPLIES));
        }
        if (message.contains(REPLY)) {
            message = message.replace(REPLY, bundle.getString(REPLY));
        }
        if (message.contains(COMMENTS)) {
            message = message.replace(COMMENTS, bundle.getString(COMMENTS));
        }
        if (message.contains(COMMENT)) {
            message = message.replace(COMMENT, bundle.getString(COMMENT));
        }
        return message;
    }
}
