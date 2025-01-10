package greencity.service;

import greencity.dto.achievementcategory.AchievementCategoryVO;
import greencity.dto.user.UserVO;
import greencity.dto.useraction.UserActionVO;
import greencity.dto.habit.HabitVO;

public interface UserActionService {
    /**
     * Method saves {@link UserActionVO}.
     *
     * @param userActionVO {@link UserActionVO}
     * @return {@link UserActionVO}
     * @author Orest Mamchuk
     */
    UserActionVO save(UserActionVO userActionVO);

    /**
     * Method updates {@link UserActionVO}.
     *
     * @param userActionVO {@link UserActionVO}
     * @return {@link UserActionVO}
     * @author Orest Mamchuk
     */
    UserActionVO updateUserActions(UserActionVO userActionVO);

    /**
     * Method find {@link UserActionVO} by id.
     *
     * @param userId     of {@link UserVO}
     * @param categoryId of {@link AchievementCategoryVO}
     * @return {@link UserActionVO}
     * @author Orest Mamchuk
     */
    UserActionVO findUserAction(Long userId, Long categoryId);

    /**
     * Method finds {@link UserActionVO} by userId, achievementCategoryId, and
     * habitId.
     *
     * @param userId     the ID of {@link UserVO}
     * @param categoryId the ID of {@link AchievementCategoryVO}
     * @param habitId    the ID of {@link HabitVO}
     * @return UserAction {@link UserActionVO}
     * @author Oksana Spodaryk
     */
    UserActionVO findUserAction(Long userId, Long categoryId, Long habitId);

    /**
     * Creates a new user action record in the system. This method is responsible
     * for persisting a new user action associated with the specified user,
     * category, and habit. It encapsulates the creation logic and ensures that the
     * action is valid and properly recorded in the database.
     *
     * @param userId     The ID of the user performing the action.
     * @param categoryId The ID of the category associated with the action.
     * @param habitId    The ID of the habit related to the action.
     * @return UserActionVO An object representing the newly created user action.
     */
    UserActionVO createUserAction(Long userId, Long categoryId, Long habitId);
}
