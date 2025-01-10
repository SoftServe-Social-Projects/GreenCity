package greencity.controller;

import com.google.gson.Gson;
import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.converters.UserArgumentResolver;
import greencity.dto.habit.HabitAssignCustomPropertiesDto;
import greencity.dto.habit.HabitAssignStatDto;
import greencity.dto.habit.UserToDoAndCustomToDoListsDto;
import greencity.dto.user.UserVO;
import greencity.enums.HabitAssignStatus;
import greencity.service.HabitAssignService;
import greencity.service.UserService;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static greencity.ModelUtils.getPrincipal;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HabitAssignControllerTest {
    private MockMvc mockMvc;

    @Mock
    private RestClient restClient;

    @Mock
    private HabitAssignService habitAssignService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private HabitAssignController habitAssignController;

    private Principal principal = getPrincipal();

    private static final String habitLink = "/habit/assign";
    private static final UserVO userVO = ModelUtils.getUserVO();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(habitAssignController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .build();
    }

    @Test
    void assign() throws Exception {

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(post(habitLink + "/{habitId}", 1L)
                .principal(principal))
            .andExpect(status().isCreated());
        verify(habitAssignService).assignDefaultHabitForUser(1L, userVO);
    }

    @Test
    void getHabitAssign() throws Exception {
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(get(habitLink + "/{habitAssignId}", 1L)
                .principal(principal))
            .andExpect(status().isOk());
        verify(habitAssignService).getByHabitAssignIdAndUserId(1L, userVO.getId(), "en");
    }

    @Test
    void updateAssignByHabitAssignId() throws Exception {
        HabitAssignStatDto habitAssignStatDto = new HabitAssignStatDto();
        habitAssignStatDto.setStatus(HabitAssignStatus.INPROGRESS);
        Gson gson = new Gson();
        String json = gson.toJson(habitAssignStatDto);
        mockMvc.perform(patch(habitLink + "/{habitAssignId}", 1L)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).updateStatusByHabitAssignId(1L, habitAssignStatDto);
    }

    @Test
    void updateStatusAndDurationOfHabitAssignTest() throws Exception {
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(put(habitLink + "/{habitAssignId}/update-status-and-duration?duration=15", 1L)
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).updateStatusAndDurationOfHabitAssign(1L, userVO.getId(), 15);
    }

    @Test
    void updateHabitAssignDurationTest() throws Exception {
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(put(habitLink + "/{habitAssignId}/update-habit-duration?duration=15", 1L)
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).updateUserHabitInfoDuration(1L, userVO.getId(), 15);
    }

    @Test
    void enrollHabit() throws Exception {
        Long habitAssignId = 2L;
        LocalDate date = LocalDate.now();

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(post(habitLink + "/{habitAssignId}/enroll/{date}", habitAssignId, date)
            .principal(principal))
            .andExpect(status().isOk());
        verify(habitAssignService).enrollHabit(habitAssignId, userVO.getId(), date, "en");
    }

    @Test
    void unenrollHabit() throws Exception {
        Long habitAssignId = 1L;
        LocalDate date = LocalDate.now();

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(post(habitLink + "/{habitAssignId}/unenroll/{date}", habitAssignId, date)
            .principal(principal))
            .andExpect(status().isOk());
        verify(habitAssignService).unenrollHabit(habitAssignId, userVO.getId(), date);
    }

    @Test
    void getHabitAssignBetweenDatesTest() throws Exception {
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(2L);

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(get(habitLink + "/activity/{from}/to/{to}", from, to)
            .principal(principal))
            .andExpect(status().isOk());

        verify(habitAssignService).findHabitAssignsBetweenDates(userVO.getId(), from, to, "en");
    }

    @Test
    void getHabitAssignByHabitIdTest() throws Exception {
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(get(habitLink + "/{habitId}/active", 1L)
                .principal(principal))
            .andExpect(status().isOk());

        verify(habitAssignService).findHabitAssignByUserIdAndHabitId(userVO.getId(), 1L, "en");
    }

    @Test
    void getCurrentUserHabitAssignsByIdAndAcquired() throws Exception {
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(get(habitLink + "/allForCurrentUser")
                .principal(principal))
            .andExpect(status().isOk());

        verify(habitAssignService).getAllHabitAssignsByUserIdAndStatusNotCancelled(userVO.getId(), "en");
    }

    @Test
    void getAllMutualHabitsWithUserTest() throws Exception {
        long friendId = 2L;
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(get(habitLink + "/allMutualHabits/{userId}", friendId)
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(principal.getName());
        verify(habitAssignService).getAllMutualHabitAssignsWithUserAndStatusNotCancelled(friendId, userVO.getId(),
            PageRequest.of(0, 20));
    }

    @Test
    void getMyHabitsOfCurrentUserTest() throws Exception {
        long friendId = 2L;
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(get(habitLink + "/myHabits/{userId}", friendId)
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(principal.getName());
        verify(habitAssignService).getMyHabitsOfCurrentUserAndStatusNotCancelled(friendId, userVO.getId(),
            PageRequest.of(0, 20));
    }

    @Test
    void getUserHabitAssignsByIdAndAcquiredTest() throws Exception {
        long friendId = 2L;
        mockMvc.perform(get(habitLink + "/allUser/{userId}", friendId)
            .principal(principal))
            .andExpect(status().isOk());

        verify(habitAssignService).getAllByUserIdAndStatusNotCancelled(friendId, PageRequest.of(0, 20));
    }

    @Test
    void deleteHabitAssignTest() throws Exception {
        Long habitAssignId = 1L;

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(delete(habitLink + "/delete/{habitAssignId}", habitAssignId)
            .principal(principal)).andExpect(status().isOk());
        verify(habitAssignService).deleteHabitAssign(habitAssignId, userVO.getId());
    }

    @Test
    void assignCustom() throws Exception {
        HabitAssignCustomPropertiesDto propertiesDto = ModelUtils.getHabitAssignCustomPropertiesDto();
        Gson gson = new Gson();
        String json = gson.toJson(propertiesDto);

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(post(habitLink + "/{habitId}/custom", 1L)
            .principal(principal)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
        verify(habitAssignService).assignCustomHabitForUser(1L, userVO, propertiesDto);
    }

    @Test
    void getAllHabitAssignsByHabitIdAndAcquired() throws Exception {
        mockMvc.perform(get(habitLink + "/{habitId}/all", 1L)
            .principal(principal))
            .andExpect(status().isOk());
        verify(habitAssignService).getAllHabitAssignsByHabitIdAndStatusNotCancelled(1L, "en");
    }

    @Test
    void getInprogressHabitAssignOnDate() throws Exception {
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(get(habitLink + "/active/{date}", LocalDate.now())
                .principal(principal))
            .andExpect(status().isOk());

        verify(habitAssignService).findInprogressHabitAssignsOnDate(userVO.getId(), LocalDate.now(), "en");
    }

    @Test
    void getUsersHabitByHabitId() throws Exception {
        Long habitAssignId = 1L;

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(get(habitLink + "/{habitAssignId}/more", habitAssignId)
            .principal(principal))
            .andExpect(status().isOk());

        verify(habitAssignService).findHabitByUserIdAndHabitAssignId(userVO.getId(), habitAssignId, "en");
    }

    @Test
    void getUserAndCustomListByUserIdAndHabitId() throws Exception {
        Long habitAssignId = 1L;

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(get(habitLink + "/{habitAssignId}/allUserAndCustomList", habitAssignId)
            .principal(principal))
            .andExpect(status().isOk());
        verify(habitAssignService).getUserToDoAndCustomToDoLists(userVO.getId(), habitAssignId, "en");
    }

    @Test
    void getUserAndCustomListByUserIdAndHabitIdAndLocale() throws Exception {
        Long habitAssignId = 1L;

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(get(habitLink + "/{habitAssignId}/allUserAndCustomList", habitAssignId)
            .principal(principal)
            .locale(Locale.forLanguageTag("ua")))
            .andExpect(status().isOk());
        verify(habitAssignService).getUserToDoAndCustomToDoLists(userVO.getId(), habitAssignId, "ua");
    }

    @Test
    void getListOfUserAndCustomToDoListsInprogress() throws Exception {
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(get(habitLink + "/allUserAndCustomToDoListsInprogress")
                .principal(principal)
                .locale(Locale.forLanguageTag("en")))
            .andExpect(status().isOk());
        verify(habitAssignService).getListOfUserAndCustomToDoListsWithStatusInprogress(userVO.getId(), "en");
    }

    @Test
    void updateUserAndCustomToDoLists() throws Exception {
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        UserToDoAndCustomToDoListsDto dto = ModelUtils.getUserToDoAndCustomToDoListsDto();
        Gson gson = new Gson();
        String json = gson.toJson(dto);
        mockMvc.perform(put(habitLink + "/{habitAssignId}/allUserAndCustomList", 1L)
                .principal(principal)
                .locale(Locale.forLanguageTag("ua"))
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).fullUpdateUserAndCustomToDoLists(userVO.getId(), 1L, dto, "ua");
    }

    @Test
    void updateProgressNotificationHasDisplayedTest() throws Exception {
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(
                put(habitLink + "/{habitAssignId}/updateProgressNotificationHasDisplayed", 1L)
                    .principal(principal)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).updateProgressNotificationHasDisplayed(1L, userVO.getId());
    }

    @Test
    void inviteFriendRequest() throws Exception {
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(post(habitLink + "/{habitId}/invite", 1L)
                        .param("friendsIds", "2", "3", "4")
                        .principal(principal)
                        .locale(Locale.forLanguageTag("ua")))
                .andExpect(status().isOk());
        verify(habitAssignService).inviteFriendForYourHabitWithEmailNotification(userVO, List.of(2L,3L,4L), 1L,
            Locale.forLanguageTag("ua"));
    }

    @Test
    void confirmInvitation() throws Exception {
        mockMvc.perform(get(habitLink + "/confirm/{habitAssignId}", 1L))
            .andExpect(status().is3xxRedirection());
        verify(habitAssignService).confirmHabitInvitation(1L);
    }

    @Test
    void getFriendsHabitsStreakTest() throws Exception {
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        mockMvc.perform(get(habitLink + "/{habitId}/friends/habit-duration-info", 1L)
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).getAllHabitsWorkingDaysInfoForCurrentUserFriends(1L, 1L);
    }
}
