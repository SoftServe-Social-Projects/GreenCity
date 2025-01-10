package greencity.service;

import com.google.maps.model.GeocodingResult;
import greencity.dto.PageableDto;
import greencity.dto.filter.FilterPlacesApiDto;
import greencity.dto.filter.FilterPlaceDto;
import greencity.dto.place.AddPlaceDto;
import greencity.dto.place.AdminPlaceDto;
import greencity.dto.place.BulkUpdatePlaceStatusDto;
import greencity.dto.place.FilterAdminPlaceDto;
import greencity.dto.place.FilterPlaceCategory;
import greencity.dto.place.PlaceAddDto;
import greencity.dto.place.PlaceByBoundsDto;
import greencity.dto.place.PlaceInfoDto;
import greencity.dto.place.PlaceResponse;
import greencity.dto.place.PlaceUpdateDto;
import greencity.dto.place.PlaceVO;
import greencity.dto.place.UpdatePlaceStatusDto;
import greencity.dto.place.UpdatePlaceStatusWithUserEmailDto;
import greencity.dto.search.SearchPlacesDto;
import greencity.dto.user.UserVO;
import greencity.enums.PlaceStatus;
import greencity.exception.exceptions.NotFoundException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * Provides the interface to manage {@code Place} entity.
 */
public interface PlaceService {
    /**
     * Finds all {@code Place} with status {@code PlaceStatus}.
     *
     * @param placeStatus a value of {@link PlaceStatus} enum.
     * @param pageable    pageable configuration.
     * @return an object of {@link PageableDto} which contains a list of
     *         {@link AdminPlaceDto}.
     * @author Roman Zahorui
     */
    PageableDto<AdminPlaceDto> getPlacesByStatus(PlaceStatus placeStatus, Pageable pageable);

    /**
     * Update status for the {@link PlaceVO} and set the time of modification.
     *
     * @param id     - {@link PlaceVO} id.
     * @param status - {@link PlaceVO} status.
     * @return saved {@link UpdatePlaceStatusDto} entity.
     */
    UpdatePlaceStatusDto updateStatus(Long id, PlaceStatus status);

    /**
     * Update statuses for the {@link PlaceVO}'s and set the time of modification.
     *
     * @param dto - {@link BulkUpdatePlaceStatusDto} with places id's and updated
     *            {@link PlaceStatus}
     * @return list of {@link UpdatePlaceStatusDto} with updated places and
     *         statuses.
     */
    List<UpdatePlaceStatusDto> updateStatuses(BulkUpdatePlaceStatusDto dto);

    /**
     * Find place by it's id.
     *
     * @param id - place id.
     * @return Place entity.
     */
    PlaceVO findById(Long id);

    /**
     * Method with return {@link Optional} of {@link PlaceVO} by comment id.
     *
     * @param id of {@link PlaceVO}.
     * @return {@link Optional} of {@link PlaceVO} .
     * @author Marian Milian
     */
    Optional<PlaceVO> findByIdOptional(Long id);

    /**
     * Method for saving proposed {@link PlaceVO} to database.
     *
     * @param dto - dto for Place entity
     * @return place {@code Place}
     * @author Kateryna Horokh
     */
    PlaceVO save(PlaceAddDto dto, String email);

    /**
     * Method to find all created {@link PlaceVO}'s by user id.
     *
     * @param userId - {@code User}'s id.
     * @return list of {@link PlaceVO}'s
     */
    List<PlaceVO> getAllCreatedPlacesByUserId(Long userId);

    /**
     * Method for updating {@link PlaceVO}.
     *
     * @param dto - dto for Place entity
     * @return place {@link PlaceVO}
     * @author Kateryna Horokh
     */
    PlaceVO update(PlaceUpdateDto dto);

    /**
     * Find all places from DB.
     *
     * @return List of places.
     */
    List<PlaceVO> findAll();

    /**
     * Find all places from DB for User with current email.
     *
     * @param pageable  {@link Pageable}.
     * @param principal {@link Principal}. Represents loggedIn User to show if place
     *                  isFavorite.
     * @return an object of {@link PageableDto} which contains a list of
     *         {@link AdminPlaceDto}.
     * @author Olena Petryshak
     * @author Olena Sotnik
     */
    PageableDto<AdminPlaceDto> findAll(Pageable pageable, Principal principal);

    /**
     * Method for deleting place by id.
     *
     * @param id - Long place's id
     */
    void deleteById(Long id);

    /**
     * Method for deleting places by ids.
     *
     * @param ids - List of id
     * @return count of deleted places
     */
    Long bulkDelete(List<Long> ids);

