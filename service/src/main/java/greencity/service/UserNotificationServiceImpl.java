package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.achievement.ActionDto;
import greencity.dto.language.LanguageVO;
import greencity.dto.notification.EmailNotificationDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.user.UserVO;
import greencity.entity.Notification;
import greencity.entity.User;
import greencity.enums.NotificationType;
import greencity.enums.ProjectName;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.HabitAssignRepo;
import greencity.repository.NotificationRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Implementation of {@link UserNotificationService}.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserNotificationServiceImpl implements UserNotificationService {
    private final NotificationRepo notificationRepo;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private static final String TOPIC = "/topic/";
    private static final String NOTIFICATION = "/notification";
    private final HabitAssignRepo habitAssignRepo;

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableAdvancedDto<NotificationDto> getNotificationsFiltered(Pageable page, Principal principal,
        String language, ProjectName projectName, List<NotificationType> notificationTypes, Boolean viewed) {
        Long userId = userService.findByEmail(principal.getName()).getId();
        Page<Notification> notifications =
            notificationRepo.findNotificationsByFilter(userId, projectName, notificationTypes, viewed, page);
        return buildPageableAdvancedDto(notifications, language);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notificationSocket(ActionDto user) {
        Long count = notificationRepo.countByTargetUserIdAndViewedIsFalse(user.getUserId());
        messagingTemplate.convertAndSend(TOPIC + user.getUserId() + NOTIFICATION, count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNotificationForAttenders(List<UserVO> attendersList, String message,
        NotificationType notificationType, Long targetId) {
        createNotificationForAttenders(attendersList, message, notificationType, targetId, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNotificationForAttenders(List<UserVO> attendersList, String message,
        NotificationType notificationType, Long targetId, String title) {
        for (UserVO targetUserVO : attendersList) {
            Notification notification = Notification.builder()
                .notificationType(notificationType)
                .projectName(ProjectName.GREENCITY)
                .targetUser(modelMapper.map(targetUserVO, User.class))
                .time(LocalDateTime.now())
                .targetId(targetId)
                .customMessage(message)
                .secondMessage(title)
                .emailSent(false)
                .build();
            notificationService.sendEmailNotification(
                modelMapper.map(notificationRepo.save(notification), EmailNotificationDto.class));
            sendNotification(notification.getTargetUser().getId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNotification(UserVO targetUser, UserVO actionUser, NotificationType notificationType) {
        Notification notification = Notification.builder()
            .notificationType(notificationType)
            .projectName(ProjectName.GREENCITY)
            .targetUser(modelMapper.map(targetUser, User.class))
            .time(LocalDateTime.now())
            .actionUsers(new ArrayList<>(List.of(modelMapper.map(actionUser, User.class))))
            .emailSent(false)
            .build();
        notificationService.sendEmailNotification(
            modelMapper.map(notificationRepo.save(notification), EmailNotificationDto.class));
        sendNotification(notification.getTargetUser().getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNotification(UserVO targetUserVO, UserVO actionUserVO, NotificationType notificationType,
        Long targetId, String customMessage) {
        Notification notification = notificationRepo
            .findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(targetUserVO.getId(),
                notificationType, targetId)
            .orElse(Notification.builder()
                .notificationType(notificationType)
                .projectName(ProjectName.GREENCITY)
                .targetUser(modelMapper.map(targetUserVO, User.class))
                .actionUsers(new ArrayList<>())
                .targetId(targetId)
                .customMessage(customMessage)
                .emailSent(false)
                .build());
        notification.getActionUsers().add(modelMapper.map(actionUserVO, User.class));
        notification.setTime(LocalDateTime.now());
        notificationService.sendEmailNotification(
            modelMapper.map(notificationRepo.save(notification), EmailNotificationDto.class));
        sendNotification(notification.getTargetUser().getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNotification(UserVO targetUserVO, UserVO actionUserVO, NotificationType notificationType,
        Long targetId, String customMessage, Long secondMessageId, String secondMessageText) {
        Notification notification = notificationRepo
            .findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(targetUserVO.getId(),
                notificationType, targetId)
            .orElse(Notification.builder()
                .notificationType(notificationType)
                .projectName(ProjectName.GREENCITY)
                .targetUser(modelMapper.map(targetUserVO, User.class))
                .actionUsers(new ArrayList<>())
                .targetId(targetId)
                .customMessage(customMessage)
                .secondMessageId(secondMessageId)
                .secondMessage(secondMessageText)
                .emailSent(false)
                .build());
        notification.getActionUsers().add(modelMapper.map(actionUserVO, User.class));
        notification.setTime(LocalDateTime.now());
        notification.setCustomMessage(customMessage);
        notificationService.sendEmailNotification(
            modelMapper.map(notificationRepo.save(notification), EmailNotificationDto.class));
        sendNotification(notification.getTargetUser().getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNewNotification(UserVO targetUserVO, NotificationType notificationType, Long targetId,
        String customMessage) {
        createNewNotification(targetUserVO, notificationType, targetId, customMessage, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNewNotification(UserVO targetUserVO, NotificationType notificationType, Long targetId,
        String customMessage, String secondMessage) {
        Notification notification = Notification.builder()
            .notificationType(notificationType)
            .projectName(ProjectName.GREENCITY)
            .targetUser(modelMapper.map(targetUserVO, User.class))
            .targetId(targetId)
            .customMessage(customMessage)
            .secondMessage(secondMessage)
            .time(LocalDateTime.now())
            .emailSent(false)
            .build();
        notificationService.sendEmailNotification(
            modelMapper.map(notificationRepo.save(notification), EmailNotificationDto.class));
        sendNotification(notification.getTargetUser().getId());
    }

    /**
     * {@inheritDoc}
     */
    public void createNewNotificationForPlaceAdded(List<UserVO> targetUsers, Long targetId, String customMessage,
        String secondMessage) {
        for (UserVO targetUser : targetUsers) {
            Notification notification = Notification.builder()
                .notificationType(NotificationType.PLACE_ADDED)
                .projectName(ProjectName.GREENCITY)
                .targetUser(modelMapper.map(targetUser, User.class))
                .time(LocalDateTime.now())
                .targetId(targetId)
                .customMessage(customMessage)
                .secondMessage(secondMessage)
                .emailSent(false)
                .build();
            notificationService.sendEmailNotification(modelMapper.map(notificationRepo.save(notification),
                EmailNotificationDto.class));
            sendNotification(notification.getTargetUser().getId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeActionUserFromNotification(UserVO targetUserVO, UserVO actionUserVO, Long targetId,
        NotificationType notificationType) {
        Notification notification = notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetId(
            targetUserVO.getId(), notificationType, targetId);
        if (notification != null) {
            if (notification.getActionUsers().size() == 1) {
                notificationRepo.delete(notification);
                return;
            }
            User user = modelMapper.map(actionUserVO, User.class);
            notification.getActionUsers()
                .removeIf(u -> u.getId().equals(user.getId()));
            notificationRepo.save(notification);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteNotification(Principal principal, Long notificationId) {
        Long userId = userService.findByEmail(principal.getName()).getId();
        if (!notificationRepo.existsByIdAndTargetUserId(notificationId, userId)) {
            throw new NotFoundException(ErrorMessage.NOTIFICATION_NOT_FOUND_BY_ID + notificationId);
        }
        notificationRepo.deleteNotificationByIdAndTargetUserId(notificationId, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unreadNotification(Long notificationId) {
        Long userId = notificationRepo.findById(notificationId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.NOTIFICATION_NOT_FOUND_BY_ID + notificationId))
            .getTargetUser().getId();
        notificationRepo.markNotificationAsNotViewed(notificationId);
        long count = notificationRepo.countByTargetUserIdAndViewedIsFalse(userId);
        messagingTemplate.convertAndSend(TOPIC + userId + NOTIFICATION, count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void viewNotification(Long notificationId) {
        Long userId = notificationRepo.findById(notificationId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.NOTIFICATION_NOT_FOUND_BY_ID + notificationId))
            .getTargetUser().getId();
        notificationRepo.markNotificationAsViewed(notificationId);
        long count = notificationRepo.countByTargetUserIdAndViewedIsFalse(userId);
        messagingTemplate.convertAndSend(TOPIC + userId + NOTIFICATION, count);
    }

    /**
     * {@inheritDoc}
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Override
    public void checkLastDayOfHabitPrimaryDurationToMessage() {
        habitAssignRepo.getHabitAssignsWithLastDayOfPrimaryDurationToMessage()
            .forEach(habitAssign -> {
                UserVO targetUser = modelMapper.map(habitAssign.getUser(), UserVO.class);
                String habitTitle = habitAssign.getHabit()
                    .getHabitTranslations()
                    .stream()
                    .filter(ht -> modelMapper.map(ht.getLanguage(), LanguageVO.class).getCode()
                        .equals(targetUser.getLanguageVO().getCode()))
                    .toList()
                    .getFirst()
                    .getName();

                createNewNotification(targetUser, NotificationType.HABIT_LAST_DAY_OF_PRIMARY_DURATION,
                    habitAssign.getId(), habitTitle);
            });
    }

    private PageableAdvancedDto<NotificationDto> buildPageableAdvancedDto(Page<Notification> notifications,
        String language) {
        List<NotificationDto> notificationDtoList = new LinkedList<>();
        for (Notification n : notifications) {
            notificationDtoList.add(createNotificationDto(n, language));
        }
        return new PageableAdvancedDto<>(
            notificationDtoList,
            notifications.getTotalElements(),
            notifications.getPageable().getPageNumber(),
            notifications.getTotalPages(),
            notifications.getNumber(),
            notifications.hasPrevious(),
            notifications.hasNext(),
            notifications.isFirst(),
            notifications.isLast());
    }

    /**
     * Method used to create {@link NotificationDto} from {@link Notification},
     * adding localized notification text.
     *
     * @param notification that should be transformed into dto
     * @param language     language code
     * @return mapped and localized {@link NotificationDto}
     */
    private NotificationDto createNotificationDto(Notification notification, String language) {
        NotificationDto dto = modelMapper.map(notification, NotificationDto.class);
        ResourceBundle bundle = ResourceBundle.getBundle("notification", Locale.forLanguageTag(language),
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT));
        dto.setTitleText(bundle.getString(dto.getNotificationType() + "_TITLE"));
        final List<User> uniqueActionUsers = new ArrayList<>(notification.getActionUsers().stream().distinct().toList());
        int size = new HashSet<>(uniqueActionUsers).size();
        dto.setActionUserText(uniqueActionUsers.stream().map(User::getName).toList());
        dto.setActionUserId(uniqueActionUsers.stream().map(User::getId).toList());
        String bodyTextTemplate = bundle.getString(dto.getNotificationType());
        String bodyText;
        switch (size) {
            case 1 -> bodyText = bodyTextTemplate;
            case 2 -> bodyText = bodyTextTemplate.replace("{user}", bundle.getString("TWO_USERS"));
            default -> bodyText = bodyTextTemplate.replace("{user}", bundle.getString("THREE_OR_MORE_USERS"));
        }
        dto.setBodyText(bodyText);
        return dto;
    }

    /**
     * Sends a new notification to a specified user.
     *
     * @param userId the ID of the user to whom the notification will be sent
     */
    private void sendNotification(Long userId) {
        long count = notificationRepo.countByTargetUserIdAndViewedIsFalse(userId);
        messagingTemplate.convertAndSend(TOPIC + userId + NOTIFICATION, count);
    }

    @Override
    public void createOrUpdateHabitInviteNotification(UserVO targetUserVO, UserVO actionUserVO, Long habitId,
        String habitName) {
        Optional<Notification> existingNotification = notificationRepo
            .findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(targetUserVO.getId(),
                NotificationType.HABIT_INVITE, habitId);

        if (existingNotification.isPresent()) {
            Notification notification = existingNotification.get();
            notification.getActionUsers().add(modelMapper.map(actionUserVO, User.class));

            notification
                .setCustomMessage(createInvitationNotificationMessage(notification.getActionUsers(), habitName));
            notification.setTime(LocalDateTime.now());
            notificationRepo.save(notification);
        } else {
            String customMessage = String.format("%s invites you to add new habit %s.",
                actionUserVO.getName(), habitName);
            createNotification(targetUserVO, actionUserVO, NotificationType.HABIT_INVITE, habitId, customMessage);
        }
    }

    private String createInvitationNotificationMessage(List<User> actionUsers, String habitName) {
        int userCount = actionUsers.size();

        return switch (userCount) {
            case 1 -> String.format("%s invites you to add new habit %s.",
                actionUsers.get(0).getName(), habitName);
            case 2 -> String.format("%s and %s invite you to add new habit %s.",
                actionUsers.get(0).getName(), actionUsers.get(1).getName(), habitName);
            default -> String.format("%s, %s and other users invite you to add new habit %s.",
                actionUsers.get(userCount - 2).getName(), actionUsers.get(userCount - 1).getName(), habitName);
        };
    }

    @Override
    public void createOrUpdateLikeNotification(UserVO targetUserVO, UserVO actionUserVO, Long newsId, String newsTitle,
        NotificationType notificationType, boolean isLike) {
        notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(
            targetUserVO.getId(), notificationType, newsId)
            .ifPresentOrElse(notification -> {
                List<User> actionUsers = notification.getActionUsers();
                actionUsers.removeIf(user -> user.getId().equals(actionUserVO.getId()));
                if (isLike) {
                    actionUsers.add(modelMapper.map(actionUserVO, User.class));
                }

                if (actionUsers.isEmpty()) {
                    notificationRepo.delete(notification);
                } else {
                    notification.setCustomMessage(newsTitle);
                    notification.setTime(LocalDateTime.now());
                    notificationRepo.save(notification);
                }
            }, () -> {
                if (isLike) {
                    createNotification(targetUserVO, actionUserVO, notificationType, newsId, newsTitle);
                }
            });
    }
}
