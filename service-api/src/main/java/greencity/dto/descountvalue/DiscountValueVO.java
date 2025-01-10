package greencity.dto.descountvalue;

import greencity.dto.place.PlaceVO;
import greencity.dto.specification.SpecificationVO;
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
@EqualsAndHashCode(exclude = {"value"})
@Builder
public class DiscountValueVO {
    private Long id;
    private Integer value;
    private PlaceVO place;
    private SpecificationVO specification;
}
