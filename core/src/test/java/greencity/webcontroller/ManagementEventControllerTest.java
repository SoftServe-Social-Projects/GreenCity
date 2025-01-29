package greencity.webcontroller;

import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.EventAttenderDto;
import greencity.dto.event.EventDateLocationDto;
import greencity.dto.event.EventDto;
import greencity.dto.event.UpdateEventRequestDto;
import greencity.dto.filter.FilterEventDto;
import greencity.dto.tag.TagDto;
import greencity.dto.user.UserVO;
import greencity.entity.event.Event;
import greencity.enums.TagType;
import greencity.repository.EventRepo;
import greencity.service.EventService;
import greencity.service.TagsService;

import java.security.Principal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManagementEventControllerTest {
    private static final String MANAGEMENT_EVENTS_LINK = "/management/events";
    @Mock
    EventService eventService;
    @Mock
    EventRepo eventRepo;
    @InjectMocks
    ManagementEventController managementEventController;
    private MockMvc mockMvc;
    @Mock
    private TagsService tagsService;
    @Mock
    private RestClient restClient;
    @Mock
    private Principal principal;
    @Mock
    private Validator mockValidator;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(managementEventController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setValidator(mockValidator)
            .build();
    }

    @Test
    @SneakyThrows
    void getAllEventsWithQuery() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Event> events = List.of(new Event(), new Event());
        PageRequest pageRequest = PageRequest.of(0, 2);
        Page<Event> page = new PageImpl<>(events, pageRequest, events.size());
        List<EventDto> eventDtos = Collections.singletonList(new EventDto().setTitle("title"));
        PageableAdvancedDto<EventDto> eventsDtoPageableDto =
            new PageableAdvancedDto<>(eventDtos, 2, 0, 3, 0, true, true, true, true);
        List<TagDto> tagDtoList = Collections.singletonList(TagDto.builder()
            .id(1L)
            .name("Social").build());

        when(tagsService.findByTypeAndLanguageCode(TagType.EVENT, "en")).thenReturn(tagDtoList);
        when(eventRepo.searchEventsBy(pageable, "title")).thenReturn(page);
        when(eventService.searchEventsBy(pageable, "title")).thenReturn(eventsDtoPageableDto);
        when(eventService.getAllEventsAddresses()).thenReturn(ModelUtils.getAddressesDtoList());

        this.mockMvc.perform(get(MANAGEMENT_EVENTS_LINK)
            .param("query", "title")
            .param("page", "0")
            .locale(Locale.ENGLISH)
            .param("size", "10"))
            .andExpect(view().name("core/management_events"))
            .andExpect(model().attribute("pageable", eventsDtoPageableDto))
            .andExpect(model().attribute("cities", List.of("Dnipro", "Lviv")))
            .andExpect(status().isOk());

        verify(eventService).searchEventsBy(pageable, "title");
    }

    @Test
    @SneakyThrows
    void getAllEventsWithFilterEventDto() {
        Pageable pageable = PageRequest.of(0, 10);
        FilterEventDto filterEventDto = new FilterEventDto();
        List<EventDto> eventDtos = Collections.singletonList(new EventDto());
        PageableAdvancedDto<EventDto> eventsDtoPageableDto =
            new PageableAdvancedDto<>(eventDtos, 1, 0, 1, 0, true, false, true, true);

        List<TagDto> tagDtoList = Collections.singletonList(
            TagDto.builder().id(1L).name("Social").build());

        when(eventService.getEventsManagement(pageable, filterEventDto, null))
            .thenReturn(eventsDtoPageableDto);
        when(tagsService.findByTypeAndLanguageCode(TagType.EVENT, "ua"))
            .thenReturn(tagDtoList);
        when(eventService.getAllEventsAddresses())
            .thenReturn(ModelUtils.getAddressesDtoList());

        this.mockMvc.perform(get(MANAGEMENT_EVENTS_LINK)
            .param("page", "0")
            .param("size", "10")
            .locale(Locale.of("UA", "ua")))
            .andExpect(view().name("core/management_events"))
            .andExpect(model().attribute("pageable", eventsDtoPageableDto))
            .andExpect(model().attribute("filterEventDto", filterEventDto))
            .andExpect(model().attribute("cities", List.of("Дніпро", "Львів")))

            .andExpect(status().isOk());

        verify(eventService).getEventsManagement(pageable, filterEventDto, null);
        verify(tagsService).findByTypeAndLanguageCode(TagType.EVENT, "ua");
    }

    @Test
    @SneakyThrows
    void getEventCreatePage() {
        String principalName = "test@example.com";
        when(principal.getName()).thenReturn(principalName);
        when(restClient.findByEmail(principalName)).thenReturn(new UserVO());

        this.mockMvc.perform(get(MANAGEMENT_EVENTS_LINK + "/create-event").principal(principal))
            .andExpect(view().name("core/management_create_event"))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void testCreateEvent() {
        String json =
            "{\"title\":\"asdgasgdgsa\",\"description\":\"<p>asdgadsgagdasdgasdggadsg</p>\",\"tags\":[\"ECONOMIC\"],\"open\":true,\"datesLocations\":[{\"startDate\":\"2024-11-11T00:00:00Z\",\"finishDate\":\"2024-11-11T23:59:00Z\",\"onlineLink\":\"https://www.greencity.cx.ua/#/greenCity\"}]}";
        MockMultipartFile addEventDtoRequestJSON =
            new MockMultipartFile("addEventDtoRequest", "", "application/json", json.getBytes());
        MockMultipartFile image =
            new MockMultipartFile("images", "image.jpg", "image/jpeg", "image content".getBytes());
        mockMvc.perform(multipart(MANAGEMENT_EVENTS_LINK)
            .file(addEventDtoRequestJSON)
            .file(image)
            .principal(() -> "user"))
            .andExpect(status().is3xxRedirection());

        verify(eventService, times(1)).save(any(AddEventDtoRequest.class), eq("user"), any(MultipartFile[].class));
    }

    @Test
    @SneakyThrows
    void testDeleteEvents() {
        List<Long> ids = List.of(1L, 2L, 3L);
        String principalName = "test@example.com";

        when(principal.getName()).thenReturn(principalName);

        this.mockMvc.perform(delete(MANAGEMENT_EVENTS_LINK)
            .contentType("application/json")
            .content("[1, 2, 3]")
            .principal(principal))
            .andExpect(status().isOk());

        for (Long id : ids) {
            verify(eventService, times(1)).delete(id, principalName);
        }
    }

    @Test
    @SneakyThrows
    void testEditEvents() {
        String json =
            "{\"id\":1,\"title\":\"asdgasgdgsa\",\"description\":\"<p>asdgadsgagdasdgasdggadsg</p>\",\"tags\":[\"ECONOMIC\"],\"open\":true,\"datesLocations\":[{\"startDate\":\"2024-11-11T00:00:00Z\",\"finishDate\":\"2024-11-11T23:59:00Z\",\"onlineLink\":\"https://www.greencity.cx.ua/#/greenCity\"}]}";
        MockMultipartFile editEventDtoRequestJSON =
            new MockMultipartFile("eventDto", "", "application/json", json.getBytes());
        MockMultipartFile image =
            new MockMultipartFile("images", "image.jpg", "image/jpeg", "image content".getBytes());
        mockMvc.perform(multipart(MANAGEMENT_EVENTS_LINK)
            .file(editEventDtoRequestJSON)
            .file(image)
            .with(request -> {
                request.setMethod("PUT");
                return request;
            })
            .principal(() -> "user"))
            .andExpect(status().is2xxSuccessful());

        verify(eventService, times(1)).update(any(UpdateEventRequestDto.class), eq("user"), any(MultipartFile[].class));
    }

    @Test
    @SneakyThrows
    void testGetEditPage() {
        EventDto mockEventDto = new EventDto();
        when(restClient.findByEmail(anyString())).thenReturn(new UserVO());
        mockEventDto.setId(1L);

        when(eventService.getEvent(eq(1L), any(Principal.class))).thenReturn(mockEventDto);

        mockMvc.perform(get(MANAGEMENT_EVENTS_LINK + "/edit/{id}", 1L)
            .principal(() -> "user"))
            .andExpect(status().isOk())
            .andExpect(view().name("core/management_edit_event"))
            .andExpect(model().attributeExists("eventDto"))
            .andExpect(model().attribute("eventDto", mockEventDto));

        verify(eventService, times(1)).getEvent(eq(1L), any(Principal.class));
    }

    @Test
    @SneakyThrows
    void getAllEventsWithSorting() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("title")));
        FilterEventDto filterEventDto = new FilterEventDto();
        List<EventDto> eventDtos = Collections.singletonList(new EventDto());
        PageableAdvancedDto<EventDto> eventsDtoPageableDto =
            new PageableAdvancedDto<>(eventDtos, 1, 0, 1, 0, true, false, true, true);

        when(eventService.getEventsManagement(pageable, filterEventDto, null))
            .thenReturn(eventsDtoPageableDto);

        this.mockMvc.perform(get(MANAGEMENT_EVENTS_LINK)
            .param("page", "0")
            .param("size", "10")
            .param("sort", "title,asc")
            .locale(Locale.ENGLISH))
            .andExpect(view().name("core/management_events"))
            .andExpect(model().attribute("pageable", eventsDtoPageableDto))
            .andExpect(status().isOk());

        verify(eventService).getEventsManagement(pageable, filterEventDto, null);
    }

    @Test
    @SneakyThrows
    void getEventTest() {
        Long eventId = 1L;

        EventDateLocationDto date1 = new EventDateLocationDto();
        date1.setStartDate(ZonedDateTime.of(2025, 1, 1, 10, 0, 0, 0, ZoneId.of("UTC")));
        date1.setFinishDate(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC")));

        EventDateLocationDto date2 = new EventDateLocationDto();
        date2.setStartDate(ZonedDateTime.of(2025, 1, 2, 10, 0, 0, 0, ZoneId.of("UTC")));
        date2.setFinishDate(ZonedDateTime.of(2025, 1, 2, 12, 0, 0, 0, ZoneId.of("UTC")));

        EventDto eventDto = new EventDto();
        eventDto.setDates(List.of(date1, date2));
        eventDto.setTitleImage("title-image.jpg");
        eventDto.setAdditionalImages(List.of("image1.jpg", "image2.jpg"));

        EventAttenderDto attender1 = EventAttenderDto.builder().id(1L).name("user1").imagePath("avatar.jpg").build();
        EventAttenderDto attender2 = EventAttenderDto.builder().id(2L).name("user1").imagePath("avatar.jpg").build();

        Set<EventAttenderDto> attenders = Set.of(attender1, attender2);

        when(eventService.getEvent(eventId, principal)).thenReturn(eventDto);
        when(eventService.getAllEventAttenders(eventId)).thenReturn(attenders);

        mockMvc.perform(get(MANAGEMENT_EVENTS_LINK + "/{eventId}", eventId).principal(principal))
            .andExpect(status().isOk())
            .andExpect(view().name("core/management_event"))
            .andExpect(model().attributeExists("eventDto"))
            .andExpect(model().attribute("eventDto", eventDto))
            .andExpect(model().attribute("formattedDate", "Jan 1, 2025 - Jan 2, 2025"))
            .andExpect(model().attribute("imageUrls", List.of("title-image.jpg", "image1.jpg", "image2.jpg")))
            .andExpect(model().attribute("eventAttenders", attenders))
            .andExpect(model().attribute("eventAttendersAvatars", List.of("avatar.jpg", "avatar.jpg")));
    }

    @Test
    @SneakyThrows
    void getAttendersTest() {
        Long eventId = 1L;
        int page = 0;
        int size = 1;
        Pageable pageable = PageRequest.of(page, size);
        when(eventService.getAttendersPage(eventId, pageable)).thenReturn(mock(Page.class));

        mockMvc.perform(get(MANAGEMENT_EVENTS_LINK + "/{eventId}/attenders", eventId)
            .param("page", String.valueOf(page))
            .param("size", String.valueOf(size)))
            .andExpect(status().isOk())
            .andExpect(view().name("core/fragments/attenders-table"))
            .andExpect(model().attributeExists("attendersPage"));
    }

    @Test
    @SneakyThrows
    void getUsersLikedEventTest() {
        Long eventId = 1L;
        int page = 0;
        int size = 1;
        Pageable pageable = PageRequest.of(page, size);
        when(eventService.getUsersLikedEventPage(eventId, pageable)).thenReturn(mock(Page.class));

        mockMvc.perform(get(MANAGEMENT_EVENTS_LINK + "/{eventId}/likes", eventId)
            .param("page", String.valueOf(page))
            .param("size", String.valueOf(size)))
            .andExpect(status().isOk())
            .andExpect(view().name("core/fragments/likes-table"))
            .andExpect(model().attributeExists("usersLikedPage"));
    }

    @Test
    @SneakyThrows
    void getUsersDislikedEventTest() {
        Long eventId = 1L;
        int page = 0;
        int size = 1;
        Pageable pageable = PageRequest.of(page, size);
        when(eventService.getUsersDislikedEventPage(eventId, pageable)).thenReturn(mock(Page.class));

        mockMvc.perform(get(MANAGEMENT_EVENTS_LINK + "/{eventId}/dislikes", eventId)
            .param("page", String.valueOf(page))
            .param("size", String.valueOf(size)))
            .andExpect(status().isOk())
            .andExpect(view().name("core/fragments/dislikes-table"))
            .andExpect(model().attributeExists("usersDislikedPage"));

    }
}
