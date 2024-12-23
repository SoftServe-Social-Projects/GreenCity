package greencity.webcontroller;

import greencity.constant.HttpStatuses;
import greencity.dto.habitstatistic.HabitInterestStatisticsDto;
import greencity.service.HabitStatisticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
     * @return A ResponseEntity containing user interest statistics.
     */
    @Operation(summary = "Retrieve user interest statistics.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK)
    })
    @GetMapping("/interest")
    public ResponseEntity<HabitInterestStatisticsDto> getUserInterestStatistics() {
        return ResponseEntity.ok(habitStatisticService.calculateUserInterest());
    }

    // todo controller for user activity statistic

    // todo controller for user activity statistic
}
