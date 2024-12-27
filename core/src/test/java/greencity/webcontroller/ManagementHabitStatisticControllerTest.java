package greencity.webcontroller;

import greencity.service.HabitStatisticService;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class ManagementHabitStatisticControllerTest {
    private MockMvc mockMvc;

    @InjectMocks
    private ManagementHabitStatisticController controller;

    @Mock
    private HabitStatisticService habitStatisticService;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @SneakyThrows
    void getStatisticsPageTest() {
        mockMvc.perform(get("/management/habit/statistics"))
            .andExpect(status().isOk())
            .andExpect(view().name("core/management_habit_statistics"));
    }

    @Test
    @SneakyThrows
    void getUserInterestStatisticsTest() {
        mockMvc.perform(get("/management/habit/statistics/interest")
            .accept(MediaType.APPLICATION_JSON));

        verify(habitStatisticService, times(1)).calculateUserInterest();
    }

    @Test
    @SneakyThrows
    void getHabitBehaviorStatisticsTest() {
        mockMvc.perform(get("/management/habit/statistics/habit-behavior")
            .accept(MediaType.APPLICATION_JSON));

        verify(habitStatisticService, times(1)).calculateHabitBehaviorStatistic();
    }

    @Test
    @SneakyThrows
    void getUserHabitInteractionStatisticsTest() {
        mockMvc.perform(get("/management/habit/statistics/user-interaction")
            .param("range", "weekly")
            .accept(MediaType.APPLICATION_JSON));

        verify(habitStatisticService, times(1)).calculateInteractions("weekly");
    }
}
