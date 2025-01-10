package greencity;

import greencity.entity.User;
import greencity.entity.VerifyEmail;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import java.time.LocalDateTime;

public class ModelUtils {

    public static User getUser() {
        return User.builder()
            .id(1L)
            .email("danylo@gmail.com")
            .name("Taras")
            .role(Role.ROLE_USER)
            .userStatus(UserStatus.ACTIVATED)
            .lastActivityTime(LocalDateTime.now())
            .verifyEmail(new VerifyEmail())
            .dateOfRegistration(LocalDateTime.now())
            .build();
    }
}
