package greencity.security.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest {
    private MockedStatic<SecurityContextHolder> securityContextHolderMock;
    @Mock
    private SecurityContext mockSecurityContext;
    @Mock
    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        securityContextHolderMock = Mockito.mockStatic(SecurityContextHolder.class);
        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);
    }

    @AfterEach
    void tearDown() {
        securityContextHolderMock.close();
    }

    @Test
    void isAuthenticatedTrueForAuthenticatedUserTest() {
        Boolean isAuthenticated = true;
        String principal = "principal";
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.isAuthenticated()).thenReturn(isAuthenticated);
        when(mockAuthentication.getPrincipal()).thenReturn(principal);

        boolean result = SecurityUtils.isAuthenticated();

        assertTrue(result);

        verify(mockAuthentication).isAuthenticated();
        verify(mockAuthentication).getPrincipal();
    }

    @Test
    void isAuthenticatedFalseForNullAuthenticationTest() {
        when(mockSecurityContext.getAuthentication()).thenReturn(null);

        boolean result = SecurityUtils.isAuthenticated();
        assertFalse(result);
    }

    @Test
    void isAuthenticatedFalseForAnonymousUserTest() {
        Boolean isAuthenticated = true;
        String principal = "anonymousUser";
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.isAuthenticated()).thenReturn(isAuthenticated);
        when(mockAuthentication.getPrincipal()).thenReturn(principal);

        boolean result = SecurityUtils.isAuthenticated();

        assertFalse(result);

        verify(mockAuthentication).isAuthenticated();
        verify(mockAuthentication).getPrincipal();
    }

    @Test
    void isAuthenticatedFalseForUnauthenticatedUserTest() {
        Boolean isAuthenticated = false;
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.isAuthenticated()).thenReturn(isAuthenticated);

        boolean result = SecurityUtils.isAuthenticated();

        assertFalse(result);

        verify(mockAuthentication).isAuthenticated();
        verify(mockAuthentication, never()).getPrincipal();
    }
}
