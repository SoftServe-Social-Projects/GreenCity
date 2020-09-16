package greencity.dto.specification;

import greencity.constant.ValidationConstants;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationNameDto {
    @NotBlank
    private String name;
}
