package greencity.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import greencity.constant.ErrorMessage;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.ImageUrlParseException;
import greencity.exception.exceptions.NotSavedException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AzureCloudStorageService implements FileService {
    private final ModelMapper modelMapper;

    @Value("${azure.connection.string}")
    private String connectionString;

    @Value("${azure.container.name}")
    private String containerName;

    /**
     * {@inheritDoc}
     */
    public String upload(MultipartFile file) {
        return upload(List.of(file)).getFirst();
    }

    /**
     * {@inheritDoc}
     */
    public List<String> upload(List<MultipartFile> files) {
        return files.stream()
            .map(pic -> {
                String blob = UUID.randomUUID().toString();
                BlobClient client = containerClient()
                    .getBlobClient(blob + pic.getOriginalFilename());
                try {
                    client.upload(new BufferedInputStream(pic.getInputStream()), pic.getSize());
                } catch (IOException e) {
                    throw new NotSavedException(ErrorMessage.FILE_NOT_SAVED);
                }
                return client.getBlobUrl();
            })
            .toList();
    }

    private BlobContainerClient containerClient() {
        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
            .connectionString(connectionString).buildClient();
        return serviceClient.getBlobContainerClient(containerName);
    }

    /**
     * {@inheritDoc}
     */
    public MultipartFile convertToMultipartImage(String image) {
        try {
            return modelMapper.map(image, MultipartFile.class);
        } catch (Exception e) {
            throw new BadRequestException(ErrorMessage.MULTIPART_FILE_BAD_REQUEST + image);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String path) {
        String fileName;
        try {
            fileName = Paths.get(new URI(path).getPath()).getFileName().toString();
        } catch (URISyntaxException e) {
            throw new ImageUrlParseException(ErrorMessage.PARSING_URL_FAILED + path);
        }
        BlobClient client = containerClient().getBlobClient(fileName);
        if (Boolean.TRUE.equals(client.exists())) {
            client.delete();
        }
    }
}
