package greencity.service;

import greencity.dto.openhours.OpeningHoursVO;
import greencity.dto.place.PlaceVO;
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
    OpeningHoursVO save(OpeningHoursVO hours);

    /**
     * Find OpeningHours entity by id.
     *
     * @param id - OpeningHours id.
     * @return OpeningHours entity.
     */
    OpeningHoursVO findById(Long id);

    /**
     * Delete entity from DB by id.
     *
     * @param id - OpeningHours id.
     * @return id of deleted OpeningHours.
     */
    Long deleteById(Long id);

    /**
     * Finds all OpeningHours records related to the specified Place.
     *
     * @param place to find by.
     * @return a list of the {@code OpeningHours} for the place.
     */
    List<OpeningHoursVO> getOpenHoursByPlace(PlaceVO place);

    /**
     * Find all opening hours from DB.
     *
     * @return List of opening hours.
     */
    List<OpeningHoursVO> findAll();

    /**
     * Update OpeningHours in DB.
     *
     * @param id           - OpeningHours id.
     * @param updatedHours - OpeningHours entity.
     * @return OpeningHours updated entity.
     */
    OpeningHoursVO update(Long id, OpeningHoursVO updatedHours);

    /**
     * Finds all {@code OpeningHours} records related to the specified
     * {@code Place}.
     *
     * @param placeId to find by.
     * @return a set of the {@code OpeningHours} for the place by id.
     */
    Set<OpeningHoursVO> findAllByPlaceId(Long placeId);

    /**
     * Delete all {@code OpeningHours} records related to the specified
     * {@code Place}.
     *
     * @param placeId to find by.
     */
    void deleteAllByPlaceId(Long placeId);
}
