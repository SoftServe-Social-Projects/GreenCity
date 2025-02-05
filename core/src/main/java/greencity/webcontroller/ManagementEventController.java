package greencity.webcontroller;

import greencity.annotations.ApiLocale;
import greencity.annotations.ValidEventDtoRequest;
import greencity.client.RestClient;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.event.AbstractEventDateLocationDto;
import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.EventAttenderDto;
import greencity.dto.event.EventDateLocationDto;
import greencity.dto.event.EventDto;
import greencity.dto.event.UpdateEventRequestDto;
import greencity.dto.filter.FilterEventDto;
import greencity.dto.user.UserProfilePictureDto;
import greencity.enums.TagType;
import greencity.service.EventService;
import greencity.service.TagsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import static greencity.constant.ErrorMessage.DATES_COULD_NOT_BE_NULL;
import static greencity.constant.ErrorMessage.DATES_LIST_COULD_NOT_BE_EMPTY;
import static greencity.constant.ManagementConstant.ADD_EVENT_DTO_REQUEST;
import static greencity.constant.ManagementConstant.ATTENDERS_PAGE;
import static greencity.constant.ManagementConstant.AUTHOR;
import static greencity.constant.ManagementConstant.BACKEND_ADDRESS_ATTRIBUTE;
import static greencity.constant.ManagementConstant.CITIES;
import static greencity.constant.ManagementConstant.DATE_TIME_FORMATTER;
import static greencity.constant.ManagementConstant.EVENT_ATTENDERS;
import static greencity.constant.ManagementConstant.EVENT_ATTENDERS_AVATARS;
import static greencity.constant.ManagementConstant.EVENT_DTO;
import static greencity.constant.ManagementConstant.EVENT_TAGS;
import static greencity.constant.ManagementConstant.FILTER_EVENT_DTO;
import static greencity.constant.ManagementConstant.FORMATTED_DATE;
import static greencity.constant.ManagementConstant.GOOGLE_MAP_API_KEY;
import static greencity.constant.ManagementConstant.IMAGES;
import static greencity.constant.ManagementConstant.IMAGE_URLS;
import static greencity.constant.ManagementConstant.PAGEABLE;
import static greencity.constant.ManagementConstant.PAGE_SIZE;
import static greencity.constant.ManagementConstant.SORT_MODEL;
import static greencity.constant.ManagementConstant.USERS_DISLIKED_PAGE;
import static greencity.constant.ManagementConstant.USERS_LIKED_PAGE;
import static greencity.constant.SwaggerExampleModel.UPDATE_EVENT;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/management/events")
public class ManagementEventController {
    private final EventService eventService;
    private final TagsService tagsService;
    private final RestClient restClient;
    private final ModelMapper getModelMapper;

    @Value("${google.maps.api.key}")
    private String googleMapApiKey;

    @Value("${address}")
    private String backendAddress;

    /**
     * Method that returns management page with all {@link EventDto}.
     *
     * @param model    Model that will be configured and returned to user.
     * @param pageable {@link Pageable}.
     * @return View template path {@link String}.
     */
    @GetMapping
    @ApiLocale
    public String getAllEvents(@RequestParam(required = false, name = "query") String query,
        Model model,
        @Parameter(hidden = true) Pageable pageable,
        FilterEventDto filterEventDto,
        @Parameter(hidden = true) Locale locale) {
        PageableAdvancedDto<EventDto> allEvents;
        if (query != null && !query.isEmpty()) {
            allEvents = eventService.searchEventsBy(pageable, query);
        } else {
            allEvents = eventService.getEventsManagement(pageable, filterEventDto, null);
        }
        model.addAttribute(PAGEABLE, allEvents);
        Sort sort = pageable.getSort();
        StringBuilder orderUrl = new StringBuilder();
        if (!sort.isEmpty()) {
            for (Sort.Order order : sort) {
                if (!orderUrl.isEmpty()) {
                    orderUrl.append("&");
                }
                orderUrl.append("sort=").append(order.getProperty()).append(",").append(order.getDirection().name());
            }
        }
        model.addAttribute(FILTER_EVENT_DTO, filterEventDto);
        model.addAttribute(SORT_MODEL, orderUrl.toString());
        model.addAttribute(EVENT_TAGS, tagsService.findByTypeAndLanguageCode(TagType.EVENT, locale.getLanguage()));
        model.addAttribute(PAGE_SIZE, pageable.getPageSize());
        model.addAttribute(BACKEND_ADDRESS_ATTRIBUTE, backendAddress);
        model.addAttribute(CITIES,
            eventService.getAllEventsAddresses().stream()
                .map(e -> "en".equals(locale.getLanguage()) ? e.getCityEn() : e.getCityUa())
                .distinct()
                .toList());

        return "core/management_events";
    }

    @GetMapping("/{eventId}")
    public String getEvent(Model model, @PathVariable("eventId") Long eventId,
        @Parameter(hidden = true) Principal principal) {
        EventDto eventDto = eventService.getEvent(eventId, principal);
        Set<EventAttenderDto> eventAttenders = eventService.getAllEventAttenders(eventId);
        List<String> attendersAvatars = eventAttenders.stream()
            .map(EventAttenderDto::getImagePath)
            .filter(Objects::nonNull)
            .toList();
        model.addAttribute(EVENT_DTO, eventDto);
        model.addAttribute(FORMATTED_DATE, getFormattedDates(eventDto.getDates()));
        model.addAttribute(IMAGE_URLS, getImageUrls(eventDto));
        model.addAttribute(EVENT_ATTENDERS, eventAttenders);
        model.addAttribute(EVENT_ATTENDERS_AVATARS, attendersAvatars);
        return "core/management_event";
    }

