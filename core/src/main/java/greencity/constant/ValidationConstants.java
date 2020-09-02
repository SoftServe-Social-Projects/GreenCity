package greencity.constant;

public final class ValidationConstants {
    public static final String EMPTY_USERNAME = "The username field can not be empty";
    public static final String INVALID_USERNAME_LENGTH = "The username field should be between 1 and 20 characters";
    public static final int USERNAME_MIN_LENGTH = 1;
    public static final int USERNAME_MAX_LENGTH = 20;

    public static final String EMPTY_EMAIL = "The email field can not be empty";
    public static final String INVALID_RESTORE_EMAIL_ADDRESS = "Must be a well-formed email address";
    public static final String INVALID_EMAIL = "The email is invalid";
    public static final String INVALID_PASSWORD = "Password has contain at least one character of "
        + "Uppercase letter (A-Z), "
        + "Lowercase letter (a-z), "
        + "Digit (0-9), "
        + "Special character (~`!@#$%^&*()+=_-{}[]|:;”’?/<>,.).";

    public static final String EMPTY_ID = "The id field can not be empty";
    public static final String NEGATIVE_ID = "The id should be positive number";
    public static final String EMPTY_STATUS = "The status field can not be empty";
    public static final String EMPTY_EMAIL_NOTIFICATION = "The emailNotification field can not be empty";

    public static final int PLACE_NAME_MAX_LENGTH = 30;
    public static final int PLACE_ADDRESS_MAX_LENGTH = 120;
    public static final int PLACE_ADDRESS_MIN_LENGTH = 3;

    public static final String CATEGORY_NAME_BAD_FORMED = "Bad formed category name: ${validatedValue}";
    public static final int CATEGORY_NAME_MAX_LENGTH = 30;
    public static final int CATEGORY_NAME_MIN_LENGTH = 3;

    public static final String EMPTY_PLACE_NAME = "The name of place field can not be empty";
    public static final String EMPTY_PLACE_ADDRESS = "The address of place field can not be empty";

    public static final String EMPTY_NAME_OF_CATEGORY = "The category name field can not be empty";

    public static final String EMPTY_VALUE_OF_LATITUDE = "The latitude can not be empty";
    public static final String EMPTY_VALUE_OF_LONGITUDE = "The longitude can not be empty";

    public static final String EMPTY_OPEN_TIME_VALUE = "The opening time can not be empty";
    public static final String EMPTY_CLOSE_TIME_VALUE = "The closing time can not be empty";

    public static final String EMPTY_WEEK_DAY_VALUE = "The week day can not be empty";

    public static final String MIN_VALUE_LATITUDE =
        "The '${validatedValue}' must be at least {value}";
    public static final String MAX_VALUE_LATITUDE =
        "The '${validatedValue}' must be at least {value}";
    public static final String MIN_VALUE_LONGITUDE =
        "The '${validatedValue}' must be at least {value}";
    public static final String MAX_VALUE_LONGITUDE =
        "The '${validatedValue}' must be at least {value}";

    public static final String N_E_LAT_CAN_NOT_BE_NULL = "North-east latitude can not be null";
    public static final String N_E_LNG_CAN_NOT_BE_NULL = "North-east longitude can not be null";
    public static final String S_W_LAT_CAN_NOT_BE_NULL = "South-west latitude can not be null";
    public static final String S_W_LNG_CAN_NOT_BE_NULL = "South-west longitude can not be null";

    public static final String LAT_MIN_VALIDATION = "Has to be greatest or equals -90";
    public static final String LAT_MAX_VALIDATION = "Has to be lower or equals 90";
    public static final String LNG_MIN_VALIDATION = "Has to be greatest or equals -180";
    public static final String LNG_MAX_VALIDATION = "Has to be lower or equals 180";

    public static final String RATE_MIN_VALUE = "The rate must be at least {value}";
    public static final String RATE_MAX_VALUE = "The rate must bigger than {min} and less then {max}.";
    public static final String EMPTY_COMMENT = "The text of comment can not be empty";

    public static final int COMMENT_MIN_LENGTH = 5;
    public static final int COMMENT_MAX_LENGTH = 300;

    public static final String EMPTY_ADVICE = "The text of name can not be empty";
    public static final String INVALID_ADVICE_LENGTH = "Invalid length of name";

    public static final int ADVICE_MIN_LENGTH = 3;
    public static final int ADVICE_MAX_LENGTH = 300;

    public static final String EMPTY_HABIT_FACT = "The text of fact can't be empty";
    public static final String INVALID_HABIT_FACT_LENGTH = "Invalid length of fact";

    public static final int HABIT_FACT_MIN_LENGTH = 3;
    public static final int HABIT_FACT_MAX_LENGTH = 300;

    public static final int DISCOUNT_VALUE_MIN = 0;
    public static final String DISCOUNT_VALUE_DOES_NOT_CORRECT = "Min discount value is 0, max discount value is 100";
    public static final int DISCOUNT_VALUE_MAX = 100;
    public static final String EMPTY_SPECIFICATION_NAME = "The specification name can not be empty";

    public static final String BAD_PHOTO_LIST_REQUEST = "Length of photos list can not be more than {max}.";
    public static final String BAD_OPENING_HOURS_LIST_REQUEST = "Length of opening hours list can not be less than 1.";
    public static final String BAD_DISCOUNT_VALUES_LIST_REQUEST = "Length of discount values list can not be less than 1.";
    public static final String EMPTY_PHOTO_NAME = "The name of photo field can not be empty";

    public static final int MIN_AMOUNT_OF_ITEMS = 0;
    public static final int MAX_AMOUNT_OF_ITEMS = 16;

    public static final int MAX_AMOUNT_OF_TAGS = 3;

    public static final String BAD_COMMA_SEPARATED_NUMBERS =
        "Non-empty string can contain numbers separated by a comma only";

    public static final String USER_CREATED = "User was created. Verification letter has been sent to your email address";

    public static final int NUMBER_OF_RECOMMENDED_ECO_NEWS = 3;

    public static final String CUSTOM_GOAL_TEXT_CANNOT_BE_EMPTY = "Custom goal text cannot be empty";

    public static final String MIN_AMOUNT_OF_TAGS = "At least one tag";

    private ValidationConstants() {
    }
}
