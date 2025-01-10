package greencity.controller;

import greencity.constant.HttpStatuses;
import greencity.dto.subscription.SubscriptionRequestDto;
import greencity.dto.subscription.SubscriptionResponseDto;
import greencity.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    /**
     * Method for saving email for subscriptions.
     *
     * @param subscriptionRequestDto {@link SubscriptionRequestDto} object with
     *                               email address and subscription type.
     * @return unsubscription token.
     */
    @Operation(summary = "Save email in database for receiving news.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = SubscriptionResponseDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
    })
    @PostMapping
    public ResponseEntity<SubscriptionResponseDto> subscribe(
        @RequestBody @Valid SubscriptionRequestDto subscriptionRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(subscriptionService.createSubscription(subscriptionRequestDto));
    }

    /**
     * Method for delete subscriber email from database using unsubscription token.
     */
    @Operation(summary = "Deleting an email from subscriptions.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @DeleteMapping("/{unsubscribeToken}")
    public ResponseEntity<Object> unsubscribe(@PathVariable UUID unsubscribeToken) {
        subscriptionService.deleteSubscription(unsubscribeToken);
        return ResponseEntity.ok().build();
    }
}
