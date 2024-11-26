package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.friends.UserFriendDto;
import greencity.dto.habit.HabitAssignDto;
import greencity.dto.habit.HabitDto;
import greencity.dto.habit.HabitInvitationDto;
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
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
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

        if (HabitInvitationStatus.ACCEPTED.equals(invitation.getStatus())) {
            throw new BadRequestException(ErrorMessage.YOU_HAS_ALREADY_ACCEPT_THIS_INVITATION);
        }

        invitation.setStatus(HabitInvitationStatus.ACCEPTED);
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
            || !HabitInvitationStatus.PENDING.equals(invitation.getStatus())) {
            throw new BadRequestException(ErrorMessage.CANNOT_REJECT_HABIT_INVITATION);
        }
        habitInvitationRepo.delete(invitation);
        checkAndDeleteHabitAssignIfUnused(invitation.getInviteeHabitAssign());
        checkAndDeleteHabitAssignIfUnused(invitation.getInviterHabitAssign());
    }

    // todo add java doc.
    @Override
    public PageableAdvancedDto<HabitInvitationDto> getAllUserHabitInvitationRequests(Long userId, String language,
        Pageable pageable) {
        Page<HabitInvitation> invitations = habitInvitationRepo.findByInviteeIdAndStatusIn(userId,
            Collections.singleton(HabitInvitationStatus.PENDING), pageable);
        return buildPageableAdvancedGenericDto(invitations, language);
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
            .filter(hi -> hi.getStatus().equals(HabitInvitationStatus.ACCEPTED))
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

    private PageableAdvancedDto<HabitInvitationDto> buildPageableAdvancedGenericDto(
        Page<HabitInvitation> invitationPage,
        String language) {
        List<HabitInvitationDto> invitationDtos = invitationPage.stream()
            .map(hi -> buildHabitInvitationDto(hi, language))
            .collect(Collectors.toList());

        return new PageableAdvancedDto<>(
            invitationDtos,
            invitationPage.getTotalElements(),
            invitationPage.getPageable().getPageNumber(),
            invitationPage.getTotalPages(),
            invitationPage.getNumber(),
            invitationPage.hasPrevious(),
            invitationPage.hasNext(),
            invitationPage.isFirst(),
            invitationPage.isLast());
    }

    private HabitInvitationDto buildHabitInvitationDto(HabitInvitation habitInvitation, String language) {
        HabitDto habitDto =
            modelMapper.map(getHabitTranslation(habitInvitation.getInviteeHabitAssign(), language), HabitDto.class);
        UserFriendDto inviter = modelMapper.map(habitInvitation.getInviter(), UserFriendDto.class);
        return new HabitInvitationDto(
            habitInvitation.getId(),
            inviter,
            habitInvitation.getStatus().toString(),
            habitDto);
    }

    private HabitTranslation getHabitTranslation(HabitAssign habitAssign, String language) {
        return habitAssign.getHabit().getHabitTranslations().stream()
            .filter(ht -> ht.getLanguage().getCode().equals(language)).findFirst()
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_TRANSLATION_NOT_FOUND + habitAssign.getHabit().getId()));
    }
}
