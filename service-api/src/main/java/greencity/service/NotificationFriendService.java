package greencity.service;

import greencity.enums.InvitationStatus;

public interface NotificationFriendService {
    /**
     * Retrieves the status of a friend request between the current user and a friend.
     *
     * @param currentUserId the ID of the current user.
     * @param friendId      the ID of the friend.
     * @return the {@link InvitationStatus} of the friend request.
     */
    InvitationStatus getFriendRequestStatus(Long currentUserId, Long friendId);
}
