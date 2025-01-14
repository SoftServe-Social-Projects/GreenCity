package greencity.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import java.util.ResourceBundle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NotificationUtilsTest {
    private static final ResourceBundle uaBundle = ResourceBundle.getBundle("notification_ua");
    private static final ResourceBundle defaultBundle = ResourceBundle.getBundle("notification");

    @ParameterizedTest(name = "Test resolveTimeInUkrainian with input {0}")
    @MethodSource("provideUkrainianTestCases")
    void testResolveTimeInUkrainian(int input, String expected) {
        String result = NotificationUtils.resolveTimesInUkrainian(input);
        assertEquals(expected, result);
    }

    private static Stream<Arguments> provideUkrainianTestCases() {
        return Stream.of(
            Arguments.of(1, ""),
            Arguments.of(2, "2 рази"),
            Arguments.of(4, "4 рази"),
            Arguments.of(5, "5 разів"),
            Arguments.of(11, "11 разів"),
            Arguments.of(19, "19 разів"),
            Arguments.of(21, "21 раз"),
            Arguments.of(22, "22 рази"),
            Arguments.of(25, "25 разів"),
            Arguments.of(100, "100 разів"),
            Arguments.of(101, "101 раз"),
            Arguments.of(102, "102 рази"),
            Arguments.of(112, "112 разів"),
            Arguments.of(123, "123 рази"));
    }

    @ParameterizedTest(name = "Test resolveTimesInEnglish with input {0}")
    @MethodSource("provideEnglishTestCases")
    void testResolveTimesInEnglish(int input, String expected) {
        String result = NotificationUtils.resolveTimesInEnglish(input);
        assertEquals(expected, result);
    }

    private static Stream<Arguments> provideEnglishTestCases() {
        return Stream.of(
            Arguments.of(1, ""),
            Arguments.of(2, "twice"),
            Arguments.of(3, "3 times"),
            Arguments.of(5, "5 times"),
            Arguments.of(11, "11 times"),
            Arguments.of(21, "21 times"));
    }

    @ParameterizedTest(name = "Test isMessageLocalizationRequired for valid notification type {0}")
    @MethodSource("provideValidNotificationTypes")
    void isMessageLocalizationRequiredForValidNotificationTypeTest(String notificationType) {
        assertTrue(NotificationUtils.isMessageLocalizationRequired(notificationType));
    }

    private static Stream<String> provideValidNotificationTypes() {
        return Stream.of(
            "ECONEWS_COMMENT_REPLY", "ECONEWS_COMMENT",
            "EVENT_COMMENT_REPLY", "EVENT_COMMENT",
            "HABIT_COMMENT", "HABIT_COMMENT_REPLY");
    }

    @ParameterizedTest(name = "Test isMessageLocalizationRequired for invalid notification type {0}")
    @MethodSource("provideInvalidNotificationTypes")
    void isMessageLocalizationRequiredForInvalidNotificationTypeTest(String notificationType) {
        assertFalse(NotificationUtils.isMessageLocalizationRequired(notificationType));
    }

    private static Stream<String> provideInvalidNotificationTypes() {
        return Stream.of(
            "ECONEWS_LIKE", "EVENT_COMMENT_LIKE", "HABIT_COMMENT_USER_TAG");
    }

    @ParameterizedTest(name = "Test localizeMessage for input {0}")
    @MethodSource("provideUkrainianLocalizationTestCases")
    void localizeMessageInUaTest(String input, String expected) {
        String localizedMessage = NotificationUtils.localizeMessage(input, uaBundle);
        assertEquals(expected, localizedMessage);
    }

    private static Stream<Arguments> provideUkrainianLocalizationTestCases() {
        return Stream.of(
            Arguments.of("COMMENT", "коментар"),
            Arguments.of("5 COMMENTS", "5 коментарів"),
            Arguments.of("REPLY", "відповідь"),
            Arguments.of("2 REPLIES", "2 відповіді"),
            Arguments.of("comment", "comment"));
    }

    @ParameterizedTest(name = "Test localizeMessage for input {0}")
    @MethodSource("provideDefaultLocalizationTestCases")
    void localizeMessageInDefaultTest(String input, String expected) {
        String localizedMessage = NotificationUtils.localizeMessage(input, defaultBundle);
        assertEquals(expected, localizedMessage);
    }

    private static Stream<Arguments> provideDefaultLocalizationTestCases() {
        return Stream.of(
            Arguments.of("COMMENT", "a comment"),
            Arguments.of("5 COMMENTS", "5 comments"),
            Arguments.of("REPLY", "a reply"),
            Arguments.of("2 REPLIES", "2 replies"),
            Arguments.of("comment", "comment"));
    }
}