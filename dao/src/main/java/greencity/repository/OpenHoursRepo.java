package greencity.repository;

import greencity.entity.OpeningHours;
import greencity.entity.Place;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides an interface to manage {@link OpeningHours} entity.
 */
@Repository
public interface OpenHoursRepo extends JpaRepository<OpeningHours, Long> {
    /**
     * Finds all {@code OpeningHours} records related to the specified
     * {@code Place}.
     *
     * @param place to find by.
     * @return a list of the {@code OpeningHours} for the place.
     */
    List<OpeningHours> findAllByPlace(Place place);

    /**
     * Finds all {@code OpeningHours} records related to the specified
     * {@code Place}.
     *
     * @param placeId to find by.
     * @return a set of the {@code OpeningHours} for the place by id.
     */
    Set<OpeningHours> findAllByPlaceId(Long placeId);

    /**
     * Delete all {@code OpeningHours} records related to the specified
     * {@code Place}.
     *
     * @param placeId to find by.
     */
    void deleteAllByPlaceId(Long placeId);
}
