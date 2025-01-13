package greencity.dto.place;

import java.util.HashSet;
import java.util.Set;
import greencity.dto.category.CategoryDto;
import greencity.dto.location.AddPlaceLocation;
import greencity.dto.openhours.OpeningHoursDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
@ToString
public class PlaceResponse {
    private String placeName;
    private CategoryDto category;
    private AddPlaceLocation locationAddressAndGeoDto;
    @Builder.Default
    private Set<OpeningHoursDto> openingHoursList = new HashSet<>();
    private String description;
    private String websiteUrl;
}
