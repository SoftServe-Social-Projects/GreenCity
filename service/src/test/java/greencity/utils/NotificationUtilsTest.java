package greencity.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationUtilsTest {
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
}