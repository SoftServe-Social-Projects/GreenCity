package greencity.config;

import greencity.security.filters.AccessTokenAuthenticationFilter;
import greencity.security.filters.XSSFilter;
import greencity.security.jwt.JwtTool;
import greencity.security.providers.JwtAuthenticationProvider;
import greencity.service.UserService;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import static greencity.constant.AppConstant.ADMIN;
import static greencity.constant.AppConstant.USER;
import static greencity.constant.AppConstant.MODERATOR;
import static greencity.constant.AppConstant.UBS_EMPLOYEE;
import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Config for security.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalAuthentication
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String COMMENTS = "/comments";
    private static final String CATEGORIES = "/categories";
    private static final String ECO_NEWS = "/eco-news";
    private static final String ECO_NEWS_ID = "/{ecoNewsId}";
    private static final String ECO_NEWS_ID_COMMENTS = ECO_NEWS + ECO_NEWS_ID + COMMENTS;
    private static final String ECO_NEWS_COMMENTS = ECO_NEWS + COMMENTS;
    private static final String REPLIES = "/replies";
    private static final String LIKE = "/like";
    private static final String DISLIKE = "/dislike";
    private static final String LIKES = "/likes";
    private static final String DISLIKES = "/dislikes";
    private static final String COUNT = "/count";
    private static final String COMMENT_ID = "/{commentId}";
    private static final String PARENT_COMMENT_ID = "/{parentCommentId}";
    private static final String EVENTS = "/events";
    private static final String EVENT_ID = "/{eventId}";
    private static final String FAVORITES = "/favorites";
    private static final String SEARCH = "/search";
    private static final String PLACES = "/places";
    private static final String ATTENDERS = "/attenders";
    private static final String ORGANIZERS = "/organizers";
    private static final String RATINGS = "/ratings";
    private static final String EVENTS_ID_COMMENTS = EVENTS + EVENT_ID + COMMENTS;
    private static final String EVENTS_COMMENTS = EVENTS + COMMENTS;
    private static final String FRIENDS = "/friends";
    private static final String HABITS = "/habits";
    private static final String FACT_OF_THE_DAY = "/fact-of-the-day";
    private static final String RANDOM = "/random";
    private static final String SUBSCRIPTIONS = "/subscriptions";
    private static final String ACTIVE = "/active";
    private static final String USER_CUSTOM_TO_DO_LIST_ITEMS = "/user/{userId}/custom-to-do-list-items";
    private static final String CUSTOM_TO_DO_LIST = "/custom/to-do-list-items/{userId}";
    private static final String CUSTOM_TO_DO_LIST_URL = CUSTOM_TO_DO_LIST + "/custom-to-do-list-items";
    private static final String CUSTOM_TO_DO_LIST_ITEMS = "/{userId}/custom-to-do-list-items";
    private static final String HABIT_ASSIGN_ID = "/habit/assign/{habitId}";
    private static final String USER_TO_DO_LIST = "/user/to-do-list-items";
    private static final String ACHIEVEMENTS = "/achievements";
    private static final String NOTIFICATIONS = "/notifications";
    private static final String NOTIFICATION_ID = "/{notificationId}";
    private static final String HABIT_INVITE = "/habit/invite";
    private static final String INVITATION_ID = "/{invitationId}";
    private static final String COMMIT_INFO = "/commit-info";
    private final JwtTool jwtTool;
    private final UserService userService;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Value("${spring.messaging.stomp.websocket.allowed-origins}")
    private String[] allowedOrigins;

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
    @SuppressWarnings("java:S4502")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(corsCustomizer -> corsCustomizer.configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOriginPatterns(List.of(allowedOrigins));
            config.setAllowedMethods(
                Arrays.asList("GET", "POST", "OPTIONS", "DELETE", "PUT", "PATCH"));
            config.setAllowedHeaders(
                Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Headers",
                    "X-Requested-With", "Origin", "Content-Type", "Accept", "Authorization"));
            config.setAllowCredentials(true);
            config.setMaxAge(3600L);
            return config;
        })).csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
            .addFilterBefore(new AccessTokenAuthenticationFilter(jwtTool, authenticationManager(), userService),
                UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new XSSFilter(),
                UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exception -> exception.authenticationEntryPoint((req, resp, exc) -> resp
                .sendError(SC_UNAUTHORIZED, "Authorize first."))
                .accessDeniedHandler((req, resp, exc) -> resp.sendError(SC_FORBIDDEN, "You don't have authorities.")))
            .authorizeHttpRequests(req -> req
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/", "/management/", "/management/login").permitAll()
                .requestMatchers("/management/**").hasAnyRole(ADMIN)
                .requestMatchers("/v2/api-docs/**",
                    "/v3/api-docs/**",
                    "/swagger.json",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/swagger-resources/**",
                    "/webjars/**")
                .permitAll()
                .requestMatchers("/css/**", "/img/**").permitAll()
                .requestMatchers(HttpMethod.GET,
                    FACT_OF_THE_DAY + RANDOM,
                    CATEGORIES,
                    "/place/info/{id}",
                    "/place/info/favorite/{placeId}",
                    "/favorite_place/favorite/{placeId}",
                    "/place/statuses",
                    "/place/all",
                    "/habit",
                    "/habit/{id}",
                    "/habit/{id}/to-do-list",
                    "/tags/search",
                    "/tags/v2/search",
                    "/habit/tags/all",
                    "/habit/statistic/{habitId}",
                    "/habit/statistic/assign/{habitAssignId}",
                    "/habit/statistic/todayStatisticsForAllHabitItems",
                    HABITS + "/comments/{id}",
                    HABITS + "/comments/{parentCommentId}/replies/active",
                    HABITS + "/{habitId}/comments/count",
                    HABITS + "/comments/{parentCommentId}/replies/active/count",
                    HABITS + "/comments/active",
                    HABITS + "/comments/{commentId}/likes/count",
                    "/place/about/{id}",
                    "/specification",
                    ECO_NEWS,
                    ECO_NEWS + "/tags",
                    ECO_NEWS + ECO_NEWS_ID + "/recommended",
                    ECO_NEWS + ECO_NEWS_ID,
                    ECO_NEWS + ECO_NEWS_ID + LIKES + COUNT,
                    ECO_NEWS + ECO_NEWS_ID + DISLIKES + COUNT,
                    ECO_NEWS_ID_COMMENTS,
                    ECO_NEWS_ID_COMMENTS + ACTIVE,
                    ECO_NEWS_COMMENTS + PARENT_COMMENT_ID + REPLIES + ACTIVE,
                    ECO_NEWS_COMMENTS + PARENT_COMMENT_ID + REPLIES + ACTIVE + COUNT,
                    ECO_NEWS + COMMENTS,
                    ECO_NEWS + COMMENTS + "/{id}",
                    ECO_NEWS + COMMENTS + LIKE,
                    ECO_NEWS + COMMENTS + COMMENT_ID + LIKES + COUNT,
                    ECO_NEWS + COMMENTS + ACTIVE,
                    ECO_NEWS_ID_COMMENTS + COUNT,
                    EVENTS_ID_COMMENTS,
                    EVENTS_ID_COMMENTS + COMMENT_ID,
                    EVENTS_COMMENTS + COMMENT_ID,
                    EVENTS_ID_COMMENTS + COMMENT_ID + COUNT,
                    EVENTS_COMMENTS + PARENT_COMMENT_ID + REPLIES + ACTIVE,
                    EVENTS_COMMENTS + PARENT_COMMENT_ID + REPLIES + COUNT,
                    EVENTS_COMMENTS + LIKE,
                    EVENTS_COMMENTS + COMMENT_ID + LIKES + COUNT,
                    EVENTS,
                    EVENTS + "/addresses",
                    EVENTS + EVENT_ID,
                    EVENTS + EVENT_ID + ATTENDERS,
                    "/languages/codes",
                    SEARCH + ECO_NEWS,
                    SEARCH + EVENTS,
                    SEARCH + PLACES,
                    "/user/emailNotifications",
                    "/user/activatedUsersAmount",
                    "/user/{userId}/habit/assign",
                    "/token",
                    "/socket/**",
                    FRIENDS + "/user/{userId}",
                    "/habit/assign/confirm/{habitAssignId}",
                    "/database/backup",
                    "/database/backupFiles",
                    COMMIT_INFO)
                .permitAll()
                .requestMatchers(HttpMethod.DELETE,
                    "/place/{id}",
                    "/place")
                .permitAll()
                .requestMatchers(HttpMethod.POST,
                    SUBSCRIPTIONS,
                    "/place/getListPlaceLocationByMapsBounds",
                    "/place/filter")
                .permitAll()
                .requestMatchers(HttpMethod.DELETE,
                    SUBSCRIPTIONS + "/{unsubscribeToken}")
                .permitAll()
                .requestMatchers(HttpMethod.GET,
                    ACHIEVEMENTS,
                    ACHIEVEMENTS + COUNT,
                    ACHIEVEMENTS + CATEGORIES,
                    CUSTOM_TO_DO_LIST_ITEMS,
                    CUSTOM_TO_DO_LIST,
                    CUSTOM_TO_DO_LIST_URL,
                    "/custom/to-do-list-items/{userId}/{habitId}",
                    ECO_NEWS + COUNT,
                    ECO_NEWS + ECO_NEWS_ID + "/summary",
                    ECO_NEWS + ECO_NEWS_ID + LIKES + "/{userId}",
                    "/favorite_place/",
                    "/to-do-list-items",
                    "/habit/assign/allForCurrentUser",
                    "/habit/assign/allMutualHabits/{userId}",
                    "/habit/assign/allUser/{userId}",
                    "/habit/assign/myHabits/{userId}",
                    "/habit/assign/active/{date}",
                    "/habit/assign/{habitAssignId}/more",
                    "/habit/assign/activity/{from}/to/{to}",
                    HABIT_ASSIGN_ID + ACTIVE,
                    HABIT_ASSIGN_ID,
                    HABIT_ASSIGN_ID + "/all",
                    "/habit/statistic/acquired/count",
                    "/habit/statistic/in-progress/count",
                    FACT_OF_THE_DAY + RANDOM + "/by-tags",
                    "/place/{status}",
                    "/place/v2/filteredPlacesCategories",
                    "/social-networks/image",
                    "/user",
                    "/user/to-do-list-items/habits/{habitId}/to-do-list",
                    USER_CUSTOM_TO_DO_LIST_ITEMS,
                    "/user/{userId}/custom-to-do-list-items/available",
                    "/user/{userId}/sixUserFriends/",
                    "/user/{userId}/profile/",
                    "/user/isOnline/{userId}/",
                    "/user/{userId}/profileStatistics/",
                    "/user/userAndSixFriendsWithOnlineStatus",
                    "/user/userAndAllFriendsWithOnlineStatus",
                    "/user/{userId}/recommendedFriends/",
                    "/user/{userId}/friends/",
                    "/user/{userId}/friendRequests/",
                    "/chat",
                    EVENTS + ATTENDERS + COUNT,
                    EVENTS + ORGANIZERS + COUNT,
                    EVENTS + EVENT_ID + LIKES,
                    EVENTS + EVENT_ID + LIKES + COUNT,
                    EVENTS + EVENT_ID + DISLIKES + COUNT,
                    EVENTS + EVENT_ID + "/requested-users",
                    "/user/to-do-list-items/{userId}/get-all-inprogress",
                    "/habit/assign/{habitAssignId}/allUserAndCustomList",
                    "/habit/assign/allUserAndCustomToDoListsInprogress",
                    "/habit/assign/{habitAssignId}",
                    "/habit/tags/search",
                    "/habit/search",
                    "/habit/my",
                    "/habit/all/{friendId}",
                    "/habit/allMutualHabits/{friendId}",
                    "/habit/{habitId}/friends/profile-pictures",
                    "habit/favorites",
                    FRIENDS + "/not-friends-yet",
                    FRIENDS + "/recommended-friends",
                    FRIENDS + "/mutual-friends",
                    FRIENDS + "/friendRequests",
                    FRIENDS + "/{userId}/all-user-friends",
                    FRIENDS + "/user-data-as-friend/{friendId}",
                    FRIENDS,
                    NOTIFICATIONS,
                    HABIT_ASSIGN_ID + "/friends/habit-duration-info",
                    "/ai/**")
                .hasAnyRole(USER, ADMIN, MODERATOR, UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.POST,
                    CATEGORIES,
                    ECO_NEWS,
                    ECO_NEWS + ECO_NEWS_ID + LIKES,
                    ECO_NEWS + ECO_NEWS_ID + DISLIKES,
                    EVENTS + EVENT_ID + DISLIKES,
                    ECO_NEWS + COMMENTS + LIKE,
                    ECO_NEWS + COMMENTS + DISLIKE,
                    HABITS + COMMENTS + DISLIKE,
                    EVENTS + COMMENTS + DISLIKE + COMMENT_ID,
                    ECO_NEWS_ID_COMMENTS,
                    ECO_NEWS_ID_COMMENTS + COMMENT_ID + LIKES,
                    EVENTS_ID_COMMENTS,
                    EVENTS_COMMENTS + LIKE + COMMENT_ID,
                    EVENTS,
                    EVENTS + EVENT_ID + ATTENDERS,
                    "/events/{eventId}/requested-users/{userId}/decline",
                    "/events/{eventId}/requested-users/{userId}/approve",
                    EVENTS + EVENT_ID + "/addToRequested",
                    EVENTS + EVENT_ID + FAVORITES,
                    EVENTS + EVENT_ID + RATINGS,
                    EVENTS + EVENT_ID + LIKE,
                    EVENTS + EVENT_ID + DISLIKE,
                    NOTIFICATIONS + NOTIFICATION_ID + "/viewNotification",
                    NOTIFICATIONS + NOTIFICATION_ID + "/unreadNotification",
                    CUSTOM_TO_DO_LIST_ITEMS,
                    "/files",
                    HABIT_ASSIGN_ID,
                    HABIT_ASSIGN_ID + "/custom",
                    "/habit/assign/{habitAssignId}/enroll/**",
                    "/habit/assign/{habitAssignId}/unenroll/{date}",
                    "/habit/statistic/{habitId}",
                    "/habit/like",
                    "/habit/dislike",
                    HABITS + "/{habitId}/comments",
                    HABITS + "/comments/like",
                    "/habit/{habitId}/favorites",
                    "/place/{placeId}/comments",
                    "/place/propose",
                    "/place/save/favorite/",
                    "/place/filter/api",
                    USER_CUSTOM_TO_DO_LIST_ITEMS,
                    USER_TO_DO_LIST,
                    "/user/{userId}/habit",
                    "/user/{userId}/userFriend/{friendId}",
                    "/user/{userId}/declineFriend/{friendId}",
                    "/user/{userId}/acceptFriend/{friendId}",
                    "/habit/custom",
                    "/custom/to-do-list-items/{userId}/{habitId}/custom-to-do-list-items",
                    FRIENDS + "/{friendId}",
                    ECO_NEWS + "/{ecoNewsId}/favorites",
                    "/habit/assign/{habitId}/invite",
                    "place/v2/save")
                .hasAnyRole(USER, ADMIN, MODERATOR, UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.PUT,
                    "/habit/statistic/{id}",
                    ECO_NEWS + ECO_NEWS_ID,
                    ECO_NEWS_ID_COMMENTS + COMMENT_ID,
                    "/favorite_place/",
                    "/user/profile",
                    EVENTS_COMMENTS + COMMENT_ID,
                    EVENTS + EVENT_ID,
                    "/habit/update/{habitId}",
                    HABIT_ASSIGN_ID + "/update-habit-duration",
                    "/habit/assign/{habitAssignId}/updateProgressNotificationHasDisplayed",
                    HABIT_ASSIGN_ID + "/allUserAndCustomList",
                    "/habit/assign/{habitAssignId}/update-status-and-duration")
                .hasAnyRole(USER, ADMIN, MODERATOR, UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.PATCH,
                    HABITS + COMMENTS,
                    ECO_NEWS + COMMENTS,
                    EVENTS_COMMENTS + COMMENT_ID,
                    ECO_NEWS + COMMENTS,
                    CUSTOM_TO_DO_LIST_ITEMS,
                    CUSTOM_TO_DO_LIST_URL,
                    HABIT_ASSIGN_ID,
                    "/to-do-list-items/toDoList/{userId}",
                    HABIT_ASSIGN_ID,
                    USER_CUSTOM_TO_DO_LIST_ITEMS,
                    USER_TO_DO_LIST + "/{toDoListItemId}/status/{status}",
                    USER_TO_DO_LIST + "/{userToDoListItemId}",
                    "/user/profilePicture",
                    "/user/deleteProfilePicture",
                    FRIENDS + "/{friendId}/acceptFriend",
                    FRIENDS + "/{friendId}/declineFriend",
                    HABIT_INVITE + INVITATION_ID + "/accept")
                .hasAnyRole(USER, ADMIN, MODERATOR, UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.DELETE,
                    ECO_NEWS + ECO_NEWS_ID,
                    ECO_NEWS + COMMENTS + ECO_NEWS_ID,
                    ECO_NEWS_ID_COMMENTS + COMMENT_ID,
                    HABITS + "/comments/{id}",
                    "/habit/{habitId}/favorites",
                    CUSTOM_TO_DO_LIST_ITEMS,
                    CUSTOM_TO_DO_LIST_URL,
                    "/favorite_place/{placeId}",
                    "/social-networks",
                    USER_CUSTOM_TO_DO_LIST_ITEMS,
                    USER_TO_DO_LIST + "/user-to-do-list-items",
                    USER_TO_DO_LIST,
                    EVENTS_COMMENTS + COMMENT_ID,
                    EVENTS + EVENT_ID,
                    EVENTS + EVENT_ID + ATTENDERS,
                    EVENTS + EVENT_ID + FAVORITES,
                    "/user/{userId}/userFriend/{friendId}",
                    "/habit/assign/delete/{habitAssignId}",
                    "/habit/delete/{customHabitId}",
                    ECO_NEWS + "/{ecoNewsId}/favorites",
                    FRIENDS,
                    FRIENDS + "/{friendId}",
                    FRIENDS + "/{friendId}/cancelRequest",
                    FRIENDS + "/{friendId}/cancelRequest",
                    "/notification/{notificationId}",
                    "/ownSecurity/user",
                    NOTIFICATIONS + NOTIFICATION_ID,
                    HABIT_INVITE + INVITATION_ID + "/reject",
                    EVENTS + EVENT_ID + "/removeFromRequested")
                .hasAnyRole(USER, ADMIN, MODERATOR, UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.GET,
                    COMMENTS,
                    COMMENTS + "/{id}",
                    "/user/all",
                    "/user/roles")
                .hasAnyRole(ADMIN, MODERATOR)
                .requestMatchers(HttpMethod.POST,
                    "/place/filter/predicate")
                .hasAnyRole(ADMIN, MODERATOR)
                .requestMatchers(HttpMethod.PUT,
                    "/place/update/")
                .hasAnyRole(ADMIN, MODERATOR)
                .requestMatchers(HttpMethod.PATCH,
                    "/place/status",
                    "/place/statuses")
                .hasAnyRole(ADMIN, MODERATOR)
                .requestMatchers(HttpMethod.DELETE,
                    "/place/{id}",
                    "/place")
                .hasAnyRole(ADMIN, MODERATOR)
                .requestMatchers(HttpMethod.POST,
                    "/user/filter")
                .hasAnyRole(ADMIN)
                .requestMatchers(HttpMethod.PATCH,
                    "/user",
                    "/user/status",
                    "/user/role",
                    "/user/update/role")
                .hasAnyRole(ADMIN)
                .requestMatchers(HttpMethod.DELETE,
                    COMMENTS)
                .hasAnyRole(ADMIN)
                .anyRequest().hasAnyRole(ADMIN))
            .logout(logout -> logout.logoutUrl("/logout")
                .logoutRequestMatcher(new AntPathRequestMatcher("/management/logout", "GET"))
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .deleteCookies("accessToken")
                .logoutSuccessUrl("/"));
        return http.build();
    }

    /**
     * Method for configure type of authentication provider.
     *
     * @param auth {@link AuthenticationManagerBuilder}
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(new JwtAuthenticationProvider(jwtTool));
    }

    /**
     * Provides AuthenticationManager.
     *
     * @return {@link AuthenticationManager}
     */
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
