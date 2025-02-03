package greencity.service;

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
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class HabitInvitationServiceImpl implements HabitInvitationService {
    private HabitInvitationRepo habitInvitationRepo;
    private HabitAssignRepo habitAssignRepo;
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

    /**
     * {@inheritDoc}
     */
    @Transactional
    public void acceptHabitInvitation(Long invitationId, UserVO invitedUser) {
        HabitInvitation invitation = habitInvitationRepo.findById(invitationId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.INVITATION_NOT_FOUND));

        if (!invitation.getInviteeHabitAssign().getUser().getId().equals(invitedUser.getId())) {
            throw new BadRequestException(ErrorMessage.CANNOT_ACCEPT_HABIT_INVITATION);
        }

        if (InvitationStatus.ACCEPTED.equals(invitation.getStatus())) {
            throw new BadRequestException(ErrorMessage.YOU_HAS_ALREADY_ACCEPT_THIS_INVITATION);
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        habitInvitationRepo.save(invitation);

        HabitAssign habitAssign = habitAssignRepo.findById(invitation.getInviteeHabitAssign().getId())
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + invitation.getInviteeHabitAssign().getId()));

        if (habitAssign.getStatus().equals(HabitAssignStatus.REQUESTED)) {
            habitAssign.setStatus(HabitAssignStatus.INPROGRESS);
            habitAssignRepo.save(habitAssign);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    public void rejectHabitInvitation(Long invitationId, UserVO invitedUser) {
        HabitInvitation invitation = habitInvitationRepo.findById(invitationId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.INVITATION_NOT_FOUND));

        if (!invitation.getInviteeHabitAssign().getUser().getId().equals(invitedUser.getId())
            || !InvitationStatus.PENDING.equals(invitation.getStatus())) {
            throw new BadRequestException(ErrorMessage.CANNOT_REJECT_HABIT_INVITATION);
        }
        habitInvitationRepo.delete(invitation);
        checkAndDeleteHabitAssignIfUnused(invitation.getInviteeHabitAssign());
        checkAndDeleteHabitAssignIfUnused(invitation.getInviterHabitAssign());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvitationStatus getHabitInvitationStatus(Long invitationId) {
        return habitInvitationRepo.findById(invitationId)
            .map(HabitInvitation::getStatus)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.INVITATION_NOT_FOUND));
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
            .filter(hi -> InvitationStatus.ACCEPTED.equals(hi.getStatus()))
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
            .filter(hi -> InvitationStatus.ACCEPTED.equals(hi.getStatus()))
            .map(HabitInvitation::getInviteeHabitAssign)
            .filter(habitAssign -> !habitAssign.getUser().getId().equals(currentUserId))
            .collect(Collectors.toList());
    }

    private void checkAndDeleteHabitAssignIfUnused(HabitAssign habitAssign) {
        boolean isHabitAssignStillUsedAsInvitee = habitInvitationRepo.existsByInviteeHabitAssign((habitAssign));
        boolean isHabitAssignStillUsedAsInviter = habitInvitationRepo.existsByInviterHabitAssign((habitAssign));

        if (!isHabitAssignStillUsedAsInvitee && !isHabitAssignStillUsedAsInviter) {
            habitAssignRepo.delete(habitAssign);
        }
    }
}
