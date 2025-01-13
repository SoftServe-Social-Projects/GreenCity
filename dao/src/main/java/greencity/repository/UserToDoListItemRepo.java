package greencity.repository;

import greencity.entity.UserToDoListItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface UserToDoListItemRepo extends JpaRepository<UserToDoListItem, Long> {
    /**
     * Method returns list of {@link UserToDoListItem} for specific habit assign.
     *
     * @param habitAssignId - id of habit assign.
     * @return list of {@link UserToDoListItem}
     */
    @Query("SELECT utdli FROM UserToDoListItem utdli where utdli.habitAssign.id =?1")
    List<UserToDoListItem> findAllByHabitAssingId(Long habitAssignId);

    /**
     * Method delete items from users to-do list by habit assign id.
     *
     * @param habitAssignId id of {@link greencity.entity.HabitAssign} needed habit
     *                      assign
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM user_to_do_list utdl "
        + "WHERE utdl.habit_assign_id =:habitAssignId ")
    void deleteToDoListItemsByHabitAssignId(Long habitAssignId);

    /**
     * Method returns default UserToDoListItem list by habit assign id.
     *
     * @param habitAssignId id of needed habit assign
     * @return List of {@link Long}
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM user_to_do_list WHERE habit_assign_id = :habitAssignId AND is_custom_item = false")
    List<UserToDoListItem> getAllAssignedToDoListItemsFull(Long habitAssignId);

    /**
     * Method returns custom UserToDoListItem list by habit assign id.
     *
     * @param habitAssignId id of needed habit assign
     * @return List of {@link Long}
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM user_to_do_list WHERE habit_assign_id = :habitAssignId AND is_custom_item = true")
    List<UserToDoListItem> getAllAssignedCustomToDoListItemsFull(Long habitAssignId);

    /**
     * Method returns to-do list item ids with specific status.
     *
     * @param habitAssignId id of needed habit assign
     * @param status        status of needed items
     * @return List of {@link Long}
     */
    @Query(nativeQuery = true,
        value = "SELECT to_do_list_item_id FROM user_to_do_list WHERE habit_assign_id = :habitAssignId"
            + " AND is_custom_item = false AND status = :status")
    List<Long> getToDoListItemsByHabitAssignIdAndStatus(Long habitAssignId, String status);

    /**
     * Method returns user to-do list item by habit assign and custom to-do list
     * item id.
     *
     * @param habitAssignId {@link Long} habit assign id.
     * @param itemId        {@link Long} custom to-do list item id.
     */
    @Query(nativeQuery = true, value = """
        select utdl.* from user_to_do_list as utdl
        where utdl.habit_assign_id = :habitAssignId
        and to_do_list_item_id = :itemId
        and is_custom_item = true""")
    Optional<UserToDoListItem> getCustomToDoItemIdByHabitAssignIdAndItemId(Long habitAssignId, Long itemId);

    /**
     * Method returns user to-do list item by habit assign and to-do list item id.
     *
     * @param habitAssignId {@link Long} habit assign id.
     * @param itemId        {@link Long} to-do list item id.
     */
    @Query(nativeQuery = true, value = """
        select utdl.* from user_to_do_list as utdl
        where utdl.habit_assign_id = :habitAssignId
        and to_do_list_item_id = :itemId
        and is_custom_item = false""")
    Optional<UserToDoListItem> getToDoItemIdByHabitAssignIdAndItemId(Long habitAssignId, Long itemId);
}
