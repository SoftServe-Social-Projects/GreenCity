package greencity.repository;

import greencity.entity.FactOfTheDay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FactOfTheDayRepo extends JpaRepository<FactOfTheDay, Long> {
    /**
     * Method finds {@link FactOfTheDay} that satisfy search query.
     * @param searchQuery query to search
     * @return pageable of fact of the day
     */
    @Query(nativeQuery = true, value = "SELECT DISTINCT fd.* FROM fact_of_the_day as fd "
        + "JOIN fact_of_the_day_translations as fdt "
        + "ON fd.id = fdt.fact_of_the_day_id "
        + "where CONCAT(fd.id,'') like lower(CONCAT(:searchQuery)) "
        + "or lower(fd.name) like lower(CONCAT('%', :searchQuery, '%')) "
        + "or lower(fdt.content) like lower(CONCAT('%', :searchQuery, '%')) "
        + "or CONCAT(fdt.id,'') like lower(CONCAT(:searchQuery)) ")
    Page<FactOfTheDay> searchBy(Pageable pageable, String searchQuery);
}
