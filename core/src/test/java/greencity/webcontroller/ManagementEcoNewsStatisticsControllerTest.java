package greencity.webcontroller;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.econews.EcoNewsAuthorStatisticDto;
import greencity.dto.econews.EcoNewsTagStatistic;
import greencity.service.ManagementEcoNewsStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManagementEcoNewsStatisticsControllerTest {

    @InjectMocks
    private ManagementEcoNewsStatisticsController controller;

    @Mock
    private ManagementEcoNewsStatisticsService ecoNewsStatisticService;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetStatisticsPage() {
        String viewName = controller.getStatisticsPage();
        assertEquals("core/management_econews_statistics", viewName);
    }

    @Test
    void testGetPublicationCount() {
        Long expectedCount = 10L;
        when(ecoNewsStatisticService.getPublicationCount()).thenReturn(expectedCount);

        ResponseEntity<Long> response = controller.getPublicationCount();

        assertEquals(ResponseEntity.ok().body(expectedCount), response);
        verify(ecoNewsStatisticService, times(1)).getPublicationCount();
    }

    @Test
    void testGetTagStatistic() {
        List<EcoNewsTagStatistic> expectedStatistics =
            Collections.singletonList(new EcoNewsTagStatistic("News, Ads", 4L));
        when(ecoNewsStatisticService.getTagStatistics()).thenReturn(expectedStatistics);

        ResponseEntity<List<EcoNewsTagStatistic>> response = controller.getTagStatistic();

        assertEquals(ResponseEntity.ok().body(expectedStatistics), response);
        verify(ecoNewsStatisticService, times(1)).getTagStatistics();
    }

    @Test
    void testGetTagStatistic_NoContent() {
        when(ecoNewsStatisticService.getTagStatistics()).thenReturn(Collections.emptyList());

        ResponseEntity<List<EcoNewsTagStatistic>> response = controller.getTagStatistic();

        assertEquals(ResponseEntity.noContent().build(), response);
        verify(ecoNewsStatisticService, times(1)).getTagStatistics();
    }

    @Test
    void testGetUserActivityPage() {
        int page = 0;
        int size = 20;
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        PageableAdvancedDto<EcoNewsAuthorStatisticDto> expectedStats = new PageableAdvancedDto<>();

        when(ecoNewsStatisticService.getEcoNewsAuthorStatistic(pageable)).thenReturn(expectedStats);

        String viewName = controller.getUserActivityPage(page, size, model);

        ArgumentCaptor<PageableAdvancedDto<EcoNewsAuthorStatisticDto>> captor =
            ArgumentCaptor.forClass(PageableAdvancedDto.class);
        verify(model).addAttribute(eq("pageable"), captor.capture());
        assertEquals(expectedStats, captor.getValue());
        assertEquals("core/fragments/statistic/eco-news-user-activity-statistic :: userStatisticsTable", viewName);
    }
}