package greencity.dto.achievementcategory;

import greencity.constant.ServiceValidationConstants;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class AchievementCategoryDto {
    @Pattern(regexp = "^[a-zA-Z0-9\\s][^<>]*$", message = ServiceValidationConstants.CATEGORY_NAME_BAD_FORMED)
    @Length(
        min = ServiceValidationConstants.CATEGORY_NAME_MIN_LENGTH,
        max = ServiceValidationConstants.CATEGORY_NAME_MAX_LENGTH)
    @NotBlank
    private String name;
}
