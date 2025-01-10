package greencity.dto.place;

import greencity.constant.ServiceValidationConstants;
import greencity.dto.category.CategoryDto;
import greencity.dto.discount.DiscountValueDto;
import greencity.dto.location.LocationAddressAndGeoDto;
import greencity.dto.openhours.OpeningHoursDto;
import greencity.dto.photo.PhotoAddDto;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class PlaceAddDto {
    @NotBlank
    @Length(max = ServiceValidationConstants.PLACE_NAME_MAX_LENGTH)
    private String name;

    @Valid
    private LocationAddressAndGeoDto location;

    @Valid
    private CategoryDto category;

    @Valid
    @Builder.Default
    @Size(min = 1, message = ServiceValidationConstants.BAD_OPENING_HOURS_LIST_REQUEST)
    private Set<OpeningHoursDto> openingHoursList = new HashSet<>();

    @Valid
    @Builder.Default
    @Size(min = 1, message = ServiceValidationConstants.BAD_DISCOUNT_VALUES_LIST_REQUEST)
    private Set<DiscountValueDto> discountValues = new HashSet<>();

    @Valid
    @Size(max = 5, message = ServiceValidationConstants.BAD_PHOTO_LIST_REQUEST)
    private List<PhotoAddDto> photos;
}
