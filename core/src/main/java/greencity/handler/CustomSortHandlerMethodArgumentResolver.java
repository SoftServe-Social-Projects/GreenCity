package greencity.handler;

import greencity.annotations.ApiPageable;
import greencity.dto.Sortable;
import greencity.validator.SortPageableValidator;
import org.springframework.lang.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Custom argument resolver for handling {@link Sort} parameters in API
 * requests. This class extends the functionality of
 * {@link SortHandlerMethodArgumentResolver} by adding validation for sort
 * parameters based on the {@link ApiPageable} annotation.
 *
 */
public class CustomSortHandlerMethodArgumentResolver implements SortArgumentResolver {
    private final SortPageableValidator sortPageableValidator;
    private final SortHandlerMethodArgumentResolver delegate;

    public CustomSortHandlerMethodArgumentResolver(SortPageableValidator sortPageableValidator,
        SortHandlerMethodArgumentResolver delegate) {
        this.sortPageableValidator = sortPageableValidator;
        this.delegate = delegate;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Sort.class.equals(parameter.getParameterType());
    }

    @NonNull
    @Override
    public Sort resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
        @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Sort sort = delegate.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        ApiPageable apiPageable = parameter.getMethodAnnotation(ApiPageable.class);

        if (sort.isSorted() && apiPageable != null) {
            Class<? extends Sortable> dtoClass = apiPageable.dtoClass();
            sortPageableValidator.validateSortParameters(sort, dtoClass);
        }
        return sort;
    }
}
