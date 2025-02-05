package greencity.service;

import greencity.ModelUtils;
import greencity.constant.ErrorMessage;
import greencity.dto.habit.HabitAssignDto;
import greencity.dto.user.UserVO;
import greencity.entity.HabitAssign;
import greencity.entity.HabitInvitation;
import greencity.entity.User;
import greencity.enums.HabitAssignStatus;
import greencity.enums.InvitationStatus;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.HabitAssignRepo;
import greencity.repository.HabitInvitationRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HabitInvitationServiceImplTest {
    @Mock
    private HabitInvitationRepo habitInvitationRepo;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    HabitAssignRepo habitAssignRepo;
    @InjectMocks
    private HabitInvitationServiceImpl habitInvitationService;

    private final Long userId = 1L;
    private final Long user2Id = 2L;
    private final Long invitationId = 1L;
    private final Long habitAssignId = 2L;

    @Test
    void testGetInvitedFriendsIdsTrackingHabitList() {
        User user1 = new User();
        user1.setId(3L);
        HabitAssign habitAssign1 = new HabitAssign();
        habitAssign1.setUser(user1);

        User user2 = new User();
        user2.setId(4L);
        HabitAssign habitAssign2 = new HabitAssign();
        habitAssign2.setUser(user2);

        HabitInvitation habitInvitation1 = new HabitInvitation();
        habitInvitation1.setInviterHabitAssign(habitAssign1);
        HabitInvitation habitInvitation2 = new HabitInvitation();
        habitInvitation2.setInviteeHabitAssign(habitAssign2);
        habitInvitation2.setStatus(InvitationStatus.ACCEPTED);

        when(habitInvitationRepo.findByInviteeHabitAssignId(habitAssignId)).thenReturn(List.of(habitInvitation1));
        when(habitInvitationRepo.findByInviterHabitAssignId(habitAssignId)).thenReturn(List.of(habitInvitation2));

        List<Long> result = habitInvitationService.getInvitedFriendsIdsTrackingHabitList(userId, habitAssignId);

        assertEquals(2, result.size());
        assertTrue(result.contains(3L));
        assertTrue(result.contains(4L));

        verify(habitInvitationRepo).findByInviteeHabitAssignId(habitAssignId);
        verify(habitInvitationRepo).findByInviterHabitAssignId(habitAssignId);
    }

    @Test
    void testGetHabitAssignsTrackingHabitList() {
        User user1 = new User();
        user1.setId(3L);
        HabitAssign habitAssign1 = new HabitAssign();
        habitAssign1.setUser(user1);

        User user2 = new User();
        user2.setId(4L);
        HabitAssign habitAssign2 = new HabitAssign();
        habitAssign2.setUser(user2);

        HabitInvitation habitInvitation1 = new HabitInvitation();
        habitInvitation1.setInviterHabitAssign(habitAssign1).setStatus(InvitationStatus.ACCEPTED);
        HabitInvitation habitInvitation2 = new HabitInvitation();
        habitInvitation2.setInviteeHabitAssign(habitAssign2).setStatus(InvitationStatus.ACCEPTED);

        when(habitInvitationRepo.findByInviteeHabitAssignId(habitAssignId)).thenReturn(List.of(habitInvitation1));
        when(habitInvitationRepo.findByInviterHabitAssignId(habitAssignId)).thenReturn(List.of(habitInvitation2));

        HabitAssignDto habitAssignDto1 = new HabitAssignDto();
        HabitAssignDto habitAssignDto2 = new HabitAssignDto();
        when(modelMapper.map(habitAssign1, HabitAssignDto.class)).thenReturn(habitAssignDto1);
        when(modelMapper.map(habitAssign2, HabitAssignDto.class)).thenReturn(habitAssignDto2);

        List<HabitAssignDto> result = habitInvitationService.getHabitAssignsTrackingHabitList(userId, habitAssignId);

        assertEquals(2, result.size());
        assertTrue(result.contains(habitAssignDto1));
        assertTrue(result.contains(habitAssignDto2));

        verify(habitInvitationRepo).findByInviteeHabitAssignId(habitAssignId);
        verify(habitInvitationRepo).findByInviterHabitAssignId(habitAssignId);
        verify(modelMapper).map(habitAssign1, HabitAssignDto.class);
        verify(modelMapper).map(habitAssign2, HabitAssignDto.class);
    }

    @Test
    void testGetInvitedFriendsIdsTrackingHabitList_NoInvitations() {
        when(habitInvitationRepo.findByInviteeHabitAssignId(habitAssignId)).thenReturn(List.of());
        when(habitInvitationRepo.findByInviterHabitAssignId(habitAssignId)).thenReturn(List.of());

        List<Long> result = habitInvitationService.getInvitedFriendsIdsTrackingHabitList(userId, habitAssignId);

        assertTrue(result.isEmpty());

        verify(habitInvitationRepo).findByInviteeHabitAssignId(habitAssignId);
        verify(habitInvitationRepo).findByInviterHabitAssignId(habitAssignId);
    }

    @Test
    void testGetHabitAssignsTrackingHabitList_NoHabitAssigns() {
        when(habitInvitationRepo.findByInviteeHabitAssignId(habitAssignId)).thenReturn(List.of());
        when(habitInvitationRepo.findByInviterHabitAssignId(habitAssignId)).thenReturn(List.of());

        List<HabitAssignDto> result = habitInvitationService.getHabitAssignsTrackingHabitList(userId, habitAssignId);

        assertTrue(result.isEmpty());

        verify(habitInvitationRepo).findByInviteeHabitAssignId(habitAssignId);
        verify(habitInvitationRepo).findByInviterHabitAssignId(habitAssignId);
        verify(modelMapper, never()).map(any(), eq(HabitAssignDto.class));
    }

    @Test
    void testAcceptHabitInvitation() {
        UserVO invitedUser = ModelUtils.getUserVO();
        invitedUser.setId(userId);

        User user1 = ModelUtils.getUser();
        user1.setId(userId);

        HabitAssign habitAssign1 = ModelUtils.getHabitAssignForMapper();
        habitAssign1.setUser(user1).setId(habitAssignId).setStatus(HabitAssignStatus.REQUESTED);

        HabitInvitation habitInvitation = ModelUtils.getHabitInvitation();
        habitInvitation.setId(invitationId);
        habitInvitation.setInviteeHabitAssign(habitAssign1);
        habitInvitation.setStatus(InvitationStatus.PENDING);

        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.of(habitInvitation));
        when(habitAssignRepo.findById(habitAssign1.getId())).thenReturn(Optional.of(habitAssign1));

        habitInvitationService.acceptHabitInvitation(invitationId, invitedUser);

        assertEquals(InvitationStatus.ACCEPTED, habitInvitation.getStatus());
        verify(habitInvitationRepo).save(habitInvitation);
    }

    @Test
    void testAcceptHabitInvitation_HabitAssignNotFound() {
        UserVO invitedUser = ModelUtils.getUserVO();
        invitedUser.setId(userId);

        User user1 = ModelUtils.getUser();
        user1.setId(userId);

        HabitAssign habitAssign1 = ModelUtils.getHabitAssignForMapper();
        habitAssign1.setUser(user1);

        HabitInvitation habitInvitation = ModelUtils.getHabitInvitation();
        habitInvitation.setId(invitationId);
        habitInvitation.setInviteeHabitAssign(habitAssign1);
        habitInvitation.setStatus(InvitationStatus.PENDING);

        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.of(habitInvitation));
        when(habitAssignRepo.findById(habitAssign1.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> habitInvitationService.acceptHabitInvitation(invitationId, invitedUser));

        assertEquals(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssign1.getId(), exception.getMessage());
    }

    @Test
    void testAcceptHabitInvitation_NoInvitation() {
        UserVO invitedUser = ModelUtils.getUserVO();

        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> habitInvitationService.acceptHabitInvitation(invitationId, invitedUser));

        assertEquals(ErrorMessage.INVITATION_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testAcceptHabitInvitation_NotAllowedToAcceptHabitInvitation() {
        UserVO invitedUser = ModelUtils.getUserVO();
        invitedUser.setId(user2Id);

        User user1 = ModelUtils.getUser();
        user1.setId(userId);
        HabitAssign habitAssign1 = ModelUtils.getHabitAssign();
        habitAssign1.setUser(user1);

        HabitInvitation habitInvitation = ModelUtils.getHabitInvitation();
        habitInvitation.setId(invitationId);
        habitInvitation.setInviteeHabitAssign(habitAssign1);
        habitInvitation.setStatus(InvitationStatus.PENDING);

        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.of(habitInvitation));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> habitInvitationService.acceptHabitInvitation(invitationId, invitedUser));

        assertEquals(ErrorMessage.CANNOT_ACCEPT_HABIT_INVITATION, exception.getMessage());
    }

    @Test
    void testAcceptHabitInvitation_AlreadyAcceptedHabitInvitation() {
        UserVO invitedUser = ModelUtils.getUserVO();
        invitedUser.setId(userId);
        User user1 = ModelUtils.getUser().setId(userId);
        HabitAssign habitAssign1 = ModelUtils.getHabitAssign().setUser(user1);

        HabitInvitation habitInvitation = ModelUtils.getHabitInvitation();
        habitInvitation.setId(invitationId);
        habitInvitation.setInviteeHabitAssign(habitAssign1);
        habitInvitation.setStatus(InvitationStatus.ACCEPTED);

        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.of(habitInvitation));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> habitInvitationService.acceptHabitInvitation(invitationId, invitedUser));

        assertEquals(ErrorMessage.YOU_HAS_ALREADY_ACCEPT_THIS_INVITATION, exception.getMessage());
    }

    @Test
    void testRejectHabitInvitation() {
        UserVO invitedUser = ModelUtils.getUserVO();
        invitedUser.setId(userId);

        User user1 = ModelUtils.getUser();
        user1.setId(userId);
        HabitAssign habitAssign1 = ModelUtils.getHabitAssign();
        habitAssign1.setUser(user1);

        HabitInvitation habitInvitation = ModelUtils.getHabitInvitation();
        habitInvitation.setId(invitationId);
        habitInvitation.setInviteeHabitAssign(habitAssign1);
        habitInvitation.setStatus(InvitationStatus.PENDING);

        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.of(habitInvitation));

        habitInvitationService.rejectHabitInvitation(invitationId, invitedUser);

        verify(habitInvitationRepo).delete(habitInvitation);
    }

    @Test
    void testRejectHabitInvitation_NoInvitation() {
        UserVO invitedUser = ModelUtils.getUserVO();

        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> habitInvitationService.rejectHabitInvitation(invitationId, invitedUser));

        assertEquals(ErrorMessage.INVITATION_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testRejectHabitInvitation_NotAllowedToRejectHabitInvitation() {
        UserVO invitedUser = ModelUtils.getUserVO();
        invitedUser.setId(user2Id);

        User user1 = ModelUtils.getUser();
        user1.setId(userId);
        HabitAssign habitAssign1 = ModelUtils.getHabitAssign();
        habitAssign1.setUser(user1);

        HabitInvitation habitInvitation = ModelUtils.getHabitInvitation();
        habitInvitation.setId(invitationId);
        habitInvitation.setInviteeHabitAssign(habitAssign1);
        habitInvitation.setStatus(InvitationStatus.PENDING);

        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.of(habitInvitation));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> habitInvitationService.rejectHabitInvitation(invitationId, invitedUser));

        assertEquals(ErrorMessage.CANNOT_REJECT_HABIT_INVITATION, exception.getMessage());
    }

    @Test
    void testRejectHabitInvitation_NotPendingStatusInvitation() {
        UserVO invitedUser = ModelUtils.getUserVO();
        invitedUser.setId(user2Id);

        User user1 = new User();
        user1.setId(userId);
        HabitAssign habitAssign1 = ModelUtils.getHabitAssign();
        habitAssign1.setUser(user1);

        HabitInvitation habitInvitation = ModelUtils.getHabitInvitation();
        habitInvitation.setId(invitationId);
        habitInvitation.setInviteeHabitAssign(habitAssign1);
        habitInvitation.setStatus(InvitationStatus.ACCEPTED);

        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.of(habitInvitation));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> habitInvitationService.rejectHabitInvitation(invitationId, invitedUser));

        assertEquals(ErrorMessage.CANNOT_REJECT_HABIT_INVITATION, exception.getMessage());
    }

    @Test
    void getHabitInvitationStatusReturnsStatusWhenExistsTest() {
        HabitInvitation habitInvitation = ModelUtils.getHabitInvitation();

        when(habitInvitationRepo.findById(habitInvitation.getId())).thenReturn(Optional.of(habitInvitation));

        InvitationStatus result = habitInvitationService.getHabitInvitationStatus(habitInvitation.getId());

        assertEquals(InvitationStatus.ACCEPTED, result);
    }

    @Test
    void getHabitInvitationStatusReturnsRejectedWhenNotExistsTest() {
        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.empty());

        InvitationStatus result = habitInvitationService.getHabitInvitationStatus(invitationId);

        assertEquals(InvitationStatus.REJECTED, result);
    }
}
