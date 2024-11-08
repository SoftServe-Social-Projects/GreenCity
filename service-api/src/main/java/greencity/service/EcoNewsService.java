package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.PageableDto;
import greencity.dto.econews.AddEcoNewsDtoRequest;
import greencity.dto.econews.AddEcoNewsDtoResponse;
import greencity.dto.econews.EcoNewContentSourceDto;
import greencity.dto.econews.EcoNewsDto;
import greencity.dto.econews.EcoNewsDtoManagement;
import greencity.dto.econews.EcoNewsGenericDto;
import greencity.dto.econews.EcoNewsVO;
import greencity.dto.econews.EcoNewsViewDto;
import greencity.dto.econews.UpdateEcoNewsDto;
import greencity.dto.search.SearchNewsDto;
import greencity.dto.user.UserVO;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface EcoNewsService {
    /**
     * Method for creating {@link EcoNewsVO} instance.
     *
     * @param addEcoNewsDtoRequest - dto with {@link EcoNewsVO} title, text, image
     *                             path.
     * @return {@link AddEcoNewsDtoResponse} instance.
     */
    AddEcoNewsDtoResponse save(AddEcoNewsDtoRequest addEcoNewsDtoRequest, MultipartFile image, String email);

    /**
     * Method for creating {@link EcoNewsVO} instance.
     *
     * @param addEcoNewsDtoRequest - dto with {@link EcoNewsVO} title, text, image
     *                             path.
     * @return {@link EcoNewsGenericDto} instance.
     */
    EcoNewsGenericDto saveEcoNews(AddEcoNewsDtoRequest addEcoNewsDtoRequest, MultipartFile image, String email);

    /**
     * Method for getting three recommended eco news.
     *
     * @param ecoNewsId don't include into the list
     * @return list of three {@link EcoNewsDto} instances.
     */
    List<EcoNewsDto> getThreeRecommendedEcoNews(Long ecoNewsId);

    /**
     * Method for getting eco news by filter params.
     *
     * @param page  parameters of to search.
     * @param tags  tags to search.
     * @param title title to search.
     * @return PageableDto with {@link EcoNewsDto} instance.
     */
    PageableAdvancedDto<EcoNewsGenericDto> find(
        Pageable page,
        List<String> tags,
        String title,
        Long authorId,
        boolean favorite,
        String email);

    /**
     * Method for getting the {@link EcoNewsVO} instance by its id.
     *
     * @param id {@link EcoNewsVO} instance id.
     * @return {@link EcoNewsVO} instance.
     */
    EcoNewsVO findById(Long id);

    /**
     * Method for getting the{@link EcoNewsDto} instance by its id and language of
     * tags.
     *
     * @param id       {@link Long} ecoNews id.
     * @param language {@link String} language for searching tags.
     * @return {@link EcoNewsDto} instance
     */
    EcoNewsDto findDtoByIdAndLanguage(Long id, String language);

    /**
     * Method for deleting the {@link EcoNewsVO} instance by its id.
     *
     * @param id   - {@link EcoNewsVO} instance id which will be deleted.
     * @param user current {@link UserVO} that wants to delete.
     */
    void delete(Long id, UserVO user);

    /**
     * Method deletes all {@link EcoNewsVO} by list of ids.
     *
     * @param listId list of id {@link EcoNewsVO}
     */
    void deleteAll(List<Long> listId);

    /**
     * Method for getting all EcoNews by searchQuery.
     *
     * @param pageable    {@link Pageable}.
     * @param searchQuery query to search.
     * @return PageableDto of {@link SearchNewsDto} instances.
     */
    PageableDto<SearchNewsDto> search(Pageable pageable, String searchQuery, Boolean isFavorite, Long userId);

    /**
     * Method for getting all published news by authorised user.
     *
     * @param user {@link UserVO}.
     * @return list of {@link EcoNewsDto} instances.
     */
    List<EcoNewsDto> getAllByUser(UserVO user);

    /**
     * Method for getting amount of all published news or only for one user.
     *
     * @param id {@link Long} user id. May be null.
     * @return amount of published news by user id or all news.
     */
    Long getAmountOfPublishedNews(Long id);

    /**
     * Method for updating {@link EcoNewsVO} instance.
     *
     * @param ecoNewsDtoManagement - instance of {@link EcoNewsDtoManagement}.
     */
    void update(EcoNewsDtoManagement ecoNewsDtoManagement, MultipartFile multipartFile);

    /**
     * Method for updating {@link EcoNewsVO} instance.
     *
     * @param updateEcoNewsDto - instance of {@link UpdateEcoNewsDto}.
     * @return instance of {@link EcoNewsGenericDto}.
     */
    EcoNewsGenericDto update(UpdateEcoNewsDto updateEcoNewsDto, MultipartFile multipartFile, UserVO user);

    /**
     * Method for adding an eco new to favorites by ecoNewsId.
     *
     * @param ecoNewsId - eco-news id.
     * @param email     - user email.
     */
    void addToFavorites(Long ecoNewsId, String email);

    /**
     * Method for removing an eco new from favorites by ecoNewsId.
     *
     * @param ecoNewsId - eco-News id.
     * @param email     - user email.
     */
    void removeFromFavorites(Long ecoNewsId, String email);

    /**
     * Find {@link EcoNewsVO} for management.
     *
     * @return a dto of {@link PageableDto}.
     */
    PageableAdvancedDto<EcoNewsDto> getFilteredDataForManagementByPage(String query, Pageable pageable,
        EcoNewsViewDto ecoNewsViewDto, Locale locale);

    /**
     * Method to mark news as liked by User.
     *
     * @param user - instance of {@link UserVO}
     * @param id   - {@link Long} eco news id.
     */
    void like(UserVO user, Long id);

    /**
     * Method to mark news as disliked by User.
     *
     * @param user - instance of {@link UserVO}
     * @param id   - {@link Long} eco news id.
     */
    void dislike(UserVO user, Long id);

    /**
     * Method to get amount of likes by eco news id.
     *
     * @param id - {@link Integer} eco news id.
     * @return amount of likes by eco news id.
     */
    Integer countLikesForEcoNews(Long id);

    /**
     * Method to get amount of dislikes by eco news id.
     *
     * @param id - {@link Integer} eco news id.
     * @return amount of dislikes by eco news id.
     */
    Integer countDislikesForEcoNews(Long id);

    /**
     * Method to check if user liked news.
     *
     * @param id     - id of {@link EcoNewsVO} to check liked or not.
     * @param userId - id of {@link UserVO}.
     * @return user liked news or not.
     */
    Boolean checkNewsIsLikedByUser(Long id, Long userId);

    /**
     * Method for getting some fields in eco news by id.
     *
     * @param id - {@link Long} eco news id.
     * @return dto {@link EcoNewContentSourceDto}.
     */
    EcoNewContentSourceDto getContentAndSourceForEcoNewsById(Long id);

    /**
     * Method for getting list of users who liked post by post id.
     *
     * @param id - {@link Long} eco news id.
     * @return set of {@link UserVO} instances.
     */
    Set<UserVO> findUsersWhoLikedPost(Long id);

    /**
     * Method for getting list of users who disliked post by post id.
     *
     * @param id - {@link Long} eco news id.
     * @return set of {@link UserVO} instances.
     */
    Set<UserVO> findUsersWhoDislikedPost(Long id);

    /**
     * Method for hiding/unhiding the {@link EcoNewsVO} instance by its id.
     *
     * @param id    - {@link EcoNewsVO} instance id which will be hidden/unhidden.
     * @param user  current {@link UserVO} that wants to hide.
     * @param value value to be set to hidden field.
     */
    void setHiddenValue(Long id, UserVO user, boolean value);

    /**
     * Method for getting 3 eco-news sorted by likes and then by comments.
     *
     * @return list of {@link EcoNewsDto} instances.
     */
    List<EcoNewsDto> getThreeInterestingEcoNews();
}