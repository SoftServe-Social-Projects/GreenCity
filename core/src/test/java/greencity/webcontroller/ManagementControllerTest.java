package greencity.webcontroller;

import greencity.TestConst;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManagementControllerTest {
    private MockMvc mockMvc;

    private static final String link = "/management";
    private static final String loginLink = "/login";

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
            .andExpect(redirectedUrl(link + loginLink))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void loginTest() throws Exception {
        ReflectionTestUtils.setField(managementController,
            "greenCityUserServerAddress", TestConst.GREENCITY_USER_SERVER_ADDRESS);

        SecurityContextHolder.getContext()
            .setAuthentication(new AnonymousAuthenticationToken("GUEST", "anonymousUser", AuthorityUtils
                .createAuthorityList("ROLE_ANONYMOUS")));

        String expectedUrl = UriComponentsBuilder.fromHttpUrl(TestConst.GREENCITY_USER_SERVER_ADDRESS)
            .path(link + loginLink)
            .build()
            .toUriString();

        this.mockMvc.perform(get(link + loginLink))
            .andExpect(redirectedUrl(expectedUrl))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void loginIfAuthenticated() throws Exception {
        SecurityContextHolder.getContext()
            .setAuthentication(new AnonymousAuthenticationToken("GUEST", "admin@df231", AuthorityUtils
                .createAuthorityList("ROLE_ADMIN")));
        this.mockMvc.perform(get(link + loginLink))
            .andExpect(redirectedUrl(link))
            .andExpect(status().is3xxRedirection());
    }
}
