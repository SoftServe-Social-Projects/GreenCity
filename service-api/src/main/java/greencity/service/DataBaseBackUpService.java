package greencity.service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for the service that handles database backup operations.
 */
public interface DataBaseBackUpService {
    /**
     * Creates a backup of the database. This method should initiate the process of
     * backing up the entire database.
     */
    void backupDatabase();

    /**
     * Retrieves URLs of backup files created between the specified start and end
     * times.
     *
     * @param start the start time of the range, inclusive
     * @param end   the end time of the range, inclusive
     * @return a list of URLs of backup files created within the specified time
     *         range
     */
    List<String> getBackUpDBUrls(LocalDateTime start, LocalDateTime end);
}
