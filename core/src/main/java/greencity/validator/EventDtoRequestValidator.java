package greencity.validator;

import greencity.annotations.ValidEventDtoRequest;
import greencity.constant.ErrorMessage;
import greencity.constant.ValidationConstants;
import greencity.dto.event.AbstractEventDateLocationDto;
import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.UpdateEventRequestDto;
import greencity.exception.exceptions.EventDtoValidationException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import static greencity.validator.UrlValidator.isUrlValid;

public class EventDtoRequestValidator
    implements ConstraintValidator<ValidEventDtoRequest, Object> {
    private static final int MAX_YEARS_OF_PLANNING = 10;

    /**
     * Validates whether the provided value adheres to the constraints defined for
     * an event DTO. The validation includes checking event date locations, ensuring
     * they fall within specified bounds, validating the tags count, and checking
     * the validity of online links. Time zones of the event start dates are
     * converted to Greenwich Mean Time (GMT) for comparison.
     *
     * @param value   The object to be validated, expected to be an instance of
     *                AddEventDtoRequest or UpdateEventDto.
     * @param context The context in which the validation is performed.
     * @return True if the value is valid according to the defined constraints,
     *         otherwise false.
     * @throws EventDtoValidationException if the value does not meet the validation
     *                                     criteria.
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            switch (value) {
                case AddEventDtoRequest addEventDtoRequest -> {
                    validateDateLocations(addEventDtoRequest.getDatesLocations());
                    convertToUTC(addEventDtoRequest.getDatesLocations());
                    validateEventDateLocations(addEventDtoRequest.getDatesLocations());
                    validateTags(addEventDtoRequest.getTags());
                }
                case UpdateEventRequestDto updateEventDto -> {
                    validateDateLocations(updateEventDto.getDatesLocations());
                    convertToUTC(updateEventDto.getDatesLocations());
                    validateEventDateLocations(updateEventDto.getDatesLocations());
                    validateTags(updateEventDto.getTags());
                }
                default -> {
                    return false;
                }
            }
            return true;
        } catch (EventDtoValidationException ex) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ex.getMessage()).addConstraintViolation();
            return false;
        }
    }

    private <T extends AbstractEventDateLocationDto> void validateDateLocations(List<T> dates) {
        if (dates == null || dates.isEmpty() || dates.size() > ValidationConstants.MAX_EVENT_DATES_AMOUNT) {
            throw new EventDtoValidationException(ErrorMessage.WRONG_COUNT_OF_EVENT_DATES);
        }

        dates.stream()
            .filter(date -> Objects.isNull(date.getStartDate()) || Objects.isNull(date.getFinishDate()))
            .findAny()
            .ifPresent(date -> {
                throw new EventDtoValidationException(ErrorMessage.INVALID_DATE);
            });
    }

    private <T extends AbstractEventDateLocationDto> void convertToUTC(List<T> dates) {
        dates.forEach(e -> {
            e.setStartDate(e.getStartDate().withZoneSameInstant(ZoneOffset.UTC));
            e.setFinishDate(e.getFinishDate().withZoneSameInstant(ZoneOffset.UTC));
        });
    }

    private <T extends AbstractEventDateLocationDto> void validateEventDateLocations(List<T> eventDateLocationDtos) {
        Set<LocalDateTime> startDateSet = new HashSet<>();
        Set<LocalDateTime> finishDateSet = new HashSet<>();

        for (T eventDateLocationDto : eventDateLocationDtos) {
            LocalDateTime startDate = eventDateLocationDto.getStartDate().toLocalDateTime();
            LocalDateTime finishDate = eventDateLocationDto.getFinishDate().toLocalDateTime();

            if (!startDateSet.add(startDate) || !finishDateSet.add(finishDate)) {
                throw new EventDtoValidationException(ErrorMessage.SAME_EVENT_DATES);
            }

            if (eventDateLocationDto.getStartDate().isBefore(ZonedDateTime.now(ZoneOffset.UTC))
                || eventDateLocationDto.getStartDate().isAfter(eventDateLocationDto.getFinishDate())
                || eventDateLocationDto.getStartDate().isAfter(ZonedDateTime.now(ZoneOffset.UTC)
                    .plusYears(MAX_YEARS_OF_PLANNING))) {
                throw new EventDtoValidationException(ErrorMessage.EVENT_START_DATE_AFTER_FINISH_DATE_OR_IN_PAST);
            }

            if (eventDateLocationDto.getOnlineLink() == null && eventDateLocationDto.getCoordinates() == null) {
                throw new EventDtoValidationException(ErrorMessage.NO_EVENT_LINK_OR_ADDRESS);
            }

            if (eventDateLocationDto.getOnlineLink() != null) {
                isUrlValid(eventDateLocationDto.getOnlineLink());
            }
        }
    }

    private void validateTags(List<String> tags) {
        int tagsSize = tags.size();
        if (tagsSize > ValidationConstants.MAX_AMOUNT_OF_TAGS) {
            throw new EventDtoValidationException(ErrorMessage.WRONG_COUNT_OF_TAGS_EXCEPTION);
        }
    }
}