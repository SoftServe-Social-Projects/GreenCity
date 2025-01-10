package greencity.service;

import greencity.dto.user.UserFilterDtoRequest;
import greencity.dto.user.UserFilterDtoResponse;
import java.util.List;

public interface FilterService {
    /**
     * Method for save filters.
     *
     * @param userId user's id who created filter.
     * @param dto    filters dto.
     * @return UserFilterDtoResponse.
     */
    UserFilterDtoResponse save(Long userId, UserFilterDtoRequest dto);

    /**
     * Method for getting user's filters.
     *
     * @param userId user's id.
     * @return UserFilterDtoResponse.
     */
    List<UserFilterDtoResponse> getAllFilters(Long userId);

    /**
     * return filter by id.
     *
     * @param filterId filter's id.
     * @return UserFilterDtoResponse.
     */
    UserFilterDtoResponse getFilterById(Long filterId);

    /**
     * Method delete filter.
     *
     * @param filterId filter's id which we have to delete.
     */
    void deleteFilterById(Long filterId);
}
