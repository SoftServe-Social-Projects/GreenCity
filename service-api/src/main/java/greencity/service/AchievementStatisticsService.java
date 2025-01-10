package greencity.service;

import greencity.dto.achievement.StatisticsDto;
import java.util.List;

public interface AchievementStatisticsService {
    /**
     * Method retrieves statistics of users by individual achievements. Returns a
     * list of achievements with the number of users who achieved each.
     *
     * @return List of {@link StatisticsDto} with achievement names and user counts
     */
    List<StatisticsDto> statisticsUsersWithAchievements();

    /**
     * Method retrieves statistics of users grouped by achievement categories.
     * Returns a list of achievement categories with the number of users who
     * achieved something in each category.
     *
     * @return List of {@link StatisticsDto} with category names and user counts
     */
    List<StatisticsDto> statisticsUsersWithAchievementsCategory();

    /**
     * Method retrieves statistics of user activity based on achievements. Returns a
     * list categorizing users as "Users with Achievements" or "Users without
     * Achievements," along with their counts.
     *
     * @return List of {@link StatisticsDto} with user activity status and
     *         respective counts
     */
    List<StatisticsDto> statisticsUsersActivity();
}
