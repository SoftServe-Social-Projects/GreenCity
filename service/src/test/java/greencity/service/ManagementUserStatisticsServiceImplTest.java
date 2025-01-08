package greencity.service;

import greencity.dto.user.UserEmailPreferencesStatisticDto;
import greencity.dto.user.UserLocationStatisticDto;
import greencity.dto.user.UserRegistrationStatisticDto;
import greencity.dto.user.UserRoleStatisticDto;
import greencity.dto.user.UserStatusStatisticDto;
import greencity.enums.EmailPreference;
import greencity.enums.EmailPreferencePeriodicity;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.repository.UserRepo;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagementUserStatisticsServiceImplTest {
    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private ManagementUserStatisticsServiceImpl managementUserStatisticsServiceImpl;

    @Test
    void testGetUserRegistrationsByDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        String granularity = "day";

        Tuple mockTuple = mock(Tuple.class);
        when(mockTuple.get(0)).thenReturn(Timestamp.valueOf(startDate));
        when(mockTuple.get(1, Long.class)).thenReturn(10L);

        when(userRepo.countUsersByRegistrationDateBetween(startDate, endDate, granularity))
            .thenReturn(List.of(mockTuple));

        List<UserRegistrationStatisticDto> result =
            managementUserStatisticsServiceImpl.getUserRegistrationsByDateRange(startDate, endDate, granularity);

        assertEquals(1, result.size());
        assertEquals(startDate, result.getFirst().getDate());
        assertEquals(10L, result.getFirst().getCount());

        verify(userRepo, times(1)).countUsersByRegistrationDateBetween(startDate, endDate, granularity);
    }

    @Test
    void testGetUserRolesDistribution() {
        List<UserRoleStatisticDto> mockResult = List.of(new UserRoleStatisticDto(Role.ROLE_USER, 100L));
        when(userRepo.getUserRolesDistribution()).thenReturn(mockResult);

        List<UserRoleStatisticDto> result = managementUserStatisticsServiceImpl.getUserRolesDistribution();

        assertEquals(mockResult, result);
        verify(userRepo, times(1)).getUserRolesDistribution();
    }

    @Test
    void testGetUserStatusesDistribution() {
        List<UserStatusStatisticDto> mockResult = List.of(new UserStatusStatisticDto(UserStatus.ACTIVATED, 200L));
        when(userRepo.getUserStatusesDistribution()).thenReturn(mockResult);

        List<UserStatusStatisticDto> result = managementUserStatisticsServiceImpl.getUserStatusesDistribution();

        assertEquals(mockResult, result);
        verify(userRepo, times(1)).getUserStatusesDistribution();
    }

    @Test
    void testGetUserLocationsDistributionCity() {
        List<UserLocationStatisticDto> mockResult = List.of(new UserLocationStatisticDto("City", 50L));
        when(userRepo.getUserLocationsDistributionByCity()).thenReturn(mockResult);

        List<UserLocationStatisticDto> result =
            managementUserStatisticsServiceImpl.getUserLocationsDistribution("city");

        assertEquals(mockResult, result);
        verify(userRepo, times(1)).getUserLocationsDistributionByCity();
    }

    @Test
    void testGetUserLocationsDistributionRegion() {
        List<UserLocationStatisticDto> mockResult = List.of(new UserLocationStatisticDto("Dnipropetrovsk", 30L));
        when(userRepo.getUserLocationsDistributionByRegion()).thenReturn(mockResult);

        List<UserLocationStatisticDto> result =
            managementUserStatisticsServiceImpl.getUserLocationsDistribution("region");

        assertEquals(mockResult, result);
        verify(userRepo, times(1)).getUserLocationsDistributionByRegion();
    }

    @Test
    void testGetUserLocationsDistributionCountry() {
        List<UserLocationStatisticDto> mockResult = List.of(new UserLocationStatisticDto("Ukraine", 70L));
        when(userRepo.getUserLocationsDistributionByCountry()).thenReturn(mockResult);

        List<UserLocationStatisticDto> result =
            managementUserStatisticsServiceImpl.getUserLocationsDistribution("country");

        assertEquals(mockResult, result);
        verify(userRepo, times(1)).getUserLocationsDistributionByCountry();
    }

    @Test
    void testGetUserEmailPreferencesDistribution() {
        List<UserEmailPreferencesStatisticDto> mockResult =
            List.of(new UserEmailPreferencesStatisticDto(EmailPreference.LIKES, EmailPreferencePeriodicity.DAILY, 25L));
        when(userRepo.getUserEmailPreferencesDistribution()).thenReturn(mockResult);

        List<UserEmailPreferencesStatisticDto> result =
            managementUserStatisticsServiceImpl.getUserEmailPreferencesDistribution();

        assertEquals(mockResult, result);
        verify(userRepo, times(1)).getUserEmailPreferencesDistribution();
    }
}