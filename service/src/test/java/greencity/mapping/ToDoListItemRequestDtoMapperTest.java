package greencity.mapping;

import greencity.entity.UserToDoListItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import greencity.dto.todolistitem.ToDoListItemRequestDto;
import greencity.enums.UserToDoListItemStatus;
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
            .targetId(1L)
            .isCustomItem(false)
            .status(UserToDoListItemStatus.INPROGRESS)
            .build();

        UserToDoListItem actual = toDoListItemRequestDtoMapper.convert(toDoListItemRequestDto);
        assertEquals(expected.getTargetId(), actual.getTargetId());
        assertFalse(actual.getIsCustomItem());
    }
}
