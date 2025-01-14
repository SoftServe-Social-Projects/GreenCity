package greencity.repository;

import greencity.entity.Location;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Provides an interface to manage {@link Location} entity.
 */
@Repository
public interface LocationRepo extends JpaRepository<Location, Long> {
    /**
     * Method return a location {@code Location} which has not a {@code Place}.
     *
     * @param lat latitude of point of the map
     * @param lng longitude of point of the map
     * @return a {@link Optional} of {@code Location}
     * @author Kateryna Horokh.
     */
    Optional<Location> findByLatAndLng(Double lat, Double lng);

    /**
     * Method checks if {@code Location} with such {@code lat} and {@code lng}
     * exist. Only first 4 decimal places of {@code lat} and {@code lng} are taken
     * into account
     *
     * @param lat latitude of point of the map
     * @param lng longitude of point of the map
     * @return {@code true} if {@code Location} with such coordinates exist, or else
     *         - {@code false}
     * @author Hrenevych Ivan.
     */
    @Query(value = """
         SELECT EXISTS (
             SELECT 1
             FROM locations l
             WHERE ROUND(CAST(l.lat AS numeric), 4) = ROUND(CAST(:lat AS numeric), 4)
               AND ROUND(CAST(l.lng AS numeric), 4) = ROUND(CAST(:lng AS numeric), 4)
         )
        """, nativeQuery = true)
    boolean existsByLatAndLng(@Param("lat") double lat, @Param("lng") double lng);
}
