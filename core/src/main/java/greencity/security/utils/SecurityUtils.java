package greencity.security.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import static greencity.constant.SecurityConstants.ANONYMOUS;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityUtils {
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            return principal != null && !ANONYMOUS.equals(principal.toString());
        }
        return false;
    }
}
