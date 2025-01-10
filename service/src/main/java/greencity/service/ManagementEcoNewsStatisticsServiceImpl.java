package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.econews.EcoNewsAuthorStatisticDto;
import greencity.dto.econews.EcoNewsTagStatistic;
import greencity.repository.EcoNewsRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ManagementEcoNewsStatisticsServiceImpl implements ManagementEcoNewsStatisticsService {
    private EcoNewsRepo ecoNewsRepo;

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getPublicationCount() {
        return ecoNewsRepo.count();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableAdvancedDto<EcoNewsAuthorStatisticDto> getEcoNewsAuthorStatistic(Pageable pageable) {
        Page<EcoNewsAuthorStatisticDto> ecoNewsAuthorStatisticDtoPage = ecoNewsRepo.getEcoNewsAuthorStatistic(pageable);
        return buildPageableAdvancedDto(ecoNewsAuthorStatisticDtoPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EcoNewsTagStatistic> getTagStatistics() {
        List<Object[]> results = ecoNewsRepo.getEcoNewsTagsStatistics(2L);
        List<EcoNewsTagStatistic> statistics = new ArrayList<>();

        for (Object[] result : results) {
            String tags = (String) result[0];
            Long count = ((Number) result[1]).longValue();
            statistics.add(new EcoNewsTagStatistic(tags, count));
        }
        statistics.forEach(r -> {
            log.info(r.toString());
            log.debug(r.toString());
        });
        return statistics;
    }

    private PageableAdvancedDto<EcoNewsAuthorStatisticDto> buildPageableAdvancedDto(
        Page<EcoNewsAuthorStatisticDto> ecoNewsPage) {
        List<EcoNewsAuthorStatisticDto> authorStatisticDtoList = ecoNewsPage.getContent();
        return new PageableAdvancedDto<>(
            authorStatisticDtoList,
            ecoNewsPage.getTotalElements(),
            ecoNewsPage.getPageable().getPageNumber(),
            ecoNewsPage.getTotalPages(),
            ecoNewsPage.getNumber(),
            ecoNewsPage.hasPrevious(),
            ecoNewsPage.hasNext(),
            ecoNewsPage.isFirst(),
            ecoNewsPage.isLast());
    }
}
