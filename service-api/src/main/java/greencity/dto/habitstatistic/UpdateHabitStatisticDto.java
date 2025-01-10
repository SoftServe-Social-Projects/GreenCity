package greencity.dto.habitstatistic;

import greencity.constant.ServiceValidationConstants;
import greencity.enums.HabitRate;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class UpdateHabitStatisticDto {
    @Min(ServiceValidationConstants.MIN_AMOUNT_OF_ITEMS)
    @Max(ServiceValidationConstants.MAX_AMOUNT_OF_ITEMS)
    @NotNull
    private Integer amountOfItems;
    @NotNull
    private HabitRate habitRate;
}
