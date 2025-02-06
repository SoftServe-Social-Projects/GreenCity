package greencity.webcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
import greencity.converters.UserArgumentResolver;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.PageableDetailedDto;
import greencity.dto.user.UserFilterDto;
import greencity.dto.user.UserFilterDtoRequest;
import greencity.dto.user.UserFilterDtoResponse;
import greencity.dto.user.UserManagementDto;
import greencity.dto.user.UserManagementVO;
import greencity.dto.user.UserManagementViewDto;
import greencity.dto.user.UserVO;
import greencity.entity.User;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.service.FilterService;
import greencity.service.HabitAssignService;
import greencity.service.UserService;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpHeaders;
import java.nio.charset.StandardCharsets;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUser;
import static greencity.ModelUtils.getUserFilterDtoResponse;
import static greencity.ModelUtils.getUserPageableDetailedDto;
import static greencity.ModelUtils.getUserRoleBody;
import static greencity.ModelUtils.getUserStatusBody;
import static greencity.ModelUtils.getUserVO;
import static greencity.TestConst.MANAGEMENT_USER_LINK;
import static greencity.TestConst.ROLE_ADMIN;
import static greencity.TestConst.STATUS_ACTIVATED;
import static greencity.TestConst.TEST_QUERY;
import static greencity.TestConst.USER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.doubleThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManagementUserControllerTest {
    @Mock
    private ModelMapper modelMapper;

    @Mock
    private RestClient restClient;

    @Mock
    private UserService userService;

    @Mock
    HabitAssignService habitAssignService;

    @Mock
    private FilterService filterService;

    @InjectMocks
    private ManagementUserController managementUserController;

    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(managementUserController).setCustomArgumentResolvers(
            new PageableHandlerMethodArgumentResolver(), new UserArgumentResolver(userService, modelMapper)).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void changeRoleTest() {
        Map<String, String> body = getUserRoleBody();
        managementUserController.changeRole(USER_ID, body);
        verify(restClient).updateRole(USER_ID, Role.ROLE_ADMIN);
    }

    @Test
    void changeStatusTest() {
        Map<String, String> body = getUserStatusBody();
        managementUserController.changeStatus(USER_ID, body);
        verify(restClient).updateStatus(USER_ID, UserStatus.ACTIVATED);
    }

    @Test
    void getAllUsersTest() throws Exception {
        var userVO = getUserVO();
        var principal = getPrincipal();
        List<UserFilterDtoResponse> response = List.of(getUserFilterDtoResponse());

        PageableDetailedDto<UserManagementVO> userPageableDetailedDto = getUserPageableDetailedDto();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(userService.getAllUsersByCriteria(any(UserFilterDto.class), any(Pageable.class)))
            .thenReturn(userPageableDetailedDto);
        when(filterService.getAllFilters(USER_ID)).thenReturn(response);

        mockMvc.perform(get(MANAGEMENT_USER_LINK + "?page=" + 0 + "&size=" + 20 + "&sort=id,DESC")
            .principal(principal)
            .param("status", STATUS_ACTIVATED)
            .param("role", ROLE_ADMIN)
            .param("query", TEST_QUERY))
            .andExpect(model().attribute("users", userPageableDetailedDto));

        verify(userService).findByEmail(anyString());
        verify(userService).getAllUsersByCriteria(any(UserFilterDto.class), any(Pageable.class));
        verify(filterService).getAllFilters(USER_ID);
    }

    @Test
    void searchTest() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, Sort.unsorted());
        UserManagementViewDto userViewDto = UserManagementViewDto.builder().id("1L").name("vivo").email("test@ukr.net")
            .userCredo("Hello").role("1").userStatus("1").build();
        String content = objectMapper.writeValueAsString(userViewDto);
        List<UserManagementVO> userManagementVOS = Collections.singletonList(new UserManagementVO());
        PageableAdvancedDto<UserManagementVO> userAdvancedDto =
            new PageableAdvancedDto<>(userManagementVOS, 20, 0, 0, 0, true, true, true, true);
        when(restClient.search(pageable, userViewDto)).thenReturn(userAdvancedDto);
        mockMvc.perform(post(MANAGEMENT_USER_LINK + "/search").content(content).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getReasonsOfDeactivation() throws Exception {
        List<String> test = List.of("test", "test");
        when(restClient.getDeactivationReason(1L, "en")).thenReturn(test);
        this.mockMvc
            .perform(
                get(MANAGEMENT_USER_LINK + "/reasons" + "?id=1" + "&admin=en").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(restClient).getDeactivationReason(1L, "en");
    }

    @Test
    void setActivatedStatus() throws Exception {
        mockMvc.perform(post(MANAGEMENT_USER_LINK + "/activate" + "?id=1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(restClient).setActivatedStatus(1L);
    }

    @Test
    void getUserLang() throws Exception {
        this.mockMvc.perform(get(MANAGEMENT_USER_LINK + "/lang" + "?id=1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(restClient).getUserLang(1L);
    }

    @Test
    void deactivateUser() throws Exception {
        List<String> test = List.of("test", "test");
        String json = objectMapper.writeValueAsString(test);
        mockMvc.perform(
            post(MANAGEMENT_USER_LINK + "/deactivate" + "?id=1").content(json).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(restClient).deactivateUser(1L, test);
    }

    @Test
    void saveUserTest() throws Exception {
        UserManagementDto dto = ModelUtils.getUserManagementDto();

        mockMvc.perform(post(MANAGEMENT_USER_LINK + "/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(dto)))
            .andExpect(status().is2xxSuccessful());

        verify(restClient).managementRegisterUser(dto);
    }

    @Test
    void saveUserTest_UserAlreadyExists() throws Exception {
        UserManagementDto dto = ModelUtils.getUserManagementDto();

        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST))
            .when(restClient).managementRegisterUser(dto);
        mockMvc.perform(post(MANAGEMENT_USER_LINK + "/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void saveUserTest_InternalServerError() throws Exception {
        UserManagementDto dto = ModelUtils.getUserManagementDto();

        doThrow(new RuntimeException()).when(restClient).managementRegisterUser(dto);

        mockMvc.perform(post(MANAGEMENT_USER_LINK + "/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void updateUserTest() throws Exception {
        UserManagementDto userManagementDto = ModelUtils.getUserManagementDto();
        String context = objectMapper.writeValueAsString(userManagementDto);

        mockMvc.perform(put(MANAGEMENT_USER_LINK + "/").content(context).accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        verify(restClient).updateUser(userManagementDto);
    }

    @Test
    void getUserById() throws Exception {
        mockMvc.perform(get(MANAGEMENT_USER_LINK + "/findById" + "?id=1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(restClient).findById(1L);
    }

    @Test
    void findFriendsByIdTest() throws Exception {
        mockMvc.perform(get(MANAGEMENT_USER_LINK + "/" + 1L + "/friends").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(restClient).findUserFriendsByUserId(1L);
    }

    @Test
    void deactivateAllTest() throws Exception {
        List<Long> list = List.of(1L, 2L);
        String context = objectMapper.writeValueAsString(list);
        mockMvc
            .perform(
                post(MANAGEMENT_USER_LINK + "/deactivateAll").content(context).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        context = objectMapper.writeValueAsString(null);

        mockMvc
            .perform(
                post(MANAGEMENT_USER_LINK + "/deactivateAll").content(context).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(restClient).deactivateAllUsers(list);
    }

    @Test
    void updateUserRole() throws Exception {
        mockMvc.perform(
            put(MANAGEMENT_USER_LINK + "/updateToDoItem/" + 1L + "/" + 1L).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).updateToDoItem(1L, 1L);
    }

    @Test
    void saveUserFilterTest() throws Exception {
        var principal = getPrincipal();
        UserFilterDtoRequest dto = UserFilterDtoRequest.builder().name("Test").userRole("ADMIN").userStatus("ACTIVATED")
            .searchCriteria("Test").build();
        UserVO userVO = getUserVO();
        User user = getUser();

        String content = objectMapper.writeValueAsString(dto);
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, User.class)).thenReturn(user);

        mockMvc.perform(post(MANAGEMENT_USER_LINK + "/filter-save").content(content).principal(principal)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isFound());
    }

    @Test
    void selectFilterTest() throws Exception {
        Long id = 1L;
        UserFilterDtoResponse dto = UserFilterDtoResponse.builder().id(1L).name("Test").userRole("ROLE_ADMIN")
            .userStatus("ACTIVATED").searchCriteria("Test").build();
        when(filterService.getFilterById(id)).thenReturn(dto);
        mockMvc.perform(get(MANAGEMENT_USER_LINK + "/select-filter/" + id).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isFound());

        verify(filterService).getFilterById(id);
    }

    @Test
    void deleteUserFilterTest() throws Exception {
        Long id = 1L;
        mockMvc.perform(get(MANAGEMENT_USER_LINK + "/" + id + "/delete-filter").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isFound());

        verify(filterService).deleteFilterById(id);
    }
}
