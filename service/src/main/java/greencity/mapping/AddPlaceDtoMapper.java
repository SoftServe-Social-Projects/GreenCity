package greencity.mapping;

import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import greencity.dto.place.AddPlaceDto;
import greencity.dto.place.PlaceResponse;

@Component
public class AddPlaceDtoMapper extends AbstractConverter<AddPlaceDto, PlaceResponse> {
    @Override
    protected PlaceResponse convert(AddPlaceDto source) {
        return PlaceResponse.builder()
            .openingHoursList(source.getOpeningHoursList())
            .placeName(source.getPlaceName())
            .description(source.getDescription())
            .websiteUrl(source.getWebsiteUrl())
            .build();
    }
}
