package greencity.service;

import greencity.dto.achievementcategory.AchievementCategoryDto;
import greencity.dto.achievementcategory.AchievementCategoryTranslationDto;
import greencity.dto.achievementcategory.AchievementCategoryVO;
import java.util.List;

public interface AchievementCategoryService {
    /**
     * Method for saving new {@link AchievementCategoryVO} to database.
     *
     * @param achievementCategoryDto - dto for {@link AchievementCategoryVO} entity.
     * @return a {@link AchievementCategoryVO}.
     * @author Orest Mamchuk
     */
    AchievementCategoryVO save(AchievementCategoryDto achievementCategoryDto);

    /**
     * Method for finding all {@link AchievementCategoryTranslationDto} with at
     * least one achievement in category.
     *
     * @return list of {@link AchievementCategoryTranslationDto}.
     * @author Viktoriia Herchanivska
     */
    List<AchievementCategoryTranslationDto> findAllWithAtLeastOneAchievement(String email);

    /**
     * Method for finding all {@link AchievementCategoryVO}.
     *
     * @return list of {@link AchievementCategoryVO}.
     * @author Orest Mamchuk
     */
    List<AchievementCategoryVO> findAllForManagement();

    /**
     * Method for finding {@link AchievementCategoryVO}.
     *
     * @param name - name category for {@link AchievementCategoryVO}
     * @return {@link AchievementCategoryVO}.
     * @author Orest Mamchuk
     */
    AchievementCategoryVO findByName(String name);
}
