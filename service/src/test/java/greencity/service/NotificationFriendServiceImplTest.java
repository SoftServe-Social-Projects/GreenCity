package greencity.service;

import greencity.TestConst;
import greencity.constant.FriendTupleConstant;
import greencity.enums.InvitationStatus;
import greencity.repository.UserRepo;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationFriendServiceImplTest {
    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private NotificationFriendServiceImpl notificationFriendService;

    private final Long currentUserId = 1L;
    private final Long friendId = 2L;

    @Test
    void getFriendRequestStatusReturnsCanceledWhenStatusIsNullTest() {
        when(userRepo.getFriendRequestStatus(currentUserId, friendId)).thenReturn(Optional.empty());

        InvitationStatus result = notificationFriendService.getFriendRequestStatus(currentUserId, friendId);

        assertEquals(InvitationStatus.CANCELED, result);
        verify(userRepo).getFriendRequestStatus(currentUserId, friendId);
    }

    @Test
    void getFriendRequestStatusReturnsPendingWhenStatusIsRequestTest() {
        when(userRepo.getFriendRequestStatus(currentUserId, friendId)).
            thenReturn(Optional.of(FriendTupleConstant.REQUEST_STATUS));

        InvitationStatus result = notificationFriendService.getFriendRequestStatus(currentUserId, friendId);

        assertEquals(InvitationStatus.PENDING, result);
    }

    @Test
    void getFriendRequestStatusReturnsAcceptedWhenStatusIsFriendTest() {
        when(userRepo.getFriendRequestStatus(currentUserId, friendId))
            .thenReturn(Optional.of(FriendTupleConstant.FRIEND_STATUS));

        InvitationStatus result = notificationFriendService.getFriendRequestStatus(currentUserId, friendId);

        assertEquals(InvitationStatus.ACCEPTED, result);
    }

    @Test
    void getFriendRequestStatusReturnsRejectedWhenStatusIsRejectedTest() {
        when(userRepo.getFriendRequestStatus(currentUserId, friendId))
            .thenReturn(Optional.of(FriendTupleConstant.REJECTED_STATUS));

        InvitationStatus result = notificationFriendService.getFriendRequestStatus(currentUserId, friendId);

        assertEquals(InvitationStatus.REJECTED, result);
    }

    @Test
    void getFriendRequestStatusReturnsCanceledWhenStatusIsUnknownTest() {
        when(userRepo.getFriendRequestStatus(currentUserId, friendId))
            .thenReturn(Optional.of(TestConst.UNKNOWN_STATUS));

        InvitationStatus result = notificationFriendService.getFriendRequestStatus(currentUserId, friendId);

        assertEquals(InvitationStatus.CANCELED, result);
    }

    @Test
    void getFriendRequestStatusHandlesInvalidUserIds() {
        when(userRepo.getFriendRequestStatus(-1L, friendId)).thenReturn(Optional.empty());

        InvitationStatus result = notificationFriendService.getFriendRequestStatus(-1L, friendId);
        assertEquals(InvitationStatus.CANCELED, result);
        verify(userRepo).getFriendRequestStatus(-1L, friendId);
    }
}