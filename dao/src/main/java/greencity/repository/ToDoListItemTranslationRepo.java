package greencity.repository;

import greencity.entity.localization.ToDoListItemTranslation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ToDoListItemTranslationRepo extends JpaRepository<ToDoListItemTranslation, Long> {
    /**
     * Method for getting all to-do list item translations for given language.
     *
     * @param languageCode code of needed language
     * @return List of {@link ToDoListItemTranslation}, that contains all
     *         to-do list item translations for needed language.
     */
    List<ToDoListItemTranslation> findAllByLanguageCode(String languageCode);

    /**
     * Method returns to-do list item translation for particular selected item
     * for specific user and language code.
     *
     * @param itemId       target user id
     * @param languageCode code of needed language
     * @return {@link ToDoListItemTranslation}
     */
    @Query(nativeQuery = true, value = "SELECT * FROM to_do_list_item_translations as g "
        + "where g.to_do_list_item_id = (SELECT ug.to_do_list_item_id FROM user_to_do_list as ug WHERE "
        + "ug.id=:itemId) AND g.language_id = (SELECT id FROM languages l where l.code =:languageCode)")
    ToDoListItemTranslation findByLangAndUserToDoListItemId(String languageCode, Long itemId);

    /**
     * Method for getting to-do list translations for given habit in specific
     * language.
     *
     * @param languageCode code of needed language
     * @param habitId      code of needed language
     * @return List of {@link ToDoListItemTranslation}, that contains all
     *         to-do list item translations for needed habit.
     */
    @Query("SELECT gt FROM ToDoListItemTranslation gt JOIN ToDoListItem g ON g.id = gt.toDoListItem.id "
        + "JOIN g.habits h ON h.id = :habitId"
        + " WHERE gt.language.code = :languageCode")
    List<ToDoListItemTranslation> findToDoListByHabitIdAndByLanguageCode(String languageCode,
                                                                         @Param(value = "habitId") Long habitId);
}
