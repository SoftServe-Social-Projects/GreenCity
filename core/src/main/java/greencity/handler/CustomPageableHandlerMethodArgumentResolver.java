package greencity.handler;

import greencity.dto.Sortable;
import greencity.validator.SortPageableValidator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import greencity.annotations.ApiPageable;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CustomPageableHandlerMethodArgumentResolver extends PageableHandlerMethodArgumentResolver {
    private final SortPageableValidator sortPageableValidator;

    public CustomPageableHandlerMethodArgumentResolver(SortPageableValidator sortPageableValidator) {
        this.sortPageableValidator = sortPageableValidator;
    }

    @Override
    @Nonnull
    public Pageable resolveArgument(@Nonnull MethodParameter parameter,
        @Nullable ModelAndViewContainer mavContainer,
        @Nonnull NativeWebRequest webRequest,
        @Nullable WebDataBinderFactory binderFactory) {
        Pageable pageable = super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        ApiPageable apiPageable = parameter.getMethodAnnotation(ApiPageable.class);
        if (apiPageable != null) {
            Class<? extends Sortable> dtoClass = apiPageable.dtoClass();
            sortPageableValidator.validateSortParameters(pageable, dtoClass);
        }
        return pageable;
    }
}
