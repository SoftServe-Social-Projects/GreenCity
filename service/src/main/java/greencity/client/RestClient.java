package greencity.client;

import static greencity.constant.AppConstant.AUTHORIZATION;

import com.google.gson.Gson;
import greencity.constant.RestTemplateLinks;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.achievement.UserVOAchievement;
import greencity.dto.place.PlaceVO;
import greencity.dto.user.UserManagementDto;
import greencity.dto.user.UserManagementVO;
import greencity.dto.user.UserManagementViewDto;
import greencity.dto.user.UserVO;
import greencity.message.AddEcoNewsMessage;
import greencity.message.SendChangePlaceStatusEmailMessage;
import greencity.message.SendHabitNotification;
import greencity.message.SendReportEmailMessage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
public class RestClient {
    private final RestTemplate restTemplate;
    @Value("${greencityuser.server.address}")
    private String greenCityUserServerAddress;
    private final HttpServletRequest httpServletRequest;

    /**
     * Method find user by principal.
     *
     * @param email of {@link UserVO}
     * @author Orest Mamchuk
     */
    public UserVO findByEmail(String email) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        return restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.USER_FIND_BY_EMAIL + RestTemplateLinks.EMAIL + email, HttpMethod.GET,
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
        return restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.SEARCH_BY + RestTemplateLinks.PAGE + pageable.getPageNumber()
            + RestTemplateLinks.SIZE + pageable.getPageSize()
            + RestTemplateLinks.QUERY + query, HttpMethod.GET, entity,
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
        return restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.USER_FIND_ID_BY_EMAIL
            + RestTemplateLinks.EMAIL + email, HttpMethod.GET, entity, Long.class)
            .getBody();
    }

    /**
     * Updates last activity time for a given user.
     *
     * @param userId               - {@link UserVO}'s id
     * @param userLastActivityTime - new {@link UserVO}'s last activity time
     * @author Orest Mamchuk
     */
    public void updateUserLastActivityTime(Long userId, Date userLastActivityTime) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        String strDate = dateFormat.format(userLastActivityTime);
        restTemplate.exchange(greenCityUserServerAddress + RestTemplateLinks.USER + "/" + userId
            + RestTemplateLinks.UPDATE_USER_LAST_ACTIVITY_TIME
            + strDate, HttpMethod.PUT, entity, Object.class);
    }

    /**
     * Method for setting {@link UserVO}'s status to DEACTIVATED, so the user will
     * not be able to log in into the system.
     *
     * @param userId - {@link UserVO}'s id
     * @author Orest Mamchuk
     */
    public void deactivateUser(Long userId) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        restTemplate.exchange(greenCityUserServerAddress + RestTemplateLinks.USER_DEACTIVATE
            + RestTemplateLinks.ID + userId, HttpMethod.PUT, entity, Object.class);
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
     * @param addEcoNewsMessage with information for sending email about adding new
     *                          eco news.
     * @author Taras Kavkalo
     */
    public void addEcoNews(AddEcoNewsMessage addEcoNewsMessage) {
        HttpEntity<AddEcoNewsMessage> entity = new HttpEntity<>(addEcoNewsMessage, setHeader());
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
        HttpEntity<SendReportEmailMessage> entity = new HttpEntity<>(reportEmailMessage, setHeader());
        restTemplate.exchange(greenCityUserServerAddress
            + RestTemplateLinks.SEND_REPORT, HttpMethod.POST, entity, Object.class)
            .getBody();
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
            new HttpEntity<>(changePlaceStatusEmailMessage, setHeader());
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
        HttpEntity<UserManagementViewDto> entity = new HttpEntity<>(userViewDto, setHeader());
        return restTemplate.exchange(
            greenCityUserServerAddress + RestTemplateLinks.USER_SEARCH + RestTemplateLinks.PAGE
                + pageable.getPageNumber()
                + RestTemplateLinks.SIZE + pageable.getPageSize(),
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
