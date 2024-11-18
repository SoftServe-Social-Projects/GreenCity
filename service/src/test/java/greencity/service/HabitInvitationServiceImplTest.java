package greencity.service;

import greencity.dto.habit.HabitAssignDto;
import greencity.entity.HabitAssign;
import greencity.entity.HabitInvitation;
import greencity.entity.User;
import greencity.repository.HabitInvitationRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @InjectMocks
    private HabitInvitationServiceImpl habitInvitationService;

    private final Long userId = 1L;
    private final Long habitAssignId = 2L;

    @BeforeEach
    void setUp() {
        habitInvitationService = new HabitInvitationServiceImpl(habitInvitationRepo, modelMapper);
    }

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
        habitInvitation1.setInviterHabitAssign(habitAssign1);
        HabitInvitation habitInvitation2 = new HabitInvitation();
        habitInvitation2.setInviteeHabitAssign(habitAssign2);

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
}
