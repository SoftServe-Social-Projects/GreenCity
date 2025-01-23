package greencity.config;

import greencity.handler.CustomPageableHandlerMethodArgumentResolver;
import greencity.validator.SortPageableValidator;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PageableConfig implements WebMvcConfigurer {
    private final SortPageableValidator sortPageableValidator = new SortPageableValidator();

    /**
     * Sets max page size for pageable objects and adds custom validation for sort
     * parameters.
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        CustomPageableHandlerMethodArgumentResolver resolver =
            new CustomPageableHandlerMethodArgumentResolver(sortPageableValidator);
        resolver.setMaxPageSize(100);
        argumentResolvers.add(resolver);
        WebMvcConfigurer.super.addArgumentResolvers(argumentResolvers);
    }
}
