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
class CustomToDoListResponseDtoMapperTest {

    @InjectMocks
    private CustomToDoListResponseDtoMapper customToDoListResponseDtoMapper;

    @Test
    void convertTest() {
        CustomToDoListItem customToDoListItem =
            ModelUtils.getCustomToDoListItem();

        CustomToDoListItemResponseDto expected = CustomToDoListItemResponseDto.builder()
            .id(customToDoListItem.getId())
            .text(customToDoListItem.getText())
            .build();
        assertEquals(expected, customToDoListResponseDtoMapper.convert(customToDoListItem));
    }

    @Test
    void mapAllToListTest() {
        CustomToDoListItem customToDoListItem =
            ModelUtils.getCustomToDoListItem();

        List<CustomToDoListItem> customToDoListItemList =
            List.of(customToDoListItem);

        CustomToDoListItemResponseDto expected = CustomToDoListItemResponseDto.builder()
            .id(customToDoListItem.getId())
            .text(customToDoListItem.getText())
            .build();
        List<CustomToDoListItemResponseDto> expectedList = List.of(expected);
        assertEquals(expectedList, customToDoListResponseDtoMapper.mapAllToList(customToDoListItemList));
    }
}
