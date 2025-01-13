package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.todolistitem.ToDoListItemResponseWithTranslationDto;
import greencity.dto.todolistitem.ToDoListItemTranslationDTO;
import greencity.entity.ToDoListItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ToDoListItemResponseWithTranslationDtoMapperTest {
    @InjectMocks
    ToDoListItemResponseDtoMapper toDoListItemResponseDtoMapper;

    @Test
    void convert() {
        ToDoListItem toDoListItem = ToDoListItem.builder()
            .id(1L)
            .translations(ModelUtils.getToDoListItemTranslations())
            .build();
        ToDoListItemResponseWithTranslationDto expected = ToDoListItemResponseWithTranslationDto.builder()
            .id(toDoListItem.getId())
            .translations(toDoListItem.getTranslations().stream().map(
                shoppingListItemTranslation -> ToDoListItemTranslationDTO.builder()
                    .id(shoppingListItemTranslation.getId())
                    .content(shoppingListItemTranslation.getContent())
                    .build())
                .collect(Collectors.toList()))
            .build();
        ToDoListItemResponseWithTranslationDto actual = toDoListItemResponseDtoMapper.convert(toDoListItem);
        assertEquals(expected, actual);
    }
}
