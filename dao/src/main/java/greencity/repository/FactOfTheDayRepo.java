package greencity.repository;

import greencity.dto.tag.TagDto;
import greencity.entity.FactOfTheDay;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FactOfTheDayRepo extends JpaRepository<FactOfTheDay, Long> {
    /**
     * Method finds {@link FactOfTheDay} that satisfy search query.
     *
     * @param searchQuery query to search
     * @return pageable of fact of the day
     */
    @Query("SELECT DISTINCT fd FROM FactOfTheDay AS fd JOIN FactOfTheDayTranslation "
        + "AS fdt ON fd.id = fdt.factOfTheDay.id "
        + "WHERE concat(fd.id, '') like lower(CONCAT(:searchQuery, '')) or "
        + "lower(fd.name) like lower(CONCAT('%', :searchQuery, '%')) "
        + "or lower(fdt.content) like lower(CONCAT('%', :searchQuery, '%')) "
        + "or CONCAT(fdt.id,'') like lower(CONCAT(:searchQuery, '')) ")
    Page<FactOfTheDay> searchBy(Pageable pageable, String searchQuery);

    /**
     * Retrieves a random {@link FactOfTheDay} based on the provided tag IDs. Uses a
     * native SQL query to optimize performance.
     *
     * @param tagsId a set of tag IDs associated with facts of the day
     * @return an {@link Optional} containing a random {@link FactOfTheDay}, if
     *         found
     */
    @Query(nativeQuery = true, value = "SELECT * FROM fact_of_the_day AS fd "
        + "INNER JOIN fact_of_the_day_tags ft ON fd.id = ft.fact_of_the_day_id "
        + "AND ft.tag_id IN :tagsId "
        + "ORDER BY RANDOM() LIMIT 1")
    Optional<FactOfTheDay> getRandomFactOfTheDay(Set<Long> tagsId);

    /**
     * Retrieves a set of {@link TagDto} for a specific Fact of the Day by its ID.
     *
     * @param id the ID of the fact of the day
     * @return a set of {@link TagDto} associated with the fact of the day
     */
    @Query("SELECT DISTINCT new greencity.dto.tag.TagDto(t.id, tt.name, l.code) "
        + "FROM FactOfTheDay f "
        + "JOIN f.tags t "
        + "JOIN t.tagTranslations tt "
        + "JOIN tt.language l "
        + "WHERE f.id = :id")
    Set<TagDto> findTagsByFactOfTheDayId(Long id);

    /**
     * Retrieves all tags related to either Facts of the Day or Habits.
     *
     * @return a set of {@link TagDto} representing tags for Facts of the Day and
     *         Habits
     */
    @Query("SELECT DISTINCT new greencity.dto.tag.TagDto(t.id, tt.name, l.code) "
        + "FROM Tag t "
        + "JOIN t.tagTranslations tt "
        + "JOIN tt.language l "
        + "WHERE t.type = 'FACT_OF_THE_DAY' or t.type = 'HABIT'")
    Set<TagDto> findAllFactOfTheDayAndHabitTags();
}
