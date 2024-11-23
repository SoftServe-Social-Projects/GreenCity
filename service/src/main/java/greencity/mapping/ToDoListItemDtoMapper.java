package greencity.mapping;

import greencity.dto.todolistitem.ToDoListItemResponseWithStatusDto;
import greencity.entity.localization.ToDoListItemTranslation;
import greencity.enums.ToDoListItemStatus;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link ToDoListItemTranslation}
 * into {@link ToDoListItemResponseWithStatusDto}.
 */
@Component
public class ToDoListItemDtoMapper extends AbstractConverter<ToDoListItemTranslation, ToDoListItemResponseWithStatusDto> {
    /**
     * Method for converting {@link ToDoListItemTranslation} into
     * {@link ToDoListItemResponseWithStatusDto}.
     *
     * @param toDoListItemTranslation object to convert.
     * @return converted object.
     */
    @Override
    protected ToDoListItemResponseWithStatusDto convert(ToDoListItemTranslation toDoListItemTranslation) {
        return new ToDoListItemResponseWithStatusDto(toDoListItemTranslation.getToDoListItem().getId(),
            toDoListItemTranslation.getContent(), ToDoListItemStatus.ACTIVE.toString());
    }
}
