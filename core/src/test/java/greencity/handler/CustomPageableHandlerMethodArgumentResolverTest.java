package greencity.handler;

import greencity.annotations.ApiPageable;
import greencity.dto.friends.UserFriendDto;
import greencity.validator.SortPageableValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.powermock.api.mockito.PowerMockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomPageableHandlerMethodArgumentResolverTest {
    @Mock
    private SortPageableValidator sortPageableValidator;

    @Mock
    private PageableHandlerMethodArgumentResolver resolver;

    @InjectMocks
    private CustomPageableHandlerMethodArgumentResolver customResolver;

    @Mock
    private MethodParameter parameter;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private ModelAndViewContainer mavContainer;

    @Mock
    private WebDataBinderFactory binderFactory;

    @Mock
    private Pageable pageable;

    @Test
    void resolveArgumentWithoutApiPageableDoesNotCallValidatorTest() {
        when(resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)).thenReturn(pageable);
        when(parameter.getMethodAnnotation(ApiPageable.class)).thenReturn(null);

        customResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        verifyNoInteractions(sortPageableValidator);
    }

    @Test
    void resolveArgumentWithApiPageableCallsValidator() {
        ApiPageable apiPageable = mock(ApiPageable.class);
        when(parameter.getMethodAnnotation(ApiPageable.class)).thenReturn(apiPageable);
        when(apiPageable.dtoClass()).thenAnswer(invocation -> UserFriendDto.class);
        when(resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)).thenReturn(pageable);

        customResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        verify(sortPageableValidator).validateSortParameters(pageable, UserFriendDto.class);
    }

}
