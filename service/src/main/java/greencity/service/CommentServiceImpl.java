package greencity.service;

import greencity.achievement.AchievementCalculation;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableDto;
import greencity.dto.comment.AddCommentDtoRequest;
import greencity.dto.comment.AddCommentDtoResponse;
import greencity.dto.comment.AmountCommentLikesDto;
import greencity.dto.comment.CommentAuthorDto;
import greencity.dto.comment.CommentDto;
import greencity.dto.comment.CommentVO;
import greencity.dto.notification.LikeNotificationDto;
import greencity.dto.user.UserSearchDto;
import greencity.dto.user.UserTagDto;
import greencity.dto.user.UserVO;
import greencity.entity.Comment;
import greencity.entity.CommentImages;
import greencity.entity.EcoNews;
import greencity.entity.Habit;
import greencity.entity.HabitTranslation;
import greencity.entity.User;
import greencity.entity.event.Event;
import greencity.enums.AchievementAction;
import greencity.enums.AchievementCategoryType;
import greencity.enums.ArticleType;
import greencity.enums.CommentActionType;
import greencity.enums.CommentStatus;
import greencity.enums.NotificationType;
import greencity.enums.Role;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.rating.RatingCalculation;
import greencity.repository.CommentRepo;
import greencity.repository.EcoNewsRepo;
import greencity.repository.EventRepo;
import greencity.repository.HabitRepo;
import greencity.repository.NotificationRepo;
import greencity.repository.UserRepo;
import greencity.repository.HabitTranslationRepo;
import greencity.repository.RatingPointsRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static greencity.constant.ErrorMessage.ECO_NEW_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.EVENT_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.HABIT_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.USER_NOT_FOUND_BY_ID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final HabitRepo habitRepo;
    private final EventRepo eventRepo;
    private final EcoNewsRepo ecoNewsRepo;
    private final UserRepo userRepo;
    private final CommentRepo commentRepo;
    private final NotificationRepo notificationRepo;
    private final HabitTranslationRepo habitTranslationRepo;
    private final RatingPointsRepo ratingPointsRepo;
    private final ModelMapper modelMapper;
    private final FileService fileService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RatingCalculation ratingCalculation;
    private final AchievementCalculation achievementCalculation;
    private final UserNotificationService userNotificationService;
    private final NotificationService notificationService;
    @Value("${client.address}")
    private String clientAddress;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AddCommentDtoResponse save(ArticleType articleType, Long articleId,
        AddCommentDtoRequest addCommentDtoRequest, MultipartFile[] images, UserVO userVO,
        Locale locale) {
        final User articleAuthor = getArticleAuthor(articleType, articleId);
        if (articleAuthor == null) {
            throw new NotFoundException("Article author not found");
        }

        Comment comment = modelMapper.map(addCommentDtoRequest, Comment.class);
        comment.setArticleType(articleType);
        comment.setArticleId(articleId);
        comment.setUser(modelMapper.map(userVO, User.class));
        comment.setStatus(CommentStatus.ORIGINAL);

        boolean isCommentReply = addCommentDtoRequest.getParentCommentId() != null
            && addCommentDtoRequest.getParentCommentId() > 0;
        if (isCommentReply) {
            Long parentCommentId = addCommentDtoRequest.getParentCommentId();
            Comment parentComment = commentRepo.findById(parentCommentId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.COMMENT_NOT_FOUND_BY_ID + parentCommentId));

            if (parentComment.getParentComment() != null) {
                throw new BadRequestException(ErrorMessage.CANNOT_REPLY_THE_REPLY);
            }

            if (!(parentComment.getArticleId().equals(articleId)
                && parentComment.getArticleType().equals(articleType))) {
                String message = ErrorMessage.COMMENT_NOT_FOUND_BY_ID + parentCommentId
                    + " in " + articleType.getName() + " with id: " + articleId;
                throw new NotFoundException(message);
            }
            comment.setParentComment(parentComment);
            if (checkUserIsNotAuthor(userVO, parentComment.getUser())) {
                createCommentReplyNotification(articleType, articleId, comment,
                    userVO,
                    modelMapper.map(parentComment.getUser(), UserVO.class), locale);
            }
        }

        addImagesToComment(comment, images);
        ratingCalculation.ratingCalculation(ratingPointsRepo.findByNameOrThrow("COMMENT_OR_REPLY"), userVO);
        achievementCalculation.calculateAchievement(userVO,
            AchievementCategoryType.COMMENT_OR_REPLY, AchievementAction.ASSIGN);

        AddCommentDtoResponse addCommentDtoResponse = modelMapper.map(
            commentRepo.save(comment), AddCommentDtoResponse.class);
        addCommentDtoResponse.setAuthor(modelMapper.map(userVO, CommentAuthorDto.class));
        if (checkUserIsNotAuthor(userVO, articleAuthor) && !isCommentReply) {
            createCommentNotification(articleType, articleId, userVO, locale);
        }
        sendNotificationToTaggedUser(comment, articleType, locale);

        return addCommentDtoResponse;
    }

    /**
     * Checks if the specified user is not the author of the article.
     *
     * @param userVO        the {@link UserVO} object representing the user to check
     * @param articleAuthor the {@link User} object representing the author of the
     *                      article
     * @return {@code true} if the user is not the author of the article;
     *         {@code false} otherwise
     */
    private boolean checkUserIsNotAuthor(final UserVO userVO, final User articleAuthor) {
        return !articleAuthor.getId().equals(userVO.getId());
    }

    /**
     * Adds a list of images to a comment.
     *
     * @param comment the {@link Comment} object to which images will be added
     * @param images  an array of {@link MultipartFile} objects representing the
     *                images to be added to the comment
     */
    private void addImagesToComment(Comment comment, MultipartFile[] images) {
        if (images != null && images.length > 0 && images[0] != null) {
            List<CommentImages> commentImages = new ArrayList<>();
            for (MultipartFile image : images) {
                if (image != null) {
                    commentImages.add(CommentImages.builder()
                        .comment(comment)
                        .link(fileService.upload(image))
                        .build());
                }
            }
            comment.setAdditionalImages(commentImages);
        }
    }

    /**
     * Sends a notification to users tagged in a comment on a specific article.
     *
     * @param articleType the type of the article where the comment is made,
     *                    {@link ArticleType}.
     * @param locale      the locale used for localization of the notification,
     *                    {@link Locale}.
     * @throws NotFoundException if a tagged user is not found by ID.
     */
    private void sendNotificationToTaggedUser(Comment comment, ArticleType articleType, Locale locale) {
        String commentText = comment.getText();
        Set<Long> usersId = getUserIdFromComment(commentText);
        NotificationType notificationType = getNotificationType(articleType, CommentActionType.COMMENT_USER_TAG);
        CommentVO commentVO = modelMapper.map(comment, CommentVO.class);
        if (!usersId.isEmpty()) {
            for (Long userId : usersId) {
                User user = userRepo.findById(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + userId));
                userNotificationService.createNotification(
                    modelMapper.map(user, UserVO.class),
                    commentVO.getUser(),
                    notificationType,
                    commentVO.getArticleId(),
                    null,
                    getArticleTitle(articleType, commentVO.getArticleId(), locale));
            }
        }
    }

    /**
     * Method to return author of article.
     *
     * @param articleType {@link ArticleType}.
     * @param articleId   {@link Long} id of an article.
     * @return article author {@link User}.
     */
    protected User getArticleAuthor(ArticleType articleType, Long articleId) {
        Long articleAuthorId = switch (articleType) {
            case HABIT -> {
                Habit habit = habitRepo.findById(articleId)
                    .orElseThrow(() -> new NotFoundException(HABIT_NOT_FOUND_BY_ID + articleId));
                yield habit.getUserId();
            }
            case EVENT -> {
                Event event = eventRepo.findById(articleId)
                    .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND_BY_ID + articleId));
                yield event.getOrganizer().getId();
            }
            case ECO_NEWS -> {
                EcoNews ecoNews = ecoNewsRepo.findById(articleId)
                    .orElseThrow(() -> new NotFoundException(ECO_NEW_NOT_FOUND_BY_ID + articleId));
                yield ecoNews.getAuthor().getId();
            }
        };
        return userRepo.findById(articleAuthorId)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_BY_ID + articleAuthorId));
    }

    /**
     * Method to return title of article.
     *
     * @param articleType {@link ArticleType}.
     * @param articleId   {@link Long} id of an article.
     * @return article title {@link User}.
     */
    protected String getArticleTitle(ArticleType articleType, Long articleId, Locale locale) {
        String articleName;
        switch (articleType) {
            case HABIT -> {
                Habit habit = habitRepo.findById(articleId)
                    .orElseThrow(() -> new NotFoundException(HABIT_NOT_FOUND_BY_ID + articleId));
                HabitTranslation habitTranslation =
                    habitTranslationRepo.findByHabitAndLanguageCode(habit, locale.getLanguage())
                        .orElseThrow(() -> new NotFoundException(
                            ErrorMessage.HABIT_TRANSLATION_NOT_FOUND + articleId));
                articleName = habitTranslation.getName();
            }
            case ECO_NEWS -> {
                EcoNews ecoNews = ecoNewsRepo.findById(articleId)
                    .orElseThrow(() -> new NotFoundException(ECO_NEW_NOT_FOUND_BY_ID + articleId));
                articleName = ecoNews.getTitle();
            }
            case EVENT -> {
                Event event = eventRepo.findById(articleId)
                    .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND_BY_ID + articleId));
                articleName = event.getTitle();
            }
            default -> throw new BadRequestException(ErrorMessage.UNSUPPORTED_ARTICLE_TYPE);
        }
        return articleName;
    }

    /**
     * Determines the appropriate notification type based on article and action
     * type.
     *
     * @param articleType the type of the article, {@link ArticleType}.
     * @param actionType  the type of action (COMMENT, REPLY, TAG).
     * @return the corresponding {@link NotificationType}.
     */
    private NotificationType getNotificationType(ArticleType articleType, CommentActionType actionType) {
        return switch (articleType) {
            case HABIT -> switch (actionType) {
                case COMMENT -> NotificationType.HABIT_COMMENT;
                case COMMENT_LIKE -> NotificationType.HABIT_COMMENT_LIKE;
                case COMMENT_REPLY -> NotificationType.HABIT_COMMENT_REPLY;
                case COMMENT_USER_TAG -> NotificationType.HABIT_COMMENT_USER_TAG;
                default -> throw new BadRequestException(ErrorMessage.UNSUPPORTED_ACTION_TYPE);
            };
            case ECO_NEWS -> switch (actionType) {
                case COMMENT -> NotificationType.ECONEWS_COMMENT;
                case COMMENT_LIKE -> NotificationType.ECONEWS_COMMENT_LIKE;
                case COMMENT_REPLY -> NotificationType.ECONEWS_COMMENT_REPLY;
                case COMMENT_USER_TAG -> NotificationType.ECONEWS_COMMENT_USER_TAG;
                default -> throw new BadRequestException(ErrorMessage.UNSUPPORTED_ACTION_TYPE);
            };
            case EVENT -> switch (actionType) {
                case COMMENT -> NotificationType.EVENT_COMMENT;
                case COMMENT_LIKE -> NotificationType.EVENT_COMMENT_LIKE;
                case COMMENT_REPLY -> NotificationType.EVENT_COMMENT_REPLY;
                case COMMENT_USER_TAG -> NotificationType.EVENT_COMMENT_USER_TAG;
                default -> throw new BadRequestException(ErrorMessage.UNSUPPORTED_ACTION_TYPE);
            };
            default -> throw new BadRequestException(ErrorMessage.UNSUPPORTED_ARTICLE_TYPE);
        };
    }

    /**
     * Creates a notification for a comment on an article.
     *
     * @param articleType the type of the article, {@link ArticleType}.
     * @param articleId   the ID of the article, {@link Long}.
     * @param userVO      the user who made the comment, {@link UserVO}.
     * @param locale      the locale used for localization of the notification,
     *                    {@link Locale}.
     */
    private void createCommentNotification(ArticleType articleType, Long articleId, UserVO userVO, Locale locale) {
        UserVO receiver = modelMapper.map(getArticleAuthor(articleType, articleId), UserVO.class);
        long commentsCount = notificationRepo
            .countActionUsersByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(receiver.getId(),
                getNotificationType(articleType, CommentActionType.COMMENT), articleId);
        String message = (commentsCount >= 1) ? (commentsCount + 1) + " COMMENTS" : "COMMENT";
        userNotificationService.createNotification(
            receiver,
            userVO,
            getNotificationType(articleType, CommentActionType.COMMENT),
            articleId,
            message,
            getArticleTitle(articleType, articleId, locale));
    }

    /**
     * Creates a notification for a comment like on an article.
     *
     * @param articleType the type of the article, {@link ArticleType}.
     * @param articleId   the ID of the article, {@link Long}.
     * @param comment     the comment that was made, {@link Comment}.
     * @param actionUser  the user who made the comment, {@link UserVO}.
     * @param locale      the locale used for localization of the notification,
     *                    {@link Locale}.
     */
    private void createCommentLikeNotification(ArticleType articleType, Long articleId, Comment comment,
        UserVO actionUser, Locale locale) {
        UserVO targetUser = modelMapper.map(comment.getUser(), UserVO.class);
        userNotificationService.createOrUpdateLikeNotification(LikeNotificationDto.builder()
            .targetUserVO(targetUser)
            .actionUserVO(actionUser)
            .newsId(articleId)
            .newsTitle(comment.getText())
            .notificationType(getNotificationType(articleType, CommentActionType.COMMENT_LIKE))
            .isLike(true)
            .secondMessageId(comment.getId())
            .secondMessageText(getArticleTitle(articleType, comment.getArticleId(), locale))
            .build());
    }

    /**
     * Creates a notification for a reply to a comment.
     *
     * @param articleType the type of the article, {@link ArticleType}.
     * @param articleId   the ID of the article, {@link Long}.
     * @param comment     the comment that was made, {@link Comment}.
     * @param receiver    the user who receive a notification, {@link UserVO}.
     * @param locale      the locale used for localization of the notification,
     *                    {@link Locale}.
     */
    private void createCommentReplyNotification(ArticleType articleType, Long articleId, Comment comment,
        UserVO sender, UserVO receiver, Locale locale) {
        long replyCount = notificationRepo
            .countUnviewedRepliesByTargetAndParent(
                receiver.getId(),
                getNotificationType(articleType, CommentActionType.COMMENT_REPLY),
                articleId,
                comment.getParentComment().getId());
        String message = (replyCount >= 1) ? (replyCount + 1) + " REPLIES" : "REPLY";
        userNotificationService.createNotification(
            receiver,
            sender,
            getNotificationType(articleType, CommentActionType.COMMENT_REPLY),
            articleId,
            message,
            comment.getParentComment().getId(),
            getArticleTitle(articleType, articleId, locale));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentDto getCommentById(ArticleType type, Long id, UserVO userVO) {
        Comment comment = commentRepo.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.COMMENT_NOT_FOUND_BY_ID + id));

        if (comment.getArticleType() != type) {
            throw new BadRequestException("Comment with id: " + id + " doesn't belong to " + type.getLink());
        }

        return convertToCommentDto(comment, userVO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<CommentDto> getAllActiveReplies(Pageable pageable, Long parentCommentId, UserVO userVO) {
        Comment parentComment = commentRepo.findById(parentCommentId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.COMMENT_NOT_FOUND_BY_ID + parentCommentId));
        Page<Comment> pages =
            commentRepo.findAllByParentCommentIdAndStatusNotOrderByCreatedDateDesc(pageable, parentComment.getId(),
                CommentStatus.DELETED);

        return convertPagesToCommentDtos(pages, userVO);
    }

    /**
     * Converts a {@link Page} of {@link Comment} entities into a
     * {@link PageableDto} containing {@link CommentDto} objects.
     *
     * @param pages  the {@link Page} of {@link Comment} entities to be converted.
     * @param userVO the {@link UserVO} representing the current user, used to
     *               determine if the user has liked each comment. This may be
     *               {@code null} if the current user's information is not
     *               available.
     * @return a {@link PageableDto} of {@link CommentDto} containing the mapped
     *         {@link CommentDto} objects.
     */
    public PageableDto<CommentDto> convertPagesToCommentDtos(Page<Comment> pages, UserVO userVO) {
        List<CommentDto> commentDtos = pages.getContent().stream().map(c -> convertToCommentDto(c, userVO)).toList();

        return new PageableDto<>(
            commentDtos,
            pages.getTotalElements(),
            pages.getPageable().getPageNumber(),
            pages.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countCommentsForHabit(Long habitId) {
        Habit habit = habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(HABIT_NOT_FOUND_BY_ID + habitId));
        return commentRepo.countNotDeletedCommentsByHabit(habit.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countCommentsForEcoNews(Long ecoNewsId) {
        EcoNews ecoNews = ecoNewsRepo.findById(ecoNewsId)
            .orElseThrow(() -> new NotFoundException(ECO_NEW_NOT_FOUND_BY_ID + ecoNewsId));
        return commentRepo.countNotDeletedCommentsByEcoNews(ecoNews.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countCommentsForEvent(Long eventId) {
        Event event = eventRepo.findById(eventId)
            .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND_BY_ID + eventId));
        return commentRepo.countNotDeletedCommentsByEvent(event.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countAllActiveReplies(Long parentCommentId) {
        if (commentRepo.findByIdAndStatusNot(parentCommentId, CommentStatus.DELETED).isEmpty()) {
            throw new NotFoundException(ErrorMessage.COMMENT_NOT_FOUND_BY_ID + parentCommentId);
        }
        return commentRepo.countByParentCommentIdAndStatusNot(parentCommentId, CommentStatus.DELETED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<CommentDto> getAllActiveComments(Pageable pageable, UserVO userVO, Long articleId,
        ArticleType articleType) {
        checkArticleExists(articleType, articleId);

        Page<Comment> pages =
            commentRepo.findAllByParentCommentIdIsNullAndArticleIdAndArticleTypeAndStatusNotOrderByCreatedDateDesc(
                pageable, articleId, articleType, CommentStatus.DELETED);

        return convertPagesToCommentDtos(pages, userVO);
    }

    /**
     * Converts a {@link Comment} entity into a {@link CommentDto} data transfer
     * object.
     *
     * @param comment the {@link Comment} entity to be converted.
     * @param user    the {@link UserVO} representing the current user, which is
     *                used to determine if the current user has liked the comment.
     * @return a {@link CommentDto} that contains the mapped information from the
     *         provided {@link Comment} entity.
     */
    private CommentDto convertToCommentDto(Comment comment, UserVO user) {
        CommentDto commentDto = modelMapper.map(comment, CommentDto.class);
        if (user != null) {
            commentDto.setCurrentUserLiked(comment.getUsersLiked().stream()
                .anyMatch(u -> u.getId().equals(user.getId())));
            commentDto.setCurrentUserDisliked(comment.getUsersDisliked().stream()
                .anyMatch(u -> u.getId().equals(user.getId())));
        }
        if (comment.getParentComment() != null) {
            commentDto.setParentCommentId(comment.getParentComment().getId());
        }
        commentDto.setAuthor(modelMapper.map(comment.getUser(), CommentAuthorDto.class));
        commentDto.setLikes(comment.getUsersLiked().size());
        commentDto.setDislikes(comment.getUsersDisliked().size());
        commentDto.setReplies(comment.getComments().size());
        commentDto.setStatus(comment.getStatus().name());
        return commentDto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void like(Long commentId, UserVO userVO, Locale locale) {
        Comment comment = commentRepo.findByIdAndStatusNot(commentId, CommentStatus.DELETED)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.COMMENT_NOT_FOUND_BY_ID + commentId));

        boolean isAuthor = comment.getUser().getId().equals(userVO.getId());

        if (isAuthor) {
            throw new BadRequestException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }

        if (removeLikeIfExists(comment, userVO)) {
            return;
        }

        removeDislikeIfExists(comment, userVO);

        User mappedUser = modelMapper.map(userVO, User.class);
        if (mappedUser.equals(comment.getUser())) {
            return;
        }

        comment.getUsersLiked().add(mappedUser);
        achievementCalculation.calculateAchievement(userVO,
            AchievementCategoryType.LIKE_COMMENT_OR_REPLY, AchievementAction.ASSIGN);
        ratingCalculation.ratingCalculation(ratingPointsRepo.findByNameOrThrow("LIKE_COMMENT_OR_REPLY"), userVO);
        createCommentLikeNotification(comment.getArticleType(), comment.getArticleId(), comment, userVO, locale);

        commentRepo.save(comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dislike(Long commentId, UserVO userVO, Locale locale) {
        Comment comment = commentRepo.findByIdAndStatusNot(commentId, CommentStatus.DELETED)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.COMMENT_NOT_FOUND_BY_ID + commentId));

        boolean isAuthor = comment.getUser().getId().equals(userVO.getId());

        if (isAuthor) {
            throw new BadRequestException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }

        removeLikeIfExists(comment, userVO);

        if (removeDislikeIfExists(comment, userVO)) {
            return;
        }

        comment.getUsersDisliked().add(modelMapper.map(userVO, User.class));

        commentRepo.save(comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void countLikes(AmountCommentLikesDto amountCommentLikesDto) {
        Comment comment = commentRepo.findByIdAndStatusNot(amountCommentLikesDto.getId(), CommentStatus.DELETED)
            .orElseThrow(
                () -> new NotFoundException(
                    ErrorMessage.COMMENT_NOT_FOUND_BY_ID + amountCommentLikesDto.getId()));
        boolean isLiked = comment.getUsersLiked().stream().map(User::getId)
            .anyMatch(x -> x.equals(amountCommentLikesDto.getUserId()));
        int size = comment.getUsersLiked().size();
        amountCommentLikesDto.setLiked(isLiked);
        amountCommentLikesDto.setAmountLikes(size);
        messagingTemplate
            .convertAndSend("/topic/" + amountCommentLikesDto.getId() + "/comment", amountCommentLikesDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void update(String commentText, Long id, UserVO userVO) {
        Comment comment = commentRepo.findByIdAndStatusNot(id, CommentStatus.DELETED)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.COMMENT_NOT_FOUND_EXCEPTION));

        if (!userVO.getId().equals(comment.getUser().getId())) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.NOT_A_CURRENT_USER);
        }

        comment.setText(commentText);
        comment.setStatus(CommentStatus.EDITED);
        commentRepo.save(comment);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void delete(Long id, UserVO userVO) {
        Comment comment = commentRepo
            .findByIdAndStatusNot(id, CommentStatus.DELETED)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.COMMENT_NOT_FOUND_BY_ID + id));

        if (userVO.getRole() != Role.ROLE_ADMIN && !userVO.getId().equals(comment.getUser().getId())) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }
        comment.setStatus(CommentStatus.DELETED);
        if (comment.getComments() != null) {
            comment.getComments()
                .forEach(c -> c.setStatus(CommentStatus.DELETED));
        }
        ratingCalculation.ratingCalculation(ratingPointsRepo.findByNameOrThrow("UNDO_COMMENT_OR_REPLY"), userVO);
        achievementCalculation.calculateAchievement(userVO,
            AchievementCategoryType.COMMENT_OR_REPLY, AchievementAction.DELETE);

        commentRepo.save(comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public void searchUsers(UserSearchDto searchUsers) {
        List<User> users = userRepo.searchUsers(searchUsers.getSearchQuery());

        List<UserTagDto> usersToTag = users.stream()
            .map(u -> modelMapper.map(u, UserTagDto.class))
            .toList();

        messagingTemplate.convertAndSend("/topic/" + searchUsers.getCurrentUserId() + "/searchUsers", usersToTag);
    }

    /**
     * Method to check if article exist.
     *
     * @param articleType {@link ArticleType}.
     * @param articleId   {@link Long} id of an article.
     * @throws NotFoundException with message for give {@link ArticleType}
     */
    void checkArticleExists(ArticleType articleType, Long articleId) {
        switch (articleType) {
            case HABIT -> {
                Optional<Habit> habit = habitRepo.findById(articleId);
                if (habit.isEmpty()) {
                    throw new NotFoundException(HABIT_NOT_FOUND_BY_ID + articleId);
                }
            }
            case ECO_NEWS -> {
                Optional<EcoNews> ecoNews = ecoNewsRepo.findById(articleId);
                if (ecoNews.isEmpty()) {
                    throw new NotFoundException(ECO_NEW_NOT_FOUND_BY_ID + articleId);
                }
            }
            case EVENT -> {
                Optional<Event> event = eventRepo.findById(articleId);
                if (event.isEmpty()) {
                    throw new NotFoundException(EVENT_NOT_FOUND_BY_ID + articleId);
                }
            }
            default -> throw new BadRequestException("Unsupported article type");
        }
    }

    /**
     * Method to extract user id from comment.
     *
     * @param message comment from {@link AddCommentDtoResponse}.
     * @return user id if present or null.
     */
    public Set<Long> getUserIdFromComment(String message) {
        String regEx = "data-userid=\"(\\d+)\"";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(message);
        Set<Long> userIds = new HashSet<>();
        if (!matcher.find()) {
            return userIds;
        }
        matcher.reset();
        while (matcher.find()) {
            userIds.add(Long.valueOf(matcher.group(1)));
        }
        return userIds;
    }

    /**
     * Removes a like from the comment if the user has already liked it. Returns
     * true if a like was removed, false otherwise.
     */
    private boolean removeLikeIfExists(Comment comment, UserVO userVO) {
        boolean userLiked = comment.getUsersLiked().stream()
            .anyMatch(user -> user.getId().equals(userVO.getId()));

        if (userLiked) {
            comment.getUsersLiked().removeIf(user -> user.getId().equals(userVO.getId()));
            achievementCalculation.calculateAchievement(userVO, AchievementCategoryType.LIKE_COMMENT_OR_REPLY,
                AchievementAction.DELETE);
            ratingCalculation.ratingCalculation(ratingPointsRepo.findByNameOrThrow("UNDO_LIKE_COMMENT_OR_REPLY"),
                userVO);

            userNotificationService.removeActionUserFromNotification(modelMapper.map(comment.getUser(), UserVO.class),
                userVO, comment.getId(), getNotificationType(comment.getArticleType(), CommentActionType.COMMENT_LIKE));

            return true;
        }
        return false;
    }

    /**
     * Removes a dislike from the comment if the user has already disliked it.
     * Returns true if a dislike was removed, false otherwise.
     */
    private boolean removeDislikeIfExists(Comment comment, UserVO userVO) {
        boolean userDisliked = comment.getUsersDisliked().stream()
            .anyMatch(user -> user.getId().equals(userVO.getId()));

        if (userDisliked) {
            comment.getUsersDisliked().removeIf(user -> user.getId().equals(userVO.getId()));
            achievementCalculation.calculateAchievement(userVO, AchievementCategoryType.LIKE_COMMENT_OR_REPLY,
                AchievementAction.DELETE);
            ratingCalculation.ratingCalculation(ratingPointsRepo.findByNameOrThrow("UNDO_LIKE_COMMENT_OR_REPLY"),
                userVO);

            userNotificationService.removeActionUserFromNotification(modelMapper.map(comment.getUser(), UserVO.class),
                userVO, comment.getId(), getNotificationType(comment.getArticleType(), CommentActionType.COMMENT_LIKE));
            return true;
        }
        return false;
    }
}
