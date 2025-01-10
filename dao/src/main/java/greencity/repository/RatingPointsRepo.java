package greencity.repository;

import greencity.constant.ErrorMessage;
import greencity.entity.RatingPoints;
import greencity.enums.Status;
import greencity.exception.exceptions.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface RatingPointsRepo extends JpaRepository<RatingPoints, Long> {
    /**
     * Finds a RatingPoints entity by its name and status.
     *
     * @param name   the name of the RatingPoints entity to find.
     * @param status the status of the RatingPoints entity to match.
     * @return an Optional containing the found RatingPoints entity, or an empty
     *         Optional if not found.
     */
    Optional<RatingPoints> findByNameAndStatus(String name, Status status);

    /**
     * Retrieves a RatingPoints entity by its name, or throws a NotFoundException if
     * not found. This method simplifies the process of fetching a RatingPoints
     * entity by its name while ensuring that an informative exception is thrown if
     * the entity does not exist.
     *
     * @param name the name of the RatingPoints entity to find.
     * @return the found RatingPoints entity.
     * @throws NotFoundException if the RatingPoints entity is not found.
     */
    default RatingPoints findByNameOrThrow(String name) {
        return findByNameAndStatus(name, Status.ACTIVE)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.RATING_POINTS_NOT_FOUND_BY_NAME + name));
    }

    /**
     * Updates the status of a RatingPoints entity based on its ID.
     *
     * @param id the ID of the RatingPoints entity whose status is to be updated.
     */
    @Modifying
    @Transactional
    @Query("UPDATE RatingPoints rp SET rp.status = :status WHERE rp.id = :id")
    void updateStatusById(@Param("id") Long id, Status status);

    /**
     * Retrieves a paginated list of RatingPoints entities filtered by their status.
     *
     * @param status   the status to filter the RatingPoints entities.
     * @param pageable the pagination information.
     * @return a Page containing the filtered RatingPoints entities.
     */
    Page<RatingPoints> findAllByStatus(Status status, Pageable pageable);

    /**
     * Updates the name of a RatingPoints entity from oldName to newName.
     *
     * @param oldName the current name of the RatingPoints entity to update.
     * @param newName the new name to set for the RatingPoints entity.
     */
    @Modifying
    @Transactional
    @Query("UPDATE RatingPoints rp SET rp.name = :newName WHERE rp.name = :oldName")
    void updateRatingPointsName(String oldName, String newName);

    /**
     * Checks whether an achievement exists for the specified RatingPoints ID. This
     * method performs a native SQL query to determine if there is an associated
     * achievement whose title matches the name of the RatingPoints entity. If the
     * name starts with 'UNDO_', the prefix is removed for the comparison. The
     * method returns true if at least one matching achievement exists, otherwise
     * false.
     *
     * @param id The ID of the RatingPoints entity to check for associated
     *           achievements.
     * @return true if an associated achievement exists; false otherwise.
     */
    @Transactional
    @Query(nativeQuery = true,
        value = "SELECT COUNT(a) > 0 FROM rating_points rp INNER JOIN achievements a  "
            + "ON a.title = CASE WHEN rp.name LIKE 'UNDO_%' THEN SUBSTRING(rp.name, 6) ELSE rp.name END "
            + "WHERE rp.id = :id")
    boolean checkByIdForExistenceOfAchievement(@Param("id") Long id);

    /**
     * Method finds and retrieves a page of RatingPoints that satisfy the search
     * query while considering the specified status.
     *
     * @param pageable    Pageable configuration for pagination.
     * @param searchQuery The query string to search for in the RatingPoints
     *                    entities. It checks against the ID, name, and points
     *                    fields, allowing for partial matches.
     * @param status      The status to filter the RatingPoints by (e.g., ACTIVE,
     *                    DELETE).
     * @return a Page containing the filtered RatingPoints entities.
     */
    @Query(value = "SELECT rp FROM RatingPoints rp "
        + "WHERE rp.status = :status "
        + "AND (CAST(rp.id AS string) LIKE CONCAT('%', :query, '%') "
        + "OR LOWER(rp.name) LIKE LOWER(CONCAT('%', :query, '%')) "
        + "OR CAST(rp.points AS string) LIKE CONCAT('%', :query, '%'))")
    Page<RatingPoints> searchBy(Pageable pageable, @Param("query") String searchQuery, @Param("status") Status status);
}
