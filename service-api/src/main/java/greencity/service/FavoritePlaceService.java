package greencity.service;

import greencity.dto.favoriteplace.FavoritePlaceDto;
import greencity.dto.favoriteplace.FavoritePlaceVO;
import greencity.dto.place.PlaceByBoundsDto;
import greencity.dto.place.PlaceInfoDto;
import greencity.dto.place.PlaceVO;
import greencity.dto.user.UserVO;
import java.util.List;

public interface FavoritePlaceService {
    /**
     * Save {@link PlaceVO} as {@link FavoritePlaceVO} for user.
     *
     * @param favoritePlaceDto - dto with {@link FavoritePlaceVO} name,
     *                         {@link PlaceVO} id and {@link UserVO} email
     * @param userEmail        - User email
     * @return {@link FavoritePlaceDto} instance
     * @author Zakhar Skaletskyi
     */
    FavoritePlaceDto save(FavoritePlaceDto favoritePlaceDto, String userEmail);

    /**
     * Update favorite place name for user.
     *
     * @param favoritePlaceDto - dto with {@link FavoritePlaceVO} name,
     *                         {@link PlaceVO} id and {@link UserVO} email
     * @param userEmail        - {@link UserVO} email
     * @return {@link FavoritePlaceDto} instance
     * @author Zakhar Skaletskyi
     */

    FavoritePlaceDto update(FavoritePlaceDto favoritePlaceDto, String userEmail);

    /**
     * Find all {@link FavoritePlaceVO} by {@link UserVO} email.
     *
     * @param email - {@link UserVO} email
     * @return list of {@link FavoritePlaceDto}
     * @author Zakhar Skaletskyi
     */
    List<PlaceByBoundsDto> findAllByUserEmail(String email);

    /**
     * Delete {@link FavoritePlaceVO} by {@link UserVO} email and {@link PlaceVO} id
     * .
     *
     * @param placeId   - {@link PlaceVO} id
     * @param userEmail - {@link UserVO} email
     * @return - id of deleted {@link FavoritePlaceVO}
     * @author Zakhar Skaletskyi
     */
    Long deleteByUserEmailAndPlaceId(Long placeId, String userEmail);

    /**
     * FInd {@link FavoritePlaceVO} by id.
     *
     * @param placeId - {@link PlaceVO} id
     * @return {@link FavoritePlaceVO} entity
     * @author Zakhar Skaletskyi
     */
    FavoritePlaceVO findByPlaceId(Long placeId);

    /**
     * Method for getting {@link FavoritePlaceVO} as {@link PlaceVO} information.
     *
     * @param favoritePlaceId - {@link FavoritePlaceVO} id
     * @return info about place with name from {@link FavoritePlaceVO}
     * @author Zakhar Skaletskyi
     */
    PlaceInfoDto getInfoFavoritePlace(Long favoritePlaceId);

    /**
     * Get {@link FavoritePlaceVO} coordinates, id and name.
     *
     * @param id    {@link FavoritePlaceVO}
     * @param email - {@link UserVO} email
     * @return {@link PlaceByBoundsDto} with name from favorite place
     * @author Zakhar Skaletskyi
     */
    PlaceByBoundsDto getFavoritePlaceWithLocation(Long id, String email);
}
