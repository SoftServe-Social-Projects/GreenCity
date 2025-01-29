package greencity.handler;

import greencity.constant.ErrorMessage;
import greencity.exception.exceptions.BadRequestException;
import javax.annotation.Nonnull;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final CustomSortHandlerMethodArgumentResolver customSortResolver;

    public CustomPageableHandlerMethodArgumentResolver(CustomSortHandlerMethodArgumentResolver customSortResolver) {
        this.customSortResolver = customSortResolver;
    }

    @Override
    @Nonnull
    public Pageable resolveArgument(@Nonnull MethodParameter methodParameter,
        ModelAndViewContainer mavContainer,
        @Nonnull NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory) {
        int page = parseParameter(webRequest, PAGE, DEFAULT_PAGE);
        int size = parseParameter(webRequest, SIZE, DEFAULT_PAGE_SIZE);

        if (size > MAX_PAGE_SIZE) {
            throw new BadRequestException(ErrorMessage.MAX_PAGE_SIZE_EXCEPTION);
        }

        Sort sort = customSortResolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

        return PageRequest.of(page, size, sort);
    }

    private int parseParameter(NativeWebRequest webRequest, String param, int defaultValue) {
        String paramValue = webRequest.getParameter(param);
        if (paramValue == null) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(paramValue);

            if (value < 0) {
                throw new IllegalArgumentException(String.format(ErrorMessage.NEGATIVE_VALUE_EXCEPTION, param));
            }

            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(ErrorMessage.INVALID_VALUE_EXCEPTION, param), e);
        }
    }
}