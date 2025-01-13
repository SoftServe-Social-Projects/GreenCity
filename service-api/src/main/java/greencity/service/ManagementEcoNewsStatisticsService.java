package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.econews.EcoNewsAuthorStatisticDto;
import greencity.dto.econews.EcoNewsTagStatistic;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ManagementEcoNewsStatisticsService {
    /**
     * Retrieves the total count of published EcoNews articles.
     *
     * @return the total number of published EcoNews articles.
     */
    Long getPublicationCount();

    /**
     * Retrieves statistics about EcoNews authors, including the number of articles
     * published by each author, ordered by the count of articles in descending
     * order.
     *
     * @return a pageable list of EcoNews author statistics.
     */
    PageableAdvancedDto<EcoNewsAuthorStatisticDto> getEcoNewsAuthorStatistic(Pageable pageable);

    /**
     * Retrieves statistics about EcoNews tags, including the count of articles
     * associated with each tag.
     *
     * @return a list of EcoNews tag statistics.
     */
    List<EcoNewsTagStatistic> getTagStatistics();
}
