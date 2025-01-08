package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.converters.UserArgumentResolver;
import greencity.dto.user.UserVO;
import greencity.enums.RecommendedFriendsType;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.FriendService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.security.Principal;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FriendControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FriendService friendService;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    FriendController friendController;

    private static final String FRIEND_LINK = "/friends";
    private final Principal principal = ModelUtils.getPrincipal();
    private final UserVO userVO = ModelUtils.getUserVO();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(friendController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
            .build();
    }

    @Test
    void deleteUserFriendTest() throws Exception {
        long friendId = 1L;

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(delete(FRIEND_LINK + "/{friendId}", friendId)
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(principal.getName());
        verify(friendService).deleteUserFriendById(userVO.getId(), friendId);
    }

    @Test
    void addNewFriendTest() throws Exception {
        long friendId = 1L;

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(post(FRIEND_LINK + "/{friendId}", friendId)
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(principal.getName());
        verify(friendService).addNewFriend(userVO.getId(), friendId);
    }

    @Test
    void acceptFriendRequestTest() throws Exception {
        long friendId = 1L;

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(patch(FRIEND_LINK + "/{friendId}/acceptFriend", friendId)
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(principal.getName());
        verify(friendService).acceptFriendRequest(userVO.getId(), friendId);
    }

    @Test
    void declineFriendRequestTest() throws Exception {
        long friendId = 1L;

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(patch(FRIEND_LINK + "/{friendId}/declineFriend", friendId)
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(principal.getName());
        verify(friendService).declineFriendRequest(userVO.getId(), friendId);
    }

    @Test
    void findUserFriendsByUserIdTest() throws Exception {
        long userId = 1L;

        mockMvc.perform(get(FRIEND_LINK + "/user/{userId}", userId))
            .andExpect(status().isOk());

        verify(friendService).findUserFriendsByUserId(PageRequest.of(0, 10), userId);
    }

    @Test
    void findAllUsersExceptMainUserAndUsersFriend() throws Exception {
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(get(FRIEND_LINK + "/not-friends-yet")
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(principal.getName());
        verify(friendService).findAllUsersExceptMainUserAndUsersFriendAndRequestersToMainUser(
                userVO.getId(),
                "",
                false,
                false,
                PageRequest.of(0, 10)
        );
    }

    @Test
    void findUserFriendsByUserIAndShowFriendStatusRelatedToCurrentUserTest() throws Exception {
        long userId = 1L;

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(get(FRIEND_LINK + "/{userId}/all-user-friends", userId)
            .param("page", "0")
            .param("size", "10")
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(principal.getName());
        verify(friendService).findUserFriendsByUserIAndShowFriendStatusRelatedToCurrentUser(PageRequest.of(0, 10),
            userId,
            userVO.getId());
    }

    @Test
    void findRecommendedFriends() throws Exception {
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(get(FRIEND_LINK + "/recommended-friends?type=FRIENDS_OF_FRIENDS")
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(principal.getName());
        verify(friendService).findRecommendedFriends(userVO.getId(), RecommendedFriendsType.FRIENDS_OF_FRIENDS,
            PageRequest.of(0, 20));
    }

    @Test
    void getMutualFriendsTest() throws Exception {
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(get(FRIEND_LINK + "/mutual-friends?friendId=1")
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(principal.getName());
        verify(friendService).getMutualFriends(userVO.getId(), 1L, PageRequest.of(0, 20));
    }

    @Test
    void getAllUserFriendsRequestsTest() throws Exception {
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(get(FRIEND_LINK + "/friendRequests")
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(principal.getName());
        verify(friendService).getAllUserFriendRequests(userVO.getId(), "", false, PageRequest.of(0, 20));
    }

    @Test
    void findAllFriendsOfUserTest() throws Exception {
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        String name = "name";
        mockMvc.perform(get(FRIEND_LINK + "?name=" + name)
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(principal.getName());
        verify(friendService).findAllFriendsOfUser(userVO.getId(), name, false, PageRequest.of(0, 20));
    }

    @Test
    void cancelRequestTest() throws Exception {
        long friendId = 1L;

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(delete(FRIEND_LINK + "/{friendId}/cancelRequest", friendId)
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(principal.getName());
        verify(friendService).deleteRequestOfCurrentUserToFriend(userVO.getId(), friendId);
    }

    @Test
    void getUserAsFriendDtoTest() throws Exception {
        var userAsFriend = ModelUtils.getUserAsFriendDto();

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(friendService.getUserAsFriend(anyLong(), anyLong())).thenReturn(userAsFriend);

        mockMvc.perform(get(FRIEND_LINK + "/user-data-as-friend/{friendId}", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(userAsFriend)))
            .andExpect(status().isOk());

        verify(userService).findByEmail(principal.getName());
        verify(friendService).getUserAsFriend(anyLong(), anyLong());
    }

    @Test
    void getUserAsFriendDtoNotFoundExceptionTest() throws Exception {
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(friendService.getUserAsFriend(anyLong(),anyLong())).thenThrow(NotFoundException.class);

        mockMvc.perform(get(FRIEND_LINK + "/user-data-as-friend/{friendId}", 1L)
                .principal(principal)
            )
            .andExpect(status().isNotFound());

        verify(userService).findByEmail(principal.getName());
        verify(friendService).getUserAsFriend(anyLong(),anyLong());
    }
}
