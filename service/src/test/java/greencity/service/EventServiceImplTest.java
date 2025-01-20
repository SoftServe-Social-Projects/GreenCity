package greencity.service;

import greencity.ModelUtils;
import greencity.TestConst;
import greencity.achievement.AchievementCalculation;
import greencity.client.RestClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.PageableDto;
import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.AddressDto;
import greencity.dto.event.EventAttenderDto;
import greencity.dto.event.EventDateLocationDto;
import greencity.dto.event.EventDto;
import greencity.dto.event.UpdateEventDto;
import greencity.dto.event.UpdateEventRequestDto;
import greencity.dto.filter.FilterEventDto;
import greencity.dto.notification.LikeNotificationDto;
import greencity.dto.search.SearchEventsDto;
import greencity.dto.tag.TagVO;
import greencity.dto.user.UserProfilePictureDto;
import greencity.dto.user.UserVO;
import greencity.entity.RatingPoints;
import greencity.entity.Tag;
import greencity.entity.User;
import greencity.entity.event.Address;
import greencity.entity.event.Event;
import greencity.entity.event.EventDateLocation;
import greencity.entity.event.EventGrade;
import greencity.entity.event.EventImages;
import greencity.enums.NotificationType;
import greencity.enums.Role;
import greencity.enums.TagType;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.mapping.events.EventDateLocationDtoMapper;
import greencity.rating.RatingCalculation;
import greencity.repository.AchievementCategoryRepo;
import greencity.repository.EventRepo;
import greencity.repository.RatingPointsRepo;
import greencity.repository.UserRepo;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import static greencity.ModelUtils.getAuthorVO;
import static greencity.ModelUtils.getEvent;
import static greencity.ModelUtils.getEventPreviewDtos;
import static greencity.ModelUtils.getFilterEventDto;
import static greencity.ModelUtils.getTupleElements;
import static greencity.ModelUtils.getTuples;
import static greencity.ModelUtils.getUser;
import static greencity.ModelUtils.getUserVO;
import static greencity.ModelUtils.getUsersHashSet;
import static greencity.ModelUtils.testUserVo;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class EventServiceImplTest {
    @Mock
    ModelMapper modelMapper;

    @Mock
    EventDateLocationDtoMapper eventDateLocationDtoMapper;

    @Mock
    EventRepo eventRepo;

    @Mock
    RestClient restClient;

    @Mock
    TagsService tagService;

    @Mock
    UserService userService;

    @Mock
    FileService fileService;

    @Mock
    GoogleApiService googleApiService;

    @Mock
    UserRepo userRepo;

    @InjectMocks
    EventServiceImpl eventService;
    @Mock
    private AchievementCalculation achievementCalculation;
    @Mock
    private RatingCalculation ratingCalculation;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private AchievementCategoryRepo achievementCategoryRepo;
    @Mock
    private UserNotificationService userNotificationService;
    @Mock
    private RatingPointsRepo ratingPointsRepo;

    @Test
    void save() {
        EventDto eventDto = ModelUtils.getEventDto();
        List<Long> eventIds = List.of(eventDto.getId());
        AddEventDtoRequest addEventDtoRequest = ModelUtils.addEventDtoRequest;
        Event event = ModelUtils.getEvent();
        List<Tag> tags = ModelUtils.getEventTags();
        User user = ModelUtils.getUser();
        RatingPoints ratingPoints = RatingPoints.builder().id(1L).name("CREATE_EVENT").points(40).build();

        when(ratingPointsRepo.findByNameOrThrow("CREATE_EVENT")).thenReturn(ratingPoints);
        when(modelMapper.map(addEventDtoRequest, Event.class)).thenReturn(event);
        when(restClient.findByEmail(anyString())).thenReturn(testUserVo);
        when(modelMapper.map(testUserVo, User.class)).thenReturn(user);
        when(eventRepo.save(event)).thenReturn(event);
        when(modelMapper.map(event, EventDto.class)).thenReturn(eventDto);
        List<TagVO> tagVOList = Collections.singletonList(ModelUtils.getTagVO());
        when(tagService.findTagsByNamesAndType(anyList(), eq(TagType.ECO_NEWS))).thenReturn(tagVOList);
        when(modelMapper.map(tagVOList, new TypeToken<List<Tag>>() {
        }.getType())).thenReturn(tags);
        when(googleApiService.getResultFromGeoCodeByCoordinates(any()))
            .thenReturn(ModelUtils.getAddressLatLngResponse());
        AddressDto build = ModelUtils.getLongitudeAndLatitude();
        when(modelMapper.map(ModelUtils.getAddressLatLngResponse(), AddressDto.class)).thenReturn(build);
        when(eventRepo.findFavoritesAmongEventIds(eventIds, user.getId())).thenReturn(List.of(event));
        when(eventRepo.findSubscribedAmongEventIds(eventIds, user.getId())).thenReturn(List.of());
        when(eventDateLocationDtoMapper.mapAllToList(addEventDtoRequest.getDatesLocations()))
            .thenReturn(event.getDates());

        EventDto resultEventDto = eventService.save(addEventDtoRequest, user.getEmail(), null);
        assertEquals(eventDto, resultEventDto);
        assertFalse(resultEventDto.isSubscribed());
        assertTrue(resultEventDto.isFavorite());

        verify(eventRepo).findFavoritesAmongEventIds(eventIds, user.getId());
        verify(eventRepo).findSubscribedAmongEventIds(eventIds, user.getId());

        MultipartFile multipartFile = ModelUtils.getMultipartFile();
        when(fileService.upload(multipartFile)).thenReturn("/url1");
        assertEquals(eventDto,
            eventService.save(addEventDtoRequest, user.getEmail(),
                new MultipartFile[] {multipartFile}));

        MultipartFile[] multipartFiles = ModelUtils.getMultipartFiles();
        when(fileService.upload(multipartFiles[0])).thenReturn("/url1");
        when(fileService.upload(multipartFiles[1])).thenReturn("/url2");
        assertEquals(eventDto,
            eventService.save(addEventDtoRequest, ModelUtils.getUser().getEmail(), multipartFiles));
    }

    @Test
    void saveEventWithoutAddress() {
        User user = ModelUtils.getUser();
        AddEventDtoRequest addEventDtoWithoutCoordinates = ModelUtils.addEventDtoWithoutAddressRequest;
        Event eventWithoutCoordinates = ModelUtils.getEventWithoutAddress();
        String email = user.getEmail();
        when(modelMapper.map(addEventDtoWithoutCoordinates, Event.class)).thenReturn(eventWithoutCoordinates);
        when(eventRepo.save(any(Event.class))).thenReturn(eventWithoutCoordinates);
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> eventService.save(addEventDtoWithoutCoordinates, email, null));
        assertEquals(ErrorMessage.INVALID_COORDINATES, exception.getMessage());
        verify(eventRepo, times(0)).save(eventWithoutCoordinates);
    }

    @Test
    void saveOnlineEventSuccessTest() {
        User user = ModelUtils.getUser();
        AddEventDtoRequest addEventDtoRequest = ModelUtils.addEventDtoWithoutAddressRequest;
        Event event = ModelUtils.getEventWithoutAddress();
        List<Tag> tags = ModelUtils.getEventTags();
        AddressDto addressDto = ModelUtils.getLongitudeAndLatitude();
        EventDto eventDto = ModelUtils.getEventDtoWithoutAddress();
        MultipartFile[] multipartFiles = ModelUtils.getMultipartFiles();

        when(eventDateLocationDtoMapper.mapAllToList(addEventDtoRequest.getDatesLocations()))
            .thenReturn(event.getDates());
        when(googleApiService.getResultFromGeoCodeByCoordinates(any()))
            .thenReturn(ModelUtils.getAddressLatLngResponse());
        when(modelMapper.map(ModelUtils.getAddressLatLngResponse(), AddressDto.class)).thenReturn(addressDto);
        when(modelMapper.map(addEventDtoRequest, Event.class)).thenReturn(event);
        when(restClient.findByEmail(anyString())).thenReturn(testUserVo);
        when(modelMapper.map(testUserVo, User.class)).thenReturn(user);
        List<TagVO> tagVOList = Collections.singletonList(ModelUtils.getTagVO());
        when(tagService.findTagsByNamesAndType(anyList(), eq(TagType.ECO_NEWS))).thenReturn(tagVOList);
        when(modelMapper.map(tagVOList, new TypeToken<List<Tag>>() {
        }.getType())).thenReturn(tags);
        when(eventRepo.save(event)).thenReturn(event);
        when(modelMapper.map(event, EventDto.class)).thenReturn(eventDto);
        when(fileService.upload(multipartFiles[0])).thenReturn("/url1");
        when(fileService.upload(multipartFiles[1])).thenReturn("/url2");

        assertEquals(eventDto,
            eventService.save(addEventDtoRequest, ModelUtils.getUser().getEmail(), multipartFiles));
    }

    @Test
    void update() {
        EventDto eventDto = ModelUtils.getEventDto();
        Event expectedEvent = ModelUtils.getEvent();
        List<Long> eventIds = List.of(eventDto.getId());
        UpdateEventRequestDto eventToUpdateDto = ModelUtils.getUpdateEventRequestDto();
        User user = ModelUtils.getUser();
        UpdateEventDto updateEventDto = ModelUtils.getUpdateEventDto();

        when(eventRepo.findById(1L)).thenReturn(Optional.of(expectedEvent));
        when(restClient.findByEmail(anyString())).thenReturn(testUserVo);
        when(modelMapper.map(testUserVo, User.class)).thenReturn(user);
        when(eventRepo.findFavoritesAmongEventIds(eventIds, user.getId())).thenReturn(List.of());
        when(eventRepo.findSubscribedAmongEventIds(eventIds, user.getId())).thenReturn(List.of(expectedEvent));
        when(modelMapper.map(expectedEvent, EventDto.class)).thenReturn(eventDto);
        when(eventRepo.save(expectedEvent)).thenReturn(expectedEvent);
        when(modelMapper.map(eventToUpdateDto, UpdateEventDto.class)).thenReturn(updateEventDto);

        EventDto actualEvent = eventService.update(eventToUpdateDto, ModelUtils.getUser().getEmail(), null);

        assertEquals(eventDto, actualEvent);

        assertFalse(actualEvent.isFavorite());
        assertTrue(actualEvent.isSubscribed());

        verify(eventRepo).findFavoritesAmongEventIds(eventIds, user.getId());
        verify(eventRepo).findSubscribedAmongEventIds(eventIds, user.getId());
        verify(restClient).findByEmail(anyString());
    }

    @Test
    void updateThrowsUserHasNoPermissionToAccessException() {
        UpdateEventRequestDto eventToUpdateDto = ModelUtils.getUpdateEventRequestDto();
        UserVO userVO = ModelUtils.getTestUserVo();
        User user = ModelUtils.getTestUser();
        String userVoEmail = userVO.getEmail();
        Event expectedEvent = ModelUtils.getEvent();
        UpdateEventDto updateEventDto = ModelUtils.getUpdateEventDto();

        when(eventRepo.findById(1L)).thenReturn(Optional.of(expectedEvent));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(modelMapper.map(eventToUpdateDto, UpdateEventDto.class)).thenReturn(updateEventDto);
        when(restClient.findByEmail(anyString())).thenReturn(userVO);

        assertThrows(UserHasNoPermissionToAccessException.class,
            () -> eventService.update(eventToUpdateDto, userVoEmail, null));

        verify(eventRepo).findById(1L);
        verify(restClient).findByEmail("user@email.com");
        verify(modelMapper).map(userVO, User.class);
        verify(eventRepo, never()).findFavoritesAmongEventIds(anyList(), anyLong());
        verify(eventRepo, never()).findSubscribedAmongEventIds(anyList(), anyLong());
    }

    @Test
    void updateFinishedEvent() {
        Event actualEvent = ModelUtils.getEventWithFinishedDate();
        UpdateEventRequestDto eventToUpdateDto = ModelUtils.getUpdateEventRequestDto();
        UpdateEventDto updateEventDto = ModelUtils.getUpdateEventDto();
        String userEmail = ModelUtils.getUser().getEmail();

        when(eventRepo.findById(any())).thenReturn(Optional.of(actualEvent));
        when(modelMapper.map(eventToUpdateDto, UpdateEventDto.class)).thenReturn(updateEventDto);
        when(modelMapper.map(testUserVo, User.class)).thenReturn(ModelUtils.getUser());
        when(restClient.findByEmail(anyString())).thenReturn(testUserVo);

        assertThrows(BadRequestException.class,
            () -> eventService.update(eventToUpdateDto, userEmail, null));

        verify(eventRepo, never()).findFavoritesAmongEventIds(anyList(), anyLong());
        verify(eventRepo, never()).findSubscribedAmongEventIds(anyList(), anyLong());
    }

    @Test
    @SneakyThrows
    void enhanceWithNewData() {
        Method method = EventServiceImpl.class.getDeclaredMethod("enhanceWithNewData", Event.class,
            UpdateEventDto.class, MultipartFile[].class);
        method.setAccessible(true);
        Event event = ModelUtils.getEvent();
        Event expectedEvent = ModelUtils.getExpectedEvent();
        UpdateEventDto eventToUpdateDto = ModelUtils.getUpdateEventDto();
        method.invoke(eventService, event, eventToUpdateDto, null);
        assertEquals(event.getTitleImage(), expectedEvent.getTitleImage());

        eventToUpdateDto.setTitle("New title");
        eventToUpdateDto.setDescription("New description");
        eventToUpdateDto.setIsOpen(false);
        eventToUpdateDto.setTags(ModelUtils.getUpdatedEventTags());
        eventToUpdateDto.setDatesLocations(ModelUtils.getUpdatedEventDateLocationDto());

        expectedEvent.setTitle("New title");
        expectedEvent.setDescription("New description");
        expectedEvent.setOpen(false);
        expectedEvent.setTags(ModelUtils.getEventTags());
        expectedEvent.setDates(List.of(ModelUtils.getUpdatedEventDateLocation()));

        List<TagVO> updatedTagVO = List.of(ModelUtils.getTagVO());
        when(tagService.findTagsWithAllTranslationsByNamesAndType(eventToUpdateDto.getTags(), TagType.EVENT))
            .thenReturn(updatedTagVO);
        when(modelMapper.map(updatedTagVO, new TypeToken<List<Tag>>() {
        }.getType())).thenReturn(ModelUtils.getEventTags());
        doNothing().when(eventRepo).deleteEventDateLocationsByEventId(1L);
        when(modelMapper.map(eventToUpdateDto.getDatesLocations().getFirst(), EventDateLocation.class))
            .thenReturn(ModelUtils.getUpdatedEventDateLocation());

        when(googleApiService.getResultFromGeoCodeByCoordinates(any()))
            .thenReturn(ModelUtils.getAddressLatLngResponse());

        method.invoke(eventService, event, eventToUpdateDto, null);
        assertEquals(event.getTitleImage(), expectedEvent.getTitleImage());
        assertEquals(event.getDescription(), expectedEvent.getDescription());
        assertEquals(event.getTags(), expectedEvent.getTags());

        eventToUpdateDto.setTitleImage("New img");
        eventToUpdateDto.setAdditionalImages(List.of("New addition image"));
        expectedEvent.setTitleImage("New img");
        expectedEvent.setAdditionalImages(List.of(EventImages.builder().link("New addition image").build()));

        method.invoke(eventService, event, eventToUpdateDto, null);
        assertEquals(expectedEvent.getAdditionalImages().getFirst().getLink(),
            event.getAdditionalImages().getFirst().getLink());
        assertEquals(event.getTitleImage(), expectedEvent.getTitleImage());

        when(eventRepo.findAllImagesLinksByEventId(anyLong())).thenReturn(List.of("New addition image"));
        doNothing().when(fileService).delete(any());

        method.invoke(eventService, event, eventToUpdateDto, null);
        assertEquals(expectedEvent.getTitleImage(), event.getTitleImage());
        assertEquals(expectedEvent.getAdditionalImages().getFirst().getLink(),
            event.getAdditionalImages().getFirst().getLink());

        eventToUpdateDto.setAdditionalImages(null);
        method.invoke(eventService, event, eventToUpdateDto, null);
        assertNull(event.getAdditionalImages());

        eventToUpdateDto.setTitleImage(null);
        expectedEvent.setTitleImage(AppConstant.DEFAULT_EVENT_IMAGES);
        method.invoke(eventService, event, eventToUpdateDto, null);
        assertEquals(expectedEvent.getTitleImage(), event.getTitleImage());

        MultipartFile[] multipartFiles = ModelUtils.getMultipartFiles();
        when(fileService.upload(multipartFiles[0])).thenReturn("url1");
        when(fileService.upload(multipartFiles[1])).thenReturn("url2");

        method.invoke(eventService, event, eventToUpdateDto, multipartFiles);

        expectedEvent.setTitleImage("url1");
        expectedEvent.setAdditionalImages(List.of(EventImages.builder().event(expectedEvent).link("url2").build()));

        method.invoke(eventService, event, eventToUpdateDto, multipartFiles);
        assertEquals(expectedEvent.getTitleImage(), event.getTitleImage());
        assertEquals(expectedEvent.getAdditionalImages().getFirst().getLink(),
            event.getAdditionalImages().getFirst().getLink());

        when(eventRepo.findAllImagesLinksByEventId(anyLong())).thenReturn(new ArrayList<>());
        doNothing().when(fileService).delete(any());
        eventToUpdateDto.setTitleImage("url");
        eventToUpdateDto.setAdditionalImages(List.of("Add img 1", "Add img 2"));
        expectedEvent.setTitleImage("url");
        expectedEvent.setAdditionalImages(List.of(EventImages.builder().event(expectedEvent).link("Add img 1").build(),
            EventImages.builder().event(expectedEvent).link("Add img 2").build()));
        method.invoke(eventService, event, eventToUpdateDto, multipartFiles);
        assertEquals(expectedEvent.getTitleImage(), event.getTitleImage());
        assertEquals(expectedEvent.getAdditionalImages().getFirst().getLink(),
            event.getAdditionalImages().get(0).getLink());
        assertEquals("url2", event.getAdditionalImages().get(3).getLink());

        eventToUpdateDto.setAdditionalImages(null);
        expectedEvent.setAdditionalImages(null);
        eventToUpdateDto.setTitleImage(null);
        expectedEvent.setTitleImage("title url");
        MultipartFile multipartFile = ModelUtils.getMultipartFile();
        when(fileService.upload(multipartFile)).thenReturn("title url");

        method.invoke(eventService, event, eventToUpdateDto, new MultipartFile[] {multipartFile});
        assertEquals(expectedEvent.getTitleImage(), event.getTitleImage());
        assertNull(event.getAdditionalImages());
    }

    @Test
    void updateTitleImage() {
        EventDto eventDto = ModelUtils.getEventDto();
        UpdateEventRequestDto eventToUpdateDto = ModelUtils.getUpdateEventRequestDto();
        eventToUpdateDto.setAdditionalImages(new ArrayList<>());
        Event event = ModelUtils.getEvent();
        List<Long> eventIds = List.of(event.getId());
        User user = ModelUtils.getUser();
        UpdateEventDto updateEventDto = ModelUtils.getUpdateEventDto();

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(modelMapper.map(testUserVo, User.class)).thenReturn(user);
        when(restClient.findByEmail(anyString())).thenReturn(testUserVo);
        when(eventRepo.save(event)).thenReturn(event);
        when(modelMapper.map(event, EventDto.class)).thenReturn(eventDto);
        when(eventRepo.findFavoritesAmongEventIds(eventIds, user.getId())).thenReturn(List.of(event));
        when(eventRepo.findSubscribedAmongEventIds(eventIds, user.getId())).thenReturn(List.of(event));
        when(modelMapper.map(eventToUpdateDto, UpdateEventDto.class)).thenReturn(updateEventDto);

        EventDto updatedEventDto = eventService.update(eventToUpdateDto, user.getEmail(), null);
        assertEquals(updatedEventDto, eventDto);
        assertTrue(updatedEventDto.isFavorite());
        assertTrue(updatedEventDto.isSubscribed());

        eventToUpdateDto.setTitle("New title");
        eventToUpdateDto.setDescription("New description");
        eventToUpdateDto.setIsOpen(false);
        eventToUpdateDto.setTags(ModelUtils.getUpdatedEventTags());
        eventToUpdateDto.setDatesLocations(ModelUtils.getUpdateEventDateLocationDto());

        eventDto.setTitle("New title");
        eventDto.setDescription("New description");
        eventDto.setOpen(false);
        eventDto.setTags(ModelUtils.getUpdatedEventTagUaEn());
        eventDto.setDates(ModelUtils.getUpdatedEventDateLocationDto());

        List<TagVO> updatedTagVO = List.of(ModelUtils.getTagVO());

        when(tagService.findTagsWithAllTranslationsByNamesAndType(eventToUpdateDto.getTags(), TagType.EVENT))
            .thenReturn(updatedTagVO);
        when(modelMapper.map(updatedTagVO, new TypeToken<List<Tag>>() {
        }.getType())).thenReturn(ModelUtils.getEventTags());
        doNothing().when(eventRepo).deleteEventDateLocationsByEventId(1L);
        when(modelMapper.map(eventToUpdateDto.getDatesLocations().getFirst(), EventDateLocation.class))
            .thenReturn(ModelUtils.getUpdatedEventDateLocation());
        when(googleApiService.getResultFromGeoCodeByCoordinates(any()))
            .thenReturn(ModelUtils.getAddressLatLngResponse());

        assertEquals(updatedEventDto, eventDto);
        assertTrue(updatedEventDto.isFavorite());
        assertTrue(updatedEventDto.isSubscribed());

        verify(restClient).findByEmail(anyString());
    }

    @ParameterizedTest
    @MethodSource("provideUserVOForDeleteEventTest")
    void deleteEventTest(UserVO userVO) {
        Event event = ModelUtils.getEvent();
        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(restClient.findByEmail(userVO.getEmail())).thenReturn(userVO);

        eventService.delete(event.getId(), userVO.getEmail());

        verify(eventRepo).findById(1L);
        verify(restClient).findByEmail(userVO.getEmail());
        verify(eventRepo).delete(event);
    }

    private static Stream<Arguments> provideUserVOForDeleteEventTest() {
        return Stream.of(
            Arguments.of(ModelUtils.getUserVO(), ModelUtils.getUser()),
            Arguments.of(ModelUtils.getUserVO().setRole(Role.ROLE_ADMIN).setId(1L),
                ModelUtils.getUser().setRole(Role.ROLE_ADMIN).setId(1L)));
    }

    @Test
    void deleteEventThrowsNotFoundExceptionTest() {
        when(eventRepo.findById(1L)).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> eventService.delete(1L, "test@mail.com"));
        verify(eventRepo).findById(1L);
    }

    @Test
    void deleteEventThrowsBadRequestExceptionTest() {
        UserVO userVO = ModelUtils.getUserVO();
        String userEmail = userVO.getEmail();
        userVO.setId(33L);
        Event event = ModelUtils.getEvent();

        when(restClient.findByEmail(userEmail)).thenReturn(userVO);
        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));

        Long eventId = event.getId();
        assertThrows(UserHasNoPermissionToAccessException.class, () -> eventService.delete(eventId, userEmail));

        verify(restClient).findByEmail(userEmail);
        verify(eventRepo).findById(1L);
    }

    @Test
    void getEventWithoutUser() {
        Event event = ModelUtils.getEvent();
        EventDto eventDto = ModelUtils.getEventDto();

        when(eventRepo.findById(anyLong())).thenReturn(Optional.of(event));
        when(modelMapper.map(event, EventDto.class)).thenReturn(eventDto);

        EventDto actual = eventService.getEvent(1L, null);

        assertEquals(eventDto.getId(), actual.getId());
        assertEquals(eventDto.getAdditionalImages(), actual.getAdditionalImages());
        assertEquals(eventDto.getTitleImage(), actual.getTitleImage());
        assertFalse(actual.isSubscribed());
        assertFalse(actual.isFavorite());
        assertNull(actual.getCurrentUserGrade());
        verify(eventRepo, never()).findFavoritesAmongEventIds(anyList(), anyLong());
        verify(eventRepo, never()).findSubscribedAmongEventIds(anyList(), anyLong());
    }

    @Test
    void getEventWithCurrentUser() {
        Event event = ModelUtils.getEvent();
        EventDto eventDto = ModelUtils.getEventDto();
        List<Long> eventIds = List.of(eventDto.getId());
        Principal principal = ModelUtils.getPrincipal();
        User user = ModelUtils.getUser();

        event.setEventGrades(List.of(EventGrade.builder().grade(5).user(user).event(event).build()));

        when(modelMapper.map(testUserVo, User.class)).thenReturn(user);
        when(restClient.findByEmail(principal.getName())).thenReturn(testUserVo);
        when(eventRepo.findById(anyLong())).thenReturn(Optional.of(event));
        when(modelMapper.map(event, EventDto.class)).thenReturn(eventDto);
        when(eventRepo.findFavoritesAmongEventIds(eventIds, user.getId())).thenReturn(List.of(event));
        when(eventRepo.findSubscribedAmongEventIds(eventIds, user.getId())).thenReturn(List.of(event));

        EventDto actual = eventService.getEvent(1L, principal);

        assertTrue(actual.isSubscribed());
        assertTrue(actual.isFavorite());
        assertEquals(5, actual.getCurrentUserGrade());
        verify(eventRepo).findFavoritesAmongEventIds(eventIds, user.getId());
        verify(eventRepo).findSubscribedAmongEventIds(eventIds, user.getId());
    }

    @Test
    void addAttenderToOpenEvent() {
        Event event = ModelUtils.getEvent();
        User user = ModelUtils.getAttenderUser();

        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(modelMapper.map(restClient.findByEmail(ModelUtils.getUserVO().getEmail()), User.class))
            .thenReturn(user);
        when(eventRepo.save(event)).thenReturn(event);

        eventService.addAttender(1L, "danylo@gmail.com");

        verify(eventRepo).findById(any());
        verify(modelMapper).map(restClient.findByEmail(ModelUtils.getUserVO().getEmail()), User.class);
        verify(eventRepo).save(event);
    }

    @Test
    void addAttenderToOpenEventIfAttenderIsOrganizerException() {
        Event event = ModelUtils.getEvent();
        User user = ModelUtils.getUser();

        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(modelMapper.map(restClient.findByEmail(ModelUtils.getUserVO().getEmail()), User.class))
            .thenReturn(user);

        assertThrows(BadRequestException.class, () -> eventService.addAttender(1L, TestConst.EMAIL));

        verify(eventRepo).findById(any());
        verify(modelMapper).map(restClient.findByEmail(ModelUtils.getUserVO().getEmail()), User.class);
        verify(eventRepo, never()).save(event);
    }

    @Test
    void addAttenderToOpenEventIfAttenderAlreadySubscribedException() {
        Event event = ModelUtils.getEvent();
        User user = ModelUtils.getAttenderUser();
        Set<User> userSet = new HashSet<>();
        userSet.add(ModelUtils.getAttenderUser());
        event.setAttenders(userSet);

        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(modelMapper.map(restClient.findByEmail(ModelUtils.getUserVO().getEmail()), User.class))
            .thenReturn(user);

        assertThrows(BadRequestException.class, () -> eventService.addAttender(1L, "danylo@gmail.com"));

        verify(eventRepo).findById(any());
        verify(modelMapper).map(restClient.findByEmail(ModelUtils.getUserVO().getEmail()), User.class);
        verify(eventRepo, never()).save(event);
    }

    @Test
    void addAttenderToCloseEvent() {
        Event event = ModelUtils.getCloseEvent();
        User user = ModelUtils.getAttenderUser();

        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(modelMapper.map(restClient.findByEmail(ModelUtils.getUserVO().getEmail()), User.class))
            .thenReturn(user);
        when(userRepo.findUserByIdAndByFriendId(2L, 1L)).thenReturn(Optional.of(user));
        when(eventRepo.save(event)).thenReturn(event);

        eventService.addAttender(1L, "danylo@gmail.com");

        verify(eventRepo).findById(any());
        verify(modelMapper).map(restClient.findByEmail(ModelUtils.getUserVO().getEmail()), User.class);
        verify(userRepo).findUserByIdAndByFriendId(2L, 1L);
        verify(eventRepo).save(event);
    }

    @Test
    void addAttenderToCloseEventIfAttenderIsOrganizerException() {
        Event event = ModelUtils.getCloseEvent();
        User user = ModelUtils.getUser();

        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(modelMapper.map(restClient.findByEmail(ModelUtils.getUserVO().getEmail()), User.class))
            .thenReturn(user);

        assertThrows(BadRequestException.class, () -> eventService.addAttender(1L, TestConst.EMAIL));

        verify(eventRepo).findById(any());
        verify(modelMapper).map(restClient.findByEmail(ModelUtils.getUserVO().getEmail()), User.class);
        verify(eventRepo, never()).save(event);
    }

    @Test
    void addAttenderToCloseEventIfAttenderAlreadySubscribedException() {
        Event event = ModelUtils.getCloseEvent();
        User user = ModelUtils.getAttenderUser();
        Set<User> userSet = new HashSet<>();
        userSet.add(ModelUtils.getAttenderUser());
        event.setAttenders(userSet);

        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(modelMapper.map(restClient.findByEmail(ModelUtils.getUserVO().getEmail()), User.class))
            .thenReturn(user);

        assertThrows(BadRequestException.class, () -> eventService.addAttender(1L, "danylo@gmail.com"));

        verify(eventRepo).findById(any());
        verify(modelMapper).map(restClient.findByEmail(ModelUtils.getUserVO().getEmail()), User.class);
        verify(eventRepo, never()).save(event);
    }

    @Test
    void addAttenderToCloseEventIfAttenderIsNotAFriendException() {
        Event event = ModelUtils.getCloseEvent();
        User user = ModelUtils.getAttenderUser();

        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(modelMapper.map(restClient.findByEmail(ModelUtils.getUserVO().getEmail()), User.class))
            .thenReturn(user);
        when(userRepo.findUserByIdAndByFriendId(2L, 1L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> eventService.addAttender(1L, "danylo@gmail.com"));

        verify(eventRepo).findById(any());
        verify(modelMapper).map(restClient.findByEmail(ModelUtils.getUserVO().getEmail()), User.class);
        verify(userRepo).findUserByIdAndByFriendId(2L, 1L);
        verify(eventRepo, never()).save(event);
    }

    @Test
    void addAttenderToEventIfEventNotFoundException() {
        when(eventRepo.findById(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> eventService.addAttender(1L, "danylo@gmail.com"));
        verify(eventRepo).findById(any());
    }

    @Test
    void addToFavoritesTest() {
        Event event = ModelUtils.getEvent();
        User user = ModelUtils.getUser();
        user.setId(2L);

        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(userRepo.findByEmail(TestConst.EMAIL)).thenReturn(Optional.of(user));
        when(eventRepo.save(event)).thenReturn(event);

        eventService.addToFavorites(1L, TestConst.EMAIL);

        verify(eventRepo).findById(any());
        verify(userRepo).findByEmail(TestConst.EMAIL);
        verify(eventRepo).save(event);
    }

    @Test
    void addToFavoritesThrowsExceptionWhenEventNotFoundTest() {
        when(eventRepo.findById(any())).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> eventService.addToFavorites(1L, TestConst.EMAIL));
        verify(eventRepo).findById(any());
    }

    @Test
    void addToFavoritesThrowsExceptionWhenUserNotFoundTest() {
        when(userRepo.findById(any())).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> eventService.addToFavorites(1L, TestConst.EMAIL));
        verify(eventRepo).findById(any());
    }

    @Test
    void addToFavoritesThrowsExceptionWhenUserHasAlreadyAddedEventToFavoritesTest() {
        Event event = ModelUtils.getEvent();
        User user = ModelUtils.getUser();

        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(userRepo.findByEmail(TestConst.EMAIL)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> eventService.addToFavorites(1L, TestConst.EMAIL));

        verify(eventRepo).findById(any());
        verify(userRepo).findByEmail(TestConst.EMAIL);
    }

    @Test
    void removeFromFavoritesTest() {
        Event event = ModelUtils.getEvent();
        User user = ModelUtils.getUser();

        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(userRepo.findByEmail(TestConst.EMAIL)).thenReturn(Optional.of(user));
        when(eventRepo.save(event)).thenReturn(event);

        eventService.removeFromFavorites(1L, TestConst.EMAIL);

        verify(eventRepo).findById(any());
        verify(userRepo).findByEmail(TestConst.EMAIL);
        verify(eventRepo).save(event);
    }

    @Test
    void removeFromFavoritesThrowsExceptionWhenEventNotFoundTest() {
        when(eventRepo.findById(any())).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> eventService.removeFromFavorites(1L, TestConst.EMAIL));
        verify(eventRepo).findById(any());
    }

    @Test
    void removeFromFavoritesThrowsExceptionWhenUserNotFoundTest() {
        when(userRepo.findById(any())).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> eventService.removeFromFavorites(1L, TestConst.EMAIL));
        verify(eventRepo).findById(any());
    }

    @Test
    void removeFromFavoritesThrowsExceptionWhenEventIsNotInFavoritesTest() {
        Event event = ModelUtils.getEvent();
        User user = ModelUtils.getUser();
        user.setId(2L);

        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(userRepo.findByEmail(TestConst.EMAIL)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> eventService.removeFromFavorites(1L, TestConst.EMAIL));

        verify(eventRepo).findById(any());
        verify(userRepo).findByEmail(TestConst.EMAIL);
    }

    @Test
    void removeAttender() {
        Event event = ModelUtils.getEvent();
        Set<User> userSet = new HashSet<>();
        User user = ModelUtils.getUser();
        user.setId(22L);
        userSet.add(user);
        event.setAttenders(userSet);
        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(restClient.findByEmail(user.getEmail())).thenReturn(ModelUtils.getUserVO());

        eventService.removeAttender(event.getId(), user.getEmail());

        verify(eventRepo).save(event);
        verify(restClient).findByEmail(user.getEmail());
    }

    @Test
    void searchTest() {
        List<Event> events = List.of(ModelUtils.getEvent());
        PageRequest pageRequest = PageRequest.of(0, 2);
        Page<Event> page = new PageImpl<>(events, pageRequest, events.size());

        when(eventRepo.find(pageRequest, "search", null, null)).thenReturn(page);
        when(modelMapper.map(ModelUtils.getEvent(), SearchEventsDto.class)).thenReturn(ModelUtils.getSearchEventsDto());

        PageableDto<SearchEventsDto> searched = eventService.search(pageRequest, "search", null, null);

        assertEquals(List.of(ModelUtils.getSearchEventsDto()), searched.getPage());
    }

    @Test
    void searchEventsBy() {
        List<Event> events = Collections.singletonList(ModelUtils.getEvent());
        List<EventDto> eventDtos = Collections.singletonList(ModelUtils.getEventDto());
        PageRequest pageRequest = PageRequest.of(0, 2);
        Page<Event> page = new PageImpl<>(events, pageRequest, events.size());
        when(eventRepo.searchEventsBy(pageRequest, "query")).thenReturn(page);
        PageableAdvancedDto<EventDto> expected =
            new PageableAdvancedDto<>(eventDtos, eventDtos.size(), 0, 1,
                0, false, false, true, true);
        assertEquals(expected.getTotalPages(), eventService.searchEventsBy(pageRequest, "query")
            .getTotalPages());
    }

    @Test
    void rateEvent() {
        Event event = ModelUtils.getEventWithFinishedDate();
        User user = ModelUtils.getAttenderUser();
        event.setAttenders(Set.of(user));
        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        doNothing().when(userService).updateEventOrganizerRating(event.getOrganizer().getId(), 2.0);
        List<Event> events = List.of(event, ModelUtils.getExpectedEvent(), ModelUtils.getEventWithGrades());
        when(eventRepo.getAllByOrganizer(event.getOrganizer())).thenReturn(events);
        eventService.rateEvent(event.getId(), user.getEmail(), 2);
        verify(eventRepo).save(event);
    }

    @Test
    void rateEventEventNotExistsNotFoundExceptionThrownTest() {
        long notExistsEventId = 999L;
        String userEmail = ModelUtils.testEmail;
        when(eventRepo.findById(notExistsEventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.rateEvent(notExistsEventId, userEmail, 2))
            .isInstanceOf(NotFoundException.class).hasMessage(ErrorMessage.EVENT_NOT_FOUND);

        verify(eventRepo, times(0)).save(any());
        verify(userService, times(0)).updateEventOrganizerRating(anyLong(), anyDouble());
    }

    @Test
    void rateEventUserRatesOwnEventUserHasNoPermissionToAccessExceptionThrownTest() {
        Event event = ModelUtils.getEventWithFinishedDate();
        User user = event.getOrganizer();
        Long eventId = event.getId();
        String userEmail = user.getEmail();
        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> eventService.rateEvent(eventId, userEmail, 2))
            .isInstanceOf(UserHasNoPermissionToAccessException.class)
            .hasMessage(ErrorMessage.USER_HAS_NO_RIGHTS_TO_RATE_EVENT);

        verify(eventRepo, times(0)).save(event);
        verify(userService, times(0)).updateEventOrganizerRating(anyLong(), anyDouble());
    }

    @Test
    void rateEventUserRatesNotFinishedEventYetBadRequestExceptionThrownTest() {
        Event event = ModelUtils.getEventNotStartedYet();
        User user = ModelUtils.getTestUser();
        Long eventId = event.getId();
        String userEmail = user.getEmail();
        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> eventService.rateEvent(eventId, userEmail, 2))
            .isInstanceOf(BadRequestException.class).hasMessage(ErrorMessage.EVENT_IS_NOT_FINISHED);

        verify(eventRepo, times(0)).save(event);
        verify(userService, times(0)).updateEventOrganizerRating(anyLong(), anyDouble());
    }

    @Test
    void rateEventUserNotEventSubscriberBadRequestExceptionThrownTest() {
        Event event = ModelUtils.getEventWithFinishedDate();
        User user = ModelUtils.getTestUser();
        Long eventId = event.getId();
        String userEmail = user.getEmail();
        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> eventService.rateEvent(eventId, userEmail, 2))
            .isInstanceOf(BadRequestException.class).hasMessage(ErrorMessage.YOU_ARE_NOT_EVENT_SUBSCRIBER);

        verify(eventRepo, times(0)).save(event);
        verify(userService, times(0)).updateEventOrganizerRating(anyLong(), anyDouble());
    }

    @Test
    void rateEventUserAlreadyRatedEventBadRequestExceptionThrownTest() {
        Event event = ModelUtils.getEventWithFinishedDate();
        User userWhoRatesEvent = ModelUtils.getTestUser();
        Long eventId = event.getId();
        String userEmail = userWhoRatesEvent.getEmail();
        event.setAttenders(Set.of(userWhoRatesEvent));
        event.setEventGrades(List.of(EventGrade.builder().grade(2).event(event).user(userWhoRatesEvent).build()));
        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(userRepo.findByEmail(userWhoRatesEvent.getEmail())).thenReturn(Optional.of(userWhoRatesEvent));

        assertThatThrownBy(() -> eventService.rateEvent(eventId, userEmail, 2))
            .isInstanceOf(BadRequestException.class).hasMessage(ErrorMessage.HAVE_ALREADY_RATED);

        verify(eventRepo, times(0)).save(event);
        verify(userService, times(0)).updateEventOrganizerRating(anyLong(), anyDouble());
    }

    @Test
    void getAllEventAttenders() {
        Event event = ModelUtils.getEvent();
        Set<EventAttenderDto> eventAttenderDtos = Set.of(ModelUtils.getEventAttenderDto());
        User user = ModelUtils.getAttenderUser();
        event.setAttenders(Set.of(user));

        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(modelMapper.map(user, EventAttenderDto.class)).thenReturn(ModelUtils.getEventAttenderDto());

        assertEquals(eventService.getAllEventAttenders(event.getId()), eventAttenderDtos);

        verify(modelMapper).map(user, EventAttenderDto.class);
    }

    @Test
    void findByIdTest() {
        Event event = ModelUtils.getEvent();
        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(eventService.findById(1L)).thenReturn(ModelUtils.getEventVO());
        assertEquals(eventService.findById(1L), ModelUtils.getEventVO());
        verify(eventRepo, times(2)).findById(any());
    }

    @Test
    void findByIdThrowNotFoundExceptionTest() {
        when(eventRepo.findById(any())).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> eventService.findById(1L));
        verify(eventRepo).findById(any());
    }

    @Test
    void getEventsForAuthorizedUserTest() {
        Pageable pageable = PageRequest.of(0, 6);
        Long userId = 1L;
        FilterEventDto filterEventDto = getFilterEventDto();
        Page<Long> idsPage = new PageImpl<>(List.of(3L, 1L), pageable, 2);
        TupleElement<?>[] elements = getTupleElements();

        List<Tuple> tuples = getTuples(elements);
        List<EventDto> eventPreviewDtoList = getEventPreviewDtos();
        PageableAdvancedDto<EventDto> eventPreviewDtoPage = new PageableAdvancedDto<>(
            eventPreviewDtoList,
            idsPage.getTotalElements(),
            pageable.getPageNumber(),
            idsPage.getTotalPages(),
            idsPage.getNumber(),
            idsPage.hasPrevious(),
            idsPage.hasNext(),
            idsPage.isFirst(),
            idsPage.isLast());
        when(restClient.findById(userId)).thenReturn(getUserVO());
        when(eventRepo.findEventsIds(pageable, filterEventDto, userId)).thenReturn(idsPage);
        when(eventRepo.loadEventDataByIds(idsPage.getContent(), userId)).thenReturn(tuples);

        PageableAdvancedDto<EventDto> result = eventService.getEvents(pageable, filterEventDto, userId);
        assertEquals(eventPreviewDtoPage, result);

        verify(restClient).findById(userId);
        verify(eventRepo).findEventsIds(pageable, filterEventDto, userId);
        verify(eventRepo).loadEventDataByIds(idsPage.getContent(), userId);
    }

    @Test
    void testGetEventsForAuthorizedUserExceedingPageTest() {
        int requestedPage = 10;
        int totalPages = 2;
        int pageSize = 6;
        Pageable pageable = PageRequest.of(requestedPage, pageSize);
        Long userId = 1L;
        FilterEventDto filterEventDto = getFilterEventDto();

        Page<Long> idsPage = new PageImpl<>(
            List.of(3L, 1L),
            PageRequest.of(0, pageSize),
            totalPages * pageSize);

        when(restClient.findById(userId)).thenReturn(getUserVO());
        when(eventRepo.findEventsIds(pageable, filterEventDto, userId)).thenReturn(idsPage);

        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> eventService.getEvents(pageable, filterEventDto, userId),
            "Expected BadRequestException to be thrown");

        String expectedMessage = String.format("Requested page %d exceeds total pages %d.", requestedPage, totalPages);
        assertEquals(expectedMessage, exception.getMessage());

        verify(restClient).findById(userId);
        verify(eventRepo).findEventsIds(pageable, filterEventDto, userId);
        verify(eventRepo, never()).loadEventDataByIds(anyList(), anyLong());
    }

    @Test
    void getEventsForUnauthorizedUserTest() {
        Pageable pageable = PageRequest.of(0, 6);
        FilterEventDto filterEventDto = getFilterEventDto();
        Page<Long> idsPage = new PageImpl<>(List.of(3L, 1L), pageable, 2);
        TupleElement<?>[] elements = getTupleElements();

        List<Tuple> tuples = getTuples(elements);
        List<EventDto> eventPreviewDtoList = getEventPreviewDtos();
        PageableAdvancedDto<EventDto> eventPreviewDtoPage = new PageableAdvancedDto<>(
            eventPreviewDtoList,
            idsPage.getTotalElements(),
            pageable.getPageNumber(),
            idsPage.getTotalPages(),
            idsPage.getNumber(),
            idsPage.hasPrevious(),
            idsPage.hasNext(),
            idsPage.isFirst(),
            idsPage.isLast());
        when(eventRepo.findEventsIds(pageable, filterEventDto, null)).thenReturn(idsPage);
        when(eventRepo.loadEventDataByIds(idsPage.getContent())).thenReturn(tuples);

        PageableAdvancedDto<EventDto> result = eventService.getEvents(pageable, filterEventDto, null);
        assertEquals(eventPreviewDtoPage, result);

        verify(eventRepo).findEventsIds(pageable, filterEventDto, null);
        verify(eventRepo).loadEventDataByIds(idsPage.getContent());
    }

    @Test
    void getEventsManagementForAuthorizedUserTest() {
        Pageable pageable = PageRequest.of(0, 6);
        Long userId = 1L;
        FilterEventDto filterEventDto = getFilterEventDto();
        Page<Long> idsPage = new PageImpl<>(List.of(3L, 1L), pageable, 2);
        TupleElement<?>[] elements = getTupleElements();

        List<Tuple> tuples = getTuples(elements);
        List<EventDto> eventPreviewDtoList = getEventPreviewDtos();
        PageableAdvancedDto<EventDto> eventPreviewDtoPage = new PageableAdvancedDto<>(
            eventPreviewDtoList,
            idsPage.getTotalElements(),
            pageable.getPageNumber(),
            idsPage.getTotalPages(),
            idsPage.getNumber(),
            idsPage.hasPrevious(),
            idsPage.hasNext(),
            idsPage.isFirst(),
            idsPage.isLast());
        when(restClient.findById(userId)).thenReturn(getUserVO());
        when(eventRepo.findEventsIdsManagement(pageable, filterEventDto, userId)).thenReturn(idsPage);
        when(eventRepo.loadEventDataByIds(idsPage.getContent(), userId)).thenReturn(tuples);

        PageableAdvancedDto<EventDto> result = eventService.getEventsManagement(pageable, filterEventDto, userId);
        assertEquals(eventPreviewDtoPage, result);

        verify(restClient).findById(userId);
        verify(eventRepo).findEventsIdsManagement(pageable, filterEventDto, userId);
        verify(eventRepo).loadEventDataByIds(idsPage.getContent(), userId);
    }

    @Test
    void getEventsManagementForUnauthorizedUserTest() {
        Pageable pageable = PageRequest.of(0, 6);
        FilterEventDto filterEventDto = getFilterEventDto();
        Page<Long> idsPage = new PageImpl<>(List.of(3L, 1L), pageable, 2);
        TupleElement<?>[] elements = getTupleElements();

        List<Tuple> tuples = getTuples(elements);
        List<EventDto> eventPreviewDtoList = getEventPreviewDtos();
        PageableAdvancedDto<EventDto> eventPreviewDtoPage = new PageableAdvancedDto<>(
            eventPreviewDtoList,
            idsPage.getTotalElements(),
            pageable.getPageNumber(),
            idsPage.getTotalPages(),
            idsPage.getNumber(),
            idsPage.hasPrevious(),
            idsPage.hasNext(),
            idsPage.isFirst(),
            idsPage.isLast());
        when(eventRepo.findEventsIdsManagement(pageable, filterEventDto, null)).thenReturn(idsPage);
        when(eventRepo.loadEventDataByIds(idsPage.getContent())).thenReturn(tuples);

        PageableAdvancedDto<EventDto> result = eventService.getEventsManagement(pageable, filterEventDto, null);
        assertEquals(eventPreviewDtoPage, result);

        verify(eventRepo).findEventsIdsManagement(pageable, filterEventDto, null);
        verify(eventRepo).loadEventDataByIds(idsPage.getContent());
    }

    @Test
    void testCheckTitleImageInImagesToDelete_TitleImageInImagesToDelete_AdditionalImagesNotEmpty() throws Exception {
        UpdateEventDto updateEventDto = ModelUtils.getUpdateEventDto();
        updateEventDto.setTitleImage("titleImage");
        updateEventDto.setAdditionalImages(List.of("newTitleImage", "additionalImage"));

        List<String> imagesToDelete = List.of("titleImage");
        when(eventRepo.findAllImagesLinksByEventId(updateEventDto.getId())).thenReturn(imagesToDelete);

        Method method = EventServiceImpl.class.getDeclaredMethod("checkTitleImageInImagesToDelete",
            UpdateEventDto.class, List.class);
        method.setAccessible(true);
        method.invoke(eventService, updateEventDto, imagesToDelete);

        assertEquals("newTitleImage", updateEventDto.getTitleImage());
        assertEquals(1, updateEventDto.getAdditionalImages().size());
        assertEquals("additionalImage", updateEventDto.getAdditionalImages().getFirst());
    }

    @Test
    void testCheckTitleImageInImagesToDelete_TitleImageInImagesToDelete_AdditionalImagesEmpty() throws Exception {
        UpdateEventDto updateEventDto = ModelUtils.getUpdateEventDto();
        updateEventDto.setTitleImage("titleImage");
        updateEventDto.setAdditionalImages(Collections.emptyList());

        List<String> imagesToDelete = List.of("titleImage");
        when(eventRepo.findAllImagesLinksByEventId(updateEventDto.getId())).thenReturn(imagesToDelete);

        Method method = EventServiceImpl.class.getDeclaredMethod("checkTitleImageInImagesToDelete",
            UpdateEventDto.class, List.class);
        method.setAccessible(true);
        method.invoke(eventService, updateEventDto, imagesToDelete);

        assertNull(updateEventDto.getTitleImage());
    }

    @Test
    void testCheckTitleImageInImagesToDelete_TitleImageNotInImagesToDelete() throws Exception {
        UpdateEventDto updateEventDto = ModelUtils.getUpdateEventDto();
        updateEventDto.setTitleImage("titleImage");
        updateEventDto.setAdditionalImages(List.of("additionalImage"));

        List<String> imagesToDelete = List.of("additionalImage");
        when(eventRepo.findAllImagesLinksByEventId(updateEventDto.getId())).thenReturn(imagesToDelete);

        Method method = EventServiceImpl.class.getDeclaredMethod("checkTitleImageInImagesToDelete",
            UpdateEventDto.class, List.class);
        method.setAccessible(true);
        method.invoke(eventService, updateEventDto, imagesToDelete);

        assertEquals("titleImage", updateEventDto.getTitleImage());
        assertEquals(1, updateEventDto.getAdditionalImages().size());
        assertEquals("additionalImage", updateEventDto.getAdditionalImages().getFirst());
    }

    @Test
    void testCheckTitleImageInImagesToDelete_NoTitleImage() throws Exception {
        UpdateEventDto updateEventDto = ModelUtils.getUpdateEventDto();
        updateEventDto.setTitleImage(null);
        updateEventDto.setAdditionalImages(List.of("additionalImage"));

        List<String> imagesToDelete = List.of("titleImage");
        when(eventRepo.findAllImagesLinksByEventId(updateEventDto.getId())).thenReturn(imagesToDelete);

        Method method = EventServiceImpl.class.getDeclaredMethod("checkTitleImageInImagesToDelete",
            UpdateEventDto.class, List.class);
        method.setAccessible(true);
        method.invoke(eventService, updateEventDto, imagesToDelete);

        assertNull(updateEventDto.getTitleImage());
    }

    @Test
    void testCheckTitleImageInImagesToDelete_NoImagesToDelete() throws Exception {
        UpdateEventDto updateEventDto = ModelUtils.getUpdateEventDto();
        updateEventDto.setTitleImage("titleImage");
        updateEventDto.setAdditionalImages(List.of("additionalImage"));

        when(eventRepo.findAllImagesLinksByEventId(updateEventDto.getId())).thenReturn(null);

        Method method = EventServiceImpl.class.getDeclaredMethod("checkTitleImageInImagesToDelete",
            UpdateEventDto.class, List.class);
        method.setAccessible(true);
        method.invoke(eventService, updateEventDto, null);

        assertEquals("titleImage", updateEventDto.getTitleImage());
        assertEquals(1, updateEventDto.getAdditionalImages().size());
        assertEquals("additionalImage", updateEventDto.getAdditionalImages().getFirst());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenDurationIsLessThan30Minutes() throws Exception {
        List<EventDateLocationDto> eventDateLocationDtos = ModelUtils.getEventDateLocationDtoWithInvalidDuration();

        Method method = EventServiceImpl.class.getDeclaredMethod(
            "checkingEqualityDateTimeInEventDateLocationDto", List.class);
        method.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            method.invoke(eventService, eventDateLocationDtos);
        });

        Throwable cause = exception.getCause();
        assertInstanceOf(IllegalArgumentException.class, cause);
        assertEquals(ErrorMessage.INVALID_DURATION_BETWEEN_START_AND_FINISH, cause.getMessage());
    }

    @Test
    void getAllEventAddressesTest() {
        AddressDto expectedAddressDto = ModelUtils.getAddressDto();
        List<AddressDto> expectedAddresses = List.of(expectedAddressDto);
        Address address = ModelUtils.getAddress();

        when(eventRepo.findAllEventsAddresses()).thenReturn(List.of(address));
        when(modelMapper.map(address, AddressDto.class)).thenReturn(expectedAddressDto);

        List<AddressDto> actualAddresses = eventService.getAllEventsAddresses();

        assertEquals(expectedAddresses, actualAddresses);
        assertTrue(actualAddresses.contains(expectedAddressDto));

        verify(eventRepo).findAllEventsAddresses();
        verify(modelMapper).map(address, AddressDto.class);
    }

    @Test
    void getCountOfAttendedEventsByUserIdTest() {
        Long userId = 1L;

        when(eventRepo.countDistinctByAttendersId(userId)).thenReturn(5L);

        Long countOfAttendedEventsByUserId = eventService.getCountOfAttendedEventsByUserId(userId);

        assertEquals(5L, countOfAttendedEventsByUserId);
        verify(eventRepo).countDistinctByAttendersId(userId);
    }

    @Test
    void getCountOfOrganizedEventsByUserIdTest() {
        Long userId = 1L;

        when(eventRepo.countDistinctByOrganizerId(userId)).thenReturn(5L);

        Long countOfOrganizedEventsByUserId = eventService.getCountOfOrganizedEventsByUserId(userId);

        assertEquals(5L, countOfOrganizedEventsByUserId);
        verify(eventRepo).countDistinctByOrganizerId(userId);
    }

    @Test
    void likeTest() {
        UserVO userVO = getUserVO();
        UserVO eventAuthorVO = getAuthorVO();
        User user = getUser();
        User eventAuthor = getUser().setId(2L);
        Event event = getEvent().setOrganizer(eventAuthor);
        RatingPoints ratingPoints = RatingPoints.builder().id(1L).name("LIKE_EVENT").points(1).build();

        when(eventRepo.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepo.findById(eventAuthor.getId())).thenReturn(Optional.of(eventAuthor));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(modelMapper.map(eventAuthor, UserVO.class)).thenReturn(eventAuthorVO);
        when(ratingPointsRepo.findByNameOrThrow("LIKE_EVENT")).thenReturn(ratingPoints);

        eventService.like(event.getId(), userVO);

        assertTrue(event.getUsersLikedEvents().stream().anyMatch(u -> u.getId().equals(userVO.getId())));

        verify(userNotificationService, times(1)).createOrUpdateLikeNotification(any(LikeNotificationDto.class));
        verify(eventRepo).findById(event.getId());
        verify(userRepo).findById(eventAuthor.getId());
        verify(modelMapper).map(userVO, User.class);
        verify(modelMapper).map(eventAuthor, UserVO.class);
    }

    @Test
    void testLikeOwn() {
        UserVO userVO = getUserVO();
        User user = getUser();
        Event event = getEvent();

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(BadRequestException.class, () -> eventService.like(1L, userVO));
    }

    @Test
    void removeLikeTest() {
        UserVO userVO = getUserVO();
        User eventAuthor = getUser().setId(2L);
        User user = getUser();
        Event event = getEvent().setOrganizer(eventAuthor);
        event.getUsersLikedEvents().add(user);
        RatingPoints ratingPoints = RatingPoints.builder().id(1L).name("UNDO_LIKE_EVENT").points(-1).build();

        when(eventRepo.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepo.findById(2L)).thenReturn(Optional.of(eventAuthor));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(ratingPointsRepo.findByNameOrThrow("UNDO_LIKE_EVENT")).thenReturn(ratingPoints);

        eventService.like(event.getId(), userVO);
        assertFalse(event.getUsersLikedEvents().stream().anyMatch(u -> u.getId().equals(userVO.getId())));

        verify(userNotificationService, times(1)).removeActionUserFromNotification(
            modelMapper.map(user, UserVO.class), userVO, event.getId(), NotificationType.EVENT_LIKE);
        verify(eventRepo).findById(event.getId());
        verify(userRepo).findById(eventAuthor.getId());
    }

    @Test
    void likeEventNotFoundTest() {
        UserVO userVO = getUserVO();
        Event event = getEvent();
        Long eventId = event.getId();

        when(eventRepo.findById(event.getId())).thenReturn(Optional.empty());

        NotFoundException exception =
            assertThrows(NotFoundException.class, () -> eventService.like(eventId, userVO));
        assertEquals(ErrorMessage.EVENT_NOT_FOUND_BY_ID + event.getId(), exception.getMessage());

        verify(eventRepo).findById(event.getId());
    }

    @Test
    void likeEventUserNotFoundTest() {
        UserVO userVO = getUserVO();
        User user = getUser();
        Event event = getEvent();
        Long eventId = event.getId();

        when(eventRepo.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepo.findById(user.getId())).thenReturn(Optional.empty());

        NotFoundException exception =
            assertThrows(NotFoundException.class, () -> eventService.like(eventId, userVO));
        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + user.getId(), exception.getMessage());

        verify(eventRepo).findById(event.getId());
        verify(userRepo).findById(user.getId());
    }

    @Test
    void likeWithNullEventOrganizerTest() {
        UserVO userVO = getUserVO();
        User user = getUser();
        Event event = getEvent();

        event.setOrganizer(null);

        when(eventRepo.findById(event.getId())).thenReturn(Optional.of(event));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);

        eventService.like(event.getId(), userVO);

        assertTrue(event.getUsersLikedEvents().stream().anyMatch(u -> u.getId().equals(userVO.getId())));

        verify(eventRepo).findById(event.getId());
        verify(userRepo, never()).findById(anyLong());
        verify(userNotificationService, never()).removeActionUserFromNotification(any(), any(), anyLong(), any());
    }

    @Test
    void removeLikeWithNullEventOrganizerTest() {
        UserVO userVO = getUserVO();
        User user = getUser();
        Event event = getEvent();

        event.setOrganizer(null);
        event.getUsersLikedEvents().add(user);

        when(eventRepo.findById(event.getId())).thenReturn(Optional.of(event));

        eventService.like(event.getId(), userVO);

        assertFalse(event.getUsersLikedEvents().stream().anyMatch(u -> u.getId().equals(userVO.getId())));

        verify(eventRepo).findById(event.getId());
        verify(userRepo, never()).findById(anyLong());
        verify(userNotificationService, never()).removeActionUserFromNotification(any(), any(), anyLong(), any());
    }

    @Test
    void givenEventDislikedByUser_whenLikedByUser_shouldRemoveDislikeAddLike() {
        UserVO userVO = getUserVO();
        User user = getUser();
        User eventAuthor = getUser().setId(2L);
        Event event = getEvent().setOrganizer(eventAuthor);
        event.setUsersLikedEvents(new HashSet<>());
        event.setUsersDislikedEvents(new HashSet<>(Set.of(user)));

        when(eventRepo.findById(anyLong())).thenReturn(Optional.of(event));
        when(userRepo.findById(event.getOrganizer().getId())).thenReturn(Optional.of(eventAuthor));

        eventService.like(1L, userVO);

        assertEquals(0, event.getUsersDislikedEvents().size());
        assertEquals(1, event.getUsersLikedEvents().size());
    }

    @Test
    void dislikeTest() {
        UserVO userVO = getUserVO();
        User eventAuthor = getUser().setId(2L);
        Event event = getEvent().setOrganizer(eventAuthor);
        event.setUsersDislikedEvents(new HashSet<>());

        when(eventRepo.findById(anyLong())).thenReturn(Optional.of(event));
        when(userRepo.findById(event.getOrganizer().getId())).thenReturn(Optional.of(eventAuthor));

        eventService.dislike(userVO, event.getId());

        verify(eventRepo).save(event);
        assertEquals(1L, event.getUsersDislikedEvents().size());
    }

    @Test
    void testDislikeOwn() {
        UserVO userVO = getUserVO();
        User user = getUser();
        Event event = getEvent();

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(BadRequestException.class, () -> eventService.dislike(userVO, 1L));
    }

    @Test
    void testDislikeIfWasAlreadyPlaced() {
        UserVO userVO = getUserVO();
        User user = getUser();
        User eventAuthor = getUser().setId(2L);
        Event event = getEvent().setOrganizer(eventAuthor);
        Set<User> usersDisliked = new HashSet<>();
        usersDisliked.add(user);
        event.setUsersDislikedEvents(usersDisliked);

        when(eventRepo.findById(anyLong())).thenReturn(Optional.of(event));
        when(userRepo.findById(event.getOrganizer().getId())).thenReturn(Optional.of(eventAuthor));
        eventService.dislike(userVO, event.getId());

        verify(eventRepo).save(event);
        assertEquals(0L, event.getUsersDislikedEvents().size());
    }

    @Test
    void givenEventLikedByUser_whenDislikedByUser_shouldRemoveLikeAndAddDislike() {
        UserVO userVO = getUserVO();
        User user = getUser();
        User eventAuthor = getUser().setId(2L);
        Event event = getEvent().setOrganizer(eventAuthor);
        event.setUsersLikedEvents(new HashSet<>(Set.of(user)));
        event.setUsersDislikedEvents(new HashSet<>());

        when(eventRepo.findById(anyLong())).thenReturn(Optional.of(event));
        when(userRepo.findById(event.getOrganizer().getId())).thenReturn(Optional.of(eventAuthor));

        eventService.dislike(userVO, 1L);

        assertEquals(0, event.getUsersLikedEvents().size());
        assertEquals(1, event.getUsersDislikedEvents().size());
    }

    @Test
    void countLikesForEventTest() {
        Event event = getEvent();
        event.setUsersLikedEvents(getUsersHashSet());

        when(eventRepo.findById(event.getId())).thenReturn(Optional.of(event));

        int actualAmountOfLikes = eventService.countLikes(event.getId());

        assertEquals(2, actualAmountOfLikes);
        verify(eventRepo).findById(event.getId());
    }

    @Test
    void countLikesForEvent_ThrowNotFoundException_Test() {
        Event event = getEvent();
        Long eventId = event.getId();

        NotFoundException exception =
            assertThrows(NotFoundException.class, () -> eventService.countLikes(eventId));
        assertEquals(ErrorMessage.EVENT_NOT_FOUND_BY_ID + event.getId(), exception.getMessage());

        assertTrue(exception.getMessage().contains(ErrorMessage.EVENT_NOT_FOUND_BY_ID + event.getId()));
        verify(eventRepo).findById(event.getId());
    }

    @Test
    void countDislikesForEventTest() {
        Event event = getEvent();
        event.setUsersDislikedEvents(getUsersHashSet());

        when(eventRepo.findById(event.getId())).thenReturn(Optional.of(event));

        int actualAmountOfLikes = eventService.countDislikes(event.getId());

        assertEquals(2, actualAmountOfLikes);
        verify(eventRepo).findById(event.getId());
    }

    @Test
    void countDislikesForEvent_ThrowNotFoundException_Test() {
        Event event = getEvent();
        Long eventId = event.getId();

        NotFoundException exception =
            assertThrows(NotFoundException.class, () -> eventService.countDislikes(eventId));
        assertEquals(ErrorMessage.EVENT_NOT_FOUND_BY_ID + event.getId(), exception.getMessage());

        assertTrue(exception.getMessage().contains(ErrorMessage.EVENT_NOT_FOUND_BY_ID + event.getId()));
        verify(eventRepo).findById(event.getId());
    }

    @Test
    void checkIsEventLikedByUserTest() {
        User user = getUser();
        UserVO userVO = getUserVO();
        Event event = getEvent();
        Set<User> usersLiked = new HashSet<>();
        usersLiked.add(user);
        event.setUsersLikedEvents(usersLiked);
        when(eventRepo.findById(event.getId())).thenReturn(Optional.of(event));

        boolean isLikedByUser = eventService.isEventLikedByUser(event.getId(), userVO);

        assertTrue(isLikedByUser);
        verify(eventRepo).findById(event.getId());
    }

    @Test
    void checkIsEventLikedByUserTest_ThrowNotFoundException_Test() {
        Event event = getEvent();
        UserVO userVO = getUserVO();
        Long eventId = event.getId();

        NotFoundException exception =
            assertThrows(NotFoundException.class, () -> eventService.isEventLikedByUser(eventId, userVO));
        assertEquals(ErrorMessage.EVENT_NOT_FOUND_BY_ID + event.getId(), exception.getMessage());

        assertTrue(exception.getMessage().contains(ErrorMessage.EVENT_NOT_FOUND_BY_ID + event.getId()));
        verify(eventRepo, times(1)).findById(event.getId());
    }

    @Test
    void checkIsEventDislikedByUserTest() {
        User user = getUser();
        UserVO userVO = getUserVO();
        Event event = getEvent();
        Set<User> usersDisliked = new HashSet<>();
        usersDisliked.add(user);
        event.setUsersDislikedEvents(usersDisliked);
        when(eventRepo.findById(event.getId())).thenReturn(Optional.of(event));

        boolean isDislikedByUser = eventService.isEventDislikedByUser(event.getId(), userVO);

        assertTrue(isDislikedByUser);
        verify(eventRepo).findById(event.getId());
    }

    @Test
    void checkIsEventDislikedByUserTest_ThrowNotFoundException_Test() {
        Event event = getEvent();
        UserVO userVO = getUserVO();
        Long eventId = event.getId();

        NotFoundException exception =
            assertThrows(NotFoundException.class, () -> eventService.isEventDislikedByUser(eventId, userVO));
        assertEquals(ErrorMessage.EVENT_NOT_FOUND_BY_ID + event.getId(), exception.getMessage());

        assertTrue(exception.getMessage().contains(ErrorMessage.EVENT_NOT_FOUND_BY_ID + event.getId()));
        verify(eventRepo, times(1)).findById(event.getId());
    }

    @Test
    void getUsersLikedByEventFoundTest() {
        Long eventId = 1L;
        Event mockEvent = new Event();
        Set<User> mockUsersLiked = new HashSet<>();
        mockUsersLiked.add(User.builder().id(1L).name("user1").profilePicturePath("imageUrl1").build());
        mockEvent.setUsersLikedEvents(mockUsersLiked);
        UserProfilePictureDto expectedProfile = new UserProfilePictureDto(1L,"user1", "imageUrl1");

        when(eventRepo.findById(eventId)).thenReturn(Optional.of(mockEvent));
        when(modelMapper.map(any(User.class), eq(UserProfilePictureDto.class))).thenReturn(expectedProfile);
        Set<UserProfilePictureDto> actualUsersLiked = eventService.getUsersLikedByEvent(eventId);

        verify(eventRepo).findById(eventId);
        verify(modelMapper, times(mockUsersLiked.size())).map(any(User.class), eq(UserProfilePictureDto.class));
        assertEquals(Collections.singleton(expectedProfile), actualUsersLiked);
    }

    @Test
    void getUsersLikedByEventEventNotFoundTest() {
        Long eventId = 1L;
        when(eventRepo.findById(eventId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> eventService.getUsersLikedByEvent(eventId));
        verify(eventRepo).findById(eventId);
        verifyNoInteractions(modelMapper);
    }

    @Test
    void getUsersDislikedByEventFoundTest() {
        Long eventId = 1L;
        Event mockEvent = new Event();
        Set<User> mockUsersDisliked = new HashSet<>();
        mockUsersDisliked.add(User.builder().id(1L).name("user1").profilePicturePath("imageUrl1").build());
        mockEvent.setUsersDislikedEvents(mockUsersDisliked);
        UserProfilePictureDto expectedProfile = new UserProfilePictureDto(1L,"user1", "imageUrl1");

        when(eventRepo.findById(eventId)).thenReturn(Optional.of(mockEvent));
        when(modelMapper.map(any(User.class), eq(UserProfilePictureDto.class))).thenReturn(expectedProfile);

        Set<UserProfilePictureDto> actualUsersDisliked = eventService.getUsersDislikedByEvent(eventId);

        verify(eventRepo).findById(eventId);
        verify(modelMapper, times(mockUsersDisliked.size())).map(any(User.class), eq(UserProfilePictureDto.class));
        assertEquals(Collections.singleton(expectedProfile), actualUsersDisliked);
    }

    @Test
    void getUsersDislikedByEventNotFoundTest() {
        Long eventId = 1L;
        when(eventRepo.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventService.getUsersDislikedByEvent(eventId));

        verify(eventRepo).findById(eventId);
        verifyNoInteractions(modelMapper);
    }

    @Test
    void getUsersLikedByEventEmptyLikedUsersTest() {
        Long eventId = 1L;
        Event mockEvent = new Event();
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(mockEvent));
        when(modelMapper.map(any(User.class), eq(UserProfilePictureDto.class))).thenReturn(null); // Simulate empty liked users

        Set<UserProfilePictureDto> actualUsersLiked = eventService.getUsersLikedByEvent(eventId);

        verify(eventRepo).findById(eventId);
        verify(modelMapper, times(0)).map(any(User.class), eq(UserProfilePictureDto.class));
        assertTrue(actualUsersLiked.isEmpty());
    }

    @Test
    void getUsersDislikedByEventEmptyDislikedUsersTest() {
        Long eventId = 1L;
        Event mockEvent = new Event();
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(mockEvent));
        when(modelMapper.map(any(User.class), eq(UserProfilePictureDto.class))).thenReturn(null); // Simulate empty disliked users

        Set<UserProfilePictureDto> actualUsersDisliked = eventService.getUsersDislikedByEvent(eventId);

        verify(eventRepo).findById(eventId);
        verify(modelMapper, times(0)).map(any(User.class), eq(UserProfilePictureDto.class));
        assertTrue(actualUsersDisliked.isEmpty());
    }

    @Test
    void addToRequestedTest() {
        Event event = ModelUtils.getEvent();
        User user = ModelUtils.getUser();
        user.setId(2L);

        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(userRepo.findByEmail(TestConst.EMAIL)).thenReturn(Optional.of(user));
        when(eventRepo.save(event)).thenReturn(event);

        eventService.addToRequested(1L, TestConst.EMAIL);

        verify(eventRepo).findById(any());
        verify(userRepo).findByEmail(TestConst.EMAIL);
        verify(eventRepo).save(event);
    }

    @Test
    void addToRequestedThrowsExceptionWhenEventNotFoundTest() {
        when(eventRepo.findById(any())).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> eventService.addToRequested(1L, TestConst.EMAIL));
        verify(eventRepo).findById(any());
    }

    @Test
    void addToRequestedThrowsExceptionWhenUserNotFoundTest() {
        when(userRepo.findById(any())).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> eventService.addToRequested(1L, TestConst.EMAIL));
        verify(eventRepo).findById(any());
    }

    @Test
    void addToRequestedThrowsExceptionWhenUserHasAlreadyAddedEventToRequestedTest() {
        Event event = ModelUtils.getEvent();
        User user = ModelUtils.getUser();

        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(userRepo.findByEmail(TestConst.EMAIL)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> eventService.addToRequested(1L, TestConst.EMAIL));

        verify(eventRepo).findById(any());
        verify(userRepo).findByEmail(TestConst.EMAIL);
    }

    @Test
    void removeFromRequestedTest() {
        Event event = ModelUtils.getEvent();
        User user = ModelUtils.getUser();

        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(userRepo.findByEmail(TestConst.EMAIL)).thenReturn(Optional.of(user));
        when(eventRepo.save(event)).thenReturn(event);

        eventService.removeFromRequested(1L, TestConst.EMAIL);

        verify(eventRepo).findById(any());
        verify(userRepo).findByEmail(TestConst.EMAIL);
        verify(eventRepo).save(event);
    }

    @Test
    void removeFromRequestedThrowsExceptionWhenEventNotFoundTest() {
        when(eventRepo.findById(any())).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> eventService.removeFromRequested(1L, TestConst.EMAIL));
        verify(eventRepo).findById(any());
    }

    @Test
    void removeFromRequestedThrowsExceptionWhenUserNotFoundTest() {
        when(userRepo.findById(any())).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> eventService.removeFromRequested(1L, TestConst.EMAIL));
        verify(eventRepo).findById(any());
    }

    @Test
    void removeFromRequestedThrowsExceptionWhenEventIsNotInRequestedTest() {
        Event event = ModelUtils.getEvent();
        User user = ModelUtils.getUser();
        user.setId(2L);

        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(userRepo.findByEmail(TestConst.EMAIL)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> eventService.removeFromRequested(1L, TestConst.EMAIL));

        verify(eventRepo).findById(any());
        verify(userRepo).findByEmail(TestConst.EMAIL);
    }

    @Test
    void getRequestedUsersTest() {
        Event event = ModelUtils.getEvent();
        User user = ModelUtils.getUser();
        Pageable pageable = PageRequest.of(0, 20);

        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(userRepo.findUsersByRequestedEvents(any(), any(Pageable.class)))
            .thenReturn(userPage);

        assertEquals(1, eventService.getRequestedUsers(1L, "", pageable).getTotalElements());

        verify(userRepo).findByEmail(anyString());
        verify(eventRepo).findById(any());
        verify(userRepo).findUsersByRequestedEvents(any(), any(Pageable.class));
    }

    @Test
    void getRequestedUsersThrowsUserHasNoPermissionExceptionTest() {
        Event event = ModelUtils.getEvent();
        User user = User.builder().id(20L).build();
        Pageable pageable = PageRequest.of(0, 20);

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(eventRepo.findById(any())).thenReturn(Optional.of(event));

        assertThrows(UserHasNoPermissionToAccessException.class,
            () -> eventService.getRequestedUsers(1L, "", pageable));

        verify(userRepo).findByEmail(anyString());
        verify(eventRepo).findById(any());
    }

    @Test
    void getRequestedUsersThrowsUserNotFoundExceptionTest() {
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventService.getRequestedUsers(1L, "", null));
    }

    @Test
    void approveRequestTest() {
        UserVO userVO = ModelUtils.getUserVO();
        User user = ModelUtils.getUser();
        User userToJoin = User.builder().build();
        Event event = ModelUtils.getEvent();
        event.getRequesters().add(userToJoin);

        when(restClient.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(userRepo.findById(any()))
            .thenReturn(Optional.of(userToJoin));
        when(eventRepo.save(any())).thenReturn(null);

        eventService.approveRequest(event.getId(), userVO.getEmail(), userToJoin.getId());

        verify(restClient).findByEmail(anyString());
        verify(modelMapper).map(userVO, User.class);
        verify(eventRepo).findById(any());
        verify(userRepo).findById(any());
        verify(eventRepo).save(any());
    }

    @Test
    void approveRequestUserHasNoAccessTest() {
        UserVO userVO = ModelUtils.getUserVO();
        User user = ModelUtils.getTestUser();
        Event event = ModelUtils.getEvent();

        when(restClient.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(eventRepo.findById(any())).thenReturn(Optional.of(event));

        Long eventId = event.getId();
        String email = userVO.getEmail();
        assertThrows(UserHasNoPermissionToAccessException.class,
            () -> eventService.approveRequest(eventId, email, null));

        verify(restClient).findByEmail(anyString());
        verify(modelMapper).map(userVO, User.class);
        verify(eventRepo).findById(any());
    }

    @Test
    void approveRequestUserDidNotRequestTest() {
        UserVO userVO = ModelUtils.getUserVO();
        User user = ModelUtils.getUser();
        User userToJoin = User.builder().build();
        Event event = ModelUtils.getEvent();
        event.getRequesters().remove(userToJoin);

        when(restClient.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(eventRepo.findById(any())).thenReturn(Optional.of(event));

        Long eventId = event.getId();
        Long userToJoinId = userToJoin.getId();
        String email = userVO.getEmail();
        assertThrows(BadRequestException.class,
            () -> eventService.approveRequest(eventId, email, userToJoinId));

        verify(restClient).findByEmail(anyString());
        verify(modelMapper).map(userVO, User.class);
        verify(eventRepo).findById(any());
    }

    @Test
    void declineRequestTest() {
        UserVO userVO = ModelUtils.getUserVO();
        User user = ModelUtils.getUser();
        User userToJoin = User.builder().build();
        Event event = ModelUtils.getEvent();
        event.getRequesters().add(userToJoin);

        when(restClient.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(eventRepo.findById(any())).thenReturn(Optional.of(event));
        when(userRepo.findById(any()))
            .thenReturn(Optional.of(userToJoin));
        when(eventRepo.save(any())).thenReturn(null);

        eventService.declineRequest(event.getId(), userVO.getEmail(), userToJoin.getId());

        verify(restClient).findByEmail(anyString());
        verify(modelMapper).map(userVO, User.class);
        verify(eventRepo).findById(any());
        verify(userRepo).findById(any());
        verify(eventRepo).save(any());
    }

    @Test
    void declineRequestUserHasNoAccessTest() {
        UserVO userVO = ModelUtils.getUserVO();
        User user = ModelUtils.getTestUser();
        Event event = ModelUtils.getEvent();

        when(restClient.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(eventRepo.findById(any())).thenReturn(Optional.of(event));

        Long eventId = event.getId();
        String email = userVO.getEmail();
        assertThrows(UserHasNoPermissionToAccessException.class,
            () -> eventService.declineRequest(eventId, email, null));

        verify(restClient).findByEmail(anyString());
        verify(modelMapper).map(userVO, User.class);
        verify(eventRepo).findById(any());
    }

    @Test
    void declineRequestUserDidNotRequestTest() {
        UserVO userVO = ModelUtils.getUserVO();
        User user = ModelUtils.getUser();
        User userToJoin = User.builder().build();
        Event event = ModelUtils.getEvent();
        event.getRequesters().remove(userToJoin);

        when(restClient.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(eventRepo.findById(any())).thenReturn(Optional.of(event));

        Long eventId = event.getId();
        Long userToJoinId = userToJoin.getId();
        String email = userVO.getEmail();
        assertThrows(BadRequestException.class,
            () -> eventService.declineRequest(eventId, email, userToJoinId));

        verify(restClient).findByEmail(anyString());
        verify(modelMapper).map(userVO, User.class);
        verify(eventRepo).findById(any());
    }
}
