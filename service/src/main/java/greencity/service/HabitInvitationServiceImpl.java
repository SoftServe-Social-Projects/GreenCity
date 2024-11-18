package greencity.service;

import greencity.dto.habit.HabitAssignDto;
import greencity.entity.HabitAssign;
import greencity.entity.HabitInvitation;
import greencity.entity.User;
import greencity.repository.HabitInvitationRepo;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class HabitInvitationServiceImpl implements HabitInvitationService {
    private HabitInvitationRepo habitInvitationRepo;
    private ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> getInvitedFriendsIdsTrackingHabitList(Long userId, Long habitAssignId) {
        return Stream.concat(
            getUsersIdWhoInvitedMe(userId, habitAssignId).stream(),
            getUsersIdWhoIHaveInvited(userId, habitAssignId).stream())
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HabitAssignDto> getHabitAssignsTrackingHabitList(Long userId, Long habitAssignId) {
        return Stream.concat(
            getHabitAssignsWhoInvitedMe(userId, habitAssignId).stream(),
            getHabitAssignsWhoIHaveInvited(userId, habitAssignId).stream())
            .distinct()
            .map(ha -> modelMapper.map(ha, HabitAssignDto.class))
            .collect(Collectors.toList());
    }

    private List<Long> getUsersIdWhoInvitedMe(Long currentUserId, Long habitAssignId) {
        return habitInvitationRepo.findByInviteeHabitAssignId(habitAssignId).stream()
            .map(HabitInvitation::getInviterHabitAssign)
            .map(HabitAssign::getUser)
            .map(User::getId)
            .filter(id -> !id.equals(currentUserId))
            .collect(Collectors.toList());
    }

    private List<Long> getUsersIdWhoIHaveInvited(Long currentUserId, Long habitAssignId) {
        return habitInvitationRepo.findByInviterHabitAssignId(habitAssignId).stream()
            .map(HabitInvitation::getInviteeHabitAssign)
            .map(HabitAssign::getUser)
            .map(User::getId)
            .filter(id -> !id.equals(currentUserId))
            .collect(Collectors.toList());
    }

    private List<HabitAssign> getHabitAssignsWhoInvitedMe(Long currentUserId, Long habitAssignId) {
        return habitInvitationRepo.findByInviteeHabitAssignId(habitAssignId).stream()
            .map(HabitInvitation::getInviterHabitAssign)
            .filter(habitAssign -> !habitAssign.getUser().getId().equals(currentUserId))
            .collect(Collectors.toList());
    }

    private List<HabitAssign> getHabitAssignsWhoIHaveInvited(Long currentUserId, Long habitAssignId) {
        return habitInvitationRepo.findByInviterHabitAssignId(habitAssignId).stream()
            .map(HabitInvitation::getInviteeHabitAssign)
            .filter(habitAssign -> !habitAssign.getUser().getId().equals(currentUserId))
            .collect(Collectors.toList());
    }
}
