package greencity.dto.habitstatistic;

import greencity.constant.ValidationConstants;
import greencity.entity.enums.HabitRate;
import java.time.ZonedDateTime;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddHabitStatisticDto {
    @Min(0)
    private Long id;
    @Range(min = ValidationConstants.MIN_AMOUNT_OF_ITEMS, max = ValidationConstants.MAX_AMOUNT_OF_ITEMS)
    @NotNull(message = "Amount of items can not be null.")
    private Integer amountOfItems;
    @NotNull(message = "Rate of the day can not be null")
    private HabitRate habitRate;
    @Min(0)
    @NotNull(message = "Habit id can not be null")
    private Long habitId;
    @NotNull(message = "Date of creation can not be null")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime createdOn;
}
