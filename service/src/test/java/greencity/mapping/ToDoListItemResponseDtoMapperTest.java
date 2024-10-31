package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.shoppinglistitem.ShoppingListItemResponseDto;
import greencity.dto.shoppinglistitem.ShoppingListItemTranslationDTO;
import greencity.entity.ShoppingListItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ShoppingListItemResponseDtoMapperTest {
    @InjectMocks
    ShoppingListItemResponseDtoMapper shoppingListItemResponseDtoMapper;

    @Test
    void convert() {
        ShoppingListItem shoppingListItem = ShoppingListItem.builder()
            .id(1L)
            .translations(ModelUtils.getShoppingListItemTranslations())
            .build();
        ShoppingListItemResponseDto expected = ShoppingListItemResponseDto.builder()
            .id(shoppingListItem.getId())
            .translations(shoppingListItem.getTranslations().stream().map(
                shoppingListItemTranslation -> ShoppingListItemTranslationDTO.builder()
                    .id(shoppingListItemTranslation.getId())
                    .content(shoppingListItemTranslation.getContent())
                    .build())
                .collect(Collectors.toList()))
            .build();
        ShoppingListItemResponseDto actual = shoppingListItemResponseDtoMapper.convert(shoppingListItem);
        assertEquals(expected, actual);
    }
}
