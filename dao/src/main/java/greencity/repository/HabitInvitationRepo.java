package greencity.repository;

import greencity.entity.HabitAssign;
import greencity.entity.HabitInvitation;
import greencity.enums.HabitInvitationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;

@Repository
public interface HabitInvitationRepo extends JpaRepository<HabitInvitation, Long> {
    /**
     * Finds a list of {@link HabitInvitation} objects where the specified habit
     * assignment is associated with the inviter. This allows retrieving all
     * invitations sent by a particular user's habit assignment.
     *
     * @param inviterHabitAssignId The ID of the habit assignment for the inviter.
     * @return A list of {@link HabitInvitation} objects linked to the specified
     *         inviter's habit assignment.
     */
    List<HabitInvitation> findByInviterHabitAssignId(Long inviterHabitAssignId);

    /**
     * Finds a list of {@link HabitInvitation} objects where the specified habit
     * assignment is associated with the invitee. This allows retrieving all
     * invitations received by a particular user's habit assignment.
     *
     * @param inviteeHabitAssignId The ID of the habit assignment for the invitee.
     * @return A list of {@link HabitInvitation} objects linked to the specified
     *         invitee's habit assignment.
     */
    List<HabitInvitation> findByInviteeHabitAssignId(Long inviteeHabitAssignId);

    boolean existsByInviterHabitAssign(HabitAssign inviterHabitAssign);

    boolean existsByInviteeHabitAssign(HabitAssign inviteeHabitAssign);

    Page<HabitInvitation> findByInviteeIdAndStatusIn(Long inviteeId, Collection<HabitInvitationStatus> statuses,
        Pageable pageable);
}
