package greencity.dto.filter;

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
    private String address;
    private String components;
    @Pattern(
            regexp = "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?((1[0-7]\\d|\\d{1,2})(\\.\\d+)?|180(\\.0+)?)$",
            message = INVALID_lAT_LNG_FORMAT
    )
    private String latlng;
    private String placeId;
    @Pattern(
            regexp = "^[a-zA-Z]{2}$",
            message = INVALID_REGION_FORMAT
    )
    private String region;
    @Pattern(
            regexp = "^[a-z]{2}$",
            message = INVALID_LANGUAGE_FORMAT
    )
    private String language;
    private boolean searchInGeocodingApi;
}
