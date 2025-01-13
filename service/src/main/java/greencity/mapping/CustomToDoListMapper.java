package greencity.mapping;

import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.entity.CustomToDoListItem;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomToDoListMapper
    extends AbstractConverter<CustomToDoListItemResponseDto, CustomToDoListItem> {
    @Override
    public CustomToDoListItem convert(CustomToDoListItemResponseDto customToDoListItemResponseDto) {
        return CustomToDoListItem.builder()
            .id(customToDoListItemResponseDto.getId())
            .text(customToDoListItemResponseDto.getText())
            .status(customToDoListItemResponseDto.getStatus())
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
        List<CustomToDoListItemResponseDto> dtoList) {
        return dtoList.stream().map(this::convert).collect(Collectors.toList());
    }
}
