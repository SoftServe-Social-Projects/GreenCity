package greencity.service;

import greencity.entity.OpeningHours;
import greencity.entity.Place;
import java.util.List;
import java.util.Set;

/**
 * Provides the interface to manage {@code OpeningHours} entity.
 */
public interface OpenHoursService {
    /**
     * Save OpeningHours to DB.
     *
     * @param hours - entity of OpeningHours.
     * @return saved OpeningHours.
     */
    OpeningHours save(OpeningHours hours);

    /**
     * Find OpeningHours entity by id.
     *
     * @param id - OpeningHours id.
     * @return OpeningHours entity.
     */
    OpeningHours findById(Long id);

    /**
     * Delete entity from DB by id.
     *
     * @param id - OpeningHours id.
     * @return id of deleted OpeningHours.
     */
    Long deleteById(Long id);

    /**
     * Finds all {@code OpeningHours} records related to the specified {@link
     * greencity.entity.Place}.
     *
     * @param place to find by.
     * @return a list of the {@code OpeningHours} for the place.
     */
    List<OpeningHours> getOpenHoursByPlace(Place place);

    /**
     * Find all opening hours from DB.
     *
     * @return List of opening hours.
     */
    List<OpeningHours> findAll();

    /**
     * Update OpeningHours in DB.
     *
     * @param id           - OpeningHours id.
     * @param updatedHours - OpeningHours entity.
     * @return OpeningHours updated entity.
     */
    OpeningHours update(Long id, OpeningHours updatedHours);

    /**
     * Finds all {@code OpeningHours} records related to the specified {@code Place}.
     *
     * @param placeId to find by.
     * @return a list of the {@code OpeningHours} for the place by id.
     */
    Set<OpeningHours> findAllByPlaceId(Long placeId);

    /**
     * Delete all {@code OpeningHours} records related to the specified {@code Place}.
     *
     * @param placeId to find by.
     */
    void deleteAllByPlaceId(Long placeId);
}
