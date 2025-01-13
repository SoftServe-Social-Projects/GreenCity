package greencity.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationConstants {
    public static final int PLACE_NAME_MAX_LENGTH = 30;
    public static final int PLACE_ADDRESS_MAX_LENGTH = 120;
    public static final int PLACE_ADDRESS_MIN_LENGTH = 3;
    public static final String EMPTY_VALUE_OF_LATITUDE = "{greenCity.validation.empty.latitude}";
    public static final String EMPTY_VALUE_OF_LONGITUDE = "{greenCity.validation.empty.longitude}";
    public static final String MIN_VALUE_LATITUDE = "{greenCity.validation.min.latitude}";
    public static final String MAX_VALUE_LATITUDE = "{greenCity.validation.max.latitude}";
    public static final String MIN_VALUE_LONGITUDE = "{greenCity.validation.min.longitude}";
    public static final String MAX_VALUE_LONGITUDE = "{greenCity.validation.max.longitude}";
    public static final String N_E_LAT_CAN_NOT_BE_NULL = "{greenCity.validation.notNull.northEastLat}";
    public static final String N_E_LNG_CAN_NOT_BE_NULL = "{greenCity.validation.notNull.northEastLng}";
    public static final String S_W_LAT_CAN_NOT_BE_NULL = "{greenCity.validation.notNull.southWestLat}";
    public static final String S_W_LNG_CAN_NOT_BE_NULL = "{greenCity.validation.notNull.southWestLng}";
    public static final int MAX_AMOUNT_OF_TAGS = 3;
    public static final String BAD_COMMA_SEPARATED_NUMBERS = "{greenCity.validation.bad.comma.separated.numbers}";
}
