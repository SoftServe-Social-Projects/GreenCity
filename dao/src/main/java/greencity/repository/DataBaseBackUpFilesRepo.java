package greencity.repository;

import greencity.entity.DataBaseBackUpFiles;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataBaseBackUpFilesRepo extends JpaRepository<DataBaseBackUpFiles, Long> {
    /**
     * Finds all backup files created between the specified start and end times.
     *
     * @param start the start time of the range, inclusive
     * @param end   the end time of the range, inclusive
     * @return a list of backup files created within the specified time range
     */
    List<DataBaseBackUpFiles> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
