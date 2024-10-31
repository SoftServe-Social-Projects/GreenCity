package greencity.mapping;

import greencity.entity.ShoppingListItem;
import greencity.entity.UserShoppingListItem;
import static org.junit.jupiter.api.Assertions.assertEquals;

import greencity.dto.shoppinglistitem.ShoppingListItemRequestDto;
import greencity.enums.ToDoListItemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShoppingListItemRequestDtoMapperTest {
    @InjectMocks
    ShoppingListItemRequestDtoMapper shoppingListItemRequestDtoMapper;

    @Test
    void convert() {
        ShoppingListItemRequestDto shoppingListItemRequestDto = new ShoppingListItemRequestDto();
        shoppingListItemRequestDto.setId(1L);

        UserShoppingListItem expected = UserShoppingListItem.builder()
            .shoppingListItem(ShoppingListItem.builder().id(shoppingListItemRequestDto.getId()).build())
            .status(ToDoListItemStatus.ACTIVE)
            .build();

        UserShoppingListItem actual = shoppingListItemRequestDtoMapper.convert(shoppingListItemRequestDto);
        assertEquals(expected.getShoppingListItem().getId(), actual.getShoppingListItem().getId());
    }
}
