package greencity.repository;

import greencity.entity.CustomToDoListItem;
import greencity.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides an interface to manage {@link CustomToDoListItem} entity.
 */
@Repository
public interface CustomToDoListItemRepo extends JpaRepository<CustomToDoListItem, Long> {
    /**
     * Method returns list of available (not ACTIVE) custom to-do list items for
     * user.
     *
     * @param userId id of the {@link User} current user
     * @return list of {@link CustomToDoListItem}
     */
    @Query("SELECT cg FROM CustomToDoListItem cg WHERE "
        + "NOT cg.status='DISABLED' "
        + "AND cg.user.id=:userId "
        + "AND cg.habit.id=:habitId "
        + "ORDER BY cg.id")
    List<CustomToDoListItem> findAllAvailableCustomToDoListItemsForUserId(@Param("userId") Long userId,
        @Param("habitId") Long habitId);

    /**
     * Method returns list of custom to-do list items by userId and habitId and
     * INPROGRESS status.
     *
     * @param userId  id of the {@link User} current user
     * @param habitId id of the {@link Long} habit
     * @return list of {@link CustomToDoListItem}
     */
    @Query("SELECT cg FROM CustomToDoListItem cg WHERE "
        + " cg.status='INPROGRESS' "
        + "AND cg.user.id=:userId "
        + "AND cg.habit.id=:habitId "
        + "ORDER BY cg.id")
    List<CustomToDoListItem> findAllCustomToDoListItemsForUserIdAndHabitIdInProgress(
        @Param("userId") Long userId, @Param("habitId") Long habitId);

    /**
     * Method find all custom to-do list items by user.
     *
     * @param userId  {@link CustomToDoListItem} id
     * @param habitId {@link CustomToDoListItem} id
     * @return list of {@link CustomToDoListItem}
     */
    List<CustomToDoListItem> findAllByUserIdAndHabitId(Long userId, Long habitId);

    /**
     * Method returns particular selected custom to-do list items for user.
     *
     * @param userId id of the {@link User} current user
     * @param itemId item id {@link Long}
     * @return {@link CustomToDoListItem}
     */
    @Query("SELECT cg FROM CustomToDoListItem cg WHERE"
        + " cg.user.id=:userId and cg.id=:itemId")
    CustomToDoListItem findByUserIdAndItemId(@Param("userId") Long userId, @Param("itemId") Long itemId);

    /**
     * Method returns custom to-do list items by status.
     *
     * @param userId id of the {@link User} current user
     * @param status item id {@link String}
     * @return {@link CustomToDoListItem}
     */
    @Query(value = "SELECT * from custom_to_do_list_items where user_id = :userId and status = :stat",
        nativeQuery = true)
    List<CustomToDoListItem> findAllByUserIdAndStatus(@Param(value = "userId") Long userId,
        @Param(value = "stat") String status);

    /**
     * Method returns all custom to-do list items.
     *
     * @param userId id of the {@link User} current user
     * @return {@link CustomToDoListItem}
     */
    @Query(value = "SELECT * from custom_to_do_list_items where user_id = :userId", nativeQuery = true)
    List<CustomToDoListItem> findAllByUserId(@Param(value = "userId") Long userId);

    /**
     * Method delete selected items from custom to-do list.
     *
     * @param habitId id of needed habit
     * @author Anton Bondar
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM custom_to_do_list_items WHERE habit_id =:habitId", nativeQuery = true)
    void deleteCustomToDoListItemsByHabitId(Long habitId);
}
