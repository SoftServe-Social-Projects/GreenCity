package greencity.webcontroller;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.econews.EcoNewsAuthorStatisticDto;
import greencity.dto.econews.EcoNewsTagStatistic;
import greencity.service.ManagementEcoNewsStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class ManagementEcoNewsStatisticsControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ManagementEcoNewsStatisticsController controller;

    @Mock
    private ManagementEcoNewsStatisticsService ecoNewsStatisticService;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGetStatisticsPage() throws Exception {
        mockMvc.perform(get("/management/econews/statistics"))
            .andExpect(status().isOk())
            .andExpect(view().name("core/management_econews_statistics"));
    }

    @Test
    void testGetPublicationCount() throws Exception {
        Long expectedCount = 10L;
        when(ecoNewsStatisticService.getPublicationCount()).thenReturn(expectedCount);

        ResultActions resultActions = mockMvc.perform(get("/management/econews/statistics/publication/count")
            .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(expectedCount.toString()));

        verify(ecoNewsStatisticService, times(1)).getPublicationCount();
    }

    @Test
    void testGetTagStatistic() throws Exception {
        List<EcoNewsTagStatistic> expectedStatistics =
            Collections.singletonList(new EcoNewsTagStatistic("News, Ads", 4L));
        when(ecoNewsStatisticService.getTagStatistics()).thenReturn(expectedStatistics);

        ResultActions resultActions = mockMvc.perform(get("/management/econews/statistics/tags")
            .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].tags").value("News, Ads"))
            .andExpect(jsonPath("$[0].count").value(4));

        verify(ecoNewsStatisticService, times(1)).getTagStatistics();
    }

    @Test
    void testGetTagStatistic_NoContent() throws Exception {
        when(ecoNewsStatisticService.getTagStatistics()).thenReturn(Collections.emptyList());

        ResultActions resultActions = mockMvc.perform(get("/management/econews/statistics/tags")
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNoContent());

        verify(ecoNewsStatisticService, times(1)).getTagStatistics();
    }

    @Test
    void testGetUserActivityPage() throws Exception {
        int page = 0;
        int size = 20;
        PageableAdvancedDto<EcoNewsAuthorStatisticDto> expectedStats = new PageableAdvancedDto<>();
        when(ecoNewsStatisticService.getEcoNewsAuthorStatistic(PageRequest.of(page, size))).thenReturn(expectedStats);

        ResultActions resultActions = mockMvc.perform(get("/management/econews/statistics/user/activity")
            .param("page", String.valueOf(page))
            .param("size", String.valueOf(size))
            .accept(MediaType.TEXT_HTML));

        resultActions.andExpect(status().isOk())
            .andExpect(view().name("core/fragments/statistic/eco-news-user-activity-statistic :: userStatisticsTable"));

        verify(ecoNewsStatisticService, times(1)).getEcoNewsAuthorStatistic(PageRequest.of(page, size));
    }
}