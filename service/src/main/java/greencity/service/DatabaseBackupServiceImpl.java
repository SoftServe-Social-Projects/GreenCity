package greencity.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import greencity.constant.ErrorMessage;
import greencity.entity.DataBaseBackUpFiles;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.DataBaseBackUpFilesRepo;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service implementation for handling database backup operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DatabaseBackupServiceImpl implements DataBaseBackUpService {
    private final DataBaseBackUpFilesRepo dataBaseBackUpFilesRepo;

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

    /**
     * Creates a backup of the database by executing the pg_dump command. Extracts
     * database connection details from the datasource URL and uses them to run the
     * backup.
     */
    @Override
    public void backupDatabase() {
        DatabaseDetails dbDetails = extractDatabaseDetails(datasourceUrl);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            ProcessBuilder processBuilder =
                new ProcessBuilder(path,
                    "-h", dbDetails.host,
                    "-p", dbDetails.port,
                    "-U", datasourceUser, "-d",
                    dbDetails.dbName);

            processBuilder.environment().put("PGPASSWORD", datasourcePassword);

            Process process = processBuilder.start();

            int exitCode;
            try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {
                executorService.submit(() -> readProcessOutput(process.getInputStream(), byteArrayOutputStream));
                executorService.submit(() -> logProcessErrors(process.getErrorStream()));

                exitCode = process.waitFor();
                executorService.shutdown();
                executorService.awaitTermination(2, TimeUnit.MINUTES);
            }

            if (exitCode == 0) {
                uploadToAzureBlobStorage(byteArrayOutputStream);
                log.info("Backup completed successfully");
            } else {
                log.error("Backup failed");
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error during backup process", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getBackUpDBUrls(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || start.isAfter(end) || end.isBefore(start)) {
            throw new BadRequestException(ErrorMessage.INVALID_TIME_RANGE);
        }

        List<String> urls = dataBaseBackUpFilesRepo.findAllByCreatedAtBetween(start, end)
            .stream()
            .map(DataBaseBackUpFiles::getUrl)
            .collect(Collectors.toList());

        if (urls.isEmpty()) {
            throw new NotFoundException(ErrorMessage.NOT_FOUND_IN_CURRENT_TIME_RANGE);
        }
        return urls;
    }

    private DatabaseDetails extractDatabaseDetails(String url) {
        String host = "localhost";
        String port = "5432";
        String dbName = "";

        Pattern pattern = Pattern.compile("jdbc:postgresql://([^:/]+)(?::(\\d+))?/([^?]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            host = matcher.group(1);
            if (matcher.group(2) != null) {
                port = matcher.group(2);
            }
            dbName = matcher.group(3);
        }
        return new DatabaseDetails(host, port, dbName);
    }

    private void readProcessOutput(InputStream inputStream, ByteArrayOutputStream outputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            log.error("Error reading process output", e);
        }
    }

    private void logProcessErrors(InputStream errorStream) {
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream))) {
            String line;
            while ((line = errorReader.readLine()) != null) {
                log.error(line);
            }
        } catch (IOException e) {
            log.error("Error reading process errors", e);
        }
    }

    private void uploadToAzureBlobStorage(ByteArrayOutputStream byteArrayOutputStream) {
        byte[] backupData = byteArrayOutputStream.toByteArray();
        try (InputStream inputStream = new ByteArrayInputStream(backupData)) {
            BlobClient blobClient = new BlobClientBuilder()
                .connectionString(azureStorageConnectionString)
                .containerName(containerName)
                .blobName("backup-" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE) + ".sql")
                .buildClient();

            blobClient.upload(inputStream, backupData.length, true);

            saveBackUpToDB(blobClient.getBlobUrl());

            log.info("Backup uploaded to Azure Blob Storage successfully");
        } catch (IOException e) {
            log.error("Error uploading backup to Azure Blob Storage", e);
        }
    }

    private void saveBackUpToDB(String url) {
        DataBaseBackUpFiles backUp = DataBaseBackUpFiles.builder()
            .url(url)
            .createdAt(LocalDateTime.now())
            .build();
        DataBaseBackUpFiles saved = dataBaseBackUpFilesRepo.save(backUp);
        log.info("Successfully saved backup with id: {}", saved.getId());
    }

    @AllArgsConstructor
    private static class DatabaseDetails {
        String host;
        String port;
        String dbName;
    }
}
