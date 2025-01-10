package greencity.dto.habitstatistic;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class GetHabitStatisticDto {
    Long amountOfUsersAcquired;
    List<HabitStatisticDto> habitStatisticDtoList;
}
