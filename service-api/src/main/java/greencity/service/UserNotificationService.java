package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.achievement.ActionDto;
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
     * Method to create Notification.
     *
     * @param targetUser        user, that should receive Notification
     * @param actionUser        user, that performed action
     * @param notificationType  type of Notification
     * @param targetId          represent the corresponding object's ID
     * @param customMessage     text of Notification, {message} in template
     * @param secondMessageId   if to secondMessageText
     * @param secondMessageText additional text, {secondMessage} in template
     * @author Volodymyr Mladonov
     */
    void createNotification(UserVO targetUser, UserVO actionUser, NotificationType notificationType,
        Long targetId, String customMessage, Long secondMessageId, String secondMessageText);

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
     * Method to create a new or update an existing habit invite notification. If a
     * notification for the specified habit and user already exists, this method
     * updates the notification by adding the new user to the list of action users
     * and adjusting the notification message accordingly. If no such notification
     * exists, a new notification is created.
     *
     * @param targetUserVO the user who is invited to the habit
     * @param actionUserVO the user who sends the invitation
     * @param habitId      the ID of the habit for which the invitation is sent
     * @param habitName    the name of the habit being invited to
     */
    void createOrUpdateHabitInviteNotification(UserVO targetUserVO, UserVO actionUserVO, Long habitId,
        String habitName);

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
     * @param targetUserVO the user who owns the news article and will receive the
     *                     notification.
     * @param actionUserVO the user who liked or unliked the news article.
     * @param newsId       the ID of the news article that was liked or unliked.
     * @param newsTitle    the title of the news article, which may be shortened for
     *                     the notification.
     * @param isLike       a boolean flag indicating whether the action is a like
     *                     (true) or an unlike (false).
     */
    void createOrUpdateLikeNotification(UserVO targetUserVO, UserVO actionUserVO, Long newsId, String newsTitle,
        NotificationType notificationType, boolean isLike);
}
