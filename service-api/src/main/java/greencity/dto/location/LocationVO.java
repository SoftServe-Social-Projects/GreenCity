package greencity.dto.location;

import greencity.dto.place.PlaceVO;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class LocationVO {
    private Long id;
    private Double lat;
    private Double lng;
    private String address;
    private PlaceVO place;
    private String addressUa;
}
