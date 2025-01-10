package greencity.repository;

import greencity.entity.DiscountValue;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
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
    void deleteAllByPlaceId(Long placeId);
}
