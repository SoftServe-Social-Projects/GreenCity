package greencity.mapping.events;

import greencity.ModelUtils;
import greencity.dto.event.AddEventDtoResponse;
import greencity.dto.event.EventDto;
import greencity.entity.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class EventDtoToEventMapperTest {
    @InjectMocks
    EventDtoToEventMapper mapper;

    @Test
    void convertTest() {
        EventDto eventDto = ModelUtils.getEventDto();
        eventDto.setAdditionalImages(new ArrayList<>());

        Event expected = ModelUtils.getEvent();

        assertEquals(expected.getDescription(), mapper.convert(eventDto).getDescription());
    }

    @Test
    void convertTestWithoutCoordinates() {
        EventDto event = ModelUtils.getEventWithoutCoordinatesDto();
        event.setAdditionalImages(new ArrayList<>());

        Event expected = ModelUtils.getEvent();

        assertEquals(expected.getDescription(), mapper.convert(event).getDescription());
    }
}
