package greencity.mapping;

import greencity.ModelUtils;
import greencity.constant.AppConstant;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.entity.HabitTranslation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class HabitTranslationMapperTests {
    @InjectMocks
    private HabitTranslationMapper habitTranslationMapper;

    @Test
    void convertTest() {
        HabitTranslationDto habitTranslationDto = ModelUtils.getHabitTranslationDto();

        HabitTranslation expected = HabitTranslation.builder()
            .description(habitTranslationDto.getDescription())
            .habitItem(habitTranslationDto.getHabitItem())
            .name(habitTranslationDto.getName())
            .build();
        assertEquals(expected, habitTranslationMapper.convert(habitTranslationDto));
    }

    @Test
    void convertUaWithValidHabitTranslationDtoSucceedsTest() {
        HabitTranslationDto habitTranslationDto = ModelUtils.getHabitTranslationDtoEnAndUa();

        HabitTranslation expected = HabitTranslation.builder()
            .description(habitTranslationDto.getDescriptionUa())
            .habitItem(habitTranslationDto.getHabitItemUa())
            .name(habitTranslationDto.getNameUa())
            .build();
        assertEquals(expected, habitTranslationMapper.convertUa(habitTranslationDto));
    }

    @Test
    void mapAllToListTest() {
        HabitTranslationDto habitTranslationDto = ModelUtils.getHabitTranslationDto();
        List<HabitTranslationDto> habitTranslationDtoList = List.of(ModelUtils.getHabitTranslationDto());

        HabitTranslation expected = HabitTranslation.builder()
            .description(habitTranslationDto.getDescription())
            .habitItem(habitTranslationDto.getHabitItem())
            .name(habitTranslationDto.getName())
            .build();
        List<HabitTranslation> expectedList = List.of(expected);
        assertEquals(expectedList, habitTranslationMapper.mapAllToList(habitTranslationDtoList));
    }

    @Test
    void mapAllToListWithEnLanguageReturnsListTest() {
        HabitTranslationDto habitTranslationDto = ModelUtils.getHabitTranslationDtoEnAndUa();
        List<HabitTranslationDto> habitTranslationDtoList = List.of(habitTranslationDto);

        HabitTranslation expected = HabitTranslation.builder()
            .description(habitTranslationDto.getDescription())
            .habitItem(habitTranslationDto.getHabitItem())
            .name(habitTranslationDto.getName())
            .build();
        List<HabitTranslation> expectedList = List.of(expected);

        assertEquals(expectedList,
            habitTranslationMapper.mapAllToList(habitTranslationDtoList, AppConstant.DEFAULT_LANGUAGE_CODE));
    }

    @Test
    void mapAllToListWithUaLanguageReturnsListTest() {
        HabitTranslationDto habitTranslationDto = ModelUtils.getHabitTranslationDtoEnAndUa();
        List<HabitTranslationDto> habitTranslationDtoList = List.of(habitTranslationDto);

        HabitTranslation expected = HabitTranslation.builder()
            .description(habitTranslationDto.getDescriptionUa())
            .habitItem(habitTranslationDto.getHabitItemUa())
            .name(habitTranslationDto.getNameUa())
            .build();
        List<HabitTranslation> expectedList = List.of(expected);

        assertEquals(expectedList,
            habitTranslationMapper.mapAllToList(habitTranslationDtoList, AppConstant.LANGUAGE_CODE_UA));
    }

    @Test
    void mapAllToListWithUaCodeButEmptyUaFieldsReturnListTest() {
        HabitTranslationDto habitTranslationDto = ModelUtils.getHabitTranslationDto();
        List<HabitTranslationDto> habitTranslationDtoList = List.of(habitTranslationDto);

        HabitTranslation expected = HabitTranslation.builder()
            .description(habitTranslationDto.getDescription())
            .habitItem(habitTranslationDto.getHabitItem())
            .name(habitTranslationDto.getName())
            .build();
        List<HabitTranslation> expectedList = List.of(expected);

        assertEquals(expectedList,
            habitTranslationMapper.mapAllToList(habitTranslationDtoList, AppConstant.LANGUAGE_CODE_UA));
    }
}
