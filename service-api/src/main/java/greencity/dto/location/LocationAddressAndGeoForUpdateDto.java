package greencity.dto.location;

import greencity.constant.ServiceValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationAddressAndGeoForUpdateDto {
    @NotBlank
    @Length(
        min = ServiceValidationConstants.PLACE_ADDRESS_MIN_LENGTH,
        max = ServiceValidationConstants.PLACE_ADDRESS_MAX_LENGTH)
    @Pattern(regexp = "^[^<>]+$")
    private String address;

    @NotNull(message = ServiceValidationConstants.EMPTY_VALUE_OF_LATITUDE)
    private Double lat;

    @NotNull(message = ServiceValidationConstants.EMPTY_VALUE_OF_LONGITUDE)
    private Double lng;

    @NotBlank
    @Length(
        min = ServiceValidationConstants.PLACE_ADDRESS_MIN_LENGTH,
        max = ServiceValidationConstants.PLACE_ADDRESS_MAX_LENGTH)
    @Pattern(regexp = "^[^<>]+$")
    private String addressUa;
}
