package greencity.mapping;

import static greencity.ModelUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import greencity.dto.habit.HabitAssignDto;
import greencity.entity.HabitAssign;
import greencity.enums.ToDoListItemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class HabitAssignMapperTest {

    @InjectMocks
    private HabitAssignMapper habitAssignMapper;

    @Test
    void testConvert() {
        HabitAssignDto habitAssignDto = getFullHabitAssignDto();
        HabitAssign convert = habitAssignMapper.convert(habitAssignDto);
        HabitAssign habitAssignForMapper = getHabitAssignForMapper();
        habitAssignForMapper.getUserToDoListItems().getFirst().setStatus(ToDoListItemStatus.INPROGRESS);
        assertEquals(habitAssignForMapper, convert);

    }

}
