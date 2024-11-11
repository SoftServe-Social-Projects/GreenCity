package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.todolistitem.ToDoListItemVO;
import greencity.dto.user.UserToDoListItemVO;
import greencity.entity.ToDoListItem;
import greencity.entity.UserToDoListItem;
import greencity.enums.ToDoListItemStatus;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        userToDoListItem.setToDoListItem(ToDoListItem.builder().id(13L).build());

        UserToDoListItemVO expected = ModelUtils.getUserToDoListItemVO();
        expected.setStatus(ToDoListItemStatus.ACTIVE);
        expected.setToDoListItemVO(ToDoListItemVO.builder().id(13L).build());
        expected.setDateCompleted(userToDoListItem.getDateCompleted());

        assertEquals(expected, mapper.convert(userToDoListItem));
    }
}
