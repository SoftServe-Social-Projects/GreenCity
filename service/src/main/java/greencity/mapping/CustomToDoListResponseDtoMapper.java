package greencity.mapping;

import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.entity.CustomToDoListItem;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomToDoListResponseDtoMapper
    extends AbstractConverter<CustomToDoListItem, CustomToDoListItemResponseDto> {
    @Override
    protected CustomToDoListItemResponseDto convert(CustomToDoListItem customToDoListItem) {
        return CustomToDoListItemResponseDto.builder()
            .id(customToDoListItem.getId())
            .text(customToDoListItem.getText())
            .status(customToDoListItem.getStatus())
            .build();
    }

    /**
     * Method that build {@link List} of {@link CustomToDoListItemResponseDto} from
     * {@link List} of {@link CustomToDoListItemResponseDto}.
     *
     * @param itemList {@link List} of {@link CustomToDoListItemResponseDto}
     * @return {@link List} of {@link CustomToDoListItemResponseDto}
     * @author Lilia Mokhnatska
     */
    public List<CustomToDoListItemResponseDto> mapAllToList(
        List<CustomToDoListItem> itemList) {
        return itemList.stream().map(this::convert).collect(Collectors.toList());
    }
}
