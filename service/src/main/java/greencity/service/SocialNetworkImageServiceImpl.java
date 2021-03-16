package greencity.service;

import greencity.constant.AppConstant;
import greencity.constant.CacheConstants;
import greencity.constant.ErrorMessage;
import greencity.constant.LogMessage;
import greencity.dto.PageableDto;
import greencity.dto.socialnetwork.SocialNetworkImageRequestDTO;
import greencity.dto.socialnetwork.SocialNetworkImageResponseDTO;
import greencity.dto.socialnetwork.SocialNetworkImageVO;
import greencity.entity.SocialNetworkImage;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.NotSavedException;
import greencity.repository.SocialNetworkImageRepo;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

@Service
@Slf4j
@AllArgsConstructor
@EnableCaching
public class SocialNetworkImageServiceImpl implements SocialNetworkImageService {
    private final SocialNetworkImageRepo socialNetworkImageRepo;
    private final FileService fileService;
    private final ModelMapper modelMapper;

    /**
     * Method creates or returns existed {@link SocialNetworkImage} by given url.
     *
     * @param url a well-formed url
     * @return {@link SocialNetworkImage}
     */
    @Override
    public SocialNetworkImageVO getSocialNetworkImageByUrl(String url) {
        try {
            URL checkUrl = new URL(url);
            Optional<SocialNetworkImageVO> optionalSocialNetworkImageVO =
                findByHostPath(checkUrl.getHost());
            return modelMapper.map(optionalSocialNetworkImageVO.isPresent() ? optionalSocialNetworkImageVO.get()
                : saveSocialNetworkImage(checkUrl), SocialNetworkImageVO.class);
        } catch (IOException e) {
            log.info(e.getMessage());
            return getDefaultSocialNetworkImage();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Orest Mamchuk
     */
    @Override
    public PageableDto<SocialNetworkImageResponseDTO> findAll(Pageable pageable) {
        log.info(LogMessage.IN_FIND_ALL);

        Page<SocialNetworkImage> pages = socialNetworkImageRepo.findAll(pageable);
        List<SocialNetworkImageResponseDTO> socialNetworkImageResponseDTOS = pages.stream()
            .map(socialNetworkImage -> modelMapper.map(socialNetworkImage, SocialNetworkImageResponseDTO.class))
            .collect(Collectors.toList());
        return new PageableDto<>(socialNetworkImageResponseDTOS,
            pages.getTotalElements(),
            pages.getPageable().getPageNumber(),
            pages.getTotalPages());
    }

    /**
     * {@inheritDoc}
     *
     * @author Orest Mamchuk
     */
    @Override
    public PageableDto<SocialNetworkImageResponseDTO> searchBy(Pageable paging, String query) {
        Page<SocialNetworkImage> page = socialNetworkImageRepo.searchBy(paging, query);
        List<SocialNetworkImageResponseDTO> socialNetworkImageResponseDTOS = page.stream()
            .map(socialNetworkImage -> modelMapper.map(socialNetworkImage, SocialNetworkImageResponseDTO.class))
            .collect(Collectors.toList());
        return new PageableDto<>(
            socialNetworkImageResponseDTOS,
            page.getTotalElements(),
            page.getPageable().getPageNumber(),
            page.getTotalPages());
    }

    /**
     * Method for deleting {@link SocialNetworkImage} by its id.
     *
     * @param id - {@link SocialNetworkImage} instance id which will be deleted.
     */
    @Override
    public void delete(Long id) {
        socialNetworkImageRepo.deleteById(id);
    }

    /**
     * Method for deleting all {@link SocialNetworkImage} instance by list of IDs.
     *
     * @param listId list of id {@link SocialNetworkImage}
     */
    @Override
    public void deleteAll(List<Long> listId) {
        listId.forEach(socialNetworkImageRepo::deleteById);
    }

    @Override
    public SocialNetworkImageResponseDTO save(SocialNetworkImageRequestDTO socialNetworkImageRequestDTO,
        MultipartFile image) {
        SocialNetworkImage toSave = modelMapper.map(socialNetworkImageRequestDTO, SocialNetworkImage.class);
        if (image != null) {
            toSave.setImagePath(fileService.upload(image).toString());
        }

        try {
            socialNetworkImageRepo.save(toSave);
        } catch (DataIntegrityViolationException e) {
            throw new NotSavedException(ErrorMessage.SOCIAL_NETWORK_IMAGE_NOT_SAVED);
        }

        return modelMapper.map(toSave, SocialNetworkImageResponseDTO.class);
    }

    /**
     * {@inheritDoc} Method for finding {@link SocialNetworkImage} by id
     *
     * @param id {@link SocialNetworkImage} instance id.
     * @return {@link SocialNetworkImage}
     */
    private SocialNetworkImage findById(Long id) {
        return socialNetworkImageRepo
            .findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.SOCIAL_NETWORK_IMAGE_FOUND_BY_ID + id));
    }

