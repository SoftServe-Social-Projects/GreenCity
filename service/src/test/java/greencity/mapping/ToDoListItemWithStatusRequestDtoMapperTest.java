package greencity.mapping;

import greencity.dto.todolistitem.ToDoListItemWithStatusRequestDto;
import greencity.entity.ToDoListItem;
import greencity.entity.UserToDoListItem;
import greencity.enums.ToDoListItemStatus;
import greencity.enums.UserToDoListItemStatus;
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
        ToDoListItemStatus status = ToDoListItemStatus.ACTIVE;
        itemDto.setId(id);
        itemDto.setStatus(status);

        UserToDoListItem expected = UserToDoListItem.builder()
            .targetId(id)
            .isCustomItem(false)
            .status(UserToDoListItemStatus.INPROGRESS)
            .build();

        UserToDoListItem actual = toDoListItemWithStatusRequestDtoMapper.convert(itemDto);
        assertEquals(expected, actual);
    }
}