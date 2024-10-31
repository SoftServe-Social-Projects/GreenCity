package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.shoppinglistitem.ShoppingListItemVO;
import greencity.dto.user.UserShoppingListItemVO;
import greencity.entity.ShoppingListItem;
import greencity.entity.UserShoppingListItem;
import greencity.enums.ToDoListItemStatus;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserShoppingListItemVOMapperTest {
    @InjectMocks
    private UserShoppingListItemVOMapper mapper;

    @Test
    void convert() {
        UserShoppingListItem userShoppingListItem = ModelUtils.getPredefinedUserShoppingListItem();
        userShoppingListItem.setDateCompleted(LocalDateTime.now());
        userShoppingListItem.setId(1L);
        userShoppingListItem.setShoppingListItem(ShoppingListItem.builder().id(13L).build());

        UserShoppingListItemVO expected = ModelUtils.getUserShoppingListItemVO();
        expected.setStatus(ToDoListItemStatus.ACTIVE);
        expected.setShoppingListItemVO(ShoppingListItemVO.builder().id(13L).build());
        expected.setDateCompleted(userShoppingListItem.getDateCompleted());

        assertEquals(expected, mapper.convert(userShoppingListItem));
    }
}
