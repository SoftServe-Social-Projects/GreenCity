package greencity.config;

import greencity.converters.UserArgumentResolver;
import greencity.service.UserService;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final UserService userService;
    private final ModelMapper modelMapper;

    /**
     * Method for configuring message source.
     *
     * @return {@link MessageSource}
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /**
     * Method for getting LocalValidatorFactoryBean.
     *
     * @return {@link LocalValidatorFactoryBean}
     */
    @Bean
    @Override
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource());
        return bean;
    }

    /**
     * Method for determining which locale is going to be used.
     *
     * @return {@link SessionLocaleResolver}
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        return localeResolver;
    }

    /**
     * Method for switching to a new locale based on the value of the lang parameter
     * appended to a request.
     *
     * @return {@link LocaleChangeInterceptor}
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    /**
     * Method that returns MultipartResolver as CommonsMultipartyResolver has been
     * superseded by StandardServletMultipartResolver after migration to SpringBoot
     * 3.1.5.
     *
     * @return {@link MultipartResolver}
     */
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.removeIf(resolver -> resolver instanceof PageableHandlerMethodArgumentResolver);
        resolvers.add(new UserArgumentResolver(userService, modelMapper));
        resolvers.add(new CustomPageableHandlerMethodArgumentResolver());
    }
}
