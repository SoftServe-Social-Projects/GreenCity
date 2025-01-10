package greencity.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LogMessage {
    public static final String IN_SAVE = "in save(), entity: {}";
    public static final String IN_FIND_BY_ID = "in findById(), id: {}";
    public static final String IN_DELETE_BY_ID = "in deleteById(), id: {}";
    public static final String IN_FIND_ALL = "in findAll()";
    public static final String IN_UPDATE = "in update(), updated entity: {}";
    public static final String IN_SEND_IMMEDIATELY_REPORT = "in sendImmediatelyReport(), newPlace: {}";
    public static final String IN_SEND_DAILY_REPORT = "in sendDailyReport(), time: {}";
    public static final String IN_SEND_WEEKLY_REPORT = "in sendWeeklyReport(), time: {}";
    public static final String IN_SEND_MONTHLY_REPORT = "in sendMonthlyReport(), time: {}";
    public static final String IN_SEND_REPORT = "in sendReport(), notificationType: {}";
    public static final String IN_GET_SUBSCRIBERS = "in getSubscribers(), notificationType: {}";
    public static final String IN_GET_CATEGORIES_WITH_PLACES_MAP = "in getCategoriesWithPlacesMap(), places: {}";
    public static final String IN_GET_UNIQUE_CATEGORIES_FROM_PLACES = "in getUniqueCategoriesFromPlaces(), places: {}";
    public static final String IN_FIND_ID_BY_EMAIL = "in findIdByEmail(), email: {}";
    public static final String IN_UPDATE_DISCOUNT_FOR_PLACE = "in updateDiscountForUpdatedPlace()";
    public static final String IN_UPDATE_OPENING_HOURS_FOR_PLACE = "in updateOpeningHoursForUpdatedPlace()";
    public static final String IN_BULK_DELETE = "in bulkDelete(), ids: {}";
    public static final String IN_UPDATE_PLACE_STATUS = "in updateStatus(), place id: {} and status: {}";
    public static final String IN_UPDATE_PLACE_STATUSES = "in updateStatuses(), updated statuses: {}";
    public static final String IN_EXISTS_BY_ID = "in existsById(), id: {}";
    public static final String IN_AVERAGE_RATE = "in averageRate(), id: {}";
    public static final String PLACE_STATUS_NOT_DIFFERENT = "the place with id: {} already has status: {}";
    public static final String IN_DELETE_BY_PLACE_ID_AND_USER_EMAIL =
        "in deleteByPlaceIdAndUserEmail(), place id: {} and status: {} ";
    public static final String IN_FIND_BY_PLACE_ID = "in findById(), placeId: {}";
    public static final String IN_GET_ACCESS_PLACE_AS_FAVORITE_PLACE =
        "in getAccessPlaceAsFavoritePlace(), placeId: {}";
    public static final String IN_GET_FAVORITE_PLACE_WITH_LOCATION =
        "in getFavoritePlaceWithLocation(), place id: {} and email: {}";
    public static final String IN_SEND_SCHEDULED_EMAIL = "in sendEmail(), time: {}, type: {}";
}
