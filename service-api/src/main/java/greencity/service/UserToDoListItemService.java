package greencity.service;

import greencity.dto.user.UserToDoListItemRequestDto;
import greencity.dto.user.UserToDoListItemRequestWithStatusDto;
import greencity.dto.user.UserToDoListItemResponseDto;
import java.util.List;

/**
 * Provides the interface to manage {@code UserToDoListItem} entity.
 */
public interface UserToDoListItemService {
    List<UserToDoListItemResponseDto> findAllForHabitAssign(Long habitAssignId, Long userId, String language);

    List<UserToDoListItemResponseDto> saveUserToDoListItems(Long habitAssignId,
        List<UserToDoListItemRequestDto> userToDoListItems, Long userId, String language);

    void deleteUserToDoListItems(Long habitAssignId, List<Long> itemIds, Long userId);

    List<UserToDoListItemResponseDto> changeStatusesUserToDoListItems(Long habitAssignId,
        List<UserToDoListItemRequestWithStatusDto> userToDoListItems, Long userId, String language);
}
