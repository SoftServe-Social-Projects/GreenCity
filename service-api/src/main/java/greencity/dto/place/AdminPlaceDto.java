package greencity.dto.place;

import greencity.dto.category.CategoryDto;
import greencity.dto.location.LocationDto;
import greencity.dto.openhours.OpenHoursDto;
import greencity.dto.user.PlaceAuthorDto;
import greencity.enums.PlaceStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class AdminPlaceDto {
    private Long id;
    private String name;
    private LocationDto location;
    private CategoryDto category;
    private List<OpenHoursDto> openingHoursList;
    private PlaceAuthorDto author;
    private PlaceStatus status;
    private LocalDateTime modifiedDate;
    private Boolean isFavorite;
}
