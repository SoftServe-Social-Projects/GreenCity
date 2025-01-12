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
     * Method returns list of available custom to-do list items for user by habit
     * id.
     *
     * @param userId id of the {@link User} current user
     * @return list of {@link CustomToDoListItem}
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM custom_to_do_list_items ctd WHERE "
            + "ctd.status like 'ACTIVE' "
            + "AND ctd.user_id=:userId "
            + "AND ctd.habit_id=:habitId")
    List<CustomToDoListItem> findAllAvailableCustomToDoListItemsForUserId(@Param("userId") Long userId,
        @Param("habitId") Long habitId);

    /**
     * Method returns list of custom to-do list items by habit assign id.
     *
     * @param habitAssignId id of the {@link greencity.entity.HabitAssign} current
     *                      habit assign
     * @return list of {@link CustomToDoListItem}
     */
    @Query(nativeQuery = true,
        value = "SELECT ctdl.* FROM custom_to_do_list_items ctdl "
            + "JOIN user_to_do_list ustdl ON ustdl.to_do_list_item_id = ctdl.id "
            + "WHERE ustdl.is_custom_item = true "
            + "AND ustdl.habit_assign_id = :habitAssignId")
    List<CustomToDoListItem> findAllByHabitAssignId(Long habitAssignId);

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
     * Method returns custom to-do list items ids created by user which are in the
     * habit and not default.
     *
     * @param habitId habit id
     * @param userId  user id
     * @return list of id.
     */
    @Query(nativeQuery = true,
        value = "select ctd.id from custom_to_do_list_items ctd where habit_id = :habitId and "
            + " ctd.is_default = false and "
            + " ctd.user_id = :userId and "
            + " ctd.status like 'ACTIVE';")
    List<Long> getAllCustomToDoListItemIdByUserIdAndByHabitIdAndNotDefault(Long habitId, Long userId);

    /**
     * Method delete not default selected items from custom to-do list.
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
