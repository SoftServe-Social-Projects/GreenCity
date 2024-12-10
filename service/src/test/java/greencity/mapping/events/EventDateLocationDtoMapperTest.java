package greencity.mapping.events;

import greencity.ModelUtils;
import greencity.dto.event.EventDateLocationDto;
import greencity.entity.event.EventDateLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
class EventDateLocationDtoMapperTest {
    @Spy
    private AddressDtoMapper addressDtoMapper;
    @InjectMocks
    private EventDateLocationDtoMapper mapper;

    @Test
    void convert() {
        EventDateLocation expected = ModelUtils.getEventDateLocation();
        EventDateLocationDto dto = EventDateLocationDto.builder()
            .id(expected.getId())
            .startDate(expected.getStartDate())
            .finishDate(expected.getFinishDate())
            .coordinates(ModelUtils.getAddressDto())
            .build();
        EventDateLocation actual = mapper.convert(dto);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getStartDate(), actual.getStartDate());
        assertEquals(expected.getAddress(), actual.getAddress());
    }

    @Test
    void convertWithoutAddress() {
        EventDateLocation expected = ModelUtils.getEventDateLocation();
        expected.setAddress(null);
        EventDateLocationDto dto = EventDateLocationDto.builder()
            .id(expected.getId())
            .startDate(expected.getStartDate())
            .finishDate(expected.getFinishDate())
            .build();
        EventDateLocation actual = mapper.convert(dto);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getStartDate(), actual.getStartDate());
        assertNull(actual.getAddress());
    }

    @Test
    void mapAllToListWithValidEventDateLocationDtoTest() {
        EventDateLocationDto eventDateLocationDto = ModelUtils.getEventDateLocationDtoWithLinkAndCoordinates();

        List<EventDateLocationDto> eventDateLocationDtoList = List.of(eventDateLocationDto);

        EventDateLocation dto = EventDateLocation.builder().id(eventDateLocationDto.getId())
            .startDate(eventDateLocationDto.getStartDate()).finishDate(eventDateLocationDto.getFinishDate())
            .address(addressDtoMapper.convert(eventDateLocationDto.getCoordinates()))
            .onlineLink(eventDateLocationDto.getOnlineLink()).build();

        List<EventDateLocation> expectedList = List.of(dto);
        assertEquals(expectedList, mapper.mapAllToList(eventDateLocationDtoList));
    }
}
