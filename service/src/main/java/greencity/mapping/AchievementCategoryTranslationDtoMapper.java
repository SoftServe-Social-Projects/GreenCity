package greencity.mapping;

import greencity.dto.achievementcategory.AchievementCategoryTranslationDto;
import greencity.entity.AchievementCategory;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class AchievementCategoryTranslationDtoMapper
    extends AbstractConverter<AchievementCategory, AchievementCategoryTranslationDto> {
    @Override
    protected AchievementCategoryTranslationDto convert(AchievementCategory achievementCategory) {
        return AchievementCategoryTranslationDto.builder()
            .id(achievementCategory.getId())
            .title(achievementCategory.getTitle())
            .titleEn(achievementCategory.getTitleEn())
            .build();
    }
}
