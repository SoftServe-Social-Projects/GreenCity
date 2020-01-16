package greencity.service;

import greencity.dto.econews.AddEcoNewsDtoResponse;
import greencity.dto.newssubscriber.NewsSubscriberResponseDto;
import greencity.entity.Category;
import greencity.entity.Place;
import greencity.entity.User;
import greencity.entity.enums.EmailNotification;
import java.util.List;
import java.util.Map;

/**
 * Provides the interface to manage sending emails to {@code User}.
 */
public interface EmailService {
    /**
     * Method for sending notification to {@link User}'s who subscribed for updates about added new places.
     *
     * @param subscribers          - list of {@link User}'s who subscribed
     * @param categoriesWithPlaces - map of {@link Category} as a key and list of {@link Place}'s as a value
     * @param notification         - {@link EmailNotification} type
     */
    void sendAddedNewPlacesReportEmail(List<User> subscribers,
                                       Map<Category, List<Place>> categoriesWithPlaces,
                                       EmailNotification notification);

    /**
     * Method for sending news for users who subscribed for updates.
     */
    void sendNewNewsForSubscriber(List<NewsSubscriberResponseDto> subscribers,
                                  AddEcoNewsDtoResponse newsDto);

    /**
     * Method for sending simple notification to {@code User} about change status.
     *
     * @param updatable - updated {@code Place}
     */
    void sendChangePlaceStatusEmail(Place updatable);

    /**
     * Method for sending verification email to {@link User}.
     *
     * @param user  - {@link User}
     */
    void sendVerificationEmail(User user);

    /**
     * Method for sending email for restore.
     *
     * @param user  - {@link User}
     * @param token - {@link String}
     */
    void sendRestoreEmail(User user, String token);
}
