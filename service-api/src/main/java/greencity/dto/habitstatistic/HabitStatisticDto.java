package greencity.dto.habitstatistic;

import greencity.enums.HabitRate;
import java.time.ZonedDateTime;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class HabitStatisticDto {
    @NotNull
    @Min(1)
    private Long id;
    @NotEmpty
    private HabitRate habitRate;
    @NotEmpty
    private ZonedDateTime createDate;
    @NotEmpty
    private Integer amountOfItems;
    @NotNull
    private Long habitAssignId;
}
