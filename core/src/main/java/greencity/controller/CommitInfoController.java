package greencity.controller;

import greencity.constant.HttpStatuses;
import greencity.dto.commitinfo.CommitInfoDto;
import greencity.service.CommitInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @Operation(summary = "Get the latest commit hash and date.")
    @ApiResponse(
        responseCode = "200",
        description = HttpStatuses.OK,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                value = """
                    {
                        "commitHash": "d6e70c46b39857846f3f13ca9756c39448ab3d6f",
                        "commitDate": "16/12/2024 10:55:00"
                    }""")))
    @ApiResponse(
        responseCode = "404",
        description = HttpStatuses.NOT_FOUND,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                value = """
                    {
                        "message": "Git repository not initialized. Commit info is unavailable."
                    }""")))
    @GetMapping
    public ResponseEntity<CommitInfoDto> getCommitInfo() {
        return ResponseEntity.ok(commitInfoService.getLatestCommitInfo());
    }
}
