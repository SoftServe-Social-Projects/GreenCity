package greencity.repository;

import greencity.entity.Tag;
import greencity.entity.localization.TagTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TagTranslationRepo extends JpaRepository<TagTranslation, Long> {
    /**
     * Method that deletes all tag translations by given ids of tag.
     *
     * @param tagIds - list of {@link Long}.
     */
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM tag_translations WHERE tag_id IN (:tagIds)")
    void bulkDeleteByTagId(List<Long> tagIds);

    /**
     * Method that allow you to find all EcoNews {@link Tag}s.
     *
     * @return list of {@link Tag}'s names
     */
    @Query(nativeQuery = true,
        value = "SELECT DISTINCT tt.* FROM tag_translations AS tt "
            + "INNER JOIN tags AS t ON tt.tag_id = t.id "
            + "INNER JOIN languages AS l ON l.id = tt.language_id "
            + "WHERE t.type LIKE 'ECO_NEWS' "
            + "AND l.code = :languageCode ORDER BY tt.tag_id")
    List<TagTranslation> findAllEcoNewsTags(String languageCode);
}
