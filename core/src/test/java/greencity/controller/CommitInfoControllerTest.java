package greencity.controller;

import greencity.dto.commitinfo.CommitInfoDto;
import greencity.exception.exceptions.ResourceNotFoundException;
import greencity.service.CommitInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(MockitoExtension.class)
class CommitInfoControllerTest {
    @InjectMocks
    private CommitInfoController commitInfoController;

    @Mock
    private CommitInfoService commitInfoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commitInfoController).build();
    }

    private static final String COMMIT_INFO_URL = "/commit-info";
    private static final String COMMIT_HASH = "abc123";
    private static final String COMMIT_DATE = "16/12/2024 12:06:32";

    @Test
    void getCommitInfoReturnsSuccessTest() throws Exception {
        CommitInfoDto commitInfoDto = new CommitInfoDto(COMMIT_HASH, COMMIT_DATE);
        when(commitInfoService.getLatestCommitInfo()).thenReturn(commitInfoDto);

        mockMvc.perform(get(COMMIT_INFO_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.commitHash").value(COMMIT_HASH))
            .andExpect(jsonPath("$.commitDate").value(COMMIT_DATE));

        verify(commitInfoService, times(1)).getLatestCommitInfo();
    }

    @Test
    void getCommitInfoReturnsErrorTest() throws Exception {
        when(commitInfoService.getLatestCommitInfo()).thenThrow(new ResourceNotFoundException());

        mockMvc.perform(get(COMMIT_INFO_URL))
            .andExpect(status().isNotFound());

        verify(commitInfoService, times(1)).getLatestCommitInfo();
    }
}
