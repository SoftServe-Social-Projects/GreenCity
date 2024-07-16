package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.notification.NotificationDto;
import greencity.entity.Notification;
import greencity.entity.User;
import greencity.enums.NotificationType;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.NotificationRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.List;
import java.util.Optional;
import static greencity.ModelUtils.TEST_USER;
import static greencity.ModelUtils.TEST_USER_VO;
import static greencity.ModelUtils.getActionDto;
import static greencity.ModelUtils.getFilterNotificationDto;
import static greencity.ModelUtils.getNotification;
import static greencity.ModelUtils.getNotificationDto;
import static greencity.ModelUtils.getNotificationWithSeveralActionUsers;
import static greencity.ModelUtils.getPageableAdvancedDtoForNotificationDto;
import static greencity.ModelUtils.getPrincipal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UserNotificationServiceImplTest {
    @InjectMocks
    UserNotificationServiceImpl userNotificationService;
    @Mock
    private NotificationRepo notificationRepo;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private UserService userService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    private static String TOPIC = "/topic/";
    private static String NOTIFICATION = "/notification";

    @Test
    void getThreeLastNotificationsTest() {
        var notification = getNotification();
        var notificationDto = getNotificationDto();

        when(userService.findByEmail("danylo@gmail.com")).thenReturn(TEST_USER_VO);
        when(modelMapper.map(TEST_USER_VO, User.class)).thenReturn(TEST_USER);
        when(notificationRepo.findTop3ByTargetUserIdAndViewedFalseOrderByTimeDesc(TEST_USER.getId()))
            .thenReturn(List.of(notification));
        when(modelMapper.map(notification, NotificationDto.class)).thenReturn(notificationDto);

        List<NotificationDto> dtos = userNotificationService.getThreeLastNotifications(getPrincipal(), "en");

        assertEquals(List.of(notificationDto), dtos);

        verify(userService).findByEmail("danylo@gmail.com");
        verify(modelMapper).map(TEST_USER_VO, User.class);
        verify(notificationRepo).findTop3ByTargetUserIdAndViewedFalseOrderByTimeDesc(TEST_USER.getId());
        verify(modelMapper).map(notification, NotificationDto.class);
    }

    @Test
    void getNotificationsFilteredTest() {
        var notification = getNotification();
        var notificationDto = getNotificationDto();
        var filterNotificationDto = getFilterNotificationDto();

        var page = PageRequest.of(0, 1);
        var notificationPage = new PageImpl<>(List.of(notification), page, 0);

        var actual = getPageableAdvancedDtoForNotificationDto();

        when(userService.findByEmail("danylo@gmail.com")).thenReturn(TEST_USER_VO);
        when(modelMapper.map(TEST_USER_VO, User.class)).thenReturn(TEST_USER);

        when(notificationRepo.findByTargetUserIdAndProjectNameInAndNotificationTypeInOrderByTimeDesc(
            TEST_USER.getId(), filterNotificationDto.getProjectName(),
            filterNotificationDto.getNotificationType(), page))
            .thenReturn(notificationPage);
        when(modelMapper.map(notification, NotificationDto.class)).thenReturn(notificationDto);

        PageableAdvancedDto<NotificationDto> expected = userNotificationService
            .getNotificationsFiltered(page, getPrincipal(), filterNotificationDto, "en");

        assertEquals(expected, actual);

        verify(userService).findByEmail("danylo@gmail.com");
        verify(modelMapper).map(TEST_USER_VO, User.class);
        verify(notificationRepo).findByTargetUserIdAndProjectNameInAndNotificationTypeInOrderByTimeDesc(
            TEST_USER.getId(), filterNotificationDto.getProjectName(),
            filterNotificationDto.getNotificationType(), page);
        verify(modelMapper).map(notification, NotificationDto.class);
    }

    @Test
    void getNotificationsTest() {
        var notification = getNotification();
        var notificationDto = getNotificationDto();

        var page = PageRequest.of(0, 1);
        var notificationPage = new PageImpl<>(List.of(notification), page, 0);

        var actual = getPageableAdvancedDtoForNotificationDto();

        when(userService.findByEmail("danylo@gmail.com")).thenReturn(TEST_USER_VO);
        when(modelMapper.map(TEST_USER_VO, User.class)).thenReturn(TEST_USER);
        when(notificationRepo.findByTargetUserId(TEST_USER.getId(), page)).thenReturn(notificationPage);
        when(modelMapper.map(notification, NotificationDto.class)).thenReturn(notificationDto);

        PageableAdvancedDto<NotificationDto> expected = userNotificationService
            .getNotifications(page, getPrincipal(), "en");

        assertEquals(expected, actual);

        verify(userService).findByEmail("danylo@gmail.com");
        verify(modelMapper).map(TEST_USER_VO, User.class);
        verify(notificationRepo).findByTargetUserId(TEST_USER.getId(), page);
        verify(modelMapper).map(notification, NotificationDto.class);
    }

    @Test
    void getNotificationTest() {
        var notification = getNotification();
        var notificationDto = getNotificationDto();

        when(userService.findByEmail("danylo@gmail.com")).thenReturn(TEST_USER_VO);
        when(modelMapper.map(TEST_USER_VO, User.class)).thenReturn(TEST_USER);
        when(notificationRepo.findByIdAndTargetUserId(1L, TEST_USER.getId())).thenReturn(notification);
        when(modelMapper.map(notification, NotificationDto.class)).thenReturn(notificationDto);

        NotificationDto expected = userNotificationService.getNotification(getPrincipal(), 1L, "en");

        assertEquals(expected, notificationDto);

        verify(userService).findByEmail("danylo@gmail.com");
        verify(modelMapper).map(TEST_USER_VO, User.class);
        verify(notificationRepo).findByIdAndTargetUserId(1L, TEST_USER.getId());
        verify(modelMapper).map(notification, NotificationDto.class);
    }

    @Test
    void getNotificationThrowNotFoundExceptionTest() {
        var principal = getPrincipal();

        when(userService.findByEmail("danylo@gmail.com")).thenReturn(TEST_USER_VO);
        when(modelMapper.map(TEST_USER_VO, User.class)).thenReturn(TEST_USER);
        when(notificationRepo.findByIdAndTargetUserId(1L, TEST_USER.getId())).thenReturn(null);

        assertThrows(NotFoundException.class, () -> userNotificationService
            .getNotification(principal, 1L, "en"));

        verify(userService).findByEmail("danylo@gmail.com");
        verify(modelMapper).map(TEST_USER_VO, User.class);
        verify(notificationRepo).findByIdAndTargetUserId(1L, TEST_USER.getId());
    }

    @Test
    void notificationSocketTest() {
        var dto = getActionDto();

        when(notificationRepo.existsByTargetUserIdAndViewedIsFalse(dto.getUserId()))
            .thenReturn(true);
        userNotificationService.notificationSocket(dto);

        verify(messagingTemplate).convertAndSend(TOPIC + dto.getUserId() + NOTIFICATION, true);
        verify(notificationRepo).existsByTargetUserIdAndViewedIsFalse(dto.getUserId());
    }

    @Test
    void createNotificationForAttendersTest() {
        when(modelMapper.map(TEST_USER_VO, User.class)).thenReturn(TEST_USER);
        userNotificationService.createNotificationForAttenders(List.of(TEST_USER_VO), "",
            NotificationType.EVENT_CREATED, 1L);
        verify(modelMapper).map(TEST_USER_VO, User.class);
        verify(messagingTemplate,times(1))
            .convertAndSend(TOPIC + TEST_USER.getId() + NOTIFICATION,true);
    }

    @Test
    void createNotificationForAttendersWithTitleTest() {
        when(modelMapper.map(TEST_USER_VO, User.class)).thenReturn(TEST_USER);
        userNotificationService.createNotificationForAttenders(List.of(TEST_USER_VO), "",
            NotificationType.EVENT_CREATED, 1L, "Title");
        verify(modelMapper).map(TEST_USER_VO, User.class);
        verify(messagingTemplate, times(1))
            .convertAndSend(TOPIC + TEST_USER.getId() + NOTIFICATION,true);
    }

    @Test
    void createNotificationTest() {
        when(modelMapper.map(TEST_USER_VO, User.class)).thenReturn(TEST_USER);
        userNotificationService.createNotification(TEST_USER_VO, TEST_USER_VO, NotificationType.EVENT_CREATED);
        verify(modelMapper, times(2)).map(TEST_USER_VO, User.class);
        verify(messagingTemplate, times(1))
            .convertAndSend(TOPIC + TEST_USER.getId() + NOTIFICATION,true);
    }

    @Test
    void createNotificationWithCustomMessageTest() {
        when(notificationRepo
            .findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(1L,
                NotificationType.EVENT_CREATED, 1L)).thenReturn(Optional.empty());
        when(modelMapper.map(TEST_USER_VO, User.class)).thenReturn(TEST_USER);
        userNotificationService.createNotification(TEST_USER_VO, TEST_USER_VO,
            NotificationType.EVENT_CREATED, 1L, "Message");

        verify(notificationRepo)
            .findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(1L,
                NotificationType.EVENT_CREATED, 1L);
        verify(modelMapper, times(2)).map(TEST_USER_VO, User.class);
        verify(messagingTemplate, times(1))
            .convertAndSend(TOPIC + TEST_USER.getId() + NOTIFICATION,true);
    }

    @Test
    void createNotificationWithSecondMessageTest() {
        when(notificationRepo
            .findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(1L,
                NotificationType.EVENT_CREATED, 1L)).thenReturn(Optional.empty());
        when(modelMapper.map(TEST_USER_VO, User.class)).thenReturn(TEST_USER);
        userNotificationService.createNotification(TEST_USER_VO, TEST_USER_VO,
            NotificationType.EVENT_CREATED, 1L, "Message", 1L,
            "Second Message");

        verify(notificationRepo)
            .findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(1L,
                NotificationType.EVENT_CREATED, 1L);
        verify(modelMapper, times(2)).map(TEST_USER_VO, User.class);
        verify(messagingTemplate, times(1))
            .convertAndSend(TOPIC + TEST_USER.getId() + NOTIFICATION,true);
    }

    @Test
    void createNewNotificationTest() {
        when(modelMapper.map(TEST_USER_VO, User.class)).thenReturn(TEST_USER);
        userNotificationService.createNewNotification(TEST_USER_VO, NotificationType.EVENT_CREATED,
            1L, "Custom Message");
        verify(modelMapper).map(TEST_USER_VO, User.class);
        verify(messagingTemplate, times(1))
            .convertAndSend(TOPIC + TEST_USER.getId() + NOTIFICATION,true);
    }

    @Test
    void removeActionUserFromNotificationTest() {
        var notification = getNotification();
        when(notificationRepo
            .findNotificationByTargetUserIdAndNotificationTypeAndTargetId(TEST_USER.getId(),
                NotificationType.EVENT_CREATED, 1L))
            .thenReturn(notification);
        userNotificationService
            .removeActionUserFromNotification(TEST_USER_VO, TEST_USER_VO, 1L, NotificationType.EVENT_CREATED);
        verify(notificationRepo).findNotificationByTargetUserIdAndNotificationTypeAndTargetId(TEST_USER.getId(),
            NotificationType.EVENT_CREATED, 1L);
    }

    @Test
    void removeActionUserFromNotificationWithSeveralActionUsersTest() {
        var notification = getNotificationWithSeveralActionUsers(3);
        when(notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetId(TEST_USER.getId(),
            NotificationType.EVENT_CREATED, 1L))
            .thenReturn(notification);
        when(modelMapper.map(TEST_USER_VO, User.class)).thenReturn(TEST_USER);
        userNotificationService
            .removeActionUserFromNotification(TEST_USER_VO, TEST_USER_VO, 1L, NotificationType.EVENT_CREATED);

        verify(notificationRepo).findNotificationByTargetUserIdAndNotificationTypeAndTargetId(TEST_USER.getId(),
            NotificationType.EVENT_CREATED, 1L);
        verify(modelMapper).map(TEST_USER_VO, User.class);

    }

    @Test
    void removeActionUserFromNotificationIfNotificationIsNullTest() {
        when(notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetId(TEST_USER.getId(),
            NotificationType.EVENT_CREATED, 1L)).thenReturn(null);
        userNotificationService.removeActionUserFromNotification(TEST_USER_VO, TEST_USER_VO, 1L,
            NotificationType.EVENT_CREATED);

        verify(notificationRepo).findNotificationByTargetUserIdAndNotificationTypeAndTargetId(TEST_USER.getId(),
            NotificationType.EVENT_CREATED, 1L);
    }

    @Test
    void deleteNotificationTest() {
        when(userService.findByEmail("danylo@gmail.com")).thenReturn(TEST_USER_VO);
        when(modelMapper.map(TEST_USER_VO, User.class)).thenReturn(TEST_USER);

        userNotificationService.deleteNotification(getPrincipal(), 1L);

        verify(userService).findByEmail("danylo@gmail.com");
        verify(modelMapper).map(TEST_USER_VO, User.class);
    }

    @Test
    void unreadNotificationTest() {
        Long notificationId = 1L;
        Long userId = 1L;
        Notification notification = mock(Notification.class);
        User user = mock(User.class);

        when(notificationRepo.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notification.getTargetUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(notificationRepo.countByTargetUserIdAndViewedIsFalse(userId)).thenReturn(0L);

        userNotificationService.unreadNotification(notificationId);

        verify(notificationRepo).findById(notificationId);
        verify(notificationRepo).countByTargetUserIdAndViewedIsFalse(userId);
        verify(messagingTemplate).convertAndSend(TOPIC + userId + NOTIFICATION, true);
        verify(notificationRepo).markNotificationAsNotViewed(notificationId);
    }

    @Test
    void viewNotificationTest() {
        Long notificationId = 1L;
        Long userId = 1L;
        Notification notification = mock(Notification.class);
        User user = mock(User.class);

        when(notificationRepo.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notification.getTargetUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(notificationRepo.countByTargetUserIdAndViewedIsFalse(userId)).thenReturn(1L);

        userNotificationService.viewNotification(notificationId);

        verify(notificationRepo).findById(notificationId);
        verify(notificationRepo).countByTargetUserIdAndViewedIsFalse(userId);
        verify(messagingTemplate).convertAndSend(TOPIC + userId + NOTIFICATION, false);
        verify(notificationRepo).markNotificationAsViewed(notificationId);
    }
}
