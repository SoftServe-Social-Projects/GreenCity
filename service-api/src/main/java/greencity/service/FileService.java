package greencity.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * Upload file to Azure Cloud Storage.
     *
     * @param file file to save.
     * @return public url of file.
     **/
    String upload(MultipartFile file);

    /**
     * Upload files to Azure Cloud Storage.
     *
     * @param files list of files to save.
     * @return list of public files urls.
     **/
    List<String> upload(List<MultipartFile> files);

    /**
     * Convert string to MultipartFile.
     *
     * @param image image to convert.
     * @return MultipartFile.
     **/
    MultipartFile convertToMultipartImage(String image);

    /**
     * Delete file from Azure Cloud Storage.
     *
     * @param path public file url.
     */
    void delete(String path);
}
