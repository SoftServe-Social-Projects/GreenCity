package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.achievement.ActionDto;
import greencity.dto.language.LanguageVO;
import greencity.dto.notification.EmailNotificationDto;
import greencity.dto.notification.LikeNotificationDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.notification.NotificationInviteDto;
import greencity.dto.user.UserVO;
import greencity.entity.Notification;
import greencity.entity.User;
import greencity.enums.InvitationStatus;
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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import static greencity.constant.AppConstant.LANGUAGE_CODE_UA;
import static greencity.constant.AppConstant.THREE_OR_MORE_USERS;
import static greencity.constant.AppConstant.TIMES_PLACEHOLDER;
import static greencity.constant.AppConstant.TWO_USERS;
import static greencity.constant.AppConstant.USER_PLACEHOLDER;
import static greencity.utils.NotificationUtils.resolveTimesInEnglish;
import static greencity.utils.NotificationUtils.resolveTimesInUkrainian;
import static greencity.utils.NotificationUtils.isMessageLocalizationRequired;
import static greencity.utils.NotificationUtils.localizeMessage;

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
    private final HabitInvitationService habitInvitationService;
    private final NotificationFriendService notificationFriendService;
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
                .time(ZonedDateTime.now())
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
            .time(ZonedDateTime.now())
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
        notification.setTime(ZonedDateTime.now());
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
            .findByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalseAndSecondMessageId(
                targetUserVO.getId(), notificationType, targetId, secondMessageId)
            .orElse(buildNotification(
                notificationType,
                targetUserVO,
                targetId,
                customMessage,
                secondMessageId,
                secondMessageText));
        notification.getActionUsers().add(modelMapper.map(actionUserVO, User.class));
        notification.setTime(ZonedDateTime.now());
        notification.setCustomMessage(customMessage);
        notificationService.sendEmailNotification(
            modelMapper.map(notificationRepo.save(notification), EmailNotificationDto.class));
        sendNotification(notification.getTargetUser().getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNotification(UserVO targetUserVO, UserVO actionUserVO, NotificationType notificationType,
        Long targetId, String customMessage, String secondMessageText) {
        final Notification notification = notificationRepo
            .findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(targetUserVO.getId(),
                notificationType, targetId)
            .orElse(buildNotification(notificationType, targetUserVO, targetId, customMessage, null,
                secondMessageText));
        notification.getActionUsers().add(modelMapper.map(actionUserVO, User.class));
        notification.setTime(ZonedDateTime.now());
        notification.setCustomMessage(customMessage);
        notificationService
            .sendEmailNotification(modelMapper.map(notificationRepo.save(notification), EmailNotificationDto.class));
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
            .time(ZonedDateTime.now())
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
                .time(ZonedDateTime.now())
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
        Notification notification = notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndIdentifier(
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

        if (NotificationType.isInviteOrRequest(notification.getNotificationType())) {
            dto = mapToNotificationInviteDto(dto, notification);
        }

        dto.setTitleText(bundle.getString(dto.getNotificationType() + "_TITLE"));
        setActionUserDetails(dto, notification);
        dto.setBodyText(generateBodyText(notification, bundle, language));

        if (dto.getMessage() != null && isMessageLocalizationRequired(dto.getNotificationType())) {
            dto.setMessage(localizeMessage(dto.getMessage(), bundle));
        }

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
    public void createOrUpdateLikeNotification(final LikeNotificationDto likeNotificationDto) {
        boolean isCommentLike = NotificationType.isCommentLike(likeNotificationDto.getNotificationType());
        Optional<Notification> baseNotification = (isCommentLike
            ? notificationRepo.findByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalseAndSecondMessageId(
                likeNotificationDto.getTargetUserVO().getId(), likeNotificationDto.getNotificationType(),
                likeNotificationDto.getNewsId(), likeNotificationDto.getSecondMessageId())
            : notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(
                likeNotificationDto.getTargetUserVO().getId(), likeNotificationDto.getNotificationType(),
                likeNotificationDto.getNewsId()));
        baseNotification.ifPresentOrElse(notification -> {
            List<User> actionUsers = notification.getActionUsers();
            actionUsers.removeIf(user -> user.getId().equals(likeNotificationDto.getActionUserVO().getId()));
            if (likeNotificationDto.isLike()) {
                actionUsers.add(modelMapper.map(likeNotificationDto.getActionUserVO(), User.class));
            }

            if (actionUsers.isEmpty()) {
                notificationRepo.delete(notification);
            } else {
                notification.setCustomMessage(likeNotificationDto.getNewsTitle());
                notification.setTime(ZonedDateTime.now());
                notificationRepo.save(notification);
            }
        }, () -> {
            if (likeNotificationDto.isLike()) {
                generateNotification(likeNotificationDto.getTargetUserVO(), likeNotificationDto.getActionUserVO(),
                    likeNotificationDto.getNotificationType(), likeNotificationDto.getNewsId(),
                    likeNotificationDto.getNewsTitle(), likeNotificationDto.getSecondMessageId(),
                    likeNotificationDto.getSecondMessageText());
            }
        });
    }

    private void generateNotification(final UserVO targetUserVO, final UserVO actionUserVO,
        final NotificationType notificationType, final Long targetId,
        final String customMessage, final Long secondMessageId,
        final String secondMessageText) {
        final Notification notification = Notification.builder()
            .notificationType(notificationType)
            .projectName(ProjectName.GREENCITY)
            .targetUser(modelMapper.map(targetUserVO, User.class))
            .actionUsers(new ArrayList<>())
            .targetId(targetId)
            .customMessage(customMessage)
            .secondMessageId(secondMessageId)
            .secondMessage(secondMessageText)
            .emailSent(false)
            .build();
        notification.getActionUsers().add(modelMapper.map(actionUserVO, User.class));
        notification.setTime(ZonedDateTime.now());
        notification.setCustomMessage(customMessage);
        notificationService.sendEmailNotification(
            modelMapper.map(notificationRepo.save(notification), EmailNotificationDto.class));
        sendNotification(notification.getTargetUser().getId());
    }

    private Notification buildNotification(NotificationType notificationType, UserVO targetUserVO, Long targetId,
        String customMessage, Long secondMessageId, String secondMessageText) {
        return Notification.builder()
            .notificationType(notificationType)
            .projectName(ProjectName.GREENCITY)
            .targetUser(modelMapper.map(targetUserVO, User.class))
            .actionUsers(new ArrayList<>())
            .targetId(targetId)
            .customMessage(customMessage)
            .secondMessageId(secondMessageId)
            .secondMessage(secondMessageText)
            .emailSent(false)
            .build();
    }

    /**
     * Maps a {@link NotificationDto} to a {@link NotificationInviteDto} and assigns
     * the invitation status.
     *
     * @param dto          the base notification DTO to map from.
     * @param notification the notification entity containing additional data.
     * @return a {@link NotificationInviteDto} with an assigned invitation status.
     */
    private NotificationInviteDto mapToNotificationInviteDto(NotificationDto dto, Notification notification) {
        NotificationInviteDto inviteDto = modelMapper.map(dto, NotificationInviteDto.class);
        InvitationStatus status = getInvitationStatus(notification);
        inviteDto.setStatus(status != null ? status.name() : null);
        return inviteDto;
    }

    /**
     * Retrieves the invitation status based on the type of notification.
     *
     * @param notification the notification object for which the status is being
     *                     retrieved.
     * @return the {@link InvitationStatus} corresponding to the notification's
     *         type. If the notification type doesn't match any of the cases,
     *         returns {@code null}.
     */
    private InvitationStatus getInvitationStatus(Notification notification) {
        return switch (notification.getNotificationType()) {
            case FRIEND_REQUEST_RECEIVED -> notificationFriendService.getFriendRequestStatus(
                notification.getTargetUser().getId(),
                notification.getActionUsers().getFirst().getId());
            case HABIT_INVITE -> habitInvitationService.getHabitInvitationStatus(
                notification.getSecondMessageId());
            default -> null;
        };
    }

    /**
     * Extracts unique action users from the notification and assigns their names
     * and IDs to the DTO.
     *
     * @param dto          the notification DTO where user details should be set.
     * @param notification the notification entity containing the action users.
     */
    private void setActionUserDetails(NotificationDto dto, Notification notification) {
        List<User> uniqueUsers = notification.getActionUsers().stream().distinct().toList();
        dto.setActionUserText(uniqueUsers.stream().map(User::getName).toList());
        dto.setActionUserId(uniqueUsers.stream().map(User::getId).toList());
    }

    /**
     * Generates a localized body text for the notification based on the number of
     * unique action users. Replaces placeholders "{user}" and "{times}"
     * dynamically.
     *
     * @param notification the notification entity to generate text for.
     * @param bundle       the resource bundle containing localized text templates.
     * @param language     the language code for localization.
     * @return the formatted body text with appropriate pluralization.
     */
    private String generateBodyText(Notification notification, ResourceBundle bundle, String language) {
        String bodyTextTemplate = bundle.getString(notification.getNotificationType().toString());
        int uniqueUserCount = new HashSet<>(notification.getActionUsers()).size();

        String bodyText = switch (uniqueUserCount) {
            case 1 -> bodyTextTemplate;
            case 2 -> bodyTextTemplate.replace(USER_PLACEHOLDER, bundle.getString(TWO_USERS));
            default -> bodyTextTemplate.replace(USER_PLACEHOLDER, bundle.getString(THREE_OR_MORE_USERS));
        };

        if (bodyText.contains(TIMES_PLACEHOLDER)) {
            int messagesCount = notification.getActionUsers().size();
            String resolvedTimes = "";

            if (uniqueUserCount == 1) {
                resolvedTimes = language.equals(LANGUAGE_CODE_UA)
                    ? resolveTimesInUkrainian(messagesCount)
                    : resolveTimesInEnglish(messagesCount);
            }

            bodyText = bodyText.replace(TIMES_PLACEHOLDER, resolvedTimes);
        }

        return bodyText;
    }
}
