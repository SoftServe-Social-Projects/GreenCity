package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.friends.UserFriendDto;
import greencity.dto.habit.HabitAssignDto;
import greencity.dto.habit.HabitDto;
import greencity.dto.habit.HabitInvitationDto;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.dto.user.UserVO;
import greencity.entity.HabitAssign;
import greencity.entity.HabitInvitation;
import greencity.entity.HabitTranslation;
import greencity.entity.User;
import greencity.enums.HabitAssignStatus;
import greencity.enums.HabitInvitationStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static greencity.ModelUtils.getHabitAssign;
import static greencity.ModelUtils.getUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
        habitInvitation1.setInviterHabitAssign(habitAssign1).setStatus(HabitInvitationStatus.ACCEPTED);
        HabitInvitation habitInvitation2 = new HabitInvitation();
        habitInvitation2.setInviteeHabitAssign(habitAssign2).setStatus(HabitInvitationStatus.ACCEPTED);

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
        Long invitationId = 1L;
        UserVO invitedUser = new UserVO();
        invitedUser.setId(3L);

        User user1 = new User();
        user1.setId(3L);
        HabitAssign habitAssign1 = new HabitAssign();
        habitAssign1.setUser(user1).setId(1L).setStatus(HabitAssignStatus.REQUESTED);

        HabitInvitation habitInvitation = new HabitInvitation();
        habitInvitation.setId(invitationId);
        habitInvitation.setInviteeHabitAssign(habitAssign1);
        habitInvitation.setStatus(HabitInvitationStatus.PENDING);

        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.of(habitInvitation));
        when(habitAssignRepo.findById(habitAssign1.getId())).thenReturn(Optional.of(habitAssign1));
        habitInvitationService.acceptHabitInvitation(invitationId, invitedUser);

        assertEquals(HabitInvitationStatus.ACCEPTED, habitInvitation.getStatus());
        verify(habitInvitationRepo).save(habitInvitation);
    }

    @Test
    void testAcceptHabitInvitation_HabitAssignNotFound() {
        Long invitationId = 1L;
        UserVO invitedUser = new UserVO();
        invitedUser.setId(3L);

        User user1 = new User();
        user1.setId(3L);
        HabitAssign habitAssign1 = new HabitAssign();
        habitAssign1.setUser(user1);

        HabitInvitation habitInvitation = new HabitInvitation();
        habitInvitation.setId(invitationId);
        habitInvitation.setInviteeHabitAssign(habitAssign1);
        habitInvitation.setStatus(HabitInvitationStatus.PENDING);

        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.of(habitInvitation));
        when(habitAssignRepo.findById(habitAssign1.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            habitInvitationService.acceptHabitInvitation(invitationId, invitedUser);
        });

        assertEquals(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssign1.getId(), exception.getMessage());
    }

    @Test
    void testAcceptHabitInvitation_NoInvitation() {
        Long invitationId = 1L;
        UserVO invitedUser = new UserVO();
        invitedUser.setId(3L);

        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            habitInvitationService.acceptHabitInvitation(invitationId, invitedUser);
        });

        assertEquals(ErrorMessage.INVITATION_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testAcceptHabitInvitation_NotAllowedToAcceptHabitInvitation() {
        Long invitationId = 1L;
        UserVO invitedUser = new UserVO();
        invitedUser.setId(4L);

        User user1 = new User();
        user1.setId(3L);
        HabitAssign habitAssign1 = new HabitAssign();
        habitAssign1.setUser(user1);

        HabitInvitation habitInvitation = new HabitInvitation();
        habitInvitation.setId(invitationId);
        habitInvitation.setInviteeHabitAssign(habitAssign1);
        habitInvitation.setStatus(HabitInvitationStatus.PENDING);

        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.of(habitInvitation));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            habitInvitationService.acceptHabitInvitation(invitationId, invitedUser);
        });

        assertEquals(ErrorMessage.CANNOT_ACCEPT_HABIT_INVITATION, exception.getMessage());
    }

    @Test
    void testAcceptHabitInvitation_AlreadyAcceptedHabitInvitation() {
        Long invitationId = 1L;
        UserVO invitedUser = new UserVO();
        invitedUser.setId(4L);
        HabitAssign habitAssign1 = new HabitAssign().setUser(new User().setId(4L));

        HabitInvitation habitInvitation = new HabitInvitation();
        habitInvitation.setId(invitationId);
        habitInvitation.setInviteeHabitAssign(habitAssign1);
        habitInvitation.setStatus(HabitInvitationStatus.ACCEPTED);

        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.of(habitInvitation));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            habitInvitationService.acceptHabitInvitation(invitationId, invitedUser);
        });

        assertEquals(ErrorMessage.YOU_HAS_ALREADY_ACCEPT_THIS_INVITATION, exception.getMessage());
    }

    @Test
    void testRejectHabitInvitation() {
        Long invitationId = 1L;
        UserVO invitedUser = new UserVO();
        invitedUser.setId(3L);

        User user1 = new User();
        user1.setId(3L);
        HabitAssign habitAssign1 = new HabitAssign();
        habitAssign1.setUser(user1);

        HabitInvitation habitInvitation = new HabitInvitation();
        habitInvitation.setId(invitationId);
        habitInvitation.setInviteeHabitAssign(habitAssign1);
        habitInvitation.setStatus(HabitInvitationStatus.PENDING);

        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.of(habitInvitation));

        habitInvitationService.rejectHabitInvitation(invitationId, invitedUser);

        verify(habitInvitationRepo).delete(habitInvitation);
    }

    @Test
    void testRejectHabitInvitation_NoInvitation() {
        Long invitationId = 1L;
        UserVO invitedUser = new UserVO();
        invitedUser.setId(3L);

        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            habitInvitationService.rejectHabitInvitation(invitationId, invitedUser);
        });

        assertEquals(ErrorMessage.INVITATION_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testRejectHabitInvitation_NotAllowedToRejectHabitInvitation() {
        Long invitationId = 1L;
        UserVO invitedUser = new UserVO();
        invitedUser.setId(4L);

        User user1 = new User();
        user1.setId(3L);
        HabitAssign habitAssign1 = new HabitAssign();
        habitAssign1.setUser(user1);

        HabitInvitation habitInvitation = new HabitInvitation();
        habitInvitation.setId(invitationId);
        habitInvitation.setInviteeHabitAssign(habitAssign1);
        habitInvitation.setStatus(HabitInvitationStatus.PENDING);

        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.of(habitInvitation));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            habitInvitationService.rejectHabitInvitation(invitationId, invitedUser);
        });

        assertEquals(ErrorMessage.CANNOT_REJECT_HABIT_INVITATION, exception.getMessage());
    }

    @Test
    void testRejectHabitInvitation_NotPendingStatusInvitation() {
        Long invitationId = 1L;
        UserVO invitedUser = new UserVO();
        invitedUser.setId(3L);

        User user1 = new User();
        user1.setId(3L);
        HabitAssign habitAssign1 = new HabitAssign();
        habitAssign1.setUser(user1);

        HabitInvitation habitInvitation = new HabitInvitation();
        habitInvitation.setId(invitationId);
        habitInvitation.setInviteeHabitAssign(habitAssign1);
        habitInvitation.setStatus(HabitInvitationStatus.ACCEPTED);

        when(habitInvitationRepo.findById(invitationId)).thenReturn(Optional.of(habitInvitation));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            habitInvitationService.rejectHabitInvitation(invitationId, invitedUser);
        });

        assertEquals(ErrorMessage.CANNOT_REJECT_HABIT_INVITATION, exception.getMessage());
    }

    @Test
    void testGetAllUserHabitInvitationRequestsNoRequests() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<HabitInvitation> invitations = new PageImpl<>(Arrays.asList(), pageable, 0);

        when(habitInvitationRepo.findByInviteeIdAndStatusIn(userId,
            Collections.singleton(HabitInvitationStatus.PENDING), pageable)).thenReturn(invitations);

        PageableAdvancedDto<HabitInvitationDto> result =
            habitInvitationService.getAllUserHabitInvitationRequests(userId, "en", pageable);

        verify(habitInvitationRepo, times(1)).findByInviteeIdAndStatusIn(eq(userId),
            eq(Collections.singleton(HabitInvitationStatus.PENDING)), eq(pageable));
        assertNotNull(result);
        assertEquals(0, result.getPage().size());
    }

    @Test
    void testGetAllUserHabitInvitationRequests() {
        Pageable pageable = PageRequest.of(0, 10);

        HabitInvitation habitInvitation = HabitInvitation.builder()
            .id(1L)
            .inviter(getUser())
            .inviteeHabitAssign(getHabitAssign())
            .status(HabitInvitationStatus.PENDING)
            .build();

        HabitDto habitDto = HabitDto.builder()
            .habitTranslation(HabitTranslationDto.builder()
                .description("Some description")
                .name("Some Habit")
                .build())
            .build();

        UserFriendDto userFriendDto = UserFriendDto.builder()
            .id(1L)
            .name("Taras")
            .build();

        Page<HabitInvitation> invitations = new PageImpl<>(Collections.singletonList(habitInvitation), pageable, 1);

        when(habitInvitationRepo.findByInviteeIdAndStatusIn(userId,
            Collections.singleton(HabitInvitationStatus.PENDING), pageable))
            .thenReturn(invitations);

        when(modelMapper.map(any(HabitTranslation.class), eq(HabitDto.class)))
            .thenReturn(habitDto);

        when(modelMapper.map(any(User.class), eq(UserFriendDto.class)))
            .thenReturn(userFriendDto);

        PageableAdvancedDto<HabitInvitationDto> result =
            habitInvitationService.getAllUserHabitInvitationRequests(userId, "en", pageable);

        verify(habitInvitationRepo, times(1)).findByInviteeIdAndStatusIn(eq(userId),
            eq(Collections.singleton(HabitInvitationStatus.PENDING)), eq(pageable));

        assertNotNull(result);
        assertEquals(1, result.getPage().size());
        assertEquals(1L, result.getPage().get(0).invitationId());
        assertEquals("Taras", result.getPage().get(0).inviter().getName());
        assertEquals("Some Habit", result.getPage().get(0).habit().getHabitTranslation().getName());
        assertEquals("Some description", result.getPage().get(0).habit().getHabitTranslation().getDescription());
    }
}
