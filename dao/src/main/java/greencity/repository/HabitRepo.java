package greencity.repository;

import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
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
     * Finds a random habit that is not marked as deleted.
     *
     * @return a random {@link Habit} entity that is not deleted, or {@code null} if
     *         no such habit exists.
     */
    @Query(nativeQuery = true,
        value = """
                WITH sampled_habits AS (
                   SELECT *
                   FROM habits
                            TABLESAMPLE SYSTEM(15)
                   WHERE is_deleted = false
                   LIMIT 1
               ),
                fallback_habit AS (
                    SELECT *
                    FROM habits
                    WHERE is_deleted = false
                    LIMIT 1
                )
               SELECT *
               FROM sampled_habits
               UNION ALL
               SELECT *
               FROM fallback_habit
               LIMIT 1;
            """)
    Habit findRandomHabit();
}
