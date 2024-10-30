package greencity.repository;

import greencity.entity.Notification;
import greencity.enums.NotificationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface NotificationRepo extends CustomNotificationRepo, JpaRepository<Notification, Long>,
    JpaSpecificationExecutor<Notification> {
    /**
     * Checks if there are any unread notifications for the specified user.
     *
     * @param targetUserId the ID of the user for whom to check for unread
     *                     notifications
     * @return true if there are unread notifications for the user, false otherwise
     */
    boolean existsByTargetUserIdAndViewedIsFalse(Long targetUserId);

    /**
     * Changes {@link Notification} `viewed` as true.
     *
     * @param notificationId to change
     */
    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.viewed = true WHERE n.id = :notificationId")
    void markNotificationAsViewed(Long notificationId);

    /**
     * Changes {@link Notification} `viewed` as false.
     *
     * @param notificationId to change
     */
    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.viewed = false WHERE n.id = :notificationId")
    void markNotificationAsNotViewed(Long notificationId);

    /**
     * Method to find specific Notification.
     *
     * @param targetUserId     User, that should receive Notification
     * @param notificationType type of Notification
     * @param targetId         id of object
     * @return {@link Notification} with specific NotificationType and id of object
     */
    Notification findNotificationByTargetUserIdAndNotificationTypeAndTargetId(Long targetUserId,
        NotificationType notificationType, Long targetId);

    /**
     * Method to find specific not viewed Notification.
     *
     * @param targetUserId     User, that should receive Notification
     * @param notificationType type of Notification
     * @param targetId         id of object
     * @return {@link Notification} with specific NotificationType and id of object
     */
    Optional<Notification> findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(
        Long targetUserId, NotificationType notificationType, Long targetId);

    /**
     * Method to delete specific Notification.
     *
     * @param notificationId id of searched Notification
     * @param targetUserId   id of user
     */
    void deleteNotificationByIdAndTargetUserId(Long notificationId, Long targetUserId);

    /**
     * Counts the number of unread notifications for a specific user. This method
     * retrieves the count of notifications that have `viewed` set to `false` for
     * the user identified by the given `userId`.
     *
     * @param userId the ID of the user whose unread notifications are to be counted
     * @return the number of unread notifications for the specified user
     */
    long countByTargetUserIdAndViewedIsFalse(Long userId);

    /**
     * Method to return all unread notifications by specific type.
     *
     * @param notificationType type of notification
     * @return List of unread notification that have specific type
     */
    @Query("SELECT n FROM Notification n "
        + "JOIN FETCH n.targetUser tu "
        + "JOIN FETCH tu.language "
        + "WHERE n.notificationType = :notificationType "
        + "AND n.viewed = false "
        + "AND n.emailSent = false")
    List<Notification> findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(
        NotificationType notificationType);

    /**
     * Method to return count of action user in notification by target user, type,
     * target id and not viewed.
     *
     * @param targetUserId     id of target user
     * @param notificationType type of notification
     * @param targetId         id of object related to notification
     *
     * @return count of action users
     */
    @Query("SELECT COUNT(n) FROM Notification n "
        + "JOIN n.actionUsers u "
        + "WHERE n.targetUser.id = :targetUserId "
        + "AND n.notificationType = :notificationType "
        + "AND n.targetId = :targetId "
        + "AND n.viewed = false")
    long countActionUsersByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(
        Long targetUserId,
        NotificationType notificationType,
        Long targetId);

    /**
     * Method to return count of action user in notification by target user, type,
     * target id and not viewed and email not sent.
     *
     * @param targetUserId     id of target user
     * @param notificationType type of notification
     * @param targetId         id of object related to notification
     *
     * @return count of action users
     */
    @Query("SELECT COUNT(n) FROM Notification n "
        + "JOIN n.actionUsers u "
        + "WHERE n.targetUser.id = :targetUserId "
        + "AND n.notificationType = :notificationType "
        + "AND n.targetId = :targetId "
        + "AND n.viewed = false "
        + "AND n.emailSent = false")
    long countActionUsersByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalseAndEmailSentIsFalse(
        Long targetUserId,
        NotificationType notificationType,
        Long targetId);

    /**
     * Checks if a notification with the specified ID exists for the specified user.
     *
     * @param notificationId the ID of the notification to check
     * @param targetUserId   the ID of the user for whom the notification belongs
     * @return true if the notification with the specified ID exists for the user,
     *         false otherwise
     */
    boolean existsByIdAndTargetUserId(Long notificationId, Long targetUserId);
}
