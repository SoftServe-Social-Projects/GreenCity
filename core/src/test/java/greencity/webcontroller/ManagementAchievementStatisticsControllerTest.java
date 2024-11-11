package greencity.webcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.achievement.StatisticsDto;
import greencity.service.AchievementStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManagementAchievementStatisticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AchievementStatisticsService achievementStatisticsService;

    @InjectMocks
    private ManagementAchievementStatisticsController managementAchievementStatisticsController;
    private final String url = "/management/achievement/statistics";

    private ObjectMapper mapper = new ObjectMapper();
    private List<StatisticsDto> statisticsDtos = List.of(
        new StatisticsDto("Achievement 1", 10L),
        new StatisticsDto("Achievement 2", 20L));

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(managementAchievementStatisticsController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void testGetAchievementStatistic() throws Exception {
        String expectedJson = mapper.writeValueAsString(statisticsDtos);

        when(achievementStatisticsService.statisticsUsersWithAchievements()).thenReturn(statisticsDtos);

        mockMvc.perform(get(url))
            .andExpect(status().isOk())
            .andExpect(view().name("core/management_achievement_statistics"))
            .andExpect(model().attributeExists("statisticalData"))
            .andExpect(model().attribute("statisticalData", expectedJson));

        verify(achievementStatisticsService, times(1)).statisticsUsersWithAchievements();
    }

    @Test
    void testGetAchievementStatisticWithProcessingError() throws Exception {
        when(achievementStatisticsService.statisticsUsersWithAchievements()).thenReturn(statisticsDtos);

        ObjectMapper faultyMapper = mock(ObjectMapper.class);
        doThrow(new RuntimeException("JSON Processing Error")).when(faultyMapper).writeValueAsString(statisticsDtos);

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("core/management_achievement_statistics"))
                .andExpect(model().attributeExists("statisticalData"));

        verify(achievementStatisticsService, times(1)).statisticsUsersWithAchievements();
    }

    @Test
    void testGetAchievementCategoryStatistic() throws Exception {
        when(achievementStatisticsService.statisticsUsersWithAchievementsCategory()).thenReturn(statisticsDtos);

        mockMvc.perform(get(url + "/by-category"))
                .andExpect(status().isOk());

        verify(achievementStatisticsService, times(1)).statisticsUsersWithAchievementsCategory();
    }

    @Test
    void testGetUsersActivityStatistic() throws Exception {
        when(achievementStatisticsService.statisticsUsersActivity()).thenReturn(statisticsDtos);

        mockMvc.perform(get(url + "/by-activity"))
                .andExpect(status().isOk());

        verify(achievementStatisticsService, times(1)).statisticsUsersActivity();
    }
}