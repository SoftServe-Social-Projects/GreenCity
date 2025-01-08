package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.habit.HabitAssignDto;
import greencity.entity.HabitAssign;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HabitAssignDtoMapperTest {
    @InjectMocks
    private HabitAssignDtoMapper habitAssignDtoMapper;

    @Test
    void convertTest() {
        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        HabitAssignDto actual = habitAssignDtoMapper.convert(habitAssign);

        HabitAssignDto expected = HabitAssignDto.builder()
            .id(habitAssign.getId())
            .status(habitAssign.getStatus())
            .createDateTime(habitAssign.getCreateDate())
            .userId(habitAssign.getUser().getId())
            .habitStreak(habitAssign.getHabitStreak())
            .workingDays(habitAssign.getWorkingDays())
            .lastEnrollmentDate(habitAssign.getLastEnrollmentDate())
            .duration(habitAssign.getDuration())
            .habitStatusCalendarDtoList(actual.getHabitStatusCalendarDtoList())
            .userToDoListItems(actual.getUserToDoListItems())
            .build();

        assertEquals(expected, actual);
    }
}
