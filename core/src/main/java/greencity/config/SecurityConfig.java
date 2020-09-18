package greencity.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import greencity.security.filters.AccessTokenAuthenticationFilter;
import greencity.security.jwt.JwtTool;
import greencity.security.providers.JwtAuthenticationProvider;
import java.util.Arrays;
import java.util.Collections;

import greencity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static greencity.constant.AppConstant.*;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

/**
 * Config for security.
 *
 * @author Nazar Stasyuk && Yurii Koval
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final JwtTool jwtTool;
    private final UserService userService;

    /**
     * Constructor.
     */
    @Autowired
    public SecurityConfig(JwtTool jwtTool, UserService userService) {
        this.jwtTool = jwtTool;
        this.userService = userService;
    }

    /**
     * Bean {@link PasswordEncoder} that uses in coding password.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Method for configure security.
     *
     * @param http {@link HttpSecurity}
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors()
                .and()
                .csrf()
                .disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(
                        new AccessTokenAuthenticationFilter(jwtTool, authenticationManager(), userService),
                        UsernamePasswordAuthenticationFilter.class
                )
                .exceptionHandling()
                .authenticationEntryPoint((req, resp, exc) -> resp.sendError(SC_UNAUTHORIZED, "Authorize first."))
                .accessDeniedHandler((req, resp, exc) -> resp.sendError(SC_FORBIDDEN, "You don't have authorities."))
                .and()
                .authorizeRequests()
                .antMatchers("/management/**", "/css/**", "/img/**").hasRole(ADMIN)
                .antMatchers(HttpMethod.GET,
                        "/ownSecurity/verifyEmail",
                        "/ownSecurity/updateAccessToken",
                        "/ownSecurity/restorePassword",
                        "/ownSecurity/changePassword",
                        "/googleSecurity",
                        "/facebookSecurity/generateFacebookAuthorizeURL",
                        "/facebookSecurity/facebook",
                        "/factoftheday/",
                        "/factoftheday/all",
                        "/factoftheday/find",
                        "/factoftheday/languages",
                        "/category",
                        "/place/info/{id}",
                        "/place/info/favorite/{placeId}",
                        "/favorite_place/favorite/{placeId}",
                        "/place/statuses",
                        "/habit/statistic/todayStatisticsForAllHabitItems",
                        "/place/about/{id}",
                        "/specification",
                        "/econews",
                        "/econews/newest",
                        "/econews/tags",
                        "/econews/tags/all",
                        "/econews/recommended",
                        "/econews/{id}",
                        "/econews/comments",
                        "/econews/comments/replies/{parentCommentId}",
                        "/econews/comments/count/comments/{ecoNewsId}",
                        "/econews/comments/count/replies/{parentCommentId}",
                        "/econews/comments/count/likes",
                        "/tipsandtricks/comments",
                        "/tipsandtricks/comments/count/comments",
                        "/tipsandtricks/comments/replies/{parentCommentId}",
                        "/tipsandtricks/comments/count/likes",
                        "/tipsandtricks/comments/count/replies",
                        "/tipsandtricks/{id}",
                        "/tipsandtricks",
                        "/tipsandtricks/tags",
                        "/tipsandtricks/tags/all",
                        "/search",
                        "/search/econews",
                        "/search/tipsandtricks",
                        "/habit/status/{habitId}",
                        "/user/emailNotifications",
                        "/user/activatedUsersAmount",
                        "/socket/**"
                ).permitAll()
                .antMatchers(HttpMethod.POST,
                        "/ownSecurity/signUp",
                        "/ownSecurity/signIn",
                        "/place/getListPlaceLocationByMapsBounds",
                        "/place/filter"
                ).permitAll()
                .antMatchers(HttpMethod.GET,
                        "/achievements",
                        "/advices/random/{habitId}",
                        "/advices",
                        "/favorite_place/",
                        "/goals",
                        "/goals/shoppingList/{userId}",
                        "/habit",
                        "/habit/statistic/{habitId}",
                        "/facts",
                        "/facts/random/{habitId}",
                        "/facts/dayFact/{languageId}",
                        "/newsSubscriber/unsubscribe",
                        "/place/{status}",
                        "/user",
                        "/user/{userId}/habits",
                        "/user/{userId}/habits/statistic",
                        "/user/{userId}/goals",
                        "/user/{userId}/customGoals",
                        "/user/{userId}/goals/available",
                        "/user/{userId}/customGoals/available",
                        "/user/{userId}/habit-dictionary/available",
                        "/user/{userId}/sixUserFriends/",
                        "/user/{userId}/profile/",
                        "/user/isOnline/{userId}/",
                        "/user/{userId}/profileStatistics/",
                        "/user/userAndSixFriendsWithOnlineStatus",
                        "/user/userAndAllFriendsWithOnlineStatus"
                ).hasAnyRole(USER, ADMIN, MODERATOR)
                .antMatchers(HttpMethod.POST,
                        "/category",
                        "/econews",
                        "/econews/comments/{econewsId}",
                        "/econews/comments/like",
                        "/files/image",
                        "/habit/assign/{habitId}",
                        "/habit/statistic/",
                        "/habit/status/enroll/{habitId}",
                        "/habit/status/unenroll/{habitId}/{date}",
                        "/habit/status/enroll/{habitId}/{date}",
                        "/newsSubscriber",
                        "/place/{placeId}/comments",
                        "/place/propose",
                        "/place/save/favorite/",
                        "/tipsandtricks/comments/{tipsAndTricksId}",
                        "/tipsandtricks/comments/like",
                        "/tipsandtricks",
                        "/user/{userId}/customGoals",
                        "/user/{userId}/goals",
                        "/user/{userId}/habit",
                        "/user/{userId}/userFriend/{friendId}",
                        "/user/profile"
                ).hasAnyRole(USER, ADMIN, MODERATOR)
                .antMatchers(HttpMethod.PUT,
                        "/favorite_place/",
                        "/ownSecurity"
                ).hasAnyRole(USER, ADMIN, MODERATOR)
                .antMatchers(HttpMethod.PATCH,
                        "/econews/comments",
                        "/goals/shoppingList/{userId}",
                        "/habit/statistic/{habitStatisticId}",
                        "/tipsandtricks/comments",
                        "/user/{userId}/customGoals",
                        "/user/{userId}/goals/{goalId}",
                        "/user/profilePicture"
                ).hasAnyRole(USER, ADMIN, MODERATOR)
                .antMatchers(HttpMethod.DELETE,
                        "/econews/comments",
                        "/favorite_place/{placeId}",
                        "/tipsandtricks/comments",
                        "/user/{userId}/customGoals",
                        "/user/{userId}/habit/{habitId}",
                        "/user/{userId}/userGoals",
                        "/user/{userId}/userFriend/{friendId}"
                ).hasAnyRole(USER, ADMIN, MODERATOR)
                .antMatchers(HttpMethod.GET,
                        "/econews/comments/replies/active/{parentCommentId}",
                        "/econews/comments/active",
                        "/newsSubscriber",
                        "/comments",
                        "/comments/{id}",
                        "/user/all",
                        "/user/roles"
                ).hasAnyRole(ADMIN, MODERATOR)
                .antMatchers(HttpMethod.POST,
                        "/advices",
                        "/facts",
                        "/place/filter/predicate",
                        "/user/filter"
                ).hasAnyRole(ADMIN, MODERATOR)
                .antMatchers(HttpMethod.PUT,
                        "/advices/{adviceId}",
                        "/facts/{factId}",
                        "/place/update/"
                ).hasAnyRole(ADMIN, MODERATOR)
                .antMatchers(HttpMethod.PATCH,
                        "/place/status",
                        "/place/statuses",
                        "/user",
                        "/user/status",
                        "/user/role"
                ).hasAnyRole(ADMIN, MODERATOR)
                .antMatchers(HttpMethod.DELETE,
                        "/advices/{adviceId}",
                        "/econews/{econewsId}",
                        "/facts/{factId}",
                        "/comments",
                        "/place/{id}",
                        "/place",
                        "/tipsandtricks/{id}"
                ).hasAnyRole(ADMIN, MODERATOR)
                .antMatchers(HttpMethod.PATCH,
                        "/user/update/role"
                ).hasRole(ADMIN)
                .anyRequest().hasAnyRole(ADMIN);
    }

    /**
     * Method for configure matchers that will be ignored in security.
     *
     * @param web {@link WebSecurity}
     */
    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/v2/api-docs/**");
        web.ignoring().antMatchers("/swagger.json");
        web.ignoring().antMatchers("/swagger-ui.html");
        web.ignoring().antMatchers("/swagger-resources/**");
        web.ignoring().antMatchers("/webjars/**");
    }


    /**
     * Method for configure type of authentication provider.
     *
     * @param auth {@link AuthenticationManagerBuilder}
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(new JwtAuthenticationProvider(jwtTool));
    }

    /**
     * Provides AuthenticationManager.
     *
     * @return {@link AuthenticationManager}
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    /**
     * Bean {@link CorsConfigurationSource} that uses for CORS setup.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(
                Arrays.asList("GET", "POST", "OPTIONS", "DELETE", "PUT", "PATCH"));
        configuration.setAllowedHeaders(
                Arrays.asList(
                        "X-Requested-With", "Origin", "Content-Type", "Accept", "Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Bean {@link GoogleIdTokenVerifier} that uses in verify googleIdToken.
     *
     * @param clientId {@link String} - google client id.
     */
    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier(@Value("${google.clientId}") String clientId) {
        return new GoogleIdTokenVerifier
                .Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }
}
