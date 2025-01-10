package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.user.UserToDoListItemResponseDto;
import greencity.entity.UserToDoListItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class UserToDoListItemResponseDtoMapperTest {
    @InjectMocks
    private UserToDoListItemResponseDtoMapper userToDoListItemResponseDtoMapper;

    @Test
    void convertTest() {
        UserToDoListItem userToDoListItem = ModelUtils.getCustomUserToDoListItem();

        UserToDoListItemResponseDto expected = UserToDoListItemResponseDto.builder()
            .id(userToDoListItem.getId())
            .status(userToDoListItem.getStatus())
            .build();

        assertEquals(expected, userToDoListItemResponseDtoMapper.convert(userToDoListItem));
    }
}
