package greencity.service;

import greencity.dto.subscription.SubscriptionRequestDto;
import greencity.dto.subscription.SubscriptionResponseDto;
import java.util.UUID;

public interface SubscriptionService {
    /**
     * Creates a new subscription based on the provided request details.
     *
     * @param subscriptionRequestDto the details of the subscription request,
     *                               including email and subscription type.
     * @return a {@link SubscriptionResponseDto} containing unsubscription token.
     */
    SubscriptionResponseDto createSubscription(SubscriptionRequestDto subscriptionRequestDto);

    /**
     * Deletes an existing subscription using the provided unsubscription token.
     *
     * @param unsubscribeToken the token used to identify the subscription to be
     *                         deleted.
     */
    void deleteSubscription(UUID unsubscribeToken);
}
