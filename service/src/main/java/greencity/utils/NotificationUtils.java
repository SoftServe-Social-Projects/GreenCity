package greencity.utils;

public class NotificationUtils {
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
}
