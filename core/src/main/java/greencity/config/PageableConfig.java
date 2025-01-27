package greencity.config;

import greencity.handler.CustomPageableHandlerMethodArgumentResolver;
import greencity.validator.SortPageableValidator;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PageableConfig implements WebMvcConfigurer {
    private final SortPageableValidator sortPageableValidator = new SortPageableValidator();
    private final PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();

    /**
     * Sets max page size for pageable objects and adds custom validation for sort
     * parameters.
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        CustomPageableHandlerMethodArgumentResolver customResolver =
            new CustomPageableHandlerMethodArgumentResolver(sortPageableValidator, resolver);
        customResolver.setMaxPageSize(100);
        argumentResolvers.add(customResolver);
        WebMvcConfigurer.super.addArgumentResolvers(argumentResolvers);
    }
}
