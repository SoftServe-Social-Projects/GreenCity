package greencity.webcontroller;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.econews.EcoNewsAuthorStatisticDto;
import greencity.dto.econews.EcoNewsTagStatistic;
import greencity.service.ManagementEcoNewsStatisticsService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/management/econews/statistics")
public class ManagementEcoNewsStatisticsController {
    private ManagementEcoNewsStatisticsService ecoNewsStatisticService;

    @GetMapping
    public String getStatisticsPage() {
        return "core/management_econews_statistics";
    }

    @GetMapping("/publication/count")
    public ResponseEntity<Long> getPublicationCount() {
        return ResponseEntity.ok().body(ecoNewsStatisticService.getPublicationCount());
    }

    @GetMapping("/tags")
    public ResponseEntity<List<EcoNewsTagStatistic>> getTagStatistic() {
        List<EcoNewsTagStatistic> statisticList = ecoNewsStatisticService.getTagStatistics();
        if (statisticList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(statisticList);
    }

    @GetMapping("/user/activity")
    public String getUserActivityPage(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        Model model) {
        Pageable pageable = PageRequest.of(page, size);
        PageableAdvancedDto<EcoNewsAuthorStatisticDto> userStats =
            ecoNewsStatisticService.getEcoNewsAuthorStatistic(pageable);

        model.addAttribute("pageable", userStats);

        return "core/fragments/statistic/eco-news-user-activity-statistic :: userStatisticsTable";
    }
}
