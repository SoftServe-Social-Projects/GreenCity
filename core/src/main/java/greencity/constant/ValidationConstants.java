package greencity.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationConstants {
    public static final String BAD_COMMA_SEPARATED_NUMBERS =
        "Non-empty string can contain numbers separated by a comma only";
    public static final int PLACE_ADDRESS_MIN_LENGTH = 3;
    public static final int PLACE_ADDRESS_MAX_LENGTH = 120;
    public static final String EMPTY_VALUE_OF_LATITUDE = "{greenCity.validation.empty.latitude}";
    public static final String EMPTY_VALUE_OF_LONGITUDE = "{greenCity.validation.empty.longitude}";
    public static final int MAX_AMOUNT_OF_TAGS = 3;
    public static final int PLACE_NAME_MAX_LENGTH = 30;
    public static final int MAX_EVENT_DATES_AMOUNT = 7;
    public static final String LANGUAGE_VALIDATION_EXCEPTION_MESSAGE = "Select correct language: 'en' or 'ua'";
    public static final String LOCALE_PART_ERROR_MESSAGE = "Locale part";
}
