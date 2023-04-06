package greencity.service;

import greencity.ModelUtils;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.habit.HabitAssignDto;
import greencity.dto.habit.HabitAssignManagementDto;
import greencity.dto.habit.HabitAssignPropertiesDto;
import greencity.dto.habit.HabitAssignStatDto;
import greencity.dto.habit.HabitAssignVO;
import greencity.dto.habit.HabitDto;
import greencity.dto.habit.HabitEnrollDto;
import greencity.dto.habit.HabitVO;
import greencity.dto.habit.HabitsDateEnrollmentDto;
import greencity.dto.habit.UpdateUserShoppingListDto;
import greencity.dto.habit.UserShoppingAndCustomShoppingListsDto;
import greencity.dto.habitstatuscalendar.HabitStatusCalendarVO;
import greencity.dto.shoppinglistitem.BulkSaveCustomShoppingListItemDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemResponseDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemSaveRequestDto;
import greencity.dto.shoppinglistitem.ShoppingListItemDto;
import greencity.dto.shoppinglistitem.ShoppingListItemWithStatusRequestDto;
import greencity.dto.user.UserShoppingListItemResponseDto;
import greencity.dto.user.UserVO;
import greencity.entity.CustomShoppingListItem;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.HabitStatusCalendar;
import greencity.entity.HabitTranslation;
import greencity.entity.Language;
import greencity.entity.ShoppingListItem;
import greencity.entity.User;
import greencity.entity.UserShoppingListItem;
import greencity.entity.localization.ShoppingListItemTranslation;
import greencity.enums.HabitAssignStatus;
import greencity.enums.ShoppingListItemStatus;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.InvalidStatusException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.ShoppingListItemNotFoundException;
import greencity.exception.exceptions.UserAlreadyHasEnrolledHabitAssign;
import greencity.exception.exceptions.UserAlreadyHasHabitAssignedException;
import greencity.exception.exceptions.UserAlreadyHasMaxNumberOfActiveHabitAssigns;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.exception.exceptions.UserHasReachedOutOfEnrollRange;
import greencity.repository.CustomShoppingListItemRepo;
import greencity.repository.HabitAssignRepo;
import greencity.repository.HabitRepo;
import greencity.repository.HabitStatusCalendarRepo;
import greencity.repository.ShoppingListItemRepo;
import greencity.repository.ShoppingListItemTranslationRepo;
import greencity.repository.UserShoppingListItemRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static greencity.ModelUtils.HABIT_ASSIGN_IN_PROGRESS;
import static greencity.ModelUtils.getFullHabitAssign;
import static greencity.ModelUtils.getFullHabitAssignDto;
import static greencity.ModelUtils.getHabitAssignUserDurationDto;
import static greencity.ModelUtils.getHabitDto;
import static greencity.ModelUtils.getHabitAssign;
import static greencity.ModelUtils.getShoppingListItem;
import static greencity.ModelUtils.getShoppingListItemTranslationList;
import static greencity.ModelUtils.getUpdateUserShoppingListDto;
import static greencity.ModelUtils.getUserShoppingListItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HabitAssignServiceImplTest {
    @Mock
    private HabitRepo habitRepo;
    @Mock
    private HabitAssignRepo habitAssignRepo;
    @Mock
    private ShoppingListItemRepo shoppingListItemRepo;
    @Mock
    private UserShoppingListItemRepo userShoppingListItemRepo;
    @Mock
    private CustomShoppingListItemRepo customShoppingListItemRepo;
    @Mock
    private HabitStatusCalendarRepo habitStatusCalendarRepo;
    @Mock
    private HabitStatusCalendarService habitStatusCalendarService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private HabitStatisticService habitStatisticService;
    @Mock
    private ShoppingListItemTranslationRepo shoppingListItemTranslationRepo;
    @InjectMocks
    private HabitAssignServiceImpl habitAssignService;
    @Mock
    private ShoppingListItemService shoppingListItemService;
    @Mock
    private CustomShoppingListItemService customShoppingListItemService;

    private static ZonedDateTime zonedDateTime = ZonedDateTime.now();

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

    private HabitVO habitVO =
        HabitVO.builder().id(1L).image("src/main/resources/static/css/background-image-footer.svg").build();

    private UserVO userVO = UserVO.builder().id(1L).build();

    private User user = User.builder().id(1L).build();

    private HabitAssign habitAssign = getHabitAssign();

    private HabitAssign fullHabitAssign = getFullHabitAssign();

    private HabitAssign habitAssignNew = HabitAssign.builder()
        .user(user).habit(habit).build();

    private HabitAssignStatDto habitAssignStatDto = HabitAssignStatDto.builder()
        .status(HabitAssignStatus.ACQUIRED).build();

    private List<HabitAssignDto> habitAssignDtos = Collections.singletonList(habitAssignDto);

    private List<HabitAssign> habitAssigns = Collections.singletonList(habitAssign);

    private List<HabitAssign> fullHabitAssigns = Collections.singletonList(fullHabitAssign);

    private HabitAssignPropertiesDto habitAssignPropertiesDto =
        HabitAssignPropertiesDto.builder().duration(14).defaultShoppingListItems(List.of(1L)).build();

    private String language = "en";

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
    void assignDefaultHabitForUserThatWasCancelled() {
        habitAssign.setStatus(HabitAssignStatus.CANCELLED);
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(habitAssignRepo.findByHabitIdAndUserIdAndStatusIsCancelled(habit.getId(), user.getId()))
            .thenReturn(habitAssign);
        when(modelMapper.map(habitAssign, HabitAssignManagementDto.class)).thenReturn(habitAssignManagementDto);
        HabitAssignManagementDto actual = habitAssignService.assignDefaultHabitForUser(habit.getId(), userVO);
        assertEquals(habitAssignManagementDto, actual);
    }

    @Test
    void assignDefaultHabitForUserAlreadyHasTheHabit() {
        when(habitAssignRepo.findAllByUserId(userVO.getId())).thenReturn(List.of(HABIT_ASSIGN_IN_PROGRESS));

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
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(habitAssignRepo.findByHabitIdAndUserIdAndStatusIsCancelled(habit.getId(), user.getId()))
            .thenReturn(habitAssign);
        when(modelMapper.map(habitAssign, HabitAssignManagementDto.class)).thenReturn(habitAssignManagementDto);
        HabitAssignManagementDto actual =
            habitAssignService.assignCustomHabitForUser(habit.getId(), userVO, habitAssignPropertiesDto);
        assertEquals(habitAssignManagementDto, actual);
    }

    @Test
    void getEndDate() {
        HabitAssignDto habitAssign = ModelUtils.getHabitAssignDto();
        habitAssign.setDuration(2);
        ZonedDateTime expected = habitAssign.getCreateDateTime().plusDays(habitAssign.getDuration());
        assertEquals(expected, habitAssignService.getEndDate(habitAssign));
    }

    @Test
    void findHabitAssignsBetweenDates() {
        HabitAssign habit1 = ModelUtils.getHabitAssign();
        HabitAssign habit2 = ModelUtils.getHabitAssign();
        habit2.setId(2L);
        habit2.getHabit().setId(2L);
        habit1.setDuration(3);
        habit2.setDuration(3);
        ZonedDateTime creation = ZonedDateTime.of(2020, 12, 28,
            12, 12, 12, 12, ZoneId.of("Europe/Kiev"));
        habit1.setCreateDate(creation);
        habit2.setCreateDate(creation);
        habit1.setHabitStatusCalendars(Collections.singletonList(HabitStatusCalendar
            .builder().enrollDate(LocalDate.of(2020, 12, 28)).build()));
        habit2.setHabitStatusCalendars(Collections.emptyList());
        List<HabitAssign> habitAssignList = Arrays.asList(habit1, habit2);
        List<HabitsDateEnrollmentDto> dtos = Arrays.asList(
            HabitsDateEnrollmentDto.builder().enrollDate(LocalDate.of(2020, 12, 27))
                .habitAssigns(Collections.emptyList()).build(),
            HabitsDateEnrollmentDto.builder().enrollDate(LocalDate.of(2020, 12, 28))
                .habitAssigns(Arrays.asList(
                    new HabitEnrollDto(1L, "", "", true),
                    new HabitEnrollDto(2L, "", "", false)))
                .build(),
            HabitsDateEnrollmentDto.builder().enrollDate(LocalDate.of(2020, 12, 29))
                .habitAssigns(Arrays.asList(
                    new HabitEnrollDto(1L, "", "", false),
                    new HabitEnrollDto(2L, "", "", false)))
                .build());

        when(habitAssignRepo.findAllHabitAssignsBetweenDates(anyLong(),
            eq(LocalDate.of(2020, 12, 27)), eq(LocalDate.of(2020, 12, 29))))
                .thenReturn(habitAssignList);

        assertEquals(dtos, habitAssignService.findHabitAssignsBetweenDates(13L,
            LocalDate.of(2020, 12, 27), LocalDate.of(2020, 12, 29),
            "en"));
    }

    @Test
    void findHabitAssignsBetweenDatesThrowsBadRequestExceptionWhenFromDateIsLaterThenTo() {
        Long userId = 2L;
        LocalDate from = LocalDate.now();
        LocalDate to = from.minusDays(1);
        String language = AppConstant.DEFAULT_LANGUAGE_CODE;

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> habitAssignService.findHabitAssignsBetweenDates(userId, from, to, language));

        assertEquals(ErrorMessage.INVALID_DATE_RANGE, exception.getMessage());

        verify(habitAssignRepo, times(0)).findAllHabitAssignsBetweenDates(anyLong(), any(), any());
    }

    @Test
    void assignCustomHabitForUser() {
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(habitAssignRepo.save(any())).thenReturn(habitAssign);
        when(modelMapper.map(habitAssign, HabitAssignManagementDto.class)).thenReturn(habitAssignManagementDto);
        HabitAssignManagementDto actual = habitAssignService.assignCustomHabitForUser(habit.getId(), userVO,
            habitAssignPropertiesDto);
        assertEquals(habitAssignManagementDto, actual);
    }

    @Test
    void updateStatusByHabitIdAndUserId() {
        when(habitAssignRepo.findByHabitIdAndUserId(1L, 1L)).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(habitAssignRepo.save(habitAssign), HabitAssignManagementDto.class))
            .thenReturn(habitAssignManagementDto);
        assertEquals(habitAssignManagementDto,
            habitAssignService.updateStatusByHabitIdAndUserId(1L, 1L, habitAssignStatDto));
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

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));
        when(habitStatusCalendarRepo.findHabitStatusCalendarByEnrollDateAndHabitAssign(date, habitAssign))
            .thenReturn(habitStatusCalendar);

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
        Long habitAssignId = 1L;
        Long userId = 2L;

        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        habitAssign.getUser().setId(userId);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));

        habitAssignService.deleteHabitAssign(habitAssignId, userId);

        verify(userShoppingListItemRepo).deleteShoppingListItemsByHabitAssignId(habitAssignId);
        verify(habitAssignRepo).delete(habitAssign);
    }

    @Test
    void deleteHabitAssignThrowsExceptionWhenHabitAssignNotExists() {
        Long habitAssignId = 1L;
        Long userId = 2L;

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.empty());

        NotFoundException exception =
            assertThrows(NotFoundException.class, () -> habitAssignService.deleteHabitAssign(habitAssignId, userId));

        assertEquals(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId, exception.getMessage());

        verify(userShoppingListItemRepo, times(0)).deleteShoppingListItemsByHabitAssignId(anyLong());
        verify(habitAssignRepo, times(0)).delete(any());
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

        verify(userShoppingListItemRepo, times(0)).deleteShoppingListItemsByHabitAssignId(anyLong());
        verify(habitAssignRepo, times(0)).delete(any());
    }

    @Test
    void getAllHabitAssignsByHabitIdAndStatusNotCancelled() {
        Long habitId = 1L;

        Language language = ModelUtils.getLanguage();
        language.setCode("en");

        HabitTranslation translation = ModelUtils.getHabitTranslation();
        translation.setLanguage(language);

        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        HabitAssignDto habitAssignDto = ModelUtils.getHabitAssignDto();

        HabitDto habitDto = ModelUtils.getHabitDto();
        habitAssign.setHabit(habit);

        habit.setHabitTranslations(Collections.singletonList(translation));

        when(habitAssignRepo.findAllByHabitId(habitId)).thenReturn(Collections.singletonList(habitAssign));
        when(modelMapper.map(habitAssign, HabitAssignDto.class)).thenReturn(habitAssignDto);
        when(modelMapper.map(translation, HabitDto.class)).thenReturn(habitDto);
        HabitAssignDto actual =
            habitAssignService.getAllHabitAssignsByHabitIdAndStatusNotCancelled(habitId, language.getCode()).get(0);

        assertEquals(habitAssignDto, actual);

    }

    @Test
    void getNumberHabitAssignsByHabitIdAndStatusTest() {
        Long habitId = 1L;

        Language language = ModelUtils.getLanguage();
        language.setCode("en");

        HabitTranslation translation = ModelUtils.getHabitTranslation();
        translation.setLanguage(language);

        List<HabitAssign> habitAssignList = Collections.singletonList(ModelUtils.getHabitAssign());

        when(habitAssignRepo.findAllHabitAssignsByStatusAndHabitId(HabitAssignStatus.ACQUIRED, habitId))
            .thenReturn(habitAssignList);

        Long actual = habitAssignService.getNumberHabitAssignsByHabitIdAndStatus(habitId, HabitAssignStatus.ACQUIRED);

        assertEquals(habitAssignList.size(), actual);

    }

    @Test
    void deleteAllHabitAssignsByHabit() {
        HabitVO habit = ModelUtils.getHabitVO();
        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        HabitAssignVO habitAssignVO = ModelUtils.getHabitAssignVO();

        when(habitAssignRepo.findAllByHabitId(any())).thenReturn(Collections.singletonList(habitAssign));
        when(modelMapper.map(habitAssign, HabitAssignVO.class)).thenReturn(habitAssignVO);
        habitAssignService.deleteAllHabitAssignsByHabit(habit);

        verify(habitStatisticService).deleteAllStatsByHabitAssign(habitAssignVO);
        verify(habitAssignRepo).delete(habitAssign);
        verify(habitAssignRepo, times(1)).delete(any());
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
        UserShoppingListItem userShoppingListItemCustom = ModelUtils.getFullUserShoppingListItem();
        HabitAssignDto habitAssignDtoCustom = ModelUtils.getHabitAssignDto();
        habitAssignDtoCustom.setCreateDateTime(zonedDateTime);

        when(habitAssignRepo.findAllByUserId(1L)).thenReturn(habitAssigns);
        when(modelMapper.map(habitAssign, HabitAssignDto.class)).thenReturn(habitAssignDtoCustom);
        when(userShoppingListItemRepo.getAllAssignedShoppingListItemsFull(any()))
            .thenReturn(List.of(userShoppingListItemCustom));

        HabitTranslation habitTranslation = habitAssign.getHabit().getHabitTranslations().stream().findFirst().get();
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(ModelUtils.getHabitAssignDto().getHabit());

        habitAssignDtos.get(0).getHabit().setShoppingListItems(
            List.of(ShoppingListItemDto.builder()
                .id(userShoppingListItemCustom.getId())
                .status(userShoppingListItemCustom.getStatus().toString())
                .text(userShoppingListItemCustom.getShoppingListItem().getTranslations().get(0).getContent())
                .build()));

        List<HabitAssignDto> actual = habitAssignService.getAllHabitAssignsByUserIdAndStatusNotCancelled(1L, "en");
        assertEquals(habitAssignDtos, actual);
    }

    @Test
    void getUserShoppingAndCustomShoppingLists() {
        Long habitAssignId = 2L;
        Long userId = 3L;

        String language = "en";

        List<CustomShoppingListItemResponseDto> customShoppingListItemResponseDtos =
            List.of(ModelUtils.getCustomShoppingListItemResponseDto());
        List<UserShoppingListItemResponseDto> userShoppingListItemResponseDtos =
            List.of(ModelUtils.getUserShoppingListItemResponseDto());
        UserShoppingAndCustomShoppingListsDto expected =
            UserShoppingAndCustomShoppingListsDto
                .builder()
                .customShoppingListItemDto(customShoppingListItemResponseDtos)
                .userShoppingListItemDto(userShoppingListItemResponseDtos)
                .build();

        when(shoppingListItemService.getUserShoppingListByHabitAssignId(userId, habitAssignId, language))
            .thenReturn(userShoppingListItemResponseDtos);
        when(
            customShoppingListItemService.findAllAvailableCustomShoppingListItemsByHabitAssignId(userId, habitAssignId))
                .thenReturn(customShoppingListItemResponseDtos);

        UserShoppingAndCustomShoppingListsDto actual =
            habitAssignService.getUserShoppingAndCustomShoppingLists(userId, habitAssignId, language);

        assertEquals(expected, actual);

        verify(shoppingListItemService).getUserShoppingListByHabitAssignId(userId, habitAssignId, language);
        verify(customShoppingListItemService).findAllAvailableCustomShoppingListItemsByHabitAssignId(userId,
            habitAssignId);
    }

    @Test
    void getUserShoppingListItemAndUserCustomShoppingListTest() {
        Habit habit1 = Habit.builder().id(3L)
            .complexity(1).build();

        List<HabitAssign> habitAssignList =
            List.of(ModelUtils.getHabitAssign(2L, habit1, HabitAssignStatus.INPROGRESS));

        List<CustomShoppingListItemResponseDto> customShoppingListItemResponseDtos =
            List.of(ModelUtils.getCustomShoppingListItemResponseDtoWithStatusInProgress());

        List<UserShoppingListItemResponseDto> userShoppingListItemResponseDtos =
            List.of(UserShoppingListItemResponseDto
                .builder().id(1L).status(ShoppingListItemStatus.INPROGRESS).build());

        List<UserShoppingAndCustomShoppingListsDto> expected = List.of(
            UserShoppingAndCustomShoppingListsDto
                .builder()
                .customShoppingListItemDto(customShoppingListItemResponseDtos)
                .userShoppingListItemDto(userShoppingListItemResponseDtos)
                .build());

        when(habitAssignRepo.findAllByUserIdAndStatusIsInProgress(1L)).thenReturn(habitAssignList);
        when(shoppingListItemService.getUserShoppingListItemsByHabitAssignIdAndStatusInProgress(2L, "en"))
            .thenReturn(userShoppingListItemResponseDtos);
        when(customShoppingListItemService.findAllCustomShoppingListItemsWithStatusInProgress(1L, 3L))
            .thenReturn(customShoppingListItemResponseDtos);

        assertEquals(expected, habitAssignService.getListOfUserAndCustomShoppingListsWithStatusInprogress(1L, "en"));

        verify(habitAssignRepo).findAllByUserIdAndStatusIsInProgress(anyLong());
        verify(shoppingListItemService).getUserShoppingListItemsByHabitAssignIdAndStatusInProgress(anyLong(), any());
        verify(customShoppingListItemService).findAllCustomShoppingListItemsWithStatusInProgress(anyLong(), anyLong());
    }

    @Test
    void getUserShoppingListItemAndUserCustomShoppingListWithNotFoundExceptionTest() {
        when(habitAssignRepo.findAllByUserIdAndStatusIsInProgress(1L)).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class, () -> habitAssignService
            .getListOfUserAndCustomShoppingListsWithStatusInprogress(1L, "en"));

        verify(habitAssignRepo).findAllByUserIdAndStatusIsInProgress(anyLong());
    }

    @Test
    void getAllHabitAssignsByUserIdAndStatusAcquired() {
        List<ShoppingListItemTranslation> list = getShoppingListItemTranslationList();
        when(habitAssignRepo.findAllByUserIdAndStatusAcquired(1L)).thenReturn(fullHabitAssigns);
        when(modelMapper.map(fullHabitAssign, HabitAssignDto.class)).thenReturn(habitAssignDto);
        when(shoppingListItemTranslationRepo.findShoppingListByHabitIdAndByLanguageCode("en", 1L))
            .thenReturn(list);
        HabitTranslation habitTranslation = habitAssign.getHabit().getHabitTranslations().stream().findFirst().get();
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(ModelUtils.getHabitDto());
        List<HabitAssignDto> actual = habitAssignService.getAllHabitAssignsByUserIdAndStatusAcquired(1L, "en");
        assertEquals(habitAssignDtos, actual);
    }

    @Test
    void getAllHabitAssignsByUserIdAndStatusAcquiredEmptyHabitAssign() {
        List<ShoppingListItemTranslation> list = getShoppingListItemTranslationList();
        when(habitAssignRepo.findAllByUserIdAndStatusAcquired(1L)).thenReturn(habitAssigns);
        when(modelMapper.map(habitAssign, HabitAssignDto.class)).thenReturn(habitAssignDto);
        when(shoppingListItemTranslationRepo.findShoppingListByHabitIdAndByLanguageCode("en", 1L))
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
    void cancelHabitAssign() {
        habitAssign.setStatus(HabitAssignStatus.INPROGRESS);
        habitAssignDto.setStatus(HabitAssignStatus.CANCELLED);

        when(habitAssignRepo.findByHabitIdAndUserIdAndStatusIsInprogress(1L, 1L)).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(habitAssign, HabitAssignDto.class)).thenReturn(habitAssignDto);

        HabitTranslation habitTranslation = habitAssign.getHabit().getHabitTranslations().stream().findFirst().get();
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(ModelUtils.getHabitDto());

        assertEquals(habitAssignDto, habitAssignService.cancelHabitAssign(1L, 1L));

        verify(habitAssignRepo).save(habitAssign);
    }

    @Test
    void getByHabitAssignIdAndUserId() {
        when(habitAssignRepo.findById(1L)).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(habitAssign, HabitAssignDto.class)).thenReturn(habitAssignDto);
        HabitTranslation habitTranslation = habitAssign.getHabit().getHabitTranslations().stream().findFirst().get();
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(ModelUtils.getHabitDto());
        assertEquals(habitAssignDto, habitAssignService.getByHabitAssignIdAndUserId(1L, 1L, language));
        verify(habitAssignRepo).findById(anyLong());
        verify(modelMapper, times(2)).map(any(), any());
    }

    @Test
    void enrollHabit() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        Long habitTranslationId = 4L;
        LocalDate localDate = LocalDate.now();
        String language = AppConstant.DEFAULT_LANGUAGE_CODE;

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
        when(modelMapper.map(habitAssign, HabitAssignDto.class)).thenReturn(habitAssignDto);
        when(modelMapper.map(translation, HabitDto.class)).thenReturn(habitDto);

        HabitAssignDto actualDto = habitAssignService.enrollHabit(habitAssignId, userId, localDate, language);
        assertEquals(1, habitAssign.getWorkingDays());
        assertEquals(habitAssignDto, actualDto);

        verify(habitAssignRepo).findById(habitAssignId);
        verify(modelMapper).map(habitAssign, HabitAssignVO.class);
        verify(habitStatusCalendarService).findHabitStatusCalendarByEnrollDateAndHabitAssign(localDate, habitAssignVO);
        verify(habitAssignRepo).save(habitAssign);
        verify(modelMapper).map(habitAssign, HabitAssignDto.class);
        verify(modelMapper).map(translation, HabitDto.class);
        verify(userShoppingListItemRepo)
            .getAllAssignedShoppingListItemsFull(habitAssignId);
    }

    @Test
    void enrollHabitThrowsNotFoundExceptionWhenHabitAssignNotExists() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        LocalDate localDate = LocalDate.now();
        String language = AppConstant.DEFAULT_LANGUAGE_CODE;

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
        verify(userShoppingListItemRepo, times(0))
            .getAllAssignedShoppingListItemsFull(anyLong());
    }

    @Test
    void enrollHabitThrowsUserHasNoPermissionToAccessExceptionWhenHabitAssignNotBelongToUser() {
        long habitAssignId = 2L;
        long userId = 3L;
        LocalDate localDate = LocalDate.now();
        String language = AppConstant.DEFAULT_LANGUAGE_CODE;

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
        verify(userShoppingListItemRepo, times(0))
            .getAllAssignedShoppingListItemsFull(anyLong());
    }

    @Test
    void enrollHabitThrowsUserAlreadyHasEnrolledHabitAssign() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        Long habitTranslationId = 4L;
        LocalDate localDate = LocalDate.now();
        String language = AppConstant.DEFAULT_LANGUAGE_CODE;

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
        verify(userShoppingListItemRepo, times(0))
            .getAllAssignedShoppingListItemsFull(anyLong());
    }

    @Test
    void enrollHabitThrowsUserHasReachedOutOfEnrollRangeWhenEnrollsInFuture() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        Long habitTranslationId = 4L;
        LocalDate localDate = LocalDate.now().plusDays(1);
        String language = AppConstant.DEFAULT_LANGUAGE_CODE;

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
        verify(userShoppingListItemRepo, times(0))
            .getAllAssignedShoppingListItemsFull(anyLong());
    }

    @Test
    void enrollHabitThrowsUserHasReachedOutOfEnrollRangeWhenPassedMaxDayOfAbilityToEnroll() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        Long habitTranslationId = 4L;
        LocalDate localDate = LocalDate.now().minusDays(AppConstant.MAX_PASSED_DAYS_OF_ABILITY_TO_ENROLL);
        String language = AppConstant.DEFAULT_LANGUAGE_CODE;

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
        verify(userShoppingListItemRepo, times(0))
            .getAllAssignedShoppingListItemsFull(anyLong());
    }

    @Test
    void findInprogressHabitAssignsOnDate() {

        Long id = 3L;
        LocalDate date = LocalDate.now();
        Language language = ModelUtils.getLanguage();
        language.setCode("en");

        HabitTranslation habitTranslation = ModelUtils.getHabitTranslation();
        habitAssign.getHabit().setHabitTranslations(Collections.singletonList(habitTranslation));

        when(habitAssignRepo.findAllInprogressHabitAssignsOnDate(anyLong(), any(LocalDate.class)))
            .thenReturn(Collections.singletonList(habitAssign));

        when(modelMapper.map(habitAssign, HabitAssignDto.class)).thenReturn(habitAssignDto);
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(habitDto);

        List<HabitAssignDto> dtoList =
            habitAssignService.findInprogressHabitAssignsOnDate(id, date, language.getCode());
        assertEquals(dtoList.get(0), habitAssignDto);

    }

    @Test
    void findInprogressHabitAssignsOnDateContent() {

        Long id = 3L;
        LocalDate date = LocalDate.now();
        Language language = ModelUtils.getLanguage();
        language.setCode("en");

        HabitTranslation habitTranslation = ModelUtils.getHabitTranslation();
        habitAssign.getHabit().setHabitTranslations(Collections.singletonList(habitTranslation));

        when(habitAssignRepo.findAllInprogressHabitAssignsOnDate(anyLong(), any(LocalDate.class)))
            .thenReturn(Collections.singletonList(habitAssign));

        when(modelMapper.map(habitAssign, HabitAssignDto.class)).thenReturn(habitAssignDto);
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(habitDto);

        List<HabitAssignDto> dtoList =
            habitAssignService.findInprogressHabitAssignsOnDateContent(id, date, language.getCode());
        assertEquals(dtoList.get(0), habitAssignDto);

    }

    @Test
    void addDefaultHabitIf() {

        UserVO userVo = ModelUtils.createUserVO2();
        UserVO userVo2 = new UserVO();
        User user = new User();
        Habit habit = new Habit();
        habit.setId(1L);
        HabitAssign habitAssign = getHabitAssign();

        when(habitAssignRepo.findAllByUserId(1L)).thenReturn(new ArrayList<>());
        when(modelMapper.map(userVo, UserVO.class)).thenReturn(userVo2);
        when(modelMapper.map(userVo2, User.class)).thenReturn(user);
        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(habitAssignRepo.findByHabitIdAndUserIdAndStatusIsCancelled(1L, user.getId())).thenReturn(habitAssign);
        when(modelMapper.map(habitAssign, HabitAssignManagementDto.class)).thenReturn(new HabitAssignManagementDto());

        habitAssignService.addDefaultHabit(userVo, "eu");

        verify(habitRepo).findById(1L);
    }

    @Test
    void addDefaultHabitWithNotEmptyUsersHabitAssign() {
        UserVO userVo = ModelUtils.createUserVO2();

        when(habitAssignRepo.findAllByUserId(1L)).thenReturn(habitAssigns);

        habitAssignService.addDefaultHabit(userVo, "eu");

        verify(modelMapper, times(0)).map(any(), any());
        verify(modelMapper, times(0)).map(any(), any());
        verify(modelMapper, times(0)).map(any(), any());
        verify(habitRepo, times(0)).findById(anyLong());
        verify(habitAssignRepo, times(0)).findByHabitIdAndUserIdAndStatusIsCancelled(anyLong(), anyLong());
        verify(modelMapper, times(0)).map(any(), any());
    }

    @Test
    void updateUserHabitInfoDurationTest() {
        HabitAssign habitAssign = getHabitAssign();
        habitAssign.setDuration(20);
        when(habitAssignRepo.existsById(anyLong())).thenReturn(true);
        when(habitAssignRepo.findByHabitAssignIdUserIdAndStatusIsInProgress(anyLong(), anyLong()))
            .thenReturn(Optional.of(habitAssign));
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
        HabitAssign habitAssign = getHabitAssign();
        habitAssign.setDuration(20);
        habitAssign.setWorkingDays(20);
        when(habitAssignRepo.existsById(anyLong())).thenReturn(true);
        when(habitAssignRepo.findByHabitAssignIdUserIdAndStatusIsInProgress(anyLong(), anyLong()))
            .thenReturn(Optional.of(habitAssign));
        var exception = assertThrows(BadRequestException.class,
            () -> habitAssignService.updateUserHabitInfoDuration(1L, 21L, 19));
        assertEquals(ErrorMessage.INVALID_DURATION, exception.getMessage());
        verify(habitAssignRepo).existsById(anyLong());
        verify(habitAssignRepo).findByHabitAssignIdUserIdAndStatusIsInProgress(anyLong(), anyLong());
    }

    @Test
    void updateUserShoppingListItem() {
        UserShoppingListItem userShoppingListItem = getUserShoppingListItem();
        when(userShoppingListItemRepo.saveAll(any())).thenReturn(List.of(userShoppingListItem));
        when(habitAssignRepo.findById(1L)).thenReturn(Optional.of(getHabitAssign()));
        when(shoppingListItemRepo.findById(1L)).thenReturn(Optional.of(getShoppingListItem()));
        habitAssignService.updateUserShoppingListItem(getUpdateUserShoppingListDto());
        verify(userShoppingListItemRepo, times(1)).saveAll(any());
    }

    @Test
    void updateUserShoppingListItemThrowException() {
        when(habitAssignRepo.findById(1L)).thenReturn(Optional.of(getHabitAssign()));
        UpdateUserShoppingListDto updateUserShoppingListDto = getUpdateUserShoppingListDto();
        assertThrows(ShoppingListItemNotFoundException.class,
            () -> habitAssignService.updateUserShoppingListItem(updateUserShoppingListDto));
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
        Habit habit = ModelUtils.getHabit(habitId, "image123");
        HabitAssign habitAssign = ModelUtils.getHabitAssign(habitAssignId, habit, HabitAssignStatus.INPROGRESS);
        habitAssign.getUser().setId(userId);
        HabitAssignDto habitAssignDto =
            ModelUtils.getHabitAssignDto(habitAssignId, habitAssign.getStatus(), habit.getImage());
        HabitTranslation habitTranslation = habitAssign.getHabit().getHabitTranslations().stream().findFirst().get();

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(shoppingListItemTranslationRepo.findShoppingListByHabitIdAndByLanguageCode(language, habitId))
            .thenReturn(new ArrayList<>());
        when(modelMapper.map(habitAssign, HabitAssignDto.class)).thenReturn(habitAssignDto);
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(habitAssignDto.getHabit());
        when(userShoppingListItemRepo.getAllAssignedShoppingListItemsFull(habitAssignId)).thenReturn(new ArrayList<>());

        var dto = habitAssignService.findHabitByUserIdAndHabitAssignId(userId, habitAssignId, language);

        assertNotNull(dto);
        assertEquals(habit.getId(), dto.getId());
        assertEquals(habit.getImage(), dto.getImage());
        assertEquals(habitAssign.getStatus(), dto.getHabitAssignStatus());
        verify(habitAssignRepo).findById(anyLong());
        verify(shoppingListItemTranslationRepo).findShoppingListByHabitIdAndByLanguageCode(anyString(), anyLong());
        verify(userShoppingListItemRepo).getAllAssignedShoppingListItemsFull(anyLong());
    }

    @Test
    void findHabitByUserIdAndHabitIdTest() {
        Long habitId = 1L;
        Long userId = 2L;
        Long habitAssignId = 3L;
        Long amountOfUsersAcquired = 4L;
        HabitAssign habitAssign = getFullHabitAssign();
        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId);

        HabitAssignDto habitAssignDto = getFullHabitAssignDto();
        habitAssignDto.setId(habitAssignId);
        habitAssignDto.setUserId(userId);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(habitAssign, HabitAssignDto.class)).thenReturn(habitAssignDto);
        when(modelMapper.map(any(HabitTranslation.class), eq(HabitDto.class))).thenReturn(getHabitDto());
        when(shoppingListItemTranslationRepo.findShoppingListByHabitIdAndByLanguageCode(language, habitId))
            .thenReturn(getShoppingListItemTranslationList());
        when(habitAssignRepo.findAmountOfUsersAcquired(habitId)).thenReturn(amountOfUsersAcquired);

        HabitDto actual = habitAssignService.findHabitByUserIdAndHabitAssignId(userId, habitAssignId, language);
        assertNotNull(actual.getAmountAcquiredUsers());

        verify(habitAssignRepo, times(1)).findById(habitAssignId);
        verify(shoppingListItemTranslationRepo, times(1)).findShoppingListByHabitIdAndByLanguageCode(language, habitId);
        verify(habitAssignRepo, times(1)).findAmountOfUsersAcquired(habitId);
    }

    @Test
    void getReadinessPercent() {
        habitAssignDto.setWorkingDays(30);
        habitAssignDto.setDuration(2);

        assertEquals(1500, habitAssignService.getReadinessPercent(habitAssignDto));
    }

    @Test
    void updateShoppingItem() {
        UserShoppingListItem userShoppingListItem = getUserShoppingListItem();
        userShoppingListItem.setStatus(ShoppingListItemStatus.ACTIVE);
        when(userShoppingListItemRepo.getAllAssignedShoppingListItemsFull(any()))
            .thenReturn(List.of(userShoppingListItem));

        habitAssignService.updateShoppingItem(1L, 1L);
        assertEquals(ShoppingListItemStatus.INPROGRESS, userShoppingListItem.getStatus());

        habitAssignService.updateShoppingItem(1L, 1L);
        assertEquals(ShoppingListItemStatus.ACTIVE, userShoppingListItem.getStatus());
    }

    @Test
    void updateShoppingItemWithNotPresentShoppingItem() {
        ShoppingListItemStatus oldStatus = ShoppingListItemStatus.ACTIVE;
        UserShoppingListItem userShoppingListItem = getUserShoppingListItem();
        userShoppingListItem.setStatus(oldStatus);
        when(userShoppingListItemRepo.getAllAssignedShoppingListItemsFull(any()))
            .thenReturn(List.of(userShoppingListItem));

        habitAssignService.updateShoppingItem(1L, 2L);
        verify(userShoppingListItemRepo, times(0)).save(any());
        assertEquals(oldStatus, userShoppingListItem.getStatus());
    }

    @Test
    void updateShoppingItemWithNotActiveAndNotInprogressStatus() {
        UserShoppingListItem userShoppingListItem = getUserShoppingListItem();
        when(userShoppingListItemRepo.getAllAssignedShoppingListItemsFull(any()))
            .thenReturn(List.of(userShoppingListItem));

        userShoppingListItem.setStatus(ShoppingListItemStatus.DONE);
        habitAssignService.updateShoppingItem(1L, 1L);
        assertEquals(ShoppingListItemStatus.DONE, userShoppingListItem.getStatus());

        userShoppingListItem.setStatus(ShoppingListItemStatus.DISABLED);
        habitAssignService.updateShoppingItem(1L, 1L);
        assertEquals(ShoppingListItemStatus.DISABLED, userShoppingListItem.getStatus());
    }

    @Test
    void fullUpdateUserAndCustomShoppingListsWithNonItem() {
        Long userId = 1L;
        Long habitAssignId = 1L;

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of())
            .customShoppingListItemDto(List.of())
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(customShoppingListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of());

        habitAssignService.fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userShoppingListItemRepo).saveAll(List.of());
        verify(userShoppingListItemRepo).deleteAll(List.of());

        verify(shoppingListItemRepo, times(0)).findByNames(getFullHabitAssign().getHabit().getId(), List.of(),
            language);
        verify(shoppingListItemService).saveUserShoppingListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customShoppingListItemRepo).findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customShoppingListItemRepo).saveAll(List.of());
        verify(customShoppingListItemRepo).deleteAll(List.of());

        BulkSaveCustomShoppingListItemDto bulkSaveCustomShoppingListItemDto =
            new BulkSaveCustomShoppingListItemDto(List.of());

        verify(customShoppingListItemService).save(bulkSaveCustomShoppingListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void saveUserShoppingListWithStatuses() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        String name = "Buy a bamboo toothbrush";
        UserShoppingListItemResponseDto responseDto = ModelUtils.getUserShoppingListItemResponseDto();
        responseDto.setId(null);
        responseDto.setText(name);

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of(responseDto))
            .customShoppingListItemDto(List.of())
            .build();

        List<String> listOfName = List.of(name);
        ShoppingListItem shoppingListItem = ModelUtils.getShoppingListItem();
        ShoppingListItemWithStatusRequestDto requestDto = ModelUtils.getShoppingListItemWithStatusRequestDto();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(shoppingListItemRepo.findByNames(getFullHabitAssign().getHabit().getId(), listOfName, language))
            .thenReturn(List.of(shoppingListItem));

        when(customShoppingListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of());

        habitAssignService.fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userShoppingListItemRepo).saveAll(List.of());
        verify(userShoppingListItemRepo).deleteAll(List.of());

        verify(shoppingListItemRepo).findByNames(getFullHabitAssign().getHabit().getId(), listOfName, language);
        verify(shoppingListItemService).saveUserShoppingListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(requestDto), language);

        verify(customShoppingListItemRepo).findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customShoppingListItemRepo).saveAll(List.of());
        verify(customShoppingListItemRepo).deleteAll(List.of());

        BulkSaveCustomShoppingListItemDto bulkSaveCustomShoppingListItemDto =
            new BulkSaveCustomShoppingListItemDto(List.of());

        verify(customShoppingListItemService).save(bulkSaveCustomShoppingListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void saveUserShoppingListWithStatusesWithNonExistentItemThrowsNotFoundException() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        String name = "Buy a bamboo toothbrush";
        UserShoppingListItemResponseDto responseDto = ModelUtils.getUserShoppingListItemResponseDto();
        responseDto.setId(null);
        responseDto.setText(name);

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of(responseDto))
            .customShoppingListItemDto(List.of())
            .build();

        List<String> listOfName = List.of(name);

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(shoppingListItemRepo.findByNames(getFullHabitAssign().getHabit().getId(), listOfName, language))
            .thenReturn(List.of());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> habitAssignService
                .fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language));

        assertEquals(ErrorMessage.SHOPPING_LIST_ITEM_NOT_FOUND_BY_NAMES + name, exception.getMessage());

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo).findById(habitAssignId);

        verify(userShoppingListItemRepo).saveAll(List.of());
        verify(userShoppingListItemRepo).deleteAll(List.of());

        verify(shoppingListItemRepo).findByNames(getFullHabitAssign().getHabit().getId(), listOfName, language);
        verify(shoppingListItemService, times(0))
            .saveUserShoppingListItems(anyLong(), anyLong(), anyList(), anyString());

        verify(customShoppingListItemRepo, times(0)).findAllByUserIdAndHabitId(userId,
            getFullHabitAssign().getHabit().getId());
        verify(customShoppingListItemRepo, times(0)).saveAll(anyList());
        verify(customShoppingListItemRepo, times(0)).deleteAll(anyList());

        verify(customShoppingListItemService, times(0)).save(any(), anyLong(), anyLong());
    }

    @Test
    void saveUserShoppingListWithStatusesWithDuplicateThrowsBadRequestException() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        UserShoppingListItemResponseDto responseDto = ModelUtils.getUserShoppingListItemResponseDto();
        responseDto.setId(null);
        UserShoppingListItemResponseDto sameResponse = ModelUtils.getUserShoppingListItemResponseDto();
        sameResponse.setId(null);

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of(responseDto, sameResponse))
            .customShoppingListItemDto(List.of())
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> habitAssignService
            .fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language));

        assertEquals(ErrorMessage.DUPLICATED_USER_SHOPPING_LIST_ITEM, exception.getMessage());

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);

        verify(userShoppingListItemRepo).saveAll(List.of());
        verify(userShoppingListItemRepo).deleteAll(List.of());

        verify(shoppingListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(shoppingListItemService, times(0))
            .saveUserShoppingListItems(anyLong(), anyLong(), anyList(), anyString());

        verify(customShoppingListItemRepo, times(0))
            .findAllByUserIdAndHabitId(anyLong(), anyLong());
        verify(customShoppingListItemRepo, times(0)).saveAll(anyList());
        verify(customShoppingListItemRepo, times(0)).deleteAll(anyList());

        verify(customShoppingListItemService, times(0)).save(any(), anyLong(), anyLong());
    }

    @Test
    void updateAndDeleteUserShoppingListWithStatusesUpdateItem() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        ShoppingListItemStatus newStatus = ShoppingListItemStatus.DONE;
        UserShoppingListItemResponseDto responseDto = ModelUtils.getUserShoppingListItemResponseDto();
        responseDto.setStatus(newStatus);

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of(responseDto))
            .customShoppingListItemDto(List.of())
            .build();

        HabitAssign habitAssign = ModelUtils.getHabitAssign();

        UserShoppingListItem userShoppingListItem = ModelUtils.getUserShoppingListItem();
        userShoppingListItem.setStatus(ShoppingListItemStatus.ACTIVE);
        userShoppingListItem.setHabitAssign(null);

        habitAssign.setUserShoppingListItems(List.of(userShoppingListItem));

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(habitAssign));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));

        when(customShoppingListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of());

        habitAssignService
            .fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        UserShoppingListItem userShoppingListItemToSave = ModelUtils.getUserShoppingListItem();
        userShoppingListItemToSave.setStatus(ShoppingListItemStatus.DONE);
        userShoppingListItemToSave.setHabitAssign(null);

        verify(userShoppingListItemRepo).saveAll(List.of(userShoppingListItemToSave));
        verify(userShoppingListItemRepo).deleteAll(List.of());

        verify(shoppingListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(shoppingListItemService).saveUserShoppingListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customShoppingListItemRepo).findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customShoppingListItemRepo).saveAll(List.of());
        verify(customShoppingListItemRepo).deleteAll(List.of());

        BulkSaveCustomShoppingListItemDto bulkSaveCustomShoppingListItemDto =
            new BulkSaveCustomShoppingListItemDto(List.of());

        verify(customShoppingListItemService).save(bulkSaveCustomShoppingListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteUserShoppingListWithStatusesUpdateItemWithDisabledStatus() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        ShoppingListItemStatus newStatus = ShoppingListItemStatus.DONE;
        UserShoppingListItemResponseDto responseDto = ModelUtils.getUserShoppingListItemResponseDto();
        responseDto.setStatus(newStatus);

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of(responseDto))
            .customShoppingListItemDto(List.of())
            .build();

        HabitAssign habitAssign = ModelUtils.getHabitAssign();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(habitAssign));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));

        UserShoppingListItem userShoppingListItem = ModelUtils.getUserShoppingListItem();
        userShoppingListItem.setStatus(ShoppingListItemStatus.DISABLED);
        userShoppingListItem.setHabitAssign(null);

        habitAssign.setUserShoppingListItems(List.of(userShoppingListItem));

        habitAssignService
            .fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        UserShoppingListItem userShoppingListItemToSave = ModelUtils.getUserShoppingListItem();
        userShoppingListItemToSave.setStatus(ShoppingListItemStatus.DONE);
        userShoppingListItemToSave.setHabitAssign(null);

        verify(userShoppingListItemRepo).saveAll(List.of(userShoppingListItemToSave));
        verify(userShoppingListItemRepo).deleteAll(List.of());

        verify(shoppingListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(shoppingListItemService).saveUserShoppingListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customShoppingListItemRepo).findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customShoppingListItemRepo).saveAll(List.of());
        verify(customShoppingListItemRepo).deleteAll(List.of());

        BulkSaveCustomShoppingListItemDto bulkSaveCustomShoppingListItemDto =
            new BulkSaveCustomShoppingListItemDto(List.of());

        verify(customShoppingListItemService).save(bulkSaveCustomShoppingListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteUserShoppingListWithStatusesWithNonExistentItemThrowsNotFoundException() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        UserShoppingListItemResponseDto responseDto = ModelUtils.getUserShoppingListItemResponseDto();

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of(responseDto))
            .customShoppingListItemDto(List.of())
            .build();

        HabitAssign habitAssign = ModelUtils.getHabitAssign();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(habitAssign));

        UserShoppingListItem userShoppingListItem = ModelUtils.getUserShoppingListItem();
        userShoppingListItem.setId(responseDto.getId() + 1);
        habitAssign.setUserShoppingListItems(List.of(userShoppingListItem));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> habitAssignService
            .fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language));

        assertEquals(ErrorMessage.USER_SHOPPING_LIST_ITEM_NOT_FOUND + responseDto.getId(), exception.getMessage());

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);

        verify(userShoppingListItemRepo, times(0)).saveAll(anyList());
        verify(userShoppingListItemRepo, times(0)).deleteAll(anyList());

        verify(shoppingListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(shoppingListItemService, times(0))
            .saveUserShoppingListItems(anyLong(), anyLong(), anyList(), anyString());

        verify(customShoppingListItemRepo, times(0))
            .findAllByUserIdAndHabitId(anyLong(), anyLong());
        verify(customShoppingListItemRepo, times(0)).saveAll(anyList());
        verify(customShoppingListItemRepo, times(0)).deleteAll(anyList());

        verify(customShoppingListItemService, times(0)).save(any(), anyLong(), anyLong());
    }

    @Test
    void updateAndDeleteUserShoppingListWithStatusesDeleteItem() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        HabitAssign habitAssign = ModelUtils.getHabitAssign();

        UserShoppingListItem userShoppingListItem = ModelUtils.getUserShoppingListItem();

        habitAssign.setUserShoppingListItems(List.of(userShoppingListItem));

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of())
            .customShoppingListItemDto(List.of())
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(habitAssign));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));

        when(customShoppingListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of());

        habitAssignService
            .fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userShoppingListItemRepo).saveAll(List.of());
        verify(userShoppingListItemRepo).deleteAll(List.of(userShoppingListItem));

        verify(shoppingListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(shoppingListItemService).saveUserShoppingListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customShoppingListItemRepo).findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customShoppingListItemRepo).saveAll(List.of());
        verify(customShoppingListItemRepo).deleteAll(List.of());

        BulkSaveCustomShoppingListItemDto bulkSaveCustomShoppingListItemDto =
            new BulkSaveCustomShoppingListItemDto(List.of());

        verify(customShoppingListItemService).save(bulkSaveCustomShoppingListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteUserShoppingListWithStatusesDeleteItemWithDisabledStatus() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        HabitAssign habitAssign = ModelUtils.getHabitAssign();

        UserShoppingListItem userShoppingListItem = ModelUtils.getUserShoppingListItem();
        userShoppingListItem.setStatus(ShoppingListItemStatus.DISABLED);

        habitAssign.setUserShoppingListItems(List.of(userShoppingListItem));

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of())
            .customShoppingListItemDto(List.of())
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(habitAssign));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));

        when(customShoppingListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of());

        habitAssignService
            .fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userShoppingListItemRepo).saveAll(List.of());
        verify(userShoppingListItemRepo).deleteAll(List.of());

        verify(shoppingListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(shoppingListItemService).saveUserShoppingListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customShoppingListItemRepo).findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customShoppingListItemRepo).saveAll(List.of());
        verify(customShoppingListItemRepo).deleteAll(List.of());

        BulkSaveCustomShoppingListItemDto bulkSaveCustomShoppingListItemDto =
            new BulkSaveCustomShoppingListItemDto(List.of());

        verify(customShoppingListItemService).save(bulkSaveCustomShoppingListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteUserShoppingListWithStatusesUpdateAndDeleteItems() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        ShoppingListItemStatus oldStatus = ShoppingListItemStatus.ACTIVE;
        ShoppingListItemStatus newStatus = ShoppingListItemStatus.DONE;
        UserShoppingListItemResponseDto responseDto = ModelUtils.getUserShoppingListItemResponseDto();
        responseDto.setStatus(newStatus);

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of(responseDto))
            .customShoppingListItemDto(List.of())
            .build();

        HabitAssign habitAssign = ModelUtils.getHabitAssign();

        UserShoppingListItem firstUserShoppingListItem = ModelUtils.getUserShoppingListItem();
        firstUserShoppingListItem.setStatus(oldStatus);
        firstUserShoppingListItem.setHabitAssign(null);

        UserShoppingListItem secondUserShoppingListItem = ModelUtils.getUserShoppingListItem();
        secondUserShoppingListItem.setId(firstUserShoppingListItem.getId() + 1);

        habitAssign.setUserShoppingListItems(List.of(firstUserShoppingListItem, secondUserShoppingListItem));

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(habitAssign));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));

        when(customShoppingListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of());

        habitAssignService
            .fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        UserShoppingListItem userShoppingListItemToSave = ModelUtils.getUserShoppingListItem();
        userShoppingListItemToSave.setStatus(newStatus);
        userShoppingListItemToSave.setHabitAssign(null);

        verify(userShoppingListItemRepo).saveAll(List.of(userShoppingListItemToSave));
        verify(userShoppingListItemRepo).deleteAll(List.of(secondUserShoppingListItem));

        verify(shoppingListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(shoppingListItemService).saveUserShoppingListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customShoppingListItemRepo).findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customShoppingListItemRepo).saveAll(List.of());
        verify(customShoppingListItemRepo).deleteAll(List.of());

        BulkSaveCustomShoppingListItemDto bulkSaveCustomShoppingListItemDto =
            new BulkSaveCustomShoppingListItemDto(List.of());

        verify(customShoppingListItemService).save(bulkSaveCustomShoppingListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteUserShoppingListWithStatusesWithDuplicateThrowsBadRequestException() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        UserShoppingListItemResponseDto responseDto = ModelUtils.getUserShoppingListItemResponseDto();
        UserShoppingListItemResponseDto sameResponseDto = ModelUtils.getUserShoppingListItemResponseDto();

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of(responseDto, sameResponseDto))
            .customShoppingListItemDto(List.of())
            .build();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> habitAssignService
            .fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language));

        assertEquals(ErrorMessage.DUPLICATED_USER_SHOPPING_LIST_ITEM, exception.getMessage());

        verify(habitAssignRepo, times(0)).findByHabitIdAndUserId(anyLong(), anyLong());
        verify(userShoppingListItemRepo, times(0)).saveAll(anyList());
        verify(userShoppingListItemRepo, times(0)).deleteAll(anyList());

        verify(shoppingListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(shoppingListItemService, times(0))
            .saveUserShoppingListItems(anyLong(), anyLong(), anyList(), anyString());

        verify(customShoppingListItemRepo, times(0))
            .findAllByUserIdAndHabitId(anyLong(), anyLong());
        verify(customShoppingListItemRepo, times(0)).saveAll(anyList());
        verify(customShoppingListItemRepo, times(0)).deleteAll(anyList());

        verify(customShoppingListItemService, times(0)).save(any(), anyLong(), anyLong());
    }

    @Test
    void updateAndDeleteUserShoppingListWithStatusesWithNotFoundHabitAssignThrowsNotFoundException() {
        Long userId = 1L;
        Long habitAssignId = 1L;

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of())
            .customShoppingListItemDto(List.of())
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> habitAssignService
            .fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language));

        assertEquals(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_WITH_CURRENT_USER_ID_AND_HABIT_ASSIGN_ID + habitAssignId,
            exception.getMessage());

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);

        verify(userShoppingListItemRepo, times(0)).saveAll(anyList());
        verify(userShoppingListItemRepo, times(0)).deleteAll(anyList());

        verify(shoppingListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(shoppingListItemService, times(0))
            .saveUserShoppingListItems(anyLong(), anyLong(), anyList(), anyString());

        verify(customShoppingListItemRepo, times(0))
            .findAllByUserIdAndHabitId(anyLong(), anyLong());
        verify(customShoppingListItemRepo, times(0)).saveAll(anyList());
        verify(customShoppingListItemRepo, times(0)).deleteAll(anyList());

        verify(customShoppingListItemService, times(0)).save(any(), anyLong(), anyLong());
    }

    @Test
    void saveCustomShoppingListWithStatuses() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        CustomShoppingListItemResponseDto responseDto = ModelUtils.getCustomShoppingListItemResponseDto();
        responseDto.setId(null);

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of())
            .customShoppingListItemDto(List.of(responseDto))
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(customShoppingListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of());

        habitAssignService.fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userShoppingListItemRepo).saveAll(List.of());
        verify(userShoppingListItemRepo).deleteAll(List.of());

        verify(shoppingListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(shoppingListItemService).saveUserShoppingListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customShoppingListItemRepo).findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customShoppingListItemRepo).saveAll(List.of());
        verify(customShoppingListItemRepo).deleteAll(List.of());

        CustomShoppingListItemSaveRequestDto requestDto =
            ModelUtils.getCustomShoppingListItemWithStatusSaveRequestDto();
        BulkSaveCustomShoppingListItemDto bulkSaveUserShoppingListItemDto =
            new BulkSaveCustomShoppingListItemDto(List.of(requestDto));

        verify(customShoppingListItemService).save(bulkSaveUserShoppingListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void saveCustomShoppingListWithStatusesWithDuplicateThrowsBadRequestException() {
        Long userId = 1L;
        Long habitAssignId = 1L;

        CustomShoppingListItemResponseDto responseDto = ModelUtils.getCustomShoppingListItemResponseDto();
        responseDto.setId(null);
        CustomShoppingListItemResponseDto sameResponseDto = ModelUtils.getCustomShoppingListItemResponseDto();
        sameResponseDto.setId(null);

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of())
            .customShoppingListItemDto(List.of(responseDto, sameResponseDto))
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> habitAssignService.fullUpdateUserAndCustomShoppingLists(userId, habitAssignId,
                dto, language));

        assertEquals(ErrorMessage.DUPLICATED_CUSTOM_SHOPPING_LIST_ITEM, exception.getMessage());

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(2)).findById(habitAssignId);

        verify(userShoppingListItemRepo).saveAll(List.of());
        verify(userShoppingListItemRepo).deleteAll(List.of());

        verify(shoppingListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(shoppingListItemService).saveUserShoppingListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customShoppingListItemRepo).findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customShoppingListItemRepo).saveAll(List.of());
        verify(customShoppingListItemRepo).deleteAll(List.of());

        verify(customShoppingListItemService, times(0)).save(any(), anyLong(), anyLong());
    }

    @Test
    void updateAndDeleteCustomShoppingListWithStatusesUpdateItem() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        ShoppingListItemStatus newStatus = ShoppingListItemStatus.DONE;
        CustomShoppingListItemResponseDto responseDto = ModelUtils.getCustomShoppingListItemResponseDto();
        responseDto.setStatus(newStatus);

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of())
            .customShoppingListItemDto(List.of(responseDto))
            .build();

        CustomShoppingListItem customShoppingListItem = ModelUtils.getCustomShoppingListItem();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(customShoppingListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of(customShoppingListItem));

        habitAssignService
            .fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userShoppingListItemRepo).saveAll(List.of());
        verify(userShoppingListItemRepo).deleteAll(List.of());

        verify(shoppingListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(shoppingListItemService).saveUserShoppingListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customShoppingListItemRepo)
            .findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());

        CustomShoppingListItem customShoppingListItemToSave = ModelUtils.getCustomShoppingListItem();
        customShoppingListItemToSave.setStatus(newStatus);

        verify(customShoppingListItemRepo).saveAll(List.of(customShoppingListItemToSave));
        verify(customShoppingListItemRepo).deleteAll(List.of());

        BulkSaveCustomShoppingListItemDto bulkSaveUserShoppingListItemDto =
            new BulkSaveCustomShoppingListItemDto(List.of());

        verify(customShoppingListItemService).save(bulkSaveUserShoppingListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteCustomShoppingListWithStatusesWithNonExistentItemThrowsNotFoundException() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        CustomShoppingListItemResponseDto responseDto = ModelUtils.getCustomShoppingListItemResponseDto();

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of())
            .customShoppingListItemDto(List.of(responseDto))
            .build();

        CustomShoppingListItem customShoppingListItem = ModelUtils.getCustomShoppingListItem();
        customShoppingListItem.setId(responseDto.getId() + 1);

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(customShoppingListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of(customShoppingListItem));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> habitAssignService
            .fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language));

        assertEquals(ErrorMessage.CUSTOM_SHOPPING_LIST_ITEM_WITH_THIS_ID_NOT_FOUND + responseDto.getId(),
            exception.getMessage());

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(2)).findById(habitAssignId);

        verify(userShoppingListItemRepo).saveAll(List.of());
        verify(userShoppingListItemRepo).deleteAll(List.of());

        verify(shoppingListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(shoppingListItemService).saveUserShoppingListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customShoppingListItemRepo)
            .findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customShoppingListItemRepo, times(0)).saveAll(anyList());
        verify(customShoppingListItemRepo, times(0)).deleteAll(anyList());

        verify(customShoppingListItemService, times(0)).save(any(), anyLong(), anyLong());
    }

    @Test
    void updateAndDeleteCustomShoppingListWithStatusesUpdateItemWithDisabledStatus() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        ShoppingListItemStatus newStatus = ShoppingListItemStatus.DONE;
        CustomShoppingListItemResponseDto responseDto = ModelUtils.getCustomShoppingListItemResponseDto();
        responseDto.setStatus(newStatus);

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of())
            .customShoppingListItemDto(List.of(responseDto))
            .build();

        CustomShoppingListItem customShoppingListItem = ModelUtils.getCustomShoppingListItem();
        customShoppingListItem.setStatus(ShoppingListItemStatus.DISABLED);

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(customShoppingListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of(customShoppingListItem));

        habitAssignService
            .fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userShoppingListItemRepo).saveAll(List.of());
        verify(userShoppingListItemRepo).deleteAll(List.of());

        verify(shoppingListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(shoppingListItemService).saveUserShoppingListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customShoppingListItemRepo)
            .findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());

        CustomShoppingListItem customShoppingListItemToSave = ModelUtils.getCustomShoppingListItem();
        customShoppingListItemToSave.setStatus(newStatus);

        verify(customShoppingListItemRepo).saveAll(List.of(customShoppingListItemToSave));
        verify(customShoppingListItemRepo).deleteAll(List.of());

        BulkSaveCustomShoppingListItemDto bulkSaveUserShoppingListItemDto =
            new BulkSaveCustomShoppingListItemDto(List.of());

        verify(customShoppingListItemService).save(bulkSaveUserShoppingListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteCustomShoppingListWithStatusesDeleteItem() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        CustomShoppingListItem customShoppingListItem = ModelUtils.getCustomShoppingListItem();

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of())
            .customShoppingListItemDto(List.of())
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(customShoppingListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of(customShoppingListItem));

        habitAssignService
            .fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userShoppingListItemRepo).saveAll(List.of());
        verify(userShoppingListItemRepo).deleteAll(List.of());

        verify(shoppingListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(shoppingListItemService).saveUserShoppingListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customShoppingListItemRepo)
            .findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customShoppingListItemRepo).saveAll(List.of());
        verify(customShoppingListItemRepo).deleteAll(List.of(customShoppingListItem));

        BulkSaveCustomShoppingListItemDto bulkSaveUserShoppingListItemDto =
            new BulkSaveCustomShoppingListItemDto(List.of());

        verify(customShoppingListItemService).save(bulkSaveUserShoppingListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteCustomShoppingListWithStatusesDeleteItemWithDisabledStatus() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        CustomShoppingListItem customShoppingListItem = ModelUtils.getCustomShoppingListItem();
        customShoppingListItem.setStatus(ShoppingListItemStatus.DISABLED);

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of())
            .customShoppingListItemDto(List.of())
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(customShoppingListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of(customShoppingListItem));

        habitAssignService
            .fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userShoppingListItemRepo).saveAll(List.of());
        verify(userShoppingListItemRepo).deleteAll(List.of());

        verify(shoppingListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(shoppingListItemService).saveUserShoppingListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customShoppingListItemRepo)
            .findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());
        verify(customShoppingListItemRepo).saveAll(List.of());
        verify(customShoppingListItemRepo).deleteAll(List.of());

        BulkSaveCustomShoppingListItemDto bulkSaveUserShoppingListItemDto =
            new BulkSaveCustomShoppingListItemDto(List.of());

        verify(customShoppingListItemService).save(bulkSaveUserShoppingListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteCustomShoppingListWithStatusesUpdateAndDeleteItems() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        ShoppingListItemStatus newStatus = ShoppingListItemStatus.DONE;
        CustomShoppingListItemResponseDto responseDto = ModelUtils.getCustomShoppingListItemResponseDto();
        responseDto.setStatus(newStatus);

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of())
            .customShoppingListItemDto(List.of(responseDto))
            .build();

        CustomShoppingListItem firstCustomShoppingListItem = ModelUtils.getCustomShoppingListItem();
        firstCustomShoppingListItem.setStatus(newStatus);

        CustomShoppingListItem secondCustomShoppingListItem = ModelUtils.getCustomShoppingListItem();
        secondCustomShoppingListItem.setId(responseDto.getId() + 1);

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(customShoppingListItemRepo.findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId()))
            .thenReturn(List.of(firstCustomShoppingListItem, secondCustomShoppingListItem));

        habitAssignService
            .fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language);

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo, times(3)).findById(habitAssignId);

        verify(userShoppingListItemRepo).saveAll(List.of());
        verify(userShoppingListItemRepo).deleteAll(List.of());

        verify(shoppingListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(shoppingListItemService).saveUserShoppingListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customShoppingListItemRepo)
            .findAllByUserIdAndHabitId(userId, getFullHabitAssign().getHabit().getId());

        CustomShoppingListItem customShoppingListItemToSave = ModelUtils.getCustomShoppingListItem();
        customShoppingListItemToSave.setStatus(newStatus);

        verify(customShoppingListItemRepo).saveAll(List.of(customShoppingListItemToSave));
        verify(customShoppingListItemRepo).deleteAll(List.of(secondCustomShoppingListItem));

        BulkSaveCustomShoppingListItemDto bulkSaveUserShoppingListItemDto =
            new BulkSaveCustomShoppingListItemDto(List.of());

        verify(customShoppingListItemService).save(bulkSaveUserShoppingListItemDto, userId,
            getFullHabitAssign().getHabit().getId());
    }

    @Test
    void updateAndDeleteCustomShoppingListWithStatusesWithDuplicateThrowsBadRequestException() {
        Long userId = 1L;
        Long habitAssignId = 1L;
        CustomShoppingListItemResponseDto responseDto = ModelUtils.getCustomShoppingListItemResponseDto();
        CustomShoppingListItemResponseDto sameResponseDto = ModelUtils.getCustomShoppingListItemResponseDto();

        UserShoppingAndCustomShoppingListsDto dto = UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of())
            .customShoppingListItemDto(List.of(responseDto, sameResponseDto))
            .build();

        when(habitAssignRepo.findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(ModelUtils.getHabitAssign()));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> habitAssignService
            .fullUpdateUserAndCustomShoppingLists(userId, habitAssignId, dto, language));
        assertEquals(ErrorMessage.DUPLICATED_CUSTOM_SHOPPING_LIST_ITEM, exception.getMessage());

        verify(habitAssignRepo).findByHabitAssignIdUserIdNotCancelledAndNotExpiredStatus(habitAssignId, userId);
        verify(habitAssignRepo).findById(habitAssignId);

        verify(userShoppingListItemRepo).saveAll(List.of());
        verify(userShoppingListItemRepo).deleteAll(List.of());

        verify(shoppingListItemRepo, times(0)).findByNames(anyLong(), anyList(), anyString());
        verify(shoppingListItemService).saveUserShoppingListItems(userId, getFullHabitAssign().getHabit().getId(),
            List.of(), language);

        verify(customShoppingListItemRepo, times(0))
            .findAllByUserIdAndHabitId(anyLong(), anyLong());
        verify(customShoppingListItemRepo, times(0)).saveAll(anyList());
        verify(customShoppingListItemRepo, times(0)).deleteAll(anyList());

        verify(customShoppingListItemService, times(0)).save(any(), anyLong(), anyLong());
    }
}
