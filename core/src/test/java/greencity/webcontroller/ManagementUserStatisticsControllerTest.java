package greencity.webcontroller;

import greencity.dto.user.UserEmailPreferencesStatisticDto;
import greencity.dto.user.UserLocationStatisticDto;
import greencity.dto.user.UserRegistrationStatisticDto;
import greencity.dto.user.UserRoleStatisticDto;
import greencity.dto.user.UserStatusStatisticDto;
import greencity.enums.EmailPreference;
import greencity.enums.EmailPreferencePeriodicity;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.service.ManagementUserStatisticsService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class ManagementUserStatisticsControllerTest {
    private MockMvc mockMvc;

    @InjectMocks
    private ManagementUserStatisticsController managementUserStatisticsController;

    @Mock
    private ManagementUserStatisticsService managementUserStatisticsService;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(managementUserStatisticsController).build();
    }

    @Test
    @SneakyThrows
    void getStatisticsPageReturnsCorrectView() {
        mockMvc.perform(get("/management/user/statistics"))
            .andExpect(status().isOk())
            .andExpect(view().name("core/management_user_statistics"));
    }

    @Test
    @SneakyThrows
    void getRegistrationStatisticReturnsDataWhenDataExists() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        String granularity = "day";

        UserRegistrationStatisticDto dto1 = new UserRegistrationStatisticDto(LocalDateTime.now(), 5L);
        UserRegistrationStatisticDto dto2 = new UserRegistrationStatisticDto(LocalDateTime.now().minusDays(1), 3L);

        when(managementUserStatisticsService.getUserRegistrationsByDateRange(any(), any(), anyString()))
            .thenReturn(Arrays.asList(dto1, dto2));

        mockMvc.perform(get("/management/user/statistics/registration")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .param("granularity", granularity))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @SneakyThrows
    void getRegistrationStatisticReturnsNoContentWhenNoData() {
        when(managementUserStatisticsService.getUserRegistrationsByDateRange(any(), any(), anyString()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/management/user/statistics/registration")
                        .param("startDate", LocalDateTime.now().minusDays(7).toString())
                        .param("endDate", LocalDateTime.now().toString())
                        .param("granularity", "day"))
                .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    void getUserRolesDistributionReturnsDataWhenDataExists() {
        UserRoleStatisticDto dto1 = new UserRoleStatisticDto(Role.ROLE_ADMIN, 2L);
        UserRoleStatisticDto dto2 = new UserRoleStatisticDto(Role.ROLE_USER, 100L);

        when(managementUserStatisticsService.getUserRolesDistribution())
            .thenReturn(Arrays.asList(dto1, dto2));

        mockMvc.perform(get("/management/user/statistics/roles"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @SneakyThrows
    void getUserRolesDistributionReturnsNoContentWhenNoData() {
        when(managementUserStatisticsService.getUserRolesDistribution())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/management/user/statistics/roles"))
                .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    void getUserStatusesDistributionReturnsDataWhenDataExists() {
        UserStatusStatisticDto dto1 = new UserStatusStatisticDto(UserStatus.ACTIVATED, 80L);
        UserStatusStatisticDto dto2 = new UserStatusStatisticDto(UserStatus.DEACTIVATED, 20L);

        when(managementUserStatisticsService.getUserStatusesDistribution())
            .thenReturn(Arrays.asList(dto1, dto2));

        mockMvc.perform(get("/management/user/statistics/statuses"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @SneakyThrows
    void getUserStatusesDistributionReturnsNoContentWhenNoData() {
        when(managementUserStatisticsService.getUserStatusesDistribution())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/management/user/statistics/statuses"))
                .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    void getUserLocationDistributionReturnsDataWhenDataExists() {
        UserLocationStatisticDto dto1 = new UserLocationStatisticDto("Dnipro", 50L);
        UserLocationStatisticDto dto2 = new UserLocationStatisticDto("Lviv", 30L);

        when(managementUserStatisticsService.getUserLocationsDistribution(anyString()))
            .thenReturn(Arrays.asList(dto1, dto2));

        mockMvc.perform(get("/management/user/statistics/locations")
            .param("groupBy", "city"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @SneakyThrows
    void getUserLocationDistributionReturnsNoContentWhenNoData()  {
        when(managementUserStatisticsService.getUserLocationsDistribution(anyString()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/management/user/statistics/locations")
                        .param("groupBy", "city"))
                .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    void getUserPreferencesDistributionReturnsDataWhenDataExists() {
        UserEmailPreferencesStatisticDto dto1 =
            new UserEmailPreferencesStatisticDto(EmailPreference.SYSTEM, EmailPreferencePeriodicity.IMMEDIATELY, 40L);
        UserEmailPreferencesStatisticDto dto2 =
            new UserEmailPreferencesStatisticDto(EmailPreference.LIKES, EmailPreferencePeriodicity.NEVER, 60L);

        when(managementUserStatisticsService.getUserEmailPreferencesDistribution())
            .thenReturn(Arrays.asList(dto1, dto2));

        mockMvc.perform(get("/management/user/statistics/preferences"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @SneakyThrows
    void getUserPreferencesDistributionReturnsNoContentWhenNoData()  {
        when(managementUserStatisticsService.getUserEmailPreferencesDistribution())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/management/user/statistics/preferences"))
                .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    void getUserLocationDistributionBadRequestWhenGroupByIsMissing() {
        mockMvc.perform(get("/management/user/statistics/locations"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void getRegistrationStatisticBadRequestWhenParametersAreMissing() {
        mockMvc.perform(get("/management/user/statistics/registration"))
            .andExpect(status().isBadRequest());
    }

}