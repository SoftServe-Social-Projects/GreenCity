package greencity.service;

import greencity.dto.habit.HabitAssignDto;
import greencity.dto.user.UserVO;
import greencity.enums.InvitationStatus;
import java.util.List;

public interface HabitInvitationService {
    /**
     * Retrieves a list of IDs representing friends who are associated with a
     * specific habit assignment, either as invitees or inviters. This method
     * consolidates information about users who have been invited by or have invited
     * the specified user for the given habit assignment.
     *
     * @param userId        The ID of the user for whom to retrieve friend
     *                      invitations.
     * @param habitAssignId The ID of the habit assignment being tracked.
     * @return A list of unique user IDs representing friends associated with the
     *         given habit assignment.
     */
    List<Long> getInvitedFriendsIdsTrackingHabitList(Long userId, Long habitAssignId);

    /**
     * Retrieves a list of {@link HabitAssignDto} objects representing the habit
     * assignments of friends who are associated with a specific habit assignment,
     * either as invitees or inviters. This method provides detailed data about
     * habit assignments related to the specified user.
     *
     * @param userId        The ID of the user for whom to retrieve associated habit
     *                      assignments.
     * @param habitAssignId The ID of the habit assignment being tracked.
     * @return A list of unique {@link HabitAssignDto} objects representing the
     *         associated habit assignments.
     */
    List<HabitAssignDto> getHabitAssignsTrackingHabitList(Long userId, Long habitAssignId);

    /**
     * Accepts a habit invitation for a user.
     *
     * @param invitationId the ID of the habit invitation to be accepted
     * @param invitedUser  the user who is accepting the invitation
     */
    void acceptHabitInvitation(Long invitationId, UserVO invitedUser);

    /**
     * Rejects a habit invitation for a user.
     *
     * @param invitationId the ID of the habit invitation to be rejected
     * @param invitedUser  the user who is rejecting the invitation
     */
    void rejectHabitInvitation(Long invitationId, UserVO invitedUser);

    /**
     * Retrieves a status of the invitation.
     *
     * @param invitationId the ID of the habit invitation.
     * @return the {@link InvitationStatus} of the habit invitation.
     */
    InvitationStatus getHabitInvitationStatus(Long invitationId);
}
