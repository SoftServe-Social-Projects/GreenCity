package greencity.webcontroller;

import greencity.constant.HttpStatuses;
import greencity.dto.habitstatistic.HabitDateCount;
import greencity.service.HabitStatisticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
@RequestMapping("/management/habit/statistics")
public class ManagementHabitStatisticController {
    HabitStatisticService habitStatisticService;

    @GetMapping
    public String getStatisticsPage() {
        return "core/management_habit_statistics";
    }

    /**
     * Endpoint to retrieve user interest statistics.
     *
     * @return ResponseEntity containing user interest statistics.
     */
    @Operation(summary = "Retrieve user interest statistics.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/interest")
    public ResponseEntity<Map<String, Long>> getUserInterestStatistics() {
        return ResponseEntity.ok(habitStatisticService.calculateUserInterest());
    }

    /**
     * Endpoint to retrieve habit behavior statistics.
     *
     * @return ResponseEntity containing habit behavior statistics.
     */
    @Operation(summary = "Retrieve statistics of how users behave with habits.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/habit-behavior")
    public ResponseEntity<Map<String, Long>> getHabitBehaviorStatistics() {
        return ResponseEntity.ok(habitStatisticService.calculateHabitBehaviorStatistic());
    }

    /**
     * Endpoint to retrieve user interaction with habits(creation, subscription).
     *
     * @return ResponseEntity containing habit behavior statistics.
     */
    @Operation(summary = "Retrieve statistics of how users interact with habits.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/user-interaction")
    public ResponseEntity<Map<String, List<HabitDateCount>>> getUserHabitInteractionStatistics(
        @RequestParam(defaultValue = "weekly") String range) {
        return ResponseEntity.ok(habitStatisticService.calculateInteractions(range));
    }
}
