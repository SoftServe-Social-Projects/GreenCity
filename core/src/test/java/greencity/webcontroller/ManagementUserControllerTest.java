package greencity.webcontroller;

import com.google.gson.Gson;
import greencity.ModelUtils;
import greencity.dto.PageableDto;
import greencity.dto.user.UserManagementDto;
import greencity.entity.User;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManagementUserControllerTest {

    private static final String managementUserControllerLink = "/management/users";

    private MockMvc mockMvc;

    @InjectMocks
    private ManagementUserController managementUserController;

    @Mock
    UserService userService;

    @Mock
    ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(managementUserController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void getAllUsers() throws Exception {
        int page = 0;
        int size = 10;
        Pageable paging = PageRequest.of(page, size, Sort.by("id").descending());
        List<UserManagementDto> userManagementDtos = Collections.singletonList(new UserManagementDto());
        PageableDto<UserManagementDto> userManagementDtoPageableDto = new PageableDto<>(userManagementDtos, 1, 0, 1);
        when(userService.findUserForManagementByPage(paging)).thenReturn(userManagementDtoPageableDto);
        this.mockMvc.perform(get(managementUserControllerLink)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(view().name("core/management_user"))
                .andExpect(status().isOk());

        verify(userService).findUserForManagementByPage(paging);

    }

    @Test
    void updateUser() throws Exception {
        UserManagementDto userManagementDto = new UserManagementDto();
        Gson gson = new Gson();
        String json = gson.toJson(userManagementDto);
        this.mockMvc.perform(put(managementUserControllerLink)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService, never()).updateUser(userManagementDto);
    }

    @Test
    void findById() throws Exception {
        User user = ModelUtils.getUser();
        when(userService.findById(1L)).thenReturn(user);
        when(modelMapper.map(user, UserManagementDto.class)).thenReturn(new UserManagementDto());

        this.mockMvc.perform(get(managementUserControllerLink + "/findById?id=1"))
                .andExpect(status().isOk());

        verify(userService).findById(1L);
    }

    @Test
    void deactivateUser() throws Exception {
        this.mockMvc.perform(post(managementUserControllerLink + "/deactivate?id=1"))
                .andExpect(status().isOk());

        verify(userService, times(1)).deactivateUser(1L);
    }

    @Test
    void setActivatedStatus() throws Exception {
        this.mockMvc.perform(post(managementUserControllerLink + "/activate?id=1"))
                .andExpect(status().isOk());

        verify(userService, times(1)).setActivatedStatus(1L);
    }

    @Test
    void deactivateAll() throws Exception {
        List<Long> longList = Arrays.asList(1L, 2L);
        Gson gson = new Gson();
        String json = gson.toJson(longList);

        this.mockMvc.perform(post(managementUserControllerLink + "/deactivateAll")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService, times(1)).deactivateAllUsers(longList);

    }
}