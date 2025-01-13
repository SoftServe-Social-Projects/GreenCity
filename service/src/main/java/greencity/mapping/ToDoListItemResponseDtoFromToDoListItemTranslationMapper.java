package greencity.mapping;

import greencity.dto.todolistitem.ToDoListItemResponseDto;
import greencity.dto.todolistitem.ToDoListItemResponseWithTranslationDto;
import greencity.entity.ToDoListItem;
import greencity.entity.localization.ToDoListItemTranslation;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link ToDoListItem} into
 * {@link ToDoListItemResponseWithTranslationDto}.
 */
@Component
public class ToDoListItemResponseDtoFromToDoListItemTranslationMapper
    extends AbstractConverter<ToDoListItemTranslation, ToDoListItemResponseDto> {
    @Override
    protected ToDoListItemResponseDto convert(ToDoListItemTranslation toDoListItemTranslation) {
        return ToDoListItemResponseDto.builder()
            .id(toDoListItemTranslation.getToDoListItem().getId())
            .text(toDoListItemTranslation.getContent())
            .build();
    }
}