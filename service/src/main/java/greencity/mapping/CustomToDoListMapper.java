package greencity.mapping;

import greencity.dto.todolistitem.CustomToDoListItemRequestDto;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.entity.CustomToDoListItem;
import greencity.enums.ToDoListItemStatus;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomToDoListMapper
    extends AbstractConverter<CustomToDoListItemRequestDto, CustomToDoListItem> {
    @Override
    public CustomToDoListItem convert(CustomToDoListItemRequestDto customToDoListItemRequestDto) {
        return CustomToDoListItem.builder()
            .id(customToDoListItemRequestDto.getId())
            .text(customToDoListItemRequestDto.getText())
            .status(ToDoListItemStatus.valueOf(customToDoListItemRequestDto.getStatus()))
            .build();
    }

    /**
     * Method that build {@link List} of {@link CustomToDoListItem} from
     * {@link List} of {@link CustomToDoListItemResponseDto}.
     *
     * @param dtoList {@link List} of {@link CustomToDoListItemResponseDto}
     * @return {@link List} of {@link CustomToDoListItem}
     * @author Lilia Mokhnatska
     */
    public List<CustomToDoListItem> mapAllToList(
        List<CustomToDoListItemRequestDto> dtoList) {
        return dtoList.stream().map(this::convert).collect(Collectors.toList());
    }
}
