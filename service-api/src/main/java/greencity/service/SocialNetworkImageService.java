package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.socialnetwork.SocialNetworkImageRequestDTO;
import greencity.dto.socialnetwork.SocialNetworkImageResponseDTO;
import greencity.dto.socialnetwork.SocialNetworkImageVO;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * SocialNetworkImageService interface.
 *
 * @author Mykola Lehkyi
 */
public interface SocialNetworkImageService {
    /**
     * Method creates or returns existed {@link SocialNetworkImageVO} by given url.
     *
     * @param url a well-formed url
     * @return {@link SocialNetworkImageVO}
     */
    SocialNetworkImageVO getSocialNetworkImageByUrl(String url);

    /**
     * Find {@link SocialNetworkImageResponseDTO} for management by page .
     *
     * @param pageable a value with pageable configuration.
     * @return a dto of {@link PageableDto}.
     * @author Orest Mamchuk
     */
    PageableDto<SocialNetworkImageResponseDTO> findAll(Pageable pageable);

    /**
     * Method for deleting the {@link SocialNetworkImageVO} instance by its id.
     *
     * @param id - {@link SocialNetworkImageVO} instance id which will be deleted.
     */
    void delete(Long id);

    /**
     * Method deletes all {@link SocialNetworkImageVO} by list of ids.
     *
     * @param listId list of id {@link SocialNetworkImageVO}
     */
    void deleteAll(List<Long> listId);

    /**
     * Method for creating {@link SocialNetworkImageVO} instance.
     *
     * @param socialNetworkImageRequestDTO - dto with
     *                                     {@link SocialNetworkImageRequestDTO}
     *                                     title, text, image path.
     * @return {@link SocialNetworkImageResponseDTO} instance.
     */
    SocialNetworkImageResponseDTO save(SocialNetworkImageRequestDTO socialNetworkImageRequestDTO, MultipartFile image);

    /**
     * Method for getting the {@link SocialNetworkImageResponseDTO} instance by its
     * id.
     *
     * @param id {@link SocialNetworkImageResponseDTO} instance id.
     * @return {@link SocialNetworkImageResponseDTO} instance.
     */
    SocialNetworkImageResponseDTO findDtoById(Long id);

    /**
     * Method for updating {@link SocialNetworkImageVO} instance.
     *
     * @param socialNetworkImageResponseDTO - instance of
     *                                      {@link SocialNetworkImageVO}.
     */
    void update(SocialNetworkImageResponseDTO socialNetworkImageResponseDTO, MultipartFile multipartFile);
}
