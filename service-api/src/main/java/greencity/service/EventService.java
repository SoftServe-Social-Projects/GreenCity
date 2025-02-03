package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.PageableDto;
import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.AddressDto;
import greencity.dto.event.EventAttenderDto;
import greencity.dto.event.EventDto;
import greencity.dto.event.EventVO;
import greencity.dto.event.UpdateEventRequestDto;
import greencity.dto.filter.FilterEventDto;
import greencity.dto.search.SearchEventsDto;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import greencity.dto.user.UserProfilePictureDto;
import greencity.dto.user.UserForListDto;
import greencity.dto.user.UserVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface EventService {
    /**
     * Method for saving Event instance.
     *
     * @param addEventDtoRequest - dto.
     * @return {@link EventDto} instance.
     */
    EventDto save(AddEventDtoRequest addEventDtoRequest, String email, MultipartFile[] images);

    /**
     * Method for deleting Event instance.
     *
     * @param eventId - event id.
     * @param email   - user email.
     */
    void delete(Long eventId, String email);

    /**
     * Method for getting Event instance.
     *
     * @param eventId - event id.
     * @return {@link EventDto} instance.
     */
    EventDto getEvent(Long eventId, Principal principal);

    /**
     * Method for getting all Event instances filtered.
     *
     * @return List of {@link EventDto} instance.
     */
    PageableAdvancedDto<EventDto> getEvents(Pageable page, FilterEventDto filterEventDto, Long userId);

    /**
     * Method for getting all Event instances filtered.
     *
     * @return List of {@link EventDto} instance.
     */
    PageableAdvancedDto<EventDto> getEventsManagement(Pageable page, FilterEventDto filterEventDto, Long userId);

    /**
     * Add an attender to the Event by id.
     *
     * @param eventId - event id.
     */
    void addAttender(Long eventId, String email);

    /**
     * Remove an attender from the Event by id.
     *
     * @param eventId - event id.
     * @param email   - user email.
     */
    void removeAttender(Long eventId, String email);

    /**
     * Method for adding an event to favorites by event id.
     *
     * @param eventId - event id.
     * @param email   - user email.
     */
    void addToFavorites(Long eventId, String email);

    /**
     * Method for removing an event from favorites by event id.
     *
     * @param eventId - event id.
     * @param email   - user email.
     */
    void removeFromFavorites(Long eventId, String email);

    /**
     * Return Events searched by some query.
     *
     * @param paging - pagination params.
     * @param query  - query to search by.
     */
    PageableAdvancedDto<EventDto> searchEventsBy(Pageable paging, String query);

    /**
     * Update Event.
     *
     * @param email    - user that edits event
     * @param eventDto - new event information
     * @param images   - new images of event
     * @return EventDto
     */
    EventDto update(UpdateEventRequestDto eventDto, String email, MultipartFile[] images);

    /**
     * Rate Event.
     *
     * @param email   - user that rates event
     * @param eventId - id of rated event
     * @param grade   - grade of event
     */
    void rateEvent(Long eventId, String email, int grade);

    /**
     * Get all event attenders.
     *
     * @param eventId - id of event
     */
    Set<EventAttenderDto> getAllEventAttenders(Long eventId);

    /**
     * Get event by id.
     *
     * @param eventId - id of event
     */
    EventVO findById(Long eventId);

    /**
     * Method for getting Events by searchQuery.
     *
     * @param pageable    {@link Pageable}
     * @param searchQuery query to search
     * @return PageableDto of {@link SearchEventsDto} instances
     */
    PageableDto<SearchEventsDto> search(Pageable pageable, String searchQuery, Boolean isFavorite, Long userId);

    /**
     * Method for getting all events' addresses.
     *
     * @return list of {@link AddressDto} instances.
     */
    List<AddressDto> getAllEventsAddresses();

    /**
     * Method for getting amount of attended events by user id.
     *
     * @param userId {@link Long} user id.
     * @return {@link Long} amount of attended events by user id.
     */
    Long getCountOfAttendedEventsByUserId(Long userId);

    /**
     * Method for getting amount of organized events by user id.
     *
     * @param userId {@link Long} user id.
     * @return {@link Long} amount of organized events by user id.
     */
    Long getCountOfOrganizedEventsByUserId(Long userId);

    /**
     * Method to like or unlike {@link EventVO} specified by id.
     *
     * @param eventId id of {@link EventVO} to like/dislike.
     * @param userVO  current {@link UserVO} who wants to like/dislike.
     */
    void like(Long eventId, UserVO userVO);

    /**
     * Method to mark event as disliked by User.
     *
     * @param user - instance of {@link UserVO}
     * @param id   - {@link Long} event id.
     */
    void dislike(UserVO user, Long id);

    /**
     * Method to get amount of likes by event id.
     *
     * @param eventId - {@link Integer} event id.
     * @return amount of likes by event id.
     */
    int countLikes(Long eventId);

    /**
     * Method to get amount of dislikes by event id.
     *
     * @param eventId - {@link Integer} event id.
     * @return amount of dislikes by event id.
     */
    int countDislikes(Long eventId);

    /**
     * Method to check if user liked an event.
     *
     * @param eventId - id of {@link EventDto} to check liked or not.
     * @param userVO  - instance of {@link UserVO}.
     * @return user liked event or not.
     */
    boolean isEventLikedByUser(Long eventId, UserVO userVO);

    /**
     * Method to check if user disliked an event.
     *
     * @param eventId - id of {@link EventDto} to check disliked or not.
     * @param userVO  - instance of {@link UserVO}.
     * @return user liked event or not.
     */
    boolean isEventDislikedByUser(Long eventId, UserVO userVO);

    /**
     * Retrieves a set of user profile pictures for all users who have liked the
     * event with the given ID.
     *
     * @param eventId the ID of the event
     * @return a set of user profile picture DTOs
     */
    Set<UserProfilePictureDto> getUsersLikedByEvent(Long eventId);

    /**
     * Retrieves a set of user profile pictures for all users who have disliked the
     * event with the given ID.
     *
     * @param eventId the ID of the event
     * @return a set of user profile picture DTOs
     */
    Set<UserProfilePictureDto> getUsersDislikedByEvent(Long eventId);

    /**
     * Method for adding an event to requested by event id.
     *
     * @param eventId - event id.
     * @param email   - user email.
     * @author Olha Pitsyk.
     */
    void addToRequested(Long eventId, String email);

    /**
     * Method for removing an event from requested by event id.
     *
     * @param eventId - event id.
     * @param email   - user email.
     * @author Olha Pitsyk.
     */
    void removeFromRequested(Long eventId, String email);

    /**
     * Method for getting all users who made request for joining the event.
     *
     * @author Olha Pitsyk.
     */
    PageableDto<UserForListDto> getRequestedUsers(Long eventId, String email, Pageable pageable);

    /**
     * Method for approving request for joining the event.
     *
     * @author Olha Pitsyk.
     */
    void approveRequest(Long eventId, String email, Long userId);

    /**
     * Method for declining request for joining the event.
     *
     * @author Olha Pitsyk.
     */
    void declineRequest(Long eventId, String email, Long userId);

    /**
     * Retrieves a paginated list of attendees for a specific event.
     *
     * @param eventId  the ID of the event for which attendees are to be retrieved
     * @param pageable the pagination information, including page number and size
     * @return a page of {@link EventAttenderDto} containing the details of event
     *         attendees
     */
    Page<EventAttenderDto> getAttendersPage(Long eventId, Pageable pageable);

    /**
     * Retrieves a paginated list of users who liked a specific event.
     *
     * @param eventId  the ID of the event for which liked users are to be retrieved
     * @param pageable the pagination information, including page number and size
     * @return a page of {@link UserProfilePictureDto} containing the details of
     *         users who liked the event
     */
    Page<UserProfilePictureDto> getUsersLikedEventPage(Long eventId, Pageable pageable);

    /**
     * Retrieves a paginated list of users who disliked a specific event.
     *
     * @param eventId  the ID of the event for which disliked users are to be
     *                 retrieved
     * @param pageable the pagination information, including page number and size
     * @return a page of {@link UserProfilePictureDto} containing the details of
     *         users who disliked the event
     */
    Page<UserProfilePictureDto> getUsersDislikedEventPage(Long eventId, Pageable pageable);
}