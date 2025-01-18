package greencity.config;

import greencity.constant.ErrorMessage;
import greencity.exception.exceptions.BadRequestException;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import static greencity.constant.PageableConstants.DEFAULT_PAGE;
import static greencity.constant.PageableConstants.DEFAULT_PAGE_SIZE;
import static greencity.constant.PageableConstants.MAX_PAGE_SIZE;
import static greencity.constant.PageableConstants.PAGE;
import static greencity.constant.PageableConstants.SIZE;

public class CustomPageableHandlerMethodArgumentResolver extends PageableHandlerMethodArgumentResolver {
    @Override
    public Pageable resolveArgument(MethodParameter methodParameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory) {
        int page = parseParameter(webRequest.getParameter(PAGE), DEFAULT_PAGE, PAGE);
        int size = parseParameter(webRequest.getParameter(SIZE), DEFAULT_PAGE_SIZE, SIZE);

        if (size > MAX_PAGE_SIZE) {
            throw new BadRequestException(ErrorMessage.MAX_PAGE_SIZE_EXCEPTION);
        }
        return PageRequest.of(page, size);
    }

    private int parseParameter(String paramValue, int defaultValue, String param) {
        if (paramValue == null) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(paramValue);

            if (value < 0) {
                String errorMessage = param.equals(PAGE)
                    ? ErrorMessage.NEGATIVE_PAGE_VALUE_EXCEPTION
                    : ErrorMessage.NEGATIVE_SIZE_VALUE_EXCEPTION;
                throw new IllegalArgumentException(errorMessage);
            }
            return value;
        } catch (NumberFormatException e) {
            String errorMessage = param.equals(PAGE)
                ? ErrorMessage.INVALID_PAGE_VALUE_EXCEPTION
                : ErrorMessage.INVALID_SIZE_VALUE_EXCEPTION;
            throw new IllegalArgumentException(errorMessage, e);
        }
    }
}