package greencity.controller;

import greencity.config.SecurityConfig;
import greencity.dto.user.UserVO;
import greencity.service.HabitInvitationService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ContextConfiguration
@Import(SecurityConfig.class)
@ExtendWith(MockitoExtension.class)
class HabitInvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private HabitInvitationService habitInvitationService;

    @InjectMocks
    private HabitInvitationController habitInvitationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(habitInvitationController).build();
    }

    @Test
    @SneakyThrows
    void acceptHabitInvitationShouldReturn200() {
        Long invitationId = 1L;
        UserVO userVO = new UserVO();
        doNothing().when(habitInvitationService).acceptHabitInvitation(invitationId, userVO);
        mockMvc.perform(patch("/habit/invite/{invitationId}/accept", invitationId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitInvitationService, times(1)).acceptHabitInvitation(invitationId, userVO);
    }

    @Test
    @SneakyThrows
    void rejectHabitInvitationShouldReturn200() {
        Long invitationId = 2L;
        UserVO userVO = new UserVO();
        doNothing().when(habitInvitationService).rejectHabitInvitation(invitationId, userVO);
        mockMvc.perform(delete("/habit/invite/{invitationId}/reject", invitationId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitInvitationService, times(1)).rejectHabitInvitation(invitationId, userVO);
    }
}
