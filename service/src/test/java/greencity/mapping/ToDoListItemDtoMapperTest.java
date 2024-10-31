package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.shoppinglistitem.ShoppingListItemDto;
import greencity.entity.localization.ShoppingListItemTranslation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class ShoppingListItemDtoMapperTest {
    @InjectMocks
    private ShoppingListItemDtoMapper shoppingListItemDtoMapper;

    @Test
    void convertTest() {
        ShoppingListItemTranslation shoppingListItemTranslation = ModelUtils.getShoppingListItemTranslation();

        ShoppingListItemDto expected = new ShoppingListItemDto(
            shoppingListItemTranslation.getShoppingListItem().getId(), shoppingListItemTranslation
                .getContent(),
            "ACTIVE");

        assertEquals(expected, shoppingListItemDtoMapper.convert(shoppingListItemTranslation));
    }
}
