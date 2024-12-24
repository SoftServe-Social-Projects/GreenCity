package greencity.client;

import greencity.annotations.CheckEmailPreference;
import greencity.constant.AppConstant;
import greencity.dto.econews.InterestingEcoNewsDto;
import greencity.dto.place.UpdatePlaceStatusWithUserEmailDto;
import greencity.dto.user.UserManagementDto;
import greencity.dto.user.UserManagementUpdateDto;
import greencity.dto.user.UserManagementVO;
import greencity.dto.user.UserManagementViewDto;
import greencity.dto.user.UserRoleDto;
import greencity.dto.user.UserStatusDto;
import greencity.dto.user.UserVO;
import greencity.enums.EmailPreference;
import greencity.enums.Role;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import greencity.enums.UserStatus;
import greencity.message.ScheduledEmailMessage;
import greencity.message.SendHabitNotification;
import greencity.message.SendReportEmailMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import greencity.security.jwt.JwtTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.util.UriComponentsBuilder;
import com.google.gson.Gson;
import greencity.constant.RestTemplateLinks;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.achievement.UserVOAchievement;
import greencity.enums.EmailNotification;
import static greencity.constant.AppConstant.AUTHORIZATION;

@Component
@Slf4j
public class RestClient {
    private final RestTemplate restTemplate;
    private final String greenCityUserServerAddress;

    private final HttpServletRequest httpServletRequest;
    private final JwtTool jwtTool;
    private final String systemEmail;

    /**
     * Constructs a new instance of the RestClient class.
     *
     * @param restTemplate               The RestTemplate to be used for making HTTP
     *                                   requests to GreenCityUser.
     * @param greenCityUserServerAddress The address of the GreenCityUser server.
     * @param httpServletRequest         The HttpServletRequest object contains data
     *                                   related to the current http request.
     * @param jwtTool                    The JwtTool is used to create JWT tokens
     *                                   for system requests to GreenCityUser.
     * @param systemEmail                The system email address used to creat JWT
     *                                   tokens for system requests to
     *                                   GreenCityUser.
     */
    public RestClient(RestTemplate restTemplate,
        @Value("${greencityuser.server.address}") String greenCityUserServerAddress,
        HttpServletRequest httpServletRequest,
        JwtTool jwtTool,
        @Value("${spring.liquibase.parameters.service-email}") String systemEmail) {
        this.restTemplate = restTemplate;
        this.greenCityUserServerAddress = greenCityUserServerAddress;
        this.httpServletRequest = httpServletRequest;
        this.jwtTool = jwtTool;
        this.systemEmail = systemEmail;
    }

