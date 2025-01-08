package greencity.repository;

import greencity.dto.achievement.StatisticsDto;
import greencity.entity.Achievement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepo extends JpaRepository<Achievement, Long> {
    /**
     * Retrieves a list of achievements that a specific user hasn't achieved yet
     * within a specified achievement category. The method identifies unachieved
     * achievements by comparing user actions count with the conditions of
     * achievements and by checking if a user already has the achievement in the
     * user_achievements table.
     *
     * @param userId                The unique identifier of the user.
     * @param achievementCategoryId The unique identifier of the achievement
     *                              category.
     * @return A list of Achievement objects that the user hasn't achieved within
     *         the specified category.
     */
    @Query(value = "SELECT ach.* "
        + "FROM achievements ach "
        + "WHERE ach.id IN ("
        + "    SELECT achievement_id "
        + "    FROM user_achievements uach "
        + "    WHERE uach.user_id = :userId"
        + "    AND uach.habit_id = :habitId"
        + ") "
        + "AND ach.condition > ("
        + "    SELECT MAX(ua.count) "
        + "    FROM user_actions ua "
        + "    WHERE ua.user_id = :userId "
        + "    AND ua.achievement_category_id = :achievementCategoryId "
        + "    AND ua.habit_id = :habitId"
        + ") "
        + "AND ach.achievement_category_id = :achievementCategoryId ",
        nativeQuery = true)
    List<Achievement> findUnAchieved(Long userId, Long achievementCategoryId, Long habitId);

    /**
     * Retrieves a list of achievements that a specific user hasn't achieved yet
     * within a specified achievement category. The method identifies unachieved
     * achievements by comparing user actions count with the conditions of
     * achievements and by checking if a user already has the achievement in the
     * user_achievements table.
     *
     * @param userId                The unique identifier of the user.
     * @param achievementCategoryId The unique identifier of the achievement
     *                              category.
     * @return A list of Achievement objects that the user hasn't achieved within
     *         the specified category.
     */
    @Query(value = """
        Select ach.*
        from achievements as ach
        where
            ach.id in
              (SELECT achievement_id from user_achievements uach where uach.user_id = :userId)
          and
            ach.condition > (SELECT ua.count
                                              from user_actions ua
                                              where ua.user_id = :userId and
                                                    ua.achievement_category_id=:achievementCategoryId)
        and
            ach.achievement_category_id=:achievementCategoryId""",
        nativeQuery = true)
    List<Achievement> findUnAchieved(Long userId, Long achievementCategoryId);

    /**
     * Searches for achievements based on a query string and returns a paginated
     * result.
     *
     * @param paging A Pageable object containing the pagination information (e.g.,
     *               page number, size, sort order).
     * @param query  The search query string to filter achievements.
     * @return A Page of Achievement objects that match the search query.
     */
    @Query("SELECT DISTINCT a FROM Achievement a "
        + "WHERE CONCAT(a.id,'') LIKE LOWER(CONCAT('%', :query, '%')) "
        + "OR LOWER(a.achievementCategory.name) LIKE LOWER(CONCAT('%', :query, '%'))"
        + "OR CONCAT(a.condition, ' ') LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Achievement> searchAchievementsBy(Pageable paging, String query);

    /**
     * Method find {@link Achievement} by categoryId and condition.
     *
     * @param achievementCategoryId of {@link Achievement}
     * @param condition             of {@link Achievement}
     * @return Achievement
     * @author Orest Mamchuk
     */
    Optional<Achievement> findByAchievementCategoryIdAndCondition(Long achievementCategoryId, Integer condition);

    /**
     * Searches for achievements that have not yet been achieved by the specified
     * user.
     *
     * @param userId The ID of the user for whom to find unachieved achievements.
     * @return A list of achievements that the user has not yet achieved.
     */
    @Query(value = "SELECT * from achievements "
        + "where id not in (select achievement_id "
        + "                 from user_achievements "
        + "                 where user_id = :userId)", nativeQuery = true)
    List<Achievement> searchAchievementsUnAchieved(Long userId);

    /**
     * Searches for achievements that have not yet been achieved by the specified
     * user and achievement category.
     *
     * @param userId                The ID of the user for whom to find unachieved
     *                              achievements.
     * @param achievementCategoryId The ID of the category to find unachieved
     *                              achievements in.
     * @return A list of achievements that the user has not yet achieved by
     *         specified category.
     */
    @Query(value = "SELECT * from achievements "
        + "where id not in (select achievement_id "
        + "                 from user_achievements "
        + "                 where user_id = :userId) "
        + "and achievement_category_id = :achievementCategoryId", nativeQuery = true)
    List<Achievement> searchAchievementsUnAchievedByCategory(Long userId, Long achievementCategoryId);

    /**
     * Method find {@link Achievement} by categoryId.
     *
     * @param achievementCategoryId of {@link Achievement}
     * @return Achievement
     * @author Viktoriia Herchanivska
     */
    List<Achievement> findAllByAchievementCategoryId(Long achievementCategoryId);

    /**
     * Method retrieves statistics of users who have achieved each achievement. Each
     * achievement's name (in English) and the count of users who achieved it are
     * returned.
     *
     * @return List of {@link StatisticsDto} with achievement names and user counts
     */
    @Query("SELECT new greencity.dto.achievement.StatisticsDto(a.nameEng, COUNT(ua.user))"
        + "FROM Achievement a "
        + "JOIN UserAchievement ua ON ua.achievement = a "
        + "GROUP BY a.nameEng ")
    List<StatisticsDto> getStatisticsUsersWithAchievements();

    /**
     * Method retrieves statistics of users grouped by achievement category. Each
     * category name and the count of users who achieved an achievement in that
     * category are returned.
     *
     * @return List of {@link StatisticsDto} with category names and user counts
     */
    @Query("SELECT new greencity.dto.achievement.StatisticsDto(ac.name, COUNT(ua.user))"
        + "FROM AchievementCategory ac "
        + "JOIN Achievement a ON a.achievementCategory = ac "
        + "JOIN UserAchievement ua ON ua.achievement = a "
        + "GROUP BY ac.name ")
    List<StatisticsDto> getStatisticsUsersWithAchievementsCategory();

    /**
     * Method retrieves the total count of users with and without achievements.
     * Users are categorized into "Users with Achievements" and "Users without
     * Achievements."
     *
     * @return List of {@link StatisticsDto} with user activity status and
     *         respective counts
     */
    @Query("SELECT new greencity.dto.achievement.StatisticsDto("
        + "CASE WHEN ua.user IS NULL THEN 'Users without Achievements' ELSE 'Users with Achievements' END, "
        + "COUNT(DISTINCT u)) "
        + "FROM User u "
        + "LEFT JOIN UserAchievement ua ON ua.user = u "
        + "GROUP BY CASE WHEN ua.user IS NULL THEN 'Users without Achievements' ELSE 'Users with Achievements' END")
    List<StatisticsDto> getUserActivityStatistics();
}
