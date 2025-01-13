package greencity.mapping;

import greencity.dto.user.UserToDoListItemResponseDto;
import greencity.entity.UserToDoListItem;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class UserToDoListItemResponseDtoMapper
    extends AbstractConverter<UserToDoListItem, UserToDoListItemResponseDto> {
    /**
     * Method for converting {@link UserToDoListItem} into
     * {@link UserToDoListItemResponseDto}.
     *
     * @param userToDoListItem object to convert.
     * @return converted object.
     */
    @Override
    public UserToDoListItemResponseDto convert(UserToDoListItem userToDoListItem) {
        return UserToDoListItemResponseDto.builder()
            .id(userToDoListItem.getId())
            .status(userToDoListItem.getStatus())
            .targetId(userToDoListItem.getTargetId())
            .isCustomItem(userToDoListItem.getIsCustomItem())
            .build();
    }
}