    /**
     * Method for getting all users by their {@link EmailNotification}.
     *
     * @param emailNotification enum with {@link EmailNotification} value.
     * @return {@link List} of {@link UserVO}.
     */
    public List<UserVO> findAllByEmailNotification(EmailNotification emailNotification) {
        HttpEntity<String> entity = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<List<UserVO>> exchange = restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.USER_FIND_ALL_BY_EMAIL_NOTIFICATION
            + RestTemplateLinks.EMAIL_NOTIFICATION + emailNotification,
            HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
            });
        return exchange.getBody();
    }

    /**
     * Method that find all users cities.
     *
     * @return {@link List} of cities.
     */
    public List<String> findAllUsersCities() {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        ResponseEntity<List<String>> exchange = restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.FIND_ALL_USERS_CITIES,
            HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
            });
        return exchange.getBody();
    }

    /**
     * Method that find all registration months.
     *
     * @return {@link Map} with months.
     */
    public Map<Integer, Long> findAllRegistrationMonthsMap() {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        ResponseEntity<Map<Integer, Long>> exchange = restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.FIND_ALL_REGISTRATION_MONTHS_MAP,
            HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
            });
        return exchange.getBody();
    }

    /**
     * Method find user by principal.
     *
     * @param email of {@link UserVO}
     */
    public UserVO findByEmail(String email) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        UriComponentsBuilder url = UriComponentsBuilder.fromHttpUrl(greenCityUserServerAddress
            + RestTemplateLinks.USER_FIND_BY_EMAIL).queryParam("email", email);
        return restTemplate.exchange(url.toUriString(), HttpMethod.GET,
            entity, UserVO.class).getBody();
    }

    /**
     * Method that allow you to find {@link UserVO} by Id.
     *
     * @param id a value of {@link Long}
     * @return {@link UserVO}
     */
    public UserVO findById(Long id) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        return restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.USER_FIND_BY_ID + RestTemplateLinks.ID + id, HttpMethod.GET, entity, UserVO.class)
            .getBody();
    }

    /**
     * Method that allow you to find {@link UserVO} by ID.
     *
     * @param id a value of {@link Long}
     * @return {@link UserVO}
     */
    public UserVOAchievement findUserForAchievement(Long id) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        return restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.USER_FIND_BY_ID_FOR_ACHIEVEMENT + RestTemplateLinks.ID + id,
            HttpMethod.GET, entity, UserVOAchievement.class).getBody();
    }

    /**
     * Find {@link UserVO} for management by page .
     *
     * @param pageable a value with pageable configuration.
     * @return a dto of {@link PageableAdvancedDto}.
     */
    public PageableAdvancedDto<UserManagementDto> findUserForManagementByPage(Pageable pageable) {
        Sort sort = pageable.getSort();
        StringBuilder orderUrl = new StringBuilder();
        if (!sort.isEmpty()) {
            for (Sort.Order order : sort) {
                orderUrl.append(orderUrl).append(order.getProperty()).append(",").append(order.getDirection());
            }
        }
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        return restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.USER_FIND_USER_FOR_MANAGEMENT + RestTemplateLinks.PAGE + pageable
                .getPageNumber()
            + RestTemplateLinks.SIZE + pageable
                .getPageSize()
            + RestTemplateLinks.SORT + orderUrl,
            HttpMethod.GET, entity,
            new ParameterizedTypeReference<PageableAdvancedDto<UserManagementDto>>() {
            }).getBody();
    }

    /**
     * Method for getting UserVO by search query.
     *
     * @param pageable {@link Pageable}.
     * @param query    query to search
     * @return {@link PageableAdvancedDto} of {@link UserManagementDto} instances.
     */
    public PageableAdvancedDto<UserManagementDto> searchBy(Pageable pageable, String query) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        UriComponentsBuilder url = UriComponentsBuilder.fromHttpUrl(greenCityUserServerAddress
            + RestTemplateLinks.SEARCH_BY)
            .queryParam("page", pageable.getPageNumber())
            .queryParam("size", pageable.getPageSize())
            .queryParam("query", query);
        return restTemplate.exchange(url.toUriString(), HttpMethod.GET, entity,
            new ParameterizedTypeReference<PageableAdvancedDto<UserManagementDto>>() {
            }).getBody();
    }

    /**
     * Method for getting UserVO by search query.
     *
     * @param userDto dto with updated fields.
     */
    public void updateUser(UserManagementDto userDto) {
        UserManagementUpdateDto updateDto = managementDtoToUpdateDto(userDto);
        HttpHeaders headers = setHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserManagementUpdateDto> entity = new HttpEntity<>(updateDto, headers);
        restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.USER + "/" + userDto.getId(), HttpMethod.PUT, entity, Object.class);
        log.info("User with id {} has been updated", userDto.getId());
    }

    private UserManagementUpdateDto managementDtoToUpdateDto(UserManagementDto userDto) {
        return UserManagementUpdateDto.builder()
            .name(userDto.getName())
            .email(userDto.getEmail())
            .userCredo(userDto.getUserCredo())
            .role(userDto.getRole())
            .userStatus(userDto.getUserStatus())
            .build();
    }

    /**
     * Method for sending change role request.
     *
     * @param id   of user whose role is being changed
     * @param role new role
     */
    public void updateRole(Long id, Role role) {
        String url = greenCityUserServerAddress
            + RestTemplateLinks.USER + "/" + id + "/role";
        HttpHeaders headers = setHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        UserRoleDto userRoleDto = new UserRoleDto(role);
        HttpEntity<UserRoleDto> entity = new HttpEntity<>(userRoleDto, headers);
        restTemplate.exchange(url, HttpMethod.PATCH, entity, Object.class);
    }

    /**
     * Method for sending change status request.
     *
     * @param id     of user whose status is being changed
     * @param status new status
     *
     * @author Anton Bondar
     */
    public void updateStatus(Long id, UserStatus status) {
        String url = greenCityUserServerAddress + RestTemplateLinks.USER + "/status";
        HttpHeaders headers = setHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        UserStatusDto userStatusDto = new UserStatusDto(id, status);
        HttpEntity<UserStatusDto> entity = new HttpEntity<>(userStatusDto, headers);
        restTemplate.exchange(url, HttpMethod.PATCH, entity, Object.class);
    }

    /**
     * Method for getting all Users.
     *
     * @return {@link List} of {@link UserVO} instances.
     */
    public List<UserVO> findAll() {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        ResponseEntity<UserVO[]> exchange = restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.USER_FIND_ALL, HttpMethod.GET, entity, UserVO[].class);
        UserVO[] responseDtos = exchange.getBody();
        assert responseDtos != null;
        return Arrays.asList(responseDtos);
    }

    /**
     * Method for getting all Users.
     *
     * @return {@link List} of {@link UserVO} instances.
     */
    public List<UserManagementDto> findUserFriendsByUserId(Long id) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        ResponseEntity<UserManagementDto[]> exchange = restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.USER + "/" + id + RestTemplateLinks.FRIENDS, HttpMethod.GET, entity,
            UserManagementDto[].class);
        UserManagementDto[] responseDtos = exchange.getBody();
        assert responseDtos != null;
        return Arrays.asList(responseDtos);
    }

    /**
     * Method that allow you to find not 'DEACTIVATED' {@link UserVO} by email.
     *
     * @param email - {@link UserVO}'s email
     * @return {@link UserVO}
     */
    public Optional<UserVO> findNotDeactivatedByEmail(String email) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        UserVO body = restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.USER_FIND_NOT_DEACTIVATED_BY_EMAIL + RestTemplateLinks.EMAIL
            + email, HttpMethod.GET, entity, UserVO.class)
            .getBody();
        assert body != null;
        return Optional.of(body);
    }

    /**
     * Method find user id by email.
     *
     * @param email of {@link UserVO}
     */
    public Long findIdByEmail(String email) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        UriComponentsBuilder url = UriComponentsBuilder.fromHttpUrl(greenCityUserServerAddress
            + RestTemplateLinks.USER_FIND_ID_BY_EMAIL).queryParam("email", email);
        return restTemplate.exchange(url.toUriString(), HttpMethod.GET, entity, Long.class).getBody();
    }

    /**
     * Method for setting {@link UserVO}'s status to DEACTIVATED, so the user will
     * not be able to log in into the system.
     *
     * @param userId      - {@link UserVO}'s id
     * @param userReasons {@link List} of {@link String}.
     */
    public void deactivateUser(Long userId, List<String> userReasons) {
        HttpHeaders headers = setHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<String>> entity = new HttpEntity<>(userReasons, headers);
        restTemplate.exchange(greenCityUserServerAddress + RestTemplateLinks.USER_DEACTIVATE
            + RestTemplateLinks.ID + userId, HttpMethod.PUT, entity, Object.class);
        log.info("User with id {} has been deactivated", userId);
    }

    /**
     * Method for getting {@link String} user language.
     *
     * @param userId of the searched {@link UserVO}.
     * @return current user language {@link String}.
     */
    public String getUserLang(Long userId) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        String body = restTemplate.exchange(greenCityUserServerAddress + RestTemplateLinks.USER_LANG
            + RestTemplateLinks.ID + userId, HttpMethod.GET, entity, String.class).getBody();
        assert body != null;
        return body;
    }

    /**
     * Method for setting {@link UserVO}'s status to ACTIVATED.
     *
     * @param userId - {@link UserVO}'s id
     */
    public void setActivatedStatus(Long userId) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        restTemplate.exchange(greenCityUserServerAddress + RestTemplateLinks.USER_ACTIVATE
            + RestTemplateLinks.ID + userId, HttpMethod.PUT, entity, Object.class);
    }

    /**
     * Method for getting a {@link List} of {@link String} - reasons for
     * deactivation of the current user.
     *
     * @param userId    {@link Long} - user's id.
     * @param adminLang {@link String} - current administrator language.
     * @return {@link List} of {@link String} - reasons for deactivation of the
     *         current user.
     */
    public List<String> getDeactivationReason(Long userId, String adminLang) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        String[] reasonDtos = restTemplate.exchange(greenCityUserServerAddress + RestTemplateLinks.USER_REASONS
            + RestTemplateLinks.ID + userId
            + RestTemplateLinks.ADMIN_LANG + adminLang, HttpMethod.GET, entity, String[].class).getBody();
        assert reasonDtos != null;
        return Arrays.asList(reasonDtos);
    }

    /**
     * Method for setting to a list of {@link UserVO} status DEACTIVATED, so the
     * users will not be able to log in into the system.
     *
     * @param listId {@link List} populated with ids of {@link UserVO} to be
     *               deleted.
     */
    public ResponseEntity<Long[]> deactivateAllUsers(List<Long> listId) {
        Gson gson = new Gson();
        String json = gson.toJson(listId);
        HttpHeaders headers = setHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);
        return restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.USER_DEACTIVATE_ALL
            + RestTemplateLinks.ID + listId, HttpMethod.PUT, entity, Long[].class);
    }

    /**
     * Register new user from admin panel.
     *
     * @param userDto dto with updated fields.
     */
    public void managementRegisterUser(UserManagementDto userDto) {
        HttpHeaders headers = setHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserManagementDto> entity = new HttpEntity<>(userDto, headers);
        restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.OWN_SECURITY_REGISTER, HttpMethod.POST, entity, Object.class);
    }

    /**
     * Send InterestingEcoNewsDto to GreenCityUser.
     *
     * @param message with information for sending email about adding new eco news.
     */
    public void sendInterestingEcoNews(InterestingEcoNewsDto message) {
        HttpHeaders headers = setHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InterestingEcoNewsDto> entity = new HttpEntity<>(message, headers);
        restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.SEND_INTERESTING_ECO_NEWS, HttpMethod.POST, entity, Object.class);
    }

    public void sendEmailNotificationChangesPlaceStatus(UpdatePlaceStatusWithUserEmailDto message) {
        HttpHeaders headers = setHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdatePlaceStatusWithUserEmailDto> entity = new HttpEntity<>(message, headers);
        restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.SEND_NOTIFICATION_STATUS_PLACE, HttpMethod.POST, entity, Object.class);
    }

    /**
     * send SendReportEmailMessage to GreenCityUser.
     *
     * @param reportEmailMessage with information for sending email report about new
     *                           places.
     */
    public void sendReport(SendReportEmailMessage reportEmailMessage) {
        HttpHeaders headers = setHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SendReportEmailMessage> entity = new HttpEntity<>(reportEmailMessage, headers);
        restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.SEND_REPORT, HttpMethod.POST, entity, Object.class);
    }

    /**
     * Delete from the database users that have status 'DEACTIVATED' and last
     * visited the site 2 years ago.
     */
    public void scheduleDeleteDeactivatedUsers() {
        HttpEntity<String> entity = new HttpEntity<>(new HttpHeaders());
        restTemplate.exchange(greenCityUserServerAddress + RestTemplateLinks.DELETE_DEACTIVATED_USERS,
            HttpMethod.POST, entity, Object.class);
    }

    /**
     * send SendHabitNotification to GreenCityUser.
     *
     * @param sendHabitNotification with information for sending email to each user
     *                              that hasn't marked any habit during some period.
     */
    public void sendHabitNotification(SendHabitNotification sendHabitNotification) {
        HttpHeaders headers = setHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SendHabitNotification> entity = new HttpEntity<>(sendHabitNotification, headers);
        restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.SEND_HABIT_NOTIFICATION, HttpMethod.POST, entity, Object.class);
    }

    /**
     * Method that allow you to search users by several values
     * {@link UserManagementViewDto}.
     *
     * @param pageable    {@link Pageable}.
     * @param userViewDto for search User.
     * @return a dto of {@link PageableAdvancedDto}.
     */
    public PageableAdvancedDto<UserManagementVO> search(Pageable pageable, UserManagementViewDto userViewDto) {
        Sort sort = pageable.getSort();
        StringBuilder orderUrl = new StringBuilder();
        if (!sort.isEmpty()) {
            for (Sort.Order order : sort) {
                orderUrl.append(orderUrl).append(order.getProperty()).append(",").append(order.getDirection());
            }
        }
        HttpHeaders headers = setHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserManagementViewDto> entity = new HttpEntity<>(userViewDto, headers);
        return restTemplate.exchange(
            greenCityUserServerAddress + RestTemplateLinks.USER_SEARCH + RestTemplateLinks.PAGE
                + pageable.getPageNumber()
                + RestTemplateLinks.SIZE + pageable.getPageSize()
                + RestTemplateLinks.SORT + orderUrl,
            HttpMethod.POST, entity,
            new ParameterizedTypeReference<PageableAdvancedDto<UserManagementVO>>() {
            }).getBody();
    }

    /**
     * Method makes headers for RestTemplate.
     *
     * @return {@link HttpEntity}
     */
    private HttpHeaders setHeader() {
        String accessToken = null;
        if (RequestContextHolder.getRequestAttributes() != null) {
            Cookie[] cookies = httpServletRequest.getCookies();
            String uri = httpServletRequest.getRequestURI();

            if (cookies != null && uri.startsWith("/management")) {
                accessToken = getTokenFromCookies(cookies);
            }
        }

        if (!StringUtils.hasLength(accessToken)) {
            accessToken = AppConstant.TOKEN_PREFIX + jwtTool.createAccessToken(systemEmail, Role.ROLE_ADMIN);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, accessToken);
        return headers;
    }

    private String getTokenFromCookies(Cookie[] cookies) {
        String token = Arrays.stream(cookies)
            .filter(c -> c.getName().equals("accessToken"))
            .findFirst()
            .map(Cookie::getValue).orElse(null);
        return token == null ? null : AppConstant.TOKEN_PREFIX + token;
    }

    /**
     * Method sends scheduled email notification.
     *
     * @param message {@link ScheduledEmailMessage}.
     */
    public void sendScheduledEmailNotification(ScheduledEmailMessage message) {
        HttpHeaders headers = setHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ScheduledEmailMessage> entity = new HttpEntity<>(message, headers);
        restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.SEND_SCHEDULED_NOTIFICATION, HttpMethod.POST, entity, Object.class);
    }

    @CheckEmailPreference(EmailPreference.SYSTEM)
    public void sendEmailNotificationSystem(ScheduledEmailMessage message) {
        sendScheduledEmailNotification(message);
    }

    @CheckEmailPreference(EmailPreference.LIKES)
    public void sendEmailNotificationLikes(ScheduledEmailMessage message) {
        sendScheduledEmailNotification(message);
    }

    @CheckEmailPreference(EmailPreference.COMMENTS)
    public void sendEmailNotificationComments(ScheduledEmailMessage message) {
        sendScheduledEmailNotification(message);
    }

    @CheckEmailPreference(EmailPreference.INVITES)
    public void sendEmailNotificationInvites(ScheduledEmailMessage message) {
        sendScheduledEmailNotification(message);
    }

    @CheckEmailPreference(EmailPreference.PLACES)
    public void sendEmailNotificationPlaces(ScheduledEmailMessage message) {
        sendScheduledEmailNotification(message);
    }
}
