package greencity.converters;

import greencity.annotations.CurrentUser;
import greencity.client.RestClient;
import greencity.dto.user.UserVO;
import java.security.Principal;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@AllArgsConstructor
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    RestClient restClient;
    ModelMapper modelMapper;

    /**
     * Method checks if parameter is {@link UserVO} and is annotated with
     * {@link CurrentUser}.
     *
     * @param parameter method parameter
     * @return boolean
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(CurrentUser.class) != null
            && parameter.getParameterType().equals(UserVO.class);
    }

    /**
     * Method returns {@link UserVO} by principal.
     *
     * @return {@link UserVO}
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Principal principal = webRequest.getUserPrincipal();
        return principal != null ? restClient.findByEmail(principal.getName()) : null;
    }
}
