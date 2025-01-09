package greencity.dto.filter;

import com.google.maps.model.ComponentFilter;
import greencity.dto.location.MapBoundsDto;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import static greencity.constant.ServiceValidationConstants.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilterGeocodingApiDto {
    private MapBoundsDto bounds;
    private String address;
    private ComponentFilter[] components;
    @Pattern(
            regexp = "^[a-zA-Z]{2}$",
            message = INVALID_REGION_FORMAT
    )
    private String region;
}
