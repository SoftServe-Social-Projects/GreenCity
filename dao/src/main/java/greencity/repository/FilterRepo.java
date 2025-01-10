package greencity.repository;

import greencity.entity.Filter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FilterRepo extends JpaRepository<Filter, Long> {
    /**
     * Method that return user's filter.
     *
     * @param userId user's id.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM filters f WHERE f.user_id =:userId")
    List<Filter> getAllFilters(@Param("userId") Long userId);
}
