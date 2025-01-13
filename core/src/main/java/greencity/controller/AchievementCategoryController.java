package greencity.controller;

import greencity.constant.HttpStatuses;
import greencity.dto.achievementcategory.AchievementCategoryTranslationDto;
import greencity.service.AchievementCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/achievements/categories")
@RequiredArgsConstructor
public class AchievementCategoryController {
    private final AchievementCategoryService achievementCategoryService;

    /**
     * Method returns all available achievement categories.
     *
     * @return list of {@link AchievementCategoryTranslationDto}
     */
    @Operation(summary = "Get all achievements categories.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @GetMapping
    public ResponseEntity<List<AchievementCategoryTranslationDto>> getAchievementCategories(
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.ok()
            .body(achievementCategoryService.findAllWithAtLeastOneAchievement(principal.getName()));
    }
}
