package greencity.repository;

import greencity.entity.Subscription;
import greencity.enums.SubscriptionType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides an interface to manage {@link Subscription} entity.
 */
@Repository
public interface SubscriptionRepo extends JpaRepository<Subscription, Long> {
    /**
     * Checks if a subscription exists by the provided unsubscription token.
     *
     * @param unsubscribeToken the unique token used to identify the subscription.
     * @return {@code true} if a subscription with the given token exists, otherwise
     *         {@code false}.
     */
    boolean existsByUnsubscribeToken(UUID unsubscribeToken);

    /**
     * Checks if a subscription exists by the provided email and subscription type.
     *
     * @param email            the email address associated with the subscription.
     * @param subscriptionType the type of subscription.
     * @return {@code true} if a subscription with the given email and type exists,
     *         otherwise {@code false}.
     */
    boolean existsByEmailAndSubscriptionType(String email, SubscriptionType subscriptionType);

    /**
     * Deletes a subscription identified by the provided unsubscription token.
     *
     * @param unsubscribeToken the unique token used to identify the subscription to
     *                         be deleted.
     */
    void deleteByUnsubscribeToken(UUID unsubscribeToken);
}
