package greencity.controller;

import greencity.constant.AppConstant;
import greencity.constant.HttpStatuses;
import greencity.dto.goal.GoalDto;
import greencity.service.GoalService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goals")
public class GoalController {
    private final GoalService goalService;

    /**
     * Constructor with parameters.
     */
    @Autowired
    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    /**
     * Method returns all goals, available for tracking for specific language.
     *
     * @param language needed language code
     * @return list of {@link GoalDto}
     */
    @ApiOperation(value = "Get all goals.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
    })
    @GetMapping("")
    public ResponseEntity<List<GoalDto>> getAll(
        @ApiParam(value = "Code of the needed language.", defaultValue = AppConstant.DEFAULT_LANGUAGE_CODE)
        @RequestParam(required = false, defaultValue = AppConstant.DEFAULT_LANGUAGE_CODE) String language) {
        return ResponseEntity.status(HttpStatus.OK).body(goalService.findAll(language));
    }
}
