package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.habit.HabitDto;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.dto.shoppinglistitem.ShoppingListItemDto;
import greencity.entity.Habit;
import greencity.entity.HabitTranslation;
import greencity.entity.Language;
import greencity.entity.localization.ShoppingListItemTranslation;
import greencity.entity.localization.TagTranslation;

import java.util.ArrayList;
import java.util.stream.Collectors;

import greencity.enums.ShoppingListItemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class HabitDtoMapperTest {

    @InjectMocks
    HabitDtoMapper habitDtoMapper;

    @Test
    void convert() {
        HabitTranslation habitTranslation = ModelUtils.getHabitTranslation();
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
                .map(TagTranslation::getName).collect(Collectors.toList()))
            .shoppingListItems(habit.getShoppingListItems() != null ? habit.getShoppingListItems().stream()
                .map(shoppingListItem -> ShoppingListItemDto.builder()
                    .id(shoppingListItem.getId())
                    .status(ShoppingListItemStatus.ACTIVE.toString())
                    .text(shoppingListItem.getTranslations().stream()
                        .filter(shoppingListItemTranslation -> shoppingListItemTranslation
                            .getLanguage().equals(language))
                        .map(ShoppingListItemTranslation::getContent)
                        .findFirst().orElse(null))
                    .build())
                .collect(Collectors.toList()) : new ArrayList<>())
            .build();

        HabitDto expected = habitDtoMapper.convert(habitTranslation);

        assertEquals(habitDto, expected);
    }
}