package greencity.validator;

import greencity.ModelUtils;
import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.UpdateEventDateLocationDto;
import greencity.dto.event.UpdateEventRequestDto;
import greencity.exception.exceptions.EventDtoValidationException;
import greencity.exception.exceptions.InvalidURLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class EventDtoRequestValidatorTest {
    @InjectMocks
    private EventDtoRequestValidator validator;

    @Test
    void withoutDatesException() {
        AddEventDtoRequest addEventDtoRequest = ModelUtils.getEventDtoWithoutDates();
        assertThrows(EventDtoValidationException.class, () -> validator.isValid(addEventDtoRequest, null));
    }

    @Test
    void withZeroDatesException() {
        AddEventDtoRequest addEventDtoRequest = ModelUtils.getEventDtoWithZeroDates();
        assertThrows(EventDtoValidationException.class, () -> validator.isValid(addEventDtoRequest, null));
    }

    @Test
    void withTooManyDatesException() {
        AddEventDtoRequest addEventDtoRequest = ModelUtils.getEventDtoWithTooManyDates();
        assertThrows(EventDtoValidationException.class, () -> validator.isValid(addEventDtoRequest, null));
    }

    @Test
    void withStartDateInPastException() {
        AddEventDtoRequest addEventDtoRequest = ModelUtils.getEventWithPastStartDate();
        assertThrows(EventDtoValidationException.class, () -> validator.isValid(addEventDtoRequest, null));
    }

    @Test
    void withStartDateAfterFinishDateException() {
        AddEventDtoRequest addEventDtoRequest = ModelUtils.getEventWithStartDateAfterFinishDate();
        assertThrows(EventDtoValidationException.class, () -> validator.isValid(addEventDtoRequest, null));
    }

    @Test
    void withoutAddressAndLinkException() {
        AddEventDtoRequest addEventDtoRequest = ModelUtils.getEventWithoutAddressAndLink();
        assertThrows(EventDtoValidationException.class, () -> validator.isValid(addEventDtoRequest, null));
    }

    @Test
    void withInvalidLinkException() {
        AddEventDtoRequest addEventDtoRequest = ModelUtils.getEventWithInvalidLink();
        assertThrows(InvalidURLException.class, () -> validator.isValid(addEventDtoRequest, null));
    }

    @Test
    void withTooManyTagsException() {
        AddEventDtoRequest addEventDtoRequest = ModelUtils.getEventWithTooManyTags();
        assertThrows(EventDtoValidationException.class, () -> validator.isValid(addEventDtoRequest, null));
    }

    @Test
    void validEvent() {
        AddEventDtoRequest addEventDtoRequest = ModelUtils.getAddEventDtoRequest();
        assertTrue(validator.isValid(addEventDtoRequest, null));
    }

    @Test
    void saveEventWithSameDates() {
        AddEventDtoRequest addEventDto = ModelUtils.getAddEventDtoRequest();
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(2L);
        addEventDto.getDatesLocations().forEach(e -> e.setStartDate(zonedDateTime));
        addEventDto.getDatesLocations().forEach(e -> e.setFinishDate(zonedDateTime));
        assertThrows(EventDtoValidationException.class, () -> validator.isValid(addEventDto, null));
    }

    @Test
    void updateWithTooManyTagsException() {
        UpdateEventRequestDto updateEventDto = ModelUtils.getUpdateEventDtoWithTooManyDates();
        assertThrows(EventDtoValidationException.class, () -> validator.isValid(updateEventDto, null));
    }

    @Test
    void updateWithEmptyDateLocations() {
        UpdateEventRequestDto updateEventDto = ModelUtils.getUpdateEventDtoWithEmptyDateLocations();
        assertThrows(EventDtoValidationException.class, () -> validator.isValid(updateEventDto, null));
    }

    @Test
    void updateEventDtoWithoutDates() {
        UpdateEventRequestDto updateEventDto = ModelUtils.getUpdateEventDtoWithoutDates();
        assertThrows(EventDtoValidationException.class, () -> validator.isValid(updateEventDto, null));

    }

    @Test
    void updateWithInvalidLinkException() {
        UpdateEventRequestDto updateEventDto = ModelUtils.getUpdateEventWithoutAddressAndLink();
        assertThrows(EventDtoValidationException.class, () -> validator.isValid(updateEventDto, null));
    }

    @Test
    void updateWithoutLinkAndCoordinates() {
        UpdateEventRequestDto updateEventDto = ModelUtils.getUpdateEventDto();
        updateEventDto.getDatesLocations().forEach(e -> e.setOnlineLink(null));
        updateEventDto.getDatesLocations().forEach(e -> e.setCoordinates(null));
        assertThrows(EventDtoValidationException.class, () -> validator.isValid(updateEventDto, null));
    }

    @Test
    void validEventUpdate() {
        UpdateEventRequestDto updateEventDto = ModelUtils.getUpdateEventDto();
        assertTrue(validator.isValid(updateEventDto, null));
    }

    @Test
    void updateEventWithSameDates() {
        UpdateEventRequestDto updateEventDto = ModelUtils.getUpdateEventDto();
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(2L);
        updateEventDto.getDatesLocations().forEach(e -> e.setStartDate(zonedDateTime));
        updateEventDto.getDatesLocations().forEach(e -> e.setFinishDate(zonedDateTime));
        assertThrows(EventDtoValidationException.class, () -> validator.isValid(updateEventDto, null));
    }

    @Test
    void invalidObjectType() {
        Object value = new Object();
        assertFalse(validator.isValid(value, null));
    }

    @Test
    void invalidDates() {
        UpdateEventRequestDto updateEventRequestDto =
            UpdateEventRequestDto.builder().datesLocations(List.of(UpdateEventDateLocationDto.builder()
                .startDate(null)
                .finishDate(null)
                .onlineLink("http://localhost:8060/swagger-ui.html#/")
                .build()))
                .tags(List.of("first", "second", "third")).build();
        assertThrows(EventDtoValidationException.class,
            () -> validator.isValid(updateEventRequestDto, null));
    }
}