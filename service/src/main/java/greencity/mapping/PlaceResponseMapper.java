package greencity.mapping;

import java.util.stream.Collectors;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import greencity.dto.category.CategoryDto;
import greencity.dto.location.AddPlaceLocation;
import greencity.dto.openhours.OpeningHoursDto;
import greencity.dto.place.PlaceResponse;
import greencity.entity.Place;

@Component
public class PlaceResponseMapper extends AbstractConverter<Place, PlaceResponse> {
    @Override
    protected PlaceResponse convert(Place source) {
        return PlaceResponse.builder()
            .category(CategoryDto.builder()
                .name(source.getCategory().getName())
                .nameUa(source.getCategory().getNameUa())
                .build())
            .placeName(source.getName())
            .openingHoursList(source.getOpeningHoursList().stream()
                .map(hour -> OpeningHoursDto.builder()
                    .weekDay(hour.getWeekDay())
                    .openTime(hour.getOpenTime())
                    .closeTime(hour.getCloseTime())
                    .build())
                .collect(Collectors.toSet()))
            .locationAddressAndGeoDto(AddPlaceLocation.builder()
                .address(source.getLocation().getAddressUa())
                .addressEng(source.getLocation().getAddress())
                .lat(source.getLocation().getLat())
                .lng(source.getLocation().getLng())
                .build())
            .websiteUrl(source.getEmail())
            .description(source.getDescription())
            .build();
    }
}
