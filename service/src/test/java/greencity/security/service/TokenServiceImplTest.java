package greencity.security.service;

import greencity.exception.exceptions.BadRequestException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {
    @InjectMocks
    private TokenServiceImpl tokenService;

    @Mock
    private HttpServletResponse response;

    @Test
    void testPassTokenToCookies_SecureCookieEnabled_Success() {
        String accessToken = "eyJhbGciOiJIUzI1NiJ9.validtoken";
        ReflectionTestUtils.setField(tokenService, "secureCookie", true);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

        tokenService.passTokenToCookies(accessToken, response);

        verify(response).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        assertEquals("accessToken", cookie.getName());
        assertEquals(URLEncoder.encode(accessToken, StandardCharsets.UTF_8), cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
        assertEquals("/", cookie.getPath());
    }

    @Test
    void testPassTokenToCookies_SecureCookieDisabled_Success() {
        String accessToken = "eyJhbGciOiJIUzI1NiJ9.validtoken";
        ReflectionTestUtils.setField(tokenService, "secureCookie", false);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

        tokenService.passTokenToCookies(accessToken, response);

        verify(response).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        assertEquals("accessToken", cookie.getName());
        assertEquals(URLEncoder.encode(accessToken, StandardCharsets.UTF_8), cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertFalse(cookie.getSecure());
        assertEquals("/", cookie.getPath());
    }

    @Test
    void testPassTokenToCookies_InvalidToken_ThrowsBadRequestException() {
        String invalidToken = "invalid.token";

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            tokenService.passTokenToCookies(invalidToken, response);
        });

        assertEquals("bad access token", exception.getMessage());
        verify(response, never()).addCookie(any(Cookie.class));
    }

    @Test
    void testPassTokenToCookies_TokenEncoding_Success() {
        String accessToken = "eyJhbGciOiJIUzI1NiJ9.tokenWithSpecialChars!@#";
        ReflectionTestUtils.setField(tokenService, "secureCookie", true);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

        tokenService.passTokenToCookies(accessToken, response);

        verify(response).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        assertEquals(URLEncoder.encode(accessToken, StandardCharsets.UTF_8), cookie.getValue());
    }
}
