package greencity.mapping;

import greencity.dto.achievementcategory.AchievementCategoryVO;
import greencity.entity.AchievementCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AchievementCategoryVOMapperTest {
    @InjectMocks
    AchievementCategoryVOMapper achievementCategoryVOMapper;

    @Test
    void convert() {
        AchievementCategory achievementCategory =
            new AchievementCategory(1L, "Name", "Назва", "Title", Collections.emptyList());
        AchievementCategoryVO achievementCategoryVO = AchievementCategoryVO.builder()
            .id(achievementCategory.getId())
            .name(achievementCategory.getName())
            .build();
        assertEquals(achievementCategoryVO, achievementCategoryVOMapper.convert(achievementCategory));
    }
}
