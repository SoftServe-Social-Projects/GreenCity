package greencity.mapping;

import greencity.dto.habit.HabitManagementDto;
import greencity.dto.habittranslation.HabitTranslationManagementDto;
import greencity.entity.Habit;
import java.util.stream.Collectors;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link Habit} into
 * {@link HabitManagementDto}.
 */
@Component
public class HabitManagementDtoMapper extends AbstractConverter<Habit, HabitManagementDto> {
    /**
     * Method convert {@link Habit} to {@link HabitManagementDto}.
     *
     * @return {@link HabitManagementDto}
     */
    @Override
    protected HabitManagementDto convert(Habit habit) {
        return HabitManagementDto.builder()
            .id(habit.getId())
            .image(habit.getImage())
            .complexity(habit.getComplexity())
            .defaultDuration(habit.getDefaultDuration())
            .isCustomHabit(habit.getIsCustomHabit())
            .isDeleted(habit.getIsDeleted())
            .habitTranslations(habit.getHabitTranslations()
                .stream().map(habitTranslation -> HabitTranslationManagementDto.builder()
                    .id(habitTranslation.getId())
                    .description(habitTranslation.getDescription())
                    .habitItem(habitTranslation.getHabitItem())
                    .name(habitTranslation.getName())
                    .languageCode(habitTranslation.getLanguage().getCode())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
