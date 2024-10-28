package greencity.service;

import greencity.ModelUtils;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableDetailedDto;
import greencity.dto.user.UserFilterDto;
import greencity.dto.user.UserManagementVO;
import greencity.dto.user.UserStatusDto;
import greencity.dto.user.UserVO;
import greencity.entity.User;
import greencity.enums.EmailNotification;
import greencity.enums.Role;
import greencity.exception.exceptions.BadUpdateRequestException;
import greencity.exception.exceptions.LowRoleLevelException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.WrongEmailException;
import greencity.exception.exceptions.WrongIdException;
import greencity.enums.UserStatus;
import greencity.mapping.UserManagementVOMapper;
import greencity.repository.UserRepo;
import greencity.repository.options.UserFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import static greencity.ModelUtils.getListUserManagementVO;
import static greencity.ModelUtils.getSortedPageable;
import static greencity.ModelUtils.getUnSortedPageable;
import static greencity.ModelUtils.getUserFilterDto;
import static greencity.ModelUtils.getUserManagementVOPage;
import static greencity.ModelUtils.getUserPage;
import static greencity.ModelUtils.testEmail;
import static greencity.ModelUtils.testEmail2;
import static greencity.ModelUtils.testUser;
import static greencity.ModelUtils.testUserRoleUser;
import static greencity.ModelUtils.testUserStatusDto;
import static greencity.ModelUtils.testUserVo;
import static greencity.ModelUtils.userVORoleUser;
import static greencity.ModelUtils.getUser;
import static greencity.enums.UserStatus.ACTIVATED;
import static greencity.enums.UserStatus.CREATED;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepo userRepo;
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    UserManagementVOMapper userManagementVOMapper;

    private final UserVO userVO = UserVO.builder()
        .id(1L)
        .name("Test Testing")
        .email("test@gmail.com")
        .role(Role.ROLE_USER)
        .userStatus(ACTIVATED)
        .emailNotification(EmailNotification.DISABLED)
        .lastActivityTime(LocalDateTime.of(2020, 10, 10, 20, 10, 10))
        .dateOfRegistration(LocalDateTime.now())
        .socialNetworks(new ArrayList<>())
        .build();

    @Test
    void findByIdTest() {
        Long id = 1L;

        User user = new User();
        user.setId(1L);

        when(userRepo.findById(id)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserVO.class)).thenReturn(userVO);
        assertEquals(userVO, userService.findById(id));
        verify(userRepo, times(1)).findById(id);
    }

    @Test
    void saveTest() {
        userService.save(userVO);
        verify(modelMapper).map(userVO, User.class);
        verify(userRepo).save(any());
    }

    @Test
    void checkIfTheUserIsOnlineExceptionTest() {
        assertThrows(WrongIdException.class, () -> userService.checkIfTheUserIsOnline(null));
    }

    @Test
    void checkIfTheUserIsOnlineEqualsTrueTest() {
        ReflectionTestUtils.setField(userService, "timeAfterLastActivity", 300000);
        Timestamp userLastActivityTime = Timestamp.valueOf(LocalDateTime.now());
        User user = getUser();

        when(userRepo.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepo.findLastActivityTimeById(anyLong())).thenReturn(Optional.of(userLastActivityTime));

        assertTrue(userService.checkIfTheUserIsOnline(1L));
    }

    @Test
    void checkIfTheUserIsOnlineEqualsFalseTest() {
        ReflectionTestUtils.setField(userService, "timeAfterLastActivity", 300000);
        User user = getUser();

        when(userRepo.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepo.findLastActivityTimeById(anyLong())).thenReturn(Optional.empty());

        assertFalse(userService.checkIfTheUserIsOnline(1L));
    }

    @Test
    void getSixFriendsWithTheHighestRatingTest() {
        List<User> friendsList = ModelUtils.getFriendsList();
        User user = User.builder()
            .id(1L)
            .userFriends(friendsList)
            .build();

        userVO.setUserFriends(friendsList.stream()
            .map(friend -> modelMapper.map(friend, UserVO.class))
            .collect(Collectors.toList()));

        when(userRepo.getSixFriendsWithTheHighestRating(user.getId())).thenReturn(user.getUserFriends().stream()
            .sorted((f1, f2) -> f2.getRating().compareTo(f1.getRating()))
            .limit(6)
            .collect(Collectors.toList()));

        assertEquals(userVO.getUserFriends().subList(2, 8),
            userService.getSixFriendsWithTheHighestRating(user.getId()));
    }

    @Test
    void checkUpdatableUserTest() {
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(getUser()));
        when(modelMapper.map(any(), any())).thenReturn(userVO);
        Exception exception = assertThrows(BadUpdateRequestException.class, () -> {
            userService.checkUpdatableUser(1L, "email");
        });
        assertEquals(ErrorMessage.USER_CANT_UPDATE_HIMSELF, exception.getMessage());
    }

    @Test
    void getInitialsByIdTest() {
        when(userRepo.findById(any())).thenReturn(Optional.of(getUser()));
        when(modelMapper.map(any(), any())).thenReturn(userVO);
        assertEquals("TT", userService.getInitialsById(12L));
        userVO.setName("Taras");
        assertEquals("T", userService.getInitialsById(12L));
    }

    @Test
    void testFindByEmail() {
        when(userRepo.findByEmail(testEmail)).thenReturn(Optional.ofNullable(testUser));
        when(modelMapper.map(testUser, UserVO.class)).thenReturn(testUserVo);

        UserVO actual = userService.findByEmail(testEmail);

        assertEquals(testUserVo, actual);

        verify(userRepo).findByEmail(testEmail);
        verify(modelMapper).map(testUser, UserVO.class);
    }

    @Test
    void testFindByEmailThrowException() {
        when(userRepo.findByEmail(testEmail)).thenReturn(Optional.empty());

        assertThrows(WrongIdException.class, () -> userService.findByEmail(testEmail));

        verify(userRepo).findByEmail(testEmail);
    }

    @Test
    void testFindNotDeactivatedByEmail() {
        when(userRepo.findNotDeactivatedByEmail(testEmail))
            .thenReturn(Optional.of(testUser));
        when(modelMapper.map(Optional.of(testUser), UserVO.class))
            .thenReturn(testUserVo);

        Optional<UserVO> actual = userService.findNotDeactivatedByEmail(testEmail);

        assertEquals(Optional.of(testUserVo), actual);
    }

    @Test
    void testFindIdByEmail() {
        when(userRepo.findIdByEmail(testEmail)).thenReturn(Optional.of(1L));

        Long actual = userService.findIdByEmail(testEmail);

        assertEquals(1L, actual);
    }

    @Test
    void testFindIdByEmailThrowsException() {
        when(userRepo.findIdByEmail(testEmail)).thenReturn(Optional.empty());

        assertThrows(WrongEmailException.class,
            () -> userService.findIdByEmail(testEmail));
    }

    @Test
    void testUpdateUserLastActivityTime() {
        Date date = new Date();

        doNothing().when(userRepo).updateUserLastActivityTime(1L, date);

        userService.updateUserLastActivityTime(1L, date);

        verify(userRepo).updateUserLastActivityTime(1L, date);
    }

    @Test
    void testUpdateStatus() {
        when(userRepo.findByEmail(testEmail2)).thenReturn(Optional.ofNullable(testUser));
        when(modelMapper.map(testUser, UserVO.class)).thenReturn(testUserVo);
        when(userRepo.findById(2L)).thenReturn(Optional.ofNullable(testUserRoleUser));
        when(modelMapper.map(testUserRoleUser, UserVO.class)).thenReturn(userVORoleUser);
        doNothing().when(userRepo).updateUserStatus(2L, String.valueOf(UserStatus.CREATED));
        when(modelMapper.map(userVORoleUser, UserStatusDto.class)).thenReturn(testUserStatusDto);

        UserStatusDto actual = userService.updateStatus(2L, CREATED, testEmail2);

        assertEquals(testUserStatusDto, actual);

        verify(userRepo, times(2)).findByEmail(anyString());
        verify(modelMapper, times(4)).map(any(User.class), eq(UserVO.class));
        verify(userRepo, times(2)).findById(anyLong());
        verify(userRepo).updateUserStatus(2L, String.valueOf(CREATED));
        verify(modelMapper).map(userVORoleUser, UserStatusDto.class);
    }

    @Test
    void testUpdateStatusThrowsBadUpdateRequestException() {
        when(userRepo.findByEmail(testEmail)).thenReturn(Optional.ofNullable(testUser));
        when(modelMapper.map(testUser, UserVO.class)).thenReturn(testUserVo);

        assertThrows(BadUpdateRequestException.class,
            () -> userService.updateStatus(1L, CREATED, testEmail));
    }

    @Test
    void testUpdateStatusThrowsLowRoleLevelException() {
        when(userRepo.findByEmail(testEmail)).thenReturn(Optional.ofNullable(testUser));
        when(modelMapper.map(testUser, UserVO.class)).thenReturn(testUserVo);
        when(userRepo.findById(2L)).thenReturn(Optional.ofNullable(testUser));
        when(modelMapper.map(testUser, UserVO.class)).thenReturn(testUserVo);

        assertThrows(LowRoleLevelException.class,
            () -> userService.updateStatus(2L, CREATED, testEmail));
    }

    @Test
    void getAllUsersByCriteriaSortedPageableTest() {
        Page<User> userPage = getUserPage();
        Pageable sortedPageable = getSortedPageable();
        UserFilterDto request = getUserFilterDto();

        List<UserManagementVO> listUserManagementVO = getListUserManagementVO();
        Page<UserManagementVO> userManagementVOPage = getUserManagementVOPage();

        when(userRepo.findAll(any(UserFilter.class), eq(sortedPageable))).thenReturn(userPage);
        when(userManagementVOMapper.mapAllToPage(getUserPage())).thenReturn(userManagementVOPage);

        PageableDetailedDto<UserManagementVO> allUsersByCriteria =
            userService.getAllUsersByCriteria(request, sortedPageable);

        assertTrue(allUsersByCriteria.getPage().containsAll(listUserManagementVO));

        verify(userRepo).findAll(any(UserFilter.class), eq(sortedPageable));
        verify(userManagementVOMapper).mapAllToPage(getUserPage());
    }

    @Test
    void getAllUsersByCriteriaUnsortedPageableTest() {
        Page<User> userPage = getUserPage();
        Pageable unsortedPageable = getUnSortedPageable();
        Pageable sortedPageable = getSortedPageable();
        UserFilterDto request = getUserFilterDto();

        List<UserManagementVO> listUserManagementVO = getListUserManagementVO();
        Page<UserManagementVO> userManagementVOPage = getUserManagementVOPage();

        when(userRepo.findAll(any(UserFilter.class), eq(sortedPageable))).thenReturn(userPage);
        when(userManagementVOMapper.mapAllToPage(getUserPage())).thenReturn(userManagementVOPage);

        PageableDetailedDto<UserManagementVO> allUsersByCriteria =
            userService.getAllUsersByCriteria(request, unsortedPageable);

        assertTrue(allUsersByCriteria.getPage().containsAll(listUserManagementVO));

        verify(userRepo).findAll(any(UserFilter.class), eq(sortedPageable));
        verify(userManagementVOMapper).mapAllToPage(getUserPage());
    }

    @Test
    void updateUserRatingTest() {
        User user = getUser();
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepo).updateUserRating(1L, 6.0d);

        userService.updateUserRating(1L, 6.0d);

        verify(userRepo).findById(1L);
        verify(userRepo).updateUserRating(1L, 6.0d);
    }

    @Test
    void updateUserRatingsThrowsNotFoundExceptionTest() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.updateUserRating(1L, 6.0d));
        verify(userRepo).findById(1L);
    }

    @Test
    void findByEmailsTest() {
        List<String> emails = List.of("email@gmail.com", "gmail@gmail.com");

        when(userRepo.findAllByEmailIn(emails)).thenReturn(List.of(getUser(), getUser()));
        when(modelMapper.map(getUser(), UserVO.class)).thenReturn(userVO);

        assertEquals(List.of(userVO, userVO), userService.findByEmails(emails));

        verify(userRepo).findAllByEmailIn(emails);
    }
}