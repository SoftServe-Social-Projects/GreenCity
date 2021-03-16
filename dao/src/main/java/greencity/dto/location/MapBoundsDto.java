package greencity.dto.location;

import greencity.constant.ValidationConstants;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MapBoundsDto {
    @Min(value = -90, message = ValidationConstants.MIN_VALUE_LATITUDE)
    @Max(value = 90, message = ValidationConstants.MAX_VALUE_LATITUDE)
    @NotNull(message = ValidationConstants.N_E_LAT_CAN_NOT_BE_NULL)
    private Double northEastLat;

    @Min(value = -180, message = ValidationConstants.MIN_VALUE_LONGITUDE)
    @Max(value = 180, message = ValidationConstants.MAX_VALUE_LONGITUDE)
    @NotNull(message = ValidationConstants.N_E_LNG_CAN_NOT_BE_NULL)
    private Double northEastLng;

    @Min(value = -90, message = ValidationConstants.MIN_VALUE_LATITUDE)
    @Max(value = 90, message = ValidationConstants.MAX_VALUE_LATITUDE)
    @NotNull(message = ValidationConstants.S_W_LAT_CAN_NOT_BE_NULL)
    private Double southWestLat;

    @Min(value = -180, message = ValidationConstants.MIN_VALUE_LONGITUDE)
    @Max(value = 180, message = ValidationConstants.MAX_VALUE_LONGITUDE)
    @NotNull(message = ValidationConstants.S_W_LNG_CAN_NOT_BE_NULL)
    private Double southWestLng;
}
