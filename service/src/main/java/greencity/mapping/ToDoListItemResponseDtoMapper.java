package greencity.mapping;

import greencity.dto.todolistitem.ToDoListItemResponseWithTranslationDto;
import greencity.dto.todolistitem.ToDoListItemTranslationDTO;
import greencity.entity.ToDoListItem;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link ToDoListItem} into
 * {@link ToDoListItemResponseWithTranslationDto}.
 */
@Component
public class ToDoListItemResponseDtoMapper
    extends AbstractConverter<ToDoListItem, ToDoListItemResponseWithTranslationDto> {
    @Override
    protected ToDoListItemResponseWithTranslationDto convert(ToDoListItem toDoListItem) {
        return ToDoListItemResponseWithTranslationDto.builder()
            .id(toDoListItem.getId())
            .translations(toDoListItem.getTranslations().stream().map(
                shoppingListItemTranslation -> ToDoListItemTranslationDTO.builder()
                    .id(shoppingListItemTranslation.getId())
                    .content(shoppingListItemTranslation.getContent())
                    .build())
                .toList())
            .build();
    }
}
