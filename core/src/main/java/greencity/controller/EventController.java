package greencity.controller;

import greencity.annotations.ApiPageableWithoutSort;
import greencity.annotations.CurrentUser;
import greencity.annotations.ImageArrayValidation;
import greencity.annotations.ValidEventDtoRequest;
import greencity.constant.ErrorMessage;
import greencity.constant.HttpStatuses;
import greencity.constant.SwaggerExampleModel;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.AddressDto;
import greencity.dto.event.EventAttenderDto;
import greencity.dto.event.EventDto;
import greencity.dto.event.EventResponseDto;
import greencity.dto.event.UpdateEventRequestDto;
import greencity.dto.filter.FilterEventDto;
import greencity.dto.user.UserVO;
import greencity.enums.EventStatus;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.WrongIdException;
import greencity.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import static greencity.constant.SwaggerExampleModel.UPDATE_EVENT;

@Slf4j
@Validated
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {
    private static final List<EventStatus> STATUSES_THAT_REQUIRE_USER_ID =
        List.of(EventStatus.JOINED, EventStatus.CREATED, EventStatus.SAVED);
    private final EventService eventService;

    /**
     * Method for creating an event.
     *
     * @return {@link EventDto} instance.
     * @author Max Bohonko, Danylo Hlynskyi.
     */
    @Operation(summary = "Create new event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventDto> save(
        @Parameter(description = SwaggerExampleModel.ADD_EVENT,
            required = true) @ValidEventDtoRequest @RequestPart AddEventDtoRequest addEventDtoRequest,
        @Parameter(hidden = true) Principal principal,
        @RequestPart(required = false) @Nullable @ImageArrayValidation(
            allowedTypes = {"image/jpeg", "image/png", "image/jpg"}) MultipartFile[] images) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(eventService.save(addEventDtoRequest, principal.getName(), images));
    }

    /**
     * Method for deleting an event.
     *
     * @author Max Bohonko.
     */
    @Operation(summary = "Delete event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Object> delete(@PathVariable Long eventId, @Parameter(hidden = true) Principal principal) {
        eventService.delete(eventId, principal.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * Method for updating {@link EventDto}.
     *
     * @author Danylo Hlynskyi
     */
    @Operation(summary = "Update event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = EventDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PutMapping(value = "/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventDto> update(
        @Parameter(required = true,
            description = UPDATE_EVENT) @ValidEventDtoRequest @RequestPart UpdateEventRequestDto eventDto,
        @Parameter(hidden = true) Principal principal,
        @RequestPart(required = false) @Nullable MultipartFile[] images,
        @PathVariable Long eventId) {
        if (!eventId.equals(eventDto.getId())) {
            throw new WrongIdException(ErrorMessage.EVENT_ID_IN_PATH_PARAM_AND_ENTITY_NOT_EQUAL);
        }
        return ResponseEntity.ok().body(eventService.update(eventDto, principal.getName(), images));
    }

    /**
     * Method for getting the event by event id.
     *
     * @return {@link EventDto} instance.
     * @author Max Bohonko.
     */
    @Operation(summary = "Get the event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getEvent(
        @PathVariable Long eventId,
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.ok().body(eventService.getEvent(eventId, principal));
    }

    /**
     * Method for getting the event by event id version 2.
     *
     * @return {@link EventResponseDto} instance.
     * @author Yurii Osovskyi.
     */
    @Operation(summary = "Get the event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/v2/{eventId}")
    public ResponseEntity<EventResponseDto> getEventV2(
        @PathVariable Long eventId,
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.ok().body(eventService.getEventV2(eventId, principal));
    }

    /**
     * Method for getting pages of events.
     *
     * @return a page of {@link EventDto} instance.
     */
    @Operation(summary = "Get all events")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST)))
    })
    @ApiPageableWithoutSort
    @GetMapping
    public ResponseEntity<PageableAdvancedDto<EventDto>> getEvents(
        @Parameter(hidden = true) Pageable pageable,
        @RequestParam(required = false, name = "user-id") Long userId,
        @Schema(
            description = "Filters for events",
            name = "FilterEventDto",
            type = "object",
            example = FilterEventDto.defaultJson) FilterEventDto filterEventDto) {
        if (filterEventDto != null && filterEventDto.getStatuses() != null) {
            validateStatusesRequireUserId(filterEventDto.getStatuses(), userId);
        }
        return ResponseEntity.ok().body(eventService.getEvents(pageable, filterEventDto, userId));
    }

    private void validateStatusesRequireUserId(List<EventStatus> statuses, Long userId) {
        if (statuses.stream().anyMatch(STATUSES_THAT_REQUIRE_USER_ID::contains) && userId == null) {
            throw new BadRequestException(ErrorMessage.STATUSES_REQUIRE_USER_ID);
        }
    }

    /**
     * Method for adding an attender to the event.
     *
     * @author Max Bohonko.
     */
    @Operation(summary = "Add an attender to the event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/{eventId}/attenders")
    public void addAttender(@PathVariable Long eventId, @Parameter(hidden = true) Principal principal) {
        eventService.addAttender(eventId, principal.getName());
    }

    /**
     * Method for removing an attender from the event.
     *
     * @author Max Bohonko.
     */
    @Operation(summary = "Remove an attender from the event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @DeleteMapping("/{eventId}/attenders")
    public ResponseEntity<Object> removeAttender(
        @PathVariable Long eventId,
        @Parameter(hidden = true) Principal principal) {
        eventService.removeAttender(eventId, principal.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * Method for adding an event to favorites by event id.
     *
     * @author Anton Bondar.
     */
    @Operation(summary = "Add an event to favorites")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/{eventId}/favorites")
    public ResponseEntity<Object> addToFavorites(@PathVariable Long eventId,
        @Parameter(hidden = true) Principal principal) {
        eventService.addToFavorites(eventId, principal.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * Method for removing an event from favorites by event id.
     *
     * @author Anton Bondar.
     */
    @Operation(summary = "Remove an event from favorites")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @DeleteMapping("/{eventId}/favorites")
    public ResponseEntity<Object> removeFromFavorites(@PathVariable Long eventId,
        @Parameter(hidden = true) Principal principal) {
        eventService.removeFromFavorites(eventId, principal.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * Method to like/unlike Event.
     *
     * @author Roman Kasarab
     */
    @Operation(summary = "Like/unlike Event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/{eventId}/like")
    public void like(@PathVariable Long eventId, @Parameter(hidden = true) @CurrentUser UserVO user) {
        eventService.like(eventId, user);
    }

    /**
     * Method to dislike Event.
     */
    @Operation(description = "Dislike event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @PostMapping("/{eventId}/dislike")
    public void dislike(@PathVariable Long eventId, @Parameter(hidden = true) @CurrentUser UserVO user) {
        eventService.dislike(user, eventId);
    }

    /**
     * Method to get amount of likes by event id.
     *
     * @return count of likes for event;
     */
    @Operation(summary = "Count likes by id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/{eventId}/likes/count")
    public ResponseEntity<Integer> countLikes(@PathVariable Long eventId) {
        return ResponseEntity.status(HttpStatus.OK).body(eventService.countLikes(eventId));
    }

    /**
     * Method to get amount of dislikes by event id.
     *
     * @return count of dislikes for event;
     */
    @Operation(summary = "Count dislikes by id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/{eventId}/dislikes/count")
    public ResponseEntity<Integer> countDislikes(@PathVariable Long eventId) {
        return ResponseEntity.status(HttpStatus.OK).body(eventService.countDislikes(eventId));
    }

    /**
     * Check if user liked event.
     *
     * @return user liked event or not.
     */
    @Operation(summary = "Check if user liked event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/{eventId}/likes")
    public ResponseEntity<Boolean> isEventLikedByUser(
        @PathVariable Long eventId, @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity.status(HttpStatus.OK).body(eventService.isEventLikedByUser(eventId, userVO));
    }

    /**
     * Check if user disliked event.
     *
     * @return user disliked event or not.
     */
    @Operation(summary = "Check if user disliked event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/{eventId}/dislikes")
    public ResponseEntity<Boolean> isEventDislikedByUser(
        @PathVariable Long eventId, @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity.status(HttpStatus.OK).body(eventService.isEventDislikedByUser(eventId, userVO));
    }

    /**
     * Method for rating event by user.
     *
     * @author Danylo Hlynskyi.
     */
    @Operation(summary = "Rate event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/{eventId}/ratings")
    public ResponseEntity<Object> rateEvent(
        @PathVariable Long eventId,
        @RequestBody @NotNull @Positive @Max(3) Integer grade,
        @Parameter(hidden = true) Principal principal) {
        eventService.rateEvent(eventId, principal.getName(), grade);
        return ResponseEntity.ok().build();
    }

    /**
     * Method for getting all event attenders.
     *
     * @return a page of {@link EventAttenderDto} instance.
     * @author Danylo Hlynskyi.
     */
    @Operation(summary = "Get all event attenders")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/{eventId}/attenders")
    public ResponseEntity<Set<EventAttenderDto>> getAllEventSubscribers(@PathVariable Long eventId) {
        return ResponseEntity.ok().body(eventService.getAllEventAttenders(eventId));
    }

    /**
     * Method for getting all events addresses.
     *
     * @return a list of {@link AddressDto} instance.
     * @author Olena Sotnik.
     */
    @Operation(summary = "Get all events addresses")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST)))
    })
    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDto>> getAllEventsAddresses() {
        return ResponseEntity.ok().body(eventService.getAllEventsAddresses());
    }

    /**
     * The method finds count of events attended by user id.
     *
     * @param userId {@link Long} id of current user.
     * @return {@link Long} count of attended events by user id.
     */
    @Operation(summary = "Finds amount of events where user is attender")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/attenders/count")
    public ResponseEntity<Long> getAllAttendersCount(@RequestParam(name = "user-id") Long userId) {
        return ResponseEntity.ok().body(eventService.getCountOfAttendedEventsByUserId(userId));
    }

    /**
     * The method finds count of events organized by user id.
     *
     * @param userId {@link Long} id of current user.
     * @return {@link Long} count of organized events by user id.
     */
    @Operation(summary = "Finds amount of events where user is organizer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/organizers/count")
    public ResponseEntity<Long> getOrganizersCount(@RequestParam(name = "user-id") Long userId) {
        return ResponseEntity.ok().body(eventService.getCountOfOrganizedEventsByUserId(userId));
    }
}
