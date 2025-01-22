package greencity.constant;

import lombok.experimental.UtilityClass;

import java.time.format.DateTimeFormatter;

@UtilityClass
public class ManagementConstant {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");
    public static final String FILTER_EVENT_DTO = "filterEventDto";
    public static final String FORMATTED_DATE = "formatedDate";
    public static final String EVENT_DTO = "eventDto";
    public static final String PAGEABLE = "pageable";
    public static final String IMAGE_URLS = "imageUrls";
    public static final String SORT_MODEL = "sortModel";
    public static final String BACKEND_ADDRESS_ATTRIBUTE = "backendAddress";
    public static final String EVENT_ATTENDERS = "eventAttenders";
    public static final String IMAGES = "images";
    public static final String EVENT_TAGS = "eventsTag";
    public static final String PAGE_SIZE = "pageSize";
    public static final String GOOGLE_MAP_API_KEY = "googleMapApiKey";
    public static final String AUTHOR = "author";
    public static final String ADD_EVENT_DTO_REQUEST = "addEventDtoRequest";
    public static final String CITIES = "cities";
    public static final String EVENT_ATTENDERS_AVATARS="eventAttendersAvatars";
    public static final String ATTENDERS_PAGE= "attendersPage";
    public static final String USERS_LIKED_PAGE= "usersLikedPage";
    public static final String USERS_DISLIKED_PAGE= "usersDislikedPage";
}
