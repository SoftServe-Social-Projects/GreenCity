package greencity.dto.filter;

import greencity.dto.specification.SpecificationNameDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterDiscountDto {
    private SpecificationNameDto specification;
    private int discountMin;
    private int discountMax;
}
