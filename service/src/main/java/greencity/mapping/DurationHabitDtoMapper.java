package greencity.mapping;

import greencity.dto.habit.DurationHabitDto;
import greencity.entity.HabitAssign;
import greencity.enums.HabitAssignStatus;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class DurationHabitDtoMapper extends AbstractConverter<HabitAssign, DurationHabitDto> {
    @Override
    protected DurationHabitDto convert(HabitAssign habitAssign) {
        return DurationHabitDto.builder()
            .duration(habitAssign.getStatus() == HabitAssignStatus.INPROGRESS ? habitAssign.getWorkingDays()
                : ChronoUnit.DAYS.between(habitAssign.getCreateDate().toLocalDate(), LocalDate.now()))
            .description(habitAssign.getHabit().getHabitTranslations().getFirst().getName())
            .build();
    }
}
