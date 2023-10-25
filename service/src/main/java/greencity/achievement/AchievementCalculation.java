package greencity.achievement;

import greencity.constant.ErrorMessage;
import greencity.dto.achievement.AchievementVO;
import greencity.dto.achievementcategory.AchievementCategoryVO;
import greencity.dto.user.UserVO;
import greencity.dto.useraction.UserActionVO;
import greencity.entity.Achievement;
import greencity.entity.AchievementCategory;
import greencity.entity.User;
import greencity.entity.UserAchievement;
import greencity.enums.AchievementCategoryType;
import greencity.enums.AchievementAction;
import greencity.enums.RatingCalculationEnum;
import greencity.rating.RatingCalculation;
import greencity.repository.AchievementRepo;
import greencity.repository.UserAchievementRepo;
import greencity.repository.UserRepo;
import greencity.repository.AchievementCategoryRepo;
import greencity.service.AchievementCategoryService;
import greencity.service.AchievementService;
import greencity.service.UserActionService;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;

@Component
public class AchievementCalculation {
    private UserActionService userActionService;
    private AchievementService achievementService;
    private AchievementCategoryService achievementCategoryService;
    private UserAchievementRepo userAchievementRepo;
    private final UserRepo userRepo;
    private final AchievementRepo achievementRepo;
    private final RatingCalculation ratingCalculation;
    private final AchievementCategoryRepo achievementCategoryRepo;
    private final ModelMapper modelMapper;

    /**
     * Constructor for initializing the required services and repositories.
     */
    public AchievementCalculation(
        UserActionService userActionService,
        @Lazy AchievementService achievementService,
        AchievementCategoryService achievementCategoryService,
        UserAchievementRepo userAchievementRepo,
        UserRepo userRepo,
        AchievementRepo achievementRepo, RatingCalculation ratingCalculation,
        AchievementCategoryRepo achievementCategoryRepo, ModelMapper modelMapper) {
        this.userActionService = userActionService;
        this.achievementService = achievementService;
        this.achievementCategoryService = achievementCategoryService;
        this.userAchievementRepo = userAchievementRepo;
        this.userRepo = userRepo;
        this.achievementRepo = achievementRepo;
        this.ratingCalculation = ratingCalculation;
        this.achievementCategoryRepo = achievementCategoryRepo;
        this.modelMapper = modelMapper;
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
        UserActionVO userActionVO =
            userActionService.findUserActionByUserIdAndAchievementCategory(user.getId(), achievementCategoryVO.getId());
        int count = updateCount(userActionVO, achievementAction);
        userActionService.updateUserActions(userActionVO);
        if (achievementAction.equals(AchievementAction.ASSIGN)) {
            saveAchievementToUser(user, achievementCategoryVO.getId(), count);
        } else if (achievementAction.equals(AchievementAction.DELETE)) {
            deleteAchievementFromUser(user, achievementCategoryVO.getId());
        }
    }

    private int updateCount(UserActionVO userActionVO, AchievementAction achievementAction) {
        int count = achievementAction.equals(AchievementAction.ASSIGN) ? userActionVO.getCount() + 1
            : userActionVO.getCount() - 1;
        userActionVO.setCount(count);
        return count;
    }

    private void saveAchievementToUser(UserVO userVO, Long achievementCategoryId, int count) {
        AchievementVO achievementVO = achievementService.findByCategoryIdAndCondition(achievementCategoryId, count);
        if (achievementVO != null) {
            Achievement achievement =
                achievementRepo.findByAchievementCategoryIdAndCondition(achievementCategoryId, count)
                    .orElseThrow(() -> new NoSuchElementException(
                        ErrorMessage.ACHIEVEMENT_CATEGORY_NOT_FOUND_BY_ID + achievementCategoryId));
            User user = modelMapper.map(userVO, User.class);
            UserAchievement userAchievement = UserAchievement.builder()
                .achievement(achievement)
                .user(user)
                .build();
            RatingCalculationEnum reason = RatingCalculationEnum.findByName(achievement.getTitle());
            ratingCalculation.ratingCalculation(reason, userVO);
            userAchievementRepo.save(userAchievement);
            calculateAchievement(userVO, AchievementCategoryType.ACHIEVEMENT, AchievementAction.ASSIGN);
        }
    }

    private void deleteAchievementFromUser(UserVO user, Long achievementCategoryId) {
        List<Achievement> achievements =
            achievementRepo.findUnAchieved(user.getId(), achievementCategoryId);
        achievements.forEach(achievement -> {
            RatingCalculationEnum reason = RatingCalculationEnum.findByName("UNDO_" + achievement.getTitle());
            AchievementCategory achievementCategory = achievementCategoryRepo.findByName("ACHIEVEMENT");
            UserActionVO userActionVO =
                userActionService.findUserActionByUserIdAndAchievementCategory(user.getId(),
                    achievementCategory.getId());
            updateCount(userActionVO, AchievementAction.DELETE);
            userActionService.updateUserActions(userActionVO);
            ratingCalculation.ratingCalculation(reason, user);
            Long achievementId = achievement.getId();
            Long userId = user.getId();

            userAchievementRepo.deleteByUserAndAchievemntId(userId, achievementId);
        });
    }
}
