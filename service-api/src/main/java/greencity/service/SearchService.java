package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.search.SearchEventsDto;
import greencity.dto.search.SearchNewsDto;
import greencity.dto.search.SearchPlacesDto;
import org.springframework.data.domain.Pageable;

/**
 * Provides the interface to manage search functionality.
 */
public interface SearchService {
    /**
     * Method that allow you to search {@link SearchNewsDto}.
     *
     * @param pageable    {@link Pageable}.
     * @param searchQuery query to search.
     * @return PageableDto of {@link SearchNewsDto} instances.
     */
    PageableDto<SearchNewsDto> searchAllNews(Pageable pageable, String searchQuery, Boolean isFavorite, Long userId);

    /**
     * Method that allow you to search {@link SearchEventsDto}.
     *
     * @param pageable    {@link Pageable}.
     * @param searchQuery query to search.
     * @param userId      current user id.
     * @return PageableDto of {@link SearchEventsDto} instances.
     */
    PageableDto<SearchEventsDto> searchAllEvents(Pageable pageable, String searchQuery, Boolean isFavorite,
        Long userId);

    /**
     * Method that allow you to search {@link SearchPlacesDto}.
     *
     * @param pageable    {@link Pageable}.
     * @param searchQuery query to search.
     * @param userId      current user id.
     * @return PageableDto of {@link SearchPlacesDto} instances.
     */
    PageableDto<SearchPlacesDto> searchAllPlaces(Pageable pageable, String searchQuery, Boolean isFavorite,
        Long userId);
}
