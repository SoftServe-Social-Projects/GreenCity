package greencity.controller;

import greencity.constant.HttpStatuses;
import greencity.dto.commitinfo.CommitInfoDto;
import greencity.service.CommitInfoService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for fetching Git commit information.
 */
@RestController
@RequestMapping("/commit-info")
@RequiredArgsConstructor
public class CommitInfoController {
    private final CommitInfoService commitInfoService;

    /**
     * Endpoint to fetch the latest Git commit hash and date.
     *
     * @return {@link CommitInfoDto}
     */
    @ApiOperation(value = "Get the latest commit hash and date.")
    @ApiResponses({
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping
    public ResponseEntity<CommitInfoDto> getCommitInfo() {
        return ResponseEntity.ok(commitInfoService.getLatestCommitInfo());
    }
}