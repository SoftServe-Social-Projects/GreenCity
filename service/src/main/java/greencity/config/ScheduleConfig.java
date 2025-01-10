package greencity.config;

import greencity.client.RestClient;
import greencity.dto.user.UserVO;
import greencity.entity.HabitAssign;
import greencity.entity.User;
import greencity.enums.HabitAssignStatus;
import greencity.message.SendHabitNotification;
import greencity.repository.HabitAssignRepo;
import greencity.repository.RatingStatisticsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import static greencity.enums.EmailNotification.*;

/**
 * Config for scheduling.
 *
 * @author Nazar Stasyuk
 * @version 1.0
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduleConfig {
    private final HabitAssignRepo habitAssignRepo;
    private final RatingStatisticsRepo ratingStatisticsRepo;
    private final RestClient restClient;

    /**
     * Invoke {@link SendHabitNotification} from EmailMessageReceiver to send email
     * letters to each user that hasn't marked any habit during last 3 days.
     *
     * @param users list of potential {@link User} to send notifications.
     */
    private void sendHabitNotificationIfNeed(List<UserVO> users) {
        ZonedDateTime end = ZonedDateTime.now();
        ZonedDateTime start = end.minusDays(3);
        for (UserVO user : users) {
            int count = habitAssignRepo.countMarkedHabitAssignsByUserIdAndPeriod(user.getId(), start, end);
            if (count == 0) {
                restClient.sendHabitNotification(new SendHabitNotification(user.getName(), user.getEmail()));
            }
        }
    }

    /**
     * Every day at 19:00 sends notifications about not marked habits to users with
     * field {@link greencity.enums.EmailNotification} equal to IMMEDIATELY or
     * DAILY.
     */
    @Scheduled(cron = "0 0 19 * * ?", zone = "Europe/Kiev")
    void sendHabitNotificationEveryDay() {
        List<UserVO> users = restClient.findAllByEmailNotification(IMMEDIATELY);
        users.addAll(restClient.findAllByEmailNotification(DAILY));
        sendHabitNotificationIfNeed(users);
        log.info("Notification sent at {} about not marked habits to {} users", LocalDateTime.now(), users.size());
    }

    /**
     * Every friday at 19:00 sends notifications about not marked habits to users
     * with field {@link greencity.enums.EmailNotification} equal to WEEKLY.
     */
    @Scheduled(cron = "0 0 19 * * FRI", zone = "Europe/Kiev")
    void sendHabitNotificationEveryWeek() {
        List<UserVO> users = restClient.findAllByEmailNotification(WEEKLY);
        sendHabitNotificationIfNeed(users);
        log.info("Notification sent at {} about not marked habits to {} users", LocalDateTime.now(), users.size());
    }

    /**
     * On th 25th of every month at 19:00 sends notifications about not marked
     * habits to users with field {@link greencity.enums.EmailNotification} equal to
     * MONTHLY.
     */
    @Scheduled(cron = "0 0 19 25 * ?", zone = "Europe/Kiev")
    void sendHabitNotificationEveryMonth() {
        List<UserVO> users = restClient.findAllByEmailNotification(MONTHLY);
        sendHabitNotificationIfNeed(users);
        log.info("Habit notifications has been sent to {} users", users.size());
    }

    /**
     * Every day at 00:00 deletes from the table rating_statistics records witch are
     * older than period in application properties.
     *
     * @author Dovganyuk Taras
     **/
    @Scheduled(cron = "0 0 0 * * ?", zone = "Europe/Kiev")
    @Transactional
    public void scheduledDeleteRatingStatisticsOlderThan() {
        ratingStatisticsRepo.scheduledDeleteOlderThan();
    }

    /**
     * Every day at 00:00 checks all Assigned Habits and if they are timed out set
     * status EXPIRED.
     *
     * @author Ostap Mykhaylivskii
     **/
    @Transactional
    @Scheduled(cron = "0 0 0 * * ?", zone = "Europe/Kiev")
    public void setExpiredStatus() {
        ZonedDateTime now = ZonedDateTime.now();
        List<HabitAssign> habitsInProgress = habitAssignRepo.findAllInProgressHabitAssigns();
        habitsInProgress.forEach(h -> {
            if (h.getCreateDate().plusDays(h.getDuration().longValue()).isBefore(now)) {
                log.info("Habit status is expired from {}. Count of habits {}", now, habitsInProgress.size());
                h.setStatus(HabitAssignStatus.EXPIRED);
            }
        });
    }
}
