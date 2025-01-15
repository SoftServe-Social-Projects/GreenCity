package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.constant.ErrorMessage;
import greencity.converters.UserArgumentResolver;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.EventDto;
import greencity.dto.event.UpdateEventRequestDto;
import greencity.dto.filter.FilterEventDto;
import greencity.dto.user.UserVO;
import greencity.enums.EventStatus;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.WrongIdException;
import greencity.service.EventService;
import greencity.service.UserService;
import java.security.Principal;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static greencity.ModelUtils.getEventDtoPageableAdvancedDto;
import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserVO;
import static greencity.TestConst.EVENT_ID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventControllerTest {
    private static final String EVENTS_CONTROLLER_LINK = "/events";
    private final Principal principal = getPrincipal();
    private MockMvc mockMvc;
    @InjectMocks
    private EventController eventController;
    @Mock
    private EventService eventService;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(eventController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .build();
    }

    @Test
    @SneakyThrows
    void getAllEventsTest() {
        Pageable pageable = PageRequest.of(0, 20);
        Long userId = 1L;

        PageableAdvancedDto<EventDto> eventDtoPageableAdvancedDto = getEventDtoPageableAdvancedDto(pageable);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        String expectedJson = objectMapper.writeValueAsString(eventDtoPageableAdvancedDto);

        FilterEventDto filterEventDto = ModelUtils.getFilterEventDto();

        when(eventService.getEvents(pageable, filterEventDto, userId))
            .thenReturn(eventDtoPageableAdvancedDto);

        mockMvc.perform(get(EVENTS_CONTROLLER_LINK)
            .param("page", "0")
            .param("size", "20")
            .param("user-id", userId.toString())
            .param("statuses", filterEventDto.getStatuses().stream().map(Enum::name).collect(Collectors.joining(",")))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(eventService).getEvents(pageable, filterEventDto, userId);
    }

    @Test
    @SneakyThrows
    void getEventsThrowExceptionTest() {
        assertThatThrownBy(
            () -> mockMvc.perform(get(EVENTS_CONTROLLER_LINK)
                .param("statuses", EventStatus.JOINED.name()))
                .andExpect(status().isBadRequest()))
            .hasCause(new BadRequestException(ErrorMessage.STATUSES_REQUIRE_USER_ID));
    }

    @Test
    @SneakyThrows
    void addAttenderTest() {
        Long eventId = 1L;
        mockMvc.perform(post(EVENTS_CONTROLLER_LINK + "/{eventId}/attenders", eventId)
            .principal(principal));
        verify(eventService).addAttender(eventId, principal.getName());
    }

    @Test
    @SneakyThrows
    void addAttenderWithNotValidIdBadRequestTest() {
        String notValidId = "id";
        mockMvc.perform(post(EVENTS_CONTROLLER_LINK + "/{eventId}/attenders", notValidId)
            .principal(principal))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void addAttenderNotFoundTest() {
        Long eventId = 1L;

        doThrow(new NotFoundException("ErrorMessage"))
            .when(eventService)
            .addAttender(eventId, principal.getName());

        assertThatThrownBy(
            () -> mockMvc.perform(post(EVENTS_CONTROLLER_LINK + "/{eventId}/attenders", eventId)
                .principal(principal))
                .andExpect(status().isNotFound()))
            .hasCause(new NotFoundException("ErrorMessage"));
    }

    @Test
    @SneakyThrows
    void addAttenderBadRequestTest() {
        Long eventId = 1L;

        doThrow(new BadRequestException("ErrorMessage"))
            .when(eventService)
            .addAttender(eventId, principal.getName());

        assertThatThrownBy(
            () -> mockMvc.perform(post(EVENTS_CONTROLLER_LINK + "/{eventId}/attenders", eventId)
                .principal(principal))
                .andExpect(status().isBadRequest()))
            .hasCause(new BadRequestException("ErrorMessage"));
    }

    @Test
    @SneakyThrows
    void addToFavoritesTest() {
        Long eventId = 1L;
        mockMvc.perform(post(EVENTS_CONTROLLER_LINK + "/{eventId}/favorites", eventId)
            .principal(principal))
            .andExpect(status().isOk());
        verify(eventService).addToFavorites(eventId, principal.getName());
    }

    @Test
    @SneakyThrows
    void removeFromFavoritesTest() {
        Long eventId = 1L;
        mockMvc.perform(delete(EVENTS_CONTROLLER_LINK + "/{eventId}/favorites", eventId)
            .principal(principal))
            .andExpect(status().isOk());
        verify(eventService).removeFromFavorites(eventId, principal.getName());
    }

    @Test
    @SneakyThrows
    void saveTest() {
        AddEventDtoRequest addEventDtoRequest = getAddEventDtoRequest();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        String json = objectMapper.writeValueAsString(addEventDtoRequest);

        MockMultipartFile jsonFile =
            new MockMultipartFile("addEventDtoRequest", "", "application/json", json.getBytes());

        mockMvc.perform(multipart(EVENTS_CONTROLLER_LINK)
            .file(jsonFile)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isCreated());

        verify(eventService).save(eq(addEventDtoRequest), eq(principal.getName()), isNull());
    }

    @Test
    @SneakyThrows
    void saveBadRequestTest() {
        String json = "{}";
        MockMultipartFile jsonFile =
            new MockMultipartFile("addEventDtoRequest", "", "application/json", json.getBytes());
        mockMvc.perform(multipart(EVENTS_CONTROLLER_LINK)
            .file(jsonFile)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void removeAttenderTest() {
        Long eventId = 1L;
        mockMvc.perform(delete(EVENTS_CONTROLLER_LINK + "/{eventId}/attenders", eventId).principal(principal))
            .andExpect(status().isOk());
        verify(eventService).removeAttender(eventId, principal.getName());
    }

    @Test
    @SneakyThrows
    void removeAttenderBadRequestTest() {
        String notValidId = "id";
        mockMvc.perform(delete(EVENTS_CONTROLLER_LINK + "/{eventId}/attenders", notValidId)
            .principal(principal))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void removeAttenderNotFoundTest() {
        Long eventId = 1L;
        doThrow(new NotFoundException("ErrorMessage"))
            .when(eventService)
            .removeAttender(eventId, principal.getName());

        assertThatThrownBy(() -> mockMvc
            .perform(delete(EVENTS_CONTROLLER_LINK + "/{eventId}/attenders", eventId).principal(principal))
            .andExpect(status().isNotFound())).hasCause(new NotFoundException("ErrorMessage"));
    }

    @Test
    @SneakyThrows
    void deleteTest() {
        mockMvc.perform(delete(EVENTS_CONTROLLER_LINK + "/{eventId}", 1)
            .principal(principal))
            .andExpect(status().isOk());

        verify(eventService).delete(1L, "test@gmail.com");
    }

    @Test
    @SneakyThrows
    void deleteFailedTest() {
        mockMvc.perform(delete(EVENTS_CONTROLLER_LINK + "/{eventId}", "not_number")
            .principal(principal))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void updateTest() {
        UpdateEventRequestDto updateEventDto = getUpdateEventDto();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        String json = objectMapper.writeValueAsString(updateEventDto);

        MockMultipartFile jsonFile =
            new MockMultipartFile("eventDto", "", "application/json", json.getBytes());

        MockMultipartHttpServletRequestBuilder builder = multipart(EVENTS_CONTROLLER_LINK + "/{eventId}", 1L);
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        mockMvc.perform(builder
            .file(jsonFile)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk());

        verify(eventService).update(updateEventDto, principal.getName(), null);
    }

    @Test
    @SneakyThrows
    void update_ThrowException_WhenIdNotEqualTest() {
        UpdateEventRequestDto updateEventDto = getUpdateEventDto();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        String json = objectMapper.writeValueAsString(updateEventDto);

        MockMultipartFile jsonFile =
            new MockMultipartFile("eventDto", "", "application/json", json.getBytes());

        MockMultipartHttpServletRequestBuilder builder = multipart(EVENTS_CONTROLLER_LINK + "/{eventId}", 2L);
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        assertThatThrownBy(() -> mockMvc
            .perform(builder
                .file(jsonFile)
                .principal(principal)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isBadRequest()))
            .hasCause(new WrongIdException(ErrorMessage.EVENT_ID_IN_PATH_PARAM_AND_ENTITY_NOT_EQUAL));
    }

    @Test
    @SneakyThrows
    void likeTest() {
        Long eventId = 1L;

        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(post(EVENTS_CONTROLLER_LINK + "/{eventId}/like", eventId)
            .principal(principal))
            .andExpect(status().isOk());

        verify(eventService).like(eventId, userVO);
    }

    @Test
    void dislikeTest() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        mockMvc.perform(post(EVENTS_CONTROLLER_LINK + "/{eventId}/dislike", 1)
            .principal(principal))
            .andExpect(status().isOk());
        verify(eventService).dislike(userVO, 1L);
    }

    @Test
    @SneakyThrows
    void countLikesTest() {
        mockMvc.perform(get(EVENTS_CONTROLLER_LINK + "/{eventId}/likes/count", EVENT_ID)
            .principal(principal))
            .andExpect(status().isOk());

        verify(eventService).countLikes(EVENT_ID);
    }

    @Test
    @SneakyThrows
    void countDislikesTest() {
        mockMvc.perform(get(EVENTS_CONTROLLER_LINK + "/{eventId}/dislikes/count", EVENT_ID)
            .principal(principal))
            .andExpect(status().isOk());

        verify(eventService).countDislikes(EVENT_ID);
    }

    @Test
    @SneakyThrows
    void checkIsEventLikedByUserTest() {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(get(EVENTS_CONTROLLER_LINK + "/{eventId}/likes", EVENT_ID)
            .principal(principal))
            .andExpect(status().isOk());

        verify(eventService).isEventLikedByUser(EVENT_ID, userVO);
    }

    @Test
    @SneakyThrows
    void checkIsEventDislikedByUserTest() {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(get(EVENTS_CONTROLLER_LINK + "/{eventId}/dislikes", EVENT_ID)
            .principal(principal))
            .andExpect(status().isOk());

        verify(eventService).isEventDislikedByUser(EVENT_ID, userVO);
    }

    @Test
    @SneakyThrows
    void rateEventTest() {
        Long eventId = 1L;
        int grade = 2;

        mockMvc.perform(post(EVENTS_CONTROLLER_LINK + "/{eventId}/ratings", eventId)
            .principal(principal)
            .content(String.valueOf(grade))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(eventService).rateEvent(eventId, principal.getName(), grade);
    }

    @Test
    @SneakyThrows
    void rateEventWithNotValidIdBadRequestTest() {
        String notValidId = "id";
        int grade = 2;

        mockMvc.perform(
            post(EVENTS_CONTROLLER_LINK + "/{eventId}/ratings", notValidId)
                .principal(principal)
                .content(String.valueOf(grade))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void rateEventWithNotValidGradeBadRequestTest() {
        Long eventId = 1L;
        String notValidGrade = "grade";

        mockMvc
            .perform(post(EVENTS_CONTROLLER_LINK + "/{eventId}/ratings", eventId)
                .principal(principal)
                .content(notValidGrade)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void rateEventNotFoundTest() {
        Long eventId = 1L;
        int grade = 2;

        doThrow(new NotFoundException("ErrorMessage"))
            .when(eventService)
            .rateEvent(eventId, principal.getName(), grade);

        assertThatThrownBy(() -> mockMvc
            .perform(post(EVENTS_CONTROLLER_LINK + "/{eventId}/ratings", eventId)
                .principal(principal)
                .content(String.valueOf(grade))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())).hasCause(new NotFoundException("ErrorMessage"));
    }

    @Test
    @SneakyThrows
    void rateEventBadRequestTest() {
        Long eventId = 1L;
        int grade = 2;

        doThrow(new BadRequestException("ErrorMessage"))
            .when(eventService)
            .rateEvent(eventId, principal.getName(), grade);

        assertThatThrownBy(() -> mockMvc
            .perform(post(EVENTS_CONTROLLER_LINK + "/{eventId}/ratings", eventId)
                .principal(principal)
                .content(String.valueOf(grade))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())).hasCause(new BadRequestException("ErrorMessage"));
    }

    @Test
    @SneakyThrows
    void updateBadRequestTest() {
        String json = "";

        MockMultipartFile jsonFile =
            new MockMultipartFile("eventDto", "", "application/json", json.getBytes());

        MockMultipartHttpServletRequestBuilder builder =
            multipart(EVENTS_CONTROLLER_LINK + "/{eventId}", 1L);
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        mockMvc.perform(builder
            .file(jsonFile)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void getEventTest() {
        mockMvc.perform(get(EVENTS_CONTROLLER_LINK + "/{eventId}", 1L).principal(principal))
            .andExpect(status().isOk());

        verify(eventService).getEvent(1L, principal);
    }

    @Test
    @SneakyThrows
    void getEventFailedTest() {
        mockMvc.perform(get(EVENTS_CONTROLLER_LINK + "/{eventId}", "not_number").principal(principal))
            .andExpect(status().isBadRequest());

        verify(eventService, times(0)).getEvent(1L, principal);
    }

    @Test
    @SneakyThrows
    void getEventResponseTest() {
        EventDto eventDto = getEventDto();

        when(eventService.getEvent(1L, principal)).thenReturn(eventDto);

        MvcResult result = mockMvc.perform(get(EVENTS_CONTROLLER_LINK + "/{eventId}", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        EventDto responseEventDto = objectMapper.readValue(result.getResponse().getContentAsString(), EventDto.class);

        assertEquals(eventDto, responseEventDto);

        verify(eventService).getEvent(1L, principal);
    }

    @Test
    @SneakyThrows
    void getAllEventSubscribersTest() {
        Long eventId = 1L;
        mockMvc.perform(get(EVENTS_CONTROLLER_LINK + "/{eventId}/attenders", eventId))
            .andExpect(status().isOk());

        verify(eventService).getAllEventAttenders(eventId);
    }

    @Test
    @SneakyThrows
    void getAllEventSubscribersWithNotValidIdBadRequestTest() {
        String notValidId = "id";
        mockMvc.perform(get(EVENTS_CONTROLLER_LINK + "/{eventId}/attenders", notValidId))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void getAllEventSubscribersNotFoundTest() {
        Long eventId = 1L;

        doThrow(new NotFoundException("ErrorMessage"))
            .when(eventService)
            .getAllEventAttenders(eventId);

        assertThatThrownBy(
            () -> mockMvc.perform(get(EVENTS_CONTROLLER_LINK + "/{eventId}/attenders", eventId))
                .andExpect(status().isNotFound()))
            .hasCause(new NotFoundException("ErrorMessage"));
    }

    @Test
    @SneakyThrows
    void getAllEventsAddressesTest() {
        mockMvc.perform(get(EVENTS_CONTROLLER_LINK + "/addresses"))
            .andExpect(status().isOk());
        verify(eventService).getAllEventsAddresses();
    }

    @Test
    @SneakyThrows
    void getAllAttendersCountTest() {
        mockMvc.perform(get(EVENTS_CONTROLLER_LINK + "/attenders/count?user-id=1"))
            .andExpect(status().isOk());
        verify(eventService).getCountOfAttendedEventsByUserId(1L);
    }

    @Test
    @SneakyThrows
    void getOrganizersCountTest() {
        mockMvc.perform(get(EVENTS_CONTROLLER_LINK + "/organizers/count?user-id=1"))
            .andExpect(status().isOk());
        verify(eventService).getCountOfOrganizedEventsByUserId(1L);
    }

    @SneakyThrows
    private AddEventDtoRequest getAddEventDtoRequest() {
        String json = """
            {
                "title":"string",
                "description":"stringstringstringstringstringstringstringstring",
                "open":true,
                "datesLocations":[
                    {
                        "startDate":"2023-05-27T15:00:00Z",
                        "finishDate":"2023-05-27T17:00:00Z",
                        "coordinates":{
                            "latitude":1,
                            "longitude":1
                        },
                        "onlineLink":"http://localhost:8080/swagger-ui.html#/events-controller"
                    }
                ],
                "tags":["Social"]
            }""";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        return objectMapper.readValue(json, AddEventDtoRequest.class);
    }

    @SneakyThrows
    private UpdateEventRequestDto getUpdateEventDto() {
        String json = """
            {
                "id":1,
                "title":"string",
                "description":"stringstringstringstringstringstringstringstring",
                "open":true,
                "datesLocations":[
                    {
                        "startDate":"2023-05-27T15:00:00Z",
                        "finishDate":"2023-05-27T17:00:00Z",
                        "coordinates":{
                            "latitude":1,
                            "longitude":1
                        },
                        "onlineLink":"http://localhost:8080/swagger-ui.html#/events-controller"
                    }
                ],
                "tags":["Social"]
            }""";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        return objectMapper.readValue(json, UpdateEventRequestDto.class);
    }

    @SneakyThrows
    private EventDto getEventDto() {
        String json = """
            {
              "additionalImages": [
                "string"
              ],
              "dates": [
                {
                    "coordinates": {
                    "streetUa": "string",
                    "streetEn": "string",
                    "houseNumber": "string",
                    "cityUa": "string",
                    "cityEn": "string",
                    "regionUa": "string",
                    "regionEn": "string",
                    "countryUa": "string",
                    "countryEn": "string",
                    "latitude": 0,
                    "longitude": 0
                  },
                  "finishDate": "2022-12-08T15:13:27.538Z",
                  "id": 0,
                  "onlineLink": "string",
                  "startDate": "2022-12-08T15:13:27.538Z"
                }
              ],
              "description": "stringstringstringstringstringstringstring",
              "creationDate": "2022-12-08",
              "id": 0,
              "isSubscribed": true,
              "open": true,
              "organizer": {
                "id": 0,
                "name": "string",
                "organizerRating": 0
              },
              "tags": [
                {
                  "id": 0,
                  "nameEn": "string",
                  "nameUa": "string"
                }
              ],
              "title": "string",
              "titleImage": "string"
            }""";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        return objectMapper.readValue(json, EventDto.class);
    }

    @Test
    @SneakyThrows
    void addToRequestedTest() {
        Long eventId = 1L;
        mockMvc.perform(post(EVENTS_CONTROLLER_LINK + "/{eventId}/addToRequested", eventId)
            .principal(principal))
            .andExpect(status().isOk());
        verify(eventService).addToRequested(eventId, principal.getName());
    }

    @Test
    @SneakyThrows
    void removeFromRequestedTest() {
        Long eventId = 1L;
        mockMvc.perform(delete(EVENTS_CONTROLLER_LINK + "/{eventId}/removeFromRequested", eventId)
            .principal(principal))
            .andExpect(status().isOk());
        verify(eventService).removeFromRequested(eventId, principal.getName());
    }

    @Test
    @SneakyThrows
    void getRequestedUsersTest() {
        Long eventId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        mockMvc.perform(get(EVENTS_CONTROLLER_LINK + "/{eventId}/requested-users", eventId)
            .principal(principal))
            .andExpect(status().isOk());
        verify(eventService).getRequestedUsers(eventId, principal.getName(), pageable);
    }

    @Test
    @SneakyThrows
    void approveRequestTest() {
        Long eventId = 1L;
        Long userId = 1L;
        mockMvc.perform(post(EVENTS_CONTROLLER_LINK + "/{eventId}/requested-users/{userId}/approve", eventId, userId)
            .principal(principal))
            .andExpect(status().isOk());
        verify(eventService).approveRequest(eventId, principal.getName(), userId);
    }

    @Test
    @SneakyThrows
    void declineRequestTest() {
        Long eventId = 1L;
        Long userId = 1L;
        mockMvc.perform(post(EVENTS_CONTROLLER_LINK + "/{eventId}/requested-users/{userId}/decline", eventId, userId)
            .principal(principal))
            .andExpect(status().isOk());
        verify(eventService).declineRequest(eventId, principal.getName(), userId);
    }
}
