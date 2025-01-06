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
     * Method returns {@param true} if location with such {@param lat} and {@param lng} exists
     *
     * @param lat latitude of point of the map
     * @param lng longitude of point of the map
     * @return boolean
     * @author Ivan Hrenevych.
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
