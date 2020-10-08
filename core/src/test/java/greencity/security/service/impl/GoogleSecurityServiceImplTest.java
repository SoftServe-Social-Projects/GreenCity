package greencity.security.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import greencity.ModelUtils;
import greencity.TestConst;
import greencity.entity.User;
import greencity.entity.enums.ROLE;
import greencity.entity.enums.UserStatus;
import greencity.exception.exceptions.UserDeactivatedException;
import greencity.security.dto.SuccessSignInDto;
import greencity.security.jwt.JwtTool;
import greencity.service.UserService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleSecurityServiceImplTest {
    @Mock
    private UserService userService;
    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;
    @Mock
    private JwtTool jwtTool;
    @Mock
    GoogleIdToken googleIdToken;
    @Spy
    GoogleIdToken.Payload payload;

    @InjectMocks
    GoogleSecurityServiceImpl googleSecurityService;

    @Test
    void authenticateUserNotNullTest() throws GeneralSecurityException, IOException {
        User user = ModelUtils.getUser();
        when(googleIdTokenVerifier.verify("1234")).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);
        when(payload.getEmail()).thenReturn("test@mail.com");
        when(userService.findByEmail("test@mail.com")).thenReturn(user);
        SuccessSignInDto result = googleSecurityService.authenticate("1234");
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getId(), result.getUserId());
    }

    @Test
    void authenticateNullUserTest() throws GeneralSecurityException, IOException {
        when(googleIdTokenVerifier.verify("1234")).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);
        when(payload.getEmail()).thenReturn("test@mail.com");
        when(userService.findByEmail("test@mail.com")).thenReturn(null);
        SuccessSignInDto result = googleSecurityService.authenticate("1234");
        assertNull(result.getUserId());
        assertNull(result.getName());
    }

    @Test
    void authenticationThrowsIllegalArgumentExceptionTest() {
        assertThrows(IllegalArgumentException.class,
            () -> googleSecurityService.authenticate("1234"));
    }

    @Test
    void authenticationThrowsUserDeactivatedExceptionTest() throws GeneralSecurityException, IOException {
        User user = User.builder()
            .id(1L)
            .email(TestConst.EMAIL)
            .name(TestConst.NAME)
            .role(ROLE.ROLE_USER)
            .userStatus(UserStatus.DEACTIVATED)
            .lastVisit(LocalDateTime.now())
            .dateOfRegistration(LocalDateTime.now())
            .build();
        when(googleIdTokenVerifier.verify("1234")).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);
        when(payload.getEmail()).thenReturn("test@mail.com");
        when(userService.findByEmail("test@mail.com")).thenReturn(user);
        assertThrows(UserDeactivatedException.class,
            () -> googleSecurityService.authenticate("1234"));
    }

    @Test
    void authenticationThrowsIllegalArgumentExceptionInCatchBlockTest() throws GeneralSecurityException, IOException {
        when(googleIdTokenVerifier.verify("1234")).thenThrow(GeneralSecurityException.class);
        assertThrows(IllegalArgumentException.class,
            () -> googleSecurityService.authenticate("1234"));
    }
}
