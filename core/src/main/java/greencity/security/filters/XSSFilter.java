package greencity.security.filters;

import greencity.security.wrappers.MultipartXSSWrapper;
import greencity.security.wrappers.XSSWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Class that provides XSS filtering for request body.
 *
 * @author Dmytro Dmytruk.
 */
@RequiredArgsConstructor
public class XSSFilter extends OncePerRequestFilter {
    @Override
    public void doFilterInternal(HttpServletRequest servletRequest,
        @SuppressWarnings("NullableProblems") HttpServletResponse servletResponse,
        @SuppressWarnings("NullableProblems") FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest.getContentType() == null) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else if (servletRequest.getContentType().contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            filterChain.doFilter(new MultipartXSSWrapper(servletRequest), servletResponse);
        } else if (servletRequest.getContentType().contains(MediaType.APPLICATION_JSON_VALUE)) {
            filterChain.doFilter(new XSSWrapper(servletRequest), servletResponse);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}
