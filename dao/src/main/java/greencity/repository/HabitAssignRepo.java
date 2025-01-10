package greencity.repository;

import greencity.dto.habitstatistic.HabitStatusCount;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.User;
import greencity.enums.HabitAssignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitAssignRepo extends JpaRepository<HabitAssign, Long>,
    JpaSpecificationExecutor<HabitAssign> {
    /**
     * Method to find {@link HabitAssign} by id.
     *
     * @param id {@link HabitAssign} id.
     * @return {@link HabitAssign} instance.
     */
    @Query(value = "SELECT ha FROM HabitAssign ha"
        + " JOIN FETCH ha.habit h LEFT JOIN FETCH h.habitTranslations ht"
        + " JOIN FETCH ht.language l"
        + " WHERE ha.id = :id")
    Optional<HabitAssign> findById(@Param("id") Long id);

    /**
     * Method to find all {@link HabitAssign} by {@link User} id (not cancelled and
     * not expired).
     *
     * @param userId {@link User} id.
     * @return list of {@link HabitAssign} instances.
     */
    @Query(value = "SELECT DISTINCT ha FROM HabitAssign ha"
        + " JOIN FETCH ha.habit h JOIN FETCH h.habitTranslations ht"
        + " JOIN FETCH ht.language l"
        + " WHERE ha.user.id = :userId AND upper(ha.status) NOT IN ('CANCELLED','EXPIRED','REQUESTED')")
    List<HabitAssign> findAllByUserId(@Param("userId") Long userId);

    /**
     * Retrieves a paginated list of {@code HabitAssign} entities for a specified
     * user that are in progress or acquired. This method includes fetching
     * associated {@code Habit}, {@code HabitTranslations}, and {@code Language}
     * entities.
     *
     * @param userId   the ID of the {@code User} whose {@code HabitAssign} entities
     *                 are to be retrieved.
     * @param pageable the {@link Pageable} object containing pagination
     *                 information.
     * @return a {@link Page} of {@code HabitAssign} entities.
     */
    @Query("""
            SELECT ha FROM HabitAssign ha
            left join fetch ha.habit h
            left join fetch h.habitTranslations ht
            left join fetch ht.language l
            WHERE ha.user.id = :userId and (ha.status = 'INPROGRESS' OR ha.status = 'ACQUIRED')
            ORDER BY ha.createDate
        """)
    Page<HabitAssign> findAllByUserId(Long userId, Pageable pageable);

    /**
     * Method to find all {@link HabitAssign} by {@link Habit} id (not canceled and
     * not expired).
     *
     * @param habitId {@link Habit} id.
     * @return list of {@link HabitAssign} instances.
     */
    @Query(value = "SELECT DISTINCT ha FROM HabitAssign ha"
        + " JOIN FETCH ha.habit h JOIN FETCH h.habitTranslations ht"
        + " JOIN FETCH ht.language l"
        + " WHERE h.id = :habitId AND upper(ha.status) NOT IN ('CANCELLED','EXPIRED')")
    List<HabitAssign> findAllByHabitId(@Param("habitId") Long habitId);

    /**
     * Method to find {@link HabitAssign} by {@link User} and {@link Habit} id's and
     * {@link ZonedDateTime} dateTime. not canceled and not expired
     *
     * @param habitId  {@link Habit} id.
     * @param userId   {@link User} id.
     * @param dateTime {@link ZonedDateTime} dateTime.
     * @return {@link HabitAssign} instance, if it doesn't exist returns Optional.
     */
    @Query("SELECT ha FROM HabitAssign ha "
        + "WHERE ha.habit.id = :habitId AND ha.user.id = :userId "
        + "AND DATE(ha.createDate) = :dateTime AND upper(ha.status) NOT IN ('CANCELLED','EXPIRED','REQUESTED')")
    Optional<HabitAssign> findByHabitIdAndUserIdAndCreateDate(@Param("habitId") Long habitId,
        @Param("userId") Long userId,
        @Param("dateTime") ZonedDateTime dateTime);

    /**
     * Method to find {@link HabitAssign}'s by {@link User} and {@link HabitAssign}
     * id and INPROGRESS status.
     *
     * @param habitAssignId {@link Long} id.
     * @param userId        {@link Long} id.
     * @return {@link HabitAssign} instance.
     * @author Anton Bondar
     */
    @Query(value = "SELECT ha FROM HabitAssign ha"
        + " WHERE ha.id = :habitAssignId AND ha.user.id = :userId AND upper(ha.status) = 'INPROGRESS'")
    Optional<HabitAssign> findByHabitAssignIdUserIdAndStatusIsInProgress(@Param("habitAssignId") Long habitAssignId,
        @Param("userId") Long userId);

    /**
     * Method to find {@link HabitAssign}'s by {@link User} and {@link HabitAssign}
     * id and REQUESTED status.
     *
     * @param habitAssignId {@link Long} id.
     * @param userId        {@link Long} id.
     * @return {@link HabitAssign} instance.
     * @author Lilia Mokhnatska
     */
    @Query(value = "SELECT ha FROM HabitAssign ha"
        + " WHERE ha.id = :habitAssignId AND ha.user.id = :userId AND upper(ha.status) = 'REQUESTED'")
    Optional<HabitAssign> findByHabitAssignIdUserIdAndStatusIsRequested(@Param("habitAssignId") Long habitAssignId,
        @Param("userId") Long userId);

    /**
     * Method to find {@link HabitAssign}'s by {@link User} id and ACQUIRED status.
     *
     * @param userId {@link User} id.
     * @return {@link HabitAssign} instance.
     */
    @Query(value = "SELECT DISTINCT ha FROM HabitAssign ha"
        + " WHERE ha.user.id = :userId AND upper(ha.status) = 'ACQUIRED'")
    List<HabitAssign> findAllByUserIdAndStatusAcquired(@Param("userId") Long userId);

    /**
     * Method to find {@link HabitAssign}'s by {@link User} id and CANCELLED status.
     *
     * @param userId {@link User} id.
     * @return {@link HabitAssign} instance.
     */
    @Query(value = "SELECT DISTINCT ha FROM HabitAssign ha"
        + " WHERE ha.user.id = :userId AND upper(ha.status) = 'CANCELLED'")
    List<HabitAssign> findAllByUserIdAndStatusIsCancelled(@Param("userId") Long userId);

    /**
     * Method to find {@link HabitAssign}'s by {@link User} and {@link Habit} id's
     * and CANCELLED status.
     *
     * @param habitId {@link Habit} id.
     * @param userId  {@link User} id.
     * @return {@link HabitAssign} instance.
     */
    @Query(value = "SELECT * FROM habit_assign ha"
        + " WHERE habit_id = :habitId AND user_id = :userId AND upper(ha.status) IN ('CANCELLED','REQUESTED')",
        nativeQuery = true)
    HabitAssign findByHabitIdAndUserIdAndStatusIsCancelledOrRequested(@Param("habitId") Long habitId,
        @Param("userId") Long userId);

    /**
     * Method to find {@link HabitAssign} by {@link User} id and {@link Habit} id
     * (with not cancelled and not expired status).
     *
     * @param userId  {@link User} id.
     * @param habitId {@link Habit} id.
     * @return {@link HabitAssign} instance, if it doesn't exist returns Optional.
     */
    @Query(value = "SELECT DISTINCT ha FROM HabitAssign AS ha "
        + "WHERE ha.habit.id = :habitId AND ha.user.id = :userId "
        + "AND upper(ha.status) NOT IN ('CANCELLED','EXPIRED')")
    Optional<HabitAssign> findByHabitIdAndUserId(@Param("habitId") Long habitId,
        @Param("userId") Long userId);

    /**
     * Method to find {@link HabitAssign} by {@link User} id and {@link HabitAssign}
     * id (with not cancelled and not expired status).
     *
     * @param userId        {@link User} id.
     * @param habitAssignId {@link HabitAssign} id.
     * @return {@link HabitAssign} instance, if it doesn't exist returns Optional.
     * @author Anton Bondar
     */
    @Query(value = "SELECT DISTINCT ha FROM HabitAssign AS ha "
        + "WHERE ha.id = :habitAssignId AND ha.user.id= :userId "
        + "AND upper(ha.status) NOT IN ('CANCELLED','EXPIRED')")
    Optional<HabitAssign> findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(
        @Param("habitAssignId") Long habitAssignId,
        @Param("userId") Long userId);

    /**
     * Method to find a list of {@link HabitAssign} instances by {@link User} id and
     * {@link Habit} id (with not cancelled and not expired status).
     *
     * @param userId  {@link User} id.
     * @param habitId {@link Habit} id.
     * @return List of {@link HabitAssign} instances, if none exist returns an empty
     *         list.
     */
    @Query(value = "SELECT DISTINCT ha FROM HabitAssign AS ha "
        + "WHERE ha.habit.id = :habitId AND ha.user.id = :userId "
        + "AND upper(ha.status) NOT IN ('CANCELLED','EXPIRED')")
    List<HabitAssign> findHabitsByHabitIdAndUserId(@Param("habitId") Long habitId,
        @Param("userId") Long userId);

    /**
     * Method for counting all inprogress {@link HabitAssign}'s by {@link User} id
     * (with not cancelled and not acquired status).
     *
     * @param userId {@link User} id.
     * @return amount of items in Optional in case of absence such info.
     */
    @Query(value = "SELECT COUNT(ha.id) FROM HabitAssign ha "
        + "WHERE upper(ha.status) = 'INPROGRESS' AND ha.user.id = :userId")
    int countHabitAssignsByUserIdAndAcquiredFalseAndCancelledFalse(@Param("userId") Long userId);

    /**
     * Method for counting {@link HabitAssign} by {@link User} id and period between
     * start/end {@link ZonedDateTime} (with not cancelled and not expired status).
     *
     * @param userId {@link User} id.
     * @param start  {@link ZonedDateTime} start time.
     * @param end    {@link ZonedDateTime} end time.
     * @return amount of items in Optional in case of absence such info.
     */
    @Query(value = "SELECT COUNT(ha) "
        + "FROM HabitAssign ha "
        + "WHERE upper(ha.status) NOT IN ('CANCELLED','EXPIRED') "
        + "AND ha.user.id = :userId "
        + "AND ha.createDate > :start AND ha.createDate < :end")
    int countMarkedHabitAssignsByUserIdAndPeriod(@Param("userId") Long userId,
        @Param("start") ZonedDateTime start,
        @Param("end") ZonedDateTime end);

    /**
     * Method to find all inprogress habit assigns on certain {@link LocalDate}.
     *
     * @param userId {@link User} id.
     * @param date   {@link LocalDate} instance.
     * @return list of {@link HabitAssign} instances.
     */
    @Query(value = "SELECT DISTINCT ha FROM HabitAssign ha "
        + "JOIN FETCH ha.habit h JOIN FETCH h.habitTranslations ht "
        + "JOIN FETCH ht.language l "
        + "WHERE upper(ha.status) = 'INPROGRESS' "
        + "AND ha.user.id = :userId "
        + "AND cast(ha.createDate as date) <= cast(:date as date) "
        + "AND cast(FUNCTION('DATEADD', DAY, ha.duration, ha.createDate) as date) >= cast(:date as date)")
    List<HabitAssign> findAllInprogressHabitAssignsOnDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * Method to find {@link HabitAssign}'s by {@link User} id and INPROGRESS
     * status.
     *
     * @param userId {@link User} id.
     * @return {@link HabitAssign} instance.
     */
    @Query(value = "SELECT ha FROM HabitAssign ha"
        + " WHERE ha.user.id = :userId AND upper(ha.status) = 'INPROGRESS'")
    List<HabitAssign> findAllByUserIdAndStatusIsInProgress(@Param("userId") Long userId);

    /**
     * Method to find all inprogress habit assigns related to user.
     *
     * @param userId {@link User} id.
     * @return list of {@link HabitAssign} instances.
     */
    @Query("SELECT DISTINCT ha FROM HabitAssign ha "
        + "JOIN FETCH ha.habit h JOIN FETCH h.habitTranslations ht "
        + "JOIN FETCH ht.language l "
        + "WHERE upper(ha.status) = 'INPROGRESS' "
        + "AND ha.user.id = :userId")
    List<HabitAssign> findAllInProgressHabitAssignsRelatedToUser(@Param("userId") Long userId);

    /**
     * Method to find all inprogress, habit assigns.
     *
     * @return list of {@link HabitAssign} instances.
     */
    @Query(value = "SELECT DISTINCT ha FROM HabitAssign ha "
        + "JOIN FETCH ha.habit h JOIN FETCH h.habitTranslations ht "
        + "JOIN FETCH ht.language l "
        + "WHERE upper(ha.status) = 'INPROGRESS'")
    List<HabitAssign> findAllInProgressHabitAssigns();

    /**
     * Method to find all habit assigns by status.
     *
     * @param status {@link HabitAssignStatus} status of habit assign.
     * @return list of {@link HabitAssign} instances.
     * @author Vira Maksymets
     */
    @Query(value = "SELECT DISTINCT ha FROM HabitAssign ha "
        + "JOIN FETCH ha.habit h JOIN FETCH h.habitTranslations ht "
        + "JOIN FETCH ht.language l "
        + "WHERE ha.status = :status AND ha.habit.id = :habitId")
    List<HabitAssign> findAllHabitAssignsByStatusAndHabitId(@Param("status") HabitAssignStatus status,
        @Param("habitId") Long habitId);

    /**
     * Method to find amount of users that acquired habit.
     *
     * @param habitId {@link Habit} id.
     * @return Long.
     * @author Oleh Kulbaba
     */
    @Query(value = "SELECT count(ha)"
        + "FROM HabitAssign ha WHERE ha.habit.id = :habitId AND ha.status='ACQUIRED'")
    Long findAmountOfUsersAcquired(@Param("habitId") Long habitId);

    /**
     * Method to change value progressNotificationHasDisplayed in
     * {@link HabitAssign} to true.
     *
     * @param habitAssignId id of {@link HabitAssign}.
     * @param userId        id of {@link User}.
     * @author Lilia Mokhnatska
     */
    @Modifying
    @Query("UPDATE HabitAssign ha SET ha.progressNotificationHasDisplayed = true "
        + "WHERE ha.id = :habitAssignId and ha.user.id = :userId")
    void updateProgressNotificationHasDisplayed(@Param("habitAssignId") Long habitAssignId,
        @Param("userId") Long userId);

    /**
     * Method to find all Habit ids by user id and status REQUESTED by friends of
     * current user.
     *
     * @param userId {@link Long} id.
     * @return list of {@link Long} habit ids requested by friends of current user.
     * @author Olena Sotnik
     */
    @Query(value = "SELECT DISTINCT ha.habit.id FROM HabitAssign ha "
        + "WHERE ha.user.id = :userId AND upper(ha.status) = 'REQUESTED'")
    List<Long> findAllHabitIdsByUserIdAndStatusIsRequested(@Param("userId") Long userId);

    /**
     * Method to find list of friends ids of current user who tracks the same Habit.
     *
     * @param userId  {@link Long} userId of current user.
     * @param habitId {@link Long} habitId.
     * @return list of {@link Long} user ids of user's friends tracking current
     *         habit.
     * @author Olena Sotnik
     */
    @Query(value = "SELECT DISTINCT ha.user_id "
        + "FROM habit_assign AS ha "
        + "JOIN users_friends AS uf "
        + "ON (ha.user_id = uf.friend_id OR ha.user_id = uf.user_id) "
        + "WHERE (uf.user_id = :userId OR uf.friend_id = :userId) "
        + "AND uf.status = 'FRIEND' "
        + "AND ha.habit_id = :habitId "
        + "AND ha.is_private = FALSE "
        + "AND ha.user_id != :userId "
        + "AND (ha.status = 'INPROGRESS' OR ha.status = 'ACQUIRED')", nativeQuery = true)
    List<Long> findFriendsIdsTrackingHabit(@Param("habitId") Long habitId, @Param("userId") Long userId);

    /**
     * Retrieves a page of {@link HabitAssign} entities for a specified user where
     * the habit is either 'INPROGRESS' or 'ACQUIRED', and where the habit is also
     * assigned to a current user with the same statuses.
     *
     * @param userId        the ID of the user for whom the habit assignments are
     *                      being retrieved
     * @param currentUserId the ID of the current user to cross-reference habit
     *                      assignments
     * @param pageable      the pagination information
     * @return a page of {@link HabitAssign} entities that match the criteria
     */
    @Query("""
            SELECT ha FROM HabitAssign ha
            left join fetch ha.habit h
            left join fetch h.habitTranslations ht
            left join fetch ht.language l
            WHERE ha.user.id = :userId
            and (ha.status = 'INPROGRESS' OR ha.status = 'ACQUIRED') AND ha.habit.id IN
            (SELECT ha1.habit.id FROM HabitAssign ha1 where ha1.user.id = :currentUserId
            AND (ha1.status = 'INPROGRESS' OR ha1.status = 'ACQUIRED'))
            ORDER BY ha.createDate
        """)
    Page<HabitAssign> findAllMutual(Long userId, Long currentUserId, Pageable pageable);

    /**
     * Retrieves a paginated list of {@code HabitAssign} entities for a specified
     * user that are in progress or acquired, and are shared with the current user.
     * This method includes fetching associated {@code Habit},
     * {@code HabitTranslations}, and {@code Language} entities.
     *
     * @param userId        the ID of the {@code User} whose {@code HabitAssign}
     *                      entities are to be retrieved.
     * @param currentUserId the ID of the current user to find mutual habit
     *                      assignments with.
     * @param pageable      the {@link Pageable} object containing pagination
     *                      information.
     * @return a {@link Page} of {@code HabitAssign} entities.
     */
    @Query("""
            SELECT ha FROM HabitAssign ha
            left join fetch ha.habit h
            left join fetch h.habitTranslations ht
            left join fetch ht.language l
            WHERE ha.user.id = :userId
            and (ha.status = 'INPROGRESS' OR ha.status = 'ACQUIRED') AND ha.habit.id IN
            (SELECT h1.id FROM Habit h1 WHERE h1.userId = :currentUserId)
            ORDER BY ha.createDate
        """)
    Page<HabitAssign> findAllOfCurrentUser(Long userId, Long currentUserId, Pageable pageable);

    /**
     * Returns a list of HabitAssign objects that match the given user IDs and habit
     * ID, provided that the status of HabitAssign is either 'INPROGRESS' or
     * 'ACQUIRED'.
     *
     * @param userIds list of user IDs for which to find HabitAssign records
     * @param habitId the ID of the habit for which to find HabitAssign records
     * @return list of HabitAssign objects that meet the specified conditions
     */
    @Query("""
            SELECT ha
            FROM HabitAssign ha
            WHERE ha.user.id IN :userIds
            AND ha.habit.id = :habitId
            AND (ha.status = 'INPROGRESS' OR ha.status = 'ACQUIRED')
        """)
    List<HabitAssign> findByUserIdsAndHabitId(List<Long> userIds, Long habitId);

    /**
     * Returns a list of HabitAssign objects that have last day of primary duration
     * to send notification to user.
     *
     * @return list of HabitAssign objects that meet the specified conditions
     */
    @Query(value = """
            SELECT ha
            FROM HabitAssign ha
            LEFT JOIN Notification n ON n.targetId = ha.id AND n.notificationType = 'HABIT_LAST_DAY_OF_PRIMARY_DURATION'
            WHERE ha.status = 'INPROGRESS'
            AND (CAST(ha.workingDays AS float) / ha.duration) >= 0.2
            AND (CAST(ha.workingDays AS float) / ha.duration) < 0.8
            AND (CAST(ha.workingDays + 1 AS float) / ha.duration) >= 0.8
            AND n.id IS NULL
        """)
    List<HabitAssign> getHabitAssignsWithLastDayOfPrimaryDurationToMessage();

    /**
     * Group HabitAssign records by status and count occurrences.
     */
    @Query("""
            SELECT new greencity.dto.habitstatistic.HabitStatusCount(ha.status, COUNT(ha))
            FROM HabitAssign ha
            WHERE ha.user.userStatus IN (greencity.enums.UserStatus.ACTIVATED)
            GROUP BY ha.status
        """)
    List<HabitStatusCount> countHabitAssignsByStatus();
}
