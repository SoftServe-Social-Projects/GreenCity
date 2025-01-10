package greencity.repository;

import greencity.entity.Tag;
import greencity.entity.localization.TagTranslation;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import greencity.enums.TagType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TagsRepo extends JpaRepository<Tag, Long>, JpaSpecificationExecutor<Tag> {
    /**
     * Method finds all tags and fetches theirs translations.
     *
     * @param pageable {@link Pageable}
     * @return list of tags {@link Page}
     * @author Markiyan Derevetskyi
     */
    @Query(value = "SELECT t FROM Tag t JOIN FETCH t.tagTranslations ORDER BY t.id",
        countQuery = "SELECT COUNT(t) FROM Tag t")
    Page<Tag> findAll(Pageable pageable);

    /**
     * Method that finds tag by given id.
     *
     * @param id - {link Long}
     * @return {@link Optional} of found {@link Tag}
     */
    @Query("SELECT t from Tag t JOIN FETCH t.tagTranslations WHERE t.id = :id")
    Optional<Tag> findById(Long id);

    /**
     * Method that allow you to find list of {@link Tag}s by names and type.
     *
     * @param names   list of {@link String} values
     * @param tagType {@link String}
     * @return list of {@link Tag}
     */
    @Query("SELECT t FROM Tag t JOIN FETCH t.tagTranslations tt WHERE LOWER(tt.name) IN :names AND t.type = :tagType")
    List<Tag> findTagsByNamesAndType(List<String> names, TagType tagType);

    /**
     * Method that allow you to find list of {@link Tag}s with all translations by
     * names and type.
     *
     * @param names   list of {@link String} values
     * @param tagType {@link String}
     * @return list of {@link Tag}
     */
    @Query("SELECT t FROM Tag t WHERE t.id IN (SELECT tt.tag.id FROM t.tagTranslations tt "
        + "WHERE LOWER(tt.name) IN :names) AND t.type = :tagType")
    List<Tag> findAllByTagTranslations(List<String> names, TagType tagType);

    /**
     * Method that search tags by all fields using filter.
     *
     * @param pageable {@link Pageable}
     * @param filter   {@link String}
     * @return found tags {@link Page}
     */
    @Query(value = "SELECT DISTINCT t FROM Tag t LEFT JOIN FETCH t.tagTranslations AS tt "
        + "WHERE CONCAT(t.id, '') LIKE LOWER(CONCAT(:filter, '')) "
        + "OR LOWER(CONCAT(t.type, '')) LIKE LOWER(CONCAT('%', :filter, '%'))"
        + "OR CONCAT(tt.id, '') LIKE LOWER(CONCAT(:filter, '')) "
        + "OR LOWER(tt.language.code) LIKE LOWER(CONCAT('%', :filter, '%')) "
        + "OR LOWER(tt.name) LIKE LOWER(CONCAT('%', :filter, '%'))",
        countQuery = "SELECT COUNT(t) FROM Tag t")
    Page<Tag> filterByAllFields(Pageable pageable, String filter);

    /**
     * Method that allow you to find list of Tags by type and language code.
     *
     * @param tagType      {@link TagType}
     * @param languageCode {@link String}
     * @return list of tag's names.
     */
    @Query("select tt from TagTranslation tt join fetch tt.tag t join fetch tt.language l "
        + "where t.type = :tagType and l.code = :languageCode order by tt.id")
    List<TagTranslation> findTagsByTypeAndLanguageCode(TagType tagType, String languageCode);

    /**
     * Method that allow you to find list of Tags by type.
     *
     * @param tagType {@link TagType}
     * @return list of tag's names.
     */
    @Query("select t from Tag t "
        + "where t.type = :tagType order by t.id")
    List<Tag> findTagsByType(TagType tagType);

    /**
     * Method that finds all Habits {@link Tag}'s.
     *
     * @return list of {@link Tag}'s names
     * @author Markiyan Derevetskyi
     */
    @Query(nativeQuery = true,
        value = "SELECT DISTINCT tt.name FROM tag_translations tt "
            + "INNER JOIN habits_tags ent ON tt.tag_id = ent.tag_id "
            + "INNER JOIN languages l ON l.id = tt.language_id "
            + "WHERE l.code = :languageCode")
    List<String> findAllHabitsTags(String languageCode);

    /**
     * Method that deletes all tags by given ids.
     *
     * @param ids - list of {@link Long}.
     */
    @Modifying
    @Query("DELETE FROM Tag t WHERE t.id in :ids")
    void bulkDelete(List<Long> ids);

    /**
     * Finds a set of {@link Tag} entities by their IDs.
     *
     * @param tagIds list of tag IDs ({@link Long})
     * @return a set of {@link Tag} objects corresponding to the provided IDs
     */
    @Query("select t from Tag t "
        + "where t.id in :tagIds")
    Set<Tag> findTagsById(List<Long> tagIds);

    /**
     * Finds the IDs of tags associated with habits that are in progress for the
     * user with the specified email.
     *
     * @param email the email of the user
     * @return a set of tag IDs ({@link Long}) associated with the user's
     *         in-progress habits
     */
    @Query(nativeQuery = true,
        value = "SELECT t.id FROM tags t "
            + "INNER JOIN habits_tags ht ON t.id = ht.tag_id "
            + "INNER JOIN habits h ON ht.habit_id = h.id "
            + "INNER JOIN habit_assign ha ON h.id = ha.habit_id "
            + "INNER JOIN users u ON ha.user_id = u.id AND ha.status = 'INPROGRESS' "
            + "WHERE u.email = :email")
    Set<Long> findTagsIdByUserHabitsInProgress(String email);
}
