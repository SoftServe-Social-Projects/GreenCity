package greencity.mapping;

import greencity.entity.ToDoListItem;
import greencity.entity.UserToDoListItem;
import static org.junit.jupiter.api.Assertions.assertEquals;

import greencity.dto.todolistitem.ToDoListItemRequestDto;
import greencity.enums.ToDoListItemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ToDoListItemRequestDtoMapperTest {
    @InjectMocks
    ToDoListItemRequestDtoMapper toDoListItemRequestDtoMapper;

    @Test
    void convert() {
        ToDoListItemRequestDto toDoListItemRequestDto = new ToDoListItemRequestDto();
        toDoListItemRequestDto.setId(1L);

        UserToDoListItem expected = UserToDoListItem.builder()
            .toDoListItem(ToDoListItem.builder().id(toDoListItemRequestDto.getId()).build())
            .status(ToDoListItemStatus.ACTIVE)
            .build();

        UserToDoListItem actual = toDoListItemRequestDtoMapper.convert(toDoListItemRequestDto);
        assertEquals(expected.getToDoListItem().getId(), actual.getToDoListItem().getId());
    }
}
