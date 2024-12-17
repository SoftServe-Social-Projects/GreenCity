package greencity.client;

import static greencity.ModelUtils.getEntity;
import static greencity.TestConst.ACCESS_TOKEN;
import static greencity.TestConst.GREEN_CITY_USER_ADDRESS;
import static greencity.TestConst.SYSTEM_EMAIL;
import static greencity.TestConst.TOKEN;
import static greencity.TestConst.UPDATE_STATUS_URL;
import static greencity.TestConst.USER_ID;
import static greencity.constant.AppConstant.AUTHORIZATION;
import greencity.dto.econews.InterestingEcoNewsDto;
import greencity.dto.place.UpdatePlaceStatusWithUserEmailDto;
import greencity.dto.user.UserStatusDto;
import greencity.dto.user.UserVO;
import greencity.dto.user.UserManagementDto;
import greencity.dto.user.UserManagementUpdateDto;
import greencity.dto.user.UserRoleDto;
import greencity.dto.user.UserManagementViewDto;
import greencity.dto.user.UserManagementVO;
import com.google.gson.Gson;
import greencity.ModelUtils;
import greencity.constant.RestTemplateLinks;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.achievement.UserVOAchievement;
import greencity.enums.EmailNotification;
import greencity.enums.PlaceStatus;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.message.ScheduledEmailMessage;
import greencity.message.SendHabitNotification;
import greencity.message.SendReportEmailMessage;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import greencity.security.jwt.JwtTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class RestClientTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private Object object;

    @Mock
    private RequestAttributes requestAttributes;

    private RestClient restClient;

    @Mock
    private JwtTool jwtTool;

    @BeforeEach
    void init() {
        restClient = new RestClient(restTemplate, GREEN_CITY_USER_ADDRESS, httpServletRequest, jwtTool, SYSTEM_EMAIL);
        RequestContextHolder.setRequestAttributes(requestAttributes);
    }

    @Test
    void findByEmail() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        UserVO userVO = ModelUtils.getUserVO();

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS + RestTemplateLinks.USER_FIND_BY_EMAIL
            + RestTemplateLinks.EMAIL + "taras@gmail.com", HttpMethod.GET,
            entity, UserVO.class)).thenReturn(ResponseEntity.ok(userVO));

        assertEquals(userVO, restClient.findByEmail("taras@gmail.com"));
    }

    @Test
    void findById() {
        UserVO userVO = ModelUtils.getUserVO();
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.USER_FIND_BY_ID + RestTemplateLinks.ID + 1L, HttpMethod.GET, entity, UserVO.class))
            .thenReturn(ResponseEntity.ok(userVO));

        assertEquals(userVO, restClient.findById(1L));
    }

    @Test
    void findUserForAchievement() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        UserVOAchievement userVOAchievement = new UserVOAchievement();

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.USER_FIND_BY_ID_FOR_ACHIEVEMENT + RestTemplateLinks.ID + 1L,
            HttpMethod.GET, entity, UserVOAchievement.class)).thenReturn(ResponseEntity.ok(userVOAchievement));

        assertEquals(userVOAchievement, restClient.findUserForAchievement(1L));
    }

    @Test
    void searchBy() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String query = "Search";
        Pageable pageable = PageRequest.of(0, 10);
        List<UserManagementDto> ecoNewsDtos = Collections.singletonList(new UserManagementDto());
        PageableAdvancedDto<UserManagementDto> pageableAdvancedDto =
            new PageableAdvancedDto<>(ecoNewsDtos, 2, 0, 3, 0, true, true, true, true);

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEARCH_BY + RestTemplateLinks.PAGE + pageable.getPageNumber()
            + RestTemplateLinks.SIZE + pageable.getPageSize()
            + RestTemplateLinks.QUERY + query, HttpMethod.GET, entity,
            new ParameterizedTypeReference<PageableAdvancedDto<UserManagementDto>>() {
            })).thenReturn(ResponseEntity.ok(pageableAdvancedDto));

        assertEquals(pageableAdvancedDto, restClient.searchBy(pageable, query));
    }

    @Test
    void updateUser() {
        // given
        UserManagementDto userManagementDto = new UserManagementDto();
        UserManagementUpdateDto userManagementUpdateDto = new UserManagementUpdateDto();
        userManagementDto.setId(1L);
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserManagementUpdateDto> entity = new HttpEntity<>(userManagementUpdateDto, headers);

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.USER + "/1", HttpMethod.PUT, entity, Object.class))
            .thenReturn(ResponseEntity.ok(object));

        restClient.updateUser(userManagementDto);

        assertEquals(ResponseEntity.ok(object), restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.USER + "/1", HttpMethod.PUT, entity, Object.class));
    }

    @Test
    void updateRole() {
        Role newRole = Role.ROLE_MODERATOR;
        UserRoleDto userRoleDto = new UserRoleDto(newRole);
        String url = GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.USER + "/1/role";
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserRoleDto> entity = new HttpEntity<>(userRoleDto, headers);

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);

        restClient.updateRole(1L, newRole);

        verify(restTemplate).exchange(url, HttpMethod.PATCH, entity, Object.class);
    }

    @Test
    void updateStatusTest() {
        String url = GREEN_CITY_USER_ADDRESS + RestTemplateLinks.USER + UPDATE_STATUS_URL;
        HttpEntity<UserStatusDto> entity = getEntity();

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);
        restClient.updateStatus(USER_ID, UserStatus.ACTIVATED);
        verify(restTemplate).exchange(url, HttpMethod.PATCH, entity, Object.class);
        verify(jwtTool).createAccessToken(anyString(), any(Role.class));
    }

    @Test
    void findAll() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        UserVO userVO = ModelUtils.getUserVO();
        UserVO[] userVOS = new UserVO[] {userVO};

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.USER_FIND_ALL, HttpMethod.GET, entity, UserVO[].class))
            .thenReturn(ResponseEntity.of(Optional.of(userVOS)));

        assertEquals(Arrays.asList(userVOS), restClient.findAll());
    }

    @Test
    void findUserFriendsByUserId() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        UserManagementDto userManagementDto = new UserManagementDto();
        UserManagementDto[] userManagementDtos = new UserManagementDto[] {userManagementDto};

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.USER + "/" + 1L + RestTemplateLinks.FRIENDS, HttpMethod.GET, entity,
            UserManagementDto[].class)).thenReturn(ResponseEntity.ok(userManagementDtos));
        restClient.findUserFriendsByUserId(1L);

        verify(restTemplate).exchange(GREEN_CITY_USER_ADDRESS + RestTemplateLinks.USER + "/"
            + 1L + RestTemplateLinks.FRIENDS, HttpMethod.GET, entity, UserManagementDto[].class);
    }

    @Test
    void findNotDeactivatedByEmail() {
        String email = "test@gmail.com";
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        UserVO userVO = ModelUtils.getUserVO();

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.USER_FIND_NOT_DEACTIVATED_BY_EMAIL + RestTemplateLinks.EMAIL
            + email, HttpMethod.GET, entity, UserVO.class)).thenReturn(ResponseEntity.ok(userVO));

        assertEquals(Optional.of(userVO), restClient.findNotDeactivatedByEmail(email));
    }

    @Test
    void findNotDeactivatedByEmailFromCookies() {
        String email = "test@gmail.com";
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, "Bearer testToken");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        UserVO userVO = ModelUtils.getUserVO();
        Cookie[] cookies = new Cookie[] {new Cookie("accessToken", "testToken")};
        when(httpServletRequest.getCookies()).thenReturn(cookies);
        when(httpServletRequest.getRequestURI()).thenReturn("/management");
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.USER_FIND_NOT_DEACTIVATED_BY_EMAIL + RestTemplateLinks.EMAIL
            + email, HttpMethod.GET, entity, UserVO.class)).thenReturn(ResponseEntity.ok(userVO));

        assertEquals(Optional.of(userVO), restClient.findNotDeactivatedByEmail(email));
    }

    @Test
    void findIdByEmail() {
        String email = "test@gmail.com";
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.USER_FIND_ID_BY_EMAIL
            + RestTemplateLinks.EMAIL + email, HttpMethod.GET, entity, Long.class))
            .thenReturn(ResponseEntity.ok(1L));

        assertEquals(1L, restClient.findIdByEmail(email));
    }

    @Test
    void getDeactivationReason() {
        HttpHeaders headers = new HttpHeaders();
        String[] test = new String[] {"test", "test"};
        List<String> listString = Arrays.asList(test);
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS + RestTemplateLinks.USER_REASONS
            + RestTemplateLinks.ID + 1L
            + RestTemplateLinks.ADMIN_LANG + "en", HttpMethod.GET, entity, String[].class))
            .thenReturn(ResponseEntity.ok(test));

        assertEquals(listString, restClient.getDeactivationReason(1L, "en"));
    }

    @Test
    void getUserLang() {
        String test = "test";
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS + RestTemplateLinks.USER_LANG
            + RestTemplateLinks.ID + 1L, HttpMethod.GET, entity, String.class))
            .thenReturn(ResponseEntity.ok(test));

        assertEquals(test, restClient.getUserLang(1L));
    }

    @Test
    void deactivateUser() {
        List<String> test = List.of("test", "test");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        HttpEntity<List<String>> entity = new HttpEntity<>(test, headers);

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS + RestTemplateLinks.USER_DEACTIVATE
            + RestTemplateLinks.ID + 1L, HttpMethod.PUT, entity, Object.class))
            .thenReturn(ResponseEntity.ok(object));

        restClient.deactivateUser(1L, test);

        verify(restTemplate).exchange(GREEN_CITY_USER_ADDRESS + RestTemplateLinks.USER_DEACTIVATE
            + RestTemplateLinks.ID + 1L, HttpMethod.PUT, entity, Object.class);
    }

    @Test
    void setActivatedStatus() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS + RestTemplateLinks.USER_ACTIVATE
            + RestTemplateLinks.ID + 1L, HttpMethod.PUT, entity, Object.class))
            .thenReturn(ResponseEntity.ok(object));

        restClient.setActivatedStatus(1L);

        verify(restTemplate).exchange(GREEN_CITY_USER_ADDRESS + RestTemplateLinks.USER_ACTIVATE
            + RestTemplateLinks.ID + 1L, HttpMethod.PUT, entity, Object.class);
    }

    @Test
    void deactivateAllUsers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        Long[] longs = new Long[] {1L, 2L};
        List<Long> listId = Arrays.asList(longs);
        Gson gson = new Gson();
        String json = gson.toJson(listId);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);

        restClient.deactivateAllUsers(listId);

        verify(restTemplate).exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.USER_DEACTIVATE_ALL
            + RestTemplateLinks.ID + listId, HttpMethod.PUT, entity, Long[].class);

        verify(jwtTool).createAccessToken(anyString(), any(Role.class));

    }

    @Test
    void managementRegisterUser() {
        UserManagementDto userManagementDto = new UserManagementDto();
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserManagementDto> entity = new HttpEntity<>(userManagementDto, headers);

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.OWN_SECURITY_REGISTER, HttpMethod.POST, entity, Object.class))
            .thenReturn(ResponseEntity.ok(object));

        restClient.managementRegisterUser(userManagementDto);

        verify(restTemplate).exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.OWN_SECURITY_REGISTER, HttpMethod.POST, entity, Object.class);
    }

    @Test
    void sendInterestingEcoNews() {
        InterestingEcoNewsDto message = ModelUtils.getInterestingEcoNewsDto();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set(AUTHORIZATION, ACCESS_TOKEN);
        HttpEntity<InterestingEcoNewsDto> entity = new HttpEntity<>(message, httpHeaders);

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEND_INTERESTING_ECO_NEWS, HttpMethod.POST, entity, Object.class))
            .thenReturn(ResponseEntity.ok(object));

        restClient.sendInterestingEcoNews(message);

        verify(restTemplate).exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEND_INTERESTING_ECO_NEWS, HttpMethod.POST, entity, Object.class);
    }

    @Test
    void sendReport() {
        SendReportEmailMessage message = ModelUtils.getSendReportEmailMessage();
        HttpEntity<SendReportEmailMessage> entity = new HttpEntity<>(message, ModelUtils.getHeaders());
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEND_REPORT, HttpMethod.POST, entity, Object.class))
            .thenReturn(ResponseEntity.ok(object));
        when(jwtTool.createAccessToken(SYSTEM_EMAIL, Role.ROLE_ADMIN)).thenReturn("accessToken");
        restClient.sendReport(message);

        verify(jwtTool).createAccessToken(SYSTEM_EMAIL, Role.ROLE_ADMIN);
        verify(restTemplate).exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEND_REPORT, HttpMethod.POST, entity, Object.class);
    }

    @Test
    void sendHabitNotification() {
        SendHabitNotification notification = ModelUtils.getSendHabitNotification();
        HttpEntity<SendHabitNotification> entity = new HttpEntity<>(notification, ModelUtils.getHeaders());

        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEND_HABIT_NOTIFICATION, HttpMethod.POST, entity, Object.class))
            .thenReturn(ResponseEntity.ok(object));
        when(jwtTool.createAccessToken(SYSTEM_EMAIL, Role.ROLE_ADMIN)).thenReturn("accessToken");

        restClient.sendHabitNotification(notification);

        verify(jwtTool).createAccessToken(SYSTEM_EMAIL, Role.ROLE_ADMIN);
        verify(restTemplate).exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEND_HABIT_NOTIFICATION, HttpMethod.POST, entity, Object.class);
    }

    @Test
    void findUserForManagementByPage() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        List<UserManagementDto> ecoNewsDtos = Collections.singletonList(new UserManagementDto());
        PageableAdvancedDto<UserManagementDto> pageableAdvancedDto =
            new PageableAdvancedDto<>(ecoNewsDtos, 2, 0, 3, 0, true, true, true, true);

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.USER_FIND_USER_FOR_MANAGEMENT + RestTemplateLinks.PAGE + pageable.getPageNumber()
            + RestTemplateLinks.SIZE + pageable.getPageSize() + "&sort=id,ASC", HttpMethod.GET, entity,
            new ParameterizedTypeReference<PageableAdvancedDto<UserManagementDto>>() {
            })).thenReturn(ResponseEntity.ok(pageableAdvancedDto));

        assertEquals(pageableAdvancedDto, restClient.findUserForManagementByPage(pageable));
    }

    @Test
    void searchTest() {
        Pageable pageable = PageRequest.of(0, 20, Sort.unsorted());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        UserManagementViewDto userViewDto =
            UserManagementViewDto.builder()
                .id("1L")
                .name("vivo")
                .email("test@ukr.net")
                .userCredo("Hello")
                .role("1")
                .userStatus("1")
                .build();
        List<UserManagementVO> userManagementVOS = Collections.singletonList(new UserManagementVO());
        PageableAdvancedDto<UserManagementVO> userAdvancedDto =
            new PageableAdvancedDto<>(userManagementVOS, 20, 0, 0, 0,
                true, true, true, true);
        HttpEntity<UserManagementViewDto> entity = new HttpEntity<>(userViewDto, headers);

        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);
        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.USER_SEARCH + RestTemplateLinks.PAGE + pageable.getPageNumber()
            + RestTemplateLinks.SIZE + pageable.getPageSize()
            + RestTemplateLinks.SORT, HttpMethod.POST, entity,
            new ParameterizedTypeReference<PageableAdvancedDto<UserManagementVO>>() {
            })).thenReturn(ResponseEntity.ok(userAdvancedDto));

        assertEquals(userAdvancedDto, restClient.search(pageable, userViewDto));
    }

    @Test
    void scheduleDeleteDeactivatedUsers() {
        HttpEntity<String> entity = new HttpEntity<>(new HttpHeaders());

        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS + RestTemplateLinks.DELETE_DEACTIVATED_USERS,
            HttpMethod.POST, entity, Object.class))
            .thenReturn(ResponseEntity.ok(object));
        restClient.scheduleDeleteDeactivatedUsers();

        verify(restTemplate, times(1)).exchange(GREEN_CITY_USER_ADDRESS + RestTemplateLinks.DELETE_DEACTIVATED_USERS,
            HttpMethod.POST, entity, Object.class);
    }

    @Test
    void findAllByEmailNotification() {
        HttpEntity<String> entity = new HttpEntity<>(new HttpHeaders());
        List<UserVO> userVOS = Collections.singletonList(ModelUtils.getUserVO());

        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.USER_FIND_ALL_BY_EMAIL_NOTIFICATION
            + RestTemplateLinks.EMAIL_NOTIFICATION + EmailNotification.IMMEDIATELY,
            HttpMethod.GET, entity, new ParameterizedTypeReference<List<UserVO>>() {
            }))
            .thenReturn(ResponseEntity.status(HttpStatus.OK).body(userVOS));

        assertEquals(userVOS, restClient.findAllByEmailNotification(EmailNotification.IMMEDIATELY));
    }

    @Test
    void findAllRegistrationMonthsMap() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        Map<Integer, Long> expected = Collections.singletonMap(1, 1L);

        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.FIND_ALL_REGISTRATION_MONTHS_MAP,
            HttpMethod.GET, entity, new ParameterizedTypeReference<Map<Integer, Long>>() {
            })).thenReturn(ResponseEntity.status(HttpStatus.OK).body(expected));
        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);

        assertEquals(expected, restClient.findAllRegistrationMonthsMap());
    }

    @Test
    void findAllUsersCities() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> expected = Collections.singletonList("text");

        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS + RestTemplateLinks.FIND_ALL_USERS_CITIES,
            HttpMethod.GET, entity, new ParameterizedTypeReference<List<String>>() {
            }))
            .thenReturn(ResponseEntity.status(HttpStatus.OK).body(expected));
        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);

        assertEquals(expected, restClient.findAllUsersCities());
    }

    @Test
    void sendScheduledNotificationTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ScheduledEmailMessage message = ModelUtils.getScheduledEmailMessage();

        HttpEntity<ScheduledEmailMessage> entity = new HttpEntity<>(message, headers);

        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEND_SCHEDULED_NOTIFICATION, HttpMethod.POST, entity, Object.class))
            .thenReturn(ResponseEntity.ok(object));
        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);

        restClient.sendScheduledEmailNotification(message);

        verify(restTemplate).exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEND_SCHEDULED_NOTIFICATION, HttpMethod.POST, entity, Object.class);
    }

    @Test
    void sendEmailNotificationSystemTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ScheduledEmailMessage message = ModelUtils.getScheduledEmailMessage();

        HttpEntity<ScheduledEmailMessage> entity = new HttpEntity<>(message, headers);

        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEND_SCHEDULED_NOTIFICATION, HttpMethod.POST, entity, Object.class))
            .thenReturn(ResponseEntity.ok(object));
        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);

        restClient.sendEmailNotificationSystem(message);

        verify(restTemplate).exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEND_SCHEDULED_NOTIFICATION, HttpMethod.POST, entity, Object.class);
    }

    @Test
    void sendEmailNotificationLikesTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ScheduledEmailMessage message = ModelUtils.getScheduledEmailMessage();

        HttpEntity<ScheduledEmailMessage> entity = new HttpEntity<>(message, headers);

        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEND_SCHEDULED_NOTIFICATION, HttpMethod.POST, entity, Object.class))
            .thenReturn(ResponseEntity.ok(object));
        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);

        restClient.sendEmailNotificationLikes(message);

        verify(restTemplate).exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEND_SCHEDULED_NOTIFICATION, HttpMethod.POST, entity, Object.class);
    }

    @Test
    void sendEmailNotificationCommentsTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ScheduledEmailMessage message = ModelUtils.getScheduledEmailMessage();

        HttpEntity<ScheduledEmailMessage> entity = new HttpEntity<>(message, headers);

        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEND_SCHEDULED_NOTIFICATION, HttpMethod.POST, entity, Object.class))
            .thenReturn(ResponseEntity.ok(object));
        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);

        restClient.sendEmailNotificationComments(message);

        verify(restTemplate).exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEND_SCHEDULED_NOTIFICATION, HttpMethod.POST, entity, Object.class);
    }

    @Test
    void sendEmailNotificationInvitesTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ScheduledEmailMessage message = ModelUtils.getScheduledEmailMessage();

        HttpEntity<ScheduledEmailMessage> entity = new HttpEntity<>(message, headers);

        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEND_SCHEDULED_NOTIFICATION, HttpMethod.POST, entity, Object.class))
            .thenReturn(ResponseEntity.ok(object));
        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);

        restClient.sendEmailNotificationInvites(message);

        verify(restTemplate).exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEND_SCHEDULED_NOTIFICATION, HttpMethod.POST, entity, Object.class);
    }

    @Test
    void sendEmailNotificationPlacesTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, ACCESS_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ScheduledEmailMessage message = ModelUtils.getScheduledEmailMessage();

        HttpEntity<ScheduledEmailMessage> entity = new HttpEntity<>(message, headers);

        when(restTemplate.exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEND_SCHEDULED_NOTIFICATION, HttpMethod.POST, entity, Object.class))
            .thenReturn(ResponseEntity.ok(object));
        when(jwtTool.createAccessToken(anyString(), any(Role.class))).thenReturn(TOKEN);

        restClient.sendEmailNotificationPlaces(message);

        verify(restTemplate).exchange(GREEN_CITY_USER_ADDRESS
            + RestTemplateLinks.SEND_SCHEDULED_NOTIFICATION, HttpMethod.POST, entity, Object.class);
    }

    @Test
    void sendEmailNotificationChangesPlaceStatusTest() {
        UpdatePlaceStatusWithUserEmailDto message = new UpdatePlaceStatusWithUserEmailDto();
        message.setPlaceName("TestPlace");
        message.setNewStatus(PlaceStatus.APPROVED);
        message.setUserName("TestUser");
        message.setEmail("test@example.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String expectedUrl = GREEN_CITY_USER_ADDRESS + RestTemplateLinks.SEND_NOTIFICATION_STATUS_PLACE;
        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
            .thenReturn(ResponseEntity.ok().build());
        restClient.sendEmailNotificationChangesPlaceStatus(message);
        verify(restTemplate, times(1)).exchange(eq(expectedUrl), eq(HttpMethod.POST), any(HttpEntity.class),
            eq(Object.class));
    }
}
