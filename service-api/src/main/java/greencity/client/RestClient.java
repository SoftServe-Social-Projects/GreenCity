package greencity.client;

import static greencity.constant.AppConstant.AUTHORIZATION;

import com.google.gson.Gson;
import greencity.constant.RestTemplateLinks;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.achievement.UserVOAchievement;
import greencity.dto.econews.EcoNewsForSendEmailDto;
import greencity.dto.place.PlaceVO;
import greencity.dto.user.*;
import greencity.enums.EmailNotification;
import greencity.message.SendChangePlaceStatusEmailMessage;
import greencity.message.SendHabitNotification;
import greencity.message.SendReportEmailMessage;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Component
public class RestClient {
    private final RestTemplate restTemplate;
    @Setter
    @Value("${greencityuser.server.address}")
    private String greenCityUserServerAddress;
    private final HttpServletRequest httpServletRequest;

    /**
     * Method for getting all users by their {@link EmailNotification}.
     *
     * @param emailNotification enum with {@link EmailNotification} value.
     * @return {@link List} of {@link UserVO}.
     * @author Taras Kavkalo
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
     * @author Taras Kavkalo
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
     * @author Taras Kavkalo
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
     * @author Orest Mamchuk
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
     * @author Orest Mamchuk
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
     * @author Orest Mamchuk
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
     * @author Orest Mamchuk
     */
    public PageableAdvancedDto<UserManagementDto> findUserForManagementByPage(Pageable pageable) {
        Sort sort = pageable.getSort();
        StringBuilder orderUrl = new StringBuilder("");
        if (!sort.isEmpty()) {
            for (Sort.Order order : sort) {
                orderUrl.append(orderUrl.toString() + order.getProperty() + "," + order.getDirection());
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
     * @author Orest Mamchuk
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
     * @author Orest Mamchuk
     */
    public void updateUser(UserManagementDto userDto) {
        HttpEntity<UserManagementDto> entity = new HttpEntity<>(userDto, setHeader());
        restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.USER, HttpMethod.PUT, entity, Object.class)
            .getBody();
    }

    /**
     * Method for getting all Users.
     *
     * @return {@link List} of {@link UserVO} instances.
     * @author Orest Mamchuk
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
     * @author Orest Mamchuk
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
     * @author Orest Mamchuk
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
     * @author Orest Mamchuk
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
     * @author Orest Mamchuk
     */
    public void deactivateUser(Long userId, List<String> userReasons) {
        HttpHeaders headers = setHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<String>> entity = new HttpEntity<>(userReasons, headers);
        restTemplate.exchange(greenCityUserServerAddress + RestTemplateLinks.USER_DEACTIVATE
            + RestTemplateLinks.ID + userId, HttpMethod.PUT, entity, Object.class);
    }

    /**
     * Method for getting {@link String} user language.
     *
     * @param userId of the searched {@link UserVO}.
     * @return current user language {@link String}.
     * @author Vlad Pikhotskyi
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
     * @author Orest Mamchuk
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
     * @author Vlad Pikhotskyi
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
     * @author Orest Mamchuk
     */
    public List<Long> deactivateAllUsers(List<Long> listId) {
        Gson gson = new Gson();
        String json = gson.toJson(listId);
        HttpEntity<String> entity = new HttpEntity<>(json, setHeader());
        ResponseEntity<Long[]> exchange = restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.USER_DEACTIVATE
            + RestTemplateLinks.ID + listId, HttpMethod.PUT, entity, Long[].class);
        Long[] responseDtos = exchange.getBody();
        assert responseDtos != null;
        return Arrays.asList(responseDtos);
    }

    /**
     * Register new user from admin panel.
     *
     * @param userDto dto with updated fields.
     * @author Orest Mamchuk
     */
    public void managementRegisterUser(UserManagementDto userDto) {
        HttpEntity<UserManagementDto> entity = new HttpEntity<>(userDto, setHeader());
        restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.OWN_SECURITY_REGISTER, HttpMethod.POST, entity, Object.class)
            .getBody();
    }

