package greencity.mapping;

import greencity.dto.habit.HabitDto;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.dto.todolistitem.ToDoListItemDto;
import greencity.entity.HabitTranslation;
import greencity.entity.localization.ToDoListItemTranslation;
import greencity.entity.localization.TagTranslation;
import java.util.ArrayList;
import java.util.stream.Collectors;
import greencity.enums.ToDoListItemStatus;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link HabitTranslation} into
 * {@link HabitDto}.
 */
@Component
public class HabitDtoMapper extends AbstractConverter<HabitTranslation, HabitDto> {
    /**
     * Method convert {@link HabitTranslation} to {@link HabitDto}.
     *
     * @return {@link HabitDto}
     */
    @Override
    protected HabitDto convert(HabitTranslation habitTranslation) {
        var language = habitTranslation.getLanguage();
        var habit = habitTranslation.getHabit();
        return HabitDto.builder()
            .id(habit.getId())
            .image(habitTranslation.getHabit().getImage())
            .defaultDuration(habitTranslation.getHabit().getDefaultDuration())
            .complexity(habit.getComplexity())
            .habitTranslation(HabitTranslationDto.builder()
                .description(habitTranslation.getDescription())
                .habitItem(habitTranslation.getHabitItem())
                .name(habitTranslation.getName())
                .languageCode(language.getCode())
                .build())
            .tags(habit.getTags().stream()
                .flatMap(tag -> tag.getTagTranslations().stream())
                .filter(tagTranslation -> tagTranslation.getLanguage().equals(language))
                .map(TagTranslation::getName).collect(Collectors.toList()))
            .toDoListItems(habit.getToDoListItems() != null ? habit.getToDoListItems().stream()
                .map(shoppingListItem -> ToDoListItemDto.builder()
                    .id(shoppingListItem.getId())
                    .status(ToDoListItemStatus.ACTIVE.toString())
                    .text(shoppingListItem.getTranslations().stream()
                        .filter(shoppingListItemTranslation -> shoppingListItemTranslation
                            .getLanguage().equals(language))
                        .map(ToDoListItemTranslation::getContent)
                        .findFirst().orElse(null))
                    .build())
                .collect(Collectors.toList()) : new ArrayList<>())
            .build();
    }
}
