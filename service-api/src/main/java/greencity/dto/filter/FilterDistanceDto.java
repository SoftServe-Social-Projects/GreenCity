package greencity.dto.filter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import static greencity.constant.ServiceValidationConstants.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterDistanceDto {
    @Min(value = -90, message = LAT_MIN_VALIDATION)
    @Max(value = 90, message = LAT_MAX_VALIDATION)
    private Double lat;
    @Min(value = -180, message = LNG_MIN_VALIDATION)
    @Max(value = 180, message = LNG_MAX_VALIDATION)
    private Double lng;
    @Positive
    private Double distance;
}
