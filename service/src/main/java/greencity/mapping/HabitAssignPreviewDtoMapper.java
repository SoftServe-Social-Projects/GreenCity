package greencity.mapping;

import greencity.dto.habit.HabitAssignPreviewDto;
import greencity.dto.habit.HabitPreviewDto;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.HabitTranslation;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link HabitAssign} into
 * {@link HabitAssignPreviewDto}.
 */
@Component
public class HabitAssignPreviewDtoMapper extends AbstractConverter<HabitAssign, HabitAssignPreviewDto> {
    /**
     * Method convert {@link HabitAssign} to {@link HabitAssignPreviewDto}.
     *
     * @return {@link HabitAssignPreviewDto}
     */
    @Override
    protected HabitAssignPreviewDto convert(HabitAssign habitAssign) {
        Habit habit = habitAssign.getHabit();
        HabitTranslation habitTranslationUa = habit.getHabitTranslations().stream()
            .filter(translation -> translation.getLanguage().getCode().equalsIgnoreCase("ua"))
            .findFirst().orElse(null);
        HabitTranslation habitTranslation = habit.getHabitTranslations().stream()
            .filter(translation -> !translation.getLanguage().getCode().equalsIgnoreCase("en"))
            .findFirst().orElse(null);
        HabitPreviewDto habitPreviewDto = null;
        if (habitTranslation != null && habitTranslationUa != null) {
            HabitTranslationDto habitTranslationDto = HabitTranslationDto.builder()
                .name(habitTranslation.getName())
                .nameUa(habitTranslationUa.getName())
                .habitItem(habitTranslation.getHabitItem())
                .habitItemUa(habitTranslationUa.getHabitItem())
                .description(habitTranslation.getDescription())
                .descriptionUa(habitTranslationUa.getDescription())
                .build();
            habitPreviewDto = HabitPreviewDto.builder()
                .id(habit.getId())
                .image(habit.getImage())
                .habitTranslation(habitTranslationDto)
                .build();
        }
        return HabitAssignPreviewDto.builder()
            .id(habitAssign.getId())
            .status(habitAssign.getStatus())
            .userId(habitAssign.getUser().getId())
            .duration(habitAssign.getDuration())
            .workingDays(habitAssign.getWorkingDays())
            .habit(habitPreviewDto)
            .build();
    }
}
