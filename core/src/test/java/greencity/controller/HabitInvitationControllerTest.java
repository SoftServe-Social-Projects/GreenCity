package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.config.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.user.UserVO;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.HabitInvitationService;
import greencity.service.UserService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.security.Principal;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockitoSettings(strictness = Strictness.LENIENT)
@ContextConfiguration
@Import(SecurityConfig.class)
@ExtendWith(MockitoExtension.class)
class HabitInvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Mock
    private HabitInvitationService habitInvitationService;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private HabitInvitationController habitInvitationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(habitInvitationController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
            .build();
    }

    private final Principal principal = ModelUtils.getPrincipal();
    private final UserVO userVO = ModelUtils.getUserVO();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    @Test
    @SneakyThrows
    void acceptHabitInvitationShouldReturn200() {
        Long invitationId = 1L;
        UserVO userVO = new UserVO();

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(patch("/habit/invite/{invitationId}/accept", invitationId)
            .principal(principal))
            .andExpect(status().isOk());

        verify(habitInvitationService, times(1)).acceptHabitInvitation(invitationId, userVO);
    }

    @Test
    @SneakyThrows
    void rejectHabitInvitationShouldReturn200() {
        Long invitationId = 2L;
        UserVO userVO = new UserVO();
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(delete("/habit/invite/{invitationId}/reject", invitationId)
            .principal(principal))
            .andExpect(status().isOk());
        verify(habitInvitationService, times(1)).rejectHabitInvitation(invitationId, userVO);
    }

    @Test
    @SneakyThrows
    void getAllUserHabitInvitationsTest() {
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(get("/habit/invite/requests")
                        .principal(principal))
                .andExpect(status().isOk());

        verify(userService).findByEmail(principal.getName());
        verify(habitInvitationService).getAllUserHabitInvitationRequests(userVO.getId(), "en", PageRequest.of(0, 20));
    }

}
