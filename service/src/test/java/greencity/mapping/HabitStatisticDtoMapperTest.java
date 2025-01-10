package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.habitstatistic.HabitStatisticDto;
import greencity.entity.HabitAssign;
import greencity.entity.HabitStatistic;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class HabitStatisticDtoMapperTest {

    @InjectMocks
    HabitStatisticDtoMapper habitStatisticDtoMapper;

    @Test
    void convert() {
        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        HabitStatistic habitStatistic = ModelUtils.getHabitStatistic();
        habitStatistic.setHabitAssign(habitAssign);
        HabitStatisticDto habitStatisticDto = HabitStatisticDto.builder()
            .id(habitStatistic.getId())
            .amountOfItems(habitStatistic.getAmountOfItems())
            .createDate(habitStatistic.getCreateDate())
            .habitRate(habitStatistic.getHabitRate())
            .habitAssignId(habitStatistic.getHabitAssign().getId())
            .build();
        HabitStatisticDto expected = habitStatisticDtoMapper.convert(habitStatistic);
        assertEquals(habitStatisticDto, expected);
    }
}