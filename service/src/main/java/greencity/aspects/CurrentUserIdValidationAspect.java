package greencity.aspects;

import greencity.annotations.CurrentUserId;
import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
import greencity.exception.exceptions.NotCurrentUserException;
import java.lang.annotation.Annotation;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * This aspect is used for method-level validation of current user id parameters
 * in controller classes.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class CurrentUserIdValidationAspect {
    private final RestClient restClient;

    /**
     * Validates method parameter which is meant to represent currently
     * authenticated user id. Pointcut matches every public method that is located
     * in the controller classes under greencity.controller package and has at least
     * one {@link Long}-typed parameter at an arbitrary position.
     *
     * @param joinPoint {@link JoinPoint} that is used for annotated parameter
     *                  observation.
     * @throws NoSuchMethodException    if the method pointed by joinPoint cannot be
     *                                  found.
     * @throws IllegalArgumentException if the supplied user id does not match real
     *                                  authenticated users id.
     */
    @Before("execution(public * greencity.controller..*.*(..,java.lang.Long,..))")
    public void validateCurrentUserIdParameter(JoinPoint joinPoint) throws NoSuchMethodException {
        getAnnotatedArgument(joinPoint).ifPresent(userId -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long currentUserId = restClient.findByEmail(authentication.getName()).getId();
            if (!currentUserId.equals(userId)) {
                throw new NotCurrentUserException(ErrorMessage.NOT_A_CURRENT_USER);
            }
        });
    }

    /**
     * Returns method parameter of type {@link Long} that is annotated with
     * {@link CurrentUserId}. The position of such a parameter can be arbitrary.
     *
     * @param joinPoint is used for annotated parameter observation.
     * @return {@link Optional} containing supplied user id or empty Optional if the
     *         parameter cannot be found.
     * @throws NoSuchMethodException if the method pointed by joinPoint cannot be
     *                               found.
     */
    private Optional<Long> getAnnotatedArgument(JoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        Class<?>[] parameterTypes = signature.getMethod().getParameterTypes();
        Annotation[][] annotations = joinPoint.getTarget().getClass()
            .getMethod(methodName, parameterTypes).getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation[] parameterAnnotations = annotations[i];
            for (Annotation parameterAnnotation : parameterAnnotations) {
                if (parameterAnnotation.annotationType().equals(CurrentUserId.class)
                    && parameterTypes[i].equals(Long.class)) {
                    return Optional.of((Long) joinPoint.getArgs()[i]);
                }
            }
        }
        return Optional.empty();
    }
}
