package greencity.security.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Service that provides a token transfer to cookies.
 */
public interface TokenService {
    /**
     * Method that pass the token to cookies.
     *
     * @param accessToken {@link String}
     * @param response    {@link HttpServletResponse}
     */
    void passTokenToCookies(String accessToken, HttpServletResponse response);
}
