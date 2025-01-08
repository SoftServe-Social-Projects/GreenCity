package greencity.repository;

import greencity.entity.PlaceComment;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides an interface to manage {@link PlaceComment} entity.
 */
@Repository
public interface PlaceCommentRepo extends JpaRepository<PlaceComment, Long> {
    /**
     * Method with return {@link Optional} of {@link PlaceComment} by comment id.
     *
     * @param id of {@link PlaceComment}.
     * @return {@link Optional} of {@link PlaceComment} .
     * @author Marian Milian
     */
    Optional<PlaceComment> findById(Long id);

    /**
     * Method with return {@link Page} of {@link PlaceComment} - s.
     *
     * @param pageable pageable configuration.
     * @return {@link Page}.
     * @author Rostyslav Khasanov
     */
    Page<PlaceComment> findAll(Pageable pageable);
}
