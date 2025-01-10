package greencity.webcontroller;

import greencity.dto.user.UserEmailPreferencesStatisticDto;
import greencity.dto.user.UserLocationStatisticDto;
import greencity.dto.user.UserRegistrationStatisticDto;
import greencity.dto.user.UserRoleStatisticDto;
import greencity.dto.user.UserStatusStatisticDto;
import greencity.service.ManagementUserStatisticsService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.List;

@Validated
@Controller
@AllArgsConstructor
@RequestMapping("/management/user/statistics")
public class ManagementUserStatisticsController {
    private final ManagementUserStatisticsService managementUserStatisticsService;

    @GetMapping
    public String getStatisticsPage() {
        return "core/management_user_statistics";
    }

    @GetMapping(value = "/registration", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserRegistrationStatisticDto>> getRegistrationStatistic(
        @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
        @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
        @RequestParam("granularity") @NotBlank @NotNull String granularity) {
        List<UserRegistrationStatisticDto> registrationStats =
            managementUserStatisticsService.getUserRegistrationsByDateRange(startDate, endDate, granularity);
        if (registrationStats.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(registrationStats);
    }

    @GetMapping(value = "/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserRoleStatisticDto>> getUserRolesDistribution() {
        List<UserRoleStatisticDto> roles = managementUserStatisticsService.getUserRolesDistribution();
        if (roles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(roles);
    }

    @GetMapping(value = "/statuses", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserStatusStatisticDto>> getUserStatusesDistribution() {
        List<UserStatusStatisticDto> statuses = managementUserStatisticsService.getUserStatusesDistribution();
        if (statuses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(statuses);
    }

    @GetMapping(value = "/locations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserLocationStatisticDto>> getUserLocationDistribution(@RequestParam String groupBy) {
        List<UserLocationStatisticDto> locations =
            managementUserStatisticsService.getUserLocationsDistribution(groupBy);
        if (locations.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(locations);
    }

    @GetMapping(value = "/preferences", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserEmailPreferencesStatisticDto>> getUserPreferencesDistribution() {
        List<UserEmailPreferencesStatisticDto> preferences =
            managementUserStatisticsService.getUserEmailPreferencesDistribution();
        if (preferences.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(preferences);
    }
}
