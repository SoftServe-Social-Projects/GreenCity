package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.econews.EcoNewsAuthorStatisticDto;
import greencity.dto.econews.EcoNewsTagStatistic;
import greencity.repository.EcoNewsRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagementEcoNewsStatisticsServiceImplTest {

    @InjectMocks
    private ManagementEcoNewsStatisticsServiceImpl service;

    @Mock
    private EcoNewsRepo ecoNewsRepo;

    @Mock
    private Page<EcoNewsAuthorStatisticDto> ecoNewsAuthorStatisticDtoPage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetPublicationCount() {
        Long expectedCount = 10L;
        when(ecoNewsRepo.count()).thenReturn(expectedCount);

        Long actualCount = service.getPublicationCount();

        assertEquals(expectedCount, actualCount);
        verify(ecoNewsRepo, times(1)).count();
    }

    @Test
    void testGetEcoNewsAuthorStatistic() {
        Pageable pageable = Pageable.ofSize(10).withPage(0);
        List<EcoNewsAuthorStatisticDto> authorStatistics = new ArrayList<>();
        authorStatistics.add(new EcoNewsAuthorStatisticDto(1L, 1L, "john", 10L));
        when(ecoNewsRepo.getEcoNewsAuthorStatistic(pageable)).thenReturn(ecoNewsAuthorStatisticDtoPage);
        when(ecoNewsAuthorStatisticDtoPage.getContent()).thenReturn(authorStatistics);
        when(ecoNewsAuthorStatisticDtoPage.getTotalElements()).thenReturn(1L);
        when(ecoNewsAuthorStatisticDtoPage.getTotalPages()).thenReturn(1);
        when(ecoNewsAuthorStatisticDtoPage.getPageable()).thenReturn(pageable);
        when(ecoNewsAuthorStatisticDtoPage.getNumber()).thenReturn(0);
        when(ecoNewsAuthorStatisticDtoPage.hasPrevious()).thenReturn(false);
        when(ecoNewsAuthorStatisticDtoPage.hasNext()).thenReturn(false);
        when(ecoNewsAuthorStatisticDtoPage.isFirst()).thenReturn(true);
        when(ecoNewsAuthorStatisticDtoPage.isLast()).thenReturn(true);

        PageableAdvancedDto<EcoNewsAuthorStatisticDto> result = service.getEcoNewsAuthorStatistic(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(authorStatistics, result.getPage());
        verify(ecoNewsRepo, times(1)).getEcoNewsAuthorStatistic(pageable);
    }

    @Test
    void testGetTagStatistics() {
        List<Object[]> results = new ArrayList<>();
        results.add(new Object[] {"tag1", 5});
        results.add(new Object[] {"tag2", 10});
        when(ecoNewsRepo.getEcoNewsTagsStatistics(2L)).thenReturn(results);

        List<EcoNewsTagStatistic> statistics = service.getTagStatistics();

        assertEquals(2, statistics.size());
        assertEquals("tag1", statistics.get(0).tags());
        assertEquals(5, statistics.get(0).count());
        assertEquals("tag2", statistics.get(1).tags());
        assertEquals(10, statistics.get(1).count());
        verify(ecoNewsRepo, times(1)).getEcoNewsTagsStatistics(2L);
    }

    @Test
    void testGetTagStatistics_EmptyList() {
        when(ecoNewsRepo.getEcoNewsTagsStatistics(2L)).thenReturn(Collections.emptyList());

        List<EcoNewsTagStatistic> statistics = service.getTagStatistics();

        assertEquals(0, statistics.size());
        verify(ecoNewsRepo, times(1)).getEcoNewsTagsStatistics(2L);
    }
}