package greencity.dto.place;

import greencity.constant.ServiceValidationConstants;
import greencity.dto.category.CategoryDto;
import greencity.dto.discount.DiscountValueDto;
import greencity.dto.location.LocationAddressAndGeoForUpdateDto;
import greencity.dto.openhours.OpeningHoursDto;
import java.util.Set;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class PlaceUpdateDto {
    @NotNull
    private Long id;

    @NotBlank
    @Length(max = ServiceValidationConstants.PLACE_NAME_MAX_LENGTH)
    private String name;

    @Valid
    private LocationAddressAndGeoForUpdateDto location;

    @Valid
    private CategoryDto category;

    @Valid
    @Size(min = 1, message = ServiceValidationConstants.BAD_OPENING_HOURS_LIST_REQUEST)
    private Set<OpeningHoursDto> openingHoursList;

    @Valid
    @Size(min = 1, message = ServiceValidationConstants.BAD_DISCOUNT_VALUES_LIST_REQUEST)
    private Set<DiscountValueDto> discountValues;
}