    /**
     * {@inheritDoc} Method for finding {@link SocialNetworkImageResponseDTO} by id
     *
     * @param id {@link SocialNetworkImageResponseDTO} instance id.
     * @return dto {@link SocialNetworkImageResponseDTO}
     */
    @Override
    public SocialNetworkImageResponseDTO findDtoById(Long id) {
        SocialNetworkImage socialNetworkImage = findById(id);
        return modelMapper.map(socialNetworkImage, SocialNetworkImageResponseDTO.class);
    }

    /**
     * {@inheritDoc} Method for updating {@link SocialNetworkImage}
     *
     * @param socialNetworkImageResponseDTO - instance of
     *                                      {@link SocialNetworkImageResponseDTO}.
     */
    @Override
    public void update(SocialNetworkImageResponseDTO socialNetworkImageResponseDTO, MultipartFile image) {
        SocialNetworkImage toUpdate = findById(socialNetworkImageResponseDTO.getId());
        toUpdate.setHostPath(socialNetworkImageResponseDTO.getHostPath());
        if (image != null) {
            toUpdate.setImagePath(fileService.upload(image).toString());
        }
        socialNetworkImageRepo.save(toUpdate);
    }

    /**
     * Method search SocialNetworkImage.
     *
     * @param hostPath host adress
     * @return optional of {@link SocialNetworkImage}
     */
    @Cacheable(value = CacheConstants.SOCIAL_NETWORK_IMAGE_CACHE_NAME)
    public Optional<SocialNetworkImageVO> findByHostPath(String hostPath) {
        Optional<SocialNetworkImage> socialNetworkImage = socialNetworkImageRepo.findByHostPath(hostPath);
        return modelMapper.map(socialNetworkImage, new TypeToken<Optional<SocialNetworkImageVO>>() {
        }.getType());
    }

    /**
     * Method creates, saves {@link SocialNetworkImage} by given URL.
     *
     * @param url link path
     * @return {@link SocialNetworkImage} result of creation
     */
    @CacheEvict(value = CacheConstants.SOCIAL_NETWORK_IMAGE_CACHE_NAME, allEntries = true)
    public SocialNetworkImageVO saveSocialNetworkImage(URL url) throws IOException {
        String imagePath = uploadImageToCloud(url);
        SocialNetworkImage socialNetworkImage = SocialNetworkImage.builder()
            .hostPath(url.getHost())
            .imagePath(imagePath)
            .build();
        return modelMapper.map(socialNetworkImageRepo.save(socialNetworkImage), SocialNetworkImageVO.class);
    }

    /**
     * Method return default social network image from DB by host key
     * DEFAULT_SOCIAL_NETWORK_IMAGE_HOST_PATH.
     *
     * @return {@link SocialNetworkImage}
     */
    public SocialNetworkImageVO getDefaultSocialNetworkImage() {
        return findByHostPath(AppConstant.DEFAULT_SOCIAL_NETWORK_IMAGE_HOST_PATH)
            .orElseThrow(() -> new RuntimeException(ErrorMessage.BAD_DEFAULT_SOCIAL_NETWORK_IMAGE_PATH));
    }

    /**
     * Method downloads icon from URL and transforms it into {@link File}. Calls
     * uploadFileToCloud().
     *
     * @param url URL of give page
     * @return URL.toString() image file location
     */
    private String uploadImageToCloud(URL url) throws IOException {
        String preparedUrlHost = url.getHost();
        String preparedFaviconUrl = String.format("http://www.google.com/s2/favicons?sz=64&domain_url=%s", URLEncoder
            .encode(preparedUrlHost, "UTF-8"));
        URL faviconUrl = new URL(preparedFaviconUrl);
        BufferedImage bufferedImage = ImageIO.read(faviconUrl);
        File tempFile = new File("tempImage.png");
        ImageIO.write(bufferedImage, "png", tempFile);
        return uploadFileToCloud(tempFile);
    }

    /**
     * Method uploads file to cloud.
     *
     * @param tempFile file to upload
     * @return URL.toString() file location
     */
    private String uploadFileToCloud(File tempFile) throws IOException {
        FileItem fileItem = new DiskFileItem("mainFile", Files.probeContentType(tempFile.toPath()),
            false, tempFile.getName(), (int) tempFile.length(), tempFile.getParentFile());
        try (InputStream inputStream = new FileInputStream(tempFile);
            OutputStream outputStream = fileItem.getOutputStream()) {
            IOUtils.copy(inputStream, outputStream);
            outputStream.flush();
        }
        CommonsMultipartFile multipartFile = new CommonsMultipartFile(fileItem);
        URL uploadCloud = fileService.upload(multipartFile);
        return uploadCloud.toString();
    }
}
