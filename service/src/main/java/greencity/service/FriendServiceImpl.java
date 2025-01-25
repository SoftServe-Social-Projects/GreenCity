package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.constant.FriendTupleConstant;
import greencity.dto.PageableDto;
import greencity.dto.friends.UserAsFriendDto;
import greencity.dto.friends.UserFriendDto;
import greencity.dto.user.UserManagementDto;
import greencity.dto.user.UserVO;
import greencity.entity.User;
import greencity.entity.UserLocation;
import greencity.enums.NotificationType;
import greencity.enums.RecommendedFriendsType;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotDeletedException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UnsupportedSortException;
import greencity.repository.CustomUserRepo;
import greencity.repository.UserRepo;
import java.util.Collections;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation of {@link FriendService}.
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class FriendServiceImpl implements FriendService {
    private final UserRepo userRepo;
    private final CustomUserRepo customUserRepo;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;
    private final UserNotificationService userNotificationService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteUserFriendById(long userId, long friendId) {
        validateUserAndFriendNotSamePerson(userId, friendId);
        validateUserAndFriendExistence(userId, friendId);
        validateFriends(userId, friendId);
        userRepo.deleteUserFriendById(userId, friendId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void addNewFriend(long userId, long friendId) {
        validateUserAndFriendNotSamePerson(userId, friendId);
        validateUserAndFriendExistence(userId, friendId);
        validateFriendRequestNotSent(userId, friendId);
        validateFriendNotExists(userId, friendId);
        userRepo.addNewFriend(userId, friendId);
        User emailReceiver = userRepo.getReferenceById(friendId);
        User friendRequestSender = userRepo.getReferenceById(userId);
        userNotificationService.createNotification(modelMapper.map(emailReceiver, UserVO.class),
            modelMapper.map(friendRequestSender, UserVO.class), NotificationType.FRIEND_REQUEST_RECEIVED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void acceptFriendRequest(long userId, long friendId) {
        validateUserAndFriendNotSamePerson(userId, friendId);
        validateUserAndFriendExistence(userId, friendId);
        validateFriendNotExists(userId, friendId);
        validateFriendRequestSentByFriend(userId, friendId);
        userRepo.acceptFriendRequest(userId, friendId);
        User user = userRepo.getReferenceById(userId);
        User friend = userRepo.getReferenceById(friendId);
        userNotificationService.createNotification(modelMapper.map(friend, UserVO.class),
            modelMapper.map(user, UserVO.class), NotificationType.FRIEND_REQUEST_ACCEPTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void declineFriendRequest(long userId, long friendId) {
        validateUserAndFriendNotSamePerson(userId, friendId);
        validateUserAndFriendExistence(userId, friendId);
        validateFriendNotExists(userId, friendId);
        validateFriendRequestSentByFriend(userId, friendId);
        userRepo.declineFriendRequest(userId, friendId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<UserManagementDto> findUserFriendsByUserId(Pageable pageable, long userId) {
        validateUserExistence(userId);
        Page<User> friends;
        if (pageable.getSort().isEmpty()) {
            friends = userRepo.getAllUserFriendsPage(pageable, userId);
        } else {
            throw new UnsupportedSortException(ErrorMessage.INVALID_SORTING_VALUE);
        }
        List<UserManagementDto> friendList =
            friends.stream().map(friend -> modelMapper.map(friend, UserManagementDto.class))
                .collect(Collectors.toList());
        return new PageableDto<>(
            friendList,
            friends.getTotalElements(),
            friends.getPageable().getPageNumber(),
            friends.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<UserFriendDto> findUserFriendsByUserIAndShowFriendStatusRelatedToCurrentUser(Pageable pageable,
        long userId, long currentUserId) {
        validateUserExistence(userId);
        Page<User> friends;
        if (pageable.getSort().isEmpty()) {
            friends = userRepo.getAllUserFriendsCollectingBySpecificConditionsAndCertainOrder(pageable, userId);
        } else {
            throw new UnsupportedSortException(ErrorMessage.INVALID_SORTING_VALUE);
        }

        List<UserFriendDto> userFriendDtoList =
            customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(currentUserId,
                friends.getContent());

        return new PageableDto<>(
            userFriendDtoList,
            friends.getTotalElements(),
            friends.getPageable().getPageNumber(),
            friends.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<UserFriendDto> findAllUsersExceptMainUserAndUsersFriendAndRequestersToMainUser(long userId,
        String name,
        boolean filterByFriendsOfFriends,
        boolean filterByCity,
        Pageable pageable) {
        Objects.requireNonNull(pageable);
        validateUserExistence(userId);
        name = name != null ? name : "";

        Page<User> users;
        if (pageable.getSort().isEmpty()) {
            users = userRepo.getAllUsersExceptMainUserAndFriendsAndRequestersToMainUser(userId, name,
                filterByFriendsOfFriends, filterByCity, pageable);
        } else {
            throw new UnsupportedSortException(ErrorMessage.INVALID_SORTING_VALUE);
        }
        List<UserFriendDto> userFriendDtoList =
            customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId, users.getContent());
        return new PageableDto<>(
            userFriendDtoList,
            users.getTotalElements(),
            users.getPageable().getPageNumber(),
            users.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<UserFriendDto> findRecommendedFriends(long userId, RecommendedFriendsType type,
        Pageable pageable) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID));
        validateUserExistence(userId);
        Page<User> mutualFriends;
        if (type == RecommendedFriendsType.FRIENDS_OF_FRIENDS) {
            mutualFriends = userRepo.getRecommendedFriendsOfFriends(userId, pageable);
        } else if (type == RecommendedFriendsType.HABITS) {
            mutualFriends = userRepo.findRecommendedFriendsByHabits(userId, pageable);
        } else if (type == RecommendedFriendsType.CITY) {
            UserLocation userLocation = user.getUserLocation();
            if (userLocation != null && userLocation.getCityUa() != null) {
                mutualFriends = userRepo.findRecommendedFriendsByCity(userId, userLocation.getCityUa(), pageable);
            } else {
                return new PageableDto<>(Collections.emptyList(), 0, pageable.getPageNumber(), 0);
            }
        } else {
            mutualFriends = getAllUsersExceptMainUserAndFriends(userId, pageable);
        }
        List<UserFriendDto> userFriendDtoList =
            customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId,
                mutualFriends.getContent());
        return new PageableDto<>(
            userFriendDtoList,
            mutualFriends.getTotalElements(),
            mutualFriends.getPageable().getPageNumber(),
            mutualFriends.getTotalPages());
    }

    @Override
    public PageableDto<UserFriendDto> getMutualFriends(Long userId, Long friendId, Pageable pageable) {
        validateUserAndFriends(userId, friendId);
        Page<User> users =
            userRepo.getMutualFriends(userId, friendId, pageable);
        List<UserFriendDto> userFriendDtoList =
            customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId, users.getContent());
        return new PageableDto<>(
            userFriendDtoList,
            users.getTotalElements(),
            users.getPageable().getPageNumber(),
            users.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<UserFriendDto> getAllUserFriendRequests(long userId, String name, boolean filterByCity,
        Pageable pageable) {
        Objects.requireNonNull(pageable);

        validateUserExistence(userId);
        Page<User> users = userRepo.getAllUserFriendRequests(userId, name, filterByCity, pageable);
        List<UserFriendDto> userFriendDtoList =
            customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId, users.getContent());
        return new PageableDto<>(
            userFriendDtoList,
            users.getTotalElements(),
            users.getPageable().getPageNumber(),
            users.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<UserFriendDto> findAllFriendsOfUser(long userId,
        String name,
        boolean filterByCity,
        Pageable pageable) {
        Objects.requireNonNull(pageable);
        validateUserExistence(userId);

        Page<User> users;
        if (pageable.getSort().isEmpty()) {
            users = userRepo.findAllFriendsOfUser(userId, name, filterByCity, pageable);
        } else {
            throw new UnsupportedSortException(ErrorMessage.INVALID_SORTING_VALUE);
        }
        List<UserFriendDto> userFriendDtoList =
            customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId, users.getContent());
        return new PageableDto<>(
            userFriendDtoList,
            users.getTotalElements(),
            users.getPageable().getPageNumber(),
            users.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteRequestOfCurrentUserToFriend(long userId, long friendId) {
        validateUserAndFriendNotSamePerson(userId, friendId);
        validateUserAndFriendExistence(userId, friendId);
        validateFriendNotExists(userId, friendId);
        validateFriendRequestSentByCurrentUser(userId, friendId);
        userRepo.canselUserRequestToFriend(userId, friendId);
    }

    private Page<User> getAllUsersExceptMainUserAndFriends(long userId, Pageable pageable) {
        return userRepo.getAllUsersExceptMainUserAndFriends(userId, "", pageable);
    }

    @Override
    public UserAsFriendDto getUserAsFriend(Long currentUserId, Long friendId) {
        validateUserExistence(friendId);
        return getUserAsFriendDto(currentUserId, friendId);
    }

    private UserAsFriendDto getUserAsFriendDto(Long id, Long friendId) {
        var tuple = userRepo.findUsersFriendByUserIdAndFriendId(id, friendId);
        var chatId = userRepo.findIdOfPrivateChatOfUsers(id, friendId);
        var userAsFriend = new UserAsFriendDto(friendId, chatId);

        if (Objects.nonNull(tuple)) {
            userAsFriend.setFriendStatus(tuple.get(FriendTupleConstant.STATUS, String.class));
            userAsFriend.setRequesterId(tuple.get(FriendTupleConstant.REQUESTER_ID, Long.class));
        }
        return userAsFriend;
    }

    private void validateUserAndFriends(Long userId, Long friendId) {
        validateUserAndFriendExistence(userId, friendId);
        validateUserAndFriendNotSamePerson(userId, friendId);
    }

    private void validateUserAndFriendExistence(long userId, long friendId) {
        validateUserExistence(userId);
        validateUserExistence(friendId);
    }

    private void validateUserExistence(long userId) {
        if (!userRepo.existsById(userId)) {
            throw new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + userId);
        }
    }

    private void validateFriendRequestNotSent(long userId, long friendId) {
        if (userRepo.isFriendRequested(userId, friendId)) {
            throw new BadRequestException(ErrorMessage.FRIEND_REQUEST_ALREADY_SENT);
        }
    }

    private void validateFriendNotExists(long userId, long friendId) {
        if (userRepo.isFriend(userId, friendId)) {
            throw new BadRequestException(ErrorMessage.FRIEND_EXISTS + friendId);
        }
    }

    private void validateFriendRequestSentByFriend(long userId, long friendId) {
        if (!userRepo.isFriendRequestedByCurrentUser(friendId, userId)) {
            throw new NotFoundException(ErrorMessage.FRIEND_REQUEST_NOT_SENT);
        }
    }

    private void validateFriendRequestSentByCurrentUser(long userId, long friendId) {
        if (!userRepo.isFriendRequestedByCurrentUser(userId, friendId)) {
            throw new NotFoundException(ErrorMessage.FRIEND_REQUEST_NOT_SENT);
        }
    }

    private void validateFriends(long userId, long friendId) {
        if (!userRepo.isFriend(userId, friendId)) {
            throw new NotDeletedException(ErrorMessage.USER_FRIENDS_LIST + friendId);
        }
    }

    private void validateUserAndFriendNotSamePerson(long userId, long friendId) {
        if (userId == friendId) {
            throw new BadRequestException(ErrorMessage.OWN_USER_ID + friendId);
        }
    }
}
