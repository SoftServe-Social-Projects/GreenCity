package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.todolistitem.ToDoListItemResponseWithStatusDto;
import greencity.entity.localization.ToDoListItemTranslation;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class ToDoListItemResponseWithStatusDtoMapperTest {
    @InjectMocks
    private ToDoListItemDtoMapper toDoListItemDtoMapper;

    @Test
    void convertTest() {
        ToDoListItemTranslation toDoListItemTranslation = ModelUtils.getToDoListItemTranslation();

        ToDoListItemResponseWithStatusDto expected = new ToDoListItemResponseWithStatusDto(
            toDoListItemTranslation.getToDoListItem().getId(), toDoListItemTranslation
                .getContent(),
            "ACTIVE");

        assertEquals(expected, toDoListItemDtoMapper.convert(toDoListItemTranslation));
    }
}
