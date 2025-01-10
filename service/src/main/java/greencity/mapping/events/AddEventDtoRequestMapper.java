package greencity.mapping.events;

import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.AddressDto;
import greencity.dto.event.EventDateLocationDto;
import greencity.entity.event.Event;
import greencity.entity.event.EventDateLocation;
import greencity.exception.exceptions.BadRequestException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map
 * {@link greencity.dto.event.AddEventDtoRequest} into {@link Event}.
 */
@Component(value = "eventDtoRequestMapper")
@RequiredArgsConstructor
public class AddEventDtoRequestMapper extends AbstractConverter<AddEventDtoRequest, Event> {
    private final AddressDtoMapper mapper;

    /**
     * Method for converting {@link greencity.dto.event.AddEventDtoRequest} into
     * {@link Event}.
     *
     * @param addEventDtoRequest object to convert.
     * @return converted object.
     */
    @Override
    public Event convert(AddEventDtoRequest addEventDtoRequest) {
        Event event = new Event();
        event.setTitle(addEventDtoRequest.getTitle());
        event.setDescription(addEventDtoRequest.getDescription());
        event.setCreationDate(LocalDate.now());
        event.setOpen(addEventDtoRequest.isOpen());

        List<EventDateLocation> eventDateLocations = new ArrayList<>();
        for (EventDateLocationDto date : addEventDtoRequest.getDatesLocations()) {
            if (date.getCoordinates() == null && date.getOnlineLink() == null
                || date.getCoordinates() != null && addressIsNotValid(date.getCoordinates())) {
                throw new BadRequestException("Coordinates or link should be set!");
            }
            EventDateLocation eventDateLocation = new EventDateLocation();
            eventDateLocation.setStartDate(date.getStartDate());
            eventDateLocation.setFinishDate(date.getFinishDate());
            if (date.getCoordinates() != null) {
                eventDateLocation.setAddress(mapper.convert(date.getCoordinates()));
            }
            if (date.getOnlineLink() != null) {
                eventDateLocation.setOnlineLink(date.getOnlineLink());
            }
            eventDateLocation.setEvent(event);
            eventDateLocations.add(eventDateLocation);
        }
        event.setDates(eventDateLocations);
        return event;
    }

    private boolean addressIsNotValid(AddressDto dto) {
        return dto.getRegionUa() == null || dto.getCountryUa() == null;
    }
}
