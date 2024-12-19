package greencity.controller;

import greencity.annotations.ApiPageableWithoutSort;
import greencity.annotations.ValidEventDtoRequest;
import greencity.constant.HttpStatuses;
import greencity.constant.SwaggerExampleModel;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.EventAttenderDto;
import greencity.dto.event.EventDto;
import greencity.dto.event.UpdateEventDto;
import greencity.dto.event.AddressDto;
import greencity.dto.filter.FilterEventDto;
import greencity.service.EventService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventsController {
    private final EventService eventService;

    /**
     * Method for creating an event.
     *
     * @return {@link EventDto} instance.
     * @author Max Bohonko, Danylo Hlynskyi.
     */
    @ApiOperation(value = "Create new event")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/create",
        consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<EventDto> save(
        @ApiParam(value = SwaggerExampleModel.ADD_EVENT,
            required = true) @ValidEventDtoRequest @RequestPart AddEventDtoRequest addEventDtoRequest,
        @ApiIgnore Principal principal,
        @RequestPart(required = false) @Nullable MultipartFile[] images) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(eventService.save(addEventDtoRequest, principal.getName(), images));
    }

    /**
     * Method for deleting an event.
     *
     * @author Max Bohonko.
     */
    @ApiOperation(value = "Delete event")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/delete/{eventId}")
    public ResponseEntity<Object> delete(@PathVariable Long eventId, @ApiIgnore Principal principal) {
        eventService.delete(eventId, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for updating {@link EventDto}.
     *
     * @author Danylo Hlynskyi
     */
    @ApiOperation(value = "Update event")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = EventDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PutMapping(value = "/update",
        consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<EventDto> update(
        @ApiParam(required = true,
            value = SwaggerExampleModel.UPDATE_EVENT) @ValidEventDtoRequest @RequestPart UpdateEventDto eventDto,
        @ApiIgnore Principal principal,
        @RequestPart(required = false) @Nullable MultipartFile[] images) {
        return ResponseEntity.status(HttpStatus.OK).body(
            eventService.update(eventDto, principal.getName(), images));
    }

    /**
     * Method for getting the event by event id.
     *
     * @return {@link EventDto} instance.
     * @author Max Bohonko.
     */
    @ApiOperation(value = "Get the event")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/event/{eventId}")
    public ResponseEntity<EventDto> getEvent(@PathVariable Long eventId, @ApiIgnore Principal principal) {
        return ResponseEntity.status(HttpStatus.OK).body(eventService.getEvent(eventId, principal));
    }

    /**
     * Method for getting pages of events.
     *
     * @return a page of {@link EventDto} instance.
     * @author Max Bohonko, Olena Sotnik.
     */
    @ApiOperation(value = "Get all events")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST)
    })
    @ApiPageableWithoutSort
    @GetMapping
    public ResponseEntity<PageableAdvancedDto<EventDto>> getEvent(
        @ApiIgnore Pageable pageable, @ApiIgnore Principal principal, FilterEventDto filterEventDto) {
        return ResponseEntity.status(HttpStatus.OK).body(eventService.getEvents(pageable, principal, filterEventDto));
    }

    /**
     * Method for getting pages of users events sorted by dates if online and by
     * closeness to coordinates of User if offline.
     *
     * @return a page of {@link EventDto} instance.
     * @author Danylo Hlysnkyi, Olena Sotnik.
     */
    @ApiOperation(value = "Get all users events")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @ApiPageableWithoutSort
    @GetMapping("/myEvents")
    public ResponseEntity<PageableAdvancedDto<EventDto>> getUserEvents(
        @ApiIgnore Pageable pageable, @ApiIgnore Principal principal,
        @RequestParam(required = false) String eventType,
        @RequestParam(required = false) String userLatitude,
        @RequestParam(required = false) String userLongitude) {
        return ResponseEntity.status(HttpStatus.OK).body(eventService.getAllUserEvents(
            pageable, principal.getName(), userLatitude, userLongitude, eventType));
    }

    /**
     * Method for getting all user's favorite events.
     *
     * @return a set of {@link EventDto} instance.
     * @author Midianyi Yurii
     */
    @ApiOperation(value = "Get all user's favorite events")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @ApiPageableWithoutSort
    @GetMapping("/getAllFavoriteEvents")
    public ResponseEntity<PageableAdvancedDto<EventDto>> getAllFavoriteEventsByUser(
        @ApiIgnore Pageable pageable, @ApiIgnore Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(eventService.getAllFavoriteEventsByUser(pageable, principal.getName()));
    }

    /**
     * Method for getting page of events which were created user.
     *
     * @return a page of{@link EventDto} instance.
     * @author Nikita Korzh.
     */
    @ApiOperation(value = "Get events created by user")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @ApiPageableWithoutSort
    @GetMapping("/myEvents/createdEvents")
    public ResponseEntity<PageableAdvancedDto<EventDto>> getEventsCreatedByUser(
        @ApiIgnore Pageable pageable, @ApiIgnore Principal principal) {
        return ResponseEntity.status(HttpStatus.OK).body(eventService
            .getEventsCreatedByUser(pageable, principal.getName()));
    }

    /**
     * Method for getting pages of users events and events which were created by
     * this user.
     *
     * @return a page of {@link EventDto} instance.
     * @author Oliyarnik Serhii.
     */
    @ApiOperation(value = "Get all users events and events which were created by this user")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @ApiPageableWithoutSort
    @GetMapping("/myEvents/relatedEvents")
    public ResponseEntity<PageableAdvancedDto<EventDto>> getRelatedToUserEvents(@ApiIgnore Pageable pageable,
        @ApiIgnore Principal principal) {
        return ResponseEntity.status(HttpStatus.OK).body(eventService
            .getRelatedToUserEvents(pageable, principal.getName()));
    }

    /**
     * Method for adding an attender to the event.
     *
     * @author Max Bohonko.
     */
    @ApiOperation(value = "Add an attender to the event")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/addAttender/{eventId}")
    public void addAttender(@PathVariable Long eventId, @ApiIgnore Principal principal) {
        eventService.addAttender(eventId, principal.getName());
    }

    /**
     * Method for removing an attender from the event.
     *
     * @author Max Bohonko.
     */
    @ApiOperation(value = "Remove an attender from the event")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/removeAttender/{eventId}")
    public ResponseEntity<Object> removeAttender(@PathVariable Long eventId, @ApiIgnore Principal principal) {
        eventService.removeAttender(eventId, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for adding an event to favorites by event id.
     *
     * @author Anton Bondar.
     */
    @ApiOperation(value = "Add an event to favorites")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/addToFavorites/{eventId}")
    public ResponseEntity<Object> addToFavorites(@PathVariable Long eventId, @ApiIgnore Principal principal) {
        eventService.addToFavorites(eventId, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for removing an event from favorites by event id.
     *
     * @author Anton Bondar.
     */
    @ApiOperation(value = "Remove an event from favorites")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/removeFromFavorites/{eventId}")
    public ResponseEntity<Object> removeFromFavorites(@PathVariable Long eventId, @ApiIgnore Principal principal) {
        eventService.removeFromFavorites(eventId, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for rating event by user.
     *
     * @author Danylo Hlynskyi.
     */
    @ApiOperation(value = "Rate event")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/rateEvent/{eventId}/{grade}")
    public ResponseEntity<Object> rateEvent(@PathVariable Long eventId, @PathVariable int grade,
        @ApiIgnore Principal principal) {
        eventService.rateEvent(eventId, principal.getName(), grade);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for getting all event attenders.
     *
     * @return a page of {@link EventAttenderDto} instance.
     * @author Danylo Hlynskyi.
     */
    @ApiOperation(value = "Get all event attenders")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/getAllSubscribers/{eventId}")
    public ResponseEntity<Set<EventAttenderDto>> getAllEventSubscribers(@PathVariable Long eventId) {
        return ResponseEntity.status(HttpStatus.OK).body(eventService.getAllEventAttenders(eventId));
    }

    /**
     * Method for getting all events addresses.
     *
     * @return a set of {@link AddressDto} instance.
     * @author Olena Sotnik.
     */
    @ApiOperation(value = "Get all events addresses")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST)
    })
    @GetMapping("/addresses")
    public ResponseEntity<Set<AddressDto>> getAllEventsAddresses() {
        return ResponseEntity.status(HttpStatus.OK).body(eventService.getAllEventsAddresses());
    }

    /**
     * The method finds count of events organized and attended by user id.
     *
     * @param userId {@link Long} id of current user.
     * @return {@link Long} count of organized and attended events by user id.
     *
     * @author Olena Sotnik
     */
    @ApiOperation(value = "Finds amount of events where user is organizer or attender")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/userEvents/count")
    public ResponseEntity<Long> findAmountOfOrganizedAndAttendedEvents(@RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(eventService.getAmountOfOrganizedAndAttendedEventsByUserId(userId));
    }
}
