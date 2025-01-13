package greencity.achievement;

import greencity.constant.ErrorMessage;
import greencity.dto.achievement.AchievementVO;
import greencity.dto.achievementcategory.AchievementCategoryVO;
import greencity.dto.user.UserVO;
import greencity.dto.useraction.UserActionVO;
import greencity.entity.Achievement;
import greencity.entity.User;
import greencity.entity.UserAchievement;
import greencity.enums.AchievementAction;
import greencity.enums.AchievementCategoryType;
import greencity.exception.exceptions.NotFoundException;
import greencity.rating.RatingCalculation;
import greencity.repository.AchievementRepo;
import greencity.repository.HabitRepo;
import greencity.repository.RatingPointsRepo;
import greencity.repository.UserAchievementRepo;
import greencity.service.AchievementCategoryService;
import greencity.service.AchievementService;
import greencity.service.UserActionService;
import jakarta.transaction.Transactional;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import static greencity.constant.AppConstant.SELF_ACHIEVEMENT_CATEGORY;

@Component
public class AchievementCalculation {
    private final UserActionService userActionService;
    private final AchievementService achievementService;
    private final AchievementCategoryService achievementCategoryService;
    private final UserAchievementRepo userAchievementRepo;
    private final AchievementRepo achievementRepo;
    private final RatingCalculation ratingCalculation;
    private final ModelMapper modelMapper;
    private final HabitRepo habitRepo;
    private final RatingPointsRepo ratingPointsRepo;

    /**
     * Constructor for initializing the required services and repositories.
     */
    public AchievementCalculation(
        UserActionService userActionService,
        @Lazy AchievementService achievementService,
        AchievementCategoryService achievementCategoryService,
        UserAchievementRepo userAchievementRepo,
        AchievementRepo achievementRepo, RatingCalculation ratingCalculation, ModelMapper modelMapper,
        HabitRepo habitRepo, RatingPointsRepo ratingPointsRepo) {
        this.userActionService = userActionService;
        this.achievementService = achievementService;
        this.achievementCategoryService = achievementCategoryService;
        this.userAchievementRepo = userAchievementRepo;
        this.achievementRepo = achievementRepo;
        this.ratingCalculation = ratingCalculation;
        this.modelMapper = modelMapper;
        this.habitRepo = habitRepo;
        this.ratingPointsRepo = ratingPointsRepo;
    }

    /**
     * Calculates the achievement based on the user's action.
     *
     * @param user              The user for whom the achievement needs to be
     *                          calculated.
     * @param category          The category of the achievement.
     * @param achievementAction The type of action (e.g., ASSIGN, DELETE).
     */
    @Transactional
    public void calculateAchievement(UserVO user, AchievementCategoryType category,
        AchievementAction achievementAction) {
        AchievementCategoryVO achievementCategoryVO = achievementCategoryService.findByName(category.name());
        int count = updateUserActionCount(user, achievementCategoryVO.getId(), achievementAction, null);
        if (AchievementAction.ASSIGN == achievementAction) {
            saveAchievementToUser(user, achievementCategoryVO, count, null);
        } else if (AchievementAction.DELETE == achievementAction) {
            deleteAchievementFromUser(user, achievementCategoryVO, null);
        }
    }

    /**
     * Calculates the achievement based on the user's action.
     *
     * @param user              The user for whom the achievement needs to be
     *                          calculated.
     * @param category          The category of the achievement.
     * @param achievementAction The type of action (e.g., ASSIGN, DELETE).
     * @param habitId           The ID of the habit related to the achievement.
     */
    @Transactional
    public void calculateAchievement(UserVO user, AchievementCategoryType category,
        AchievementAction achievementAction, Long habitId) {
        AchievementCategoryVO achievementCategoryVO = achievementCategoryService.findByName(category.name());
        int count = updateUserActionCount(user, achievementCategoryVO.getId(), achievementAction, habitId);
        if (AchievementAction.ASSIGN == achievementAction) {
            saveAchievementToUser(user, achievementCategoryVO, count, habitId);
        } else if (AchievementAction.DELETE == achievementAction) {
            deleteAchievementFromUser(user, achievementCategoryVO, habitId);
        }
    }

    private void saveAchievementToUser(UserVO userVO, AchievementCategoryVO ac, int count, Long habitId) {
        var achievementCategoryId = ac.getId();
        AchievementVO achievementVO = achievementService.findByCategoryIdAndCondition(achievementCategoryId, count);
        if (achievementVO != null) {
            Achievement achievement =
                achievementRepo.findByAchievementCategoryIdAndCondition(achievementCategoryId, count)
                    .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.ACHIEVEMENT_CATEGORY_NOT_FOUND_BY_ID + achievementCategoryId));
            UserAchievement userAchievement = UserAchievement.builder()
                .achievement(achievement)
                .user(modelMapper.map(userVO, User.class))
                .build();
            if (habitId != null) {
                userAchievement.setHabit(habitRepo.findById(habitId).orElseThrow(() -> new NotFoundException(
                    ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId)));
            }
            ratingCalculation.ratingCalculation(ratingPointsRepo.findByNameOrThrow(achievement.getTitle()), userVO);
            userAchievementRepo.save(userAchievement);

            if (!ac.getName().equals(SELF_ACHIEVEMENT_CATEGORY)) {
                calculateAchievement(userVO, AchievementCategoryType.ACHIEVEMENT, AchievementAction.ASSIGN);
            }
        }
    }

    private void deleteAchievementFromUser(UserVO user, AchievementCategoryVO ac, Long habitId) {
        List<Achievement> achievements;
        var achievementCategoryId = ac.getId();
        if (habitId != null) {
            achievements =
                achievementRepo.findUnAchieved(user.getId(), achievementCategoryId, habitId);
        } else {
            achievements = achievementRepo.findUnAchieved(user.getId(), achievementCategoryId);
        }
        if (!achievements.isEmpty()) {
            achievements.forEach(achievement -> {
                ratingCalculation.ratingCalculation(
                    ratingPointsRepo.findByNameOrThrow("UNDO_" + achievement.getTitle()),
                    user);
                userAchievementRepo.deleteByUserAndAchievementId(user.getId(), achievement.getId());
            });

            if (!ac.getName().equals(SELF_ACHIEVEMENT_CATEGORY)) {
                calculateAchievement(user, AchievementCategoryType.ACHIEVEMENT, AchievementAction.DELETE);
            }
        }
    }

    private int updateUserActionCount(UserVO user, Long achievementCategoryVOId,
        AchievementAction achievementAction, Long habitId) {
        UserActionVO userActionVO =
            habitId == null ? (userActionService.findUserAction(user.getId(), achievementCategoryVOId))
                : (userActionService.findUserAction(user.getId(), achievementCategoryVOId, habitId));
        if (userActionVO == null) {
            userActionVO = userActionService.createUserAction(user.getId(), achievementCategoryVOId, habitId);
        }
        int count = userActionVO.getCount() + ((AchievementAction.ASSIGN == achievementAction) ? 1 : -1);
        count = Math.max(count, 0);
        userActionVO.setCount(count);
        userActionService.updateUserActions(userActionVO);
        return count;
    }
}
