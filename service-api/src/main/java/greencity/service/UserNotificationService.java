package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.achievement.ActionDto;
import greencity.dto.notification.LikeNotificationDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.user.UserVO;
import greencity.enums.NotificationType;
import greencity.enums.ProjectName;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.security.Principal;
import java.util.List;

@Service
public interface UserNotificationService {
    /**
     * Method for getting Notification instances filtered.
     *
     * @param pageable          page of notifications
     * @param principal         user to get notifications
     * @param language          language code
     * @param projectName       project name
     * @param notificationTypes types of notification
     * @param viewed            notification is viewed or not. Can be null
     * @return Page of {@link NotificationDto} instance.
     */
    PageableAdvancedDto<NotificationDto> getNotificationsFiltered(Pageable pageable, Principal principal,
        String language, ProjectName projectName, List<NotificationType> notificationTypes, Boolean viewed);

    /**
     * Method for sending socket.
     *
     * @param user user, requested
     * @author Volodymyr Mladonov
     */
    void notificationSocket(ActionDto user);

    /**
     * Method to create Notification for many Users.
     *
     * @param attendersList    list of Users to receive Notification.
     * @param message          title of Event, {message} in template
     * @param notificationType type of Notification
     * @param targetId         represent the corresponding object's ID
     * @author Volodymyr Mladonov
     */
    void createNotificationForAttenders(List<UserVO> attendersList, String message,
        NotificationType notificationType, Long targetId);

    /**
     * Method to create Notification for many Users.
     *
     * @param attendersList    list of Users to receive Notification.
     * @param message          text to be inserted into template instead of
     *                         {message}
     * @param secondMessage    additional text, {secondMessage} in template
     * @param notificationType type of Notification
     * @param targetId         represent the corresponding object's ID
     * @author Volodymyr Mladonov
     */
    void createNotificationForAttenders(List<UserVO> attendersList, String message,
        NotificationType notificationType, Long targetId, String secondMessage);

    /**
     * Method to create Notification without CustomMessage.
     *
     * @param targetUser       user, that should receive Notification
     * @param actionUser       user, that triggered Notification
     * @param notificationType type of Notification
     * @author Volodymyr Mladonov
     */
    void createNotification(UserVO targetUser, UserVO actionUser, NotificationType notificationType);

    /**
     * Method to create Notification.
     *
     * @param targetUser       user, that should receive Notification
     * @param actionUser       user, that performed action
     * @param notificationType type of Notification
     * @param targetId         represent the corresponding object's ID
     * @param customMessage    text of Notification
     * @author Volodymyr Mladonov
     */
    void createNotification(UserVO targetUser, UserVO actionUser, NotificationType notificationType,
        Long targetId, String customMessage);

    /**
     * Creates a notification for a target user. Notifications are uniquely
     * identified by the combination of {@code targetUserId},
     * {@code notificationType}, {@code targetId}, and {@code secondMessageId}.
     *
     * @param targetUser        the user who will receive the notification
     * @param actionUser        the user who performed the action triggering the
     *                          notification
     * @param notificationType  the type of notification to be created
     * @param targetId          represent the corresponding object's ID
     * @param customMessage     a custom message for the notification
     * @param secondMessageId   a secondary identifier for additional context
     * @param secondMessageText a secondary text for additional context
     * @author Vitalii Fedyk
     */
    void createNotification(UserVO targetUser, UserVO actionUser, NotificationType notificationType,
        Long targetId, String customMessage, Long secondMessageId, String secondMessageText);

    /**
     * Creates a notification for a target user. Notifications are uniquely
     * identified by the combination of {@code targetUserId},
     * {@code notificationType}, and {@code targetId}.
     *
     * @param targetUser        the user who will receive the notification
     * @param actionUser        the user who performed the action triggering the
     *                          notification
     * @param notificationType  the type of notification to be created
     * @param targetId          represent the corresponding object's ID
     * @param customMessage     a custom message for the notification
     * @param secondMessageText a secondary text for additional context
     * @author Vitalii Fedyk
     */
    void createNotification(UserVO targetUser, UserVO actionUser, NotificationType notificationType,
        Long targetId, String customMessage, String secondMessageText);

    /**
     * Method to create Notification without actionUser.
     *
     * @param targetUser       user, that should receive Notification
     * @param notificationType type of Notification
     * @param targetId         represent the corresponding object's ID
     * @param customMessage    text to be inserted into Notification {message}
     */
    void createNewNotification(UserVO targetUser, NotificationType notificationType, Long targetId,
        String customMessage);

    /**
     * Method to create Notification without actionUser.
     *
     * @param targetUser       user, that should receive Notification
     * @param notificationType type of Notification
     * @param targetId         represent the corresponding object's ID
     * @param customMessage    text to be inserted into Notification {message}
     * @param secondMessage    text to be inserted into Notification {secondMessage}
     */
    void createNewNotification(UserVO targetUser, NotificationType notificationType, Long targetId,
        String customMessage, String secondMessage);

    /**
     * Method to create Notification without actionUser.
     *
     * @param targetUsers   users, that should receive place added notification
     * @param targetId      represent the corresponding object's ID
     * @param customMessage text to be inserted into Notification {message}
     * @param secondMessage text to be inserted into Notification {secondMessage}
     */
    void createNewNotificationForPlaceAdded(List<UserVO> targetUsers, Long targetId, String customMessage,
        String secondMessage);

    /**
     * Method to remove ActionUser from Notification or delete Notification if that
     * was the only ActionUser. Called when user canceled the action.
     *
     * @param targetUser       User, that should receive Notification
     * @param actionUser       User, that canceled the action
     * @param targetId         represent the corresponding object's ID
     * @param notificationType type of Notification
     * @author Volodymyr Mladonov
     */
    void removeActionUserFromNotification(UserVO targetUser, UserVO actionUser, Long targetId,
        NotificationType notificationType);

    /**
     * Method to delete specific Notification.
     *
     * @param principal      user
     * @param notificationId id of notification, that should be deleted
     * @author Volodymyr Mladonov
     */
    void deleteNotification(Principal principal, Long notificationId);

    /**
     * Method to mark specific Notification as unread.
     *
     * @param notificationId id of notification, that should be marked
     */
    void unreadNotification(Long notificationId);

    /**
     * Method to mark specific Notification as read.
     *
     * @param notificationId id of notification, that should be marked
     */
    void viewNotification(Long notificationId);

    /**
     * Creates a new like notification or updates an existing one. If a notification
     * for the specified news article and target user already exists and is not yet
     * viewed, this method will update the existing notification by adding the
     * action user to the list of users who liked the article. It will also adjust
     * the notification message accordingly to reflect the new state. If no such
     * notification exists, a new notification will be created. If `isLike` is false
     * and the notification exists, the action user will be removed from the list of
     * users who liked the article. If the list becomes empty as a result, the
     * notification will be deleted.
     *
     * @param likeNotificationDto the DTO containing information about the like
     *                            notification
     */
    void createOrUpdateLikeNotification(LikeNotificationDto likeNotificationDto);

    /**
     * Method to send notification on last day of primary duration habit has 20%-79%
     * successful progress.
     */
    void checkLastDayOfHabitPrimaryDurationToMessage();
}
