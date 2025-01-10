package greencity.webcontroller;

import greencity.security.service.TokenService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TokenControllerTest {

    private MockMvc mockMvc;

    private static final String TOKEN_LINK = "/token";

    @InjectMocks
    private TokenController tokenController;

    @Mock
    private TokenService tokenService;

    @Mock
    private Validator mockValidator;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(tokenController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setValidator(mockValidator)
            .build();
    }

    @Test
    void passTokenToCookies() throws Exception {
        String accessToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtYXJ";

        mockMvc.perform(get(TOKEN_LINK + "?accessToken=" + accessToken))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/management"));

        Mockito.verify(tokenService, Mockito.times(1))
            .passTokenToCookies(Mockito.eq(accessToken), Mockito.any(HttpServletResponse.class));
    }

    @Test
    void passTokenToCookiesSpecialCharactersToken() throws Exception {
        String specialAccessToken = "eyJhbGciOiJIUzI1NiJ9~eyJzdWIiOiJ";

        mockMvc.perform(get(TOKEN_LINK + "?accessToken=" + specialAccessToken))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/management"));

        Mockito.verify(tokenService, Mockito.times(1))
            .passTokenToCookies(Mockito.eq(specialAccessToken), Mockito.any(HttpServletResponse.class));
    }
}