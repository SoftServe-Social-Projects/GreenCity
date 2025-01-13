package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.todolistitem.CustomToDoListItemRequestDto;
import greencity.entity.CustomToDoListItem;
import greencity.enums.ToDoListItemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CustomToDoListMapperTests {
    @InjectMocks
    private CustomToDoListMapper customToDoListMapper;

    @Test
    void convertTest() {
        CustomToDoListItemRequestDto customToDoListItemRequestDto =
            ModelUtils.getCustomToDoListItemRequestDto();

        CustomToDoListItem expected = CustomToDoListItem.builder()
            .id(customToDoListItemRequestDto.getId())
            .status(ToDoListItemStatus.valueOf(customToDoListItemRequestDto.getStatus()))
            .text(customToDoListItemRequestDto.getText())
            .build();
        assertEquals(expected, customToDoListMapper.convert(customToDoListItemRequestDto));
    }

    @Test
    void mapAllToListTest() {
        CustomToDoListItemRequestDto customToDoListItemRequestDto =
            ModelUtils.getCustomToDoListItemRequestDto();
        List<CustomToDoListItemRequestDto> customToDoListItemRequestDtoList =
            List.of(ModelUtils.getCustomToDoListItemRequestDto());

        CustomToDoListItem expected = CustomToDoListItem.builder()
            .id(customToDoListItemRequestDto.getId())
            .status(ToDoListItemStatus.valueOf(customToDoListItemRequestDto.getStatus()))
            .text(customToDoListItemRequestDto.getText())
            .build();
        List<CustomToDoListItem> expectedList = List.of(expected);
        assertEquals(expectedList, customToDoListMapper.mapAllToList(customToDoListItemRequestDtoList));
    }
}
