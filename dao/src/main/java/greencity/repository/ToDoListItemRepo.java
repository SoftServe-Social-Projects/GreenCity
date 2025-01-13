package greencity.repository;

import greencity.entity.ToDoListItem;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ToDoListItemRepo
    extends JpaRepository<ToDoListItem, Long>, JpaSpecificationExecutor<ToDoListItem> {
    /**
     * Method returns {@link ToDoListItem} by search query and page.
     *
     * @param paging {@link Pageable}.
     * @param query  query to search.
     * @return list of {@link ToDoListItem}.
     */
    @Query("SELECT g FROM ToDoListItem g join g.translations as gt"
        + " WHERE CONCAT(g.id,'') LIKE LOWER(CONCAT('%', :query, '%')) "
        + "OR LOWER(gt.language.code) LIKE LOWER(CONCAT('%', :query, '%'))"
        + "OR LOWER(gt.content) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<ToDoListItem> searchBy(Pageable paging, String query);

    /**
     * Method returns list of to-do list item ids which are not in the habit.
     *
     * @param habitId habit id
     * @return list of id.
     */
    @Query(nativeQuery = true,
        value = "select to_do_list_items.id from to_do_list_items where id not in"
            + " (select to_do_list_item_id from habit_to_do_list_items where habit_id = :habitId and "
            + "habit_to_do_list_items.status like 'ACTIVE');")
    List<Long> getAllToDoListItemsByHabitIdNotContained(@Param("habitId") Long habitId);

    /**
     * Method returns list of to-do list item ids which are in the habit.
     *
     * @param habitId habit id
     * @return list of id.
     */
    @Query(nativeQuery = true,
        value = "select to_do_list_item_id from habit_to_do_list_items where habit_id = :habitId and "
            + " habit_to_do_list_items.status like 'ACTIVE';")
    List<Long> getAllToDoListItemIdByHabitIdIsContained(@Param("habitId") Long habitId);

    /**
     * Method returns {@link ToDoListItem} by list item id and pageable.
     *
     * @param listId habit id
     * @return list of {@link ToDoListItem}.
     */
    @Query("select g from ToDoListItem g where g.id in(:listId)")
    Page<ToDoListItem> getToDoListByListOfIdPageable(List<Long> listId, Pageable pageable);

    /**
     * Method returns {@link ToDoListItem} by list item id.
     *
     * @param listId habit id
     * @return list of {@link ToDoListItem}.
     */
    @Query("select g from ToDoListItem g where g.id in( :listId )")
    List<ToDoListItem> getToDoListByListOfId(List<Long> listId);

    /**
     * Method returns list of {@link ToDoListItem} by habit assign id.
     *
     * @param habitAssignId id of {@link greencity.entity.HabitAssign} current habit
     *                      assign
     * @return list of {@link ToDoListItem}.
     */
    @Query(nativeQuery = true,
        value = "SELECT tdli.* FROM to_do_list_items tdli "
            + "JOIN user_to_do_list ustdl ON ustdl.to_do_list_item_id = tdli.id "
            + "WHERE ustdl.is_custom_item = false "
            + "AND ustdl.habit_assign_id = :habitAssignId")
    List<ToDoListItem> findAllByHabitAssignId(Long habitAssignId);
}
