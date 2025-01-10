package greencity.repository;

import greencity.entity.ToDoListItem;
import java.util.List;
import greencity.entity.localization.ToDoListItemTranslation;
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
     * Method returns ToDoList id which are not in the habit.
     *
     * @param habitId habit id
     * @return list of id.
     */
    @Query(nativeQuery = true,
        value = "select to_do_list_items.id from to_do_list_items  where id not in"
            + " (select to_do_list_item_id from habit_to_do_list_items where habit_id = :habitId and "
            + "habit_to_do_list_items.status like 'ACTUAL');")
    List<Long> getAllToDoListItemsByHabitIdNotContained(@Param("habitId") Long habitId);

    /**
     * Method returns to-do list items id which are in the habit.
     *
     * @param habitId habit id
     * @return list of id.
     */
    @Query(nativeQuery = true,
        value = "select to_do_list_item_id from habit_to_do_list_items  where habit_id = :habitId and "
            + " habit_to_do_list_items.status like 'ACTUAL';")
    List<Long> getAllToDoListItemIdByHabitIdISContained(@Param("habitId") Long habitId);

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
     * Method returns user's to-do list for active items and habits in progress.
     *
     * @param userId id of the {@link Long} current user
     * @param code   language code {@link String}
     * @return {@link ToDoListItemTranslation}
     */
    @Query("""
        select translations from UserToDoListItem as usli\s
        join HabitAssign as ha on ha.id = usli.habitAssign.id
        join ToDoListItemTranslation as translations on
        translations.toDoListItem.id = usli.toDoListItem.id
        join Language as lang on translations.language.id = lang.id
        where usli.status = 'INPROGRESS'
        and ha.status = 'INPROGRESS'
        and ha.user.id = :userId
        and lang.code = :code""")
    List<ToDoListItemTranslation> findInProgressByUserIdAndLanguageCode(@Param("userId") Long userId,
        @Param("code") String code);

    /**
     * Method returns {@link ToDoListItem} by habitId, list of name and language
     * code.
     *
     * @param habitId      habit id
     * @param itemNames    list of to-do items name
     * @param languageCode language code
     * @return list of {@link ToDoListItem}
     */
    @Query("SELECT sli FROM ToDoListItem sli "
        + "JOIN ToDoListItemTranslation slt ON sli.id = slt.toDoListItem.id "
        + "JOIN sli.habits h ON h.id = :habitId"
        + " WHERE slt.language.code = :languageCode AND slt.content in :listOfName")
    List<ToDoListItem> findByNames(@Param("habitId") Long habitId, @Param("listOfName") List<String> itemNames,
        String languageCode);
}
