package greencity.controller;

import greencity.constant.HttpStatuses;
import greencity.dto.achievement.AchievementDTO;
import greencity.dto.achievement.AchievementNotification;
import greencity.dto.achievement.AchievementVO;
import greencity.dto.user.UserVO;
import greencity.enums.AchievementCategoryType;
import greencity.enums.AchievementType;
import greencity.service.AchievementService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/achievements")
public class AchievementController {
    private final AchievementService achievementService;

    /**
     * Constructor with parameters.
     */
    @Autowired
    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    /**
     * Method returns all achievements, available for achieving.
     *
     * @return list of {@link AchievementDTO}
     */
    @ApiOperation(value = "Get all achievements.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
    })
    @GetMapping("")
    public ResponseEntity<List<AchievementVO>> getAll() {
        return ResponseEntity.status(HttpStatus.OK).body(achievementService.findAll());
    }

    /**
     * Method notifies of the achievement.
     *
     * @param userId of {@link UserVO}
     * @return list {@link AchievementNotification}
     */
    @ApiOperation(value = "Get all the achievements that need to notify.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
    })
    @GetMapping("/notification/{userId}")
    public ResponseEntity<List<AchievementNotification>> getNotification(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(achievementService.findAchievementsWithStatusActive(userId));
    }

    /**
     * Method for achievement calculation.
     */
    @ApiOperation(value = "Calculate achievements.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
    })
    @PostMapping("/calculate-achievement")
    public ResponseEntity<HttpStatus> calculateAchievements(@RequestParam Long id,
        @RequestParam AchievementType setter,
        @RequestParam AchievementCategoryType socialNetwork,
        @RequestParam int size) {
        achievementService.calculateAchievements(id, setter, socialNetwork, size);
        return ResponseEntity.ok().build();
    }
}
