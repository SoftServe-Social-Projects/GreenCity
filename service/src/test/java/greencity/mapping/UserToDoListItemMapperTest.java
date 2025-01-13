package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.todolistitem.ToDoListItemVO;
import greencity.dto.habit.HabitAssignVO;
import greencity.dto.user.UserToDoListItemVO;
import greencity.entity.UserToDoListItem;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserToDoListItemMapperTest {
    @InjectMocks
    private UserToDoListItemMapper mapper;

    @Test
    void convert() {
        UserToDoListItem expected = ModelUtils.getUserToDoListItem();
        UserToDoListItemVO toConvert = UserToDoListItemVO.builder()
            .id(expected.getId())
            .status(expected.getStatus())
            .habitAssign(HabitAssignVO.builder()
                .id(expected.getHabitAssign().getId())
                .status(expected.getHabitAssign().getStatus())
                .habitStreak(expected.getHabitAssign().getHabitStreak())
                .duration(expected.getHabitAssign().getDuration())
                .lastEnrollmentDate(expected.getHabitAssign().getLastEnrollmentDate())
                .workingDays(expected.getHabitAssign().getWorkingDays())
                .build())
            .toDoListItemVO(ToDoListItemVO.builder()
                .id(expected.getToDoListItem().getId())
                .build())
            .dateCompleted(expected.getDateCompleted())
            .build();

        assertEquals(expected, mapper.convert(toConvert));
    }
}
