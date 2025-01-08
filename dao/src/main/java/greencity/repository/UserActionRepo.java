package greencity.repository;

import greencity.entity.AchievementCategory;
import greencity.entity.Habit;
import greencity.entity.User;
import greencity.entity.UserAction;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserActionRepo extends JpaRepository<UserAction, Long> {
    /**
     * Method find {@link UserAction} by userId and achievementCategoryId.
     *
     * @param userId                of {@link User}
     * @param achievementCategoryId of {@link AchievementCategory}
     * @return UserAction {@link UserAction}
     * @author Orest Mamchuk
     */
    @Query(value = "SELECT ua FROM UserAction ua "
        + "WHERE ua.achievementCategory.id = :achievementCategoryId "
        + "AND ua.user.id = :userId")
    UserAction findByUserIdAndAchievementCategoryId(Long userId, Long achievementCategoryId);

    /**
     * Method finds {@link UserAction} by userId, achievementCategoryId, and
     * habitId.
     *
     * @param userId                the ID of {@link User}
     * @param achievementCategoryId the ID of {@link AchievementCategory}
     * @param habitId               the ID of {@link Habit}
     * @return UserAction {@link UserAction}
     * @author Oksana Spodaryk
     */
    @Query(value = "SELECT ua FROM UserAction ua "
        + "WHERE ua.achievementCategory.id = :achievementCategoryId "
        + "AND ua.user.id = :userId "
        + "AND ua.habit.id = :habitId")
    UserAction findByUserIdAndAchievementCategoryIdAndHabitId(Long userId, Long achievementCategoryId, Long habitId);

    /**
     * Method find {@link UserAction} by userId.
     *
     * @param userId of {@link User}
     * @return UserAction {@link UserAction}
     * @author Viktoriia Herchanivska
     */
    List<UserAction> findAllByUserId(Long userId);

    /**
     * Deletes all {@link UserAction} entities associated with the specified habit.
     *
     * @param habitId the ID of the {@link Habit}.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserAction ua WHERE ua.habit.id = :habitId")
    void deleteAllByHabitId(@Param("habitId") Long habitId);
}
