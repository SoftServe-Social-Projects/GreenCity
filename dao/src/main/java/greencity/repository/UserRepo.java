package greencity.repository;

import greencity.dto.friends.UserFriendDto;
import greencity.dto.habit.HabitVO;
import greencity.dto.user.UserManagementVO;
import greencity.dto.user.UserVO;
import greencity.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    /**
     * Find {@link User} by email.
     *
     * @param email user email.
     * @return {@link User}
     */
    Optional<User> findByEmail(String email);

    /**
     * Find all {@link UserManagementVO}.
     *
     * @param filter   filter parameters
     * @param pageable pagination
     * @return list of all {@link UserManagementVO}
     */
    @Query(" SELECT new greencity.dto.user.UserManagementVO(u.id, u.name, u.email, u.userCredo, u.role, u.userStatus) "
        + " FROM User u ")
    Page<UserManagementVO> findAllManagementVo(Specification<User> filter, Pageable pageable);

    /**
     * Find not 'DEACTIVATED' {@link User} by email.
     *
     * @param email - {@link User}'s email
     * @return found {@link User}
     * @author Vasyl Zhovnir
     */
    @Query("FROM User WHERE email=:email AND userStatus <> 1")
    Optional<User> findNotDeactivatedByEmail(String email);

    /**
     * Find id by email.
     *
     * @param email - User email
     * @return User id
     * @author Zakhar Skaletskyi
     */
    @Query("SELECT id FROM User WHERE email=:email")
    Optional<Long> findIdByEmail(String email);

    /**
     * Updates last activity time for a given user.
     *
     * @param userId               - {@link User}'s id
     * @param userLastActivityTime - new {@link User}'s last activity time
     * @author Yurii Zhurakovskyi
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE User SET last_activity_time=:userLastActivityTime WHERE id=:userId")
    void updateUserLastActivityTime(Long userId, Date userLastActivityTime);

    /**
     * Updates user status for a given user.
     *
     * @param userId     - {@link User}'s id
     * @param userStatus {@link String} - string value of user status to set
     */
    @Modifying
    @Transactional
    @Query("UPDATE User SET userStatus = CASE "
        + "WHEN (:userStatus = 'DEACTIVATED') THEN 1 "
        + "WHEN (:userStatus = 'ACTIVATED') THEN 2 "
        + "WHEN (:userStatus = 'CREATED') THEN 3 "
        + "WHEN (:userStatus = 'BLOCKED') THEN 4 "
        + "ELSE 0 END "
        + "WHERE id = :userId")
    void updateUserStatus(Long userId, String userStatus);

    /**
     * Updates user role for a given user.
     *
     * @param userId   - {@link User}'s id
     * @param userRole {@link String} - string value of user role to set
     */
    @Modifying
    @Transactional
    @Query("UPDATE User SET role = CASE "
        + "WHEN (:userRole = 'ROLE_USER') THEN 0 "
        + "WHEN (:userRole = 'ROLE_ADMIN') THEN 1 "
        + "WHEN (:userRole = 'ROLE_MODERATOR') THEN 2 "
        + "ELSE 3 END "
        + "WHERE id = :userId")
    void updateUserRole(Long userId, String userRole);

    /**
     * Find the last activity time by {@link User}'s id.
     *
     * @param userId - {@link User}'s id
     * @return {@link Date}
     */
    @Query(nativeQuery = true,
        value = "SELECT last_activity_time FROM users WHERE id=:userId")
    Optional<Timestamp> findLastActivityTimeById(Long userId);

    /**
     * Get six friends with the highest rating {@link User}.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM users WHERE users.id IN ( "
        + "(SELECT user_id FROM users_friends WHERE friend_id = :userId AND status = 'FRIEND') "
        + "UNION (SELECT friend_id FROM users_friends WHERE user_id = :userId AND status = 'FRIEND')) "
        + "ORDER BY users.rating DESC LIMIT 6;")
    List<User> getSixFriendsWithTheHighestRating(Long userId);

    /**
     * Updates user rating as event organizer.
     *
     * @param userId {@link User}'s id
     * @param rate   new {@link User}'s rating as event organizer
     * @author Danylo Hlynskyi
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE User SET eventOrganizerRating=:rate WHERE id=:userId")
    void updateUserEventOrganizerRating(Long userId, Double rate);

    /**
     * Find user by user id and friend id when their status is Friend.
     *
     * @param userId   {@link Long} user id
     * @param friendId {@link Long} friend id
     * @return {@link Optional} of {@link User}
     * @author Julia Seti
     */
    @Query(nativeQuery = true, value = "SELECT DISTINCT * FROM users AS u "
        + "WHERE u.id = "
        + "((SELECT user_id FROM users_friends "
        + "WHERE user_id = :userId AND friend_id = :friendId AND status = 'FRIEND') "
        + "UNION "
        + "(SELECT friend_id FROM users_friends "
        + "WHERE user_id = :friendId AND friend_id = :userId AND status = 'FRIEND'))")
    Optional<User> findUserByIdAndByFriendId(Long userId, Long friendId);

    /**
     * Retrieves the list of the user's friends (which have INPROGRESS assign to the
     * habit).
     *
     * @param habitId {@link HabitVO} id.
     * @param userId  {@link UserVO} id.
     * @return List of friends.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM ((SELECT user_id FROM users_friends AS uf "
        + "WHERE uf.friend_id = :userId AND uf.status = 'FRIEND' AND "
        + "(SELECT count(*) FROM habit_assign ha WHERE ha.habit_id = :habitId AND ha.user_id = uf.user_id "
        + "AND ha.status = 'INPROGRESS') = 1) "
        + "UNION "
        + "(SELECT friend_id FROM users_friends AS uf "
        + "WHERE uf.user_id = :userId AND uf.status = 'FRIEND' AND "
        + "(SELECT count(*) FROM habit_assign ha WHERE ha.habit_id = :habitId AND ha.user_id = uf.friend_id "
        + "AND ha.status = 'INPROGRESS') = 1)) as ui JOIN users as u ON user_id = u.id")
    List<User> getFriendsAssignedToHabit(Long userId, Long habitId);

    /**
     * Delete friend {@link User}.
     */
    @Modifying
    @Query(nativeQuery = true,
        value = "DELETE FROM users_friends WHERE (user_id = :userId AND friend_id = :friendId)"
            + " OR (user_id = :friendId AND friend_id = :userId)")
    void deleteUserFriendById(Long userId, Long friendId);

    /**
     * Checks if a user is a friend of another user.
     *
     * @param userId   The ID of the user to check if they are a friend.
     * @param friendId The ID of the potential friend.
     * @return {@code true} if the user is a friend of the other user, {@code false}
     *         otherwise.
     */
    @Query(nativeQuery = true,
        value = "SELECT EXISTS(SELECT * FROM users_friends WHERE status = 'FRIEND' AND ("
            + "user_id = :userId AND friend_id = :friendId OR "
            + "user_id = :friendId AND friend_id = :userId))")
    boolean isFriend(Long userId, Long friendId);

    /**
     * Checks if a friend request exists between two users.
     *
     * @param userId   The ID of the user.
     * @param friendId The ID of the friend.
     * @return {@code true} if a friend request exists between the two users,
     *         {@code false} otherwise.
     */
    @Query(nativeQuery = true,
        value = "SELECT EXISTS(SELECT * FROM users_friends WHERE status = 'REQUEST' AND ("
            + "user_id = :userId AND friend_id = :friendId OR "
            + "user_id = :friendId AND friend_id = :userId))")
    boolean isFriendRequested(Long userId, Long friendId);

    /**
     * <<<<<<< HEAD Checks if a friend requested by current user with userId.
     *
     * @param userId   The ID of the user.
     * @param friendId The ID of the friend.
     * @return {@code true} if a friend requested by current user, {@code false}
     *         otherwise.
     */
    @Query(nativeQuery = true,
        value = "SELECT EXISTS(SELECT * FROM users_friends WHERE status = 'REQUEST' AND "
            + "user_id = :userId AND friend_id = :friendId)")
    boolean isFriendRequestedByCurrentUser(Long userId, Long friendId);

    /**
     * ======= >>>>>>> dev Adds a new friend for a user.
     *
     * @param userId   The ID of the user.
     * @param friendId The ID of the friend to be added.
     */
    @Modifying
    @Query(nativeQuery = true,
        value = "INSERT INTO users_friends(user_id, friend_id, status, created_date) "
            + "VALUES (:userId, :friendId, 'REQUEST', CURRENT_TIMESTAMP)")
    void addNewFriend(Long userId, Long friendId);

    /**
     * Accept friend request.
     *
     * @param userId   The ID of the user.
     * @param friendId The ID of the friend to be added.
     */
    @Modifying
    @Query(nativeQuery = true,
        value = "UPDATE users_friends SET status = 'FRIEND' "
            + "WHERE user_id = :friendId AND friend_id = :userId")
    void acceptFriendRequest(Long userId, Long friendId);

    /**
     * Decline friend request.
     *
     * @param userId   The ID of the user.
     * @param friendId The ID of the friend to be declined.
     */
    @Modifying
    @Query(nativeQuery = true,
        value = "DELETE FROM users_friends WHERE user_id = :friendId AND friend_id = :userId")
    void declineFriendRequest(Long userId, Long friendId);

    /**
     * Get all user friends.
     *
     * @param userId The ID of the user.
     *
     * @return list of {@link User}.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM users WHERE id IN ( "
        + "(SELECT user_id FROM users_friends WHERE friend_id = :userId and status = 'FRIEND')"
        + "UNION (SELECT friend_id FROM users_friends WHERE user_id = :userId and status = 'FRIEND'));")
    List<User> getAllUserFriends(Long userId);

    /**
     * Method that finds all users except current user and his friends.
     *
     * @param pageable current page.
     * @param userId   current user's id.
     * @param friends  {@link List} of {@link User} which are user friends.
     * @param name     name filter.
     *
     * @return {@link Slice} of {@link UserFriendDto}.
     */
    @Query(nativeQuery = true, name = "User.getAllUsersExceptMainUserAndFriends")
    Slice<UserFriendDto> getAllUsersExceptMainUserAndFriends(Pageable pageable, Long userId,
        List<User> friends, String name);

    /**
     * Get count of not user friends.
     *
     * @param userId id of the user.
     * @param name   name filter.
     *
     * @return {@link Long} count of not user friends.
     */
    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM users WHERE id NOT IN ( "
        + "(SELECT user_id FROM users_friends WHERE friend_id = :userId and status = 'FRIEND')"
        + "UNION (SELECT friend_id FROM users_friends WHERE user_id = :userId and status = 'FRIEND')) "
        + "AND LOWER(name) LIKE LOWER(CONCAT('%', :name, '%')) AND id != :userId")
    Long getCountOfNotUserFriends(Long userId, String name);
}