package greencity.service;

import greencity.dto.user.UserEmailPreferencesStatisticDto;
import greencity.dto.user.UserLocationStatisticDto;
import greencity.dto.user.UserRegistrationStatisticDto;
import greencity.dto.user.UserRoleStatisticDto;
import greencity.dto.user.UserStatusStatisticDto;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public interface ManagementUserStatisticsService {
    /**
     * Method to get list of dates and counts of registered users.
     *
     * @param startDate   {@code LocalDateTime} startDate.
     * @param endDate     {@code LocalDateTime} endDate.
     * @param granularity {@code String} (eg. day, week, month, year).
     * @return {@link List} of {@link UserRegistrationStatisticDto}.
     */
    List<UserRegistrationStatisticDto> getUserRegistrationsByDateRange(LocalDateTime startDate, LocalDateTime endDate,
        String granularity);

    /**
     * Method to get List of {@link UserRoleStatisticDto} to show distribution of
     * roles.
     *
     * @return {@link List} of {@link UserRoleStatisticDto}
     */
    List<UserRoleStatisticDto> getUserRolesDistribution();

    /**
     * Method to get List of {@link UserStatusStatisticDto} to show distribution of
     * statuses.
     *
     * @return {@link List} of {@link UserStatusStatisticDto}
     */
    List<UserStatusStatisticDto> getUserStatusesDistribution();

    /**
     * Method to get List of {@link UserLocationStatisticDto} to show distribution
     * of statuses.
     *
     * @return {@link List} of {@link UserLocationStatisticDto}
     */
    List<UserLocationStatisticDto> getUserLocationsDistribution(String groupBy);

    /**
     * Method to get List of {@link UserEmailPreferencesStatisticDto} to show
     * distribution of preferences by type and periodicity.
     *
     * @return {@link List} of {@link UserEmailPreferencesStatisticDto}
     */
    List<UserEmailPreferencesStatisticDto> getUserEmailPreferencesDistribution();
}
