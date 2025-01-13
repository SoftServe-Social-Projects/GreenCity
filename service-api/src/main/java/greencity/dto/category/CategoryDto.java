package greencity.dto.category;

import greencity.constant.ServiceValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
    @Pattern(regexp = "^[a-zA-Z0-9\\s][^<>]*$", message = ServiceValidationConstants.CATEGORY_NAME_BAD_FORMED)
    @Length(
        min = ServiceValidationConstants.CATEGORY_NAME_MIN_LENGTH,
        max = ServiceValidationConstants.CATEGORY_NAME_MAX_LENGTH)
    @NotBlank
    private String name;

    private String nameUa;

    private Long parentCategoryId;
}