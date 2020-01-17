package greencity.service.impl;

import greencity.constant.AppConstant;
import greencity.constant.LogMessage;
import greencity.entity.Category;
import greencity.entity.Place;
import greencity.entity.User;
import greencity.entity.enums.EmailNotification;
import greencity.entity.enums.PlaceStatus;
import greencity.event.SendImmediatelyReportEvent;
import greencity.repository.PlaceRepo;
import greencity.repository.UserRepo;
import greencity.service.NotificationService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    private static final ZoneId ZONE_ID = ZoneId.of(AppConstant.UKRAINE_TIMEZONE);
    private final UserRepo userRepo;
    private final PlaceRepo placeRepo;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Constructor.
     */
    @Autowired
    public NotificationServiceImpl(UserRepo userRepo, PlaceRepo placeRepo,
                                   ApplicationEventPublisher applicationEventPublisher) {
        this.userRepo = userRepo;
        this.placeRepo = placeRepo;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void sendImmediatelyReport(Place newPlace) {
        log.info(LogMessage.IN_SEND_IMMEDIATELY_REPORT, new Place());
        EmailNotification emailNotification = EmailNotification.IMMEDIATELY;
        List<User> subscribers = getSubscribers(emailNotification);
        Map<Category, List<Place>> categoriesWithPlacesMap = new HashMap<>();
        categoriesWithPlacesMap.put(newPlace.getCategory(), Collections.singletonList(newPlace));

        applicationEventPublisher.publishEvent(
            new SendImmediatelyReportEvent(this, subscribers,
                categoriesWithPlacesMap, emailNotification));
    }

    /**
     * {@inheritDoc}
     *
     * @author Nazar Vladyka
     */
    @Scheduled(cron = "0 0 12 ? * *", zone = AppConstant.UKRAINE_TIMEZONE)
    @Override
    public void sendDailyReport() {
        log.info(LogMessage.IN_SEND_DAILY_REPORT, LocalDateTime.now(ZONE_ID));
        LocalDateTime startDate = LocalDateTime.now(ZONE_ID).minusDays(1);
        sendReport(EmailNotification.DAILY, startDate);
    }

    /**
     * {@inheritDoc}
     *
     * @author Nazar Vladyka
     */
    @Scheduled(cron = "0 0 12 ? * MON", zone = AppConstant.UKRAINE_TIMEZONE)
    @Override
    public void sendWeeklyReport() {
        log.info(LogMessage.IN_SEND_WEEKLY_REPORT, LocalDateTime.now(ZONE_ID));
        LocalDateTime startDate = LocalDateTime.now(ZONE_ID).minusWeeks(1);
        sendReport(EmailNotification.WEEKLY, startDate);
    }

    /**
     * {@inheritDoc}
     *
     * @author Nazar Vladyka
     */
    @Scheduled(cron = "0 0 12 1 * ?", zone = AppConstant.UKRAINE_TIMEZONE)
    @Override
    public void sendMonthlyReport() {
        log.info(LogMessage.IN_SEND_MONTHLY_REPORT, LocalDateTime.now(ZONE_ID));
        LocalDateTime startDate = LocalDateTime.now(ZONE_ID).minusMonths(1);
        sendReport(EmailNotification.MONTHLY, startDate);
    }

    private void sendReport(EmailNotification emailNotification, LocalDateTime startDate) {
        log.info(LogMessage.IN_SEND_REPORT, emailNotification);
        List<User> subscribers = getSubscribers(emailNotification);
        Map<Category, List<Place>> categoriesWithPlacesMap = new HashMap<>();
        LocalDateTime endDate = LocalDateTime.now(ZONE_ID);

        if (!subscribers.isEmpty()) {
            List<Place> places = placeRepo.findAllByModifiedDateBetweenAndStatus(
                startDate, endDate, PlaceStatus.APPROVED);
            categoriesWithPlacesMap = getCategoriesWithPlacesMap(places);
        }
        if (!categoriesWithPlacesMap.isEmpty()) {
            applicationEventPublisher.publishEvent(
                new SendImmediatelyReportEvent(this, subscribers,
                    categoriesWithPlacesMap, emailNotification));
        }
    }

    private List<User> getSubscribers(EmailNotification emailNotification) {
        log.info(LogMessage.IN_GET_SUBSCRIBERS, emailNotification);
        return userRepo.findAllByEmailNotification(emailNotification);
    }

    private Map<Category, List<Place>> getCategoriesWithPlacesMap(List<Place> places) {
        log.info(LogMessage.IN_GET_CATEGORIES_WITH_PLACES_MAP, places);
        Map<Category, List<Place>> categoriesWithPlacesMap = new HashMap<>();
        List<Category> categories = getUniqueCategoriesFromPlaces(places);
        List<Place> placesByCategory;
        for (Category category : categories) {
            placesByCategory = new ArrayList<>();
            for (Place place : places) {
                if (place.getCategory().equals(category)) {
                    placesByCategory.add(place);
                }
            }
            categoriesWithPlacesMap.put(category, placesByCategory);
        }
        return categoriesWithPlacesMap;
    }

    private List<Category> getUniqueCategoriesFromPlaces(List<Place> places) {
        log.info(LogMessage.IN_GET_UNIQUE_CATEGORIES_FROM_PLACES, places);
        return places.stream()
            .map(Place::getCategory)
            .collect(Collectors.toList()).stream()
            .distinct()
            .collect(Collectors.toList());
    }
}
