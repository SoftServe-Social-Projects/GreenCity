package greencity.webcontroller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManagementControllerTest {
    @Value("${greencityuser.server.address}")
    private String greenCityUserServerAddress;
    private MockMvc mockMvc;

    private static final String link = "/management";

    @InjectMocks
    private ManagementController managementController;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(managementController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void goToIndexTest() throws Exception {
        this.mockMvc.perform(get(link))
            .andExpect(view().name("core/index"))
            .andExpect(status().isOk());
    }

    @Test
    void redirectLoginTest() throws Exception {
        this.mockMvc.perform(get("/"))
            .andExpect(redirectedUrl(link + "/login"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void loginTest() throws Exception {
        SecurityContextHolder.getContext()
            .setAuthentication(new AnonymousAuthenticationToken("GUEST", "anonymousUser", AuthorityUtils
                .createAuthorityList("ROLE_ANONYMOUS")));
        this.mockMvc.perform(get(link + "/login"))
            .andExpect(redirectedUrl(greenCityUserServerAddress + link + "/login"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void loginIfAuthenticated() throws Exception {
        SecurityContextHolder.getContext()
            .setAuthentication(new AnonymousAuthenticationToken("GUEST", "admin@df231", AuthorityUtils
                .createAuthorityList("ROLE_ADMIN")));
        this.mockMvc.perform(get(link + "/login"))
            .andExpect(redirectedUrl(link))
            .andExpect(status().is3xxRedirection());
    }
}
