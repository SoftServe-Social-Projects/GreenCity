package greencity.repository;

import greencity.entity.Place;
import greencity.enums.PlaceStatus;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Provides an interface to manage {@link Place} entity.
 */
@Repository
public interface PlaceRepo extends PlaceSearchRepo, JpaRepository<Place, Long>, JpaSpecificationExecutor<Place> {
    /**
     * Finds all places related to the given {@code PlaceStatus}.
     *
     * @param status   to find by.
     * @param pageable pageable configuration.
     * @return a list of places with the given {@code PlaceStatus}.
     */
    Page<Place> findAllByStatusOrderByModifiedDateDesc(PlaceStatus status, Pageable pageable);

    /**
     * Method to find all created {@link Place}'s by user id.
     *
     * @param userId - {@code User}'s id.
     * @return list of {@link Place}'s
     */
    @Query(nativeQuery = true, value = "SELECT * FROM places p WHERE p.author_id = :userId")
    List<Place> findAllByUserId(@Param("userId") Long userId);

    /**
     * Method to find average rate.
     *
     * @param id place
     * @return average rate
     */
    @Query(value = "SELECT AVG(r.rate) FROM Estimate r " + "WHERE r.place.id = :id")
    Double getAverageRate(@Param("id") Long id);

    /**
     * The method to find all {@link Place}'s which was added between 2 dates and
     * has {@link PlaceStatus}.
     *
     * @param startDate - start date of search
     * @param endDate   - end date of search
     * @param status    - {@link PlaceStatus} of places
     * @return list of {@link Place}'s
     */
    List<Place> findAllByModifiedDateBetweenAndStatus(ZonedDateTime startDate, ZonedDateTime endDate,
        PlaceStatus status);

    /**
     * Method returns {@link Place} by search query and page.
     *
     * @param pageable    {@link Pageable}.
     * @param searchQuery query to search.
     * @return list of {@link Place}.
     */
    @Query("SELECT p FROM Place p WHERE CONCAT(p.id,'') LIKE LOWER(CONCAT('%', :searchQuery, '%')) "
        + "OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchQuery, '%'))")
    Page<Place> searchBy(Pageable pageable, String searchQuery);

    /**
     * Method to get place by category name.
     *
     * @param category category to search
     * @return - places with searching category
     */
    @Query(nativeQuery = true,
        value = "SELECT p.* FROM places p "
            + "join categories c on c.id = p.category_id "
            + "WHERE c.name IN (:category) "
            + "or c.name_ua IN (:category)")
    List<Place> findPlaceByCategory(String[] category);

    /**
     * Finds a place by its name.
     *
     * @param name the name of the place.
     * @return an optional containing the place if found, or empty otherwise.
     */
    @Query("SELECT p FROM Place p WHERE LOWER(p.name) = LOWER(:name)")
    Optional<Place> findByNameIgnoreCase(@Param("name") String name);
}