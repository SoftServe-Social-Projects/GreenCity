package greencity.repository;

import greencity.dto.filter.FilterEventDto;
import greencity.entity.event.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventSearchRepo {
    /**
     * Method for search list event ids by {@link FilterEventDto}.
     *
     * @param pageable       {@link Pageable}.
     * @param filterEventDto {@link FilterEventDto}.
     * @return list of event ids.
     */
    Page<Long> findEventsIds(Pageable pageable, FilterEventDto filterEventDto, Long userId);

    /**
     * Method for search list event ids by {@link FilterEventDto}.
     *
     * @param pageable       {@link Pageable}.
     * @param filterEventDto {@link FilterEventDto}.
     * @return list of event ids.
     */
    Page<Long> findEventsIdsManagement(Pageable pageable, FilterEventDto filterEventDto, Long userId);

    /**
     * Method for search events by title and text.
     *
     * @param pageable      {@link Pageable}.
     * @param searchingText text criteria for searching.
     * @return {@link Page} of {@link Event}.
     */
    Page<Event> find(Pageable pageable, String searchingText, Boolean isFavorite, Long userId);
}
