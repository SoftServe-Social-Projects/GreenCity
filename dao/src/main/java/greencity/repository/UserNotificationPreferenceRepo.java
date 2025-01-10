package greencity.repository;

import greencity.entity.UserNotificationPreference;
import greencity.enums.EmailPreference;
import greencity.enums.EmailPreferencePeriodicity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Set;

public interface UserNotificationPreferenceRepo extends JpaRepository<UserNotificationPreference, Long> {
    Set<UserNotificationPreference> findAllByUserId(Long id);

    boolean existsByUserIdAndEmailPreferenceAndPeriodicity(Long id, EmailPreference emailPreference,
        EmailPreferencePeriodicity periodicity);
}
