package greencity.dto.filter;

import com.google.maps.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilterPlacesApiDto {
    private LatLng location;
    private int radius;
    private RankBy rankBy;
    private String keyword;
    private PriceLevel minPrice;
    private PriceLevel maxPrice;
    private String name;
    private boolean openNow;
    private PlaceType type;

    public static final String defaultJson = """
        {
          "location": {
            "lat": 0,
            "lng": 0
          },
          "radius": 0,
          "rankBy": "PROMINENCE",
          "keyword": "string",
          "minPrice": "0",
          "maxPrice": "4",
          "name": "string",
          "openNow": true,
          "type": "restaurant"
        }
        """;
}
