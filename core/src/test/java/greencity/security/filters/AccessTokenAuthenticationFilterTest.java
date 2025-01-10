package greencity.security.filters;

import greencity.client.RestClient;
import greencity.dto.user.UserVO;
import greencity.security.jwt.JwtTool;
import io.jsonwebtoken.ExpiredJwtException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccessTokenAuthenticationFilterTest {
    private PrintStream systemOut;
    private ByteArrayOutputStream systemOutContent;

    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    FilterChain chain;
    @Mock
    JwtTool jwtTool;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    RestClient restClient;

    @InjectMocks
    private AccessTokenAuthenticationFilter authenticationFilter;

    @BeforeEach
    void setUp() {
        systemOut = System.out;
        systemOutContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(systemOutContent));
    }

    @AfterEach
    void restoreSystemOutStream() {
        System.setOut(systemOut);
    }

    @Test
    void doFilterInternalTest() throws IOException, ServletException {
        when(jwtTool.getTokenFromHttpServletRequest(request)).thenReturn("SuperSecretAccessToken");
        when(authenticationManager.authenticate(any()))
            .thenReturn(new UsernamePasswordAuthenticationToken("test@mail.com", null));
        when(restClient.findNotDeactivatedByEmail("test@mail.com"))
            .thenReturn(Optional.of(UserVO.builder().id(1L).build()));
        doNothing().when(chain).doFilter(request, response);

        authenticationFilter.doFilterInternal(request, response, chain);
        verify(authenticationManager).authenticate(any());
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternalTokenHasExpiredTest() throws IOException, ServletException {
        String token = "SuperSecretAccessToken";
        when(jwtTool.getTokenFromHttpServletRequest(request)).thenReturn(token);
        when(authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(token, null)))
            .thenThrow(ExpiredJwtException.class);
        authenticationFilter.doFilterInternal(request, response, chain);
        verify(jwtTool).getTokenFromHttpServletRequest(request);
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void doFilterInternalAccessDeniedTest() throws IOException, ServletException {
        String token = "SuperSecretAccessToken";
        when(jwtTool.getTokenFromHttpServletRequest(request)).thenReturn(token);
        when(authenticationManager.authenticate(any()))
            .thenReturn(new UsernamePasswordAuthenticationToken("test@mail.com", null));
        when(restClient.findNotDeactivatedByEmail("test@mail.com")).thenThrow(RuntimeException.class);
        authenticationFilter.doFilterInternal(request, response, chain);
        verify(jwtTool).getTokenFromHttpServletRequest(request);
        verify(authenticationManager).authenticate(any());
    }
}
