package greencity.mapping;

import greencity.dto.habit.ShortHabitDto;
import greencity.entity.Habit;
import greencity.entity.HabitTranslation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ShortHabitDtoMapperTest {

    private ShortHabitDtoMapper mapper;
    private final Long id = 1L;
    private final String habitTranslationName = "Test Habit";
    private final HabitTranslation habitTranslation = HabitTranslation.builder().name(habitTranslationName).build();

    private final Habit habit = Habit.builder()
        .id(id)
        .habitTranslations(Collections.singletonList(habitTranslation))
        .build();

    @BeforeEach
    void setUp() {
        mapper = new ShortHabitDtoMapper();
    }

    @Test
    void convert() {
        ShortHabitDto result = mapper.convert(habit);

        assertEquals(id, result.getId());
        assertEquals(habitTranslationName, result.getDescription());
    }
}