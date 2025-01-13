package greencity.security.filters;

import greencity.security.wrappers.MultipartXSSWrapper;
import greencity.security.wrappers.XSSWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class XSSFilterTest {
    @InjectMocks
    private XSSFilter xssFilter;

    @Mock
    private FilterChain filterChain;

    @Test
    void testDoFilterInternalNullContentType() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain spyFilterChain = spy(new MockFilterChain());
        xssFilter.doFilterInternal(request, response, spyFilterChain);
        verify(spyFilterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalMultipartFormData() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("multipart/form-data");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain spyFilterChain = spy(new MockFilterChain());
        xssFilter.doFilterInternal(request, response, spyFilterChain);
        verify(spyFilterChain, times(1)).doFilter(any(MultipartXSSWrapper.class), any(HttpServletResponse.class));
    }

    @Test
    void testDoFilterInternalJson() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain spyFilterChain = spy(new MockFilterChain());
        xssFilter.doFilterInternal(request, response, spyFilterChain);
        verify(spyFilterChain, times(1)).doFilter(any(XSSWrapper.class), any(HttpServletResponse.class));
    }
}
