package greencity.security.jwt;

import static greencity.constant.AppConstant.ROLE;
import greencity.enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import javax.crypto.SecretKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author Yurii Koval
 */
@ExtendWith(SpringExtension.class)
class JwtToolTest {
    @Mock
    HttpServletRequest request;

    @InjectMocks
    private JwtTool jwtTool;

    @BeforeEach
    public void init() {
        ReflectionTestUtils.setField(jwtTool, "accessTokenValidTimeInMinutes", 15);
        ReflectionTestUtils.setField(jwtTool, "accessTokenKey", "123123123123123123123123123123123123");
    }

    @Test
    void createAccessToken() {
        String expectedEmail = "test@gmail.com";
        Role expectedRole = Role.ROLE_USER;
        final String accessToken = jwtTool.createAccessToken(expectedEmail, expectedRole);
        System.out.println(accessToken);

        SecretKey key = Keys.hmacShaKeyFor(jwtTool.getAccessTokenKey().getBytes());

        String actualEmail = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(accessToken)
            .getPayload()
            .getSubject();
        assertEquals(expectedEmail, actualEmail);
        @SuppressWarnings({"unchecked, rawtype"})
        List<String> authorities = (List<String>) Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(accessToken)
            .getPayload()
            .get(ROLE);
        assertEquals(expectedRole, Role.valueOf(authorities.getFirst()));
    }

    @Test
    void getTokenFromHttpServletRequest() {
        final String expectedToken = "An AccessToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + expectedToken);
        String actualToken = jwtTool.getTokenFromHttpServletRequest(request);
        assertEquals(expectedToken, actualToken);
    }
}
