package greencity.mapping;

import greencity.dto.habit.HabitDto;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.todolistitem.ToDoListItemResponseDto;
import greencity.entity.CustomToDoListItem;
import greencity.entity.Habit;
import greencity.entity.HabitTranslation;
import greencity.entity.Language;
import greencity.entity.ToDoListItem;
import greencity.entity.localization.ToDoListItemTranslation;
import greencity.entity.localization.TagTranslation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static greencity.ModelUtils.getCustomToDoListItem;
import static greencity.ModelUtils.getHabitTranslation;
import static greencity.ModelUtils.getToDoListItem;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class HabitDtoMapperTest {

    @InjectMocks
    HabitDtoMapper habitDtoMapper;

    @Test
    void convert() {
        HabitTranslation habitTranslation = getHabitTranslation();
        Habit habit = habitTranslation.getHabit();
        Language language = habitTranslation.getLanguage();

        HabitDto habitDto = HabitDto.builder()
            .id(habit.getId())
            .image(habitTranslation.getHabit().getImage())
            .defaultDuration(habitTranslation.getHabit().getDefaultDuration())
            .complexity(1)
            .habitTranslation(HabitTranslationDto.builder()
                .description(habitTranslation.getDescription())
                .habitItem(habitTranslation.getHabitItem())
                .name(habitTranslation.getName())
                .languageCode(language.getCode())
                .build())
            .tags(habit.getTags().stream()
                .flatMap(tag -> tag.getTagTranslations().stream())
                .filter(tagTranslation -> tagTranslation.getLanguage().equals(language))
                .map(TagTranslation::getName).toList())
            .toDoListItems(new ArrayList<>())
            .customToDoListItems(new ArrayList<>())
            .build();

        HabitDto expected = habitDtoMapper.convert(habitTranslation);

        assertEquals(habitDto, expected);
    }

    @Test
    void convertWithToDoListAndCustomToDoList() {
        HabitTranslation habitTranslation = getHabitTranslation();
        Habit habit = habitTranslation.getHabit();
        ToDoListItem toDoListItem = getToDoListItem();
        habit.setToDoListItems(Set.of(toDoListItem));
        CustomToDoListItem customToDoListItem = getCustomToDoListItem();
        habit.setCustomToDoListItems(List.of(customToDoListItem));
        Language language = habitTranslation.getLanguage();

        HabitDto habitDto = HabitDto.builder()
            .id(habit.getId())
            .image(habitTranslation.getHabit().getImage())
            .defaultDuration(habitTranslation.getHabit().getDefaultDuration())
            .complexity(1)
            .habitTranslation(HabitTranslationDto.builder()
                .description(habitTranslation.getDescription())
                .habitItem(habitTranslation.getHabitItem())
                .name(habitTranslation.getName())
                .languageCode(language.getCode())
                .build())
            .tags(habit.getTags().stream()
                .flatMap(tag -> tag.getTagTranslations().stream())
                .filter(tagTranslation -> tagTranslation.getLanguage().equals(language))
                .map(TagTranslation::getName).toList())
            .toDoListItems(List.of(ToDoListItemResponseDto.builder()
                .id(toDoListItem.getId())
                .text(toDoListItem.getTranslations().stream()
                    .filter(toDoListItemTranslation -> toDoListItemTranslation
                        .getLanguage().equals(language))
                    .map(ToDoListItemTranslation::getContent)
                    .findFirst().orElse(null))
                .build()))
            .customToDoListItems(List.of(CustomToDoListItemResponseDto.builder()
                .id(customToDoListItem.getId())
                .text(customToDoListItem.getText())
                .build()))
            .build();

        HabitDto expected = habitDtoMapper.convert(habitTranslation);

        assertEquals(habitDto, expected);
    }
}