package greencity.security.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import static greencity.constant.AppConstant.GOOGLE_USER_NAME;
import static greencity.constant.ErrorMessage.*;
import greencity.entity.User;
import greencity.entity.enums.EmailNotification;
import greencity.entity.enums.ROLE;
import greencity.entity.enums.UserStatus;
import greencity.exception.exceptions.UserDeactivatedException;
import greencity.security.dto.SuccessSignInDto;
import greencity.security.events.SignInEvent;
import greencity.security.jwt.JwtTool;
import greencity.security.service.GoogleSecurityService;
import greencity.service.UserService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@inheritDoc}
 */
@Slf4j
@Service
public class GoogleSecurityServiceImpl implements GoogleSecurityService {
    private final UserService userService;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final JwtTool jwtTool;
    private final ApplicationEventPublisher appEventPublisher;


    /**
     * Constructor.
     *
     * @param userService           {@link UserService} - service of {@link User} logic.
     * @param jwtTool               {@link JwtTool} - tool for jwt logic.
     * @param googleIdTokenVerifier {@link GoogleIdTokenVerifier} - tool for verify googleIdToken.
     * @param appEventPublisher     {@link ApplicationEventPublisher} - tool for eventPublisher logic.
     */
    @Autowired
    public GoogleSecurityServiceImpl(UserService userService,
                                     JwtTool jwtTool,
                                     GoogleIdTokenVerifier googleIdTokenVerifier,
                                     ApplicationEventPublisher appEventPublisher
    ) {
        this.userService = userService;
        this.jwtTool = jwtTool;
        this.googleIdTokenVerifier = googleIdTokenVerifier;
        this.appEventPublisher = appEventPublisher;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public SuccessSignInDto authenticate(String idToken) {
        try {
            GoogleIdToken googleIdToken = googleIdTokenVerifier.verify(idToken);
            if (googleIdToken != null) {
                GoogleIdToken.Payload payload = googleIdToken.getPayload();
                String email = payload.getEmail();
                String userName = (String) payload.get(GOOGLE_USER_NAME);
                User byEmail;
                byEmail = userService.findByEmail(email);
                User user = byEmail;
                if (user == null) {
                    log.info(USER_NOT_FOUND_BY_EMAIL + email);
                    user = createNewUser(email, userName);
                    User savedUser = userService.save(user);
                    appEventPublisher.publishEvent(new SignInEvent(savedUser));
                    log.info("Google sign-up and sign-in user - {}", user.getEmail());
                    return getSuccessSignInDto(user);
                } else {
                    if (user.getUserStatus() == UserStatus.DEACTIVATED) {
                        throw new UserDeactivatedException(USER_DEACTIVATED);
                    }
                    log.info("Google sign-in exist user - {}", user.getEmail());
                    return getSuccessSignInDto(user);
                }
            }
            throw new IllegalArgumentException(BAD_GOOGLE_TOKEN);
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalArgumentException(BAD_GOOGLE_TOKEN + ". " + e.getMessage());
        }
    }

    private User createNewUser(String email, String userName) {
        return User.builder()
            .email(email)
            .name(userName)
            .role(ROLE.ROLE_USER)
            .dateOfRegistration(LocalDateTime.now())
            .lastVisit(LocalDateTime.now())
            .userStatus(UserStatus.ACTIVATED)
            .emailNotification(EmailNotification.DISABLED)
            .refreshTokenKey(jwtTool.generateTokenKey())
            .build();
    }

    private SuccessSignInDto getSuccessSignInDto(User user) {
        String accessToken = jwtTool.createAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtTool.createRefreshToken(user);
        return new SuccessSignInDto(user.getId(), accessToken, refreshToken, user.getName(), false);
    }
}
