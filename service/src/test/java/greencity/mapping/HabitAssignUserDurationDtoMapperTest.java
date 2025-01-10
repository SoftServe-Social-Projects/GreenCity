package greencity.mapping;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static greencity.ModelUtils.getHabitAssignUserDurationDto;
import static greencity.ModelUtils.getHabitAssignWithUserToDoListItem;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class HabitAssignUserDurationDtoMapperTest {
    @InjectMocks
    private HabitAssignUserDurationDtoMapper mapper;

    @Test
    void convertHabitAssignToHabitAssignUserDurationDtoTest() {
        var habitAssign = getHabitAssignWithUserToDoListItem();
        var dto = getHabitAssignUserDurationDto();
        assertEquals(dto, mapper.convert(habitAssign));
    }
}
