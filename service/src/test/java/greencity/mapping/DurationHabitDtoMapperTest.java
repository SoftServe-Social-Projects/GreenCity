package greencity.mapping;

import greencity.dto.habit.DurationHabitDto;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.HabitTranslation;
import greencity.enums.HabitAssignStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.ZonedDateTime;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class DurationHabitDtoMapperTest {
    private DurationHabitDtoMapper durationHabitDtoMapper;
    private final String name = "Test Habit";
    private final HabitTranslation habitTranslation = HabitTranslation.builder().name(name).build();

    private final Habit habit = Habit.builder().habitTranslations(Collections.singletonList(habitTranslation)).build();
    HabitAssign habitAssign = HabitAssign.builder()
        .habit(habit)
        .workingDays(5)
        .createDate(ZonedDateTime.now().minusDays(10))
        .build();

    @BeforeEach
    void setUp() {
        durationHabitDtoMapper = new DurationHabitDtoMapper();
    }

    @Test
    void convert_HabitAssignInProgress_ShouldCalculateWorkingDays() {
        habitAssign.setStatus(HabitAssignStatus.INPROGRESS);

        DurationHabitDto result = durationHabitDtoMapper.convert(habitAssign);

        assertEquals(5L, result.getDuration());
        assertEquals(name, result.getDescription());
    }

    @Test
    void convert_HabitAssignNotInProgress_ShouldCalculateDaysBetweenCreateDateAndNow() {
        habitAssign.setStatus(HabitAssignStatus.ACQUIRED);

        DurationHabitDto result = durationHabitDtoMapper.convert(habitAssign);

        assertEquals(10L, result.getDuration());
        assertEquals(name, result.getDescription());
    }
}