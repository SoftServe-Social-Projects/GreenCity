package greencity.service;

import greencity.ModelUtils;
import static greencity.ModelUtils.getCategory;
import static greencity.ModelUtils.getEmailNotificationDto;
import static greencity.ModelUtils.getNotification;
import static greencity.ModelUtils.getPlace;
import static greencity.ModelUtils.getSubscriberDto;
import static greencity.ModelUtils.getUser;
import static greencity.ModelUtils.getUserVO;
import greencity.client.RestClient;
import greencity.dto.category.CategoryDto;
import greencity.dto.notification.EmailNotificationDto;
import greencity.dto.place.PlaceNotificationDto;
import greencity.dto.user.SubscriberDto;
import greencity.dto.user.UserVO;
import greencity.entity.Notification;
import greencity.entity.Place;
import greencity.entity.User;
import greencity.enums.EmailPreference;
import greencity.enums.EmailPreferencePeriodicity;
import greencity.enums.NotificationType;
import greencity.message.ScheduledEmailMessage;
import greencity.message.SendReportEmailMessage;
import greencity.repository.NotificationRepo;
import greencity.repository.PlaceRepo;
import greencity.repository.UserNotificationPreferenceRepo;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class NotificationServiceImplTest {

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private PlaceRepo placeRepo;

    @Mock
    private UserService userService;

    @Mock
    private NotificationRepo notificationRepo;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private RestClient restClient;

    @Mock
    private UserNotificationPreferenceRepo userNotificationPreferenceRepo;

    @Test
    void sendDailyReportTest() {
        EmailPreferencePeriodicity periodicity = EmailPreferencePeriodicity.DAILY;
        EmailPreference emailPreference = EmailPreference.PLACES;
        UserVO userVO = getUserVO();

        Place testPlace1 = getPlace();
        testPlace1.setCategory(getCategory());
        testPlace1.setId(1L);

        Place testPlace2 = getPlace();
        testPlace1.setCategory(getCategory());
        testPlace1.setId(2L);

        when(userService.getUsersIdByEmailPreferenceAndEmailPeriodicity(emailPreference, periodicity))
            .thenReturn(Collections.singletonList(userVO));
        when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(1L, emailPreference,
            EmailPreferencePeriodicity.TWICE_A_DAY))
            .thenReturn(true);
        when(modelMapper.map(userVO, SubscriberDto.class))
            .thenReturn(getSubscriberDto());
        when(placeRepo.findAllByModifiedDateBetweenAndStatus(any(ZonedDateTime.class), any(ZonedDateTime.class), any()))
            .thenReturn(List.of(testPlace1, testPlace2));
        when(modelMapper.map(testPlace1, PlaceNotificationDto.class))
            .thenReturn(new PlaceNotificationDto("name", new CategoryDto("category", "test", null)));
        when(modelMapper.map(testPlace2, PlaceNotificationDto.class))
            .thenReturn(new PlaceNotificationDto("name1", new CategoryDto("category1", "test", null)));
        when(modelMapper.map(testPlace1.getCategory(), CategoryDto.class))
            .thenReturn(new CategoryDto("category", "test", null));
        when(modelMapper.map(testPlace2.getCategory(), CategoryDto.class))
            .thenReturn(new CategoryDto("category1", "test", null));

        notificationService.sendDailyReport();

        verify(restClient).sendReport(any(SendReportEmailMessage.class));
    }

    @Test
    void sendWeeklyReportTest() {
        EmailPreferencePeriodicity periodicity = EmailPreferencePeriodicity.WEEKLY;
        EmailPreference emailPreference = EmailPreference.PLACES;
        UserVO userVO = getUserVO();

        Place testPlace1 = getPlace();
        testPlace1.setCategory(getCategory());
        testPlace1.setId(1L);

        Place testPlace2 = getPlace();
        testPlace1.setCategory(getCategory());
        testPlace1.setId(2L);

        when(userService.getUsersIdByEmailPreferenceAndEmailPeriodicity(emailPreference, periodicity))
            .thenReturn(Collections.singletonList(userVO));
        when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(1L, emailPreference,
            EmailPreferencePeriodicity.TWICE_A_DAY))
            .thenReturn(true);
        when(modelMapper.map(userVO, SubscriberDto.class))
            .thenReturn(getSubscriberDto());
        when(placeRepo.findAllByModifiedDateBetweenAndStatus(any(ZonedDateTime.class), any(ZonedDateTime.class), any()))
            .thenReturn(List.of(testPlace1, testPlace2));
        when(modelMapper.map(testPlace1, PlaceNotificationDto.class))
            .thenReturn(new PlaceNotificationDto("name", new CategoryDto("category", "test", null)));
        when(modelMapper.map(testPlace2, PlaceNotificationDto.class))
            .thenReturn(new PlaceNotificationDto("name1", new CategoryDto("category1", "test", null)));
        when(modelMapper.map(testPlace1.getCategory(), CategoryDto.class))
            .thenReturn(new CategoryDto("category", "test", null));
        when(modelMapper.map(testPlace2.getCategory(), CategoryDto.class))
            .thenReturn(new CategoryDto("category1", "test", null));

        notificationService.sendWeeklyReport();

        verify(restClient).sendReport(any(SendReportEmailMessage.class));
    }

    @Test
    void sendMonthlyReportTest() {
        EmailPreferencePeriodicity periodicity = EmailPreferencePeriodicity.MONTHLY;
        EmailPreference emailPreference = EmailPreference.PLACES;
        UserVO userVO = getUserVO();

        Place testPlace1 = getPlace();
        testPlace1.setCategory(getCategory());
        testPlace1.setId(1L);

        Place testPlace2 = getPlace();
        testPlace1.setCategory(getCategory());
        testPlace1.setId(2L);

        when(userService.getUsersIdByEmailPreferenceAndEmailPeriodicity(emailPreference, periodicity))
            .thenReturn(Collections.singletonList(userVO));
        when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(1L, emailPreference,
            EmailPreferencePeriodicity.TWICE_A_DAY))
            .thenReturn(true);
        when(modelMapper.map(userVO, SubscriberDto.class))
            .thenReturn(getSubscriberDto());
        when(placeRepo.findAllByModifiedDateBetweenAndStatus(any(ZonedDateTime.class), any(ZonedDateTime.class), any()))
            .thenReturn(List.of(testPlace1, testPlace2));
        when(modelMapper.map(testPlace1, PlaceNotificationDto.class))
            .thenReturn(new PlaceNotificationDto("name", new CategoryDto("category", "test", null)));
        when(modelMapper.map(testPlace2, PlaceNotificationDto.class))
            .thenReturn(new PlaceNotificationDto("name1", new CategoryDto("category1", "test", null)));
        when(modelMapper.map(testPlace1.getCategory(), CategoryDto.class))
            .thenReturn(new CategoryDto("category", "test", null));
        when(modelMapper.map(testPlace2.getCategory(), CategoryDto.class))
            .thenReturn(new CategoryDto("category1", "test", null));

        notificationService.sendMonthlyReport();

        verify(restClient).sendReport(any(SendReportEmailMessage.class));
    }

    @Test
    void sendEmailNotificationSystemToOneUserTest() {
        EmailNotificationDto notificationDto = getEmailNotificationDto();
        Notification notification = getNotification();
        notification.setTargetUser(getUser());
        when(modelMapper.map(notificationDto, Notification.class)).thenReturn(notification);
        when(userService.findById(anyLong())).thenReturn(getUserVO());
        notificationService.sendEmailNotification(notificationDto);
        ArgumentCaptor<ScheduledEmailMessage> captor = ArgumentCaptor.forClass(ScheduledEmailMessage.class);
        await().atMost(5, SECONDS)
            .untilAsserted(() -> verify(restClient).sendEmailNotificationSystem(captor.capture()));
        ScheduledEmailMessage capturedMessage = captor.getValue();
        assertEquals(notificationDto.getTargetUser().getEmail(), capturedMessage.getEmail());
        assertEquals(notificationDto.getTargetUser().getName(), capturedMessage.getUsername());
    }

    @Test
    void sendEmailNotificationInvitesToOneUserTest() {
        EmailNotificationDto notificationDto = getEmailNotificationDto();
        Notification notification = getNotification();
        User user = getUser();
        UserVO userVO = getUserVO();
        notification.setTargetUser(user);
        notification.setNotificationType(NotificationType.HABIT_INVITE);
        when(modelMapper.map(notificationDto, Notification.class)).thenReturn(notification);
        when(userService.findById(anyLong())).thenReturn(userVO);
        notificationService.sendEmailNotification(notificationDto);
        ArgumentCaptor<ScheduledEmailMessage> captor = ArgumentCaptor.forClass(ScheduledEmailMessage.class);
        await().atMost(5, SECONDS)
            .untilAsserted(() -> verify(restClient).sendEmailNotificationInvites(captor.capture()));
        ScheduledEmailMessage capturedMessage = captor.getValue();
        assertEquals(notificationDto.getTargetUser().getEmail(), capturedMessage.getEmail());
        assertEquals(notificationDto.getTargetUser().getName(), capturedMessage.getUsername());
    }

    @Test
    void sendEmailNotificationCommentsToOneUserTest() {
        EmailNotificationDto notificationDto = getEmailNotificationDto();
        Notification notification = getNotification();
        User user = getUser();
        notification.setTargetUser(user);
        notification.setNotificationType(NotificationType.ECONEWS_COMMENT);
        when(modelMapper.map(notificationDto, Notification.class)).thenReturn(notification);
        when(userService.findById(anyLong())).thenReturn(getUserVO());
        notificationService.sendEmailNotification(notificationDto);
        ArgumentCaptor<ScheduledEmailMessage> captor = ArgumentCaptor.forClass(ScheduledEmailMessage.class);
        await().atMost(5, SECONDS)
            .untilAsserted(() -> verify(restClient).sendEmailNotificationComments(captor.capture()));
        ScheduledEmailMessage capturedMessage = captor.getValue();
        assertEquals(notificationDto.getTargetUser().getEmail(), capturedMessage.getEmail());
        assertEquals(notificationDto.getTargetUser().getName(), capturedMessage.getUsername());
    }

    @Test
    void sendEmailNotificationLikesToOneUserTest() {
        EmailNotificationDto notificationDto = getEmailNotificationDto();
        Notification notification = getNotification();
        notification.setTargetUser(getUser());
        notification.setNotificationType(NotificationType.ECONEWS_LIKE);
        when(modelMapper.map(notificationDto, Notification.class)).thenReturn(notification);
        when(userService.findById(anyLong())).thenReturn(getUserVO());
        notificationService.sendEmailNotification(notificationDto);
        ArgumentCaptor<ScheduledEmailMessage> captor = ArgumentCaptor.forClass(ScheduledEmailMessage.class);
        await().atMost(5, SECONDS)
            .untilAsserted(() -> verify(restClient).sendEmailNotificationLikes(captor.capture()));
        ScheduledEmailMessage capturedMessage = captor.getValue();
        assertEquals(notificationDto.getTargetUser().getEmail(), capturedMessage.getEmail());
        assertEquals(notificationDto.getTargetUser().getName(), capturedMessage.getUsername());
    }

    @Test
    void sendFriendRequestScheduledEmail() {
        Notification notification = getNotification();
        User targetUser = getUser();
        targetUser.setLanguage(ModelUtils.getLanguage());
        notification.setTargetUser(targetUser);
        LocalDateTime mockDateTime = LocalDateTime.of(2024, 7, 1, 10, 0);
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(() -> LocalDateTime.now(any(ZoneId.class))).thenReturn(mockDateTime);
            when(notificationRepo
                .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(
                    NotificationType.FRIEND_REQUEST_RECEIVED))
                .thenReturn(Collections.singletonList(notification));
            when(notificationRepo
                .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(
                    NotificationType.FRIEND_REQUEST_ACCEPTED))
                .thenReturn(Collections.singletonList(notification));
            when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(targetUser.getId(),
                EmailPreference.INVITES, EmailPreferencePeriodicity.TWICE_A_DAY)).thenReturn(false);
            when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(targetUser.getId(),
                EmailPreference.INVITES, EmailPreferencePeriodicity.DAILY)).thenReturn(false);
            when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(targetUser.getId(),
                EmailPreference.INVITES, EmailPreferencePeriodicity.WEEKLY)).thenReturn(true);
            when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(targetUser.getId(),
                EmailPreference.COMMENTS, EmailPreferencePeriodicity.MONTHLY)).thenReturn(false);

            notificationService.sendFriendRequestScheduledEmail();
            ArgumentCaptor<ScheduledEmailMessage> captor = ArgumentCaptor.forClass(ScheduledEmailMessage.class);
            await().atMost(5, SECONDS)
                .untilAsserted(() -> verify(restClient, times(2)).sendScheduledEmailNotification(captor.capture()));
            List<ScheduledEmailMessage> capturedMessages = captor.getAllValues();
            for (ScheduledEmailMessage capturedMessage : capturedMessages) {
                assertEquals(notification.getTargetUser().getEmail(), capturedMessage.getEmail());
                assertEquals(notification.getTargetUser().getName(), capturedMessage.getUsername());
            }
        }
    }

    @Test
    void sendCommentReplyScheduledEmail() {
        Notification notification = getNotification();
        User targetUser = getUser();
        targetUser.setLanguage(ModelUtils.getLanguage());
        notification.setTargetUser(targetUser);
        LocalDateTime mockDateTime = LocalDateTime.of(2024, 7, 1, 10, 0);
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(() -> LocalDateTime.now(any(ZoneId.class))).thenReturn(mockDateTime);
            when(notificationRepo
                .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(NotificationType.ECONEWS_COMMENT_REPLY))
                .thenReturn(Collections.singletonList(notification));
            when(notificationRepo
                .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(NotificationType.EVENT_COMMENT_REPLY))
                .thenReturn(Collections.singletonList(notification));
            when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(targetUser.getId(),
                EmailPreference.COMMENTS, EmailPreferencePeriodicity.TWICE_A_DAY)).thenReturn(false);
            when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(targetUser.getId(),
                EmailPreference.COMMENTS, EmailPreferencePeriodicity.DAILY)).thenReturn(true);
            when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(targetUser.getId(),
                EmailPreference.COMMENTS, EmailPreferencePeriodicity.WEEKLY)).thenReturn(false);
            when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(targetUser.getId(),
                EmailPreference.COMMENTS, EmailPreferencePeriodicity.MONTHLY)).thenReturn(false);

            notificationService.sendCommentReplyScheduledEmail();
            ArgumentCaptor<ScheduledEmailMessage> captor = ArgumentCaptor.forClass(ScheduledEmailMessage.class);
            await().atMost(5, SECONDS)
                .untilAsserted(() -> verify(restClient, times(2)).sendScheduledEmailNotification(captor.capture()));
            List<ScheduledEmailMessage> capturedMessages = captor.getAllValues();
            for (ScheduledEmailMessage capturedMessage : capturedMessages) {
                assertEquals(notification.getTargetUser().getEmail(), capturedMessage.getEmail());
                assertEquals(notification.getTargetUser().getName(), capturedMessage.getUsername());
            }
        }
    }

    @Test
    void sendCommentScheduledEmail() {
        Notification notification = getNotification();
        User targetUser = getUser();
        notification.setTargetUser(targetUser);
        LocalDateTime mockDateTime = LocalDateTime.of(2024, 7, 12, 10, 0);
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(() -> LocalDateTime.now(any(ZoneId.class))).thenReturn(mockDateTime);
            when(notificationRepo
                .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(NotificationType.ECONEWS_COMMENT))
                .thenReturn(Collections.singletonList(notification));
            when(notificationRepo
                .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(NotificationType.EVENT_COMMENT))
                .thenReturn(Collections.singletonList(notification));
            when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(targetUser.getId(),
                EmailPreference.COMMENTS, EmailPreferencePeriodicity.TWICE_A_DAY)).thenReturn(true);
            when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(targetUser.getId(),
                EmailPreference.COMMENTS, EmailPreferencePeriodicity.DAILY)).thenReturn(false);
            when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(targetUser.getId(),
                EmailPreference.COMMENTS, EmailPreferencePeriodicity.WEEKLY)).thenReturn(false);
            when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(targetUser.getId(),
                EmailPreference.COMMENTS, EmailPreferencePeriodicity.MONTHLY)).thenReturn(false);

            notificationService.sendCommentScheduledEmail();
            ArgumentCaptor<ScheduledEmailMessage> captor = ArgumentCaptor.forClass(ScheduledEmailMessage.class);
            await().atMost(5, SECONDS)
                .untilAsserted(() -> verify(restClient, times(2)).sendScheduledEmailNotification(captor.capture()));
            List<ScheduledEmailMessage> capturedMessages = captor.getAllValues();
            for (ScheduledEmailMessage capturedMessage : capturedMessages) {
                assertEquals(notification.getTargetUser().getEmail(), capturedMessage.getEmail());
                assertEquals(notification.getTargetUser().getName(), capturedMessage.getUsername());
            }
        }
    }

    @Test
    void sendLikeScheduledEmail() {
        Notification notification = getNotification();
        User targetUser = getUser();
        targetUser.setLanguage(ModelUtils.getLanguage());
        notification.setTargetUser(targetUser);
        LocalDateTime mockDateTime = LocalDateTime.of(2024, 8, 1, 18, 0);
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(() -> LocalDateTime.now(any(ZoneId.class))).thenReturn(mockDateTime);
            when(notificationRepo
                .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(NotificationType.ECONEWS_COMMENT_LIKE))
                .thenReturn(Collections.singletonList(notification));
            when(notificationRepo
                .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(NotificationType.ECONEWS_LIKE))
                .thenReturn(Collections.singletonList(notification));
            when(notificationRepo
                .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(NotificationType.EVENT_COMMENT_LIKE))
                .thenReturn(Collections.singletonList(notification));
            when(notificationRepo
                .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(NotificationType.HABIT_LIKE))
                .thenReturn(Collections.singletonList(notification));
            when(notificationRepo
                .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(NotificationType.HABIT_COMMENT_LIKE))
                .thenReturn(Collections.singletonList(notification));
            when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(targetUser.getId(),
                EmailPreference.LIKES, EmailPreferencePeriodicity.TWICE_A_DAY)).thenReturn(false);
            when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(targetUser.getId(),
                EmailPreference.LIKES, EmailPreferencePeriodicity.DAILY)).thenReturn(false);
            when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(targetUser.getId(),
                EmailPreference.LIKES, EmailPreferencePeriodicity.WEEKLY)).thenReturn(false);
            when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(targetUser.getId(),
                EmailPreference.LIKES, EmailPreferencePeriodicity.MONTHLY)).thenReturn(true);

            notificationService.sendLikeScheduledEmail();
            ArgumentCaptor<ScheduledEmailMessage> captor = ArgumentCaptor.forClass(ScheduledEmailMessage.class);
            await().atMost(5, SECONDS)
                .untilAsserted(() -> verify(restClient, times(5)).sendScheduledEmailNotification(captor.capture()));
            List<ScheduledEmailMessage> capturedMessages = captor.getAllValues();
            for (ScheduledEmailMessage capturedMessage : capturedMessages) {
                assertEquals(notification.getTargetUser().getEmail(), capturedMessage.getEmail());
                assertEquals(notification.getTargetUser().getName(), capturedMessage.getUsername());
            }
        }
    }

    @Test
    void sendTaggedInCommentScheduledEmail() {
        Notification notification = getNotification();
        User targetUser = getUser();
        targetUser.setLanguage(ModelUtils.getLanguage());
        notification.setTargetUser(targetUser);
        when(notificationRepo
            .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(NotificationType.EVENT_COMMENT_USER_TAG))
            .thenReturn(Collections.singletonList(notification));
        when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(eq(targetUser.getId()),
            eq(EmailPreference.COMMENTS), any())).thenReturn(true);
        notificationService.sendTaggedInCommentScheduledEmail();
        ArgumentCaptor<ScheduledEmailMessage> captor = ArgumentCaptor.forClass(ScheduledEmailMessage.class);
        await().atMost(5, SECONDS)
            .untilAsserted(() -> verify(restClient, times(1)).sendScheduledEmailNotification(captor.capture()));
        List<ScheduledEmailMessage> capturedMessages = captor.getAllValues();
        for (ScheduledEmailMessage capturedMessage : capturedMessages) {
            assertEquals(notification.getTargetUser().getEmail(), capturedMessage.getEmail());
            assertEquals(notification.getTargetUser().getName(), capturedMessage.getUsername());
        }
    }

    @Test
    void sendHabitInviteScheduledEmail() {
        Notification notification = getNotification();
        User targetUser = getUser();
        targetUser.setLanguage(ModelUtils.getLanguage());
        notification.setTargetUser(targetUser);
        when(notificationRepo
            .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(NotificationType.HABIT_INVITE))
            .thenReturn(Collections.singletonList(notification));
        when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(eq(targetUser.getId()),
            eq(EmailPreference.INVITES), any())).thenReturn(true);
        notificationService.sendHabitInviteScheduledEmail();
        ArgumentCaptor<ScheduledEmailMessage> captor = ArgumentCaptor.forClass(ScheduledEmailMessage.class);
        await().atMost(5, SECONDS)
            .untilAsserted(() -> verify(restClient, times(1)).sendScheduledEmailNotification(captor.capture()));
        List<ScheduledEmailMessage> capturedMessages = captor.getAllValues();
        for (ScheduledEmailMessage capturedMessage : capturedMessages) {
            assertEquals(notification.getTargetUser().getEmail(), capturedMessage.getEmail());
            assertEquals(notification.getTargetUser().getName(), capturedMessage.getUsername());
        }
    }

    @Test
    void sendSystemNotificationsScheduledEmail() {
        Notification notification = getNotification();
        User targetUser = getUser();
        targetUser.setLanguage(ModelUtils.getLanguage());
        notification.setTargetUser(targetUser);
        when(notificationRepo
            .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(NotificationType.ECONEWS_CREATED))
            .thenReturn(Collections.singletonList(notification));
        when(notificationRepo
            .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(NotificationType.EVENT_CREATED))
            .thenReturn(Collections.singletonList(notification));
        when(notificationRepo
            .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(NotificationType.EVENT_CANCELED))
            .thenReturn(Collections.singletonList(notification));
        when(notificationRepo
            .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(NotificationType.EVENT_NAME_UPDATED))
            .thenReturn(Collections.singletonList(notification));
        when(notificationRepo
            .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(NotificationType.EVENT_UPDATED))
            .thenReturn(Collections.singletonList(notification));
        when(notificationRepo
            .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(NotificationType.EVENT_JOINED))
            .thenReturn(Collections.singletonList(notification));
        when(notificationRepo
            .findAllByNotificationByTypeAndViewedIsFalseAndEmailSentIsFalse(
                NotificationType.HABIT_LAST_DAY_OF_PRIMARY_DURATION))
            .thenReturn(Collections.singletonList(notification));
        when(userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(eq(targetUser.getId()),
            eq(EmailPreference.SYSTEM), any())).thenReturn(true);
        notificationService.sendSystemNotificationsScheduledEmail();
        ArgumentCaptor<ScheduledEmailMessage> captor = ArgumentCaptor.forClass(ScheduledEmailMessage.class);
        await().atMost(5, SECONDS)
            .untilAsserted(() -> verify(restClient, times(7)).sendScheduledEmailNotification(captor.capture()));
        List<ScheduledEmailMessage> capturedMessages = captor.getAllValues();
        for (ScheduledEmailMessage capturedMessage : capturedMessages) {
            assertEquals(notification.getTargetUser().getEmail(), capturedMessage.getEmail());
            assertEquals(notification.getTargetUser().getName(), capturedMessage.getUsername());
        }
    }
}