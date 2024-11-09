package greencity.service;

import greencity.ModelUtils;
import greencity.achievement.AchievementCalculation;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.habit.HabitAssignCustomPropertiesDto;
import greencity.dto.habit.HabitAssignDto;
import greencity.dto.habit.HabitAssignManagementDto;
import greencity.dto.habit.HabitAssignPreviewDto;
import greencity.dto.habit.HabitAssignPropertiesDto;
import greencity.dto.habit.HabitAssignStatDto;
import greencity.dto.habit.HabitAssignVO;
import greencity.dto.habit.HabitDto;
import greencity.dto.habit.HabitVO;
import greencity.dto.habit.HabitWorkingDaysDto;
import greencity.dto.habit.HabitsDateEnrollmentDto;
import greencity.dto.habit.UserToDoAndCustomToDoListsDto;
import greencity.dto.habitstatuscalendar.HabitStatusCalendarVO;
import greencity.dto.todolistitem.BulkSaveCustomToDoListItemDto;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.todolistitem.CustomToDoListItemSaveRequestDto;
import greencity.dto.todolistitem.ToDoListItemDto;
import greencity.dto.todolistitem.ToDoListItemWithStatusRequestDto;
import greencity.dto.user.UserToDoListItemResponseDto;
import greencity.dto.user.UserVO;
import greencity.entity.CustomToDoListItem;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.HabitStatusCalendar;
import greencity.entity.HabitTranslation;
import greencity.entity.Language;
import greencity.entity.ToDoListItem;
import greencity.entity.User;
import greencity.entity.UserToDoListItem;
import greencity.entity.RatingPoints;
import greencity.entity.localization.ToDoListItemTranslation;
import greencity.enums.HabitAssignStatus;
import greencity.enums.ToDoListItemStatus;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.CustomToDoListItemNotSavedException;
import greencity.exception.exceptions.InvalidStatusException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserAlreadyHasEnrolledHabitAssign;
import greencity.exception.exceptions.UserAlreadyHasHabitAssignedException;
import greencity.exception.exceptions.UserAlreadyHasMaxNumberOfActiveHabitAssigns;
import greencity.exception.exceptions.UserHasNoFriendWithIdException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.exception.exceptions.UserHasReachedOutOfEnrollRange;
import greencity.rating.RatingCalculation;
import greencity.repository.CustomToDoListItemRepo;
import greencity.repository.HabitAssignRepo;
import greencity.repository.HabitRepo;
import greencity.repository.HabitStatusCalendarRepo;
import greencity.repository.ToDoListItemRepo;
import greencity.repository.ToDoListItemTranslationRepo;
import greencity.repository.UserRepo;
import greencity.repository.UserToDoListItemRepo;
import greencity.repository.RatingPointsRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static greencity.ModelUtils.habitAssignInProgress;
import static greencity.ModelUtils.getFullHabitAssign;
import static greencity.ModelUtils.getFullHabitAssignDto;
import static greencity.ModelUtils.getHabitAssign;
import static greencity.ModelUtils.getHabitAssignUserDurationDto;
import static greencity.ModelUtils.getHabitDto;
import static greencity.ModelUtils.getHabitTranslation;
import static greencity.ModelUtils.getHabitsDateEnrollmentDtos;
import static greencity.ModelUtils.getToDoListItemTranslationList;
import static greencity.ModelUtils.getUser;
import static greencity.ModelUtils.getUserToDoListItem;
import static greencity.ModelUtils.getUserVO;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HabitAssignServiceImplTest {
    @Mock
    private HabitRepo habitRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private HabitAssignRepo habitAssignRepo;
    @Mock
    private ToDoListItemRepo toDoListItemRepo;
    @Mock
    private UserToDoListItemRepo userToDoListItemRepo;
    @Mock
    private CustomToDoListItemRepo customToDoListItemRepo;
    @Mock
    private HabitStatusCalendarRepo habitStatusCalendarRepo;
    @Mock
    private RatingPointsRepo ratingPointsRepo;
    @Mock
    private HabitStatusCalendarService habitStatusCalendarService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private HabitStatisticService habitStatisticService;
    @Mock
    private ToDoListItemTranslationRepo toDoListItemTranslationRepo;
    @InjectMocks
    private HabitAssignServiceImpl habitAssignService;
    @Mock
    private ToDoListItemService toDoListItemService;
    @Mock
    private CustomToDoListItemService customToDoListItemService;
    @Mock
    private UserService userService;
    @Mock
    UserNotificationService userNotificationService;

    @Mock
    private RatingCalculation ratingCalculation;

    @Mock
    private AchievementCalculation achievementCalculation;

    private static final ZonedDateTime zonedDateTime = ZonedDateTime.now();

    private HabitDto habitDto = HabitDto.builder().id(1L).build();

    private HabitAssignDto habitAssignDto = HabitAssignDto.builder().id(1L)
        .userId(1L)
        .status(HabitAssignStatus.ACQUIRED)
        .habit(ModelUtils.getHabitDto())
        .createDateTime(zonedDateTime).habit(habitDto).build();

    private Habit habit = ModelUtils.getHabit();

    private HabitAssignManagementDto habitAssignManagementDto = HabitAssignManagementDto.builder()
        .id(1L)
        .createDateTime(zonedDateTime).habitId(habit.getId()).build();

    private UserVO userVO = UserVO.builder().id(1L).build();

    private User user = User.builder().id(1L).build();

    private final HabitAssign habitAssign = getHabitAssign();

    private HabitAssign fullHabitAssign = getFullHabitAssign();

    private HabitAssignStatDto habitAssignStatDto = HabitAssignStatDto.builder()
        .status(HabitAssignStatus.ACQUIRED).build();

    private List<HabitAssignDto> habitAssignDtos = Collections.singletonList(habitAssignDto);

    private List<HabitAssign> habitAssigns = Collections.singletonList(habitAssign);

    private List<HabitAssign> fullHabitAssigns = Collections.singletonList(fullHabitAssign);

    private HabitAssignPropertiesDto habitAssignPropertiesDto =
        HabitAssignPropertiesDto.builder().duration(14).defaultToDoListItems(List.of(1L)).isPrivate(false).build();

    private HabitAssignCustomPropertiesDto habitAssignCustomPropertiesDto =
        HabitAssignCustomPropertiesDto.builder()
            .habitAssignPropertiesDto(habitAssignPropertiesDto)
            .friendsIdsList(List.of())
            .build();

    private HabitAssignCustomPropertiesDto habitAssignCustomPropertiesDtoWithCustomToDoListItem =
        HabitAssignCustomPropertiesDto.builder()
            .habitAssignPropertiesDto(habitAssignPropertiesDto)
            .friendsIdsList(List.of())
            .customToDoListItemList(List.of(ModelUtils.getCustomToDoListItemSaveRequestDto()))
            .build();

    private final String language = AppConstant.DEFAULT_LANGUAGE_CODE;

    @Test
    void getByHabitAssignIdAndUserIdThrowsNotFoundExceptionWhenHabitAssignNotExists() {
        Long habitAssignId = 2L;
        Long userId = 3L;

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> habitAssignService.getByHabitAssignIdAndUserId(habitAssignId, userId, language));

        verify(habitAssignRepo).findById(habitAssignId);
    }

    @Test
    void getByHabitAssignIdAndUserIdThrowsUserHasNoPermissionToAccessExceptionWhenHabitAssignNotBelongToUser() {
        long habitAssignId = 2L;
        long userId = 3L;

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId + 1);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));

        assertThrows(UserHasNoPermissionToAccessException.class,
            () -> habitAssignService.getByHabitAssignIdAndUserId(habitAssignId, userId, language));

        verify(habitAssignRepo).findById(habitAssignId);
    }

    @Test
    void assignDefaultHabitForUser() {
        when(habitAssignRepo.findAllByUserId(userVO.getId())).thenReturn(List.of(habitAssign));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(habitAssignRepo.save(any())).thenReturn(habitAssign);
        when(modelMapper.map(habitAssign, HabitAssignManagementDto.class)).thenReturn(habitAssignManagementDto);
        HabitAssignManagementDto actual = habitAssignService.assignDefaultHabitForUser(habit.getId(), userVO);
        assertEquals(habitAssignManagementDto, actual);
    }

    @Test
    void assignDefaultHabitForUserWithEmptyToDoList() {
        when(habitAssignRepo.findAllByUserId(userVO.getId())).thenReturn(List.of(habitAssign));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(habitAssignRepo.save(any())).thenReturn(habitAssign);
        when(modelMapper.map(habitAssign, HabitAssignManagementDto.class)).thenReturn(habitAssignManagementDto);
        when(toDoListItemRepo.getAllToDoListItemIdByHabitIdISContained(habit.getId()))
                .thenReturn(Collections.emptyList());

        HabitAssignManagementDto actual = habitAssignService.assignDefaultHabitForUser(habit.getId(), userVO);
        assertEquals(habitAssignManagementDto, actual);
        verify(toDoListItemRepo, never()).getToDoListByListOfId(any());
    }

    @Test
    void assignDefaultHabitForUserWithNotEmptyToDoList() {
        when(habitAssignRepo.findAllByUserId(userVO.getId())).thenReturn(List.of(habitAssign));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(habitAssignRepo.save(any())).thenReturn(habitAssign);
        when(modelMapper.map(habitAssign, HabitAssignManagementDto.class)).thenReturn(habitAssignManagementDto);
        when(toDoListItemRepo.getAllToDoListItemIdByHabitIdISContained(anyLong()))
                .thenReturn(Arrays.asList(2L, 3L, 4L));
        HabitAssignManagementDto actual = habitAssignService.assignDefaultHabitForUser(habit.getId(), userVO);
        assertEquals(habitAssignManagementDto, actual);
        verify(toDoListItemRepo).getToDoListByListOfId(any());
    }

    @Test
    void assignDefaultHabitForUserThatWasCancelled() {
        HabitAssign assign = getHabitAssign();
        assign.setStatus(HabitAssignStatus.CANCELLED);

        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(habitAssignRepo.findAllByUserId(user.getId()))
            .thenReturn(List.of(assign));
        when(habitAssignRepo.countHabitAssignsByUserIdAndAcquiredFalseAndCancelledFalse(user.getId())).thenReturn(3);
        when(habitAssignRepo.findByHabitIdAndUserIdAndCreateDate(
            eq(habit.getId()),
            eq(user.getId()),
            any(ZonedDateTime.class))).thenReturn(Optional.empty());
        when(habitAssignRepo.findByHabitIdAndUserIdAndStatusIsCancelledOrRequested(habit.getId(), user.getId()))
            .thenReturn(assign);
        when(habitAssignRepo.save(assign)).thenReturn(assign);
        when(modelMapper.map(assign, HabitAssignManagementDto.class)).thenReturn(habitAssignManagementDto);

        HabitAssignManagementDto actual = habitAssignService.assignDefaultHabitForUser(habit.getId(), userVO);
        assertEquals(habitAssignManagementDto, actual);

        verify(habitRepo).findById(habit.getId());
        verify(modelMapper).map(userVO, User.class);
        verify(habitAssignRepo).findAllByUserId(user.getId());
        verify(habitAssignRepo).countHabitAssignsByUserIdAndAcquiredFalseAndCancelledFalse(user.getId());
        verify(habitAssignRepo).findByHabitIdAndUserIdAndCreateDate(
            eq(habit.getId()),
            eq(user.getId()),
            any(ZonedDateTime.class));
        verify(habitAssignRepo).findByHabitIdAndUserIdAndStatusIsCancelledOrRequested(habit.getId(), user.getId());
        verify(habitAssignRepo).save(assign);
        verify(modelMapper).map(assign, HabitAssignManagementDto.class);
    }

    @Test
    void assignDefaultHabitForUserAlreadyHasTheHabit() {
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(habitAssignRepo.findAllByUserId(userVO.getId())).thenReturn(List.of(habitAssignInProgress));

        assertThrows(UserAlreadyHasHabitAssignedException.class,
                () -> habitAssignService.assignDefaultHabitForUser(1L, userVO));
    }

    @Test
    void assignDefaultHabitForUserAlreadyHasMaxQTYHabits() {
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(habitAssignRepo.countHabitAssignsByUserIdAndAcquiredFalseAndCancelledFalse(
                user.getId()))
                .thenReturn(10);
        assertThrows(UserAlreadyHasMaxNumberOfActiveHabitAssigns.class,
                () -> habitAssignService.assignDefaultHabitForUser(1L, userVO));
    }

    @Test
    void assignDefaultHabitForUserAlreadyHasAssignedForCurrentDay() {
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(habitAssignRepo.findByHabitIdAndUserIdAndCreateDate(any(), any(), any()))
                .thenReturn(Optional.of(habitAssign));
        assertThrows(UserAlreadyHasHabitAssignedException.class,
                () -> habitAssignService.assignDefaultHabitForUser(1L, userVO));
    }

    @Test
    void assignCustomHabitForUserThatWasCancelled() {
        habitAssign.setStatus(HabitAssignStatus.CANCELLED);

        when(habitAssignRepo.save(any())).thenReturn(habitAssign);
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(habitAssignRepo.findByHabitIdAndUserIdAndStatusIsCancelledOrRequested(habit.getId(), user.getId()))
            .thenReturn(habitAssign);
        when(modelMapper.map(habitAssign, HabitAssignManagementDto.class)).thenReturn(habitAssignManagementDto);

        List<HabitAssignManagementDto> actual = habitAssignService.assignCustomHabitForUser(habit.getId(), userVO,
            habitAssignCustomPropertiesDto);
        assertEquals(List.of(habitAssignManagementDto), actual);
    }

    @Test
    void assignCustomHabitForUserWithFriend() {
        User userFriend1 = User.builder().id(3L).build();

        UserVO userVO1 = UserVO.builder().id(1L).build();

        User user1 = User.builder().id(1L).userFriends(List.of(userFriend1)).build();

        HabitAssignCustomPropertiesDto habitAssignCustomPropertiesDtoWithFriend =
            HabitAssignCustomPropertiesDto.builder()
                .habitAssignPropertiesDto(habitAssignPropertiesDto)
                .friendsIdsList(List.of(3L))
                .build();

        when(habitAssignRepo.findAllByUserId(userVO1.getId())).thenReturn(List.of(habitAssign));
        when(modelMapper.map(userVO1, User.class)).thenReturn(user1);
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(habitAssignRepo.save(any())).thenReturn(habitAssign);
        when(modelMapper.map(habitAssign, HabitAssignManagementDto.class)).thenReturn(habitAssignManagementDto);
        when(userRepo.findById(userFriend1.getId())).thenReturn(Optional.of(userFriend1));
        when(userRepo.isFriend(user1.getId(), userFriend1.getId())).thenReturn(true);

        List<HabitAssignManagementDto> actual = habitAssignService
            .assignCustomHabitForUser(habit.getId(), userVO1, habitAssignCustomPropertiesDtoWithFriend);

        assertEquals(List.of(habitAssignManagementDto, habitAssignManagementDto), actual);
    }

    @Test
    void assignCustomHabitForUserWithFriendNullHabitAssign() {
        UserVO userVO1 = UserVO.builder().id(1L).build();

        User user1 = User.builder().id(1L).build();

        HabitAssignCustomPropertiesDto habitAssignCustomPropertiesDtoWithFriend =
            HabitAssignCustomPropertiesDto.builder()
                .habitAssignPropertiesDto(habitAssignPropertiesDto)
                .build();

        when(habitAssignRepo.findAllByUserId(userVO1.getId())).thenReturn(List.of(habitAssign));
        when(modelMapper.map(userVO1, User.class)).thenReturn(user1);
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(habitAssignRepo.save(any())).thenReturn(habitAssign);
        when(modelMapper.map(habitAssign, HabitAssignManagementDto.class)).thenReturn(habitAssignManagementDto);

        List<HabitAssignManagementDto> actual = habitAssignService
            .assignCustomHabitForUser(habit.getId(), userVO1, habitAssignCustomPropertiesDtoWithFriend);

        assertEquals(List.of(habitAssignManagementDto), actual);
        verify(userRepo, never()).isFriend(anyLong(), anyLong());
        verify(userRepo, never()).findById(anyLong());
        verify(habitAssignRepo).save(any(HabitAssign.class));
    }

    @Test
    void assignCustomHabitForUserThrowsNotFoundException() {
        HabitAssignCustomPropertiesDto habitAssignCustomPropertiesDtoWithFriend =
            HabitAssignCustomPropertiesDto.builder()
                .habitAssignPropertiesDto(habitAssignPropertiesDto)
                .friendsIdsList(List.of(3L))
                .build();
        when(habitAssignRepo.findAllByUserId(userVO.getId())).thenReturn(List.of(habitAssign));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(habitAssignRepo.save(any())).thenReturn(habitAssign);
        when(modelMapper.map(habitAssign, HabitAssignManagementDto.class)).thenReturn(habitAssignManagementDto);

        assertThrows(NotFoundException.class, () -> habitAssignService
            .assignCustomHabitForUser(1L, userVO, habitAssignCustomPropertiesDtoWithFriend));
    }

    @Test
    void assignCustomHabitForUserThrowsUserHasNoFriendWithIdException() {

        User userFriend1 = User.builder().id(3L).build();

        HabitAssignCustomPropertiesDto habitAssignCustomPropertiesDtoWithFriend =
            HabitAssignCustomPropertiesDto.builder()
                .habitAssignPropertiesDto(habitAssignPropertiesDto)
                .friendsIdsList(List.of(3L))
                .build();
        when(habitAssignRepo.findAllByUserId(userVO.getId())).thenReturn(List.of(habitAssign));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(habitAssignRepo.save(any())).thenReturn(habitAssign);
        when(modelMapper.map(habitAssign, HabitAssignManagementDto.class)).thenReturn(habitAssignManagementDto);
        when(userRepo.findById(userFriend1.getId())).thenReturn(Optional.of(userFriend1));

        assertThrows(UserHasNoFriendWithIdException.class,
            () -> habitAssignService.assignCustomHabitForUser(1L, userVO, habitAssignCustomPropertiesDtoWithFriend));
    }

    @Test
    void assignCustomHabitForUser() {
        when(habitAssignRepo.findAllByUserId(userVO.getId())).thenReturn(List.of(habitAssign));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(habitAssignRepo.save(any())).thenReturn(habitAssign);
        when(modelMapper.map(habitAssign, HabitAssignManagementDto.class)).thenReturn(habitAssignManagementDto);
        List<HabitAssignManagementDto> actual = habitAssignService
                .assignCustomHabitForUser(habit.getId(), userVO, habitAssignCustomPropertiesDto);
        assertEquals(List.of(habitAssignManagementDto), actual);
    }

    @Test
    void assignCustomHabitForUserWithCustomToDoListItemList() {
        user.setCustomToDoListItems(new ArrayList<>());
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(habitAssignRepo.findAllByUserId(userVO.getId())).thenReturn(List.of(habitAssign));
        when(customToDoListItemRepo.save(any())).thenReturn(ModelUtils.getCustomToDoListItem());
        when(modelMapper.map(ModelUtils.getCustomToDoListItemSaveRequestDto(), CustomToDoListItem.class))
            .thenReturn(ModelUtils.getCustomToDoListItem());

        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(habitAssignRepo.save(any())).thenReturn(habitAssign);
        when(modelMapper.map(habitAssign, HabitAssignManagementDto.class)).thenReturn(habitAssignManagementDto);
        List<HabitAssignManagementDto> actual = habitAssignService.assignCustomHabitForUser(habit.getId(), userVO,
            habitAssignCustomPropertiesDtoWithCustomToDoListItem);

        assertEquals(List.of(habitAssignManagementDto), actual);

        verify(modelMapper).map(userVO, User.class);
        verify(habitAssignRepo).findAllByUserId(userVO.getId());
        verify(customToDoListItemRepo).save(any());
        verify(modelMapper).map(ModelUtils.getCustomToDoListItemSaveRequestDto(), CustomToDoListItem.class);
        verify(habitRepo).findById(habit.getId());
        verify(modelMapper).map(habitAssign, HabitAssignManagementDto.class);
        verify(habitAssignRepo).save(any());
    }

    @Test
    void assignCustomHabitForUserThrowsCustomToDoListItemNotSavedException() {
        user.setCustomToDoListItems(List.of(ModelUtils.getCustomToDoListItem()));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(habitAssignRepo.findAllByUserId(userVO.getId())).thenReturn(List.of(habitAssign));
        when(modelMapper.map(ModelUtils.getCustomToDoListItemSaveRequestDto(), CustomToDoListItem.class))
            .thenReturn(ModelUtils.getCustomToDoListItem());
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));

        String expectedErrorMessage = String.format(ErrorMessage.CUSTOM_TO_DO_LIST_ITEM_EXISTS,
            ModelUtils.getCustomToDoListItem().getText());

        CustomToDoListItemNotSavedException exception = assertThrows(CustomToDoListItemNotSavedException.class,
            () -> habitAssignService.assignCustomHabitForUser(1L, userVO,
                habitAssignCustomPropertiesDtoWithCustomToDoListItem));
        System.out.println(exception.getMessage());
        assertEquals(expectedErrorMessage, exception.getMessage());

        verify(modelMapper).map(userVO, User.class);
        verify(habitAssignRepo).findAllByUserId(userVO.getId());
        verify(modelMapper).map(ModelUtils.getCustomToDoListItemSaveRequestDto(), CustomToDoListItem.class);
        verify(habitRepo).findById(habit.getId());
    }

    @Test
    void getEndDate() {
        HabitAssignDto habitAssignDuration2 = ModelUtils.getHabitAssignDto();
        habitAssignDuration2.setDuration(2);
        ZonedDateTime expected = habitAssignDuration2.getCreateDateTime().plusDays(habitAssignDuration2.getDuration());
        assertEquals(expected, habitAssignService.getEndDate(habitAssignDuration2));
    }

    @Test
    void findHabitAssignsBetweenDatesTest() {
        HabitAssign habitForCurrentUser = ModelUtils.getHabitAssignForCurrentUser();

        HabitAssign additionalHabit = ModelUtils.getAdditionalHabitAssignForCurrentUser();
        List<HabitAssign> habitAssignsList = Arrays.asList(habitForCurrentUser, additionalHabit);

        List<HabitsDateEnrollmentDto> dtos = getHabitsDateEnrollmentDtos();

        when(habitAssignRepo.findAllInProgressHabitAssignsRelatedToUser(1L))
            .thenReturn(habitAssignsList);

        assertEquals(dtos, habitAssignService.findHabitAssignsBetweenDates(
            1L,
            LocalDate.of(2020, 12, 27),
            LocalDate.of(2020, 12, 29),
            "en"));

        verify(habitAssignRepo).findAllInProgressHabitAssignsRelatedToUser(anyLong());
    }

    @Test
    void findHabitAssignsBetweenDatesWhenStartDateIsEarlierThanFromTest() {
        HabitAssign habitForCurrentUser = ModelUtils.getHabitAssignForCurrentUser();
        habitForCurrentUser.setCreateDate(ZonedDateTime.of(
            2010,
            12,
            28,
            12,
            12,
            12,
            12, ZoneId.of("Europe/Kiev")));

        HabitAssign additionalHabit = ModelUtils.getAdditionalHabitAssignForCurrentUser();
        additionalHabit.setCreateDate(ZonedDateTime.of(
            2010,
            12,
            28,
            12,
            12,
            12,
            12, ZoneId.of("Europe/Kiev")));

        List<HabitAssign> habitAssignsList = Arrays.asList(habitForCurrentUser, additionalHabit);

        when(habitAssignRepo.findAllInProgressHabitAssignsRelatedToUser(1L))
            .thenReturn(habitAssignsList);

        List<HabitsDateEnrollmentDto> habitsDateEnrollmentDtos = habitAssignService.findHabitAssignsBetweenDates(
            1L,
            LocalDate.of(2020, 12, 27),
            LocalDate.of(2020, 12, 29),
            "en");

        assertEquals(Collections.emptyList(), habitsDateEnrollmentDtos.getFirst().getHabitAssigns());
        verify(habitAssignRepo).findAllInProgressHabitAssignsRelatedToUser(anyLong());
    }

    @Test
    void findHabitAssignsBetweenDatesWhenStartDateIsLaterThanFromTest() {
        HabitAssign habitForCurrentUser = ModelUtils.getHabitAssignForCurrentUser();
        habitForCurrentUser.setCreateDate(ZonedDateTime.of(
            2040,
            12,
            28,
            12,
            12,
            12,
            12, ZoneId.of("Europe/Kiev")));

        HabitAssign additionalHabit = ModelUtils.getAdditionalHabitAssignForCurrentUser();
        additionalHabit.setCreateDate(ZonedDateTime.of(
            2040,
            12,
            28,
            12,
            12,
            12,
            12, ZoneId.of("Europe/Kiev")));

        List<HabitAssign> habitAssignsList = Arrays.asList(habitForCurrentUser, additionalHabit);

        when(habitAssignRepo.findAllInProgressHabitAssignsRelatedToUser(1L))
            .thenReturn(habitAssignsList);

        List<HabitsDateEnrollmentDto> habitsDateEnrollmentDtos = habitAssignService.findHabitAssignsBetweenDates(
            1L,
            LocalDate.of(2020, 12, 27),
            LocalDate.of(2020, 12, 29),
            "en");

        assertEquals(Collections.emptyList(), habitsDateEnrollmentDtos.getFirst().getHabitAssigns());
        verify(habitAssignRepo).findAllInProgressHabitAssignsRelatedToUser(anyLong());
    }

    @Test
    void findHabitAssignsBetweenDatesThrowsBadRequestExceptionWhenFromDateIsLaterThenTo() {
        Long userId = 2L;
        LocalDate from = LocalDate.now();
        LocalDate to = from.minusDays(1);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> habitAssignService.findHabitAssignsBetweenDates(userId, from, to, language));

        assertEquals(ErrorMessage.INVALID_DATE_RANGE, exception.getMessage());

        verify(habitAssignRepo, times(0)).findAllInProgressHabitAssignsRelatedToUser(anyLong());
    }

    @Test
    void updateStatusByHabitAssignId() {
        when(habitAssignRepo.findById(1L)).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(habitAssignRepo.save(habitAssign), HabitAssignManagementDto.class))
                .thenReturn(habitAssignManagementDto);
        assertEquals(habitAssignManagementDto,
                habitAssignService.updateStatusByHabitAssignId(1L, habitAssignStatDto));
    }

    @Test
    void unenrollHabit() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        LocalDate date = LocalDate.now();

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId);

        HabitStatusCalendar habitStatusCalendar = ModelUtils.getHabitStatusCalendar();
        habitStatusCalendar.setEnrollDate(date);

        List<HabitStatusCalendar> list = new ArrayList<>();
        list.add(habitStatusCalendar);
        habitAssign.setHabitStatusCalendars(list);
        RatingPoints ratingPoints =
            RatingPoints.builder().id(1L).name("UNDO_DAYS_OF_HABIT_IN_PROGRESS").points(-1).build();

        when(ratingPointsRepo.findByNameOrThrow("UNDO_DAYS_OF_HABIT_IN_PROGRESS")).thenReturn(ratingPoints);
        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));
        when(habitStatusCalendarRepo.findHabitStatusCalendarByEnrollDateAndHabitAssign(date, habitAssign))
            .thenReturn(habitStatusCalendar);
        when(userService.findById(any())).thenReturn(getUserVO());

        habitAssignService.unenrollHabit(habitAssignId, userId, date);
        assertEquals(0, habitAssign.getHabitStatusCalendars().size());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(habitStatusCalendarRepo).findHabitStatusCalendarByEnrollDateAndHabitAssign(date, habitAssign);
        verify(habitStatusCalendarRepo).delete(habitStatusCalendar);
        verify(habitAssignRepo).save(habitAssign);
    }

    @Test
    void unenrollHabitThrowsNotFoundExceptionWhenHabitAssignNotExists() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        LocalDate date = LocalDate.now();

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.empty());

        NotFoundException exception =
            assertThrows(NotFoundException.class, () -> habitAssignService.unenrollHabit(habitAssignId, userId, date));

        assertEquals(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(habitStatusCalendarRepo, times(0)).findHabitStatusCalendarByEnrollDateAndHabitAssign(any(), any());
        verify(habitStatusCalendarRepo, times(0)).delete(any());
        verify(habitAssignRepo, times(0)).save(any());
    }

    @Test
    void unenrollHabitThrowsUserHasNoPermissionToAccessExceptionWhenHabitAssignNotBelongToUser() {
        long habitAssignId = 2L;
        long userId = 3L;
        LocalDate date = LocalDate.now();

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId + 1);

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));

        UserHasNoPermissionToAccessException exception =
            assertThrows(UserHasNoPermissionToAccessException.class,
                () -> habitAssignService.unenrollHabit(habitAssignId, userId, date));

        assertEquals(ErrorMessage.USER_HAS_NO_PERMISSION, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(habitStatusCalendarRepo, times(0)).findHabitStatusCalendarByEnrollDateAndHabitAssign(any(), any());
        verify(habitStatusCalendarRepo, times(0)).delete(any());
        verify(habitAssignRepo, times(0)).save(any());
    }

    @Test
    void unenrollHabitThrowsNotFoundExceptionWhenHabitNotEnrolled() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        LocalDate date = LocalDate.now();

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId);

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));
        when(habitStatusCalendarRepo.findHabitStatusCalendarByEnrollDateAndHabitAssign(date, habitAssign))
            .thenReturn(null);

        NotFoundException exception =
            assertThrows(NotFoundException.class, () -> habitAssignService.unenrollHabit(habitAssignId, userId, date));

        assertEquals(ErrorMessage.HABIT_IS_NOT_ENROLLED_ON_CURRENT_DATE + date, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(habitStatusCalendarRepo).findHabitStatusCalendarByEnrollDateAndHabitAssign(date, habitAssign);
        verify(habitStatusCalendarRepo, times(0)).delete(any());
        verify(habitAssignRepo, times(0)).save(any());
    }

    @Test
    void deleteHabitAssign() {
        Long habitId = 1L;
        Long habitAssignId = 1L;
        Long userId = 2L;
        HabitAssign habitAssignForDelete = ModelUtils.getHabitAssign();
        habitAssignForDelete.getUser().setId(userId);
        habitAssignForDelete.setWorkingDays(10);
        RatingPoints ratingPoints =
            RatingPoints.builder().id(1L).name("UNDO_DAYS_OF_HABIT_IN_PROGRESS").points(-1).build();

        when(ratingPointsRepo.findByNameOrThrow("UNDO_DAYS_OF_HABIT_IN_PROGRESS")).thenReturn(ratingPoints);
        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssignForDelete));
        when(userService.findById(any())).thenReturn(getUserVO());

        habitAssignService.deleteHabitAssign(habitAssignId, userId);

        verify(userToDoListItemRepo).deleteToDoListItemsByHabitAssignId(habitAssignId);
        verify(customToDoListItemRepo).deleteCustomToDoListItemsByHabitId(habitId);
        verify(habitAssignRepo).delete(habitAssignForDelete);
    }

    @Test
    void deleteHabitAssignThrowsExceptionWhenHabitAssignNotExists() {
        Long habitAssignId = 1L;
        Long userId = 2L;

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.empty());

        NotFoundException exception =
            assertThrows(NotFoundException.class, () -> habitAssignService.deleteHabitAssign(habitAssignId, userId));

        assertEquals(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId, exception.getMessage());

        verify(userToDoListItemRepo, times(0)).deleteToDoListItemsByHabitAssignId(anyLong());
        verify(customToDoListItemRepo, times(0)).deleteCustomToDoListItemsByHabitId(anyLong());
        verify(habitAssignRepo, times(0)).delete(any(HabitAssign.class));
    }

    @Test
    void deleteHabitAssignThrowsExceptionWhenHabitAssignNotBelongsToUser() {
        long habitAssignId = 1L;
        long userId = 2L;

        habitAssign.getUser().setId(userId + 1);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));

        UserHasNoPermissionToAccessException exception =
            assertThrows(UserHasNoPermissionToAccessException.class,
                () -> habitAssignService.deleteHabitAssign(habitAssignId, userId));

        assertEquals(ErrorMessage.USER_HAS_NO_PERMISSION, exception.getMessage());

        verify(userToDoListItemRepo, times(0)).deleteToDoListItemsByHabitAssignId(anyLong());
        verify(customToDoListItemRepo, times(0)).deleteCustomToDoListItemsByHabitId(anyLong());
        verify(habitAssignRepo, times(0)).delete(any(HabitAssign.class));
    }

    @Test
    void getAllHabitAssignsByHabitIdAndStatusNotCancelled() {
        Long habitId = 1L;

        Language languageEn = ModelUtils.getLanguage();

        HabitTranslation translation = ModelUtils.getHabitTranslation();
        translation.setLanguage(languageEn);

        HabitAssign habitAssignNotCancelled = ModelUtils.getHabitAssign();

        habitAssignNotCancelled.setHabit(habit);

        habit.setHabitTranslations(Collections.singletonList(translation));

        when(habitAssignRepo.findAllByHabitId(habitId)).thenReturn(Collections.singletonList(habitAssignNotCancelled));
        when(modelMapper.map(habitAssignNotCancelled, HabitAssignDto.class)).thenReturn(habitAssignDto);
        when(modelMapper.map(translation, HabitDto.class)).thenReturn(habitDto);
        HabitAssignDto actual =
            habitAssignService.getAllHabitAssignsByHabitIdAndStatusNotCancelled(habitId, languageEn.getCode()).get(0);

        assertEquals(habitAssignDto, actual);

    }

    @Test
    void getNumberHabitAssignsByHabitIdAndStatusTest() {
        Long habitId = 1L;

        Language languageEn = ModelUtils.getLanguage();

        HabitTranslation translation = ModelUtils.getHabitTranslation();
        translation.setLanguage(languageEn);

        List<HabitAssign> habitAssignList = Collections.singletonList(ModelUtils.getHabitAssign());

        when(habitAssignRepo.findAllHabitAssignsByStatusAndHabitId(HabitAssignStatus.ACQUIRED, habitId))
            .thenReturn(habitAssignList);

        Long actual = habitAssignService.getNumberHabitAssignsByHabitIdAndStatus(habitId, HabitAssignStatus.ACQUIRED);

        assertEquals(habitAssignList.size(), actual);

    }

    @Test
    void deleteAllHabitAssignsByHabit() {
        HabitVO habitVO = ModelUtils.getHabitVO();
        HabitAssignVO habitAssignVO = ModelUtils.getHabitAssignVO();

        when(habitAssignRepo.findAllByHabitId(any())).thenReturn(Collections.singletonList(habitAssign));
        when(modelMapper.map(habitAssign, HabitAssignVO.class)).thenReturn(habitAssignVO);
        habitAssignService.deleteAllHabitAssignsByHabit(habitVO);

        verify(habitStatisticService).deleteAllStatsByHabitAssign(habitAssignVO);
        verify(habitAssignRepo, times(1)).delete(habitAssign);
    }

    @Test
    void buildHabitAssignDtoContent() {
        when(habitAssignRepo.findByHabitIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(habitAssign,
                HabitAssignDto.class)).thenReturn(habitAssignDto);
        HabitTranslation habitTranslation = habitAssign.getHabit().getHabitTranslations().stream().findFirst().get();
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(ModelUtils.getHabitDto());
        assertEquals(habitAssignDto, habitAssignService.findHabitAssignByUserIdAndHabitId(1L, 1L, "en"));
    }

    @Test
    void getAllHabitAssignsByUserIdAndStatusNotCancelled() {
        UserToDoListItem userToDoListItemCustom = ModelUtils.getFullUserToDoListItem();
        HabitAssignDto habitAssignDtoCustom = ModelUtils.getHabitAssignDtoWithFriendsIds();
        List<HabitAssignDto> expected = List.of(habitAssignDtoCustom);

        when(habitAssignRepo.findAllByUserId(1L)).thenReturn(habitAssigns);
        when(modelMapper.map(habitAssign, HabitAssignDto.class)).thenReturn(habitAssignDtoCustom);
        when(userToDoListItemRepo.getAllAssignedToDoListItemsFull(any()))
            .thenReturn(List.of(userToDoListItemCustom));
        when(habitAssignRepo.findFriendsIdsTrackingHabit(anyLong(), anyLong())).thenReturn(List.of(1L, 2L));

        HabitTranslation habitTranslation = habitAssign.getHabit().getHabitTranslations().stream().findFirst().get();
        when(modelMapper.map(habitTranslation, HabitDto.class))
            .thenReturn(ModelUtils.getHabitAssignDtoWithFriendsIds().getHabit());

        expected.getFirst().getHabit().setToDoListItems(
            List.of(ToDoListItemDto.builder()
                .id(userToDoListItemCustom.getId())
                .status(userToDoListItemCustom.getStatus().toString())
                .text(userToDoListItemCustom.getToDoListItem().getTranslations().get(0).getContent())
                .build()));

        List<HabitAssignDto> actual = habitAssignService.getAllHabitAssignsByUserIdAndStatusNotCancelled(1L, "en");
        assertEquals(expected, actual);
    }

    @Test
    void getAllMutualHabitAssignsWithUserAndStatusNotCancelledTest() {
        Long userId = 1L, currentUserId = 2L;
        Pageable pageable = PageRequest.of(0, 6);
        List<HabitAssign> habitAssignList = List.of(habitAssign);
        Page<HabitAssign> returnedPage = new PageImpl<>(habitAssignList, pageable, habitAssignList.size());
        HabitAssignPreviewDto habitAssignPreviewDto = HabitAssignPreviewDto.builder()
            .id(habitAssign.getId())
            .status(habitAssign.getStatus())
            .userId(habitAssign.getUser().getId())
            .duration(habitAssign.getDuration())
            .workingDays(habitAssign.getWorkingDays())
            .build();
        habitAssign.getHabit().setHabitTranslations(List.of(
            HabitTranslation.builder()
                .id(1L)
                .name("name")
                .habitItem("habitItem")
                .description("description")
                .language(Language.builder().id(1L).code("ua").build())
                .build(),
            HabitTranslation.builder()
                .id(2L)
                .name("nameUa")
                .habitItem("habitItemUa")
                .description("descriptionUa")
                .language(Language.builder().id(1L).code("en").build())
                .build()));
        PageableAdvancedDto<HabitAssignPreviewDto> expected =
            new PageableAdvancedDto<>(List.of(habitAssignPreviewDto), returnedPage.getTotalElements(),
                returnedPage.getPageable().getPageNumber(), returnedPage.getTotalPages(), returnedPage.getNumber(),
                returnedPage.hasPrevious(), returnedPage.hasNext(), returnedPage.isFirst(), returnedPage.isLast());

        when(habitAssignRepo.findAllMutual(userId, currentUserId, pageable)).thenReturn(returnedPage);
        when(modelMapper.map(habitAssign, HabitAssignPreviewDto.class)).thenReturn(habitAssignPreviewDto);

        var actual =
            habitAssignService.getAllMutualHabitAssignsWithUserAndStatusNotCancelled(userId, currentUserId, pageable);

        verify(habitAssignRepo).findAllMutual(userId, currentUserId, pageable);
        verify(modelMapper).map(habitAssign, HabitAssignPreviewDto.class);
        assertArrayEquals(expected.getPage().toArray(), actual.getPage().toArray());
    }

    @Test
    void getMyHabitsOfCurrentUserAndStatusNotCancelledTest() {
        Long userId = 1L, currentUserId = 2L;
        Pageable pageable = PageRequest.of(0, 6);
        List<HabitAssign> habitAssignList = List.of(habitAssign);
        Page<HabitAssign> returnedPage = new PageImpl<>(habitAssignList, pageable, habitAssignList.size());
        HabitAssignPreviewDto habitAssignPreviewDto = HabitAssignPreviewDto.builder()
            .id(habitAssign.getId())
            .status(habitAssign.getStatus())
            .userId(habitAssign.getUser().getId())
            .duration(habitAssign.getDuration())
            .workingDays(habitAssign.getWorkingDays())
            .build();
        habitAssign.getHabit().setHabitTranslations(List.of(
            HabitTranslation.builder()
                .id(1L)
                .name("name")
                .habitItem("habitItem")
                .description("description")
                .language(Language.builder().id(1L).code("ua").build())
                .build(),
            HabitTranslation.builder()
                .id(2L)
                .name("nameUa")
                .habitItem("habitItemUa")
                .description("descriptionUa")
                .language(Language.builder().id(1L).code("en").build())
                .build()));
        PageableAdvancedDto<HabitAssignPreviewDto> expected =
            new PageableAdvancedDto<>(List.of(habitAssignPreviewDto), returnedPage.getTotalElements(),
                returnedPage.getPageable().getPageNumber(), returnedPage.getTotalPages(), returnedPage.getNumber(),
                returnedPage.hasPrevious(), returnedPage.hasNext(), returnedPage.isFirst(), returnedPage.isLast());

        when(habitAssignRepo.findAllOfCurrentUser(userId, currentUserId, pageable)).thenReturn(returnedPage);
        when(modelMapper.map(habitAssign, HabitAssignPreviewDto.class)).thenReturn(habitAssignPreviewDto);

        var actual =
            habitAssignService.getMyHabitsOfCurrentUserAndStatusNotCancelled(userId, currentUserId, pageable);

        verify(habitAssignRepo).findAllOfCurrentUser(userId, currentUserId, pageable);
        verify(modelMapper).map(habitAssign, HabitAssignPreviewDto.class);
        assertArrayEquals(expected.getPage().toArray(), actual.getPage().toArray());
    }

    @Test
    void getAllByUserIdAndStatusNotCancelledTest() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 6);
        List<HabitAssign> habitAssignList = List.of(habitAssign);
        Page<HabitAssign> returnedPage = new PageImpl<>(habitAssignList, pageable, habitAssignList.size());
        HabitAssignPreviewDto habitAssignPreviewDto = HabitAssignPreviewDto.builder()
            .id(habitAssign.getId())
            .status(habitAssign.getStatus())
            .userId(habitAssign.getUser().getId())
            .duration(habitAssign.getDuration())
            .workingDays(habitAssign.getWorkingDays())
            .build();
        habitAssign.getHabit().setHabitTranslations(List.of(
            HabitTranslation.builder()
                .id(1L)
                .name("name")
                .habitItem("habitItem")
                .description("description")
                .language(Language.builder().id(1L).code("ua").build())
                .build(),
            HabitTranslation.builder()
                .id(2L)
                .name("nameUa")
                .habitItem("habitItemUa")
                .description("descriptionUa")
                .language(Language.builder().id(1L).code("en").build())
                .build()));
        PageableAdvancedDto<HabitAssignPreviewDto> expected =
            new PageableAdvancedDto<>(List.of(habitAssignPreviewDto), returnedPage.getTotalElements(),
                returnedPage.getPageable().getPageNumber(), returnedPage.getTotalPages(), returnedPage.getNumber(),
                returnedPage.hasPrevious(), returnedPage.hasNext(), returnedPage.isFirst(), returnedPage.isLast());

        when(habitAssignRepo.findAllByUserId(userId, pageable)).thenReturn(returnedPage);
        when(modelMapper.map(habitAssign, HabitAssignPreviewDto.class)).thenReturn(habitAssignPreviewDto);

        var actual =
            habitAssignService.getAllByUserIdAndStatusNotCancelled(userId, pageable);

        verify(habitAssignRepo).findAllByUserId(userId, pageable);
        verify(modelMapper).map(habitAssign, HabitAssignPreviewDto.class);
        assertArrayEquals(expected.getPage().toArray(), actual.getPage().toArray());
    }

    @Test
    void getUserToDoAndCustomToDoLists() {
        Long habitAssignId = 2L;
        Long userId = 3L;

        List<CustomToDoListItemResponseDto> customToDoListItemResponseDtos =
            List.of(ModelUtils.getCustomToDoListItemResponseDto());
        List<UserToDoListItemResponseDto> userToDoListItemResponseDtos =
            List.of(ModelUtils.getUserToDoListItemResponseDto());
        UserToDoAndCustomToDoListsDto expected =
            UserToDoAndCustomToDoListsDto
                .builder()
                .customToDoListItemDto(customToDoListItemResponseDtos)
                .userToDoListItemDto(userToDoListItemResponseDtos)
                .build();

        when(toDoListItemService.getUserToDoListByHabitAssignId(userId, habitAssignId, language))
            .thenReturn(userToDoListItemResponseDtos);
        when(
            customToDoListItemService.findAllAvailableCustomToDoListItemsByHabitAssignId(userId, habitAssignId))
            .thenReturn(customToDoListItemResponseDtos);

        UserToDoAndCustomToDoListsDto actual =
            habitAssignService.getUserToDoAndCustomToDoLists(userId, habitAssignId, language);

        assertEquals(expected, actual);

        verify(toDoListItemService).getUserToDoListByHabitAssignId(userId, habitAssignId, language);
        verify(customToDoListItemService).findAllAvailableCustomToDoListItemsByHabitAssignId(userId,
            habitAssignId);
    }

    @Test
    void getUserToDoListItemAndUserCustomToDoListTest() {
        Habit habit1 = Habit.builder().id(3L)
            .complexity(1).build();

        List<HabitAssign> habitAssignList =
            List.of(ModelUtils.getHabitAssign(2L, habit1, HabitAssignStatus.INPROGRESS));

        List<CustomToDoListItemResponseDto> customToDoListItemResponseDtos =
            List.of(ModelUtils.getCustomToDoListItemResponseDtoWithStatusInProgress());

        List<UserToDoListItemResponseDto> userToDoListItemResponseDtos =
            List.of(UserToDoListItemResponseDto
                .builder().id(1L).status(ToDoListItemStatus.INPROGRESS).build());

        List<UserToDoAndCustomToDoListsDto> expected = List.of(
            UserToDoAndCustomToDoListsDto
                .builder()
                .customToDoListItemDto(customToDoListItemResponseDtos)
                .userToDoListItemDto(userToDoListItemResponseDtos)
                .build());

        when(habitAssignRepo.findAllByUserIdAndStatusIsInProgress(1L)).thenReturn(habitAssignList);
        when(toDoListItemService.getUserToDoListItemsByHabitAssignIdAndStatusInProgress(2L, "en"))
            .thenReturn(userToDoListItemResponseDtos);
        when(customToDoListItemService.findAllCustomToDoListItemsWithStatusInProgress(1L, 3L))
            .thenReturn(customToDoListItemResponseDtos);

        assertEquals(expected, habitAssignService.getListOfUserAndCustomToDoListsWithStatusInprogress(1L, "en"));

        verify(habitAssignRepo).findAllByUserIdAndStatusIsInProgress(anyLong());
        verify(toDoListItemService).getUserToDoListItemsByHabitAssignIdAndStatusInProgress(anyLong(), any());
        verify(customToDoListItemService).findAllCustomToDoListItemsWithStatusInProgress(anyLong(), anyLong());
    }

    @Test
    void getUserToDoListItemAndUserCustomToDoListWithNotFoundExceptionTest() {
        when(habitAssignRepo.findAllByUserIdAndStatusIsInProgress(1L)).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class, () -> habitAssignService
                .getListOfUserAndCustomToDoListsWithStatusInprogress(1L, "en"));

        verify(habitAssignRepo).findAllByUserIdAndStatusIsInProgress(anyLong());
    }

    @Test
    void getAllHabitAssignsByUserIdAndStatusAcquired() {
        List<ToDoListItemTranslation> list = getToDoListItemTranslationList();
        when(habitAssignRepo.findAllByUserIdAndStatusAcquired(1L)).thenReturn(fullHabitAssigns);
        when(modelMapper.map(fullHabitAssign, HabitAssignDto.class)).thenReturn(habitAssignDto);
        when(toDoListItemTranslationRepo.findToDoListByHabitIdAndByLanguageCode("en", 1L))
            .thenReturn(list);
        HabitTranslation habitTranslation = habitAssign.getHabit().getHabitTranslations().stream().findFirst().get();
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(ModelUtils.getHabitDto());
        List<HabitAssignDto> actual = habitAssignService.getAllHabitAssignsByUserIdAndStatusAcquired(1L, "en");
        assertEquals(habitAssignDtos, actual);
    }

    @Test
    void getAllHabitAssignsByUserIdAndStatusAcquiredEmptyHabitAssign() {
        List<ToDoListItemTranslation> list = getToDoListItemTranslationList();
        when(habitAssignRepo.findAllByUserIdAndStatusAcquired(1L)).thenReturn(habitAssigns);
        when(modelMapper.map(habitAssign, HabitAssignDto.class)).thenReturn(habitAssignDto);
        when(toDoListItemTranslationRepo.findToDoListByHabitIdAndByLanguageCode("en", 1L))
            .thenReturn(list);
        HabitTranslation habitTranslation = habitAssign.getHabit().getHabitTranslations().stream().findFirst().get();
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(ModelUtils.getHabitDto());
        List<HabitAssignDto> actual = habitAssignService.getAllHabitAssignsByUserIdAndStatusAcquired(1L, "en");
        assertEquals(habitAssignDtos, actual);
    }

    @Test
    void getAllHabitAssignsByUserIdAndCancelledStatus() {
        when(habitAssignRepo.findAllByUserIdAndStatusIsCancelled(1L)).thenReturn(habitAssigns);
        when(modelMapper.map(habitAssign, HabitAssignDto.class)).thenReturn(habitAssignDto);
        HabitTranslation habitTranslation = habitAssign.getHabit().getHabitTranslations().stream().findFirst().get();
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(ModelUtils.getHabitDto());
        List<HabitAssignDto> actual = habitAssignService.getAllHabitAssignsByUserIdAndCancelledStatus(1L, "en");
        assertEquals(habitAssignDtos, actual);
    }

    @Test
    void getByHabitAssignIdAndUserId() {
        Long userId = 1L;
        Long habitAssignId = 2L;
        Long habitAuthorId = 3L;

        habitAssign.setId(habitAssignId);
        habitAssign.getHabit().setUserId(habitAuthorId);
        HabitTranslation habitTranslation = habitAssign.getHabit().getHabitTranslations().stream().findFirst().get();

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(habitAssign, HabitAssignDto.class)).thenReturn(habitAssignDto);
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(ModelUtils.getHabitDto());

        HabitAssignDto result = habitAssignService.getByHabitAssignIdAndUserId(habitAssignId, userId, language);

        assertEquals(habitAssignDto, result);
        assertEquals(habitAuthorId, result.getHabit().getUsersIdWhoCreatedCustomHabit());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(modelMapper).map(habitAssign, HabitAssignDto.class);
        verify(modelMapper).map(habitTranslation, HabitDto.class);
    }

    @Test
    void enrollHabit() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        Long habitTranslationId = 4L;
        LocalDate localDate = LocalDate.now();

        HabitTranslation translation = ModelUtils.getHabitTranslation();
        translation.setId(habitTranslationId);

        habitAssign.setId(habitAssignId);
        habitAssign.setHabit(habit);
        habitAssign.getHabit().setHabitTranslations(Collections.singletonList(translation));
        habitAssign.getUser().setId(userId);
        habitAssign.setDuration(14);
        habitAssign.setWorkingDays(0);

        HabitAssignVO habitAssignVO = ModelUtils.getHabitAssignVO();
        RatingPoints ratingPoints = RatingPoints.builder().id(1L).name("DAYS_OF_HABIT_IN_PROGRESS").points(1).build();

        when(ratingPointsRepo.findByNameOrThrow("DAYS_OF_HABIT_IN_PROGRESS")).thenReturn(ratingPoints);
        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(habitAssign, HabitAssignVO.class)).thenReturn(habitAssignVO);
        when(habitStatusCalendarService
            .findHabitStatusCalendarByEnrollDateAndHabitAssign(localDate, habitAssignVO))
            .thenReturn(null);
        when(modelMapper.map(habitAssign, HabitAssignDto.class)).thenReturn(habitAssignDto);
        when(modelMapper.map(translation, HabitDto.class)).thenReturn(habitDto);
        when(userService.findById(any())).thenReturn(getUserVO());

        HabitAssignDto actualDto = habitAssignService.enrollHabit(habitAssignId, userId, localDate, language);
        assertEquals(1, habitAssign.getWorkingDays());
        assertEquals(habitAssignDto, actualDto);

        verify(habitAssignRepo).findById(habitAssignId);
        verify(modelMapper).map(habitAssign, HabitAssignVO.class);
        verify(habitStatusCalendarService).findHabitStatusCalendarByEnrollDateAndHabitAssign(localDate, habitAssignVO);
        verify(habitAssignRepo).save(habitAssign);
        verify(modelMapper).map(habitAssign, HabitAssignDto.class);
        verify(modelMapper).map(translation, HabitDto.class);
        verify(userToDoListItemRepo)
            .getAllAssignedToDoListItemsFull(habitAssignId);
    }

    @Test
    void enrollHabitThrowsNotFoundExceptionWhenHabitAssignNotExists() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        LocalDate localDate = LocalDate.now();

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> habitAssignService.enrollHabit(habitAssignId, userId, localDate, language));

        assertEquals(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId,
            exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(modelMapper, times(0)).map(any(), eq(HabitAssignVO.class));
        verify(habitStatusCalendarService, times(0)).findHabitStatusCalendarByEnrollDateAndHabitAssign(any(), any());
        verify(habitAssignRepo, times(0)).save(any());
        verify(modelMapper, times(0)).map(any(), eq(HabitAssignDto.class));
        verify(modelMapper, times(0)).map(any(), eq(HabitDto.class));
        verify(userToDoListItemRepo, times(0))
            .getAllAssignedToDoListItemsFull(anyLong());
    }

    @Test
    void enrollHabitThrowsUserHasNoPermissionToAccessExceptionWhenHabitAssignNotBelongToUser() {
        long habitAssignId = 2L;
        long userId = 3L;
        LocalDate localDate = LocalDate.now();

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId + 1L);
        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));

        UserHasNoPermissionToAccessException exception = assertThrows(UserHasNoPermissionToAccessException.class,
            () -> habitAssignService.enrollHabit(habitAssignId, userId, localDate, language));

        assertEquals(ErrorMessage.USER_HAS_NO_PERMISSION, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(modelMapper, times(0)).map(any(), eq(HabitAssignVO.class));
        verify(habitStatusCalendarService, times(0)).findHabitStatusCalendarByEnrollDateAndHabitAssign(any(), any());
        verify(habitAssignRepo, times(0)).save(any());
        verify(modelMapper, times(0)).map(any(), eq(HabitAssignDto.class));
        verify(modelMapper, times(0)).map(any(), eq(HabitDto.class));
        verify(userToDoListItemRepo, times(0))
            .getAllAssignedToDoListItemsFull(anyLong());
    }

    @Test
    void enrollHabitThrowsUserAlreadyHasEnrolledHabitAssign() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        Long habitTranslationId = 4L;
        LocalDate localDate = LocalDate.now();

        HabitTranslation translation = ModelUtils.getHabitTranslation();
        translation.setId(habitTranslationId);

        habitAssign.setId(habitAssignId);
        habitAssign.setHabit(habit);
        habitAssign.getHabit().setHabitTranslations(Collections.singletonList(translation));
        habitAssign.getUser().setId(userId);

        HabitAssignVO habitAssignVO = ModelUtils.getHabitAssignVO();

        HabitStatusCalendarVO habitStatusCalendarVO = ModelUtils.getHabitStatusCalendarVO();

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(habitAssign, HabitAssignVO.class)).thenReturn(habitAssignVO);
        when(habitStatusCalendarService
            .findHabitStatusCalendarByEnrollDateAndHabitAssign(localDate, habitAssignVO))
            .thenReturn(habitStatusCalendarVO);

        UserAlreadyHasEnrolledHabitAssign exception = assertThrows(UserAlreadyHasEnrolledHabitAssign.class,
            () -> habitAssignService.enrollHabit(habitAssignId, userId, localDate, language));

        assertEquals(ErrorMessage.HABIT_HAS_BEEN_ALREADY_ENROLLED, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(modelMapper).map(habitAssign, HabitAssignVO.class);
        verify(habitStatusCalendarService).findHabitStatusCalendarByEnrollDateAndHabitAssign(localDate, habitAssignVO);
        verify(habitAssignRepo, times(0)).save(any());
        verify(modelMapper, times(0)).map(any(), eq(HabitAssignDto.class));
        verify(modelMapper, times(0)).map(any(), eq(HabitDto.class));
        verify(userToDoListItemRepo, times(0))
            .getAllAssignedToDoListItemsFull(anyLong());
    }

    @Test
    void enrollHabitThrowsExceptionWhenWorkingDaysEqualOrGreaterThanDuration() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        Long habitTranslationId = 4L;
        LocalDate localDate = LocalDate.now();
        String lang = AppConstant.DEFAULT_LANGUAGE_CODE;

        HabitTranslation translation = ModelUtils.getHabitTranslation();
        translation.setId(habitTranslationId);

        habitAssign.setId(habitAssignId);
        habitAssign.setHabit(habit);
        habitAssign.getHabit().setHabitTranslations(Collections.singletonList(translation));
        habitAssign.getUser().setId(userId);
        habitAssign.setDuration(14);
        habitAssign.setWorkingDays(14);

        HabitAssignVO habitAssignVO = ModelUtils.getHabitAssignVO();

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(habitAssign, HabitAssignVO.class)).thenReturn(habitAssignVO);
        when(habitStatusCalendarService
            .findHabitStatusCalendarByEnrollDateAndHabitAssign(localDate, habitAssignVO))
            .thenReturn(null);

        UserHasReachedOutOfEnrollRange exception = assertThrows(UserHasReachedOutOfEnrollRange.class,
            () -> habitAssignService.enrollHabit(habitAssignId, userId, localDate, lang));

        assertEquals(ErrorMessage.HABIT_ASSIGN_ENROLL_RANGE_REACHED, exception.getMessage());

        habitAssign.setWorkingDays(11);
        habitAssign.setDuration(10);

        UserHasReachedOutOfEnrollRange exception2 = assertThrows(UserHasReachedOutOfEnrollRange.class,
            () -> habitAssignService.enrollHabit(habitAssignId, userId, localDate, lang));

        assertEquals(ErrorMessage.HABIT_ASSIGN_ENROLL_RANGE_REACHED, exception2.getMessage());

        verify(habitAssignRepo, times(2)).findById(habitAssignId);
        verify(modelMapper, times(2)).map(habitAssign, HabitAssignVO.class);
        verify(habitStatusCalendarService, times(2)).findHabitStatusCalendarByEnrollDateAndHabitAssign(localDate,
            habitAssignVO);
        verify(habitAssignRepo, times(0)).save(any());
        verify(modelMapper, times(0)).map(any(), eq(HabitAssignDto.class));
        verify(modelMapper, times(0)).map(any(), eq(HabitDto.class));
        verify(userToDoListItemRepo, times(0))
            .getAllAssignedToDoListItemsFull(anyLong());
    }

    @Test
    void enrollHabitThrowsUserHasReachedOutOfEnrollRangeWhenEnrollsInFuture() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        Long habitTranslationId = 4L;
        LocalDate localDate = LocalDate.now().plusDays(1);

        HabitTranslation translation = ModelUtils.getHabitTranslation();
        translation.setId(habitTranslationId);

        habitAssign.setId(habitAssignId);
        habitAssign.setHabit(habit);
        habitAssign.getHabit().setHabitTranslations(Collections.singletonList(translation));
        habitAssign.getUser().setId(userId);

        HabitAssignVO habitAssignVO = ModelUtils.getHabitAssignVO();

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(habitAssign, HabitAssignVO.class)).thenReturn(habitAssignVO);
        when(habitStatusCalendarService
            .findHabitStatusCalendarByEnrollDateAndHabitAssign(localDate, habitAssignVO))
            .thenReturn(null);

        UserHasReachedOutOfEnrollRange exception = assertThrows(UserHasReachedOutOfEnrollRange.class,
            () -> habitAssignService.enrollHabit(habitAssignId, userId, localDate, language));

        assertEquals(ErrorMessage.HABIT_STATUS_CALENDAR_OUT_OF_ENROLL_RANGE, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(modelMapper).map(habitAssign, HabitAssignVO.class);
        verify(habitStatusCalendarService).findHabitStatusCalendarByEnrollDateAndHabitAssign(localDate, habitAssignVO);
        verify(habitAssignRepo, times(0)).save(any());
        verify(modelMapper, times(0)).map(any(), eq(HabitAssignDto.class));
        verify(modelMapper, times(0)).map(any(), eq(HabitDto.class));
        verify(userToDoListItemRepo, times(0))
            .getAllAssignedToDoListItemsFull(anyLong());
    }

    @Test
    void enrollHabitThrowsUserHasReachedOutOfEnrollRangeWhenPassedMaxDayOfAbilityToEnroll() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        Long habitTranslationId = 4L;
        LocalDate localDate = LocalDate.now().minusDays(AppConstant.MAX_PASSED_DAYS_OF_ABILITY_TO_ENROLL);

        HabitTranslation translation = ModelUtils.getHabitTranslation();
        translation.setId(habitTranslationId);

        habitAssign.setId(habitAssignId);
        habitAssign.setHabit(habit);
        habitAssign.getHabit().setHabitTranslations(Collections.singletonList(translation));
        habitAssign.getUser().setId(userId);

        HabitAssignVO habitAssignVO = ModelUtils.getHabitAssignVO();

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(habitAssign, HabitAssignVO.class)).thenReturn(habitAssignVO);
        when(habitStatusCalendarService
            .findHabitStatusCalendarByEnrollDateAndHabitAssign(localDate, habitAssignVO))
            .thenReturn(null);

        UserHasReachedOutOfEnrollRange exception = assertThrows(UserHasReachedOutOfEnrollRange.class,
            () -> habitAssignService.enrollHabit(habitAssignId, userId, localDate, language));

        assertEquals(ErrorMessage.HABIT_STATUS_CALENDAR_OUT_OF_ENROLL_RANGE, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(modelMapper).map(habitAssign, HabitAssignVO.class);
        verify(habitStatusCalendarService).findHabitStatusCalendarByEnrollDateAndHabitAssign(localDate, habitAssignVO);
        verify(habitAssignRepo, times(0)).save(any());
        verify(modelMapper, times(0)).map(any(), eq(HabitAssignDto.class));
        verify(modelMapper, times(0)).map(any(), eq(HabitDto.class));
        verify(userToDoListItemRepo, times(0))
            .getAllAssignedToDoListItemsFull(anyLong());
    }

    @Test
    void findInprogressHabitAssignsOnDate() {

        Long id = 3L;
        LocalDate date = LocalDate.now();
        Language languageEn = ModelUtils.getLanguage();

        HabitTranslation habitTranslation = ModelUtils.getHabitTranslation();
        habitAssign.getHabit().setHabitTranslations(Collections.singletonList(habitTranslation));

        when(habitAssignRepo.findAllInprogressHabitAssignsOnDate(anyLong(), any(LocalDate.class)))
            .thenReturn(Collections.singletonList(habitAssign));

        when(modelMapper.map(habitAssign, HabitAssignDto.class)).thenReturn(habitAssignDto);
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(habitDto);

        List<HabitAssignDto> dtoList =
            habitAssignService.findInprogressHabitAssignsOnDate(id, date, languageEn.getCode());
        assertEquals(dtoList.getFirst(), habitAssignDto);

    }

    @Test
    void findInprogressHabitAssignsOnDateContent() {

        Long id = 3L;
        LocalDate date = LocalDate.now();
        Language languageEn = ModelUtils.getLanguage();

        HabitTranslation habitTranslation = ModelUtils.getHabitTranslation();
        habitAssign.getHabit().setHabitTranslations(Collections.singletonList(habitTranslation));

        when(habitAssignRepo.findAllInprogressHabitAssignsOnDate(anyLong(), any(LocalDate.class)))
            .thenReturn(Collections.singletonList(habitAssign));

        when(modelMapper.map(habitAssign, HabitAssignDto.class)).thenReturn(habitAssignDto);
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(habitDto);

        List<HabitAssignDto> dtoList =
            habitAssignService.findInprogressHabitAssignsOnDateContent(id, date, languageEn.getCode());
        assertEquals(dtoList.getFirst(), habitAssignDto);

    }

    @Test
    void updateStatusAndDurationOfHabitAssignTest() {
        HabitAssign habitAssignRequested = getHabitAssign()
            .setStatus(HabitAssignStatus.REQUESTED)
            .setDuration(20);
        when(habitAssignRepo.findById(anyLong())).thenReturn(Optional.of(habitAssignRequested));
        when(habitAssignRepo.findByHabitAssignIdUserIdAndStatusIsRequested(anyLong(), anyLong()))
            .thenReturn(Optional.of(habitAssignRequested));
        when(modelMapper.map(any(), any())).thenReturn(getHabitAssignUserDurationDto());

        var result = habitAssignService.updateStatusAndDurationOfHabitAssign(1L, 21L, 20);
        assertEquals(20, result.getDuration());
        assertEquals(HabitAssignStatus.INPROGRESS, result.getStatus());
        verify(habitAssignRepo).findById(anyLong());
        verify(habitAssignRepo).findByHabitAssignIdUserIdAndStatusIsRequested(anyLong(), anyLong());
        verify(modelMapper).map(any(), any());
    }

    @Test
    void updateStatusAndDurationOfHabitAssignThrowNotFoundExceptionTest() {
        when(habitAssignRepo.findById(anyLong())).thenReturn(Optional.empty());
        var exception = assertThrows(NotFoundException.class,
                () -> habitAssignService.updateStatusAndDurationOfHabitAssign(1L, 21L, 1));
        assertEquals(exception.getMessage(), ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + 1L);
        verify(habitAssignRepo).findById(anyLong());
    }

    @Test
    void updateStatusAndDurationOfHabitAssignThrowInvalidStatusExceptionTest() {
        HabitAssign habitAssignInProgress = ModelUtils.getHabitAssign()
            .setStatus(HabitAssignStatus.INPROGRESS);
        when(habitAssignRepo.findById(anyLong())).thenReturn(Optional.of(habitAssignInProgress));
        when(habitAssignRepo.findByHabitAssignIdUserIdAndStatusIsRequested(anyLong(), anyLong()))
            .thenReturn(Optional.empty());
        var exception = assertThrows(InvalidStatusException.class,
            () -> habitAssignService.updateStatusAndDurationOfHabitAssign(1L, 21L, 1));
        assertEquals(ErrorMessage.HABIT_ASSIGN_STATUS_IS_NOT_REQUESTED_OR_USER_HAS_NOT_ANY_ASSIGNED_HABITS,
            exception.getMessage());
        verify(habitAssignRepo).findById(anyLong());
        verify(habitAssignRepo).findByHabitAssignIdUserIdAndStatusIsRequested(anyLong(), anyLong());
    }

    @Test
    void updateUserHabitInfoDurationTest() {
        HabitAssign habitAssignDuration20 = getHabitAssign().setDuration(20);
        when(habitAssignRepo.existsById(anyLong())).thenReturn(true);
        when(habitAssignRepo.findByHabitAssignIdUserIdAndStatusIsInProgress(anyLong(), anyLong()))
            .thenReturn(Optional.of(habitAssignDuration20));
        when(modelMapper.map(any(), any())).thenReturn(getHabitAssignUserDurationDto());
        var result = habitAssignService.updateUserHabitInfoDuration(1L, 21L, 20);
        assertEquals(20, result.getDuration());
        verify(habitAssignRepo).existsById(anyLong());
        verify(habitAssignRepo).findByHabitAssignIdUserIdAndStatusIsInProgress(anyLong(), anyLong());
        verify(modelMapper).map(any(), any());
    }

    @Test
    void updateUserHabitInfoDurationThrowNotFoundExceptionTest() {
        when(habitAssignRepo.existsById(anyLong())).thenReturn(false);
        var exception = assertThrows(NotFoundException.class,
                () -> habitAssignService.updateUserHabitInfoDuration(1L, 21L, 1));
        assertEquals(exception.getMessage(), ErrorMessage.HABIT_NOT_FOUND_BY_ID + 1L);
        verify(habitAssignRepo).existsById(anyLong());
    }

    @Test
    void updateUserHabitInfoDurationThrowInvalidStatusExceptionTest() {
        when(habitAssignRepo.existsById(anyLong())).thenReturn(true);
        when(habitAssignRepo.findByHabitAssignIdUserIdAndStatusIsInProgress(anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        var exception = assertThrows(InvalidStatusException.class,
                () -> habitAssignService.updateUserHabitInfoDuration(1L, 21L, 1));
        assertEquals(ErrorMessage.HABIT_ASSIGN_STATUS_IS_NOT_INPROGRESS_OR_USER_HAS_NOT_ANY_ASSIGNED_HABITS,
                exception.getMessage());
        verify(habitAssignRepo).existsById(anyLong());
        verify(habitAssignRepo).findByHabitAssignIdUserIdAndStatusIsInProgress(anyLong(), anyLong());
    }

    @Test
    void updateUserHabitInfoDurationThrowBadRequestExceptionTest() {
        HabitAssign habitAssignBadRequest = getHabitAssign();
        habitAssignBadRequest.setDuration(20);
        habitAssignBadRequest.setWorkingDays(20);
        when(habitAssignRepo.existsById(anyLong())).thenReturn(true);
        when(habitAssignRepo.findByHabitAssignIdUserIdAndStatusIsInProgress(anyLong(), anyLong()))
            .thenReturn(Optional.of(habitAssignBadRequest));
        var exception = assertThrows(BadRequestException.class,
            () -> habitAssignService.updateUserHabitInfoDuration(1L, 21L, 19));
        assertEquals(ErrorMessage.INVALID_DURATION, exception.getMessage());
        verify(habitAssignRepo).existsById(anyLong());
        verify(habitAssignRepo).findByHabitAssignIdUserIdAndStatusIsInProgress(anyLong(), anyLong());
    }

    @Test
    void findHabitByUserIdAndHabitAssignIdThrowsNotFoundExceptionWhenHabitAssignNotExists() {
        Long userId = 1L;
        Long habitAssignId = 2L;

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> habitAssignService
            .findHabitByUserIdAndHabitAssignId(userId, habitAssignId, "ua"));

        assertEquals(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId, exception.getMessage());
    }

    @Test
    void findHabitByUserIdAndHabitAssignIdThrowsNotFoundExceptionWhenHabitAssignNotBelongsToUser() {
        long userId = 1L;
        long habitAssignId = 2L;

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId + 1);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));

        UserHasNoPermissionToAccessException exception =
            assertThrows(UserHasNoPermissionToAccessException.class, () -> habitAssignService
                .findHabitByUserIdAndHabitAssignId(userId, habitAssignId, "ua"));

        assertEquals(ErrorMessage.USER_HAS_NO_PERMISSION, exception.getMessage());
    }

    @Test
    void findHabitWithHabitAssignStatus() {
        Long habitId = 1L;
        Long userId = 2L;
        Long habitAssignId = 3L;
        Habit habitWithHabitAssignStatus = ModelUtils.getHabit(habitId, "image123");
        HabitAssign habitAssignInProgress =
            ModelUtils.getHabitAssign(habitAssignId, habitWithHabitAssignStatus, HabitAssignStatus.INPROGRESS);
        habitAssignInProgress.getUser().setId(userId);
        HabitAssignDto habitAssignDtoInProgress =
            ModelUtils.getHabitAssignDto(habitAssignId, habitAssignInProgress.getStatus(),
                habitWithHabitAssignStatus.getImage());
        HabitTranslation habitTranslation =
            habitAssignInProgress.getHabit().getHabitTranslations().stream().findFirst().get();

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssignInProgress));
        when(toDoListItemTranslationRepo.findToDoListByHabitIdAndByLanguageCode(language, habitId))
            .thenReturn(new ArrayList<>());
        when(modelMapper.map(habitAssignInProgress, HabitAssignDto.class)).thenReturn(habitAssignDtoInProgress);
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(habitAssignDtoInProgress.getHabit());
        when(userToDoListItemRepo.getAllAssignedToDoListItemsFull(habitAssignId)).thenReturn(new ArrayList<>());

        var dto = habitAssignService.findHabitByUserIdAndHabitAssignId(userId, habitAssignId, language);

        assertNotNull(dto);
        assertEquals(habitWithHabitAssignStatus.getId(), dto.getId());
        assertEquals(habitWithHabitAssignStatus.getImage(), dto.getImage());
        assertEquals(habitAssignInProgress.getStatus(), dto.getHabitAssignStatus());
        verify(habitAssignRepo).findById(anyLong());
        verify(toDoListItemTranslationRepo).findToDoListByHabitIdAndByLanguageCode(anyString(), anyLong());
        verify(userToDoListItemRepo).getAllAssignedToDoListItemsFull(anyLong());
    }

    @Test
    void findHabitByUserIdAndHabitIdTest() {
        Long habitId = 1L;
        Long userId = 2L;
        Long habitAssignId = 3L;
        Long amountOfUsersAcquired = 4L;
        fullHabitAssign.setId(habitAssignId);
        fullHabitAssign.getUser().setId(userId);

        HabitAssignDto fullHabitAssignDto = getFullHabitAssignDto();
        fullHabitAssignDto.setId(habitAssignId);
        fullHabitAssignDto.setUserId(userId);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(fullHabitAssign));
        when(modelMapper.map(fullHabitAssign, HabitAssignDto.class)).thenReturn(fullHabitAssignDto);
        when(modelMapper.map(any(HabitTranslation.class), eq(HabitDto.class))).thenReturn(getHabitDto());
        when(toDoListItemTranslationRepo.findToDoListByHabitIdAndByLanguageCode(language, habitId))
            .thenReturn(getToDoListItemTranslationList());
        when(habitAssignRepo.findAmountOfUsersAcquired(habitId)).thenReturn(amountOfUsersAcquired);

        HabitDto actual = habitAssignService.findHabitByUserIdAndHabitAssignId(userId, habitAssignId, language);
        assertNotNull(actual.getAmountAcquiredUsers());

        verify(habitAssignRepo, times(1)).findById(habitAssignId);
        verify(toDoListItemTranslationRepo, times(1)).findToDoListByHabitIdAndByLanguageCode(language, habitId);
        verify(habitAssignRepo, times(1)).findAmountOfUsersAcquired(habitId);
    }

    @Test
    void getReadinessPercent() {
        habitAssignDto.setWorkingDays(30);
        habitAssignDto.setDuration(2);

        assertEquals(1500, habitAssignService.getReadinessPercent(habitAssignDto));
    }

    @Test
    void updateToDoItem() {
        UserToDoListItem userToDoListItem = getUserToDoListItem();
        userToDoListItem.setStatus(ToDoListItemStatus.ACTIVE);
        when(userToDoListItemRepo.getAllAssignedToDoListItemsFull(any()))
            .thenReturn(List.of(userToDoListItem));

        habitAssignService.updateToDoItem(1L, 1L);
        assertEquals(ToDoListItemStatus.INPROGRESS, userToDoListItem.getStatus());

        habitAssignService.updateToDoItem(1L, 1L);
        assertEquals(ToDoListItemStatus.ACTIVE, userToDoListItem.getStatus());
    }

    @Test
    void updateToDoItemWithNotPresentToDoItem() {
        ToDoListItemStatus oldStatus = ToDoListItemStatus.ACTIVE;
        UserToDoListItem userToDoListItem = getUserToDoListItem();
        userToDoListItem.setStatus(oldStatus);
        when(userToDoListItemRepo.getAllAssignedToDoListItemsFull(any()))
            .thenReturn(List.of(userToDoListItem));

        habitAssignService.updateToDoItem(1L, 2L);
        verify(userToDoListItemRepo, times(0)).save(any());
        assertEquals(oldStatus, userToDoListItem.getStatus());
    }

    @Test
    void updateToDoItemWithNotActiveAndNotInprogressStatus() {
        UserToDoListItem userToDoListItem = getUserToDoListItem();
        when(userToDoListItemRepo.getAllAssignedToDoListItemsFull(any()))
            .thenReturn(List.of(userToDoListItem));

        userToDoListItem.setStatus(ToDoListItemStatus.DONE);
        habitAssignService.updateToDoItem(1L, 1L);
        assertEquals(ToDoListItemStatus.DONE, userToDoListItem.getStatus());

        userToDoListItem.setStatus(ToDoListItemStatus.DISABLED);
        habitAssignService.updateToDoItem(1L, 1L);
        assertEquals(ToDoListItemStatus.DISABLED, userToDoListItem.getStatus());
    }

    @Test
    void fullUpdateUserAndCustomToDoListsWithNonItem() {
        Long userId = 1L;
        Long habitAssignId = 1L;

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of())
            .customToDoListItemDto(List.of())
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(customToDoListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of());

        habitAssignService.fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userToDoListItemRepo).saveAll(List.of());
        verify(userToDoListItemRepo).deleteAll(List.of());

        verify(toDoListItemRepo, times(0)).findByNames(getFullHabitAssign().getHabit().getId(), List.of(),
            language);
        verify(toDoListItemService).saveUserToDoListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customToDoListItemRepo).findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customToDoListItemRepo).saveAll(List.of());
        verify(customToDoListItemRepo).deleteAll(List.of());

        BulkSaveCustomToDoListItemDto bulkSaveCustomToDoListItemDto =
            new BulkSaveCustomToDoListItemDto(List.of());

        verify(customToDoListItemService).save(bulkSaveCustomToDoListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void saveUserToDoListWithStatuses() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        String name = "Buy a bamboo toothbrush";
        UserToDoListItemResponseDto responseDto = ModelUtils.getUserToDoListItemResponseDto();
        responseDto.setId(null);
        responseDto.setText(name);

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of(responseDto))
            .customToDoListItemDto(List.of())
            .build();

        List<String> listOfName = List.of(name);
        ToDoListItem toDoListItem = ModelUtils.getToDoListItem();
        ToDoListItemWithStatusRequestDto requestDto = ModelUtils.getToDoListItemWithStatusRequestDto();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(toDoListItemRepo.findByNames(getFullHabitAssign().getHabit().getId(), listOfName, language))
            .thenReturn(List.of(toDoListItem));

        when(customToDoListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of());

        habitAssignService.fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userToDoListItemRepo).saveAll(List.of());
        verify(userToDoListItemRepo).deleteAll(List.of());

        verify(toDoListItemRepo).findByNames(getFullHabitAssign().getHabit().getId(), listOfName, language);
        verify(toDoListItemService).saveUserToDoListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(requestDto), language);

        verify(customToDoListItemRepo).findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customToDoListItemRepo).saveAll(List.of());
        verify(customToDoListItemRepo).deleteAll(List.of());

        BulkSaveCustomToDoListItemDto bulkSaveCustomToDoListItemDto =
            new BulkSaveCustomToDoListItemDto(List.of());

        verify(customToDoListItemService).save(bulkSaveCustomToDoListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void saveUserToDoListWithStatusesWithNonExistentItemThrowsNotFoundException() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        String name = "Buy a bamboo toothbrush";
        UserToDoListItemResponseDto responseDto = ModelUtils.getUserToDoListItemResponseDto();
        responseDto.setId(null);
        responseDto.setText(name);

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of(responseDto))
            .customToDoListItemDto(List.of())
            .build();

        List<String> listOfName = List.of(name);

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(toDoListItemRepo.findByNames(getFullHabitAssign().getHabit().getId(), listOfName, language))
            .thenReturn(List.of());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> habitAssignService
                .fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language));

        assertEquals(ErrorMessage.TO_DO_LIST_ITEM_NOT_FOUND_BY_NAMES + name, exception.getMessage());

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo).findById(habitAssignId);

        verify(userToDoListItemRepo).saveAll(List.of());
        verify(userToDoListItemRepo).deleteAll(List.of());

        verify(toDoListItemRepo).findByNames(getFullHabitAssign().getHabit().getId(), listOfName, language);
        verify(toDoListItemService, times(0))
            .saveUserToDoListItems(anyLong(), anyLong(), anyList(), anyString());

        verify(customToDoListItemRepo, times(0)).findAllByUserIdAndHabitId(userId,
            getFullHabitAssign().getHabit().getId());
        verify(customToDoListItemRepo, times(0)).saveAll(anyList());
        verify(customToDoListItemRepo, times(0)).deleteAll(anyList());

        verify(customToDoListItemService, times(0)).save(any(), anyLong(), anyLong());
    }

    @Test
    void saveUserToDoListWithStatusesWithDuplicateThrowsBadRequestException() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        UserToDoListItemResponseDto responseDto = ModelUtils.getUserToDoListItemResponseDto();
        responseDto.setId(null);
        UserToDoListItemResponseDto sameResponse = ModelUtils.getUserToDoListItemResponseDto();
        sameResponse.setId(null);

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of(responseDto, sameResponse))
            .customToDoListItemDto(List.of())
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> habitAssignService
            .fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language));

        assertEquals(ErrorMessage.DUPLICATED_USER_TO_DO_LIST_ITEM, exception.getMessage());

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);

        verify(userToDoListItemRepo).saveAll(List.of());
        verify(userToDoListItemRepo).deleteAll(List.of());

        verify(toDoListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(toDoListItemService, times(0))
            .saveUserToDoListItems(anyLong(), anyLong(), anyList(), anyString());

        verify(customToDoListItemRepo, times(0))
            .findAllByUserIdAndHabitId(anyLong(), anyLong());
        verify(customToDoListItemRepo, times(0)).saveAll(anyList());
        verify(customToDoListItemRepo, times(0)).deleteAll(anyList());

        verify(customToDoListItemService, times(0)).save(any(), anyLong(), anyLong());
    }

    @Test
    void updateAndDeleteUserToDoListWithStatusesUpdateItem() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        ToDoListItemStatus newStatus = ToDoListItemStatus.DONE;
        UserToDoListItemResponseDto responseDto = ModelUtils.getUserToDoListItemResponseDto();
        responseDto.setStatus(newStatus);

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of(responseDto))
            .customToDoListItemDto(List.of())
            .build();

        HabitAssign habitAssignWithToDoList = ModelUtils.getHabitAssign();

        UserToDoListItem userToDoListItem = ModelUtils.getUserToDoListItem();
        userToDoListItem.setStatus(ToDoListItemStatus.ACTIVE);
        userToDoListItem.setHabitAssign(null);

        habitAssignWithToDoList.setUserToDoListItems(List.of(userToDoListItem));

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(habitAssignWithToDoList));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssignWithToDoList));

        when(customToDoListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of());

        habitAssignService
            .fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        UserToDoListItem userToDoListItemToSave = ModelUtils.getUserToDoListItem();
        userToDoListItemToSave.setStatus(ToDoListItemStatus.DONE);
        userToDoListItemToSave.setHabitAssign(null);

        verify(userToDoListItemRepo).saveAll(List.of(userToDoListItemToSave));
        verify(userToDoListItemRepo).deleteAll(List.of());

        verify(toDoListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(toDoListItemService).saveUserToDoListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customToDoListItemRepo).findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customToDoListItemRepo).saveAll(List.of());
        verify(customToDoListItemRepo).deleteAll(List.of());

        BulkSaveCustomToDoListItemDto bulkSaveCustomToDoListItemDto =
            new BulkSaveCustomToDoListItemDto(List.of());

        verify(customToDoListItemService).save(bulkSaveCustomToDoListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteUserToDoListWithStatusesUpdateItemWithDisabledStatus() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        ToDoListItemStatus newStatus = ToDoListItemStatus.DONE;
        UserToDoListItemResponseDto responseDto = ModelUtils.getUserToDoListItemResponseDto();
        responseDto.setStatus(newStatus);

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of(responseDto))
            .customToDoListItemDto(List.of())
            .build();

        HabitAssign habitAssignWithToDoList = ModelUtils.getHabitAssign();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(habitAssignWithToDoList));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssignWithToDoList));

        UserToDoListItem userToDoListItem = ModelUtils.getUserToDoListItem();
        userToDoListItem.setStatus(ToDoListItemStatus.DISABLED);
        userToDoListItem.setHabitAssign(null);

        habitAssignWithToDoList.setUserToDoListItems(List.of(userToDoListItem));

        habitAssignService
            .fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        UserToDoListItem userToDoListItemToSave = ModelUtils.getUserToDoListItem();
        userToDoListItemToSave.setStatus(ToDoListItemStatus.DONE);
        userToDoListItemToSave.setHabitAssign(null);

        verify(userToDoListItemRepo).saveAll(List.of(userToDoListItemToSave));
        verify(userToDoListItemRepo).deleteAll(List.of());

        verify(toDoListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(toDoListItemService).saveUserToDoListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customToDoListItemRepo).findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customToDoListItemRepo).saveAll(List.of());
        verify(customToDoListItemRepo).deleteAll(List.of());

        BulkSaveCustomToDoListItemDto bulkSaveCustomToDoListItemDto =
            new BulkSaveCustomToDoListItemDto(List.of());

        verify(customToDoListItemService).save(bulkSaveCustomToDoListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteUserToDoListWithStatusesWithNonExistentItemThrowsNotFoundException() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        UserToDoListItemResponseDto responseDto = ModelUtils.getUserToDoListItemResponseDto();

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of(responseDto))
            .customToDoListItemDto(List.of())
            .build();

        HabitAssign habitAssignWithToDoList = ModelUtils.getHabitAssign();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(habitAssignWithToDoList));

        UserToDoListItem userToDoListItem = ModelUtils.getUserToDoListItem();
        userToDoListItem.setId(responseDto.getId() + 1);
        habitAssignWithToDoList.setUserToDoListItems(List.of(userToDoListItem));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> habitAssignService
            .fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language));

        assertEquals(ErrorMessage.USER_TO_DO_LIST_ITEM_NOT_FOUND + responseDto.getId(), exception.getMessage());

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);

        verify(userToDoListItemRepo, times(0)).saveAll(anyList());
        verify(userToDoListItemRepo, times(0)).deleteAll(anyList());

        verify(toDoListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(toDoListItemService, times(0))
            .saveUserToDoListItems(anyLong(), anyLong(), anyList(), anyString());

        verify(customToDoListItemRepo, times(0))
            .findAllByUserIdAndHabitId(anyLong(), anyLong());
        verify(customToDoListItemRepo, times(0)).saveAll(anyList());
        verify(customToDoListItemRepo, times(0)).deleteAll(anyList());

        verify(customToDoListItemService, times(0)).save(any(), anyLong(), anyLong());
    }

    @Test
    void updateAndDeleteUserToDoListWithStatusesDeleteItem() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        HabitAssign habitAssignWithToDoList = ModelUtils.getHabitAssign();

        UserToDoListItem userToDoListItem = ModelUtils.getUserToDoListItem();

        habitAssignWithToDoList.setUserToDoListItems(List.of(userToDoListItem));

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of())
            .customToDoListItemDto(List.of())
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(habitAssignWithToDoList));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssignWithToDoList));

        when(customToDoListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of());

        habitAssignService
            .fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userToDoListItemRepo).saveAll(List.of());
        verify(userToDoListItemRepo).deleteAll(List.of(userToDoListItem));

        verify(toDoListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(toDoListItemService).saveUserToDoListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customToDoListItemRepo).findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customToDoListItemRepo).saveAll(List.of());
        verify(customToDoListItemRepo).deleteAll(List.of());

        BulkSaveCustomToDoListItemDto bulkSaveCustomToDoListItemDto =
            new BulkSaveCustomToDoListItemDto(List.of());

        verify(customToDoListItemService).save(bulkSaveCustomToDoListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteUserToDoListWithStatusesDeleteItemWithDisabledStatus() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        HabitAssign habitAssignWithToDoList = ModelUtils.getHabitAssign();

        UserToDoListItem userToDoListItem = ModelUtils.getUserToDoListItem();
        userToDoListItem.setStatus(ToDoListItemStatus.DISABLED);

        habitAssignWithToDoList.setUserToDoListItems(List.of(userToDoListItem));

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of())
            .customToDoListItemDto(List.of())
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(habitAssignWithToDoList));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssignWithToDoList));

        when(customToDoListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of());

        habitAssignService
            .fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userToDoListItemRepo).saveAll(List.of());
        verify(userToDoListItemRepo).deleteAll(List.of());

        verify(toDoListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(toDoListItemService).saveUserToDoListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customToDoListItemRepo).findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customToDoListItemRepo).saveAll(List.of());
        verify(customToDoListItemRepo).deleteAll(List.of());

        BulkSaveCustomToDoListItemDto bulkSaveCustomToDoListItemDto =
            new BulkSaveCustomToDoListItemDto(List.of());

        verify(customToDoListItemService).save(bulkSaveCustomToDoListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteUserToDoListWithStatusesUpdateAndDeleteItems() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        ToDoListItemStatus oldStatus = ToDoListItemStatus.ACTIVE;
        ToDoListItemStatus newStatus = ToDoListItemStatus.DONE;
        UserToDoListItemResponseDto responseDto = ModelUtils.getUserToDoListItemResponseDto();
        responseDto.setStatus(newStatus);

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of(responseDto))
            .customToDoListItemDto(List.of())
            .build();

        HabitAssign habitAssignWithToDoList = ModelUtils.getHabitAssign();

        UserToDoListItem firstUserToDoListItem = ModelUtils.getUserToDoListItem();
        firstUserToDoListItem.setStatus(oldStatus);
        firstUserToDoListItem.setHabitAssign(null);

        UserToDoListItem secondUserToDoListItem = ModelUtils.getUserToDoListItem();
        secondUserToDoListItem.setId(firstUserToDoListItem.getId() + 1);

        habitAssignWithToDoList
            .setUserToDoListItems(List.of(firstUserToDoListItem, secondUserToDoListItem));

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(habitAssignWithToDoList));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssignWithToDoList));

        when(customToDoListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of());

        habitAssignService
            .fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        UserToDoListItem userToDoListItemToSave = ModelUtils.getUserToDoListItem();
        userToDoListItemToSave.setStatus(newStatus);
        userToDoListItemToSave.setHabitAssign(null);

        verify(userToDoListItemRepo).saveAll(List.of(userToDoListItemToSave));
        verify(userToDoListItemRepo).deleteAll(List.of(secondUserToDoListItem));

        verify(toDoListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(toDoListItemService).saveUserToDoListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customToDoListItemRepo).findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customToDoListItemRepo).saveAll(List.of());
        verify(customToDoListItemRepo).deleteAll(List.of());

        BulkSaveCustomToDoListItemDto bulkSaveCustomToDoListItemDto =
            new BulkSaveCustomToDoListItemDto(List.of());

        verify(customToDoListItemService).save(bulkSaveCustomToDoListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteUserToDoListWithStatusesWithDuplicateThrowsBadRequestException() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        UserToDoListItemResponseDto responseDto = ModelUtils.getUserToDoListItemResponseDto();
        UserToDoListItemResponseDto sameResponseDto = ModelUtils.getUserToDoListItemResponseDto();

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of(responseDto, sameResponseDto))
            .customToDoListItemDto(List.of())
            .build();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> habitAssignService
            .fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language));

        assertEquals(ErrorMessage.DUPLICATED_USER_TO_DO_LIST_ITEM, exception.getMessage());

        verify(habitAssignRepo, times(0)).findByHabitIdAndUserId(anyLong(), anyLong());
        verify(userToDoListItemRepo, times(0)).saveAll(anyList());
        verify(userToDoListItemRepo, times(0)).deleteAll(anyList());

        verify(toDoListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(toDoListItemService, times(0))
            .saveUserToDoListItems(anyLong(), anyLong(), anyList(), anyString());

        verify(customToDoListItemRepo, times(0))
            .findAllByUserIdAndHabitId(anyLong(), anyLong());
        verify(customToDoListItemRepo, times(0)).saveAll(anyList());
        verify(customToDoListItemRepo, times(0)).deleteAll(anyList());

        verify(customToDoListItemService, times(0)).save(any(), anyLong(), anyLong());
    }

    @Test
    void updateAndDeleteUserToDoListWithStatusesWithNotFoundHabitAssignThrowsNotFoundException() {
        Long userId = 1L;
        Long habitAssignId = 1L;

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of())
            .customToDoListItemDto(List.of())
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> habitAssignService
            .fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language));

        assertEquals(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_WITH_CURRENT_USER_ID_AND_HABIT_ASSIGN_ID + habitAssignId,
            exception.getMessage());

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);

        verify(userToDoListItemRepo, times(0)).saveAll(anyList());
        verify(userToDoListItemRepo, times(0)).deleteAll(anyList());

        verify(toDoListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(toDoListItemService, times(0))
            .saveUserToDoListItems(anyLong(), anyLong(), anyList(), anyString());

        verify(customToDoListItemRepo, times(0))
            .findAllByUserIdAndHabitId(anyLong(), anyLong());
        verify(customToDoListItemRepo, times(0)).saveAll(anyList());
        verify(customToDoListItemRepo, times(0)).deleteAll(anyList());

        verify(customToDoListItemService, times(0)).save(any(), anyLong(), anyLong());
    }

    @Test
    void saveCustomToDoListWithStatuses() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        CustomToDoListItemResponseDto responseDto = ModelUtils.getCustomToDoListItemResponseDto();
        responseDto.setId(null);

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of())
            .customToDoListItemDto(List.of(responseDto))
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(customToDoListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of());

        habitAssignService.fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userToDoListItemRepo).saveAll(List.of());
        verify(userToDoListItemRepo).deleteAll(List.of());

        verify(toDoListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(toDoListItemService).saveUserToDoListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customToDoListItemRepo).findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customToDoListItemRepo).saveAll(List.of());
        verify(customToDoListItemRepo).deleteAll(List.of());

        CustomToDoListItemSaveRequestDto requestDto =
            ModelUtils.getCustomToDoListItemWithStatusSaveRequestDto();
        BulkSaveCustomToDoListItemDto bulkSaveUserToDoListItemDto =
            new BulkSaveCustomToDoListItemDto(List.of(requestDto));

        verify(customToDoListItemService).save(bulkSaveUserToDoListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void saveCustomToDoListWithStatusesWithDuplicateThrowsBadRequestException() {
        Long userId = 1L;
        Long habitAssignId = 1L;

        CustomToDoListItemResponseDto responseDto = ModelUtils.getCustomToDoListItemResponseDto();
        responseDto.setId(null);
        CustomToDoListItemResponseDto sameResponseDto = ModelUtils.getCustomToDoListItemResponseDto();
        sameResponseDto.setId(null);

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of())
            .customToDoListItemDto(List.of(responseDto, sameResponseDto))
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> habitAssignService.fullUpdateUserAndCustomToDoLists(userId, habitAssignId,
                dto, language));

        assertEquals(ErrorMessage.DUPLICATED_CUSTOM_TO_DO_LIST_ITEM, exception.getMessage());

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(2)).findById(habitAssignId);

        verify(userToDoListItemRepo).saveAll(List.of());
        verify(userToDoListItemRepo).deleteAll(List.of());

        verify(toDoListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(toDoListItemService).saveUserToDoListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customToDoListItemRepo).findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customToDoListItemRepo).saveAll(List.of());
        verify(customToDoListItemRepo).deleteAll(List.of());

        verify(customToDoListItemService, times(0)).save(any(), anyLong(), anyLong());
    }

    @Test
    void updateAndDeleteCustomToDoListWithStatusesUpdateItem() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        ToDoListItemStatus newStatus = ToDoListItemStatus.DONE;
        CustomToDoListItemResponseDto responseDto = ModelUtils.getCustomToDoListItemResponseDto();
        responseDto.setStatus(newStatus);

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of())
            .customToDoListItemDto(List.of(responseDto))
            .build();

        CustomToDoListItem customToDoListItem = ModelUtils.getCustomToDoListItem();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(customToDoListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of(customToDoListItem));

        habitAssignService
            .fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userToDoListItemRepo).saveAll(List.of());
        verify(userToDoListItemRepo).deleteAll(List.of());

        verify(toDoListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(toDoListItemService).saveUserToDoListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customToDoListItemRepo)
            .findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());

        CustomToDoListItem customToDoListItemToSave = ModelUtils.getCustomToDoListItem();
        customToDoListItemToSave.setStatus(newStatus);

        verify(customToDoListItemRepo).saveAll(List.of(customToDoListItemToSave));
        verify(customToDoListItemRepo).deleteAll(List.of());

        BulkSaveCustomToDoListItemDto bulkSaveUserToDoListItemDto =
            new BulkSaveCustomToDoListItemDto(List.of());

        verify(customToDoListItemService).save(bulkSaveUserToDoListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteCustomToDoListWithStatusesWithNonExistentItemThrowsNotFoundException() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        CustomToDoListItemResponseDto responseDto = ModelUtils.getCustomToDoListItemResponseDto();

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of())
            .customToDoListItemDto(List.of(responseDto))
            .build();

        CustomToDoListItem customToDoListItem = ModelUtils.getCustomToDoListItem();
        customToDoListItem.setId(responseDto.getId() + 1);

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(customToDoListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of(customToDoListItem));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> habitAssignService
            .fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language));

        assertEquals(ErrorMessage.CUSTOM_TO_DO_LIST_ITEM_WITH_THIS_ID_NOT_FOUND + responseDto.getId(),
            exception.getMessage());

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(2)).findById(habitAssignId);

        verify(userToDoListItemRepo).saveAll(List.of());
        verify(userToDoListItemRepo).deleteAll(List.of());

        verify(toDoListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(toDoListItemService).saveUserToDoListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customToDoListItemRepo)
            .findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customToDoListItemRepo, times(0)).saveAll(anyList());
        verify(customToDoListItemRepo, times(0)).deleteAll(anyList());

        verify(customToDoListItemService, times(0)).save(any(), anyLong(), anyLong());
    }

    @Test
    void updateAndDeleteCustomToDoListWithStatusesUpdateItemWithDisabledStatus() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        ToDoListItemStatus newStatus = ToDoListItemStatus.DONE;
        CustomToDoListItemResponseDto responseDto = ModelUtils.getCustomToDoListItemResponseDto();
        responseDto.setStatus(newStatus);

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of())
            .customToDoListItemDto(List.of(responseDto))
            .build();

        CustomToDoListItem customToDoListItem = ModelUtils.getCustomToDoListItem();
        customToDoListItem.setStatus(ToDoListItemStatus.DISABLED);

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(customToDoListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of(customToDoListItem));

        habitAssignService
            .fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userToDoListItemRepo).saveAll(List.of());
        verify(userToDoListItemRepo).deleteAll(List.of());

        verify(toDoListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(toDoListItemService).saveUserToDoListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customToDoListItemRepo)
            .findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());

        CustomToDoListItem customToDoListItemToSave = ModelUtils.getCustomToDoListItem();
        customToDoListItemToSave.setStatus(newStatus);

        verify(customToDoListItemRepo).saveAll(List.of(customToDoListItemToSave));
        verify(customToDoListItemRepo).deleteAll(List.of());

        BulkSaveCustomToDoListItemDto bulkSaveUserToDoListItemDto =
            new BulkSaveCustomToDoListItemDto(List.of());

        verify(customToDoListItemService).save(bulkSaveUserToDoListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteCustomToDoListWithStatusesDeleteItem() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        CustomToDoListItem customToDoListItem = ModelUtils.getCustomToDoListItem();

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of())
            .customToDoListItemDto(List.of())
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(customToDoListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of(customToDoListItem));

        habitAssignService
            .fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userToDoListItemRepo).saveAll(List.of());
        verify(userToDoListItemRepo).deleteAll(List.of());

        verify(toDoListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(toDoListItemService).saveUserToDoListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customToDoListItemRepo)
            .findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customToDoListItemRepo).saveAll(List.of());
        verify(customToDoListItemRepo).deleteAll(List.of(customToDoListItem));

        BulkSaveCustomToDoListItemDto bulkSaveUserToDoListItemDto =
            new BulkSaveCustomToDoListItemDto(List.of());

        verify(customToDoListItemService).save(bulkSaveUserToDoListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteCustomToDoListWithStatusesDeleteItemWithDisabledStatus() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        CustomToDoListItem customToDoListItem = ModelUtils.getCustomToDoListItem();
        customToDoListItem.setStatus(ToDoListItemStatus.DISABLED);

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of())
            .customToDoListItemDto(List.of())
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(customToDoListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of(customToDoListItem));

        habitAssignService
            .fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userToDoListItemRepo).saveAll(List.of());
        verify(userToDoListItemRepo).deleteAll(List.of());

        verify(toDoListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(toDoListItemService).saveUserToDoListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customToDoListItemRepo)
            .findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customToDoListItemRepo).saveAll(List.of());
        verify(customToDoListItemRepo).deleteAll(List.of());

        BulkSaveCustomToDoListItemDto bulkSaveUserToDoListItemDto =
            new BulkSaveCustomToDoListItemDto(List.of());

        verify(customToDoListItemService).save(bulkSaveUserToDoListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteCustomToDoListWithStatusesUpdateAndDeleteItems() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        ToDoListItemStatus newStatus = ToDoListItemStatus.DONE;
        CustomToDoListItemResponseDto responseDto = ModelUtils.getCustomToDoListItemResponseDto();
        responseDto.setStatus(newStatus);

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of())
            .customToDoListItemDto(List.of(responseDto))
            .build();

        CustomToDoListItem firstCustomToDoListItem = ModelUtils.getCustomToDoListItem();
        firstCustomToDoListItem.setStatus(newStatus);

        CustomToDoListItem secondCustomToDoListItem = ModelUtils.getCustomToDoListItem();
        secondCustomToDoListItem.setId(responseDto.getId() + 1);

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(customToDoListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of(firstCustomToDoListItem, secondCustomToDoListItem));

        habitAssignService
            .fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userToDoListItemRepo).saveAll(List.of());
        verify(userToDoListItemRepo).deleteAll(List.of());

        verify(toDoListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(toDoListItemService).saveUserToDoListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customToDoListItemRepo)
            .findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());

        CustomToDoListItem customToDoListItemToSave = ModelUtils.getCustomToDoListItem();
        customToDoListItemToSave.setStatus(newStatus);

        verify(customToDoListItemRepo).saveAll(List.of(customToDoListItemToSave));
        verify(customToDoListItemRepo).deleteAll(List.of(secondCustomToDoListItem));

        BulkSaveCustomToDoListItemDto bulkSaveUserToDoListItemDto =
            new BulkSaveCustomToDoListItemDto(List.of());

        verify(customToDoListItemService).save(bulkSaveUserToDoListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteCustomToDoListWithStatusesWithDuplicateThrowsBadRequestException() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        CustomToDoListItemResponseDto responseDto = ModelUtils.getCustomToDoListItemResponseDto();
        CustomToDoListItemResponseDto sameResponseDto = ModelUtils.getCustomToDoListItemResponseDto();

        UserToDoAndCustomToDoListsDto dto = UserToDoAndCustomToDoListsDto.builder()
            .userToDoListItemDto(List.of())
            .customToDoListItemDto(List.of(responseDto, sameResponseDto))
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> habitAssignService
            .fullUpdateUserAndCustomToDoLists(userId, habitAssignId, dto, language));
        assertEquals(ErrorMessage.DUPLICATED_CUSTOM_TO_DO_LIST_ITEM, exception.getMessage());

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo).findById(habitAssignId);

        verify(userToDoListItemRepo).saveAll(List.of());
        verify(userToDoListItemRepo).deleteAll(List.of());

        verify(toDoListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(toDoListItemService).saveUserToDoListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customToDoListItemRepo, times(0))
            .findAllByUserIdAndHabitId(anyLong(), anyLong());
        verify(customToDoListItemRepo, times(0)).saveAll(anyList());
        verify(customToDoListItemRepo, times(0)).deleteAll(anyList());

        verify(customToDoListItemService, times(0)).save(any(), anyLong(), anyLong());
    }

    @Test
    void updateProgressNotificationHasDisplayedTest() {
        Long habitAssignId = 1L;
        Long userId = 2L;

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(new HabitAssign()));

        habitAssignService.updateProgressNotificationHasDisplayed(habitAssignId, userId);

        verify(habitAssignRepo).updateProgressNotificationHasDisplayed(habitAssignId, userId);
    }

    @Test
    void updateProgressNotificationHasDisplayedTrowsExceptionTest() {
        Long habitAssignId = 1L;
        Long userId = 2L;

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> habitAssignService.updateProgressNotificationHasDisplayed(habitAssignId, userId));

    }

    @Test
    void testInviteFriendForYourHabitWithEmailNotificationUserHasNoFriend() {
        Long friendId = 10L;
        Long habitId = 1L;
        Locale locale = Locale.of("en");
        List<Long> friendsList = List.of(10L);

        when(userRepo.findById(friendId)).thenReturn(Optional.of(user));
        when(userRepo.isFriend(userVO.getId(), friendId)).thenReturn(false);

        assertThrows(UserHasNoFriendWithIdException.class,
            () -> habitAssignService.inviteFriendForYourHabitWithEmailNotification(userVO, friendsList, habitId,
                locale));
    }

    @Test
    void testInviteFriendForYourHabitWithEmailNotificationFriendNotFound() {
        Long friendId = 10L;
        Long habitId = 1L;
        Locale locale = Locale.of("en");
        List<Long> friendsList = List.of(10L);

        when(userRepo.findById(friendId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> habitAssignService
                .inviteFriendForYourHabitWithEmailNotification(userVO, friendsList, habitId, locale));
    }

    @Test
    void testInviteFriendForYourHabitWithEmailNotificationHabitNotFound() {
        Long friendId = 10L;
        Long habitId = 1L;
        Locale locale = Locale.of("en");
        List<Long> friendsList = List.of(10L);

        when(userRepo.isFriend(userVO.getId(), friendId)).thenReturn(true);
        when(userRepo.findById(friendId)).thenReturn(Optional.of(getUser()));
        when(habitRepo.findById(habitId)).thenReturn(Optional.empty());
        when(modelMapper.map(any(), eq(UserVO.class))).thenReturn(getUserVO());

        assertThrows(NotFoundException.class,
            () -> habitAssignService
                .inviteFriendForYourHabitWithEmailNotification(userVO, friendsList, habitId, locale));
    }

    @Test
    void testInviteFriendForYourHabitWithEmailNotificationSuccessfulAssign() {
        Long friendId = 1L;
        Locale locale = Locale.of("en");
        User friend = getUser();
        Long habitId = 1L;

        when(userRepo.isFriend(userVO.getId(), friendId)).thenReturn(true);
        when(userRepo.findById(friendId)).thenReturn(Optional.of(friend));
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(modelMapper.map(friend, UserVO.class)).thenReturn(new UserVO());

        HabitAssign habitAssignCancelled = new HabitAssign();
        habitAssignCancelled.setId(1L);
        habitAssignCancelled.setStatus(HabitAssignStatus.CANCELLED);
        habitAssignCancelled.setHabit(Habit.builder()
            .id(1L)
            .habitTranslations(List.of(getHabitTranslation()))
            .build());

        when(habitAssignRepo.findByHabitIdAndUserIdAndStatusIsCancelledOrRequested(habitId, friendId)).thenReturn(
            habitAssignCancelled);
        when(habitAssignRepo.save(any(HabitAssign.class))).thenReturn(habitAssignCancelled);

        habitAssignService.inviteFriendForYourHabitWithEmailNotification(userVO, List.of(friendId), habitId,
            locale);

        verify(habitAssignRepo).save(any(HabitAssign.class));
        verify(habitAssignRepo).save(any(HabitAssign.class));
    }

    @Test
    void testInviteFriendForYourHabitWithEmailNotificationNewHabitAssign() {
        Locale locale = Locale.of("en");
        User friend = getUser();
        Long habitId = 1L;
        Long friendId = friend.getId();
        when(userRepo.isFriend(userVO.getId(), friendId)).thenReturn(true);
        when(userRepo.findById(friendId)).thenReturn(Optional.of(friend));
        when(habitRepo.findById(habitId)).thenReturn(Optional.of(habit));
        when(modelMapper.map(friend, UserVO.class)).thenReturn(new UserVO());
        when(habitAssignRepo.findByHabitIdAndUserIdAndStatusIsCancelledOrRequested(habitId, friendId)).thenReturn(null);
        when(habitAssignRepo.save(any())).thenReturn(getHabitAssign());
        when(toDoListItemRepo.getAllToDoListItemIdByHabitIdISContained(habitId)).thenReturn(List.of(1L));
        when(habitAssignRepo.save(any())).thenReturn(getHabitAssign());

        habitAssignService.inviteFriendForYourHabitWithEmailNotification(userVO, List.of(friendId), habitId,
            locale);

        verify(habitAssignRepo, times(1)).save(any(HabitAssign.class));
        verify(toDoListItemRepo).getAllToDoListItemIdByHabitIdISContained(habit.getId());
        verify(userNotificationService).createOrUpdateHabitInviteNotification(new UserVO(), userVO,
            habit.getId(), "");

    }

    @Test
    void confirmHabitInvitation() {
        Long habitAssignId = 1L;
        habitAssign.setStatus(HabitAssignStatus.REQUESTED);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));

        habitAssignService.confirmHabitInvitation(habitAssignId);

        verify(habitAssignRepo).save(habitAssign);
        assertEquals(HabitAssignStatus.INPROGRESS, habitAssign.getStatus());
    }

    @Test
    void confirmHabitInvitationWithInvalidId() {
        Long habitAssignId = 100L;

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> habitAssignService.confirmHabitInvitation(habitAssignId));
    }

    @Test
    void confirmHabitInvitationWithInvalidHabitStatus() {
        Long habitAssignId = 100L;

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));

        assertThrows(BadRequestException.class,
            () -> habitAssignService.confirmHabitInvitation(habitAssignId));
    }

    @Test
    void getAllHabitsWorkingDaysInfoForCurrentUserFriendsTest() {
        Long userId = 2L;
        Long habitId = 1L;
        List<Long> friendsIds = List.of(1L);
        List<HabitAssign> assignList = List.of(getHabitAssign());
        HabitWorkingDaysDto expected = HabitWorkingDaysDto.builder()
            .userId(1L)
            .duration(0)
            .workingDays(0)
            .build();

        when(habitAssignRepo.findFriendsIdsTrackingHabit(habitId, userId))
            .thenReturn(friendsIds);
        when(habitAssignRepo.findByUserIdsAndHabitId(friendsIds, habitId))
            .thenReturn(assignList);

        List<HabitWorkingDaysDto> allHabitsWorkingDaysInfoForCurrentUserFriends =
            habitAssignService.getAllHabitsWorkingDaysInfoForCurrentUserFriends(userId, habitId);

        assertEquals(expected.getUserId(), allHabitsWorkingDaysInfoForCurrentUserFriends.getFirst().getUserId());
        assertEquals(expected.getWorkingDays(),
            allHabitsWorkingDaysInfoForCurrentUserFriends.getFirst().getWorkingDays());
        assertEquals(expected.getDuration(), allHabitsWorkingDaysInfoForCurrentUserFriends.getFirst().getDuration());
    }

    @Test
    void getAllHabitsWorkingDaysInfoForCurrentUserFriendsTestWithNoFriendAssigned() {
        Long userId = 2L;
        Long habitId = 1L;

        when(habitAssignRepo.findFriendsIdsTrackingHabit(habitId, userId))
            .thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class,
            () -> habitAssignService.getAllHabitsWorkingDaysInfoForCurrentUserFriends(userId, habitId));
    }
}
