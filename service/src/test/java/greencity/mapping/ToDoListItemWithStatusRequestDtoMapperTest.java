package greencity.mapping;

import greencity.dto.shoppinglistitem.ShoppingListItemWithStatusRequestDto;
import greencity.entity.ShoppingListItem;
import greencity.entity.UserShoppingListItem;
import greencity.enums.ToDoListItemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ShoppingListItemWithStatusRequestDtoMapperTest {
    @InjectMocks
    ShoppingListItemWithStatusRequestDtoMapper shoppingListItemWithStatusRequestDtoMapper;

    @Test
    void convert() {
        ShoppingListItemWithStatusRequestDto itemDto = new ShoppingListItemWithStatusRequestDto();
        Long id = 1L;
        ToDoListItemStatus status = ToDoListItemStatus.DONE;
        itemDto.setId(id);
        itemDto.setStatus(status);

        UserShoppingListItem expected = UserShoppingListItem.builder()
            .shoppingListItem(ShoppingListItem.builder().id(id).build())
            .status(status)
            .build();

        UserShoppingListItem actual = shoppingListItemWithStatusRequestDtoMapper.convert(itemDto);
        assertEquals(expected, actual);
    }
}