    @GetMapping("/{eventId}/attenders")
    public String getAttenders(@PathVariable("eventId") Long eventId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EventAttenderDto> attandersPage = eventService.getAttendersPage(eventId, pageable);
        model.addAttribute(ATTENDERS_PAGE, attandersPage);
        return "core/fragments/attenders-table";
    }

    @GetMapping("/{eventId}/likes")
    public String getUsersLikedEvent(@PathVariable("eventId") Long eventId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserProfilePictureDto> usersLikedPage = eventService.getUsersLikedEventPage(eventId, pageable);
        model.addAttribute(USERS_LIKED_PAGE, usersLikedPage);
        return "core/fragments/likes-table";
    }

    @GetMapping("/{eventId}/dislikes")
    public String getUsersDislikedEvent(@PathVariable("eventId") Long eventId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserProfilePictureDto> usersDislikedPage = eventService.getUsersDislikedEventPage(eventId, pageable);
        model.addAttribute(USERS_DISLIKED_PAGE, usersDislikedPage);
        return "core/fragments/dislikes-table";
    }

    @GetMapping("/create-event")
    public String getEventCreatePage(Model model, Principal principal) {
        model.addAttribute(ADD_EVENT_DTO_REQUEST, new AddEventDtoRequest());
        model.addAttribute(IMAGES, new MultipartFile[] {});
        model.addAttribute(BACKEND_ADDRESS_ATTRIBUTE, backendAddress);
        model.addAttribute(AUTHOR, restClient.findByEmail(principal.getName()).getName());
        model.addAttribute(GOOGLE_MAP_API_KEY, googleMapApiKey);
        return "core/management_create_event";
    }

    @PostMapping
    public String createEvent(@RequestPart("addEventDtoRequest") AddEventDtoRequest addEventDtoRequest,
        @RequestPart("images") MultipartFile[] images,
        Principal principal,
        Model model) {
        model.addAttribute(ADD_EVENT_DTO_REQUEST, new AddEventDtoRequest());
        model.addAttribute(IMAGES, new MultipartFile[] {});
        eventService.save(addEventDtoRequest, principal.getName(), images);
        return "redirect:/management/events";
    }

    @DeleteMapping
    public ResponseEntity<Object> delete(@RequestBody List<Long> ids, Principal principal) {
        String email = principal.getName();
        for (Long id : ids) {
            eventService.delete(id, email);
        }
        return ResponseEntity.ok().build();
    }

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
    @PutMapping(
        consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<EventDto> update(
        @Parameter(required = true,
            description = UPDATE_EVENT) @ValidEventDtoRequest @RequestPart UpdateEventRequestDto eventDto,
        @Parameter(hidden = true) Principal principal,
        @RequestPart(required = false) @Nullable MultipartFile[] images) {
        return ResponseEntity.status(HttpStatus.OK).body(
            eventService.update(eventDto, principal.getName(), images));
    }

    @GetMapping("/edit/{id}")
    public String editEvent(@PathVariable("id") Long id, Model model, Principal principal) {
        model.addAttribute(BACKEND_ADDRESS_ATTRIBUTE, backendAddress);
        model.addAttribute(AUTHOR, restClient.findByEmail(principal.getName()).getName());
        model.addAttribute(EVENT_DTO, eventService.getEvent(id, principal));
        model.addAttribute(GOOGLE_MAP_API_KEY, googleMapApiKey);
        return "core/management_edit_event";
    }

    private String getFormattedDates(List<EventDateLocationDto> dates) {
        if (Objects.isNull(dates)) {
            throw new IllegalArgumentException(DATES_COULD_NOT_BE_NULL);
        }

        EventDateLocationDto firstDateDto = dates.stream()
            .min(Comparator.comparing(AbstractEventDateLocationDto::getStartDate))
            .orElseThrow(() -> new IllegalArgumentException(DATES_LIST_COULD_NOT_BE_EMPTY));

        EventDateLocationDto lastDateDto = dates.stream()
            .max(Comparator.comparing(AbstractEventDateLocationDto::getFinishDate))
            .orElseThrow(() -> new IllegalArgumentException(DATES_LIST_COULD_NOT_BE_EMPTY));

        if (firstDateDto.getStartDate().toLocalDate().equals(lastDateDto.getStartDate().toLocalDate())) {
            return firstDateDto.getStartDate().format(DATE_TIME_FORMATTER);
        } else {
            return firstDateDto.getStartDate().format(DATE_TIME_FORMATTER) + " - "
                + lastDateDto.getFinishDate().format(DATE_TIME_FORMATTER);
        }
    }

    private List<String> getImageUrls(EventDto eventDto) {
        List<String> urls = new ArrayList<>();
        urls.add(eventDto.getTitleImage());
        urls.addAll(Objects.nonNull(eventDto.getAdditionalImages()) ? eventDto.getAdditionalImages() : List.of());
        return urls;
    }
}
