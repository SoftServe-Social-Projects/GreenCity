package greencity.repository;

import greencity.dto.friends.UserFriendHabitInviteDto;
import greencity.entity.HabitAssign;
import greencity.entity.HabitInvitation;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
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

    boolean existsByInviterHabitAssignAndInviteeHabitAssign(HabitAssign inviterHabitAssign, HabitAssign habitAssign);

    /**
     * Retrieves a paginated list of a user's friends with optional name filtering.
     * Each friend is represented as a {@link UserFriendHabitInviteDto}, including a
     * {@code hasInvitation} flag indicating if the friend has a pending habit
     * invitation for the specified habit. Friends without invitations will have
     * {@code hasInvitation} set to {@code false}.
     *
     * @param userId   the ID of the user whose friends are retrieved.
     * @param name     an optional case-insensitive name filter.
     * @param habitId  the ID of the habit to check for pending invitations.
     * @param pageable pagination information for the result.
     * @return a {@link Page} of {@link UserFriendHabitInviteDto}.
     */
    @Query(nativeQuery = true, value = """
        WITH friends AS (
            SELECT DISTINCT
                CASE WHEN friend_id = :userId THEN user_id ELSE friend_id END AS id
            FROM users_friends
            WHERE (user_id = :userId OR friend_id = :userId) AND status = 'FRIEND'
        ),
        filtered_friends AS (
            SELECT u.id,
                   u.name,
                   u.email,
                   u.profile_picture
            FROM users u
            WHERE u.id IN (SELECT id FROM friends)
              AND (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')))
        ),
        relevant_habit_assignments AS (
            SELECT DISTINCT ha.id AS habit_assign_id,
                            ha.user_id AS friend_id
            FROM habit_assign ha
            WHERE ha.habit_id = :habitId AND ha.status IN ('REQUESTED', 'INPROGRESS')
        ),
        invitations AS (
            SELECT DISTINCT
                CASE
                    WHEN i.inviter_id = :userId THEN i.invitee_id
                    ELSE i.inviter_id
                END AS friend_id,
                CASE
                    WHEN i.status = 'ACCEPTED' THEN TRUE
                    ELSE FALSE
                END AS has_accepted_invitation,
                i.status IN ('PENDING', 'ACCEPTED') AS has_invitation
            FROM habit_invitations i
            WHERE i.status IN ('PENDING', 'ACCEPTED')
              AND (
                  (i.inviter_id = :userId AND i.inviter_habit_assign_id
                        IN (SELECT habit_assign_id FROM relevant_habit_assignments))
                  OR
                  (i.invitee_id = :userId AND i.inviter_habit_assign_id
                        IN (SELECT habit_assign_id FROM relevant_habit_assignments))
              )
        )
        SELECT f.id,
               f.name,
               f.email,
               f.profile_picture,
               COALESCE(inv.has_invitation, FALSE) AS has_invitation,
               COALESCE(inv.has_accepted_invitation, FALSE) AS has_accepted_invitation
        FROM filtered_friends f
        LEFT JOIN invitations inv ON f.id = inv.friend_id
        """)
    List<Tuple> findUserFriendsWithHabitInvites(
        Long userId, String name, Long habitId, Pageable pageable);
}
