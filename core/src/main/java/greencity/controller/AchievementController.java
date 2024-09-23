package greencity.controller;

import greencity.constant.HttpStatuses;
import greencity.dto.achievement.AchievementVO;
import greencity.dto.achievement.ActionDto;
import greencity.dto.achievementcategory.AchievementCategoryTranslationDto;
import greencity.enums.AchievementStatus;
import greencity.service.AchievementCategoryService;
import greencity.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/achievements")
@RequiredArgsConstructor
public class AchievementController {
    private final AchievementService achievementService;
    private final AchievementCategoryService achievementCategoryService;

    /**
     * Method returns all achievements, available for achieving.
     *
     * @return list of {@link AchievementVO}
     */
    @Operation(summary = "Get all achievements by type.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
    })
    @GetMapping
    public ResponseEntity<List<AchievementVO>> getAll(@Parameter(hidden = true) Principal principal,
        @Parameter(description = "Available values : ACHIEVED, UNACHIEVED."
            + " Leave this field empty if you need items with any status") @RequestParam(
                required = false) AchievementStatus achievementStatus,
        @RequestParam(required = false) Long achievementCategoryId) {
        return ResponseEntity.ok().body(
            achievementService.findAllByTypeAndCategory(principal.getName(), achievementStatus, achievementCategoryId));
    }

    /**
     * Method to test socket (delete).
     */
    @MessageMapping("/achieve")
    public void achieve(@Payload ActionDto user) {
        achievementService.achieve(user);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getAchievementCount(@Parameter(hidden = true) Principal principal,
        @Parameter(description = "Available values : ACHIEVED, UNACHIEVED."
            + " Leave this field empty if you need items with any status") @RequestParam(
                required = false) AchievementStatus achievementStatus,
        @RequestParam(required = false) Long achievementCategoryId) {
        return ResponseEntity.ok().body(achievementService.findAchievementCountByTypeAndCategory(principal.getName(),
            achievementStatus, achievementCategoryId));
    }

    @GetMapping("/getAllCategories")
    public ResponseEntity<List<AchievementCategoryTranslationDto>> getAchievementCategories(
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.ok().body(achievementCategoryService.findAll());
    }
}