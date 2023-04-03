package greencity.controller;

import com.google.gson.Gson;
import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.dto.habit.HabitAssignPropertiesDto;
import greencity.dto.habit.HabitAssignStatDto;
import greencity.dto.habit.UpdateUserShoppingListDto;
import greencity.dto.habit.UserShoppingAndCustomShoppingListsDto;
import greencity.dto.user.UserVO;
import greencity.enums.HabitAssignStatus;
import greencity.service.HabitAssignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Locale;

import static greencity.ModelUtils.getPrincipal;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    HabitAssignService habitAssignService;

    @InjectMocks
    HabitAssignController habitAssignController;

    private Principal principal = getPrincipal();

    private static final String habitLink = "/habit/assign";

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(habitAssignController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void assign() throws Exception {
        UserVO user = ModelUtils.getUserVO();
        mockMvc.perform(post(habitLink + "/{habitId}", 1)
            .principal(principal))
            .andExpect(status().isCreated());
        Long id = 1L;
        verify(habitAssignService, never()).assignDefaultHabitForUser(id, user);
    }

    @Test
    void getHabitAssign() throws Exception {
        mockMvc.perform(get(habitLink + "/{habitAssignId}", 1))
            .andExpect(status().isOk());
        verify(habitAssignService).getByHabitAssignIdAndUserId(1L, null, "en");
    }

    @Test
    void updateAssignByHabitId() throws Exception {
        HabitAssignStatDto habitAssignStatDto = new HabitAssignStatDto();
        habitAssignStatDto.setStatus(HabitAssignStatus.INPROGRESS);
        Gson gson = new Gson();
        String json = gson.toJson(habitAssignStatDto);
        mockMvc.perform(patch(habitLink + "/{habitId}", 1)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).updateStatusByHabitIdAndUserId(1L, null, habitAssignStatDto);
    }

    @Test
    void updateHabitAssignDurationTest() throws Exception {
        mockMvc.perform(put(habitLink + "/{habitAssignId}/update-habit-duration?duration=15", 1L)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).updateUserHabitInfoDuration(1L, null, 15);
    }

    @Test
    void enrollHabit() throws Exception {
        mockMvc.perform(post(habitLink + "/{habitId}/enroll/{date}", 1, LocalDate.now()))
            .andExpect(status().isOk());
        verify(habitAssignService).enrollHabit(1L, null, LocalDate.now(), "en");
    }

    @Test
    void unenrollHabit() throws Exception {
        mockMvc.perform(post(habitLink + "/{habitId}/unenroll/{date}", 1, LocalDate.now()))
            .andExpect(status().isOk());
        verify(habitAssignService).unenrollHabit(1L, null, LocalDate.now());
    }

    @Test
    void getHabitAssignBetweenDatesTest() throws Exception {
        Locale locale = new Locale("en", "US");
        mockMvc.perform(get(habitLink + "/activity/{from}/to/{to}", LocalDate.now(), LocalDate.now().plusDays(2L)))
            .andExpect(status().isOk());

        verify(habitAssignService).findHabitAssignsBetweenDates(null, LocalDate.now(),
            LocalDate.now().plusDays(2L), "en");
    }

    @Test
    void cancelHabitAssign() throws Exception {
        mockMvc.perform(patch(habitLink + "/cancel/{habitId}", 1L)
            .principal(principal))
            .andExpect(status().isOk());

        verify(habitAssignService).cancelHabitAssign(1L, null);
    }

    @Test
    void getHabitAssignByHabitIdTest() throws Exception {
        mockMvc.perform(get(habitLink + "/{habitId}/active", 1L)
            .principal(principal))
            .andExpect(status().isOk());

        verify(habitAssignService).findHabitAssignByUserIdAndHabitId(null, 1L, "en");
    }

    @Test
    void getCurrentUserHabitAssignsByIdAndAcquired() throws Exception {
        mockMvc.perform(get(habitLink + "/allForCurrentUser")
            .principal(principal))
            .andExpect(status().isOk());

        verify(habitAssignService).getAllHabitAssignsByUserIdAndStatusNotCancelled(null, "en");
    }

    @Test
    void deleteHabitAssignTest() throws Exception {
        Long habitAssignId = 1L;

        Principal principal = () -> "xd87@ukr.net";
        mockMvc.perform(delete(habitLink + "/delete/{habitAssignId}", habitAssignId)
            .principal(principal)).andExpect(status().isOk());
        verify(habitAssignService).deleteHabitAssign(habitAssignId, null);
    }

    @Test
    void updateShoppingListStatus() throws Exception {
        UpdateUserShoppingListDto updateUserShoppingListDto = ModelUtils.getUpdateUserShoppingListDto();
        Gson gson = new Gson();
        String shoppingListJSON = gson.toJson(updateUserShoppingListDto);
        mockMvc.perform(put(habitLink + "/saveShoppingListForHabitAssign")
            .content(shoppingListJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).updateUserShoppingListItem(updateUserShoppingListDto);
    }

    @Test
    void assignCustom() throws Exception {
        HabitAssignPropertiesDto propertiesDto = ModelUtils.getHabitAssignPropertiesDto();
        Gson gson = new Gson();
        String json = gson.toJson(propertiesDto);
        UserVO userVO = new UserVO();
        mockMvc.perform(post(habitLink + "/{habitId}/custom", 1L)
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
        mockMvc.perform(get(habitLink + "/active/{date}", LocalDate.now()))
            .andExpect(status().isOk());

        verify(habitAssignService).findInprogressHabitAssignsOnDate(null, LocalDate.now(), "en");
    }

    @Test
    void getUsersHabitByHabitId() throws Exception {
        Long habitAssignId = 1L;
        mockMvc.perform(get(habitLink + "/{habitAssignId}/more", habitAssignId))
            .andExpect(status().isOk());

        verify(habitAssignService).findHabitByUserIdAndHabitAssignId(null, habitAssignId, "en");
    }

    @Test
    void getUserAndCustomListByUserIdAndHabitId() throws Exception {
        Long habitAssignId = 1L;
        mockMvc.perform(get(habitLink + "/{habitAssignId}/allUserAndCustomList", habitAssignId))
            .andExpect(status().isOk());
        verify(habitAssignService).getUserShoppingAndCustomShoppingLists(null, habitAssignId, "en");
    }

    @Test
    void getUserAndCustomListByUserIdAndHabitIdAndLocale() throws Exception {
        Long habitAssignId = 1L;
        mockMvc.perform(get(habitLink + "/{habitAssignId}/allUserAndCustomList", habitAssignId)
            .locale(Locale.forLanguageTag("ua")))
            .andExpect(status().isOk());
        verify(habitAssignService).getUserShoppingAndCustomShoppingLists(null, habitAssignId, "ua");
    }

    @Test
    void getListOfUserAndCustomShoppingListsInprogress() throws Exception {
        mockMvc.perform(get(habitLink + "/allUserAndCustomShoppingListsInprogress")
            .principal(principal)
            .locale(Locale.forLanguageTag("en")))
            .andExpect(status().isOk());
        verify(habitAssignService).getListOfUserAndCustomShoppingListsWithStatusInprogress(null, "en");
    }

    @Test
    void updateUserAndCustomShoppingLists() throws Exception {
        UserShoppingAndCustomShoppingListsDto dto = ModelUtils.getUserShoppingAndCustomShoppingListsDto();
        Gson gson = new Gson();
        String json = gson.toJson(dto);
        mockMvc.perform(put(habitLink + "/{habitAssignId}/allUserAndCustomList", 1L)
            .locale(Locale.forLanguageTag("ua"))
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).fullUpdateUserAndCustomShoppingLists(null, 1L, dto, "ua");
    }
}