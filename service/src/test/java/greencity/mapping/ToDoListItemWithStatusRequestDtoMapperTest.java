package greencity.mapping;

import greencity.dto.todolistitem.ToDoListItemWithStatusRequestDto;
import greencity.entity.ToDoListItem;
import greencity.entity.UserToDoListItem;
import greencity.enums.ToDoListItemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ToDoListItemWithStatusRequestDtoMapperTest {
    @InjectMocks
    ToDoListItemWithStatusRequestDtoMapper toDoListItemWithStatusRequestDtoMapper;

    @Test
    void convert() {
        ToDoListItemWithStatusRequestDto itemDto = new ToDoListItemWithStatusRequestDto();
        Long id = 1L;
        ToDoListItemStatus status = ToDoListItemStatus.DONE;
        itemDto.setId(id);
        itemDto.setStatus(status);

        UserToDoListItem expected = UserToDoListItem.builder()
            .toDoListItem(ToDoListItem.builder().id(id).build())
            .status(status)
            .build();

        UserToDoListItem actual = toDoListItemWithStatusRequestDtoMapper.convert(itemDto);
        assertEquals(expected, actual);
    }
}