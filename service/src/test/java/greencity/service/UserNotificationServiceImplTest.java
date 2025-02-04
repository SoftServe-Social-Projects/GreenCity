package greencity.service;

import greencity.TestConst;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.achievement.ActionDto;
import greencity.dto.language.LanguageVO;
import greencity.dto.notification.EmailNotificationDto;
import greencity.dto.notification.LikeNotificationDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.notification.NotificationInviteDto;
import greencity.dto.user.UserVO;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.Notification;
import greencity.entity.User;
import greencity.enums.HabitAssignStatus;
import greencity.enums.InvitationStatus;
import greencity.enums.NotificationType;
import greencity.enums.ProjectName;
import greencity.repository.HabitAssignRepo;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.NotificationRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.security.Principal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static greencity.ModelUtils.*;
import static greencity.enums.NotificationType.EVENT_COMMENT_USER_TAG;
import static greencity.enums.NotificationType.FRIEND_REQUEST_RECEIVED;
import static greencity.enums.ProjectName.GREENCITY;
import static org.junit.jupiter.api.Assertions.*;
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
    private NotificationFriendService notificationFriendService;
    @Mock
    private HabitInvitationService habitInvitationService;
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

    static Stream<Arguments> getNotificationScenariosInEnglish() {
        return Stream.of(
            Arguments.of(
                "You have been tagged in the comment.",
                "{user} tagged you  in the event «{secondMessage}».",
                List.of(getUser()),
                List.of(1L),
                List.of("Taras")),
            Arguments.of(
                "You have been tagged in the comment.",
                "{user1} and {user2} tagged you  in the event «{secondMessage}».",
                List.of(getUser(), getUser().setId(2L)),
                List.of(1L, 2L),
                List.of("Taras", "Taras")),
            Arguments.of(
                "You have been tagged in the comment.",
                "{user} tagged you twice in the event «{secondMessage}».",
                List.of(getUser(), getUser()),
                List.of(1L),
                List.of("Taras")));
    }

    @ParameterizedTest
    @MethodSource("getNotificationScenariosInEnglish")
    void getNotificationsWithTaggingFilteredInEnglish(
        String titleText,
        String bodyText,
        List<User> actionUsers,
        List<Long> actionUsersId,
        List<String> actionUserText) {
        Notification notification = getBaseOfNotificationForEventCommentUserTag(actionUsers);
        NotificationDto notificationDto = NotificationDto.builder()
            .notificationId(1L)
            .projectName(String.valueOf(GREENCITY))
            .notificationType(EVENT_COMMENT_USER_TAG.name())
            .time(ZonedDateTime.of(2100, 1, 31, 12, 0, 0, 0, ZoneId.of("UTC")))
            .viewed(true)
            .build();
        List<Notification> list = List.of(notification);

        PageRequest pageRequest = PageRequest.of(0, 1);
        PageImpl<Notification> page = new PageImpl<>(list, pageRequest, 1);

        when(userService.findByEmail("danylo@gmail.com")).thenReturn(testUserVo);
        when(notificationRepo.findNotificationsByFilter(testUserVo.getId(),
            ProjectName.GREENCITY,
            null,
            true,
            pageRequest))
            .thenReturn(page);
        when(modelMapper.map(notification, NotificationDto.class)).thenReturn(notificationDto);

        PageableAdvancedDto<NotificationDto> actual = userNotificationService
            .getNotificationsFiltered(
                pageRequest,
                getPrincipal(),
                "en",
                ProjectName.GREENCITY,
                null,
                true);
        PageableAdvancedDto<NotificationDto> expected =
            getPageableAdvancedDtoWithNotificationForEventCommentUserTag(
                getBaseOfNotificationDtoForEventCommentUserTag(
                    titleText,
                    bodyText,
                    actionUsersId,
                    actionUserText));

        assertEquals(expected, actual);

        verify(userService).findByEmail("danylo@gmail.com");
        verify(notificationRepo)
            .findNotificationsByFilter(testUser.getId(), ProjectName.GREENCITY, null, true,
                pageRequest);
        verify(modelMapper).map(notification, NotificationDto.class);
    }

    @Test
    void getNotificationsFilteredWhenNotificationTypeInviteTest() {
        User targetUser = getUser().setId(1L);
        List<User> actionUsers = List.of(getUser().setId(2L));
        Notification friendRequestNotification = getNotification()
            .setNotificationType(NotificationType.FRIEND_REQUEST_RECEIVED)
            .setTargetUser(targetUser)
            .setActionUsers(actionUsers);
        Notification habitInviteNotification = getNotification()
            .setNotificationType(NotificationType.HABIT_INVITE)
            .setSecondMessageId(3L);

        NotificationDto friendRequestDto = getNotificationDto()
            .setNotificationType(NotificationType.FRIEND_REQUEST_RECEIVED.toString());
        NotificationDto habitInviteDto = getNotificationDto()
            .setNotificationType(NotificationType.HABIT_INVITE.toString());

        NotificationInviteDto friendRequestInviteDto = getNotificationInviteDto();
        friendRequestInviteDto.setNotificationType(NotificationType.FRIEND_REQUEST_RECEIVED.toString());
        NotificationInviteDto habitInviteInviteDto = getNotificationInviteDto();
        friendRequestInviteDto.setNotificationType(NotificationType.HABIT_INVITE.toString());

        PageRequest page = PageRequest.of(0, 2);
        PageImpl<Notification> notificationPage = new PageImpl<>(
            List.of(friendRequestNotification, habitInviteNotification), page, 2);

        when(userService.findByEmail("danylo@gmail.com")).thenReturn(testUserVo);
        when(notificationRepo.findNotificationsByFilter(testUser.getId(), ProjectName.GREENCITY, null,
            true, page))
            .thenReturn(notificationPage);
        when(modelMapper.map(friendRequestNotification, NotificationDto.class)).thenReturn(friendRequestDto);
        when(modelMapper.map(habitInviteNotification, NotificationDto.class)).thenReturn(habitInviteDto);

        when(notificationFriendService.getFriendRequestStatus(1L, 2L))
            .thenReturn(InvitationStatus.PENDING);
        when(habitInvitationService.getHabitInvitationStatus(3L))
            .thenReturn(InvitationStatus.ACCEPTED);

        when(modelMapper.map(friendRequestDto, NotificationInviteDto.class))
            .thenReturn(friendRequestInviteDto);
        when(modelMapper.map(habitInviteDto, NotificationInviteDto.class))
            .thenReturn(habitInviteInviteDto);

        PageableAdvancedDto<NotificationDto> expected = getPageableAdvancedDtoForNotificationInviteDto(
            List.of(friendRequestInviteDto, habitInviteInviteDto));

        PageableAdvancedDto<NotificationDto> result = userNotificationService
            .getNotificationsFiltered(page, getPrincipal(), "en", ProjectName.GREENCITY, null,
                true);

        NotificationInviteDto notificationInviteDto = (NotificationInviteDto) result.getPage().getFirst();

        assertEquals(expected, result);
        assertEquals(InvitationStatus.PENDING.toString(), notificationInviteDto.getStatus());
        verify(userService).findByEmail("danylo@gmail.com");
        verify(notificationRepo).findNotificationsByFilter(testUser.getId(), ProjectName.GREENCITY, null,
            true, page);
        verify(modelMapper).map(friendRequestNotification, NotificationDto.class);
        verify(modelMapper).map(habitInviteNotification, NotificationDto.class);
        verify(notificationFriendService).getFriendRequestStatus(1L, 2L);
        verify(habitInvitationService).getHabitInvitationStatus(3L);
    }

    static Stream<Arguments> getNotificationScenariosInUkrainian() {
        return Stream.of(
            Arguments.of(
                "Вас тегнули в коментарі",
                "{user} позначив вас  у події «{secondMessage}».",
                List.of(getUser()),
                List.of(1L),
                List.of("Taras")),
            Arguments.of(
                "Вас тегнули в коментарі",
                "{user1} та {user2} позначив вас  у події «{secondMessage}».",
                List.of(getUser(), getUser().setId(2L)),
                List.of(1L, 2L),
                List.of("Taras", "Taras")),
            Arguments.of(
                "Вас тегнули в коментарі",
                "{user} позначив вас 2 рази у події «{secondMessage}».",
                List.of(getUser(), getUser()),
                List.of(1L),
                List.of("Taras")),
            Arguments.of(
                "Вас тегнули в коментарі",
                "{user} позначив вас 5 разів у події «{secondMessage}».",
                List.of(getUser(), getUser(), getUser(), getUser(), getUser()),
                List.of(1L),
                List.of("Taras")),
            Arguments.of(
                "Вас тегнули в коментарі",
                "{user} позначив вас 10 разів у події «{secondMessage}».",
                List.of(getUser(), getUser(), getUser(), getUser(), getUser(), getUser(), getUser(), getUser(),
                    getUser(), getUser()),
                List.of(1L),
                List.of("Taras")));
    }

    @ParameterizedTest
    @MethodSource("getNotificationScenariosInUkrainian")
    void getNotificationsWithTaggingFilteredInUkrainian(
        String titleText,
        String bodyText,
        List<User> actionUsers,
        List<Long> actionUsersId,
        List<String> actionUserText) {
        Notification notification = getBaseOfNotificationForEventCommentUserTag(actionUsers);
        NotificationDto notificationDto = NotificationDto.builder()
            .notificationId(1L)
            .projectName(String.valueOf(GREENCITY))
            .notificationType(EVENT_COMMENT_USER_TAG.name())
            .time(ZonedDateTime.of(2100, 1, 31, 12, 0, 0, 0, ZoneId.of("UTC")))
            .viewed(true)
            .build();
        List<Notification> list = List.of(notification);

        PageRequest pageRequest = PageRequest.of(0, 1);
        PageImpl<Notification> page = new PageImpl<>(list, pageRequest, 1);

        when(userService.findByEmail("danylo@gmail.com")).thenReturn(testUserVo);
        when(notificationRepo.findNotificationsByFilter(testUserVo.getId(),
            ProjectName.GREENCITY,
            null,
            true,
            pageRequest))
            .thenReturn(page);
        when(modelMapper.map(notification, NotificationDto.class)).thenReturn(notificationDto);

        PageableAdvancedDto<NotificationDto> actual = userNotificationService
            .getNotificationsFiltered(
                pageRequest,
                getPrincipal(),
                "ua",
                ProjectName.GREENCITY,
                null,
                true);
        PageableAdvancedDto<NotificationDto> expected =
            getPageableAdvancedDtoWithNotificationForEventCommentUserTag(
                getBaseOfNotificationDtoForEventCommentUserTag(
                    titleText,
                    bodyText,
                    actionUsersId,
                    actionUserText));
        assertEquals(expected, actual);

        verify(userService).findByEmail("danylo@gmail.com");
        verify(notificationRepo)
            .findNotificationsByFilter(testUser.getId(), ProjectName.GREENCITY, null, true, pageRequest);
        verify(modelMapper).map(notification, NotificationDto.class);
    }

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
            .findByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalseAndSecondMessageId(1L,
                NotificationType.EVENT_CREATED, 1L, 1L)).thenReturn(Optional.empty());
        when(notificationRepo.countByTargetUserIdAndViewedIsFalse(testUserVo.getId())).thenReturn(1L);
        when(modelMapper.map(testUserVo, User.class)).thenReturn(testUser);
        userNotificationService.createNotification(testUserVo, testUserVo,
            NotificationType.EVENT_CREATED, 1L, "Message", 1L,
            "Second Message");

        verify(notificationRepo)
            .findByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalseAndSecondMessageId(1L,
                NotificationType.EVENT_CREATED, 1L, 1L);
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
            .findNotificationByTargetUserIdAndNotificationTypeAndIdentifier(testUser.getId(),
                NotificationType.EVENT_CREATED, 1L))
            .thenReturn(notification);
        userNotificationService
            .removeActionUserFromNotification(testUserVo, testUserVo, 1L, NotificationType.EVENT_CREATED);
        verify(notificationRepo).findNotificationByTargetUserIdAndNotificationTypeAndIdentifier(testUser.getId(),
            NotificationType.EVENT_CREATED, 1L);
    }

    @Test
    void removeActionUserFromNotificationWithSeveralActionUsersTest() {
        var notification = getNotificationWithSeveralActionUsers(3);
        when(notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndIdentifier(testUser.getId(),
            NotificationType.EVENT_CREATED, 1L))
            .thenReturn(notification);
        when(modelMapper.map(testUserVo, User.class)).thenReturn(testUser);
        userNotificationService
            .removeActionUserFromNotification(testUserVo, testUserVo, 1L, NotificationType.EVENT_CREATED);

        verify(notificationRepo).findNotificationByTargetUserIdAndNotificationTypeAndIdentifier(testUser.getId(),
            NotificationType.EVENT_CREATED, 1L);
        verify(modelMapper).map(testUserVo, User.class);
    }

    @Test
    void removeActionUserFromNotificationIfNotificationIsNullTest() {
        when(notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndIdentifier(testUser.getId(),
            NotificationType.EVENT_CREATED, 1L)).thenReturn(null);
        userNotificationService.removeActionUserFromNotification(testUserVo, testUserVo, 1L,
            NotificationType.EVENT_CREATED);

        verify(notificationRepo, times(0)).findNotificationByTargetUserIdAndNotificationTypeAndTargetId(testUser.getId(),
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
    void createNotification_ShouldUpdateExistingNotificationWithoutIdentifierSecondMessageId() {
        Notification notification = mock(Notification.class);
        UserVO targetUserVO = mock(UserVO.class);
        UserVO actionUserVO = mock(UserVO.class);
        NotificationType notificationType = NotificationType.HABIT_LIKE;
        Long targetId = 1L;
        String customMessage = "Custom Message";
        String secondMessageText = "Second Message";
        User targetUser = mock(User.class);

        when(targetUserVO.getId()).thenReturn(1L);
        when(notification.getTargetUser()).thenReturn(targetUser);
        when(notification.getTargetUser().getId()).thenReturn(1L);
        when(notificationRepo
            .findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(targetUserVO.getId(),
                notificationType, targetId))
            .thenReturn(Optional.of(notification));

        userNotificationService.createNotification(targetUserVO, actionUserVO, notificationType, targetId,
            customMessage, secondMessageText);

        verify(notificationRepo).findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(
            targetUserVO.getId(),
            notificationType, targetId);
        verify(notificationService).sendEmailNotification(
            modelMapper.map(notificationRepo.save(notification), EmailNotificationDto.class));
        verify(messagingTemplate).convertAndSend(TOPIC + targetUser.getId() + NOTIFICATION, 0L);
    }

    @Test
    void createNotification_ShouldCreateNotificationWithoutIdentifierSecondMessageId() {
        Notification notification = mock(Notification.class);
        UserVO targetUserVO = mock(UserVO.class);
        UserVO actionUserVO = mock(UserVO.class);
        NotificationType notificationType = NotificationType.HABIT_LIKE;
        Long targetId = 1L;
        String customMessage = "Custom Message";
        String secondMessageText = "Second Message";
        User targetUser = mock(User.class);

        when(targetUserVO.getId()).thenReturn(1L);
        when(notification.getTargetUser()).thenReturn(targetUser);
        when(notification.getTargetUser().getId()).thenReturn(1L);
        when(modelMapper.map(targetUserVO, User.class)).thenReturn(targetUser);
        when(notificationRepo
            .findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(targetUserVO.getId(),
                notificationType, targetId))
            .thenReturn(Optional.ofNullable(null));

        userNotificationService.createNotification(targetUserVO, actionUserVO, notificationType, targetId,
            customMessage, secondMessageText);

        verify(notificationRepo).findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(
            targetUserVO.getId(), notificationType, targetId);
        verify(notificationService).sendEmailNotification(
            modelMapper.map(notificationRepo.save(notification), EmailNotificationDto.class));
        verify(messagingTemplate).convertAndSend(TOPIC + targetUser.getId() + NOTIFICATION, 0L);
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
    @DisplayName("createOrUpdateLikeNotification updates existing notification when liking")
    void testCreateOrUpdateLikeNotification_UpdateExistingNotification_AddLike() {
        UserVO targetUserVO = mock(UserVO.class);
        UserVO actionUserVO = mock(UserVO.class);
        User actionUser = mock(User.class);
        Long newsId = 1L;
        String newsTitle = "Test News";
        long secondMessageId = 1L;

        Notification existingNotification = mock(Notification.class);
        List<User> actionUsers = new ArrayList<>();
        when(existingNotification.getActionUsers()).thenReturn(actionUsers);
        when(notificationRepo.findByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalseAndSecondMessageId(
            anyLong(), any(), anyLong(), anyLong()))
            .thenReturn(Optional.of(existingNotification));
        when(modelMapper.map(actionUserVO, User.class)).thenReturn(actionUser);

        userNotificationService.createOrUpdateLikeNotification(LikeNotificationDto.builder()
            .targetUserVO(targetUserVO)
            .actionUserVO(actionUserVO)
            .newsId(newsId)
            .newsTitle(newsTitle)
            .notificationType(NotificationType.ECONEWS_COMMENT_LIKE)
            .secondMessageId(secondMessageId)
            .isLike(true)
            .build());

        assertTrue(actionUsers.contains(actionUser), "Action users should contain the actionUser.");

        verify(existingNotification).setCustomMessage(anyString());
        verify(existingNotification).setTime(any(ZonedDateTime.class));
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
        long secondMessageId = 1L;

        Notification existingNotification = mock(Notification.class);
        List<User> actionUsers = new ArrayList<>();
        actionUsers.add(actionUser);
        when(existingNotification.getActionUsers()).thenReturn(actionUsers);
        when(notificationRepo.findByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalseAndSecondMessageId(
            anyLong(),
            any(), anyLong(), anyLong()))
            .thenReturn(Optional.of(existingNotification));
        when(actionUserVO.getId()).thenReturn(1L);
        when(actionUser.getId()).thenReturn(1L);

        userNotificationService.createOrUpdateLikeNotification(LikeNotificationDto.builder()
            .targetUserVO(targetUserVO)
            .actionUserVO(actionUserVO)
            .newsId(newsId)
            .newsTitle(newsTitle)
            .notificationType(NotificationType.ECONEWS_COMMENT_LIKE)
            .secondMessageId(secondMessageId)
            .isLike(false)
            .build());

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
        long secondMessageId = 1L;

        when(notificationRepo.findByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalseAndSecondMessageId(
            anyLong(), any(), anyLong(), anyLong()))
            .thenReturn(Optional.empty());
        when(modelMapper.map(any(UserVO.class), eq(User.class))).thenReturn(actionUser);

        userNotificationService.createOrUpdateLikeNotification(LikeNotificationDto.builder()
            .targetUserVO(targetUserVO)
            .actionUserVO(actionUserVO)
            .newsId(newsId)
            .newsTitle(newsTitle)
            .notificationType(NotificationType.ECONEWS_COMMENT_LIKE)
            .secondMessageId(secondMessageId)
            .isLike(true)
            .build());

        verify(notificationRepo, times(1)).save(any(Notification.class));
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
