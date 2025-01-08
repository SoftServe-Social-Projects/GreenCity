package greencity.repository;

import greencity.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserAchievementRepo extends JpaRepository<UserAchievement, Long> {
    /**
     * Retrieves a list of UserAchievement objects associated with a given user ID.
     *
     * @param userId The unique identifier of the user for whom to fetch the
     *               achievements.
     * @return A list of UserAchievement objects related to the specified user ID.
     */
    List<UserAchievement> getUserAchievementByUserId(Long userId);

    /**
     * Deletes a user's achievement record based on the provided user ID and
     * achievement ID.
     *
     * @param userId        The unique identifier of the user.
     * @param achievementId The unique identifier of the achievement to be deleted.
     */
    @Modifying
    @Query(value = "DELETE FROM user_achievements "
        + "WHERE user_id = :userId AND achievement_id = :achievementId", nativeQuery = true)
    void deleteByUserAndAchievementId(Long userId, Long achievementId);

    /**
     * Retrieves a list of UserAchievement objects associated with a given user ID
     * and achievement category ID.
     *
     * @param userId                The unique identifier of the user for whom to
     *                              fetch the achievements.
     * @param achievementCategoryId The unique identifier of the achievement
     *                              category to search the achievements in.
     * @return A list of UserAchievement objects related to the specified user ID
     *         and achievement category ID.
     */
    List<UserAchievement> findAllByUserIdAndAchievement_AchievementCategoryId(Long userId, Long achievementCategoryId);
}
