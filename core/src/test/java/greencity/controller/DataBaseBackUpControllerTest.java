package greencity.controller;

import greencity.service.DataBaseBackUpService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataBaseBackUpControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DataBaseBackUpService dataBaseBackUpService;

    @InjectMocks
    private DataBaseBackUpController controller;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .build();
    }

    @Test
    void testBackupDatabase() throws Exception {
        doNothing().when(dataBaseBackUpService).backupDatabase();

        mockMvc.perform(MockMvcRequestBuilders.get("/database/backup")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("Database backup completed successfully!"));
    }

    @Test
    void testGetBackupFiles() throws Exception {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> urls = Arrays.asList("http://backup1.com", "http://backup2.com");
        when(dataBaseBackUpService.getBackUpDBUrls(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(urls);

        mockMvc.perform(MockMvcRequestBuilders.get("/database/backupFiles")
            .param("start", start.toString())
            .param("end", end.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
