package greencity.dto.place;

import greencity.dto.placecomment.PlaceCommentDto;
import greencity.dto.discount.DiscountValueDto;
import greencity.dto.location.LocationDto;
import greencity.dto.openhours.OpenHoursDto;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Builder;
import org.springframework.lang.Nullable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class PlaceInfoDto {
    private Long id;
    private String name;
    private LocationDto location;
    private List<OpenHoursDto> openingHoursList;
    private List<DiscountValueDto> discountValues;
    private List<PlaceCommentDto> comments;
    private Double rate;
    private String description;
    private String websiteUrl;
    @Nullable
    private List<String> placeImages;
}
