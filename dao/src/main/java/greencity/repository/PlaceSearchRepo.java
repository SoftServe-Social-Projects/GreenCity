package greencity.repository;

import greencity.entity.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlaceSearchRepo {
    /**
     * Method for search places by title and text.
     *
     * @param pageable      {@link Pageable}.
     * @param searchingText text criteria for searching.
     * @return {@link Page} of {@link Place}.
     */
    Page<Place> find(Pageable pageable, String searchingText, Boolean isFavorite, Long userId);
}
