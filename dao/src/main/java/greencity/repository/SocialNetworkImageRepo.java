package greencity.repository;

import greencity.entity.SocialNetworkImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SocialNetworkImageRepo extends JpaRepository<SocialNetworkImage, Long> {
    /**
     * Method finds {@link SocialNetworkImage} by given host address.
     *
     * @param hostPath host address
     * @return Optional of {@link SocialNetworkImage}
     */
    Optional<SocialNetworkImage> findByHostPath(String hostPath);

    /**
     * Method returns {@link SocialNetworkImage} by search query and page.
     *
     * @param paging {@link Pageable}.
     * @param query  query to search.
     * @return list of {@link SocialNetworkImage}.
     */
    @Query("SELECT s FROM SocialNetworkImage s WHERE CONCAT(s.id,'') LIKE LOWER(CONCAT('%', :query, '%')) "
        + "OR LOWER(s.imagePath) LIKE LOWER(CONCAT('%', :query, '%'))"
        + "OR LOWER(s.hostPath) LIKE LOWER(CONCAT('%', :query, '%')) ")
    Page<SocialNetworkImage> searchBy(Pageable paging, String query);
}
