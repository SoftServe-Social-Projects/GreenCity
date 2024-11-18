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

    @Query(nativeQuery = true,
            value = "SELECT ctdl.* FROM custom_to_do_list_items ctdl "
                    + "JOIN user_to_do_list ustdl ON ustdl.target_id = ctdl.id "
                    + "WHERE ustdl.is_custom_item = true "
                    + "AND ustdl.habit_assign_id = :habitAssignId")
    List<CustomToDoListItem> findAllByHabitAssignId(Long habitAssignId);

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
     * Method find all default custom to-do list items by habit.
     *
     * @param habitId {@link CustomToDoListItem} id
     * @return list of {@link CustomToDoListItem}
     */
    List<CustomToDoListItem> findAllByHabitIdAndIsDefaultTrue(Long habitId);

    /**
     * Method returns default custom to-do list items ids which are in the habit.
     *
     * @param habitId habit id
     * @return list of id.
     */
    @Query(nativeQuery = true,
            value = "select ctd.id from custom_to_do_list_items ctd where habit_id = :habitId and "
                    + " ctd.is_default = true and "
                    + " ctd.status like 'ACTIVE';")
    List<Long> getAllCustomToDoListItemIdByHabitIdIsContained(Long habitId);

    /**
     * Method returns custom to-do list items ids created by user which are in the habit and not default.
     *
     * @param habitId habit id
     * @param userId user id
     * @return list of id.
     */
    @Query(nativeQuery = true,
            value = "select ctd.id from custom_to_do_list_items ctd where habit_id = :habitId and "
                    + " ctd.is_default = false and "
                    + " ctd.user_id = :userId and "
                    + " ctd.status like 'ACTIVE';")
    List<Long> getAllCustomToDoListItemIdByUserIdAndByHabitIdAndNotDefault(Long habitId, Long userId);

    /**
     * Method returns {@link CustomToDoListItem} by list item id.
     *
     * @param listId habit id
     * @return list of {@link CustomToDoListItem}.
     */
    @Query("select g from CustomToDoListItem g where g.id in( :listId )")
    List<CustomToDoListItem> getToDoListByListOfId(List<Long> listId);

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
    @Query(value = "DELETE FROM custom_to_do_list_items WHERE habit_id =:habitId "
            + "AND custom_to_do_list_items.user_id = :userId "
            + "AND is_default = false", nativeQuery = true)
    void deleteNotDefaultCustomToDoListItemsByHabitIdAndUserId(Long habitId, Long userId);
}
