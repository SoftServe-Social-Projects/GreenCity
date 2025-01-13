package greencity.mapping;

import greencity.dto.todolistitem.ToDoListItemDto;
import greencity.entity.localization.ToDoListItemTranslation;
import greencity.enums.ToDoListItemStatus;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link ToDoListItemTranslation}
 * into {@link ToDoListItemDto}.
 */
@Component
public class ToDoListItemDtoMapper extends AbstractConverter<ToDoListItemTranslation, ToDoListItemDto> {
    /**
     * Method for converting {@link ToDoListItemTranslation} into
     * {@link ToDoListItemDto}.
     *
     * @param toDoListItemTranslation object to convert.
     * @return converted object.
     */
    @Override
    protected ToDoListItemDto convert(ToDoListItemTranslation toDoListItemTranslation) {
        return new ToDoListItemDto(toDoListItemTranslation.getToDoListItem().getId(),
            toDoListItemTranslation.getContent(), ToDoListItemStatus.ACTIVE.toString());
    }
}
