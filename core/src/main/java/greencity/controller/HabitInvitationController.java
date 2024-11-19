package greencity.controller;

import greencity.annotations.CurrentUser;
import greencity.constant.HttpStatuses;
import greencity.dto.user.UserVO;
import greencity.service.HabitInvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/habit/invite")
public class HabitInvitationController {
    private final HabitInvitationService habitInvitationService;

    /**
     * Method for accepting a habit invitation.
     *
     * @param invitationId ID of the invitation.
     * @param userVO       {@link UserVO} representing the authenticated user.
     */
    @Operation(summary = "Accept habit invitation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND))),
    })
    @PatchMapping("/{invitationId}/accept")
    public ResponseEntity<Void> acceptHabitInvitation(
        @Parameter(description = "Habit invitation ID. Cannot be empty.") @PathVariable Long invitationId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        habitInvitationService.acceptHabitInvitation(invitationId, userVO);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for rejecting a habit invitation. Changes status from REQUEST to
     * REJECTED.
     *
     * @param invitationId ID of the invitation.
     * @param userVO       {@link UserVO} representing the authenticated user.
     */
    @Operation(summary = "Reject habit invitation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND))),
    })
    @DeleteMapping("/{invitationId}/reject")
    public ResponseEntity<Void> rejectHabitInvitation(
        @Parameter(description = "Habit invitation ID. Cannot be empty.") @PathVariable Long invitationId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        habitInvitationService.rejectHabitInvitation(invitationId, userVO);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
