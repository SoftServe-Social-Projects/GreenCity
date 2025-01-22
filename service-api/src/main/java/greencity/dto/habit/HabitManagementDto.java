package greencity.dto.habit;

import greencity.constant.ServiceValidationConstants;
import greencity.dto.Sortable;
import greencity.dto.habittranslation.HabitTranslationManagementDto;
import java.io.Serializable;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class HabitManagementDto implements Serializable, Sortable {
    private Long id;
    private String image;
    @Min(value = 1, message = ServiceValidationConstants.HABIT_COMPLEXITY)
    @Max(value = 3, message = ServiceValidationConstants.HABIT_COMPLEXITY)
    @NotNull
    private Integer complexity;
    @Valid
    private List<HabitTranslationManagementDto> habitTranslations;
    @Min(value = 7, message = ServiceValidationConstants.HABIT_DEFAULT_DURATION)
    @Max(value = 56, message = ServiceValidationConstants.HABIT_DEFAULT_DURATION)
    private Integer defaultDuration;
    private Boolean isCustomHabit;
    private Boolean isDeleted;

    @Override
    public List<String> getSortableFields() {
        return List.of();
    }
}
