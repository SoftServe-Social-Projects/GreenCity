package greencity.repository;

import greencity.entity.DiscountValue;
import java.util.Set;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountValuesRepo extends JpaRepository<DiscountValue, Long> {
    /**
     * Finds all {@link DiscountValue} records related to the specified
     * {@code Place}.
     *
     * @param placeId to find by.
     * @return a set of the {@code DiscountValue} for the place by id.
     */
    Set<DiscountValue> findAllByPlaceId(Long placeId);

    /**
     * Delete all {@code DiscountValue} records related to the specified
     * {@code Place}.
     *
     * @param placeId to find by.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM DiscountValue d WHERE d.place.id = :placeId")
    void deleteAllByPlaceId(@Param("placeId") Long placeId);
}
