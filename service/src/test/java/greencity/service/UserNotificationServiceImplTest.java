package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.achievement.ActionDto;
import greencity.dto.language.LanguageVO;
import greencity.dto.notification.EmailNotificationDto;
import greencity.dto.notification.LikeNotificationDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.user.UserVO;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.Notification;
import greencity.entity.User;
import greencity.enums.HabitAssignStatus;
import greencity.enums.NotificationType;
import greencity.enums.ProjectName;
import greencity.repository.HabitAssignRepo;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.NotificationRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.lang.reflect.Method;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static greencity.ModelUtils.getActionDto;
import static greencity.ModelUtils.getHabit;
import static greencity.ModelUtils.getHabitAssign;
import static greencity.ModelUtils.getHabitTranslation;
import static greencity.ModelUtils.getLanguage;
import static greencity.ModelUtils.getLanguageVO;
import static greencity.ModelUtils.getNotification;
import static greencity.ModelUtils.getNotificationDto;
import static greencity.ModelUtils.getNotificationWithSeveralActionUsers;
import static greencity.ModelUtils.getPageableAdvancedDtoForNotificationDto;
import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUser;
import static greencity.ModelUtils.getUserVO;
import static greencity.ModelUtils.testUser;
import static greencity.ModelUtils.testUserVo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserNotificationServiceImplTest {
    private static final String TOPIC = "/topic/";
    private static final String NOTIFICATION = "/notification";
    @InjectMocks
    UserNotificationServiceImpl userNotificationService;
    @Mock
    private NotificationRepo notificationRepo;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private UserService userService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private HabitAssignRepo habitAssignRepo;

    @Test
    void getNotificationsFilteredTest() {
        Notification notification = getNotification();
        NotificationDto notificationDto = getNotificationDto();

        PageRequest page = PageRequest.of(0, 1);
        PageImpl<Notification> notificationPage = new PageImpl<>(List.of(notification), page, 0);

        PageableAdvancedDto<NotificationDto> actual = getPageableAdvancedDtoForNotificationDto();

        when(userService.findByEmail("danylo@gmail.com")).thenReturn(testUserVo);

        when(notificationRepo.findNotificationsByFilter(testUser.getId(), ProjectName.GREENCITY, null, true, page))
            .thenReturn(notificationPage);
        when(modelMapper.map(notification, NotificationDto.class)).thenReturn(notificationDto);

        PageableAdvancedDto<NotificationDto> expected = userNotificationService
            .getNotificationsFiltered(page, getPrincipal(), "en", ProjectName.GREENCITY, null, true);

        assertEquals(expected, actual);

        verify(userService).findByEmail("danylo@gmail.com");
        verify(notificationRepo).findNotificationsByFilter(testUser.getId(), ProjectName.GREENCITY, null, true, page);
        verify(modelMapper).map(notification, NotificationDto.class);
    }

    @Test
    void notificationSocketTest() {
        ActionDto dto = getActionDto();

        when(notificationRepo.countByTargetUserIdAndViewedIsFalse(dto.getUserId()))
            .thenReturn(1L);
        userNotificationService.notificationSocket(dto);

        verify(messagingTemplate).convertAndSend(TOPIC + dto.getUserId() + NOTIFICATION, 1L);
        verify(notificationRepo).countByTargetUserIdAndViewedIsFalse(dto.getUserId());
    }

    @Test
    void createNotificationForAttendersTest() {
        when(modelMapper.map(testUserVo, User.class)).thenReturn(testUser);
        when(notificationRepo.countByTargetUserIdAndViewedIsFalse(testUserVo.getId())).thenReturn(1L);
        userNotificationService.createNotificationForAttenders(List.of(testUserVo), "",
            NotificationType.EVENT_CREATED, 1L);
        verify(modelMapper).map(testUserVo, User.class);
        verify(messagingTemplate, times(1))
            .convertAndSend(TOPIC + testUser.getId() + NOTIFICATION, 1L);
    }

    @Test
    void createNotificationForAttendersWithTitleTest() {
        when(modelMapper.map(testUserVo, User.class)).thenReturn(testUser);
        when(notificationRepo.countByTargetUserIdAndViewedIsFalse(testUserVo.getId())).thenReturn(1L);
        userNotificationService.createNotificationForAttenders(List.of(testUserVo), "",
            NotificationType.EVENT_CREATED, 1L, "Title");
        verify(modelMapper).map(testUserVo, User.class);
        verify(messagingTemplate).convertAndSend(TOPIC + testUser.getId() + NOTIFICATION, 1L);
    }

    @Test
    void createNewNotificationForPlaceAddedTest() {
        when(modelMapper.map(testUserVo, User.class)).thenReturn(testUser);
        when(notificationRepo.countByTargetUserIdAndViewedIsFalse(testUserVo.getId())).thenReturn(1L);
        userNotificationService.createNewNotificationForPlaceAdded(List.of(testUserVo, testUserVo), 1L,
            "Category", "Name");

        verify(modelMapper, times(2)).map(testUserVo, User.class);
        verify(messagingTemplate, times(2)).convertAndSend(TOPIC + testUser.getId() + NOTIFICATION, 1L);
    }

    @Test
    void createNotificationTest() {
        when(modelMapper.map(testUserVo, User.class)).thenReturn(testUser);
        when(notificationRepo.countByTargetUserIdAndViewedIsFalse(testUserVo.getId())).thenReturn(1L);
        userNotificationService.createNotification(testUserVo, testUserVo, NotificationType.EVENT_CREATED);
        verify(modelMapper, times(2)).map(testUserVo, User.class);
        verify(messagingTemplate, times(1))
            .convertAndSend(TOPIC + testUser.getId() + NOTIFICATION, 1L);
    }

    @Test
    void createNotificationWithCustomMessageTest() {
        when(notificationRepo
            .findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(1L,
                NotificationType.EVENT_CREATED, 1L)).thenReturn(Optional.empty());
        when(notificationRepo.countByTargetUserIdAndViewedIsFalse(testUserVo.getId())).thenReturn(1L);

        when(modelMapper.map(testUserVo, User.class)).thenReturn(testUser);
        userNotificationService.createNotification(testUserVo, testUserVo,
            NotificationType.EVENT_CREATED, 1L, "Message");

        verify(notificationRepo)
            .findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(1L,
                NotificationType.EVENT_CREATED, 1L);
        verify(modelMapper, times(2)).map(testUserVo, User.class);
        verify(messagingTemplate, times(1))
            .convertAndSend(TOPIC + testUser.getId() + NOTIFICATION, 1L);
    }

    @Test
    void createNotificationWithSecondMessageTest() {
        when(notificationRepo
            .findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(1L,
                NotificationType.EVENT_CREATED, 1L)).thenReturn(Optional.empty());
        when(notificationRepo.countByTargetUserIdAndViewedIsFalse(testUserVo.getId())).thenReturn(1L);
        when(modelMapper.map(testUserVo, User.class)).thenReturn(testUser);
        userNotificationService.createNotification(testUserVo, testUserVo,
            NotificationType.EVENT_CREATED, 1L, "Message", 1L,
            "Second Message");

        verify(notificationRepo)
            .findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(1L,
                NotificationType.EVENT_CREATED, 1L);
        verify(modelMapper, times(2)).map(testUserVo, User.class);
        verify(messagingTemplate, times(1))
            .convertAndSend(TOPIC + testUser.getId() + NOTIFICATION, 1L);
    }

    @Test
    void createNewNotificationTest() {
        when(modelMapper.map(testUserVo, User.class)).thenReturn(testUser);
        when(notificationRepo.countByTargetUserIdAndViewedIsFalse(testUserVo.getId())).thenReturn(1L);
        userNotificationService.createNewNotification(testUserVo, NotificationType.EVENT_CREATED,
            1L, "Custom Message");
        verify(modelMapper).map(testUserVo, User.class);
        verify(messagingTemplate, times(1))
            .convertAndSend(TOPIC + testUser.getId() + NOTIFICATION, 1L);
    }

    @Test
    void removeActionUserFromNotificationTest() {
        var notification = getNotification();
        when(notificationRepo
            .findNotificationByTargetUserIdAndNotificationTypeAndTargetId(testUser.getId(),
                NotificationType.EVENT_CREATED, 1L))
            .thenReturn(notification);
        userNotificationService
            .removeActionUserFromNotification(testUserVo, testUserVo, 1L, NotificationType.EVENT_CREATED);
        verify(notificationRepo).findNotificationByTargetUserIdAndNotificationTypeAndTargetId(testUser.getId(),
            NotificationType.EVENT_CREATED, 1L);
    }

    @Test
    void removeActionUserFromNotificationWithSeveralActionUsersTest() {
        var notification = getNotificationWithSeveralActionUsers(3);
        when(notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetId(testUser.getId(),
            NotificationType.EVENT_CREATED, 1L))
            .thenReturn(notification);
        when(modelMapper.map(testUserVo, User.class)).thenReturn(testUser);
        userNotificationService
            .removeActionUserFromNotification(testUserVo, testUserVo, 1L, NotificationType.EVENT_CREATED);

        verify(notificationRepo).findNotificationByTargetUserIdAndNotificationTypeAndTargetId(testUser.getId(),
            NotificationType.EVENT_CREATED, 1L);
        verify(modelMapper).map(testUserVo, User.class);
    }

    @Test
    void removeActionUserFromNotificationIfNotificationIsNullTest() {
        when(notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetId(testUser.getId(),
            NotificationType.EVENT_CREATED, 1L)).thenReturn(null);
        userNotificationService.removeActionUserFromNotification(testUserVo, testUserVo, 1L,
            NotificationType.EVENT_CREATED);

        verify(notificationRepo).findNotificationByTargetUserIdAndNotificationTypeAndTargetId(testUser.getId(),
            NotificationType.EVENT_CREATED, 1L);
    }

    @Test
    void deleteNotificationTest() {
        Long notificationId = 1L;
        when(userService.findByEmail("danylo@gmail.com")).thenReturn(testUserVo);
        when(notificationRepo.existsByIdAndTargetUserId(notificationId, testUserVo.getId())).thenReturn(true);

        userNotificationService.deleteNotification(getPrincipal(), notificationId);

        verify(userService).findByEmail("danylo@gmail.com");
        verify(notificationRepo).existsByIdAndTargetUserId(notificationId, testUserVo.getId());
    }

    @Test
    void deleteNonExistentNotificationAndGetNotFoundExceptionTest() {
        Long notificationId = 1L;
        when(userService.findByEmail("danylo@gmail.com")).thenReturn(testUserVo);
        when(notificationRepo.existsByIdAndTargetUserId(notificationId, testUserVo.getId())).thenReturn(false);

        Principal principal = getPrincipal();
        assertThrows(NotFoundException.class,
            () -> userNotificationService.deleteNotification(principal, notificationId));
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
        verify(messagingTemplate).convertAndSend(TOPIC + userId + NOTIFICATION, 0L);
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
        verify(messagingTemplate).convertAndSend(TOPIC + userId + NOTIFICATION, 1L);
        verify(notificationRepo).markNotificationAsViewed(notificationId);
    }

    @Test
    @DisplayName("createOrUpdateHabitInviteNotification method updates existing notification")
    void testCreateOrUpdateHabitInviteNotification_UpdateExistingNotification() {
        UserVO targetUserVO = mock(UserVO.class);
        UserVO actionUserVO = mock(UserVO.class);
        User actionUser = mock(User.class);
        Long habitId = 1L;
        String habitName = "Test Habit";

        Notification existingNotification = mock(Notification.class);
        List<User> actionUsers = new ArrayList<>();
        when(existingNotification.getActionUsers()).thenReturn(actionUsers);
        when(notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(anyLong(),
            any(), anyLong()))
            .thenReturn(Optional.of(existingNotification));
        when(modelMapper.map(actionUserVO, User.class)).thenReturn(actionUser);

        userNotificationService.createOrUpdateHabitInviteNotification(targetUserVO, actionUserVO, habitId, habitName);

        assertEquals(1, actionUsers.size());
        assertEquals(actionUser, actionUsers.getFirst());

        verify(existingNotification).setCustomMessage(anyString());
        verify(existingNotification).setTime(any(LocalDateTime.class));
        verify(notificationRepo).save(existingNotification);
    }

    @Test
    @DisplayName("createOrUpdateHabitInviteNotification method creates new notification")
    void testCreateOrUpdateHabitInviteNotification_CreateNewNotification() {
        UserVO targetUserVO = mock(UserVO.class);
        UserVO actionUserVO = mock(UserVO.class);
        Long habitId = 1L;
        String habitName = "Test Habit";

        when(notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(anyLong(),
            any(), anyLong()))
            .thenReturn(Optional.empty());

        User targetUser = mock(User.class);
        when(modelMapper.map(targetUserVO, User.class)).thenReturn(targetUser);
        when(targetUser.getId()).thenReturn(1L);

        userNotificationService.createOrUpdateHabitInviteNotification(targetUserVO, actionUserVO, habitId, habitName);

        verify(notificationRepo, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("createOrUpdateLikeNotification updates existing notification when liking")
    void testCreateOrUpdateLikeNotification_UpdateExistingNotification_AddLike() {
        UserVO targetUserVO = mock(UserVO.class);
        UserVO actionUserVO = mock(UserVO.class);
        User actionUser = mock(User.class);
        Long newsId = 1L;
        String newsTitle = "Test News";

        Notification existingNotification = mock(Notification.class);
        List<User> actionUsers = new ArrayList<>();
        when(existingNotification.getActionUsers()).thenReturn(actionUsers);
        when(notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(anyLong(),
            any(), anyLong()))
            .thenReturn(Optional.of(existingNotification));
        when(modelMapper.map(actionUserVO, User.class)).thenReturn(actionUser);

        userNotificationService.createOrUpdateLikeNotification(any(LikeNotificationDto.class));

        assertTrue(actionUsers.contains(actionUser), "Action users should contain the actionUser.");

        verify(existingNotification).setCustomMessage(anyString());
        verify(existingNotification).setTime(any(LocalDateTime.class));
        verify(notificationRepo).save(existingNotification);
        verify(notificationRepo, never()).delete(existingNotification);
    }

    @Test
    @DisplayName("createOrUpdateLikeNotification updates existing notification when unliking and deletes it if no users left")
    void testCreateOrUpdateLikeNotification_UpdateExistingNotification_RemoveLike() {
        UserVO targetUserVO = mock(UserVO.class);
        UserVO actionUserVO = mock(UserVO.class);
        User actionUser = mock(User.class);
        Long newsId = 1L;
        String newsTitle = "Test News";

        Notification existingNotification = mock(Notification.class);
        List<User> actionUsers = new ArrayList<>();
        actionUsers.add(actionUser);
        when(existingNotification.getActionUsers()).thenReturn(actionUsers);
        when(notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(anyLong(),
            any(), anyLong()))
            .thenReturn(Optional.of(existingNotification));
        when(actionUserVO.getId()).thenReturn(1L);
        when(actionUser.getId()).thenReturn(1L);

        userNotificationService.createOrUpdateLikeNotification(any(LikeNotificationDto.class));

        assertTrue(actionUsers.isEmpty(), "Action users should be empty after unliking.");

        verify(notificationRepo).delete(existingNotification);
        verify(notificationRepo, never()).save(existingNotification);
    }

    @Test
    @DisplayName("createOrUpdateLikeNotification creates new notification when liking and no existing notification")
    void testCreateOrUpdateLikeNotification_CreateNewNotification_AddLike() {
        UserVO targetUserVO = mock(UserVO.class);
        UserVO actionUserVO = mock(UserVO.class);
        User actionUser = mock(User.class);
        Long newsId = 1L;
        String newsTitle = "Test News";

        when(notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(anyLong(),
            any(), anyLong()))
            .thenReturn(Optional.empty());
        when(modelMapper.map(any(UserVO.class), eq(User.class))).thenReturn(actionUser);

        userNotificationService.createOrUpdateLikeNotification(any(LikeNotificationDto.class));

        verify(notificationRepo, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("createInvitationNotificationMessage with two users")
    void testCreateInvitationNotificationMessage_TwoUsers() throws Exception {
        User user1 = User.builder().name("Taras").build();
        User user2 = User.builder().name("Petro").build();

        List<User> actionUsers = List.of(user1, user2);
        String habitName = "Test Habit";

        Method method = UserNotificationServiceImpl.class.getDeclaredMethod("createInvitationNotificationMessage",
            List.class, String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(userNotificationService, actionUsers, habitName);

        assertEquals("Taras and Petro invite you to add new habit Test Habit.", result);
    }

    @Test
    @DisplayName("createInvitationNotificationMessage with more than two users")
    void testCreateInvitationNotificationMessage_MoreThanTwoUsers() throws Exception {
        User user1 = User.builder().name("Taras").build();
        User user2 = User.builder().name("Petro").build();
        User user3 = User.builder().name("Vasyl").build();

        List<User> actionUsers = List.of(user1, user2, user3);
        String habitName = "Test Habit";

        Method method = UserNotificationServiceImpl.class.getDeclaredMethod("createInvitationNotificationMessage",
            List.class, String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(userNotificationService, actionUsers, habitName);

        assertEquals("Petro, Vasyl and other users invite you to add new habit Test Habit.", result);
    }

    @Test
    void checkLastDayOfHabitPrimaryDurationToMessageNoHabitAssigns() {
        when(habitAssignRepo.getHabitAssignsWithLastDayOfPrimaryDurationToMessage()).thenReturn(Collections.emptyList());

        userNotificationService.checkLastDayOfHabitPrimaryDurationToMessage();

        verify(habitAssignRepo).getHabitAssignsWithLastDayOfPrimaryDurationToMessage();
        verifyNoMoreInteractions(modelMapper, notificationService);
    }

    @Test
    void checkLastDayOfHabitPrimaryDurationToMessageShouldSendNotification() {
        Habit habit = getHabit().setHabitTranslations(List.of(getHabitTranslation()));
        User user = getUser().setId(2L);
        UserVO userVO = getUserVO().setId(2L);
        HabitAssign habitAssign = getHabitAssign(HabitAssignStatus.INPROGRESS).setUser(user).setHabit(habit);

        when(habitAssignRepo.getHabitAssignsWithLastDayOfPrimaryDurationToMessage())
            .thenReturn(List.of(habitAssign));
        when(modelMapper.map(getLanguage(), LanguageVO.class)).thenReturn(getLanguageVO());
        when(modelMapper.map(user, UserVO.class)).thenReturn(userVO);
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(notificationRepo.countByTargetUserIdAndViewedIsFalse(user.getId())).thenReturn(1L);
        userNotificationService.checkLastDayOfHabitPrimaryDurationToMessage();

        ArgumentCaptor<EmailNotificationDto> emailNotificationCaptor =
            ArgumentCaptor.forClass(EmailNotificationDto.class);
        verify(notificationService).sendEmailNotification(emailNotificationCaptor.capture());

        verify(messagingTemplate, times(1))
            .convertAndSend(TOPIC + user.getId() + NOTIFICATION, 1L);
    }

}
