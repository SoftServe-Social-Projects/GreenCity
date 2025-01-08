package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.achievementcategory.AchievementCategoryTranslationDto;
import greencity.entity.AchievementCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AchievementCategoryTranslationDtoMapperTest {
    @InjectMocks
    private AchievementCategoryTranslationDtoMapper mapper;

    @Test
    void convert() {
        AchievementCategory achievementCategory = ModelUtils.getAchievementCategory();
        AchievementCategoryTranslationDto expected = AchievementCategoryTranslationDto.builder()
            .id(achievementCategory.getId())
            .title(achievementCategory.getTitle())
            .titleEn(achievementCategory.getTitleEn())
            .build();
        assertEquals(expected, mapper.convert(achievementCategory));
    }
}