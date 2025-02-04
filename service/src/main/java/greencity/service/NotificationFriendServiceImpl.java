package greencity.service;

import greencity.constant.FriendTupleConstant;
import greencity.enums.InvitationStatus;
import greencity.repository.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link NotificationFriendService}. Needed for
 * {@link UserNotificationService}
 */
@Service
@AllArgsConstructor
public class NotificationFriendServiceImpl implements NotificationFriendService {
    private final UserRepo userRepo;

    /**
     * {@inheritDoc}
     */
    @Override
    public InvitationStatus getFriendRequestStatus(Long currentUserId, Long friendId) {
        String status = userRepo.getFriendRequestStatus(currentUserId, friendId);
        if (status == null) {
            return InvitationStatus.CANCELED;
        }

        return switch (status) {
            case FriendTupleConstant.REQUEST_STATUS -> InvitationStatus.PENDING;
            case FriendTupleConstant.FRIEND_STATUS -> InvitationStatus.ACCEPTED;
            case FriendTupleConstant.REJECTED_STATUS -> InvitationStatus.REJECTED;
            default -> InvitationStatus.CANCELED;
        };
    }
}
