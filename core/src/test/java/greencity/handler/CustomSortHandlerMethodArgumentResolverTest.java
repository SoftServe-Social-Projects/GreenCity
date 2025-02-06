package greencity.handler;

import greencity.annotations.ApiPageable;
import greencity.dto.Sortable;
import greencity.validator.SortPageableValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomSortHandlerMethodArgumentResolverTest {
    private CustomSortHandlerMethodArgumentResolver resolver;

    @Mock
    private SortHandlerMethodArgumentResolver delegate;

    @Mock
    private SortPageableValidator validator;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private MethodParameter methodParameter;

    @BeforeEach
    void setUp() {
        resolver = new CustomSortHandlerMethodArgumentResolver(validator, delegate);
    }

    @Test
    void supportsParameterShouldReturnTrueForSortTypeTest() {
        when(methodParameter.getParameterType()).thenAnswer(invocation -> Sort.class);
        boolean result = resolver.supportsParameter(methodParameter);
        assertTrue(result);
    }

    @Test
    void supportsParameterShouldReturnFalseForNonSortTypeTest() {
        when(methodParameter.getParameterType()).thenAnswer(invocation ->String.class);
        boolean result = resolver.supportsParameter(methodParameter);
        assertFalse(result);
    }

    @Test
    void supportsParameterShouldReturnFalseForNullParameterTypeTest() {
        when(methodParameter.getParameterType()).thenReturn(null);
        boolean result = resolver.supportsParameter(methodParameter);
        assertFalse(result);
    }

    @Test
    void resolveArgumentShouldReturnSortFromDelegateTest() {
        Sort expectedSort = Sort.by("name").ascending();
        when(delegate.resolveArgument(eq(methodParameter), any(), eq(webRequest), any())).thenReturn(expectedSort);
        when(methodParameter.getMethodAnnotation(ApiPageable.class)).thenReturn(null);

        Sort result = resolver.resolveArgument(methodParameter, null, webRequest, null);

        assertEquals(expectedSort, result);
        verify(delegate).resolveArgument(eq(methodParameter), any(), eq(webRequest), any());
        verifyNoInteractions(validator);
    }

    @Test
    void resolveArgumentShouldValidateSortParametersWhenApiPageablePresentTest() {
        Sort expectedSort = Sort.by("name").ascending();
        ApiPageable apiPageable = mock(ApiPageable.class);
        Class<? extends Sortable> dtoClass = Sortable.class;

        when(delegate.resolveArgument(eq(methodParameter), any(), eq(webRequest), any())).thenReturn(expectedSort);
        when(methodParameter.getMethodAnnotation(ApiPageable.class)).thenReturn(apiPageable);
        when(apiPageable.dtoClass()).thenAnswer(invocation -> dtoClass);

        Sort result = resolver.resolveArgument(methodParameter, null, webRequest, null);

        assertEquals(expectedSort, result);
        verify(validator).validateSortParameters(expectedSort, dtoClass);
        verify(delegate).resolveArgument(eq(methodParameter), any(), eq(webRequest), any());
    }

    @Test
    void resolveArgumentShouldSkipValidationWhenSortIsUnsortedTest() {
        Sort unsorted = Sort.unsorted();
        when(delegate.resolveArgument(eq(methodParameter), any(), eq(webRequest), any())).thenReturn(unsorted);
        when(methodParameter.getMethodAnnotation(ApiPageable.class)).thenReturn(mock(ApiPageable.class));

        Sort result = resolver.resolveArgument(methodParameter, null, webRequest, null);

        assertEquals(unsorted, result);
        verify(delegate).resolveArgument(eq(methodParameter), any(), eq(webRequest), any());
        verifyNoInteractions(validator);
    }
}
