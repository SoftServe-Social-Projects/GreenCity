package greencity.dto.location;

import greencity.constant.ServiceValidationConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MapBoundsDto {
    @Min(value = -90, message = ServiceValidationConstants.MIN_VALUE_LATITUDE)
    @Max(value = 90, message = ServiceValidationConstants.MAX_VALUE_LATITUDE)
    @NotNull(message = ServiceValidationConstants.N_E_LAT_CAN_NOT_BE_NULL)
    private Double northEastLat;

    @Min(value = -180, message = ServiceValidationConstants.MIN_VALUE_LONGITUDE)
    @Max(value = 180, message = ServiceValidationConstants.MAX_VALUE_LONGITUDE)
    @NotNull(message = ServiceValidationConstants.N_E_LNG_CAN_NOT_BE_NULL)
    private Double northEastLng;

    @Min(value = -90, message = ServiceValidationConstants.MIN_VALUE_LATITUDE)
    @Max(value = 90, message = ServiceValidationConstants.MAX_VALUE_LATITUDE)
    @NotNull(message = ServiceValidationConstants.S_W_LAT_CAN_NOT_BE_NULL)
    private Double southWestLat;

    @Min(value = -180, message = ServiceValidationConstants.MIN_VALUE_LONGITUDE)
    @Max(value = 180, message = ServiceValidationConstants.MAX_VALUE_LONGITUDE)
    @NotNull(message = ServiceValidationConstants.S_W_LNG_CAN_NOT_BE_NULL)
    private Double southWestLng;
}
