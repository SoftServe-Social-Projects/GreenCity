package greencity.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ServiceValidationConstants {
    public static final int ADVICE_MIN_LENGTH = 3;
    public static final int ADVICE_MAX_LENGTH = 300;
    public static final int MIN_AMOUNT_OF_ITEMS = 0;
    public static final int MAX_AMOUNT_OF_ITEMS = 16;
    public static final String CATEGORY_NAME_BAD_FORMED = "{greenCity.validation.bad.formed.category.name}";
    public static final int CATEGORY_NAME_MIN_LENGTH = 3;
    public static final int CATEGORY_NAME_MAX_LENGTH = 30;
    public static final int PLACE_NAME_MAX_LENGTH = 30;
    public static final int HABIT_FACT_MIN_LENGTH = 3;
    public static final int HABIT_FACT_MAX_LENGTH = 300;
    public static final int USERNAME_MIN_LENGTH = 6;
    public static final int USERNAME_MAX_LENGTH = 30;
    public static final String INVALID_EMAIL = "{greenCity.validation.invalid.email}";
    public static final String MIN_AMOUNT_OF_TAGS = "{greenCity.validation.empty.tags}";
    public static final int MAX_AMOUNT_OF_SOCIAL_NETWORK_LINKS = 5;
    public static final int PLACE_ADDRESS_MAX_LENGTH = 120;
    public static final int PLACE_ADDRESS_MIN_LENGTH = 3;
    public static final String EMPTY_VALUE_OF_LATITUDE = "{greenCity.validation.empty.latitude}";
    public static final String EMPTY_VALUE_OF_LONGITUDE = "{greenCity.validation.empty.longitude}";
    public static final int DISCOUNT_VALUE_MIN = 0;
    public static final String DISCOUNT_VALUE_DOES_NOT_CORRECT = "{greenCity.validation.invalid.discount.value}";
    public static final int DISCOUNT_VALUE_MAX = 100;
    public static final String BAD_OPENING_HOURS_LIST_REQUEST = "{greenCity.validation.bad.openingHoursList}";
    public static final String BAD_DISCOUNT_VALUES_LIST_REQUEST = "{greenCity.validation.bad.discountValues}";
    public static final String BAD_PHOTO_LIST_REQUEST = "{greenCity.validation.bad.photo.request}";
    public static final String NEGATIVE_ID = "{greenCity.validation.negative.id}";
    public static final int COMMENT_MIN_LENGTH = 5;
    public static final int COMMENT_MAX_LENGTH = 300;
    public static final String MIN_VALUE_LATITUDE = "{greenCity.validation.min.latitude}";
    public static final String MAX_VALUE_LATITUDE = "{greenCity.validation.max.latitude}";
    public static final String MIN_VALUE_LONGITUDE = "{greenCity.validation.min.longitude}";
    public static final String MAX_VALUE_LONGITUDE = "{greenCity.validation.max.longitude}";
    public static final String N_E_LNG_CAN_NOT_BE_NULL = "{greenCity.validation.notNull.northEastLng}";
    public static final String N_E_LAT_CAN_NOT_BE_NULL = "{greenCity.validation.notNull.northEastLat}";
    public static final String S_W_LAT_CAN_NOT_BE_NULL = "{greenCity.validation.notNull.southWestLat}";
    public static final String S_W_LNG_CAN_NOT_BE_NULL = "{greenCity.validation.notNull.southWestLng}";
    public static final String LAT_MIN_VALIDATION = "{greenCity.validation.min.lat}";
    public static final String LAT_MAX_VALIDATION = "{greenCity.validation.max.lat}";
    public static final String LNG_MIN_VALIDATION = "{greenCity.validation.min.lng}";
    public static final String LNG_MAX_VALIDATION = "{greenCity.validation.max.lng}";
    public static final String RATE_MIN_VALUE = "{greenCity.validation.min.rate}";
    public static final String RATE_MAX_VALUE = "{greenCity.validation.max.rate}";
    public static final String HABIT_COMPLEXITY = "{greenCity.validation.habit.complexity}";
    public static final String TAG_LIST_MIN_LENGTH = "{greenCity.validation.min.tags}";
    public static final String TAG_LIST_MAX_LENGTH = "{greenCity.validation.max.tags}";
    public static final String EMAIL_REGEXP = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    public static final String HABIT_DEFAULT_DURATION = "{greenCity.validation.habit.defaultDuration}";
}
