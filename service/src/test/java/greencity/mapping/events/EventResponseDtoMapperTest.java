package greencity.mapping.events;

import greencity.ModelUtils;
import greencity.dto.event.EventResponseDto;
import greencity.entity.event.Event;
import greencity.entity.event.EventDateLocation;
import greencity.entity.event.EventGrade;
import greencity.service.CommentService;
import greencity.utils.EventUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static greencity.ModelUtils.getEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

        assertEquals(expected.getEventInformation().getTitle(), result.getEventInformation().getTitle());
        assertEquals(event.getUsersLikedEvents().size(), result.getLikes());
    }

    @Test
    void isRelevantFieldTest() {
        when(commentService.countCommentsForEvent(any())).thenReturn(0);
        Event event = getEvent();
        EventResponseDto result = mapper.convert(event);
        assertTrue(result.getIsRelevant());
    }
}