    /**
     * Method for getting place information.
     *
     * @param id place
     * @return PlaceInfoDto with info about place
     * @author Dmytro Dovhal
     */
    PlaceInfoDto getInfoById(Long id);

    /**
     * Check {@link PlaceVO} existing by id.
     *
     * @param id - {@link PlaceVO} id
     * @return boolean check result
     * @author Zakhar Skaletskyi
     */
    boolean existsById(Long id);

    /**
     * The method which return a list {@code PlaceByBoundsDto} with information
     * about place, * location depends on the map bounds.
     *
     * @param filterPlaceDto contains northEastLng, northEastLat,southWestLat,
     *                       southWestLng of current state of map
     * @return a list of {@code PlaceByBoundsDto}
     * @author Marian Milian.
     */
    List<PlaceByBoundsDto> findPlacesByMapsBounds(FilterPlaceDto filterPlaceDto);

    /**
     * Get average rate of {@link PlaceVO}.
     *
     * @param id - {@link PlaceVO} id
     * @return byte rate number
     * @author Zakhar Skaletskyi
     */
    Double averageRate(Long id);

    /**
     * The method finds all {@link PlaceVO}'s filtered by the parameters contained
     * in {@param filterDto} object.
     *
     * @param filterDto contains objects whose values determine the filter
     *                  parameters of the returned list.
     * @return a list of {@link PlaceByBoundsDto}
     * @author Roman Zahouri
     */
    List<PlaceByBoundsDto> getPlacesByFilter(FilterPlaceDto filterDto,
        UserVO userVO);

    /**
     * The method finds all {@link GeocodingResult}'s from {@link GoogleApiService}
     * filtered by the parameters contained in {@param filterDto} object.
     *
     * @param filterDto contains objects whose values determine the filter
     *                  parameters of the returned list.
     * @return a list of {@link PlaceByBoundsDto}
     * @author Roman Zahouri
     */
    List<PlaceByBoundsDto> getPlacesByFilter(FilterPlacesApiDto filterDto,
        UserVO userVO);

    /**
     * The method finds all {@link PlaceVO}'s filtered by the parameters contained
     * in {@param filterDto} object.
     *
     * @param filterDto contains objects whose values determine the filter
     *                  parameters of the returned list.
     * @param pageable  pageable configuration.
     * @return a list of {@code PlaceByBoundsDto}
     * @author Rostyslav Khasanov
     */
    PageableDto<AdminPlaceDto> filterPlaceBySearchPredicate(FilterPlaceDto filterDto, Pageable pageable);

    /**
     * Method finds all filtered places for admin page.
     *
     * @param filterDto contains objects whose values determine the filter
     *                  parameters of the returned list.
     * @param pageable  pageable configuration.
     * @return list of {@link AdminPlaceDto}
     */
    PageableDto<AdminPlaceDto> getFilteredPlacesForAdmin(FilterAdminPlaceDto filterDto, Pageable pageable);

    /**
     * Get list of available statuses of {@link PlaceVO}.
     *
     * @return available {@link PlaceVO} statuses.
     */
    List<PlaceStatus> getStatuses();

    /**
     * Method for getting place information by id.
     *
     * @param id place
     * @return PlaceUpdateDto with info about place
     * @author Kateryna Horokh
     */
    PlaceUpdateDto getInfoForUpdatingById(Long id);

    /**
     * Method for getting Places by searchQuery.
     *
     * @param pageable    {@link Pageable}.
     * @param searchQuery query to search,
     * @return an object of {@link PageableDto} which contains a list of
     *         {@link AdminPlaceDto}.
     * @author Olena Petryshak
     */
    PageableDto<AdminPlaceDto> searchBy(Pageable pageable, String searchQuery);

    /**
     * Method to get all Place Filter Categories.
     */
    List<FilterPlaceCategory> getAllPlaceCategories();

    /**
     * Method for create new place From UI.
     */
    PlaceResponse addPlaceFromUi(AddPlaceDto dto, String email, MultipartFile[] images);

    /**
     * Method for getting Places by searchQuery.
     *
     * @param pageable    {@link Pageable}
     * @param searchQuery query to search
     * @return PageableDto of {@link SearchPlacesDto} instances
     */
    PageableDto<SearchPlacesDto> search(Pageable pageable, String searchQuery, Boolean isFavorite, Long userId);

    /**
     * Updates the status of a place and ensures the user with the given email
     * exists.
     *
     * @param dto Contains the place name, user email, and the new status.
     * @return The same UpdatePlaceStatusWithUserEmailDto.
     * @throws NotFoundException If the place or user is not found.
     */
    UpdatePlaceStatusWithUserEmailDto updatePlaceStatus(UpdatePlaceStatusWithUserEmailDto dto);
}