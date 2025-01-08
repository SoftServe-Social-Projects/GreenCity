package greencity.aspects;

import greencity.annotations.CheckEmailPreference;
import greencity.dto.user.UserVO;
import greencity.entity.Notification;
import greencity.enums.EmailPreference;
import greencity.enums.EmailPreferencePeriodicity;
import greencity.message.EmailMessage;
import greencity.repository.UserNotificationPreferenceRepo;
import greencity.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class EmailPreferenceAspect {
    private final UserNotificationPreferenceRepo userNotificationPreferenceRepo;
    private final UserServiceImpl userServiceImpl;

    @Around("@annotation(checkEmailPreference)")
    public Object checkEmailPreference(ProceedingJoinPoint proceedingJoinPoint,
        CheckEmailPreference checkEmailPreference) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        EmailPreference emailPreference = checkEmailPreference.value();
        String email = extractEmailFromArgs(args);
        UserVO user = userServiceImpl.findByEmail(email);

        boolean hasPreference =
            userNotificationPreferenceRepo.existsByUserIdAndEmailPreferenceAndPeriodicity(user.getId(), emailPreference,
                EmailPreferencePeriodicity.IMMEDIATELY);

        if (hasPreference) {
            return proceedingJoinPoint.proceed();
        } else {
            return null;
        }
    }

    public static String extractEmail(Object message) {
        if (message instanceof EmailMessage) {
            return ((EmailMessage) message).getEmail();
        } else if (message instanceof Notification) {
            return ((Notification) message).getTargetUser().getEmail();
        }
        return null;
    }

    private String extractEmailFromArgs(Object[] args) {
        for (Object arg : args) {
            String email = extractEmail(arg);
            if (email != null) {
                return email;
            }
        }
        return null;
    }
}