    /**
     * send AddEcoNewsMessage to GreenCityUser.
     *
     * @param message with information for sending email about adding new eco news.
     * @author Taras Kavkalo
     */
    public void addEcoNews(EcoNewsForSendEmailDto message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EcoNewsForSendEmailDto> entity = new HttpEntity<>(message, headers);
        restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.ADD_ECO_NEWS, HttpMethod.POST, entity, Object.class)
            .getBody();
    }

    /**
     * send SendReportEmailMessage to GreenCityUser.
     *
     * @param reportEmailMessage with information for sending email report about new
     *                           places.
     * @author Taras Kavkalo
     */
    public void sendReport(SendReportEmailMessage reportEmailMessage) {
        HttpEntity<SendReportEmailMessage> entity = new HttpEntity<>(reportEmailMessage, new HttpHeaders());
        restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.SEND_REPORT, HttpMethod.POST, entity, Object.class)
            .getBody();
    }

    /**
     * Delete from the database users that have status 'DEACTIVATED' and last
     * visited the site 2 years ago.
     *
     * @author Taras Kavkalo
     */
    public void scheduleDeleteDeactivatedUsers() {
        HttpEntity<String> entity = new HttpEntity<>(new HttpHeaders());
        restTemplate.exchange(greenCityUserServerAddress + RestTemplateLinks.DELETE_DEACTIVATED_USERS,
            HttpMethod.POST, entity, Object.class).getBody();
    }

    /**
     * send SendChangePlaceStatusEmailMessage to GreenCityUser.
     *
     * @param changePlaceStatusEmailMessage with information for sending email
     *                                      during status update for {@link PlaceVO}
     *                                      when PlaceStatus.PROPOSED.
     * @author Taras Kavkalo
     */
    public void changePlaceStatus(SendChangePlaceStatusEmailMessage changePlaceStatusEmailMessage) {
        HttpEntity<SendChangePlaceStatusEmailMessage> entity =
            new HttpEntity<>(changePlaceStatusEmailMessage, new HttpHeaders());
        restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.CHANGE_PLACE_STATUS, HttpMethod.POST, entity, Object.class)
            .getBody();
    }

    /**
     * send SendHabitNotification to GreenCityUser.
     *
     * @param sendHabitNotification with information for sending email to each user
     *                              that hasn't marked any habit during some period.
     * @author Taras Kavkalo
     */
    public void sendHabitNotification(SendHabitNotification sendHabitNotification) {
        HttpEntity<SendHabitNotification> entity = new HttpEntity<>(sendHabitNotification, new HttpHeaders());
        restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.SEND_HABIT_NOTIFICATION, HttpMethod.POST, entity, Object.class)
            .getBody();
    }

    /**
     * Method that allow you to save new {@link UserVO}.
     *
     * @param userVO for save User.
     * @author Orest Mamchuk
     */
    public void save(UserVO userVO) {
        HttpEntity<UserVO> entity = new HttpEntity<>(userVO, setHeader());
        restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.USER, HttpMethod.POST, entity, Object.class)
            .getBody();
    }

    /**
     * Method that allow you to save new {@link UserVO}.
     *
     * @param userVO for save User.
     * @author Orest Mamchuk
     */
    public void save(UserVO userVO, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, accessToken);
        HttpEntity<UserVO> entity = new HttpEntity<>(userVO, headers);
        restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.USER, HttpMethod.POST, entity, Object.class)
            .getBody();
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
        StringBuilder orderUrl = new StringBuilder("");
        if (!sort.isEmpty()) {
            for (Sort.Order order : sort) {
                orderUrl.append(orderUrl.toString() + order.getProperty() + "," + order.getDirection());
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
        String accessToken = httpServletRequest.getHeader(AUTHORIZATION);
        Cookie[] cookies = httpServletRequest.getCookies();
        String uri = httpServletRequest.getRequestURI();
        if (cookies != null && uri.startsWith("/management")) {
            accessToken = getTokenFromCookies(cookies);
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
        return token == null ? null : "Bearer " + token;
    }
}
