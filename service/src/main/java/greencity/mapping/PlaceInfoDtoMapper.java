package greencity.mapping;

import greencity.dto.breaktime.BreakTimeDto;
import greencity.dto.location.LocationDto;
import greencity.dto.openhours.OpenHoursDto;
import greencity.dto.place.PlaceInfoDto;
import greencity.entity.OpeningHours;
import greencity.entity.Photo;
import greencity.entity.Place;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that used by {@link ModelMapper} to map {@link Place} into
 * {@link PlaceInfoDto}.
 *
 * @author Vasyl Zhovnir
 */
@Component
public class PlaceInfoDtoMapper extends AbstractConverter<Place, PlaceInfoDto> {
    /**
     * Method convert {@link Place} to {@link PlaceInfoDto}.
     *
     * @return {@link PlaceInfoDto}
     */
    @Override
    protected PlaceInfoDto convert(Place source) {
        PlaceInfoDto placeInfoDto = new PlaceInfoDto();
        placeInfoDto.setId(source.getId());
        placeInfoDto.setName(source.getName());
        placeInfoDto.setLocation(LocationDto.builder()
            .id(source.getLocation().getId())
            .lat(source.getLocation().getLat())
            .lng(source.getLocation().getLng())
            .address(source.getLocation().getAddress())
            .build());
        List<String> images = source.getPhotos().stream().map(Photo::getName).toList();

        placeInfoDto.setPlaceImages(images);
        placeInfoDto.setDescription(source.getDescription());
        placeInfoDto.setWebsiteUrl(source.getEmail());
        placeInfoDto.setOpeningHoursList(mapFromOpeningHoursToOpenHoursDto(source));
        return placeInfoDto;
    }

    private List<OpenHoursDto> mapFromOpeningHoursToOpenHoursDto(Place place) {
        List<OpenHoursDto> list = new ArrayList<>();
        for (OpeningHours openingHours : place.getOpeningHoursList()) {
            OpenHoursDto openHoursDto = new OpenHoursDto();
            openHoursDto.setId(openingHours.getId());
            openHoursDto.setOpenTime(openingHours.getOpenTime());
            openHoursDto.setWeekDay(openingHours.getWeekDay());
            openHoursDto.setCloseTime(openingHours.getCloseTime());
            openHoursDto.setBreakTime(BreakTimeDto.builder()
                .startTime(openingHours.getBreakTime().getStartTime())
                .endTime(openingHours.getBreakTime().getEndTime())
                .build());
            list.add(openHoursDto);
        }
        return list;
    }
}
