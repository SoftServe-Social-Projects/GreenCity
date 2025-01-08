package greencity.repository;

import greencity.entity.UserToDoListItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserToDoListItemRepo extends JpaRepository<UserToDoListItem, Long> {
    /**
     * Method returns list of {@link UserToDoListItem} for specific user.
     *
     * @param habitAssignId - id of habit assign.
     * @return list of {@link UserToDoListItem}
     */
    @Query("SELECT utdli FROM UserToDoListItem utdli where utdli.habitAssign.id =?1")
    List<UserToDoListItem> findAllByHabitAssingId(Long habitAssignId);

    /**
     * Method delete selected item from users to-do list.
     *
     * @param toDoListItemId id of needed goal
     * @param habitAssignId  id of needed habit assign
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM user_to_do_list utdl "
        + "WHERE utdl.to_do_list_item_id =:toDoListItemId AND utdl.habit_assign_id =:habitAssignId ")
    void deleteByToDoListItemIdAndHabitAssignId(Long toDoListItemId, Long habitAssignId);

    /**
     * Method delete selected items from users to-do list.
     *
     * @param habitAssignId id of needed habit assign
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM user_to_do_list utdl "
        + "WHERE utdl.habit_assign_id =:habitAssignId ")
    void deleteToDoListItemsByHabitAssignId(Long habitAssignId);

    /**
     * Method returns to-do list ids for habit.
     *
     * @param id id of needed habit
     * @return List of {@link Long}
     */
    @Query(nativeQuery = true, value = "SELECT to_do_list_item_id FROM habit_to_do_list_items "
        + "WHERE habit_id = :id")
    List<Long> getToDoListItemsIdForHabit(Long id);

    /**
     * Method returns to-do list ids selected by user.
     *
     * @param id id of needed habit assign
     * @return List of {@link Long}
     */
    @Query(nativeQuery = true,
        value = "SELECT to_do_list_item_id FROM user_to_do_list WHERE habit_assign_id = :id")
    List<Long> getAllAssignedToDoListItems(Long id);

    /**
     * Method returns UserToDoListItem list by habit assign id.
     *
     * @param id id of needed habit assign
     * @return List of {@link Long}
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM user_to_do_list WHERE habit_assign_id = :id")
    List<UserToDoListItem> getAllAssignedToDoListItemsFull(Long id);

    /**
     * Method returns user to-do list items by habitAssignId and INPROGRESS status.
     *
     * @param habitAssignId id of needed habit assign
     * @return List of {@link UserToDoListItem}
     */

    @Query("SELECT utdli FROM UserToDoListItem utdli WHERE "
        + "utdli.status='INPROGRESS' "
        + "AND utdli.habitAssign.id=:habitAssignId "
        + "ORDER BY utdli.id")
    List<UserToDoListItem> findUserToDoListItemsByHabitAssignIdAndStatusInProgress(
        @Param("habitAssignId") Long habitAssignId);

    /**
     * Method returns to-do list with statuses DONE.
     *
     * @param habitAssignId id of needed habit assign
     * @param status        status of needed items
     * @return List of {@link Long}
     */
    @Query(nativeQuery = true,
        value = "SELECT to_do_list_item_id FROM user_to_do_list WHERE habit_assign_id = :habitAssignId"
            + " AND status = :status")
    List<Long> getToDoListItemsByHabitAssignIdAndStatus(Long habitAssignId, String status);

    /**
     * Method returns to-do list with statuses DONE.
     *
     * @param userId {@link Long} user id.
     * @param itemId {@link Long} custom to-do list item id.
     */
    @Query(nativeQuery = true, value = """
        select utdl.id from user_to_do_list as utdl
        join habit_assign as ha on ha.id = habit_assign_id
        where ha.user_id = :userId and to_do_list_item_id = :itemId""")
    Optional<Long> getByUserAndItemId(Long userId, Long itemId);

    /**
     * Method returns {@link UserToDoListItem} by user to-do list item id and user
     * id.
     *
     * @param userToDoListItemId {@link Long}
     * @param userId             {@link Long}
     * @return {@link UserToDoListItem}
     * @author Anton Bondar
     */
    @Query(value = "SELECT u FROM UserToDoListItem u JOIN HabitAssign ha ON ha.id = u.habitAssign.id "
        + "WHERE ha.user.id =:userId AND u.id =:userToDoListItemId")
    List<UserToDoListItem> getAllByUserToDoListIdAndUserId(
        @Param(value = "userToDoListItemId") Long userToDoListItemId,
        @Param(value = "userId") Long userId);

    /*
     * @Query(nativeQuery = true, value =
     * "SELECT utdl.duration, utdl.working_days, utdl.status FROM user_to_do_list utdl "
     * + "JOIN habit_assign ha ON ha.id = utdl.habit_assign_id " +
     * "JOIN to_do_list_item tdli ON tdli.id = utdl.to_do_list_item " + "JOIN  "
     */
}
