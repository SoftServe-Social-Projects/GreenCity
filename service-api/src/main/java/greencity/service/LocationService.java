package greencity.service;

import greencity.dto.location.LocationVO;
import java.util.List;
import java.util.Optional;

/**
 * Provides the interface to manage {@code Location} entity.
 */
public interface LocationService {
    /**
     * Update Location in DB.
     *
     * @param id       - Location id.
     * @param location - Location entity.
     * @return Location updated entity.
     */
    LocationVO update(Long id, LocationVO location);

    /**
     * Find Location entity by id.
     *
     * @param id - Location id.
     * @return Location entity.
     */
    LocationVO findById(Long id);

    /**
     * Find all locations from DB.
     *
     * @return List of categories.
     */
    List<LocationVO> findAll();

    /**
     * Delete entity from DB by id.
     *
     * @param id - Location id.
     * @return id of deleted Location.
     */
    Long deleteById(Long id);

    /**
     * Save Location to DB.
     *
     * @param location - entity of Location.
     * @return saved Location.
     */
    LocationVO save(LocationVO location);

    /**
     * Method return a location {@code Location} which has not a {@code Place}.
     *
     * @param lat latitude of point of the map
     * @param lng longitude of point of the map
     * @return a {@link Optional} of {@code Location}
     * @author Kateryna Horokh.
     */
    Optional<LocationVO> findByLatAndLng(Double lat, Double lng);

    /**
     * Method checks if {@code Location} with such {@code lat} and {@code lng} exist.
     * Only first 4 decimal places of {@code lat} and {@code lng} are taken into account
     *
     * @param lat latitude of point of the map
     * @param lng longitude of point of the map
     * @return {@code true} if {@code Location} with such coordinates exist, or else - {@code false}
     * @author Hrenevych Ivan.
     */
    boolean existsByLatAndLng(Double lat, Double lng);
}
