package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.user.UserShoppingListItemResponseDto;
import greencity.entity.UserShoppingListItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class UserShoppingListItemResponseDtoMapperTest {
    @InjectMocks
    private UserShoppingListItemResponseDtoMapper userShoppingListItemResponseDtoMapper;

    @Test
    void convertTest() {
        UserShoppingListItem userShoppingListItem = ModelUtils.getCustomUserShoppingListItem();

        UserShoppingListItemResponseDto expected = UserShoppingListItemResponseDto.builder()
            .id(userShoppingListItem.getId())
            .status(userShoppingListItem.getStatus())
            .build();

        assertEquals(expected, userShoppingListItemResponseDtoMapper.convert(userShoppingListItem));
    }
}
