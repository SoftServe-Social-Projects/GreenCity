package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.ratingstatistics.RatingPointsDto;
import greencity.enums.Status;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Provides the interface to manage {@link RatingPointsDto}.
 */
public interface RatingPointsService {
    /**
     * Get all {@link RatingPointsDto} with pageable configuration.
     *
     * @param pageable {@link Pageable} object to configure page size, number, and
     *                 sorting options
     * @return {@link PageableAdvancedDto} containing the list of all
     *         {@link RatingPointsDto} and pagination details
     */
    PageableAdvancedDto<RatingPointsDto> getAllRatingPointsByPage(Pageable pageable);

    /**
     * Saves a new {@link RatingPointsDto} with the specified name to the database
     * and returns the created object.
     *
     * @param ratingPointsName the name of the new rating points to be created
     * @return the list of {@link RatingPointsDto} created after persisting it to
     *         the database
     */
    List<RatingPointsDto> createRatingPoints(String ratingPointsName);

    /**
     * Updates the specified {@link RatingPointsDto} in the database.
     *
     * @param ratingPoints the {@link RatingPointsDto} object containing the updated
     *                     values
     * @return the updated {@link RatingPointsDto} after saving it to the database
     */
    RatingPointsDto updateRatingPoints(RatingPointsDto ratingPoints);

    /**
     * Deletes the {@link RatingPointsDto} entity identified by the specified ID.
     *
     * @param id the ID of the {@link RatingPointsDto} to be deleted
     */
    void deleteRatingPoints(Long id);

    /**
     * Get deleted {@link RatingPointsDto} entities with pageable configuration.
     *
     * @param pageable {@link Pageable} object to configure page size, number, and
     *                 sorting options
     * @return {@link PageableAdvancedDto} containing the list of deleted
     *         {@link RatingPointsDto} and pagination details
     */
    PageableAdvancedDto<RatingPointsDto> getDeletedRatingPoints(Pageable pageable);

    /**
     * Restores a deleted {@link RatingPointsDto} entity identified by the specified
     * ID.
     *
     * @param id the ID of the {@link RatingPointsDto} to be restored
     */
    void restoreDeletedRatingPoints(Long id);

    /**
     * Updates the name of a {@link RatingPointsDto} identified by the old name.
     *
     * @param oldName the current name of the Rating Points to be updated
     * @param newName the new name to assign to the Rating Points
     */
    void updateRatingPointsName(String oldName, String newName);

    /**
     * Retrieves a paginated list of {@link RatingPointsDto} that match the provided
     * search query.
     *
     * @param pageable    The pagination information, including page number and
     *                    size.
     * @param searchQuery The query string used to filter the RatingPoints.
     * @param status      The status of the RatingPoints to filter (e.g., ACTIVE,
     *                    DELETE).
     * @return A {@link PageableAdvancedDto} containing the filtered results.
     */
    PageableAdvancedDto<RatingPointsDto> searchBy(Pageable pageable, String searchQuery, Status status);
}
