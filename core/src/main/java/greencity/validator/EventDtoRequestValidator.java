package greencity.validator;

import greencity.annotations.ValidEventDtoRequest;
import greencity.constant.ErrorMessage;
import greencity.constant.ValidationConstants;
import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.AddressDto;
import greencity.dto.event.EventDateLocationDto;
import greencity.dto.event.UpdateEventDto;
import greencity.exception.exceptions.EventDtoValidationException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.ZonedDateTime;
import java.util.List;

import static greencity.validator.UrlValidator.isUrlValid;

public class EventDtoRequestValidator implements ConstraintValidator<ValidEventDtoRequest, Object> {
    @Override
    public void initialize(ValidEventDtoRequest constraintAnnotation) {
        // Initializes the validator in preparation for #isValid calls
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value instanceof AddEventDtoRequest) {
            AddEventDtoRequest addEventDtoRequest = (AddEventDtoRequest) value;
            List<EventDateLocationDto> eventDateLocationDtos = addEventDtoRequest.getDatesLocations();

            if (eventDateLocationDtos == null || eventDateLocationDtos.isEmpty()
                || eventDateLocationDtos.size() > ValidationConstants.MAX_EVENT_DATES_AMOUNT) {
                throw new EventDtoValidationException(ErrorMessage.WRONG_COUNT_OF_EVENT_DATES);
            }

            for (var eventDateLocationDto : eventDateLocationDtos) {
                if (!validateEventDateLocation(eventDateLocationDto, context)) {
                    return false;
                }

                if (eventDateLocationDto.getStartDate().isBefore(ZonedDateTime.now())
                    || eventDateLocationDto.getStartDate().isAfter(eventDateLocationDto.getFinishDate())) {
                    throw new EventDtoValidationException(ErrorMessage.EVENT_START_DATE_AFTER_FINISH_DATE_OR_IN_PAST);
                }

                AddressDto addressDto = eventDateLocationDto.getCoordinates();
                String onlineLink = eventDateLocationDto.getOnlineLink();
                if (onlineLink == null && addressDto == null) {
                    throw new EventDtoValidationException(ErrorMessage.NO_EVENT_LINK_OR_ADDRESS);
                }
                if (onlineLink != null) {
                    isUrlValid(onlineLink);
                }
            }

            if (addEventDtoRequest.getTags().size() > ValidationConstants.MAX_AMOUNT_OF_TAGS) {
                throw new EventDtoValidationException(ErrorMessage.WRONG_COUNT_OF_TAGS_EXCEPTION);
            }

            return true;
        } else if (value instanceof UpdateEventDto) {
            UpdateEventDto updateEventDto = (UpdateEventDto) value;
            List<EventDateLocationDto> eventDateLocationDtos = updateEventDto.getDatesLocations();

            for (var eventDateLocationDto : eventDateLocationDtos) {
                if (!validateEventDateLocation(eventDateLocationDto, context)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    private boolean validateEventDateLocation(EventDateLocationDto eventDateLocationDto,
        ConstraintValidatorContext context) {
        if (eventDateLocationDto.getStartDate() == null) {
            context.buildConstraintViolationWithTemplate(ErrorMessage.EMPTY_START_DATE)
                .addPropertyNode("startDate")
                .addConstraintViolation();
            return false;
        }

        if (eventDateLocationDto.getFinishDate() == null) {
            context.buildConstraintViolationWithTemplate(ErrorMessage.EMPTY_FINISH_DATE)
                .addPropertyNode("finishDate")
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}