package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.search.SearchNewsDto;
import greencity.dto.search.SearchResponseDto;
import greencity.dto.search.SearchTipsAndTricksDto;
import org.springframework.data.domain.Pageable;

/**
 * Provides the interface to manage search functionality.
 *
 * @author Kovaliv Taras
 * @version 1.0
 */
public interface SearchService {
    /**
     * Method that allow you to search {@link SearchResponseDto}.
     *
     * @param searchQuery query to search
     * @return {@link SearchResponseDto}
     */
    SearchResponseDto search(String searchQuery);

    /**
     * Method that allow you to search {@link SearchNewsDto}.
     *
     * @param pageable    {@link Pageable}.
     * @param searchQuery query to search.
     * @return PageableDto of {@link SearchNewsDto} instances.
     */
    PageableDto<SearchNewsDto> searchAllNews(Pageable pageable, String searchQuery);

    /**
     * Method that allow you to search {@link SearchTipsAndTricksDto}.
     *
     * @param pageable    {@link Pageable}.
     * @param searchQuery query to search.
     * @return PageableDto of {@link SearchTipsAndTricksDto} instances.
     */
    PageableDto<SearchTipsAndTricksDto> searchAllTipsAndTricks(Pageable pageable, String searchQuery);
}
