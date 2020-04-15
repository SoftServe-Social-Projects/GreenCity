package greencity.security.providers;

import greencity.entity.enums.ROLE;
import greencity.security.jwt.JwtTool;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * @author Yurii Koval
 */
public class JwtAuthenticationProviderTest {
    private final ROLE expectedRole = ROLE.ROLE_USER;

    @Mock
    JwtTool jwtTool;

    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtTool);
    }

    @Test
    public void authenticateWithValidAccessToken() {
        final String accessToken = "eyJhbGciOiJIUzI1NiJ9"
            + ".eyJzdWIiOiJ0ZXN0QGdtYWlsLmNvbSIsImF1dGhvcml0aWVzIjpbIlJPTEVfVVN"
            + "FUiJdLCJpYXQiOjE1NzU4NDUzNTAsImV4cCI6NjE1NzU4NDUyOTB9"
            + ".x1D799yGc0dj2uWDQYusnLyG5r6-Rjj6UgBhp2JjVDE";
        when(jwtTool.getAccessTokenKey()).thenReturn("123123123");
        Date expectedExpiration = new Date(61575845290000L); // 3921 year
        Date actualExpiration = Jwts.parser()
            .setSigningKey(jwtTool.getAccessTokenKey())
            .parseClaimsJws(accessToken)
            .getBody()
            .getExpiration();
        assertEquals(expectedExpiration, actualExpiration);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            accessToken,
            null
        );
        Authentication actual = jwtAuthenticationProvider.authenticate(authentication);
        final String expectedEmail = "test@gmail.com";
        assertEquals(expectedEmail, actual.getPrincipal());
        assertEquals(
            Stream.of(expectedRole)
                .map(ROLE::toString)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()),
            actual.getAuthorities()
        );
        assertEquals("", actual.getCredentials());
    }

    @Test(expected = ExpiredJwtException.class)
    public void authenticateWithExpiredAccessToken() {
        when(jwtTool.getAccessTokenKey()).thenReturn("123123123");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "eyJhbGciOiJIUzI1NiJ9"
                + ".eyJzdWIiOiJ0ZXN0QGdtYWlsLmNvbSIsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE1Nz"
                + "U4Mzk3OTMsImV4cCI6MTU3NTg0MDY5M30"
                + ".DYna1ycZd7eaUBrXKGzYvEMwcybe7l5YiliOR-LfyRw",
            null
        );
        jwtAuthenticationProvider.authenticate(authentication);
    }

    @Test(expected = Exception.class)
    public void authenticateWithMalformedAccessToken() {
        when(jwtTool.getAccessTokenKey()).thenReturn("123123123");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "Malformed"
                + ".eyJzdWIiOiJ0ZXN0QGdtYWlsLmNvbSIsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE1Nz"
                + "U4Mzk3OTMsImV4cCI6MTU3NTg0MDY5M30"
                + ".DYna1ycZd7eaUBrXKGzYvEMwcybe7l5YiliOR-LfyRw",
            null
        );
        jwtAuthenticationProvider.authenticate(authentication);
    }
}