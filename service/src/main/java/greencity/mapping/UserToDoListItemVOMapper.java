package greencity.mapping;

import greencity.dto.todolistitem.ToDoListItemVO;
import greencity.dto.habit.HabitAssignVO;
import greencity.dto.user.UserToDoListItemVO;
import greencity.entity.UserToDoListItem;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class UserToDoListItemVOMapper extends AbstractConverter<UserToDoListItem, UserToDoListItemVO> {
    @Override
    protected UserToDoListItemVO convert(UserToDoListItem userToDoListItem) {
        return UserToDoListItemVO.builder()
            .id(userToDoListItem.getId())
            .toDoListItemVO(ToDoListItemVO.builder()
                .id(userToDoListItem.getToDoListItem().getId())
                .build())
            .status(userToDoListItem.getStatus())
            .habitAssign(HabitAssignVO.builder()
                .id(userToDoListItem.getHabitAssign().getId()).build())
            .dateCompleted(userToDoListItem.getDateCompleted())
            .build();
    }
}
