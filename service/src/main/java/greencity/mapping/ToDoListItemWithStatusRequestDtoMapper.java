package greencity.mapping;

import greencity.dto.todolistitem.ToDoListItemWithStatusRequestDto;
import greencity.entity.ToDoListItem;
import greencity.entity.UserToDoListItem;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class ToDoListItemWithStatusRequestDtoMapper
    extends AbstractConverter<ToDoListItemWithStatusRequestDto, UserToDoListItem> {
    /**
     * Method for converting {@link ToDoListItemWithStatusRequestDto} into
     * {@link UserToDoListItem}.
     *
     * @param itemDto object to convert.
     * @return converted object.
     */
    @Override
    protected UserToDoListItem convert(ToDoListItemWithStatusRequestDto itemDto) {
        return UserToDoListItem.builder()
            .toDoListItem(ToDoListItem.builder().id(itemDto.getId()).build())
            .status(itemDto.getStatus())
            .build();
    }
}
