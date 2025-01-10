package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.achievement.AchievementVO;
import greencity.dto.achievement.AchievementPostDto;
import greencity.dto.achievement.AchievementManagementDto;
import greencity.dto.achievement.ActionDto;
import greencity.enums.AchievementStatus;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AchievementService {
    /**
     * Find {@link AchievementVO} for management by page .
     *
     * @param page a value with pageable configuration.
     * @return a dto of {@link PageableAdvancedDto}.
     * @author Orest Mamchuk
     */
    PageableAdvancedDto<AchievementVO> findAll(Pageable page);

    /**
     * {@inheritDoc} Method that allow you to save new achievement
     *
     * @param achievementPostDto a value of {@link AchievementVO}
     * @return a dto of {@link AchievementVO}
     * @author Orest Mamchuk
     */
    AchievementVO save(AchievementPostDto achievementPostDto);

    /**
     * Method for getting {@link AchievementVO} by search query.
     *
     * @param paging {@link Pageable}.
     * @param query  query to search,
     * @return {@link PageableAdvancedDto} of {@link AchievementVO} instances.
     * @author Orest Mamchuk
     */
    PageableAdvancedDto<AchievementVO> searchAchievementBy(Pageable paging, String query);

    /**
     * Method delete {@link AchievementVO} by id.
     *
     * @param id Long
     * @return id of deleted {@link AchievementVO}
     * @author Orest Mamchuk
     */
    Long delete(Long id);

    /**
     * Method that deletes all achievement by given ids.
     *
     * @param ids - list of {@link Long}
     * @author Orest Mamchuk
     */
    void deleteAll(List<Long> ids);

    /**
     * Method updates {@link AchievementVO}.
     *
     * @param achievementManagementDto {@link AchievementManagementDto}
     * @return instance of {@link AchievementPostDto}
     * @author Orest Mamchuk
     */
    AchievementPostDto update(AchievementManagementDto achievementManagementDto);

    /**
     * Method find {@link AchievementVO} by categoryId and condition.
     *
     * @param categoryId of {@link AchievementVO}
     * @param condition  of {@link AchievementVO}
     * @return {@link AchievementVO}
     * @author Orest Mamchuk
     */
    AchievementVO findByCategoryIdAndCondition(Long categoryId, Integer condition);

    /**
     * Retrieves a list of achievements based on the given type and the principal's
     * email.
     *
     * @param principalEmail        The email of the principal (usually the
     *                              logged-in user) for whom the achievements need
     *                              to be fetched.
     * @param achievementStatus     The status of the achievements to filter by
     *                              (e.g., "ACHIEVED", "UNACHIEVED").
     * @param achievementCategoryId The ID of the achievement category to filter by
     *                              category.
     * @return List AchievementVO Returns a list of achievements matching the given
     *         criteria.
     */
    List<AchievementVO> findAllByTypeAndCategory(String principalEmail, AchievementStatus achievementStatus,
        Long achievementCategoryId);

    /**
     * Method for achieve.
     */
    void achieve(ActionDto user);

    /**
     * Retrieves a quantity of achievements based on the given type and the
     * principal's email.
     *
     * @param principalEmail        The email of the principal (usually the
     *                              logged-in user) for whom the achievements need
     *                              to be fetched.
     * @param achievementStatus     The status of the achievements to filter by
     *                              (e.g., "ACHIEVED", "UNACHIEVED").
     * @param achievementCategoryId The ID of the achievement category to filter by
     *                              category.
     * @return Integer Returns a quantity of achievements matching the given
     *         criteria.
     */
    Integer findAchievementCountByTypeAndCategory(String principalEmail, AchievementStatus achievementStatus,
        Long achievementCategoryId);
}
