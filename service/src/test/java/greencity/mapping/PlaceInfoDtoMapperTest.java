package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.breaktime.BreakTimeDto;
import greencity.dto.location.LocationDto;
import greencity.dto.openhours.OpenHoursDto;
import greencity.dto.place.PlaceInfoDto;
import greencity.entity.BreakTime;
import greencity.entity.OpeningHours;
import greencity.entity.Place;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ExtendWith(SpringExtension.class)
class PlaceInfoDtoMapperTest {
    @InjectMocks
    private PlaceInfoDtoMapper mapper;

    @Test
    void convert() {
        Place place = ModelUtils.getPlace();

        LocationDto locationDto = LocationDto.builder()
            .id(place.getLocation().getId())
            .lat(place.getLocation().getLat())
            .lng(place.getLocation().getLng())
            .address(place.getLocation().getAddress()).build();

        PlaceInfoDto placeInfoDto = new PlaceInfoDto();
        placeInfoDto.setId(1L);
        placeInfoDto.setName("Forum");
        placeInfoDto.setLocation(locationDto);
        placeInfoDto.setPlaceImages(new ArrayList<>());
        placeInfoDto.setDescription(place.getDescription());
        placeInfoDto.setWebsiteUrl(place.getEmail());

        OpeningHours openingHours = OpeningHours.builder()
            .id(1L)
            .openTime(LocalTime.of(8, 30))
            .breakTime(BreakTime.builder()
                .id(1L)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .build())
            .closeTime(LocalTime.of(20, 0))
            .weekDay(DayOfWeek.MONDAY)
            .build();

        OpenHoursDto openHoursDto = OpenHoursDto.builder()
            .id(openingHours.getId())
            .openTime(openingHours.getOpenTime())
            .breakTime(BreakTimeDto.builder()
                .startTime(openingHours.getBreakTime().getStartTime())
                .endTime(openingHours.getBreakTime().getEndTime())
                .build())
            .closeTime(openingHours.getCloseTime())
            .weekDay(openingHours.getWeekDay())
            .build();

        placeInfoDto.setOpeningHoursList(List.of(openHoursDto));
        place.setOpeningHoursList(Set.of(openingHours));

        Assertions.assertEquals(placeInfoDto, mapper.convert(place));
    }
}
