package greencity.mapping;

import java.util.Set;
import java.util.stream.Collectors;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import greencity.dto.place.PlaceResponse;
import greencity.entity.OpeningHours;
import greencity.entity.Place;
import greencity.enums.PlaceStatus;

@Component
public class PlaceMapper extends AbstractConverter<PlaceResponse, Place> {
    @Override
    protected Place convert(PlaceResponse source) {
        Place place = new Place();
        place.setName(source.getPlaceName());
        place.setOpeningHoursList(mapOpeningHoursList(source, place));
        place.setStatus(PlaceStatus.APPROVED);
        place.setDescription(source.getDescription());
        place.setEmail(source.getWebsiteUrl());
        return place;
    }

    private Set<OpeningHours> mapOpeningHoursList(PlaceResponse placeResponse, Place place) {
        return placeResponse.getOpeningHoursList().stream()
            .map(dto -> OpeningHours.builder()
                .place(place)
                .openTime(dto.getOpenTime())
                .closeTime(dto.getCloseTime())
                .weekDay(dto.getWeekDay())
                .build())
            .collect(Collectors.toSet());
    }
}
