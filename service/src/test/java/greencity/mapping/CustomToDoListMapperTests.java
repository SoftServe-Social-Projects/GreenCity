package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.entity.CustomToDoListItem;
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
        CustomToDoListItemResponseDto customToDoListItemResponseDto =
            ModelUtils.getCustomToDoListItemResponseDto();

        CustomToDoListItem expected = CustomToDoListItem.builder()
            .id(customToDoListItemResponseDto.getId())
            .status(customToDoListItemResponseDto.getStatus())
            .text(customToDoListItemResponseDto.getText())
            .build();
        assertEquals(expected, customToDoListMapper.convert(customToDoListItemResponseDto));
    }

    @Test
    void mapAllToListTest() {
        CustomToDoListItemResponseDto customToDoListItemResponseDto =
            ModelUtils.getCustomToDoListItemResponseDto();
        List<CustomToDoListItemResponseDto> customToDoListItemResponseDtoList =
            List.of(ModelUtils.getCustomToDoListItemResponseDto());

        CustomToDoListItem expected = CustomToDoListItem.builder()
            .id(customToDoListItemResponseDto.getId())
            .status(customToDoListItemResponseDto.getStatus())
            .text(customToDoListItemResponseDto.getText())
            .build();
        List<CustomToDoListItem> expectedList = List.of(expected);
        assertEquals(expectedList, customToDoListMapper.mapAllToList(customToDoListItemResponseDtoList));
    }
}
