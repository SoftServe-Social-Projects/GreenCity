package greencity.constant;

public final class AppConstant {
    public static final String UKRAINE_TIMEZONE = "Europe/Kiev";
    public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String REGISTRATION_EMAIL_FIELD_NAME = "email";
    public static final Integer CONSTANT_OF_FORMULA_HAVERSINE_KM = 6371;
    public static final String GOOGLE_PICTURE = "picture";
    public static final String ADMIN = "ADMIN";
    public static final String UBS_EMPLOYEE = "UBS_EMPLOYEE";
    public static final String MODERATOR = "MODERATOR";
    public static final String USER = "USER";
    public static final String AUTHORIZATION = "Authorization";
    public static final String ROLE = "role";
    public static final String VALIDATION_EMAIL = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
    public static final String DEFAULT_LANGUAGE_CODE = "en";
    public static final Double DEFAULT_RATING = 0.0;
    public static final String DEFAULT_SOCIAL_NETWORK_IMAGE_HOST_PATH = "img/default_social_network_icon.png";
    public static final String USERNAME = "name";
    public static final String FACEBOOK_OBJECT_ID = "me";
    public static final Integer MAX_NUMBER_OF_HABIT_ASSIGNS_FOR_USER = 6;
    public static final int MIN_DAYS_DURATION_OF_HABIT_ASSIGN_FOR_USER = 7;
    public static final int MAX_DAYS_DURATION_OF_HABIT_ASSIGN_FOR_USER = 56;
    public static final Integer MAX_PASSED_DAYS_OF_ABILITY_TO_ENROLL = 8;
    public static final String DEFAULT_HABIT_IMAGE =
        "https://csb10032000a548f571.blob.core.windows.net/allfiles/8f09887c-2fbf-4ee1-95fc-6763a1873b93EventDefaultImage.png";

    private AppConstant() {
    }
}
