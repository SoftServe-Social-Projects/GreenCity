package greencity.mapping;

import greencity.dto.habit.ShortHabitDto;
import greencity.entity.Habit;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class ShortHabitDtoMapper extends AbstractConverter<Habit, ShortHabitDto> {
    @Override
    protected ShortHabitDto convert(Habit habit) {
        return ShortHabitDto.builder()
            .id(habit.getId())
            .description(habit.getHabitTranslations().getFirst().getName())
            .build();
    }
}