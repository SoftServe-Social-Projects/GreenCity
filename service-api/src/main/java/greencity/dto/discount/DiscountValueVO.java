package greencity.dto.discount;

import greencity.dto.place.PlaceVO;
import greencity.dto.specification.SpecificationVO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountValueVO {
    private Long id;
    private Integer value;
    private PlaceVO place;
    private SpecificationVO specification;
}
