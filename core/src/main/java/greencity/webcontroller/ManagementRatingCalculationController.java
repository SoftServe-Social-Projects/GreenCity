package greencity.webcontroller;

import greencity.annotations.ApiPageable;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.ratingstatistics.RatingPointsDto;
import greencity.enums.Status;
import greencity.service.RatingPointsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/management/rating/calculation")
@RequiredArgsConstructor
public class ManagementRatingCalculationController {
    private final RatingPointsService ratingPointsService;

    /**
     * Returns the management page displaying rating points.
     *
     * @param query    Optional search parameter to filter rating points based on
     *                 the provided input
     * @param model    ModelAndView that will be configured and returned to the user
     * @param pageable Pageable configuration for pagination
     * @return the name of the view
     */
    @ApiPageable
    @Operation(summary = "Retrieve the management page with Rating Points.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @GetMapping
    public String getRatingPoints(@RequestParam(required = false, name = "query") String query,
        Model model, @Parameter(hidden = true) Pageable pageable) {
        PageableAdvancedDto<RatingPointsDto> pageableDto = query == null || query.isEmpty()
            ? ratingPointsService.getAllRatingPointsByPage(pageable)
            : ratingPointsService.searchBy(pageable, query, Status.ACTIVE);
        model.addAttribute("ratings", pageableDto);
        model.addAttribute("query", query);
        return "core/management_rating_calculation";
    }

    /**
     * Deletes the Rating Points entity identified by the specified ID.
     *
     * @param id the ID of the Rating Points to delete
     * @return response entity indicating the result of the delete operation
     */
    @Operation(summary = "Delete Rating Points by ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        ratingPointsService.deleteRatingPoints(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Updates the Rating Points entity with the provided data.
     *
     * @param ratingPointsDto Data transfer object containing updated information
     * @return response entity with the updated Rating Points
     */
    @Operation(summary = "Update the specified Rating Points.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST)
    })
    @PutMapping
    public ResponseEntity<RatingPointsDto> updateRatingPoints(@RequestBody RatingPointsDto ratingPointsDto) {
        RatingPointsDto updatedRatingPoints = ratingPointsService.updateRatingPoints(ratingPointsDto);
        return ResponseEntity.ok(updatedRatingPoints);
    }

    /**
     * Gets a page with deleted Rating Points.
     *
     * @param query    Optional search parameter to filter deleted rating points
     *                 based on the provided input. If provided, it will filter the
     *                 deleted rating points by the specified query.
     * @param pageable Pageable configuration for pagination
     * @param model    Model that will be configured and returned to the user
     * @return the name of the view containing the paginated list of deleted Rating
     *         Points
     */
    @Operation(summary = "Get deleted Rating Points.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/deleted")
    public String getDeletedRatingPoints(@RequestParam(required = false, name = "query") String query,
        Model model, @Parameter(hidden = true) Pageable pageable) {
        PageableAdvancedDto<RatingPointsDto> pageableDto = query == null || query.isEmpty()
            ? ratingPointsService.getDeletedRatingPoints(pageable)
            : ratingPointsService.searchBy(pageable, query, Status.DELETE);
        model.addAttribute("ratings", pageableDto);
        model.addAttribute("query", query);
        return "core/management_rating_deleted";
    }

    /**
     * Restores the deleted Rating Points identified by the specified ID.
     *
     * @param id the ID of the Rating Points to restore
     * @return response entity indicating the result of the restore operation
     */
    @Operation(summary = "Restore deleted Rating Points by ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PutMapping("/restore/{id}")
    public ResponseEntity<Object> restoreDeletedRatingPoints(@PathVariable Long id) {
        ratingPointsService.restoreDeletedRatingPoints(id);
        return ResponseEntity.ok().build();
    }
}
