package greencity.webcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.achievement.StatisticsDto;
import greencity.service.AchievementStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/management/achievement/statistics")
public class ManagementAchievementStatisticsController {
    private final AchievementStatisticsService achievementStatisticsService;

    /**
     * Method retrieves statistics of users by individual achievements and adds them
     * to the model. Renders the "core/management_achievement_statistics" page with
     * JSON-formatted data of achievements and user counts.
     *
     * @param model Spring Model object to pass attributes to the view
     * @return View name for achievement statistics
     */
    @GetMapping
    public String getAchievementStatistic(Model model) {
        List<StatisticsDto> statisticsDtos = achievementStatisticsService.statisticsUsersWithAchievements();
        ObjectMapper mapper = new ObjectMapper();
        try {
            model.addAttribute("statisticalData", mapper.writeValueAsString(statisticsDtos));
        } catch (Exception e) {
            log.error("An error occurred while processing: ", e);
        }
        return "core/management_achievement_statistics";
    }

    /**
     * Method retrieves statistics of users grouped by achievement categories.
     * Returns a JSON response with a list of categories and the count of users in
     * each.
     *
     * @return {@link ResponseEntity} containing a list of {@link StatisticsDto}
     *         with category names and user counts
     */
    @GetMapping("/by-category")
    public ResponseEntity<List<StatisticsDto>> getAchievementCategoryStatistic() {
        return ResponseEntity.ok(achievementStatisticsService.statisticsUsersWithAchievementsCategory());
    }

    /**
     * Method retrieves statistics of user activity based on achievements. Returns a
     * JSON response with a list categorizing users as "Users with Achievements" or
     * "Users without Achievements," along with their counts.
     *
     * @return {@link ResponseEntity} containing a list of {@link StatisticsDto}
     *         with user activity status and respective counts
     */
    @GetMapping("/by-activity")
    public ResponseEntity<List<StatisticsDto>> getUsersActivityStatistic() {
        return ResponseEntity.ok(achievementStatisticsService.statisticsUsersActivity());
    }
}
