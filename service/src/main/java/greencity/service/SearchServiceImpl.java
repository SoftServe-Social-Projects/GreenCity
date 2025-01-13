package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.search.SearchEventsDto;
import greencity.dto.search.SearchNewsDto;
import greencity.dto.search.SearchPlacesDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final EcoNewsService ecoNewsService;
    private final EventService eventService;
    private final PlaceService placeService;

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<SearchNewsDto> searchAllNews(Pageable pageable, String searchQuery, Boolean isFavorite,
        Long userId) {
        return ecoNewsService.search(pageable, searchQuery, isFavorite, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<SearchEventsDto> searchAllEvents(Pageable pageable, String searchQuery, Boolean isFavorite,
        Long userId) {
        return eventService.search(pageable, searchQuery, isFavorite, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<SearchPlacesDto> searchAllPlaces(Pageable pageable, String searchQuery, Boolean isFavorite,
        Long userId) {
        return placeService.search(pageable, searchQuery, isFavorite, userId);
    }
}
