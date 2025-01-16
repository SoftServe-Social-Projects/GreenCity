package greencity.service;

import greencity.client.RestClient;
import greencity.constant.AppConstant;
import greencity.constant.LogMessage;
import greencity.dto.category.CategoryDto;
import greencity.dto.language.LanguageVO;
import greencity.dto.notification.EmailNotificationDto;
import greencity.dto.place.PlaceNotificationDto;
import greencity.dto.user.SubscriberDto;
import greencity.entity.Notification;
import greencity.entity.Place;
import greencity.enums.EmailPreference;
import greencity.enums.EmailPreferencePeriodicity;
import greencity.enums.NotificationType;
import greencity.enums.PlaceStatus;
import greencity.message.ScheduledEmailMessage;
import greencity.message.SendReportEmailMessage;
import greencity.repository.NotificationRepo;
import greencity.repository.PlaceRepo;
import greencity.repository.UserNotificationPreferenceRepo;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static greencity.utils.NotificationUtils.resolveTimesInEnglish;
import static greencity.utils.NotificationUtils.resolveTimesInUkrainian;
import static greencity.utils.NotificationUtils.isMessageLocalizationRequired;
import static greencity.utils.NotificationUtils.localizeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private static final ZoneId ZONE_ID = ZoneId.of(AppConstant.UKRAINE_TIMEZONE);
    private final PlaceRepo placeRepo;
    private final NotificationRepo notificationRepo;
    private final ModelMapper modelMapper;
    private final RestClient restClient;
    private final ThreadPoolExecutor emailThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    private final UserNotificationPreferenceRepo userNotificationPreferenceRepo;
    private final UserService userService;
    @Value("${client.address}")
    private String clientAddress;

    /**
     * {@inheritDoc}
     */
    @Scheduled(cron = "0 0 12 ? * *", zone = AppConstant.UKRAINE_TIMEZONE)
    @Override
    public void sendDailyReport() {
        log.info(LogMessage.IN_SEND_DAILY_REPORT, LocalDateTime.now(ZONE_ID));
        LocalDateTime startDate = LocalDateTime.now(ZONE_ID).minusDays(1);
        sendReport(EmailPreferencePeriodicity.DAILY, startDate);
    }

    /**
     * {@inheritDoc}
     */
    @Scheduled(cron = "0 0 12 ? * MON", zone = AppConstant.UKRAINE_TIMEZONE)
    @Override
    public void sendWeeklyReport() {
        log.info(LogMessage.IN_SEND_WEEKLY_REPORT, LocalDateTime.now(ZONE_ID));
        LocalDateTime startDate = LocalDateTime.now(ZONE_ID).minusWeeks(1);
        sendReport(EmailPreferencePeriodicity.WEEKLY, startDate);
    }

    /**
     * {@inheritDoc}
     */
    @Scheduled(cron = "0 0 12 1 * ?", zone = AppConstant.UKRAINE_TIMEZONE)
    @Override
    public void sendMonthlyReport() {
        log.info(LogMessage.IN_SEND_MONTHLY_REPORT, LocalDateTime.now(ZONE_ID));
        LocalDateTime startDate = LocalDateTime.now(ZONE_ID).minusMonths(1);
        sendReport(EmailPreferencePeriodicity.MONTHLY, startDate);
    }

    /**
     * {@inheritDoc}
     */
    @Scheduled(cron = "0 0 11,20 * * *", zone = AppConstant.UKRAINE_TIMEZONE)
    @Override
    public void sendLikeScheduledEmail() {
        LocalDateTime now = LocalDateTime.now(ZONE_ID);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.ECONEWS_COMMENT_LIKE);
        sendScheduledNotifications(NotificationType.ECONEWS_COMMENT_LIKE, EmailPreference.LIKES, now);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.ECONEWS_LIKE);
        sendScheduledNotifications(NotificationType.ECONEWS_LIKE, EmailPreference.LIKES, now);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.EVENT_COMMENT_LIKE);
        sendScheduledNotifications(NotificationType.EVENT_COMMENT_LIKE, EmailPreference.LIKES, now);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.HABIT_LIKE);
        sendScheduledNotifications(NotificationType.HABIT_LIKE, EmailPreference.LIKES, now);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.HABIT_COMMENT_LIKE);
        sendScheduledNotifications(NotificationType.HABIT_COMMENT_LIKE, EmailPreference.LIKES, now);
    }

    /**
     * {@inheritDoc}
     */
    @Scheduled(cron = "0 0 10,19 * * *", zone = AppConstant.UKRAINE_TIMEZONE)
    @Override
    public void sendCommentScheduledEmail() {
        LocalDateTime now = LocalDateTime.now(ZONE_ID);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.ECONEWS_COMMENT);
        sendScheduledNotifications(NotificationType.ECONEWS_COMMENT, EmailPreference.COMMENTS, now);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.EVENT_COMMENT);
        sendScheduledNotifications(NotificationType.EVENT_COMMENT, EmailPreference.COMMENTS, now);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.HABIT_COMMENT);
        sendScheduledNotifications(NotificationType.HABIT_COMMENT, EmailPreference.COMMENTS, now);
    }

    /**
     * {@inheritDoc}
     */
    @Scheduled(cron = "0 0 10,19 * * *", zone = AppConstant.UKRAINE_TIMEZONE)
    @Override
    public void sendCommentReplyScheduledEmail() {
        LocalDateTime now = LocalDateTime.now(ZONE_ID);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.ECONEWS_COMMENT_REPLY);
        sendScheduledNotifications(NotificationType.ECONEWS_COMMENT_REPLY, EmailPreference.COMMENTS, now);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.EVENT_COMMENT_REPLY);
        sendScheduledNotifications(NotificationType.EVENT_COMMENT_REPLY, EmailPreference.COMMENTS, now);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.HABIT_COMMENT_REPLY);
        sendScheduledNotifications(NotificationType.HABIT_COMMENT_REPLY, EmailPreference.COMMENTS, now);
    }

    /**
     * {@inheritDoc}
     */
    @Scheduled(cron = "0 0 11,20 * * *", zone = AppConstant.UKRAINE_TIMEZONE)
    @Override
    public void sendFriendRequestScheduledEmail() {
        LocalDateTime now = LocalDateTime.now(ZONE_ID);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.FRIEND_REQUEST_RECEIVED);
        sendScheduledNotifications(NotificationType.FRIEND_REQUEST_RECEIVED, EmailPreference.INVITES, now);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.FRIEND_REQUEST_ACCEPTED);
        sendScheduledNotifications(NotificationType.FRIEND_REQUEST_ACCEPTED, EmailPreference.INVITES, now);
    }

    /**
     * {@inheritDoc}
     */
    @Scheduled(cron = "0 0 10,19 * * *", zone = AppConstant.UKRAINE_TIMEZONE)
    @Override
    public void sendTaggedInCommentScheduledEmail() {
        LocalDateTime now = LocalDateTime.now(ZONE_ID);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.EVENT_COMMENT_USER_TAG);
        sendScheduledNotifications(NotificationType.EVENT_COMMENT_USER_TAG, EmailPreference.COMMENTS, now);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.ECONEWS_COMMENT_USER_TAG);
        sendScheduledNotifications(NotificationType.ECONEWS_COMMENT_USER_TAG, EmailPreference.COMMENTS, now);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.HABIT_COMMENT_USER_TAG);
        sendScheduledNotifications(NotificationType.HABIT_COMMENT_USER_TAG, EmailPreference.COMMENTS, now);
    }

    /**
     * {@inheritDoc}
     */
    @Scheduled(cron = "0 0 11,20 * * *", zone = AppConstant.UKRAINE_TIMEZONE)
    @Override
    public void sendHabitInviteScheduledEmail() {
        LocalDateTime now = LocalDateTime.now(ZONE_ID);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.HABIT_INVITE);
        sendScheduledNotifications(NotificationType.HABIT_INVITE, EmailPreference.INVITES, now);
    }

    /**
     * {@inheritDoc}
     */
    @Scheduled(cron = "0 0 9,18 * * *", zone = AppConstant.UKRAINE_TIMEZONE)
    @Override
    public void sendSystemNotificationsScheduledEmail() {
        LocalDateTime now = LocalDateTime.now(ZONE_ID);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.ECONEWS_CREATED);
        sendScheduledNotifications(NotificationType.ECONEWS_CREATED, EmailPreference.SYSTEM, now);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.EVENT_CREATED);
        sendScheduledNotifications(NotificationType.EVENT_CREATED, EmailPreference.SYSTEM, now);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.EVENT_CANCELED);
        sendScheduledNotifications(NotificationType.EVENT_CANCELED, EmailPreference.SYSTEM, now);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.EVENT_NAME_UPDATED);
        sendScheduledNotifications(NotificationType.EVENT_NAME_UPDATED, EmailPreference.SYSTEM, now);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.EVENT_UPDATED);
        sendScheduledNotifications(NotificationType.EVENT_UPDATED, EmailPreference.SYSTEM, now);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.EVENT_JOINED);
        sendScheduledNotifications(NotificationType.EVENT_JOINED, EmailPreference.SYSTEM, now);
        log.info(LogMessage.IN_SEND_SCHEDULED_EMAIL, now, NotificationType.HABIT_LAST_DAY_OF_PRIMARY_DURATION);
        sendScheduledNotifications(NotificationType.HABIT_LAST_DAY_OF_PRIMARY_DURATION, EmailPreference.SYSTEM, now);
    }

    private void sendScheduledNotifications(NotificationType type, EmailPreference emailPreference, LocalDateTime now) {
        emailThreadPool.submit(() -> {
            List<Notification> notifications =
                notificationRepo.findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(type);
            if (!notifications.isEmpty()) {
                notifications.stream()
                    .filter(n -> isTimeToSendScheduleNotification(n.getTargetUser().getId(), emailPreference, now))
                    .map(notification -> notification.setEmailSent(true))
                    .forEach(notification -> {
                        ScheduledEmailMessage message = createScheduledEmailMessage(notification,
                            notification.getTargetUser().getLanguage().getCode());
                        restClient.sendScheduledEmailNotification(message);
                    });
                notificationRepo.saveAll(notifications);
            }
        });
    }

    private boolean isTimeToSendScheduleNotification(Long userId, EmailPreference emailPreference, LocalDateTime now) {
        boolean timeToSend = userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(userId,
            emailPreference, EmailPreferencePeriodicity.TWICE_A_DAY);
        if (now.getHour() < 12) {
            timeToSend = timeToSend || userNotificationPreferenceRepo
                .existsByUserIdAndEmailPreferenceAndPeriodicity(userId, emailPreference,
                    EmailPreferencePeriodicity.DAILY);
        }
        if (now.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
            timeToSend = timeToSend || userNotificationPreferenceRepo
                .existsByUserIdAndEmailPreferenceAndPeriodicity(userId, emailPreference,
                    EmailPreferencePeriodicity.WEEKLY);
        }
        if (now.getDayOfMonth() == 1) {
            timeToSend = timeToSend || userNotificationPreferenceRepo
                .existsByUserIdAndEmailPreferenceAndPeriodicity(userId, emailPreference,
                    EmailPreferencePeriodicity.MONTHLY);
        }
        return timeToSend;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendEmailNotification(EmailNotificationDto notificationDto) {
        Notification notification = modelMapper.map(notificationDto, Notification.class);
        NotificationType type = notification.getNotificationType();
        LanguageVO userLanguage = userService.findById(notification.getTargetUser().getId()).getLanguageVO();
        ScheduledEmailMessage message = createScheduledEmailMessage(notification, userLanguage.getCode());
        List<NotificationType> likes = List.of(
            NotificationType.ECONEWS_COMMENT_LIKE,
            NotificationType.ECONEWS_LIKE,
            NotificationType.EVENT_COMMENT_LIKE,
            NotificationType.HABIT_LIKE,
            NotificationType.HABIT_COMMENT_LIKE);
        List<NotificationType> comments = List.of(
            NotificationType.ECONEWS_COMMENT,
            NotificationType.EVENT_COMMENT,
            NotificationType.HABIT_COMMENT,
            NotificationType.ECONEWS_COMMENT_REPLY,
            NotificationType.EVENT_COMMENT_REPLY,
            NotificationType.HABIT_COMMENT_REPLY,
            NotificationType.ECONEWS_COMMENT_USER_TAG,
            NotificationType.EVENT_COMMENT_USER_TAG,
            NotificationType.HABIT_COMMENT_USER_TAG);
        List<NotificationType> invites = List.of(
            NotificationType.FRIEND_REQUEST_RECEIVED,
            NotificationType.FRIEND_REQUEST_ACCEPTED,
            NotificationType.HABIT_INVITE);
        List<NotificationType> systems = List.of(
            NotificationType.ECONEWS_CREATED,
            NotificationType.EVENT_CREATED,
            NotificationType.EVENT_CANCELED,
            NotificationType.EVENT_NAME_UPDATED,
            NotificationType.EVENT_UPDATED,
            NotificationType.EVENT_JOINED,
            NotificationType.HABIT_LAST_DAY_OF_PRIMARY_DURATION);
        List<NotificationType> places = List.of(
            NotificationType.PLACE_STATUS,
            NotificationType.PLACE_ADDED);
        try {
            if (likes.contains(type)) {
                restClient.sendEmailNotificationLikes(message);
            } else if (comments.contains(type)) {
                restClient.sendEmailNotificationComments(message);
            } else if (invites.contains(type)) {
                restClient.sendEmailNotificationInvites(message);
            } else if (systems.contains(type)) {
                restClient.sendEmailNotificationSystem(message);
            } else if (places.contains(type)) {
                restClient.sendEmailNotificationPlaces(message);
            }
            notification.setEmailSent(true);
            notificationRepo.save(notification);
        } catch (Exception e) {
            log.warn(LogMessage.IN_SEND_SCHEDULED_EMAIL, LocalDateTime.now(), type);
        }
    }

    private void sendReport(EmailPreferencePeriodicity periodicity, LocalDateTime startDate) {
        log.info(LogMessage.IN_SEND_REPORT, periodicity);
        LocalDateTime now = LocalDateTime.now(ZONE_ID);
        List<SubscriberDto> subscribers = userService.getUsersIdByEmailPreferenceAndEmailPeriodicity(
            EmailPreference.PLACES, periodicity).stream()
            .filter(u -> isTimeToSendScheduleNotification(u.getId(), EmailPreference.PLACES, now))
            .map(o -> modelMapper.map(o, SubscriberDto.class))
            .toList();

        Map<CategoryDto, List<PlaceNotificationDto>> categoriesDtoWithPlacesDtoMap = new HashMap<>();
        if (!subscribers.isEmpty()) {
            List<Place> places = placeRepo.findAllByModifiedDateBetweenAndStatus(startDate.atZone(ZONE_ID),
                now.atZone(ZONE_ID), PlaceStatus.APPROVED);
            categoriesDtoWithPlacesDtoMap = getCategoriesDtoWithPlacesDtoMap(places);
        }
        if (!categoriesDtoWithPlacesDtoMap.isEmpty()) {
            restClient.sendReport(new SendReportEmailMessage(subscribers, categoriesDtoWithPlacesDtoMap, periodicity));
        }
    }

    private Map<CategoryDto, List<PlaceNotificationDto>> getCategoriesDtoWithPlacesDtoMap(List<Place> places) {
        log.info(LogMessage.IN_GET_CATEGORIES_WITH_PLACES_MAP, places.toString());
        List<PlaceNotificationDto> placeDto = places.stream()
            .map(o -> modelMapper.map(o, PlaceNotificationDto.class))
            .toList();
        Map<CategoryDto, List<PlaceNotificationDto>> categoriesWithPlacesMap = new HashMap<>();
        List<CategoryDto> categories = getUniqueCategoriesFromPlaces(places);
        List<PlaceNotificationDto> placesByCategory = new ArrayList<>();
        for (CategoryDto category : categories) {
            for (PlaceNotificationDto place : placeDto) {
                if (place.getCategory().equals(category)) {
                    placesByCategory.add(place);
                }
            }
            categoriesWithPlacesMap.put(category, placesByCategory);
        }
        return categoriesWithPlacesMap;
    }

    private List<CategoryDto> getUniqueCategoriesFromPlaces(List<Place> places) {
        log.info(LogMessage.IN_GET_UNIQUE_CATEGORIES_FROM_PLACES, places);
        return places.stream()
            .map(Place::getCategory)
            .distinct()
            .map(o -> modelMapper.map(o, CategoryDto.class))
            .toList();
    }

    private ScheduledEmailMessage createScheduledEmailMessage(Notification notification, String language) {
        ResourceBundle bundle = ResourceBundle.getBundle("notification", Locale.forLanguageTag(language),
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT));
        String notificationType = notification.getNotificationType().toString();
        String subject = bundle.getString(notificationType + "_TITLE");
        String bodyTemplate = bundle.getString(notificationType);
        String actionUserText;
        long actionUsersSize = notification.getActionUsers().stream().distinct().toList().size();
        if (actionUsersSize > 1) {
            actionUserText = actionUsersSize + " " + bundle.getString("USERS");
        } else if (actionUsersSize == 1) {
            actionUserText = notification.getActionUsers().getFirst().getName();
        } else {
            actionUserText = "";
        }
        String customMessage = notification.getCustomMessage() != null ? notification.getCustomMessage() : "";
        if (!customMessage.isEmpty() && isMessageLocalizationRequired(notificationType)) {
            customMessage = localizeMessage(customMessage, bundle);
        }
        String secondMessage = notification.getSecondMessage() != null ? notification.getSecondMessage() : "";
        int messagesCount = notification.getActionUsers().size();
        String times = language.equals("ua")
            ? resolveTimesInUkrainian(messagesCount)
            : resolveTimesInEnglish(messagesCount);
        String body = bodyTemplate
            .replace("{user}", actionUserText)
            .replace("{message}", customMessage)
            .replace("{secondMessage}", secondMessage)
            .replace("{times}", times);

        return ScheduledEmailMessage.builder()
            .email(notification.getTargetUser().getEmail())
            .username(notification.getTargetUser().getName())
            .baseLink(createBaseLink(notification))
            .subject(subject)
            .body(body)
            .language(language)
            .isUbs(false)
            .build();
    }

    private String createBaseLink(Notification notification) {
        if (notification.getNotificationType() == NotificationType.EVENT_COMMENT) {
            return clientAddress + "/#/events/" + notification.getTargetId();
        }
        return clientAddress + "/#/profile/" + notification.getTargetUser().getId() + "/notifications";
    }
}
