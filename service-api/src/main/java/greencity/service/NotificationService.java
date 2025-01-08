package greencity.service;

import greencity.dto.notification.EmailNotificationDto;

public interface NotificationService {
    /**
     * Method for sending report about new places at 12:00:00pm every day to
     * {@code User}'s who subscribed and has {@code EmailNotification} type - DAILY.
     */
    void sendDailyReport();

    /**
     * Method for sending report about new places at 12:00:00pm, on every Monday,
     * every month to {@code User}'s who subscribed and has
     * {@code EmailNotification} type - WEEKLY.
     */
    void sendWeeklyReport();

    /**
     * Method for sending report about new places at 12:00:00pm, on the 1st day,
     * every month to {@code User}'s who subscribed and has
     * {@code EmailNotification} type - MONTHLY.
     */
    void sendMonthlyReport();

    /**
     * Method for sending scheduled email to user has unread notifications connected
     * with likes. Sending is performed 2 times a day.
     */
    void sendLikeScheduledEmail();

    /**
     * Method for sending scheduled email to user has unread notifications connected
     * with comments. Sending is performed 2 times a day.
     */
    void sendCommentScheduledEmail();

    /**
     * Method for sending scheduled email to user has unread notifications connected
     * with comment replies. Sending is performed 2 times a day.
     */
    void sendCommentReplyScheduledEmail();

    /**
     * Method for sending scheduled email to user has unread notifications connected
     * with friend requests. Sending is performed 2 times a day.
     */
    void sendFriendRequestScheduledEmail();

    /**
     * Method for sending scheduled email to user has unread notifications connected
     * with tagging in the comment. Sending is performed 2 times a day.
     */
    void sendTaggedInCommentScheduledEmail();

    /**
     * Method for sending scheduled email to user has unread notifications connected
     * with habit assign invites. Sending is performed 2 times a day.
     */
    void sendHabitInviteScheduledEmail();

    /**
     * Method for sending scheduled email to user has unread notifications connected
     * with system notifications. Sending is performed 2 times a day.
     */
    void sendSystemNotificationsScheduledEmail();

    /**
     * Method for sending an email notification to one User. Method retrieve
     * notification type and send email to user immediately.
     *
     * @param notification {@link EmailNotificationDto}.
     */
    void sendEmailNotification(EmailNotificationDto notification);
}
