package greencity.mapping;

import greencity.dto.todolistitem.ToDoListItemRequestDto;
import greencity.entity.UserToDoListItem;
import greencity.enums.UserToDoListItemStatus;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class ToDoListItemRequestDtoMapper
    extends AbstractConverter<ToDoListItemRequestDto, UserToDoListItem> {
    /**
     * Method for converting {@link ToDoListItemRequestDto} into
     * {@link UserToDoListItem}.
     *
     * @param toDoListItemRequestDto object to convert.
     * @return converted object.
     */
    @Override
    protected UserToDoListItem convert(ToDoListItemRequestDto toDoListItemRequestDto) {
        return UserToDoListItem.builder()
            .targetId(toDoListItemRequestDto.getId())
            .isCustomItem(false)
            .status(UserToDoListItemStatus.INPROGRESS)
            .build();
    }
}
