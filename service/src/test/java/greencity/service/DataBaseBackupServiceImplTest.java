package greencity.service;

import greencity.entity.DataBaseBackUpFiles;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.DataBaseBackUpFilesRepo;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DataBaseBackupServiceImplTest {
    @Mock
    private DataBaseBackUpFilesRepo dataBaseBackUpFilesRepo;

    @Spy
    @InjectMocks
    private DatabaseBackupServiceImpl databaseBackupService;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUser;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${azure.connection.string}")
    private String azureStorageConnectionString;

    @Value("${azure.container.name}")
    private String containerName;

    @Value("${pg.dump.path}")
    private String path;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetBackUpDBUrlsSuccess() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now();

        when(dataBaseBackUpFilesRepo.findAllByCreatedAtBetween(start, end))
            .thenReturn(Arrays.asList(new DataBaseBackUpFiles(1L, "http://backup1.com", LocalDateTime.now()),
                new DataBaseBackUpFiles(2L, "http://backup2.com", LocalDateTime.now())));

        List<String> urls = databaseBackupService.getBackUpDBUrls(start, end);

        assertNotNull(urls);
        assertEquals(2, urls.size());
        assertEquals("http://backup1.com", urls.get(0));
        assertEquals("http://backup2.com", urls.get(1));
    }

    @Test
    void testGetBackUpDBUrlsEmptyList() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now();

        when(dataBaseBackUpFilesRepo.findAllByCreatedAtBetween(start, end)).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> databaseBackupService.getBackUpDBUrls(start, end));
    }

    @Test
    void testGetBackUpDBUrlsInvalidTimeRange() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().minusHours(1);

        assertThrows(BadRequestException.class, () -> databaseBackupService.getBackUpDBUrls(start, end));
    }
}
