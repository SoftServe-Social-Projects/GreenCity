package greencity.repository;

import greencity.entity.AchievementCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface AchievementCategoryRepo extends JpaRepository<AchievementCategory, Long> {
    /**
     * Finds {@link AchievementCategory} by name.
     *
     * @param name to find by.
     * @return a category by name.
     */
    Optional<AchievementCategory> findByName(String name);

    /**
     * Finds list of {@link AchievementCategory} with at least one achievement in
     * category.
     *
     * @return a list of categories with at least one achievement in category.
     */
    @Query(value = "SELECT ac.* FROM achievement_categories ac "
        + "WHERE ac.id IN ("
        + "    SELECT DISTINCT a.achievement_category_id "
        + "    FROM achievements a"
        + ")",
        nativeQuery = true)
    List<AchievementCategory> findAllWithAtLeastOneAchievement();
}
