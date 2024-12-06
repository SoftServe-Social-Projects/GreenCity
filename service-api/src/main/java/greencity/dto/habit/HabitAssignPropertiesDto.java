package greencity.dto.habit;

import greencity.constant.AppConstant;
import java.util.List;
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
public class HabitAssignPropertiesDto {
    @NotNull
    @Min(AppConstant.MIN_DAYS_DURATION)
    @Max(AppConstant.MAX_DAYS_DURATION)
    private Integer duration;

    @NotNull
    private Boolean isPrivate;

    private List<Long> defaultToDoListItemIds;

    private List<Long> defaultCustomToDoListItemIds;
}
