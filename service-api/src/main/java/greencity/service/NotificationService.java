package greencity.service;

import greencity.constant.AppConstant;
import greencity.constant.LogMessage;
import greencity.dto.place.PlaceVO;
import greencity.enums.NotificationType;
import greencity.message.GeneralEmailMessage;
import greencity.message.HabitAssignNotificationMessage;
import greencity.message.UserReceivedCommentMessage;
import greencity.message.UserReceivedCommentReplyMessage;
import greencity.message.UserTaggedInCommentMessage;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.Set;

public interface NotificationService {
    /**
     * Method for sending report about new places immediately to {@code User}'s who
     * subscribed and has {@code EmailNotification} type - IMMEDIATELY.
     *
     * @param newPlace - new {@code Place} which was added on the map
     */
    void sendImmediatelyReport(PlaceVO newPlace);

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
     * Method for sending scheduled email to user has unread notifications
     * connected with likes. Sending is performed 2 times a day.
     */
    void sendLikeScheduledEmail();

    /**
     * Method for sending scheduled email to user has unread notifications
     * connected with comments. Sending is performed 2 times a day.
     */
    void sendCommentScheduledEmail();

    /**
     * Method for sending scheduled email to user has unread notifications
     * connected with comment replies. Sending is performed 2 times a day.
     */
    void sendCommentReplyScheduledEmail();

    /**
     * Method for sending scheduled email to user has unread notifications
     * connected with friend requests. Sending is performed 2 times a day.
     */
    void sendFriendRequestScheduledEmail();

    /**
     * method sends a general email notification to many Users.
     *
     * @param usersEmails {@link Set} to this users email will be sent.
     * @param subject     subject of email message.
     * @param message     text of email message.
     * @author Yurii Midianyi
     */
    void sendEmailNotification(Set<String> usersEmails, String subject, String message);

    /**
     * method sends a general email notification to one User.
     *
     * @param generalEmailMessage {@link GeneralEmailMessage}.
     * @author Yurii Midianyi
     */
    void sendEmailNotification(GeneralEmailMessage generalEmailMessage);

    /**
     * Method send a habit notification message to user.
     *
     * @param message {@link HabitAssignNotificationMessage}.
     */
    void sendHabitAssignEmailNotification(HabitAssignNotificationMessage message);

    /**
     * Method send a notification message when user is mentioned in comment.
     *
     * @param message {@link UserTaggedInCommentMessage}.
     */
    void sendUsersTaggedInCommentEmailNotification(UserTaggedInCommentMessage message);
}
