package greencity.mapping.events;

import greencity.dto.event.EventDateLocationDto;
import greencity.dto.event.EventDto;
import greencity.entity.event.Event;
import greencity.entity.event.EventDateLocation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class that used by {@link ModelMapper} to map {@link Event} into
 * {@link EventDto}.
 */
@Component
@RequiredArgsConstructor
public class EventDateLocationDtoMapper extends AbstractConverter<EventDateLocationDto, EventDateLocation> {
    private final AddressDtoMapper mapper;

    /**
     * Method for converting {@link EventDateLocationDto} into
     * {@link EventDateLocation}.
     *
     * @param eventDateLocationDto object to convert.
     * @return converted object.
     */
    @Override
    public EventDateLocation convert(EventDateLocationDto eventDateLocationDto) {
        EventDateLocation eventDateLocation = new EventDateLocation();
        eventDateLocation.setStartDate(eventDateLocationDto.getStartDate());
        eventDateLocation.setFinishDate(eventDateLocationDto.getFinishDate());
        eventDateLocation.setId(eventDateLocationDto.getId());
        if (eventDateLocationDto.getOnlineLink() != null) {
            eventDateLocation.setOnlineLink(eventDateLocationDto.getOnlineLink());
        }
        if (eventDateLocationDto.getCoordinates() != null) {
            eventDateLocation.setAddress(mapper.convert(eventDateLocationDto.getCoordinates()));
        }
        return eventDateLocation;
    }

    /**
     * Method that build {@link List} of {@link EventDateLocation} from {@link List}
     * of {@link EventDateLocationDto}.
     *
     * @param eventDateLocationDtoList {@link List} of {@link EventDateLocationDto}
     *
     * @return {@link List} of {@link EventDateLocation}
     */
    public List<EventDateLocation> mapAllToList(List<EventDateLocationDto> eventDateLocationDtoList) {
        return eventDateLocationDtoList.stream().map(this::convert).collect(Collectors.toList());
    }
}
