package greencity.dto.discount;

import greencity.constant.ServiceValidationConstants;
import greencity.dto.specification.SpecificationNameDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(exclude = {"value"})
public class DiscountValueDto {
    @Range(min = ServiceValidationConstants.DISCOUNT_VALUE_MIN, max = ServiceValidationConstants.DISCOUNT_VALUE_MAX,
        message = ServiceValidationConstants.DISCOUNT_VALUE_DOES_NOT_CORRECT)
    private int value = 0;

    @Valid
    private SpecificationNameDto specification;
}
