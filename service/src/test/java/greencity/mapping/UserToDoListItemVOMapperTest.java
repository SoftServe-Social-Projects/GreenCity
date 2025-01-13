package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.user.UserToDoListItemVO;
import greencity.entity.UserToDoListItem;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import greencity.enums.UserToDoListItemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserToDoListItemVOMapperTest {
    @InjectMocks
    private UserToDoListItemVOMapper mapper;

    @Test
    void convert() {
        UserToDoListItem userToDoListItem = ModelUtils.getPredefinedUserToDoListItem();
        userToDoListItem.setDateCompleted(LocalDateTime.now());
        userToDoListItem.setId(1L);
        userToDoListItem.setTargetId(13L);
        userToDoListItem.setIsCustomItem(false);

        UserToDoListItemVO expected = ModelUtils.getUserToDoListItemVO();
        expected.setStatus(UserToDoListItemStatus.INPROGRESS);
        expected.setTargetId(13L);
        expected.setIsCustomItem(false);
        expected.setDateCompleted(userToDoListItem.getDateCompleted());

        assertEquals(expected, mapper.convert(userToDoListItem));
    }
}
