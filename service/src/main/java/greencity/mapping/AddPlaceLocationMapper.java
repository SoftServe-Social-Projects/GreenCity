package greencity.mapping;

import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import greencity.dto.location.AddPlaceLocation;
import greencity.entity.Location;

@Component
public class AddPlaceLocationMapper extends AbstractConverter<AddPlaceLocation, Location> {
    @Override
    protected Location convert(AddPlaceLocation source) {
        return Location.builder()
            .addressUa(source.getAddress())
            .address(source.getAddressEng())
            .lng(source.getLng())
            .lat(source.getLat())
            .build();
    }
}
