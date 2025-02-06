package greencity.service;

import greencity.ModelUtils;
import greencity.constant.ErrorMessage;
import greencity.constant.FriendTupleConstant;
import greencity.dto.PageableDto;
import greencity.dto.friends.UserAsFriendDto;
import greencity.dto.friends.UserFriendDto;
import greencity.dto.user.UserManagementDto;
import greencity.entity.User;
import greencity.entity.UserLocation;
import greencity.enums.RecommendedFriendsType;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotDeletedException;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.CustomUserRepo;
import greencity.repository.UserRepo;
import jakarta.persistence.Tuple;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;

@ExtendWith(MockitoExtension.class)
class FriendServiceImplTest {
    @InjectMocks
    private FriendServiceImpl friendService;

    @Mock
    private UserRepo userRepo;
    @Mock
    private CustomUserRepo customUserRepo;
    @Mock
    private ModelMapper modelMapper;

    @Mock
    NotificationService notificationService;

    @Mock
    UserNotificationService userNotificationService;

    @Test
    void deleteUserFriendByIdTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(true);
        when(userRepo.isFriend(userId, friendId)).thenReturn(true);

        friendService.deleteUserFriendById(userId, friendId);

        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo).isFriend(userId, friendId);
        verify(userRepo).deleteUserFriendById(userId, friendId);
    }

    @Test
    void deleteUserFriendByIdWhenUserNotFoundTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(false);

        Exception exception = assertThrows(NotFoundException.class,
            () -> friendService.deleteUserFriendById(userId, friendId));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + userId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo, never()).existsById(friendId);
        verify(userRepo, never()).isFriend(anyLong(), anyLong());
        verify(userRepo, never()).deleteUserFriendById(anyLong(), anyLong());
    }

    @Test
    void deleteUserFriendByIdWhenFriendNotFoundTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(false);

        Exception exception = assertThrows(NotFoundException.class,
            () -> friendService.deleteUserFriendById(userId, friendId));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + friendId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo, never()).isFriend(anyLong(), anyLong());
        verify(userRepo, never()).deleteUserFriendById(anyLong(), anyLong());
    }

    @Test
    void deleteUserFriendByIdWhenUserIsNotFriendTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(true);
        when(userRepo.isFriend(userId, friendId)).thenReturn(false);

        Exception exception = assertThrows(NotDeletedException.class,
            () -> friendService.deleteUserFriendById(userId, friendId));

        assertEquals(ErrorMessage.USER_FRIENDS_LIST + friendId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo).isFriend(userId, friendId);
        verify(userRepo, never()).deleteUserFriendById(anyLong(), anyLong());
    }

    @Test
    void addNewFriendTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(true);
        when(userRepo.isFriendRequested(userId, friendId)).thenReturn(false);
        when(userRepo.isFriend(userId, friendId)).thenReturn(false);
        when(userRepo.getReferenceById(userId)).thenReturn(ModelUtils.getUser());
        when(userRepo.getReferenceById(friendId)).thenReturn(ModelUtils.getTestUser());
        friendService.addNewFriend(userId, friendId);
        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo).isFriendRequested(userId, friendId);
        verify(userRepo).isFriend(userId, friendId);
        verify(userRepo).addNewFriend(userId, friendId);
        verify(userRepo, times(2)).getReferenceById(anyLong());
    }

    @Test
    void addNewFriendWhenUserIdEqualFriendIdTest() {
        long userId = 1L;
        long friendId = 1L;

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> friendService.addNewFriend(userId, friendId));

        assertEquals(ErrorMessage.OWN_USER_ID + friendId, exception.getMessage());

        verify(userRepo, never()).existsById(anyLong());
        verify(userRepo, never()).isFriendRequested(anyLong(), anyLong());
        verify(userRepo, never()).isFriend(anyLong(), anyLong());
        verify(userRepo, never()).addNewFriend(anyLong(), anyLong());
    }

    @Test
    void addNewFriendWhenUserNotFoundTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> friendService.addNewFriend(userId, friendId));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + userId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo, never()).existsById(friendId);
        verify(userRepo, never()).isFriendRequested(anyLong(), anyLong());
        verify(userRepo, never()).isFriend(anyLong(), anyLong());
        verify(userRepo, never()).addNewFriend(anyLong(), anyLong());
    }

    @Test
    void addNewFriendWhenFriendNotFoundTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> friendService.addNewFriend(userId, friendId));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + friendId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo, never()).isFriendRequested(anyLong(), anyLong());
        verify(userRepo, never()).isFriend(anyLong(), anyLong());
        verify(userRepo, never()).addNewFriend(anyLong(), anyLong());
    }

    @Test
    void addNewFriendWhenRequestIsAlreadyMadeTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(true);
        when(userRepo.isFriendRequested(userId, friendId)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> friendService.addNewFriend(userId, friendId));

        assertEquals(ErrorMessage.FRIEND_REQUEST_ALREADY_SENT, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo).isFriendRequested(userId, friendId);
        verify(userRepo, never()).isFriend(anyLong(), anyLong());
        verify(userRepo, never()).addNewFriend(anyLong(), anyLong());
    }

    @Test
    void addNewFriendWhenUsersIsAlreadyFriendsTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(true);
        when(userRepo.isFriendRequested(userId, friendId)).thenReturn(false);
        when(userRepo.isFriend(userId, friendId)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> friendService.addNewFriend(userId, friendId));

        assertEquals(ErrorMessage.FRIEND_EXISTS + friendId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo).isFriendRequested(userId, friendId);
        verify(userRepo).isFriend(userId, friendId);
        verify(userRepo, never()).addNewFriend(anyLong(), anyLong());
    }

    @Test
    void acceptFriendRequestTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(true);
        when(userRepo.isFriend(userId, friendId)).thenReturn(false);
        when(userRepo.isFriendRequestedByCurrentUser(friendId, userId)).thenReturn(true);
        when(userRepo.getReferenceById(userId)).thenReturn(ModelUtils.getUser());
        when(userRepo.getReferenceById(friendId)).thenReturn(ModelUtils.getTestUser());
        friendService.acceptFriendRequest(userId, friendId);
        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo).isFriend(userId, friendId);
        verify(userRepo).isFriendRequestedByCurrentUser(friendId, userId);
        verify(userRepo).acceptFriendRequest(userId, friendId);
        verify(userRepo, times(2)).getReferenceById(anyLong());
    }

    @Test
    void acceptFriendRequestWhenUserIdEqualFriendIdTest() {
        long userId = 1L;
        long friendId = 1L;

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> friendService.acceptFriendRequest(userId, friendId));

        assertEquals(ErrorMessage.OWN_USER_ID + friendId, exception.getMessage());

        verify(userRepo, never()).existsById(anyLong());
        verify(userRepo, never()).isFriend(anyLong(), anyLong());
        verify(userRepo, never()).isFriendRequestedByCurrentUser(anyLong(), anyLong());
        verify(userRepo, never()).acceptFriendRequest(anyLong(), anyLong());
    }

    @Test
    void acceptFriendRequestWhenUserNotFoundTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> friendService.acceptFriendRequest(userId, friendId));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + userId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo, never()).existsById(friendId);
        verify(userRepo, never()).isFriend(anyLong(), anyLong());
        verify(userRepo, never()).isFriendRequestedByCurrentUser(anyLong(), anyLong());
        verify(userRepo, never()).acceptFriendRequest(anyLong(), anyLong());
    }

    @Test
    void acceptFriendRequestWhenFriendNotFoundTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> friendService.acceptFriendRequest(userId, friendId));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + friendId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo, never()).isFriend(anyLong(), anyLong());
        verify(userRepo, never()).isFriendRequestedByCurrentUser(anyLong(), anyLong());
        verify(userRepo, never()).acceptFriendRequest(anyLong(), anyLong());
    }

    @Test
    void acceptFriendRequestWhenUsersIsAlreadyFriendsTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(true);
        when(userRepo.isFriend(userId, friendId)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> friendService.acceptFriendRequest(userId, friendId));

        assertEquals(ErrorMessage.FRIEND_EXISTS + friendId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo).isFriend(userId, friendId);
        verify(userRepo, never()).isFriendRequestedByCurrentUser(anyLong(), anyLong());
        verify(userRepo, never()).acceptFriendRequest(anyLong(), anyLong());
    }

    @Test
    void acceptFriendRequestWhenRequestIsAlreadyMadeTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(true);
        when(userRepo.isFriend(userId, friendId)).thenReturn(false);
        when(userRepo.isFriendRequestedByCurrentUser(friendId, userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> friendService.acceptFriendRequest(userId, friendId));

        assertEquals(ErrorMessage.FRIEND_REQUEST_NOT_SENT, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo).isFriend(userId, friendId);
        verify(userRepo).isFriendRequestedByCurrentUser(friendId, userId);
        verify(userRepo, never()).acceptFriendRequest(anyLong(), anyLong());
    }

    @Test
    void declineFriendRequestTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(true);
        when(userRepo.isFriend(userId, friendId)).thenReturn(false);
        when(userRepo.isFriendRequestedByCurrentUser(friendId, userId)).thenReturn(true);

        friendService.declineFriendRequest(userId, friendId);

        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo).isFriend(userId, friendId);
        verify(userRepo).isFriendRequestedByCurrentUser(friendId, userId);
        verify(userRepo).declineFriendRequest(userId, friendId);
    }

    @Test
    void declineFriendRequestWhenUserIdEqualFriendIdTest() {
        long userId = 1L;
        long friendId = 1L;

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> friendService.declineFriendRequest(userId, friendId));

        assertEquals(ErrorMessage.OWN_USER_ID + friendId, exception.getMessage());

        verify(userRepo, never()).existsById(anyLong());
        verify(userRepo, never()).isFriend(anyLong(), anyLong());
        verify(userRepo, never()).isFriendRequestedByCurrentUser(anyLong(), anyLong());
        verify(userRepo, never()).declineFriendRequest(anyLong(), anyLong());
    }

    @Test
    void declineFriendRequestWhenUserNotFoundTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> friendService.declineFriendRequest(userId, friendId));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + userId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo, never()).existsById(friendId);
        verify(userRepo, never()).isFriend(anyLong(), anyLong());
        verify(userRepo, never()).isFriendRequestedByCurrentUser(anyLong(), anyLong());
        verify(userRepo, never()).declineFriendRequest(anyLong(), anyLong());
    }

    @Test
    void declineFriendRequestWhenFriendNotFoundTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> friendService.declineFriendRequest(userId, friendId));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + friendId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo, never()).isFriend(anyLong(), anyLong());
        verify(userRepo, never()).isFriendRequestedByCurrentUser(anyLong(), anyLong());
        verify(userRepo, never()).declineFriendRequest(anyLong(), anyLong());
    }

    @Test
    void declineFriendRequestWhenUsersIsAlreadyFriendsTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(true);
        when(userRepo.isFriend(userId, friendId)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> friendService.declineFriendRequest(userId, friendId));

        assertEquals(ErrorMessage.FRIEND_EXISTS + friendId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo).isFriend(userId, friendId);
        verify(userRepo, never()).isFriendRequestedByCurrentUser(anyLong(), anyLong());
        verify(userRepo, never()).declineFriendRequest(anyLong(), anyLong());
    }

    @Test
    void declineFriendRequestWhenRequestIsAlreadyMadeTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(true);
        when(userRepo.isFriend(userId, friendId)).thenReturn(false);
        when(userRepo.isFriendRequestedByCurrentUser(friendId, userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> friendService.declineFriendRequest(userId, friendId));

        assertEquals(ErrorMessage.FRIEND_REQUEST_NOT_SENT, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo).isFriend(userId, friendId);
        verify(userRepo).isFriendRequestedByCurrentUser(friendId, userId);
        verify(userRepo, never()).declineFriendRequest(anyLong(), anyLong());
    }

    @Test
    void findUserFriendsByUserIdTest() {
        long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> users = new PageImpl<>(List.of(ModelUtils.getUser()), pageable, 1);

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.getAllUserFriendsPage(pageable, userId)).thenReturn(users);

        friendService.findUserFriendsByUserId(pageable, userId);

        verify(userRepo).existsById(userId);
        verify(userRepo).getAllUserFriendsPage(pageable, userId);
        verify(modelMapper).map(users.getContent().getFirst(), UserManagementDto.class);
    }

    @Test
    void findUserFriendsByUserIdWhenUserNotFoundTest() {
        long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepo.existsById(userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> friendService.findUserFriendsByUserId(pageable, userId));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + userId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(modelMapper, never()).map(anyLong(), any());
    }

    @Test
    void findAllUsersExceptMainUserAndUsersFriendAndRequestersToMainUserTest() {
        long userId = 1L;
        int page = 0;
        int size = 1;
        long totalElements = 50;
        Pageable pageable = PageRequest.of(page, size);
        UserFriendDto expectedResult = ModelUtils.getUserFriendDto();
        Page<User> userPage = new PageImpl<>(List.of(ModelUtils.getUser()), pageable, totalElements);
        String name = "vi";

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.getAllUsersExceptMainUserAndFriendsAndRequestersToMainUser(userId, name, false, false, pageable))
            .thenReturn(userPage);
        when(
            customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId, userPage.getContent()))
            .thenReturn(List.of(expectedResult));

        PageableDto<UserFriendDto> pageableDto =
            friendService.findAllUsersExceptMainUserAndUsersFriendAndRequestersToMainUser(userId, name, false, false,
                pageable);

        assertNotNull(pageableDto);
        assertNotNull(pageableDto.getPage());
        assertEquals(1, pageableDto.getPage().size());
        assertEquals(expectedResult, pageableDto.getPage().getFirst());
        assertEquals(totalElements, pageableDto.getTotalElements());
        assertEquals(totalElements, pageableDto.getTotalPages());
        assertEquals(page, pageableDto.getCurrentPage());

        verify(userRepo).existsById(userId);
        verify(userRepo).getAllUsersExceptMainUserAndFriendsAndRequestersToMainUser(userId, name, false, false,
            pageable);
        verify(customUserRepo).fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId,
            userPage.getContent());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void findAllUsersExceptMainUserAndUsersFriendAndRequestersToMainUserWhenNameIsNullTest(String name) {
        long userId = 1L;
        boolean filterByFriendsOfFriends = false;
        boolean filterByCity = false;
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.getAllUsersExceptMainUserAndFriendsAndRequestersToMainUser(userId, "",
            filterByFriendsOfFriends, filterByCity, pageable)).thenReturn(ModelUtils.getUserPage());
        when(customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId,
            ModelUtils.getUserPage().getContent()))
            .thenReturn(List.of(ModelUtils.getUserFriendDtoListFromUserPage()));

        PageableDto<UserFriendDto> result =
            friendService.findAllUsersExceptMainUserAndUsersFriendAndRequestersToMainUser(userId, name, false, false,
                pageable);

        verify(userRepo, times(1)).existsById(userId);

        assertEquals(1, result.getPage().size());
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getCurrentPage());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void findUserFriendsByUserIAndShowFriendStatusRelatedToCurrentUserTest() {
        long userId = 1L;
        long currentUserId = 2L;
        int page = 0;
        int size = 1;
        long totalElements = 50;
        Pageable pageable = PageRequest.of(page, size);
        UserFriendDto expectedResult = ModelUtils.getUserFriendDto();
        Page<User> userPage = new PageImpl<>(List.of(ModelUtils.getUser()), pageable, totalElements);

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.getAllUserFriendsCollectingBySpecificConditionsAndCertainOrder(pageable, userId))
            .thenReturn(userPage);
        when(
            customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(currentUserId,
                userPage.getContent()))
            .thenReturn(List.of(expectedResult));

        PageableDto<UserFriendDto> pageableDto = friendService
            .findUserFriendsByUserIAndShowFriendStatusRelatedToCurrentUser(pageable, userId, currentUserId);

        assertNotNull(pageableDto);
        assertNotNull(pageableDto.getPage());
        assertEquals(1, pageableDto.getPage().size());
        assertEquals(expectedResult, pageableDto.getPage().getFirst());
        assertEquals(totalElements, pageableDto.getTotalElements());
        assertEquals(totalElements, pageableDto.getTotalPages());
        assertEquals(page, pageableDto.getCurrentPage());

        verify(userRepo).existsById(userId);
        verify(userRepo).getAllUserFriendsCollectingBySpecificConditionsAndCertainOrder(pageable, userId);
        verify(customUserRepo).fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(currentUserId,
            userPage.getContent());
    }

    @Test
    void findUserFriendsByUserIAndShowFriendStatusRelatedToCurrentUser_UserNotFoundTest() {
        long userId = 1L;
        long currentUserId = 2L;
        Pageable pageable = PageRequest.of(0, 1);

        when(userRepo.existsById(userId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> friendService
            .findUserFriendsByUserIAndShowFriendStatusRelatedToCurrentUser(pageable, userId, currentUserId));

        verify(userRepo).existsById(userId);
    }

    @Test
    void findRecommendedFriendsByFriendsOfFriends() {
        long userId = 1L;
        int page = 0;
        int size = 1;
        long totalElements = 50;
        Pageable pageable = PageRequest.of(page, size);
        UserFriendDto expectedResult = ModelUtils.getUserFriendDto();
        Page<User> userPage = new PageImpl<>(List.of(ModelUtils.getUser()), pageable, totalElements);

        when(userRepo.findById(userId)).thenReturn(Optional.of(new User()));
        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.getRecommendedFriendsOfFriends(userId, pageable)).thenReturn(userPage);
        when(
            customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId, userPage.getContent()))
            .thenReturn(List.of(expectedResult));

        PageableDto<UserFriendDto> pageableDto =
            friendService.findRecommendedFriends(userId, RecommendedFriendsType.FRIENDS_OF_FRIENDS, pageable);

        assertNotNull(pageableDto.getPage());
        assertEquals(1, pageableDto.getPage().size());
        assertEquals(expectedResult, pageableDto.getPage().getFirst());
        assertEquals(totalElements, pageableDto.getTotalElements());
        assertEquals(totalElements, pageableDto.getTotalPages());
        assertEquals(page, pageableDto.getCurrentPage());

        verify(userRepo).findById(userId);
        verify(userRepo).existsById(userId);
        verify(userRepo).getRecommendedFriendsOfFriends(userId, pageable);
        verify(customUserRepo).fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId,
            userPage.getContent());
    }

    @Test
    void findRecommendedFriendsWithoutType() {
        long userId = 1L;
        int page = 0;
        int size = 1;
        long totalElements = 50;
        Pageable pageable = PageRequest.of(page, size);
        UserFriendDto expectedResult = ModelUtils.getUserFriendDto();
        Page<User> userPage = new PageImpl<>(List.of(ModelUtils.getUser()), pageable, totalElements);

        when(userRepo.findById(userId)).thenReturn(Optional.of(new User()));
        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.getAllUsersExceptMainUserAndFriends(userId, "", pageable)).thenReturn(userPage);
        when(
            customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId, userPage.getContent()))
            .thenReturn(List.of(expectedResult));

        PageableDto<UserFriendDto> pageableDto =
            friendService.findRecommendedFriends(userId, null, pageable);

        assertNotNull(pageableDto.getPage());
        assertEquals(1, pageableDto.getPage().size());
        assertEquals(expectedResult, pageableDto.getPage().getFirst());
        assertEquals(totalElements, pageableDto.getTotalElements());
        assertEquals(totalElements, pageableDto.getTotalPages());
        assertEquals(page, pageableDto.getCurrentPage());

        verify(userRepo).findById(userId);
        verify(userRepo).existsById(userId);
        verify(userRepo).getAllUsersExceptMainUserAndFriends(userId, "", pageable);
        verify(customUserRepo).fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId,
            userPage.getContent());
    }

    @Test
    void findRecommendedFriendsByHabits() {
        long userId = 1L;
        int page = 0;
        int size = 1;
        long totalElements = 50;
        Pageable pageable = PageRequest.of(page, size);
        UserFriendDto expectedResult = ModelUtils.getUserFriendDto();
        Page<User> userPage = new PageImpl<>(List.of(ModelUtils.getUser()), pageable, totalElements);

        when(userRepo.findById(userId)).thenReturn(Optional.of(new User()));
        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.findRecommendedFriendsByHabits(userId, pageable)).thenReturn(userPage);
        when(
            customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId, userPage.getContent()))
            .thenReturn(List.of(expectedResult));

        PageableDto<UserFriendDto> pageableDto =
            friendService.findRecommendedFriends(userId, RecommendedFriendsType.HABITS, pageable);

        assertNotNull(pageableDto.getPage());
        assertEquals(1, pageableDto.getPage().size());
        assertEquals(expectedResult, pageableDto.getPage().getFirst());
        assertEquals(totalElements, pageableDto.getTotalElements());
        assertEquals(totalElements, pageableDto.getTotalPages());
        assertEquals(page, pageableDto.getCurrentPage());

        verify(userRepo).findById(userId);
        verify(userRepo).existsById(userId);
        verify(userRepo).findRecommendedFriendsByHabits(userId, pageable);
        verify(customUserRepo).fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId,
            userPage.getContent());
    }

    @Test
    void findRecommendedFriendsByCity() {
        long userId = 1L;
        int page = 0;
        int size = 1;
        long totalElements = 50;
        Pageable pageable = PageRequest.of(page, size);
        UserFriendDto expectedResult = ModelUtils.getUserFriendDto();
        Page<User> userPage = new PageImpl<>(List.of(ModelUtils.getUser()), pageable, totalElements);

        User userWithLocation = new User();
        UserLocation userLocation = new UserLocation();
        userLocation.setCityUa("testCity");
        userWithLocation.setUserLocation(userLocation);

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.findById(userId)).thenReturn(Optional.of(userWithLocation));
        when(userRepo.findRecommendedFriendsByCity(userId, "testCity", pageable)).thenReturn(userPage);
        when(
            customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId, userPage.getContent()))
            .thenReturn(List.of(expectedResult));
        PageableDto<UserFriendDto> pageableDto =
            friendService.findRecommendedFriends(userId, RecommendedFriendsType.CITY, pageable);

        assertNotNull(pageableDto.getPage());
        assertEquals(1, pageableDto.getPage().size());
        assertEquals(expectedResult, pageableDto.getPage().getFirst());
        assertEquals(totalElements, pageableDto.getTotalElements());
        assertEquals(totalElements, pageableDto.getTotalPages());
        assertEquals(page, pageableDto.getCurrentPage());

        verify(userRepo).findById(userId);
        verify(userRepo).existsById(userId);
        verify(userRepo).findRecommendedFriendsByCity(userId, "testCity", pageable);
        verify(customUserRepo).fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId,
            userPage.getContent());
    }

    @Test
    void findRecommendedFriendsByCityWhenLocationNullTest() {
        long userId = 1L;
        int page = 0;
        int size = 1;
        Pageable pageable = PageRequest.of(page, size);
        User userWithNullLocation = new User();

        when(userRepo.findById(userId)).thenReturn(Optional.of(userWithNullLocation));
        when(userRepo.existsById(userId)).thenReturn(true);
        PageableDto<UserFriendDto> pageableDto =
            friendService.findRecommendedFriends(userId, RecommendedFriendsType.CITY, pageable);

        assertNotNull(pageableDto);
        assertTrue(pageableDto.getPage().isEmpty());
        assertEquals(0, pageableDto.getTotalElements());
        assertEquals(0, pageableDto.getTotalPages());
        assertEquals(page, pageableDto.getCurrentPage());

        verify(userRepo, times(1)).findById(userId);
        verify(userRepo, never()).findRecommendedFriendsByCity(anyLong(), anyString(), any(Pageable.class));
    }

    @Test
    void findRecommendedFriendsByCityWhenCityNullTest() {
        long userId = 1L;
        int page = 0;
        int size = 1;
        Pageable pageable = PageRequest.of(page, size);
        UserLocation userLocation = new UserLocation();
        User userWithNullCity = new User();
        userWithNullCity.setUserLocation(userLocation);

        when(userRepo.findById(userId)).thenReturn(Optional.of(userWithNullCity));
        when(userRepo.existsById(userId)).thenReturn(true);
        PageableDto<UserFriendDto> pageableDto =
            friendService.findRecommendedFriends(userId, RecommendedFriendsType.CITY, pageable);

        assertNotNull(pageableDto);
        assertTrue(pageableDto.getPage().isEmpty());
        assertEquals(0, pageableDto.getTotalElements());
        assertEquals(0, pageableDto.getTotalPages());
        assertEquals(page, pageableDto.getCurrentPage());

        verify(userRepo, times(1)).findById(userId);
        verify(userRepo, never()).findRecommendedFriendsByCity(anyLong(), anyString(), any(Pageable.class));
    }

    @Test
    void getMutualFriendsTest() {
        long userId = 1L;
        long friendId = 2L;
        int page = 0;
        int size = 1;
        long totalElements = 50;
        Pageable pageable = PageRequest.of(page, size);
        UserFriendDto expectedResult = ModelUtils.getUserFriendDto();
        Page<User> userPage = new PageImpl<>(List.of(ModelUtils.getUser()), pageable, totalElements);

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(true);
        when(userRepo.getMutualFriends(userId, friendId, pageable)).thenReturn(userPage);
        when(
            customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId, userPage.getContent()))
            .thenReturn(List.of(expectedResult));

        PageableDto<UserFriendDto> pageableDto =
            friendService.getMutualFriends(userId, friendId, pageable);

        assertNotNull(pageableDto.getPage());
        assertEquals(1, pageableDto.getPage().size());
        assertEquals(expectedResult, pageableDto.getPage().getFirst());
        assertEquals(totalElements, pageableDto.getTotalElements());
        assertEquals(totalElements, pageableDto.getTotalPages());
        assertEquals(page, pageableDto.getCurrentPage());

        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo).getMutualFriends(userId, friendId, pageable);
        verify(customUserRepo).fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId,
            userPage.getContent());
    }

    @Test
    void findAllUsersExceptMainUserAndUsersFriendAndRequestersToMainUserWhenUserNotFoundTest() {
        long userId = 1L;
        String name = "vi";
        Pageable pageable = PageRequest.of(0, 20);

        when(userRepo.existsById(userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> friendService.findAllUsersExceptMainUserAndUsersFriendAndRequestersToMainUser(
                userId,
                name,
                false,
                false,
                pageable));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + userId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo, never()).getAllUsersExceptMainUserAndFriends(anyLong(), anyString(), any());
        verify(customUserRepo, never()).fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(anyLong(), any());
    }

    @Test
    void findAllUsersExceptMainUserAndUsersFriendAndRequestersToMainUserWhenPageableIsNullTest() {
        long userId = 1L;
        String name = "vi";
        boolean filterByFriendsOfFriends = false;
        boolean filterByCity = false;

        assertThrows(NullPointerException.class,
            () -> friendService.findAllUsersExceptMainUserAndUsersFriendAndRequestersToMainUser(userId, name,
                filterByFriendsOfFriends, filterByCity, null));

        verify(userRepo, never()).existsById(anyLong());
        verify(userRepo, never()).getAllUsersExceptMainUserAndFriends(anyLong(), anyString(), any());
        verify(customUserRepo, never()).fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(anyLong(), any());
    }

    @Test
    void getAllUserFriendRequestsTest() {
        long userId = 1L;
        String name = "";
        boolean filterByCity = false;
        int page = 0;
        int size = 1;
        long totalElements = 50;
        Pageable pageable = PageRequest.of(page, size);
        UserFriendDto expectedResult = ModelUtils.getUserFriendDto();
        Page<User> userPage = new PageImpl<>(List.of(ModelUtils.getUser()), pageable, totalElements);

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.getAllUserFriendRequests(userId, name, filterByCity, pageable)).thenReturn(userPage);
        when(
            customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId, userPage.getContent()))
            .thenReturn(List.of(expectedResult));

        PageableDto<UserFriendDto> pageableDto =
            friendService.getAllUserFriendRequests(userId, name, filterByCity, pageable);

        assertNotNull(pageableDto);
        assertNotNull(pageableDto.getPage());
        assertEquals(1, pageableDto.getPage().size());
        assertEquals(expectedResult, pageableDto.getPage().getFirst());
        assertEquals(totalElements, pageableDto.getTotalElements());
        assertEquals(totalElements, pageableDto.getTotalPages());
        assertEquals(page, pageableDto.getCurrentPage());

        verify(userRepo).existsById(userId);
        verify(userRepo).getAllUserFriendRequests(userId, name, filterByCity, pageable);
        verify(customUserRepo).fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId,
            userPage.getContent());
    }

    @Test
    void getAllUserFriendRequestsWhenUserNotFoundTest() {
        long userId = 1L;
        String name = "";
        boolean filterByCity = false;
        Pageable pageable = PageRequest.of(0, 20);

        when(userRepo.existsById(userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> friendService.getAllUserFriendRequests(userId, name, filterByCity, pageable));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + userId, exception.getMessage());

        verify(userRepo).existsById(1L);
        verify(userRepo, never()).getAllUserFriendRequests(anyLong(), anyString(), anyBoolean(), any());
        verify(customUserRepo, never()).fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(anyLong(), any());
    }

    @Test
    void getAllUserFriendRequestsWhenPageableIsNullTest() {
        long userId = 1L;
        String name = "";
        boolean filterByCity = false;

        assertThrows(NullPointerException.class,
            () -> friendService.getAllUserFriendRequests(userId, name, filterByCity, null));

        verify(userRepo, never()).existsById(anyLong());
        verify(userRepo, never()).getAllUserFriendRequests(anyLong(), anyString(), anyBoolean(), any());
        verify(customUserRepo, never()).fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(anyLong(), any());
    }

    @Test
    void findAllFriendsOfUserTest() {
        long userId = 1L;
        String name = "vi";
        boolean filterByCity = false;
        int page = 0;
        int size = 1;
        long totalElements = 50;
        Pageable pageable = PageRequest.of(page, size);
        UserFriendDto expectedResult = ModelUtils.getUserFriendDto();
        expectedResult.setFriendStatus("FRIEND");
        Page<User> userPage = new PageImpl<>(List.of(ModelUtils.getUser()), pageable, totalElements);

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.findAllFriendsOfUser(userId, name, filterByCity, pageable)).thenReturn(userPage);
        when(
            customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId, userPage.getContent()))
            .thenReturn(List.of(expectedResult));

        PageableDto<UserFriendDto> pageableDto =
            friendService.findAllFriendsOfUser(userId, name, filterByCity, pageable);

        assertNotNull(pageableDto);
        assertNotNull(pageableDto.getPage());
        assertEquals(1, pageableDto.getPage().size());
        assertEquals(expectedResult, pageableDto.getPage().getFirst());
        assertEquals(totalElements, pageableDto.getTotalElements());
        assertEquals(totalElements, pageableDto.getTotalPages());
        assertEquals(page, pageableDto.getCurrentPage());

        verify(userRepo).existsById(userId);
        verify(userRepo).findAllFriendsOfUser(userId, name, filterByCity, pageable);
        verify(customUserRepo).fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId,
            userPage.getContent());
    }

    @Test
    void findAllFriendsOfUserWhenNameIsNullTest() {
        long userId = 1L;
        boolean filterByCity = false;
        int page = 0;
        int size = 1;
        long totalElements = 50;
        Pageable pageable = PageRequest.of(page, size);
        UserFriendDto expectedResult = ModelUtils.getUserFriendDto();
        Page<User> userPage = new PageImpl<>(List.of(ModelUtils.getUser()), pageable, totalElements);

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.findAllFriendsOfUser(userId, null, filterByCity, pageable)).thenReturn(userPage);
        when(
            customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId, userPage.getContent()))
            .thenReturn(List.of(expectedResult));

        PageableDto<UserFriendDto> pageableDto =
            friendService.findAllFriendsOfUser(userId, null, filterByCity, pageable);

        assertNotNull(pageableDto);
        assertNotNull(pageableDto.getPage());
        assertEquals(1, pageableDto.getPage().size());
        assertEquals(expectedResult, pageableDto.getPage().getFirst());
        assertEquals(totalElements, pageableDto.getTotalElements());
        assertEquals(totalElements, pageableDto.getTotalPages());
        assertEquals(page, pageableDto.getCurrentPage());

        verify(userRepo).existsById(userId);
        verify(userRepo).findAllFriendsOfUser(userId, null, filterByCity, pageable);
        verify(customUserRepo).fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId,
            userPage.getContent());
    }

    @Test
    void findAllFriendsOfUserWhenUserNotFoundTest() {
        long userId = 1L;
        String name = "vi";
        boolean filterByCity = false;
        Pageable pageable = PageRequest.of(0, 20);

        when(userRepo.existsById(userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> friendService.findAllFriendsOfUser(userId, name, filterByCity, pageable));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + userId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo, never()).findAllFriendsOfUser(anyLong(), anyString(), anyBoolean(), any());
        verify(customUserRepo, never()).fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(anyLong(), any());
    }

    @Test
    void findAllFriendsOfUserWhenPageableIsNullTest() {
        long userId = 1L;
        String name = "vi";
        boolean filterByCity = false;

        assertThrows(NullPointerException.class,
            () -> friendService.findAllFriendsOfUser(userId, name, filterByCity, null));

        verify(userRepo, never()).existsById(anyLong());
        verify(userRepo, never()).findAllFriendsOfUser(anyLong(), anyString(), anyBoolean(), any());
        verify(customUserRepo, never()).fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(anyLong(), any());
    }

    @Test
    void deleteRequestOfCurrentUserToFriendTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(true);
        when(userRepo.isFriend(userId, friendId)).thenReturn(false);
        when(userRepo.isFriendRequestedByCurrentUser(userId, friendId)).thenReturn(true);

        friendService.deleteRequestOfCurrentUserToFriend(userId, friendId);

        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo).isFriend(userId, friendId);
        verify(userRepo).isFriendRequestedByCurrentUser(userId, friendId);
        verify(userRepo).canselUserRequestToFriend(userId, friendId);
    }

    @Test
    void deleteRequestOfCurrentUserToFriendWhenUserIdEqualFriendIdTest() {
        long userId = 1L;
        long friendId = 1L;

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> friendService.deleteRequestOfCurrentUserToFriend(userId, friendId));

        assertEquals(ErrorMessage.OWN_USER_ID + friendId, exception.getMessage());

        verify(userRepo, never()).existsById(anyLong());
        verify(userRepo, never()).isFriend(anyLong(), anyLong());
        verify(userRepo, never()).isFriendRequestedByCurrentUser(anyLong(), anyLong());
        verify(userRepo, never()).deleteUserFriendById(anyLong(), anyLong());
    }

    @Test
    void deleteRequestOfCurrentUserToFriendWhenUserNotFoundTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> friendService.deleteRequestOfCurrentUserToFriend(userId, friendId));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + userId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo, never()).existsById(friendId);
        verify(userRepo, never()).isFriend(anyLong(), anyLong());
        verify(userRepo, never()).isFriendRequestedByCurrentUser(anyLong(), anyLong());
        verify(userRepo, never()).canselUserRequestToFriend(anyLong(), anyLong());
    }

    @Test
    void deleteRequestOfCurrentUserToFriendWhenFriendNotFoundTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> friendService.deleteRequestOfCurrentUserToFriend(userId, friendId));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + friendId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo, never()).isFriend(anyLong(), anyLong());
        verify(userRepo, never()).isFriendRequestedByCurrentUser(anyLong(), anyLong());
        verify(userRepo, never()).canselUserRequestToFriend(anyLong(), anyLong());
    }

    @Test
    void deleteRequestOfCurrentUserToFriendWhenUsersIsAlreadyFriendsTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(true);
        when(userRepo.isFriend(userId, friendId)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> friendService.deleteRequestOfCurrentUserToFriend(userId, friendId));

        assertEquals(ErrorMessage.FRIEND_EXISTS + friendId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo).isFriend(userId, friendId);
        verify(userRepo, never()).isFriendRequestedByCurrentUser(anyLong(), anyLong());
        verify(userRepo, never()).canselUserRequestToFriend(anyLong(), anyLong());
    }

    @Test
    void deleteRequestOfCurrentUserToFriendWhenRequestIsNotMadeTest() {
        long userId = 1L;
        long friendId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(userRepo.existsById(friendId)).thenReturn(true);
        when(userRepo.isFriend(userId, friendId)).thenReturn(false);
        when(userRepo.isFriendRequestedByCurrentUser(userId, friendId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> friendService.deleteRequestOfCurrentUserToFriend(userId, friendId));

        assertEquals(ErrorMessage.FRIEND_REQUEST_NOT_SENT, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(userRepo).existsById(friendId);
        verify(userRepo).isFriend(userId, friendId);
        verify(userRepo).isFriendRequestedByCurrentUser(userId, friendId);
        verify(userRepo, never()).canselUserRequestToFriend(anyLong(), anyLong());
    }

    @Test
    void getUserAsFriendTest() {
        UserAsFriendDto expected = ModelUtils.getUserAsFriendDto();
        Long userId = 2L;
        Long friendId = expected.getId();
        Tuple tuple = mock(Tuple.class);

        when(userRepo.existsById(friendId)).thenReturn(true);
        when(userRepo.findUsersFriendByUserIdAndFriendId(userId, friendId))
            .thenReturn(tuple);
        when(userRepo.findIdOfPrivateChatOfUsers(userId, friendId))
            .thenReturn(expected.getChatId());

        when(tuple.get(FriendTupleConstant.STATUS, String.class)).thenReturn(expected.getFriendStatus());
        when(tuple.get(FriendTupleConstant.REQUESTER_ID, Long.class)).thenReturn(expected.getRequesterId());

        assertEquals(expected, friendService.getUserAsFriend(userId, friendId));

        verify(userRepo).existsById(anyLong());
        verify(userRepo).findUsersFriendByUserIdAndFriendId(anyLong(), anyLong());
        verify(userRepo).findIdOfPrivateChatOfUsers(anyLong(), anyLong());
        verify(tuple).get(FriendTupleConstant.STATUS, String.class);
        verify(tuple).get(FriendTupleConstant.REQUESTER_ID, Long.class);
    }

    @Test
    void getUserAsFriendIfUsersAreNotFriendsAndDoNotHaveChatTest() {
        UserAsFriendDto expected = new UserAsFriendDto(1L, null);

        Long userId = 2L;
        Long friendId = expected.getId();

        when(userRepo.existsById(friendId)).thenReturn(true);
        when(userRepo.findUsersFriendByUserIdAndFriendId(userId, friendId)).thenReturn(null);
        when(userRepo.findIdOfPrivateChatOfUsers(userId, friendId)).thenReturn(null);

        assertEquals(expected, friendService.getUserAsFriend(userId, friendId));
        verify(userRepo).existsById(anyLong());
        verify(userRepo).findUsersFriendByUserIdAndFriendId(anyLong(), anyLong());
        verify(userRepo).findIdOfPrivateChatOfUsers(anyLong(), anyLong());
    }

    @Test
    void getUserAsFriendIfUsersThrowsNotFoundExceptionTest() {
        Long friendId = 2L;

        when(userRepo.existsById(friendId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> friendService.getUserAsFriend(1L, friendId));
        verify(userRepo).existsById(anyLong());
    }
}
