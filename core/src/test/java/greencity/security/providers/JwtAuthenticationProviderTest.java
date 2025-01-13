package greencity.security.providers;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import greencity.enums.Role;
import greencity.security.jwt.JwtTool;
import io.jsonwebtoken.ExpiredJwtException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author Yurii Koval
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationProviderTest {
    private final Role expectedRole = Role.ROLE_ADMIN;

    @Mock
    JwtTool jwtTool;

    @InjectMocks
    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @BeforeEach
    public void setUp() {
        jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtTool);
    }

    @Test
    void authenticateWithValidAccessToken() {
        final String accessToken = """
            eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJxcXFAZW1haWwuY29tIiwicm9sZSI6WyJST0xFX0FE\
            TUlOIl0sImlhdCI6MTY1NDYzNjc2OSwiZXhwIjo2MTY1NDYzNjcwOX0.ajLrWu7MNoXWlPRWi\
            LD9d7vDzScqx8-9eBl3ZlYlspQ\
            """;
        when(jwtTool.getAccessTokenKey()).thenReturn("12312312312312312312312312312312312");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            accessToken,
            null);
        Authentication actual = jwtAuthenticationProvider.authenticate(authentication);
        final String expectedEmail = "qqq@email.com";
        assertEquals(expectedEmail, actual.getPrincipal());
        assertEquals(
            Stream.of(expectedRole)
                .map(Role::toString)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()),
            actual.getAuthorities());
        assertEquals("", actual.getCredentials());
    }

    @Test
    void authenticateWithExpiredAccessToken() {
        when(jwtTool.getAccessTokenKey()).thenReturn("12312312312312312312312312312312312");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                """
                    eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJxcXFAZW1haWwuY29tIiwicm9sZSI6WyJST0xF\
                    X0FETUlOIl0sImlhdCI6MTY1NDYzNjc2OSwiZXhwIjoxNjU0NjM2NzcwfQ.pnNNTOtgKp\
                    ZBdfX2XXtXBscmAOFVuk1aLbU0hH3SwQ4\
                    """,
                null);
        Assertions
                .assertThrows(ExpiredJwtException.class,
                        () -> jwtAuthenticationProvider.authenticate(authentication));
    }

    @Test
    void authenticateWithMalformedAccessToken() {
        when(jwtTool.getAccessTokenKey()).thenReturn("123123123");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "Malformed"
                + ".eyJzdWIiOiJ0ZXN0QGdtYWlsLmNvbSIsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE1Nz"
                + "U4Mzk3OTMsImV4cCI6MTU3NTg0MDY5M30"
                + ".DYna1ycZd7eaUBrXKGzYvEMwcybe7l5YiliOR-LfyRw",
            null);
        Assertions
            .assertThrows(Exception.class,
                () -> jwtAuthenticationProvider.authenticate(authentication));
    }

    @Test
    void supportsTest() {
        assertTrue(jwtAuthenticationProvider.supports(UsernamePasswordAuthenticationToken.class));
    }
}
