package greencity.mapping.events;

import greencity.ModelUtils;
import greencity.dto.event.EventResponseDto;
import greencity.entity.event.Event;
import greencity.service.CommentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import static greencity.ModelUtils.getEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class EventResponseDtoMapperTest {
    @InjectMocks
    EventResponseDtoMapper mapper;

    @Mock
    CommentService commentService;

    @Test
    void convertTest() {
        Event event = getEvent();
        event.setAdditionalImages(new ArrayList<>());
        event.setUsersLikedEvents(Set.of(ModelUtils.getUser()));
        EventResponseDto expected = ModelUtils.getEventResponseDto();

        EventResponseDto result = mapper.convert(event);

        assertEquals(expected.eventInformation().title(), result.eventInformation().title());
        assertEquals(event.getUsersLikedEvents().size(), result.likes());
    }

    @Test
    void convertHasNullAddressTest() {
        Event event = getEvent();
        event.getDates().forEach(date -> date.setAddress(null));

        EventResponseDto result = mapper.convert(event);

        result.dates().forEach(dateInfo -> assertNull(dateInfo.coordinates()));
        assertEquals(event.getId(), result.id());

        result.dates().forEach(dateInfo -> assertTrue(Optional.ofNullable(dateInfo.coordinates()).isEmpty(),
            "Coordinates should be empty when address is null"));
        assertEquals(event.getId(), result.id(), "Event ID should be preserved even with null address");
    }
}
