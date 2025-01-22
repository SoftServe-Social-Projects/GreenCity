package greencity.validator;

import greencity.ModelUtils;
import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.UpdateEventDateLocationDto;
import greencity.dto.event.UpdateEventRequestDto;
import greencity.exception.exceptions.InvalidURLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class EventDtoRequestValidatorTest {
    @InjectMocks
    private EventDtoRequestValidator validator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString()))
            .thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(constraintValidatorContext);
    }

    @Test
    void withoutDatesException() {
        AddEventDtoRequest addEventDtoRequest = ModelUtils.getEventDtoWithoutDates();
        boolean isValid = validator.isValid(addEventDtoRequest, constraintValidatorContext);
        assertFalse(isValid, "Validation should fail and return false for empty date locations");
    }

    @Test
    void withZeroDatesException() {
        AddEventDtoRequest addEventDtoRequest = ModelUtils.getEventDtoWithZeroDates();
        boolean isValid = validator.isValid(addEventDtoRequest, constraintValidatorContext);
        assertFalse(isValid, "Validation should fail and return false for zero date locations");
    }

    @Test
    void withTooManyDatesException() {
        AddEventDtoRequest addEventDtoRequest = ModelUtils.getEventDtoWithTooManyDates();
        boolean isValid = validator.isValid(addEventDtoRequest, constraintValidatorContext);
        assertFalse(isValid, "Validation should fail and return false for too many date locations");
    }

    @Test
    void withStartDateInPastException() {
        AddEventDtoRequest addEventDtoRequest = ModelUtils.getEventWithPastStartDate();
        boolean isValid = validator.isValid(addEventDtoRequest, constraintValidatorContext);
        assertFalse(isValid, "Validation should fail and return false for past start date");
    }

    @Test
    void withStartDateAfterFinishDateException() {
        AddEventDtoRequest addEventDtoRequest = ModelUtils.getEventWithStartDateAfterFinishDate();
        boolean isValid = validator.isValid(addEventDtoRequest, constraintValidatorContext);
        assertFalse(isValid, "Validation should fail and return false for start date after finish date");
    }

    @Test
    void withoutAddressAndLinkException() {
        AddEventDtoRequest addEventDtoRequest = ModelUtils.getEventWithoutAddressAndLink();
        boolean isValid = validator.isValid(addEventDtoRequest, constraintValidatorContext);
        assertFalse(isValid, "Validation should fail and return false for event without address and link");
    }

    @Test
    void withInvalidLinkException() {
        AddEventDtoRequest addEventDtoRequest = ModelUtils.getEventWithInvalidLink();
        assertThrows(InvalidURLException.class,
            () -> validator.isValid(addEventDtoRequest, constraintValidatorContext));
    }

    @Test
    void withTooManyTagsException() {
        AddEventDtoRequest addEventDtoRequest = ModelUtils.getEventWithTooManyTags();
        boolean isValid = validator.isValid(addEventDtoRequest, constraintValidatorContext);
        assertFalse(isValid, "Validation should fail and return false for too many tags");
    }

    @Test
    void validEvent() {
        AddEventDtoRequest addEventDtoRequest = ModelUtils.getAddEventDtoRequest();
        assertTrue(validator.isValid(addEventDtoRequest, constraintValidatorContext));
    }

    @Test
    void saveEventWithSameDates() {
        AddEventDtoRequest addEventDto = ModelUtils.getAddEventDtoRequest();
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(2L);
        addEventDto.getDatesLocations().forEach(e -> e.setStartDate(zonedDateTime));
        addEventDto.getDatesLocations().forEach(e -> e.setFinishDate(zonedDateTime));
        boolean isValid = validator.isValid(addEventDto, constraintValidatorContext);
        assertFalse(isValid, "Validation should fail and return false same event with same dates");
    }

    @Test
    void updateWithTooManyTagsException() {
        UpdateEventRequestDto updateEventDto = ModelUtils.getUpdateEventDtoWithTooManyDates();
        boolean isValid = validator.isValid(updateEventDto, constraintValidatorContext);
        assertFalse(isValid, "Validation should fail and return false for too many tags");
    }

    @Test
    void updateWithEmptyDateLocations() {
        UpdateEventRequestDto updateEventDto = ModelUtils.getUpdateEventDtoWithEmptyDateLocations();
        boolean isValid = validator.isValid(updateEventDto, constraintValidatorContext);
        assertFalse(isValid, "Validation should fail and return false for empty date locations");
    }

    @Test
    void updateEventDtoWithoutDates() {
        UpdateEventRequestDto updateEventDto = ModelUtils.getUpdateEventDtoWithoutDates();
        boolean isValid = validator.isValid(updateEventDto, constraintValidatorContext);
        assertFalse(isValid, "Validation should fail and return false event without dates");
    }

    @Test
    void updateWithInvalidLinkException() {
        UpdateEventRequestDto updateEventDto = ModelUtils.getUpdateEventWithoutAddressAndLink();
        boolean isValid = validator.isValid(updateEventDto, constraintValidatorContext);
        assertFalse(isValid, "Validation should fail and return false for invalid link");
    }

    @Test
    void updateWithoutLinkAndCoordinates() {
        UpdateEventRequestDto updateEventDto = ModelUtils.getUpdateEventDto();
        updateEventDto.getDatesLocations().forEach(e -> e.setOnlineLink(null));
        updateEventDto.getDatesLocations().forEach(e -> e.setCoordinates(null));
        boolean isValid = validator.isValid(updateEventDto, constraintValidatorContext);
        assertFalse(isValid, "Validation should fail and return false for dto without link and coordinates");
    }

    @Test
    void validEventUpdate() {
        UpdateEventRequestDto updateEventDto = ModelUtils.getUpdateEventDto();
        assertTrue(validator.isValid(updateEventDto, constraintValidatorContext));
    }

    @Test
    void updateEventWithSameDates() {
        UpdateEventRequestDto updateEventDto = ModelUtils.getUpdateEventDto();
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(2L);
        updateEventDto.getDatesLocations().forEach(e -> e.setStartDate(zonedDateTime));
        updateEventDto.getDatesLocations().forEach(e -> e.setFinishDate(zonedDateTime));
        boolean isValid = validator.isValid(updateEventDto, constraintValidatorContext);
        assertFalse(isValid, "Validation should fail and return false for event with same dates");
    }

    @Test
    void invalidObjectType() {
        Object value = new Object();
        assertFalse(validator.isValid(value, constraintValidatorContext));
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
        boolean isValid = validator.isValid(updateEventRequestDto, constraintValidatorContext);
        assertFalse(isValid);
    }
}