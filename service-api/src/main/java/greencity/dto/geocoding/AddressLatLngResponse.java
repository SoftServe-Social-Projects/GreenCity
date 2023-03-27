package greencity.dto.geocoding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@Setter
public class AddressLatLngResponse {
    private double latitude;
    private double longitude;
    private AddressResponse addressEn;
    private AddressResponse addressUa;
}
