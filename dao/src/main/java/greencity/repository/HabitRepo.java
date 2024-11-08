package greencity.repository;

import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Provides an interface to manage {@link Habit} entity.
 */
@Repository
public interface HabitRepo extends JpaRepository<Habit, Long>, JpaSpecificationExecutor<Habit> {
    /**
     * Method add goal to habit by id and status ACTIVE. This method use native SQL
     * query.
     *
     * @param habitID Id of Habit
     * @param itemID  Id of ToDoListItem
     * @author Marian Diakiv
     */
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "insert into habit_to_do_list_items(habit_id,to_do_list_item_id)"
        + "values (:habitID,:itemID);")
    void addToDoListItemToHabit(@Param("habitID") Long habitID, @Param("itemID") Long itemID);

    /**
     * Method to change status. This method use native SQL query.
     *
     * @param habitID Id of Habit
     * @param itemID  Id of To-do list item
     * @author Marian Diakiv
     */
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update habit_to_do_list_items set status = 'DELETED'"
        + " where habit_to_do_list_items.habit_id = :habitID and "
        + "habit_to_do_list_items.to_do_list_item_id = :itemID"
        + " and habit_to_do_list_items.status like 'ACTUAL'")
    void upadateToDoListItemInHabit(@Param("habitID") Long habitID, @Param("itemID") Long itemID);

    /**
     * Method to find customHabit by id and isCustomHabit true.
     *
     * @param id - custom habit id
     * @return {@link Optional} of {@link Habit} instance if present by id
     *
     * @author Olena Sotnik
     */
    Optional<Habit> findByIdAndIsCustomHabitIsTrue(@Param("id") Long id);

    /**
     * Method to find habit assign of user who owns habit by userId and habitId.
     *
     * @param userId  {@link Long} userId of user who owns habit.
     * @param habitId {@link Long} habitId.
     * @return {@link List} of {@link HabitAssign} of current habit's owner.
     *
     * @author Olena Sotnik
     */
    @Query(value = "SELECT DISTINCT ha.id "
        + "FROM habit_assign AS ha "
        + "WHERE ha.habit_id =:habitId "
        + "AND ha.user_id =:userId", nativeQuery = true)
    List<Long> findHabitAssignByHabitIdAndHabitOwnerId(@Param("habitId") Long habitId, @Param("userId") Long userId);

    /**
     * Finds and returns a list of IDs for habits that are visible to a given user.
     * The returned habits include:
     * <ul>
     * <li>Habits owned by the user.</li>
     * <li>Public habits</li>
     * <li>Private habits that are assigned to the user.</li>
     * <li>Private habits that are shared with the user's friends (using the
     * isSharedWithFriends flag).</li>
     * </ul>
     *
     * @param userId the ID of the user for whom visible habit IDs are to be
     *               retrieved.
     * @return a {@link List} of {@link Long} IDs of habits that are visible to thе
     *         specified user.
     */
    @Query(value = """
        SELECT DISTINCT h.id
        FROM habits h
        LEFT JOIN habits_tags ht ON h.id = ht.habit_id
        LEFT JOIN tags t ON ht.tag_id = t.id
        LEFT JOIN tag_translations tt ON tt.tag_id = t.id AND tt.language_id = 2 AND tt.name = 'Private'
        LEFT JOIN habit_assign ha ON h.id = ha.habit_id AND ha.user_id = :userId
        LEFT JOIN users_friends uf ON uf.status = 'FRIEND'
                                    AND ((uf.user_id = :userId AND uf.friend_id = h.user_id)
                                         OR (uf.user_id = h.user_id AND uf.friend_id = :userId))
        WHERE h.user_id = :userId
        OR tt.name IS NULL
        OR ha.user_id = :userId
        OR (h.is_shared_with_friends = TRUE AND uf.user_id IS NOT NULL)
        """, nativeQuery = true)
    List<Long> findVisibleCustomHabitsIdsByUserId(@Param("userId") Long userId);

    /**
     * Determines if a habit is private based on its tags.
     *
     * @param habitId the ID of the habit to check.
     * @return {@code true} if the habit is private, {@code false} otherwise.
     */
    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM habits_tags ht
                JOIN tag_translations tt ON ht.tag_id = tt.tag_id
                WHERE ht.habit_id = :habitId
                AND tt.language_id = 2
                AND tt.name = 'Private'
            )
        """, nativeQuery = true)
    boolean isHabitPrivate(@Param("habitId") Long habitId);
}
