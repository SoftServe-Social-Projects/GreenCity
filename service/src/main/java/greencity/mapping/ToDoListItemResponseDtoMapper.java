package greencity.mapping;

import greencity.dto.todolistitem.ToDoListItemResponseDto;
import greencity.dto.todolistitem.ToDoListItemTranslationDTO;
import greencity.entity.ToDoListItem;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

/**
 * Class that used by {@link ModelMapper} to map {@link ToDoListItem} into
 * {@link ToDoListItemResponseDto}.
 */
@Component
public class ToDoListItemResponseDtoMapper
    extends AbstractConverter<ToDoListItem, ToDoListItemResponseDto> {
    @Override
    protected ToDoListItemResponseDto convert(ToDoListItem toDoListItem) {
        return ToDoListItemResponseDto.builder()
            .id(toDoListItem.getId())
            .translations(toDoListItem.getTranslations().stream().map(
                shoppingListItemTranslation -> ToDoListItemTranslationDTO.builder()
                    .id(shoppingListItemTranslation.getId())
                    .content(shoppingListItemTranslation.getContent())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
