package greencity.service;

import greencity.ModelUtils;
import greencity.TestConst;
import greencity.achievement.AchievementCalculation;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableDto;
import greencity.dto.friends.UserFriendHabitInviteDto;
import greencity.dto.habit.CustomHabitDtoRequest;
import greencity.dto.habit.CustomHabitDtoResponse;
import greencity.dto.habit.HabitDto;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.dto.user.UserProfilePictureDto;
import greencity.dto.user.UserVO;
import greencity.entity.CustomToDoListItem;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.HabitTranslation;
import greencity.entity.Language;
import greencity.entity.RatingPoints;
import greencity.entity.Tag;
import greencity.entity.User;
import greencity.enums.HabitAssignStatus;
import greencity.enums.Role;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoFriendWithIdException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.exception.exceptions.WrongEmailException;
import greencity.mapping.CustomHabitMapper;
import greencity.mapping.CustomToDoListMapper;
import greencity.mapping.CustomToDoListResponseDtoMapper;
import greencity.mapping.HabitTranslationDtoMapper;
import greencity.mapping.HabitTranslationMapper;
import greencity.rating.RatingCalculation;
import greencity.repository.CustomToDoListItemRepo;
import greencity.repository.HabitAssignRepo;
import greencity.repository.HabitInvitationRepo;
import greencity.repository.HabitRepo;
import greencity.repository.HabitTranslationRepo;
import greencity.repository.LanguageRepo;
import greencity.repository.RatingPointsRepo;
import greencity.repository.TagsRepo;
import greencity.repository.ToDoListItemTranslationRepo;
import greencity.repository.UserRepo;
import greencity.repository.options.HabitTranslationFilter;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import static greencity.ModelUtils.getCustomToDoListItemRequestDto;
import static greencity.ModelUtils.getHabit;
import static greencity.ModelUtils.getHabitAssign;
import static greencity.ModelUtils.getHabitDto;
import static greencity.ModelUtils.getHabitTranslation;
import static greencity.ModelUtils.getHabitTranslationDto;
import static greencity.ModelUtils.getHabitTranslationUa;
import static greencity.ModelUtils.getUser;
import static greencity.ModelUtils.getUserFriendInviteHabitDtoTuple1;
import static greencity.ModelUtils.getUserFriendInviteHabitDtoTuple2;
import static greencity.ModelUtils.getUserVO;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class HabitServiceImplTest {

    @InjectMocks
    private HabitServiceImpl habitService;

    @Mock
    private HabitRepo habitRepo;

    @Mock
    private HabitTranslationRepo habitTranslationRepo;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CustomHabitMapper customHabitMapper;

    @Mock
    private HabitTranslationMapper habitTranslationMapper;

    @Mock
    private CustomToDoListMapper customToDoListMapper;

    @Mock
    private CustomToDoListResponseDtoMapper customToDoListResponseDtoMapper;

    @Mock
    private HabitTranslationDtoMapper habitTranslationDtoMapper;

    @Mock
    FileService fileService;

    @Mock
    private ToDoListItemTranslationRepo toDoListItemTranslationRepo;

    @Mock
    private HabitAssignRepo habitAssignRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private TagsRepo tagsRepo;

    @Mock
    private LanguageRepo languageRepo;

    @Mock
    private CustomToDoListItemRepo customToDoListItemRepo;

    @Mock
    private RatingPointsRepo ratingPointsRepo;

    @Mock
    private HabitAssignService habitAssignService;

    @Mock
    private HabitInvitationService habitInvitationService;

    @Mock
    private RatingCalculation ratingCalculation;

    @Mock
    private AchievementCalculation achievementCalculation;

    @Mock
    private UserNotificationServiceImpl userNotificationService;

    @Mock
    private HabitInvitationRepo habitInvitationRepo;

    @Mock
    private FriendService friendService;

    @Test()
    void getByIdAndLanguageCodeIsCustomHabitFalse() {
        Habit habit = ModelUtils.getHabit();
        habit.setIsCustomHabit(false);
        HabitDto habitDto = ModelUtils.getHabitDto();
        habitDto.setIsCustomHabit(false);
        HabitTranslation habitTranslation = ModelUtils.getHabitTranslation();
        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(habitTranslationRepo.findByHabitAndLanguageCode(habit, "en"))
            .thenReturn(Optional.of(habitTranslation));
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(habitDto);
        when(habitAssignRepo.findAmountOfUsersAcquired(anyLong())).thenReturn(5L);
        assertEquals(habitDto, habitService.getByIdAndLanguageCode(1L, "en"));
        verify(habitRepo).findById(1L);
        verify(habitTranslationRepo).findByHabitAndLanguageCode(habit, "en");
        verify(modelMapper).map(habitTranslation, HabitDto.class);
        verify(habitAssignRepo).findAmountOfUsersAcquired(anyLong());
    }

    @Test()
    void getByIdAndLanguageCodeIsCustomHabitTrue() {
        Habit habit = ModelUtils.getHabit();
        habit.setIsCustomHabit(true);
        habit.setCustomToDoListItems(List.of(ModelUtils.getCustomToDoListItem()));
        HabitDto habitDto = ModelUtils.getHabitDto();
        habitDto.setIsCustomHabit(true);
        habitDto.setCustomToDoListItems(List.of(ModelUtils.getCustomToDoListItemResponseDto()));
        HabitTranslation habitTranslation = ModelUtils.getHabitTranslation();
        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(habitTranslationRepo.findByHabitAndLanguageCode(habit, "en"))
            .thenReturn(Optional.of(habitTranslation));
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(habitDto);
        when(habitAssignRepo.findAmountOfUsersAcquired(anyLong())).thenReturn(5L);
        assertEquals(habitDto, habitService.getByIdAndLanguageCode(1L, "en"));
        verify(habitRepo).findById(1L);
        verify(habitTranslationRepo).findByHabitAndLanguageCode(habit, "en");
        verify(modelMapper).map(habitTranslation, HabitDto.class);
        verify(habitAssignRepo).findAmountOfUsersAcquired(anyLong());
    }

    @Test
    void getByIdAndLanguageCodeNotFoundException() {
        assertThrows(NotFoundException.class, () -> habitService.getByIdAndLanguageCode(1L, "en"));
    }

    @Test
    void getByIdAndLanguageCodeHabitTranslationNotFoundException2() {
        Habit habit = ModelUtils.getHabit();
        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        assertThrows(NotFoundException.class, () -> habitService.getByIdAndLanguageCode(1L, "en"));
    }

    @Test
    void getAllHabitsByLanguageCode() {
        Pageable pageable = PageRequest.of(0, 2);
        HabitTranslation habitTranslationUa = ModelUtils.getHabitTranslationUa();
        HabitTranslation habitTranslation = ModelUtils.getHabitTranslation();
        Page<HabitTranslation> habitTranslationPage =
            new PageImpl<>(Collections.singletonList(habitTranslation), pageable, 10);
        String languageCode = "en";
        Habit habit = ModelUtils.getHabit();
        habit.setIsCustomHabit(true);
        habit.setUserId(1L);
        HabitDto habitDto = ModelUtils.getHabitDto();
        habitDto.setIsCustomHabit(true);
        UserVO userVO = ModelUtils.getUserVO();
        List<Long> requestedCustomHabitIds = List.of(1L);
        when(habitAssignRepo.findAllHabitIdsByUserIdAndStatusIsRequested(1L)).thenReturn(requestedCustomHabitIds);
        when(habitTranslationRepo.findAllByLanguageCodeAndHabitAssignIdsRequestedAndUserId(pageable,
            requestedCustomHabitIds, userVO.getId(), "en")).thenReturn(habitTranslationPage);
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(habitDto);
        when(habitAssignRepo.findAmountOfUsersAcquired(anyLong())).thenReturn(5L);
        when(habitRepo.findById(1L)).thenReturn(Optional.ofNullable(habit));
        when(habitAssignRepo.findHabitsByHabitIdAndUserId(anyLong(), anyLong()))
            .thenReturn(List.of(getHabitAssign(), getHabitAssign(HabitAssignStatus.INPROGRESS)));
        when(habitTranslationRepo.getHabitTranslationByUaLanguage(habit.getId())).thenReturn(habitTranslationUa);
        when(userRepo.findUserLanguageCodeByUserId(userVO.getId())).thenReturn("en");
        List<HabitDto> habitDtoList = Collections.singletonList(habitDto);
        PageableDto pageableDto = new PageableDto(habitDtoList, habitTranslationPage.getTotalElements(),
            habitTranslationPage.getPageable().getPageNumber(), habitTranslationPage.getTotalPages());
        assertEquals(pageableDto, habitService.getAllHabitsByLanguageCode(userVO, pageable, languageCode));
        assertDoesNotThrow(() -> new IllegalArgumentException(ErrorMessage.EMPTY_HABIT_ASSIGN_LIST));

        verify(habitTranslationRepo).findAllByLanguageCodeAndHabitAssignIdsRequestedAndUserId(any(Pageable.class),
            anyList(), anyLong(), anyString());
        verify(habitTranslationRepo).getHabitTranslationByUaLanguage(anyLong());
        verify(modelMapper).map(habitTranslation, HabitDto.class);
        verify(habitAssignRepo).findAmountOfUsersAcquired(anyLong());
        verify(habitAssignRepo).findHabitsByHabitIdAndUserId(anyLong(), anyLong());
        verify(habitAssignRepo).findAllHabitIdsByUserIdAndStatusIsRequested(anyLong());
        verify(habitRepo).findById(1L);
    }

    @Test
    void getMyHabits() {
        Pageable pageable = PageRequest.of(0, 2);
        Long userId = 0L;
        String languageCode = "en";
        HabitTranslation habitTranslation = ModelUtils.getHabitTranslation();
        HabitTranslation habitTranslationUa = ModelUtils.getHabitTranslationUa();
        HabitDto habitDto = ModelUtils.getHabitDto();
        habitDto.setIsCustomHabit(true);
        List<HabitDto> habitDtoList = Collections.singletonList(habitDto);
        Page<HabitTranslation> habitTranslationPage =
            new PageImpl<>(Collections.singletonList(habitTranslation), pageable, 10);
        PageableDto<HabitDto> pageableDto = new PageableDto<>(habitDtoList, habitTranslationPage.getTotalElements(),
            habitTranslationPage.getPageable().getPageNumber(), habitTranslationPage.getTotalPages());
        Habit habit = ModelUtils.getHabit();
        habit.setIsCustomHabit(true);
        habit.setUserId(1L);

        when(habitTranslationRepo.findMyHabits(pageable, userId, languageCode))
            .thenReturn(habitTranslationPage);
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(habitDto);
        when(habitAssignRepo.findAmountOfUsersAcquired(anyLong())).thenReturn(5L);
        when(habitRepo.findById(1L)).thenReturn(Optional.ofNullable(habit));
        when(habitAssignRepo.findHabitsByHabitIdAndUserId(anyLong(), anyLong()))
            .thenReturn(List.of(getHabitAssign(), getHabitAssign(HabitAssignStatus.INPROGRESS)));
        when(habitTranslationRepo.getHabitTranslationByUaLanguage(habit.getId())).thenReturn(habitTranslationUa);

        assertEquals(pageableDto, habitService.getMyHabits(userId, pageable, languageCode));

        verify(habitTranslationRepo).findMyHabits(pageable, userId, languageCode);
        verify(habitTranslationRepo).getHabitTranslationByUaLanguage(anyLong());
        verify(modelMapper).map(habitTranslation, HabitDto.class);
        verify(habitAssignRepo).findAmountOfUsersAcquired(anyLong());
        verify(habitAssignRepo).findHabitsByHabitIdAndUserId(anyLong(), anyLong());
        verify(habitRepo).findById(1L);
    }

    @Test
    void getAllHabitsOfFriend() {
        Pageable pageable = PageRequest.of(0, 2);
        Long userId = 0L;
        Long friendId = 1L;
        String languageCode = "en";
        HabitTranslation habitTranslation = ModelUtils.getHabitTranslation();
        HabitTranslation habitTranslationUa = ModelUtils.getHabitTranslationUa();
        HabitDto habitDto = ModelUtils.getHabitDto();
        habitDto.setIsCustomHabit(true);
        List<HabitDto> habitDtoList = Collections.singletonList(habitDto);
        Page<HabitTranslation> habitTranslationPage =
            new PageImpl<>(Collections.singletonList(habitTranslation), pageable, 10);
        PageableDto<HabitDto> pageableDto = new PageableDto<>(habitDtoList, habitTranslationPage.getTotalElements(),
            habitTranslationPage.getPageable().getPageNumber(), habitTranslationPage.getTotalPages());
        Habit habit = ModelUtils.getHabit();
        habit.setIsCustomHabit(true);
        habit.setUserId(1L);
        UserVO userVO = ModelUtils.getUserVO();
        List<Long> requestedCustomHabitIds = List.of(1L);

        when(userRepo.isFriend(userId, friendId)).thenReturn(true);
        when(habitTranslationRepo.findAllHabitsOfFriend(pageable, friendId, languageCode))
            .thenReturn(habitTranslationPage);
        when(habitTranslationRepo.findAllByLanguageCodeAndHabitAssignIdsRequestedAndUserId(pageable,
            requestedCustomHabitIds, userVO.getId(), languageCode)).thenReturn(habitTranslationPage);
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(habitDto);
        when(habitAssignRepo.findAmountOfUsersAcquired(anyLong())).thenReturn(5L);
        when(habitRepo.findById(1L)).thenReturn(Optional.ofNullable(habit));
        when(habitAssignRepo.findHabitsByHabitIdAndUserId(anyLong(), anyLong()))
            .thenReturn(List.of(getHabitAssign(), getHabitAssign(HabitAssignStatus.INPROGRESS)));
        when(habitTranslationRepo.getHabitTranslationByUaLanguage(habit.getId())).thenReturn(habitTranslationUa);
        when(userRepo.findUserLanguageCodeByUserId(userVO.getId())).thenReturn(languageCode);

        assertEquals(pageableDto, habitService.getAllHabitsOfFriend(userId, friendId, pageable, languageCode));

        verify(userRepo).isFriend(userId, friendId);
        verify(habitTranslationRepo).findAllHabitsOfFriend(pageable, friendId, languageCode);
        verify(habitTranslationRepo).getHabitTranslationByUaLanguage(anyLong());
        verify(modelMapper).map(habitTranslation, HabitDto.class);
        verify(habitAssignRepo).findAmountOfUsersAcquired(anyLong());
        verify(habitAssignRepo, times(2)).findHabitsByHabitIdAndUserId(anyLong(), anyLong());
        verify(habitRepo).findById(1L);
    }

    @Test
    void getAllHabitsOfFriendUserHasNoFriendException() {
        Long userId = 1L;
        Long friendId = 2L;
        String languageCode = "en";
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepo.isFriend(userId, friendId)).thenReturn(false);

        assertThrows(UserHasNoFriendWithIdException.class, () -> {
            habitService.getAllHabitsOfFriend(userId, friendId, pageable, languageCode);
        });

        verify(userRepo, never()).findUserLanguageCodeByUserId(anyLong());
        verify(habitTranslationRepo, never()).findAllMutualHabitsWithFriend(any(Pageable.class), anyLong(), anyLong(),
            anyString());
    }

    @Test
    void getAllMutualHabitsWithFriend() {
        Pageable pageable = PageRequest.of(0, 2);
        Long userId = 0L;
        Long friendId = 1L;
        String languageCode = "en";
        HabitTranslation habitTranslation = ModelUtils.getHabitTranslation();
        HabitTranslation habitTranslationUa = ModelUtils.getHabitTranslationUa();
        HabitDto habitDto = ModelUtils.getHabitDto();
        habitDto.setIsCustomHabit(true);
        List<HabitDto> habitDtoList = Collections.singletonList(habitDto);
        Page<HabitTranslation> habitTranslationPage =
            new PageImpl<>(Collections.singletonList(habitTranslation), pageable, 10);
        PageableDto<HabitDto> pageableDto = new PageableDto<>(habitDtoList, habitTranslationPage.getTotalElements(),
            habitTranslationPage.getPageable().getPageNumber(), habitTranslationPage.getTotalPages());
        Habit habit = ModelUtils.getHabit();
        habit.setIsCustomHabit(true);
        habit.setUserId(1L);
        UserVO userVO = ModelUtils.getUserVO();
        List<Long> requestedCustomHabitIds = List.of(1L);

        when(userRepo.isFriend(userId, friendId)).thenReturn(true);
        when(habitTranslationRepo.findAllMutualHabitsWithFriend(pageable, userId, friendId, languageCode))
            .thenReturn(habitTranslationPage);
        when(habitTranslationRepo.findAllByLanguageCodeAndHabitAssignIdsRequestedAndUserId(pageable,
            requestedCustomHabitIds, userVO.getId(), languageCode)).thenReturn(habitTranslationPage);
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(habitDto);
        when(habitAssignRepo.findAmountOfUsersAcquired(anyLong())).thenReturn(5L);
        when(habitRepo.findById(1L)).thenReturn(Optional.ofNullable(habit));
        when(habitAssignRepo.findHabitsByHabitIdAndUserId(anyLong(), anyLong()))
            .thenReturn(List.of(getHabitAssign(), getHabitAssign(HabitAssignStatus.INPROGRESS)));
        when(habitTranslationRepo.getHabitTranslationByUaLanguage(habit.getId())).thenReturn(habitTranslationUa);
        when(userRepo.findUserLanguageCodeByUserId(userVO.getId())).thenReturn(languageCode);

        assertEquals(pageableDto, habitService.getAllMutualHabitsWithFriend(userId, friendId, pageable, languageCode));

        verify(userRepo).isFriend(userId, friendId);
        verify(habitTranslationRepo).findAllMutualHabitsWithFriend(pageable, userId, friendId, languageCode);
        verify(habitTranslationRepo).getHabitTranslationByUaLanguage(anyLong());
        verify(modelMapper).map(habitTranslation, HabitDto.class);
        verify(habitAssignRepo).findAmountOfUsersAcquired(anyLong());
        verify(habitAssignRepo, times(2)).findHabitsByHabitIdAndUserId(anyLong(), anyLong());
        verify(habitRepo).findById(1L);
    }

    @Test
    void getAllMutualHabitsWithFriendUserHasNoFriendException() {
        Long userId = 1L;
        Long friendId = 2L;
        String languageCode = "en";
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepo.isFriend(userId, friendId)).thenReturn(false);

        assertThrows(UserHasNoFriendWithIdException.class, () -> {
            habitService.getAllMutualHabitsWithFriend(userId, friendId, pageable, languageCode);
        });

        verify(userRepo, never()).findUserLanguageCodeByUserId(anyLong());
        verify(habitTranslationRepo, never()).findAllMutualHabitsWithFriend(any(Pageable.class), anyLong(), anyLong(),
            anyString());
    }

    @Test
    void getAllHabitsByLanguageCodeWhenRequestedCustomHabitIdsIsEmpty() {
        Pageable pageable = PageRequest.of(0, 2);
        HabitTranslation habitTranslation = ModelUtils.getHabitTranslation();
        HabitTranslation habitTranslationUa = ModelUtils.getHabitTranslationUa();
        String languageCode = "en";
        Page<HabitTranslation> habitTranslationPage =
            new PageImpl<>(Collections.singletonList(habitTranslation), pageable, 10);
        Habit habit = ModelUtils.getHabit();
        habit.setIsCustomHabit(true);
        habit.setUserId(1L);
        HabitDto habitDto = ModelUtils.getHabitDto();
        habitDto.setIsCustomHabit(true);
        habitDto.setHabitAssignStatus(HabitAssignStatus.ACQUIRED);
        UserVO userVO = ModelUtils.getUserVO();
        List<Long> requestedCustomHabitIds = new ArrayList<>();
        when(habitAssignRepo.findAllHabitIdsByUserIdAndStatusIsRequested(1L)).thenReturn(requestedCustomHabitIds);
        when(habitTranslationRepo.findAllByLanguageCodeAndHabitAssignIdsRequestedAndUserId(pageable,
            requestedCustomHabitIds, userVO.getId(), languageCode)).thenReturn(habitTranslationPage);
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(habitDto);
        when(habitAssignRepo.findAmountOfUsersAcquired(anyLong())).thenReturn(5L);
        when(habitRepo.findById(1L)).thenReturn(Optional.ofNullable(habit));
        when(habitAssignRepo.findHabitsByHabitIdAndUserId(anyLong(), anyLong()))
            .thenReturn(List.of(getHabitAssign(), getHabitAssign()));
        when(habitTranslationRepo.getHabitTranslationByUaLanguage(habit.getId())).thenReturn(habitTranslationUa);
        List<HabitDto> habitDtoList = Collections.singletonList(habitDto);
        PageableDto pageableDto = new PageableDto(habitDtoList, habitTranslationPage.getTotalElements(),
            habitTranslationPage.getPageable().getPageNumber(), habitTranslationPage.getTotalPages());
        assertEquals(pageableDto, habitService.getAllHabitsByLanguageCode(userVO, pageable, languageCode));
        assertDoesNotThrow(() -> new IllegalArgumentException(ErrorMessage.EMPTY_HABIT_ASSIGN_LIST));

        verify(habitTranslationRepo).findAllByLanguageCodeAndHabitAssignIdsRequestedAndUserId(any(Pageable.class),
            anyList(), anyLong(), anyString());
        verify(modelMapper).map(habitTranslation, HabitDto.class);
        verify(habitAssignRepo).findAmountOfUsersAcquired(anyLong());
        verify(habitAssignRepo).findHabitsByHabitIdAndUserId(anyLong(), anyLong());
        verify(habitAssignRepo).findAllHabitIdsByUserIdAndStatusIsRequested(anyLong());
        verify(habitTranslationRepo).getHabitTranslationByUaLanguage(anyLong());
        verify(habitRepo).findById(1L);
    }

    @Test
    void getAllByTagsAndLanguageCodeWithoutExcluded() {
        Pageable pageable = PageRequest.of(0, 2);
        String tag = "ECO_NEWS";
        List<String> tags = Collections.singletonList(tag);
        List<String> lowerCaseTags = Collections.singletonList(tag.toLowerCase());
        boolean excludeAssigned = false;
        Long userId = 1L;
        HabitTranslation habitTranslation = ModelUtils.getHabitTranslation();
        HabitDto habitDto = ModelUtils.getHabitDto();
        Page<HabitTranslation> habitTranslationPage =
            new PageImpl<>(Collections.singletonList(habitTranslation), pageable, 10);
        List<HabitDto> habitDtoList = Collections.singletonList(habitDto);
        PageableDto pageableDto = new PageableDto(habitDtoList, habitTranslationPage.getTotalElements(),
            habitTranslationPage.getPageable().getPageNumber(), habitTranslationPage.getTotalPages());
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(habitDto);
        when(habitAssignRepo.findAmountOfUsersAcquired(anyLong())).thenReturn(5L);
        when(habitTranslationRepo.findAllByTagsAndLanguageCode(pageable, lowerCaseTags, "en"))
            .thenReturn(habitTranslationPage);
        assertEquals(pageableDto,
            habitService.getAllByTagsAndLanguageCode(pageable, tags, "en", excludeAssigned, userId));
    }

    @Test
    void getAllByTagsAndLanguageCodeWithExcluded() {
        Pageable pageable = PageRequest.of(0, 2);
        String tag = "ECO_NEWS";
        List<String> tags = Collections.singletonList(tag);
        List<String> lowerCaseTags = Collections.singletonList(tag.toLowerCase());
        boolean excludeAssigned = true;
        Long userId = 1L;
        HabitTranslation habitTranslation = ModelUtils.getHabitTranslation();
        HabitDto habitDto = ModelUtils.getHabitDto();
        Page<HabitTranslation> habitTranslationPage =
            new PageImpl<>(Collections.singletonList(habitTranslation), pageable, 10);
        List<HabitDto> habitDtoList = Collections.singletonList(habitDto);
        PageableDto pageableDto = new PageableDto(habitDtoList, habitTranslationPage.getTotalElements(),
            habitTranslationPage.getPageable().getPageNumber(), habitTranslationPage.getTotalPages());
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(habitDto);
        when(habitAssignRepo.findAmountOfUsersAcquired(anyLong())).thenReturn(5L);
        when(habitTranslationRepo.findUnassignedHabitTranslationsByLanguageAndTags(pageable, lowerCaseTags, "en",
            userId))
            .thenReturn(habitTranslationPage);
        assertEquals(pageableDto,
            habitService.getAllByTagsAndLanguageCode(pageable, tags, "en", excludeAssigned, userId));
    }

    @Test
    void getAllByDifferentParameters() {
        Pageable pageable = PageRequest.of(0, 2);
        UserVO userVO = getUserVO();
        List<String> tags = List.of("reusable");
        List<Integer> complexities = List.of(1, 2, 3);
        Boolean isCustomHabit = true;
        String languageCode = "ua";
        HabitTranslation habitTranslation = getHabitTranslation();
        HabitTranslation habitTranslationUa = getHabitTranslationUa();
        Page<HabitTranslation> habitTranslationPage =
            new PageImpl<>(Collections.singletonList(habitTranslation), pageable, 10);
        Habit habit = getHabit();
        habit.setIsCustomHabit(true);
        habit.setUserId(1L);
        HabitDto habitDto = getHabitDto();
        habitDto.setIsCustomHabit(true);
        habitDto.setHabitAssignStatus(HabitAssignStatus.ACQUIRED);

        when(habitTranslationRepo.findAll(any(HabitTranslationFilter.class), any(Pageable.class)))
            .thenReturn(habitTranslationPage);
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(habitDto);
        when(habitAssignRepo.findAmountOfUsersAcquired(anyLong())).thenReturn(5L);
        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(habitTranslationRepo.getHabitTranslationByUaLanguage(habit.getId())).thenReturn(habitTranslationUa);
        when(habitAssignRepo.findHabitsByHabitIdAndUserId(anyLong(), anyLong()))
            .thenReturn(List.of(getHabitAssign(), getHabitAssign()));
        List<HabitDto> habitDtoList = Collections.singletonList(habitDto);
        PageableDto pageableDto = new PageableDto(habitDtoList, habitTranslationPage.getTotalElements(),
            habitTranslationPage.getPageable().getPageNumber(), habitTranslationPage.getTotalPages());

        assertEquals(pageableDto, habitService.getAllByDifferentParameters(userVO, pageable, Optional.of(tags),
            Optional.of(isCustomHabit), Optional.of(complexities), languageCode));
        assertDoesNotThrow(() -> new IllegalArgumentException(ErrorMessage.EMPTY_HABIT_ASSIGN_LIST));

        verify(habitTranslationRepo).findAll(any(HabitTranslationFilter.class), any(Pageable.class));
        verify(modelMapper).map(habitTranslation, HabitDto.class);
        verify(habitAssignRepo).findAmountOfUsersAcquired(anyLong());
        verify(habitRepo).findById(anyLong());
        verify(habitAssignRepo).findHabitsByHabitIdAndUserId(anyLong(), anyLong());
        verify(habitTranslationRepo).getHabitTranslationByUaLanguage(anyLong());
    }

    @Test
    void addToDoListItemToHabitTest() {
        doNothing().when(habitRepo).addToDoListItemToHabit(1L, 1L);
        habitService.addToDoListItemToHabit(1L, 1L);
        verify(habitRepo).addToDoListItemToHabit(1L, 1L);
    }

    @Test
    void deleteToDoListItemTest() {
        doNothing().when(habitRepo).upadateToDoListItemInHabit(1L, 1L);
        habitService.deleteToDoListItem(1L, 1L);
        verify(habitRepo).upadateToDoListItemInHabit(1L, 1L);
    }

    @Test
    void addAllToDoListItemToHabitTest() {
        List<Long> listID = Collections.singletonList(1L);
        doNothing().when(habitRepo).addToDoListItemToHabit(listID.getFirst(), 1L);
        habitService.addAllToDoListItemsByListOfId(1L, listID);
        verify(habitRepo, times(1)).addToDoListItemToHabit(listID.getFirst(), 1L);
    }

    @Test
    void deleteAllToDoListItemToHabitTest() {
        List<Long> listID = Collections.singletonList(1L);
        doNothing().when(habitRepo).addToDoListItemToHabit(listID.getFirst(), 1L);
        habitService.deleteAllToDoListItemsByListOfId(1L, listID);
        verify(habitRepo, times(1)).upadateToDoListItemInHabit(listID.getFirst(), 1L);
    }

    @Test
    void addCustomHabitTestWithImagePathInDto() throws IOException {
        User user = ModelUtils.getUser();
        Tag tag = ModelUtils.getTagHabitForServiceTest();
        Language languageUa = ModelUtils.getLanguageUa();
        Language languageEn = ModelUtils.getLanguage();
        Habit habit = ModelUtils.getCustomHabitForServiceTest();
        MultipartFile image = ModelUtils.getFile();
        String imageToEncode = Base64.getEncoder().encodeToString(image.getBytes());
        habit.setTags(Set.of(tag));
        habit.setUserId(1L);
        habit.setImage(imageToEncode);
        CustomToDoListItem customToDoListItem = ModelUtils.getCustomToDoListItemForServiceTest();

        CustomHabitDtoRequest addCustomHabitDtoRequest =
            ModelUtils.getAddCustomHabitDtoRequestForServiceTest();
        addCustomHabitDtoRequest.setImage(imageToEncode);
        addCustomHabitDtoRequest.setCustomToDoListItemDto(List.of(getCustomToDoListItemRequestDto()));
        CustomHabitDtoResponse addCustomHabitDtoResponse = ModelUtils.getAddCustomHabitDtoResponse();
        addCustomHabitDtoResponse.setImage(imageToEncode);

        HabitTranslationDto habitTranslationDto = ModelUtils.getHabitTranslationDto();

        List<HabitTranslationDto> habitTranslationDtoList = List.of(
            habitTranslationDto.setLanguageCode("en"),
            habitTranslationDto.setLanguageCode("ua"));

        HabitTranslation habitTranslationUa = ModelUtils.getHabitTranslationForServiceTest();
        List<HabitTranslation> habitTranslationList = List.of(
            habitTranslationUa.setLanguage(languageEn),
            habitTranslationUa.setLanguage(languageUa));

        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(habitRepo.save(customHabitMapper.convert(addCustomHabitDtoRequest))).thenReturn(habit);
        when(tagsRepo.findById(20L)).thenReturn(Optional.of(tag));
        when(habitTranslationMapper.mapAllToList(List.of(habitTranslationDto), "ua"))
            .thenReturn(List.of(habitTranslationUa));
        when(habitTranslationMapper.mapAllToList(List.of(habitTranslationDto), "en"))
            .thenReturn(List.of(habitTranslationUa));
        when(languageRepo.findByCode("ua")).thenReturn(Optional.of(languageUa));
        when(languageRepo.findByCode("en")).thenReturn(Optional.of(languageEn));
        when(modelMapper.map(habit, CustomHabitDtoResponse.class)).thenReturn(addCustomHabitDtoResponse);
        when(customToDoListItemRepo.findAllByHabitIdAndIsDefaultTrue(anyLong()))
            .thenReturn(List.of(customToDoListItem));
        when(habitTranslationRepo.findAllByHabit(habit)).thenReturn(habitTranslationList);
        when(habitTranslationDtoMapper.mapAllToList(habitTranslationList)).thenReturn(habitTranslationDtoList);

        when(fileService.upload(image)).thenReturn(imageToEncode);

        assertEquals(addCustomHabitDtoResponse,
            habitService.addCustomHabit(addCustomHabitDtoRequest, null, "taras@gmail.com"));

        verify(userRepo).findByEmail(user.getEmail());
        verify(habitRepo).save(customHabitMapper.convert(addCustomHabitDtoRequest));
        verify(customHabitMapper, times(3)).convert(addCustomHabitDtoRequest);
        verify(tagsRepo).findById(20L);
        verify(habitTranslationMapper, times(1)).mapAllToList(List.of(habitTranslationDto), "ua");
        verify(habitTranslationMapper, times(1)).mapAllToList(List.of(habitTranslationDto), "en");
        verify(languageRepo, times(2)).findByCode(anyString());
        verify(modelMapper).map(habit, CustomHabitDtoResponse.class);
        verify(habitTranslationRepo).findAllByHabit(habit);
        verify(habitTranslationDtoMapper).mapAllToList(habitTranslationList);
        verify(fileService).convertToMultipartImage(any());
    }

    @Test
    void addCustomHabitTestWithImageFile() throws IOException {
        User user = ModelUtils.getUser();
        Tag tag = ModelUtils.getTagHabitForServiceTest();
        Language languageUa = ModelUtils.getLanguageUa();
        Language languageEn = ModelUtils.getLanguage();
        Habit habit = ModelUtils.getCustomHabitForServiceTest();
        MultipartFile image = ModelUtils.getFile();
        String imageToEncode = Base64.getEncoder().encodeToString(image.getBytes());
        habit.setTags(Set.of(tag));
        habit.setUserId(1L);
        habit.setImage(imageToEncode);
        CustomToDoListItem customToDoListItem = ModelUtils.getCustomToDoListItemForServiceTest();

        CustomHabitDtoRequest addCustomHabitDtoRequest =
            ModelUtils.getAddCustomHabitDtoRequestForServiceTest();
        CustomHabitDtoResponse addCustomHabitDtoResponse = ModelUtils.getAddCustomHabitDtoResponse();
        addCustomHabitDtoResponse.setImage(imageToEncode);

        HabitTranslationDto habitTranslationDto = ModelUtils.getHabitTranslationDto();

        List<HabitTranslationDto> habitTranslationDtoList = List.of(
            habitTranslationDto.setLanguageCode("en"),
            habitTranslationDto.setLanguageCode("ua"));

        HabitTranslation habitTranslationUa = ModelUtils.getHabitTranslationForServiceTest();
        List<HabitTranslation> habitTranslationList = List.of(
            habitTranslationUa.setLanguage(languageEn),
            habitTranslationUa.setLanguage(languageUa));

        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(habitRepo.save(customHabitMapper.convert(addCustomHabitDtoRequest))).thenReturn(habit);
        when(tagsRepo.findById(20L)).thenReturn(Optional.of(tag));
        when(habitTranslationMapper.mapAllToList(List.of(habitTranslationDto)))
            .thenReturn(List.of(habitTranslationUa));
        when(languageRepo.findByCode("ua")).thenReturn(Optional.of(languageUa));
        when(languageRepo.findByCode("en")).thenReturn(Optional.of(languageEn));
        when(customToDoListItemRepo.findAllByUserIdAndHabitId(1L, 1L)).thenReturn(List.of(customToDoListItem));
        when(habitTranslationMapper.mapAllToList(List.of(habitTranslationDto), "ua"))
            .thenReturn(List.of(habitTranslationUa));
        when(habitTranslationMapper.mapAllToList(List.of(habitTranslationDto), "en"))
            .thenReturn(List.of(habitTranslationUa));
        when(modelMapper.map(habit, CustomHabitDtoResponse.class)).thenReturn(addCustomHabitDtoResponse);
        when(customToDoListItemRepo.findAllByHabitIdAndIsDefaultTrue(anyLong()))
            .thenReturn(List.of(customToDoListItem));
        when(habitTranslationRepo.findAllByHabit(habit)).thenReturn(habitTranslationList);
        when(habitTranslationDtoMapper.mapAllToList(habitTranslationList)).thenReturn(habitTranslationDtoList);

        when(fileService.upload(image)).thenReturn(imageToEncode);

        assertEquals(addCustomHabitDtoResponse,
            habitService.addCustomHabit(addCustomHabitDtoRequest, image, "taras@gmail.com"));

        verify(userRepo).findByEmail(user.getEmail());
        verify(habitRepo).save(customHabitMapper.convert(addCustomHabitDtoRequest));
        verify(customHabitMapper, times(3)).convert(addCustomHabitDtoRequest);
        verify(tagsRepo).findById(20L);
        verify(habitTranslationMapper, times(1)).mapAllToList(List.of(habitTranslationDto), "ua");
        verify(habitTranslationMapper, times(1)).mapAllToList(List.of(habitTranslationDto), "en");
        verify(languageRepo, times(2)).findByCode(anyString());
        verify(modelMapper).map(habit, CustomHabitDtoResponse.class);
        verify(habitTranslationRepo).findAllByHabit(habit);
        verify(habitTranslationDtoMapper).mapAllToList(habitTranslationList);
        verify(fileService).upload(any(MultipartFile.class));
    }

    @Test
    void addCustomHabitTest2() throws IOException {
        User user = ModelUtils.getUser();
        Tag tag = ModelUtils.getTagHabitForServiceTest();
        Language languageUa = ModelUtils.getLanguageUa();
        Language languageEn = ModelUtils.getLanguage();
        Habit habit = ModelUtils.getCustomHabitForServiceTest();
        MultipartFile image = ModelUtils.getFile();
        String imageToEncode = Base64.getEncoder().encodeToString(image.getBytes());
        habit.setTags(Set.of(tag));
        habit.setUserId(1L);
        habit.setImage(imageToEncode);
        CustomToDoListItem customToDoListItem = ModelUtils.getCustomToDoListItemForServiceTest();

        CustomHabitDtoRequest addCustomHabitDtoRequest =
            ModelUtils.getAddCustomHabitDtoRequestForServiceTest();
        CustomHabitDtoResponse addCustomHabitDtoResponse = ModelUtils.getAddCustomHabitDtoResponse();
        addCustomHabitDtoResponse.setImage(imageToEncode);

        HabitTranslationDto habitTranslationDto = ModelUtils.getHabitTranslationDto();

        List<HabitTranslationDto> habitTranslationDtoList = List.of(
            habitTranslationDto.setLanguageCode("en"),
            habitTranslationDto.setLanguageCode("ua"));

        HabitTranslation habitTranslationUa = ModelUtils.getHabitTranslationForServiceTest();
        List<HabitTranslation> habitTranslationList = List.of(
            habitTranslationUa.setLanguage(languageEn),
            habitTranslationUa.setLanguage(languageUa));

        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(habitRepo.save(customHabitMapper.convert(addCustomHabitDtoRequest))).thenReturn(habit);
        when(tagsRepo.findById(20L)).thenReturn(Optional.of(tag));
        when(habitTranslationMapper.mapAllToList(List.of(habitTranslationDto), "ua"))
            .thenReturn(List.of(habitTranslationUa));
        when(habitTranslationMapper.mapAllToList(List.of(habitTranslationDto), "en"))
            .thenReturn(List.of(habitTranslationUa));
        when(languageRepo.findByCode("ua")).thenReturn(Optional.of(languageUa));
        when(languageRepo.findByCode("en")).thenReturn(Optional.of(languageEn));
        when(modelMapper.map(habit, CustomHabitDtoResponse.class)).thenReturn(addCustomHabitDtoResponse);
        when(customToDoListItemRepo.findAllByHabitIdAndIsDefaultTrue(anyLong()))
            .thenReturn(List.of(customToDoListItem));
        when(habitTranslationRepo.findAllByHabit(habit)).thenReturn(habitTranslationList);
        when(habitTranslationDtoMapper.mapAllToList(habitTranslationList)).thenReturn(habitTranslationDtoList);

        when(fileService.upload(image)).thenReturn(imageToEncode);

        assertEquals(addCustomHabitDtoResponse,
            habitService.addCustomHabit(addCustomHabitDtoRequest, null, "taras@gmail.com"));

        verify(userRepo).findByEmail(user.getEmail());
        verify(habitRepo).save(customHabitMapper.convert(addCustomHabitDtoRequest));
        verify(customHabitMapper, times(3)).convert(addCustomHabitDtoRequest);
        verify(tagsRepo).findById(20L);
        verify(habitTranslationMapper, times(1)).mapAllToList(List.of(habitTranslationDto), "ua");
        verify(habitTranslationMapper, times(1)).mapAllToList(List.of(habitTranslationDto), "en");
        verify(languageRepo, times(2)).findByCode(anyString());
        verify(modelMapper).map(habit, CustomHabitDtoResponse.class);
        verify(habitTranslationRepo).findAllByHabit(habit);
        verify(habitTranslationDtoMapper).mapAllToList(habitTranslationList);
    }

    @Test
    void addCustomHabitNoSuchElementExceptionWithNotExistingLanguageCodeTestUa() throws IOException {
        User user = ModelUtils.getUser();
        Tag tag = ModelUtils.getTagHabitForServiceTest();
        Habit habit = ModelUtils.getCustomHabitForServiceTest();
        MultipartFile image = ModelUtils.getFile();
        String imageToEncode = Base64.getEncoder().encodeToString(image.getBytes());
        habit.setTags(Set.of(tag));
        habit.setUserId(1L);
        habit.setImage(imageToEncode);
        CustomHabitDtoRequest addCustomHabitDtoRequest =
            ModelUtils.getAddCustomHabitDtoRequestForServiceTest();
        addCustomHabitDtoRequest.setImage(imageToEncode);
        HabitTranslationDto habitTranslationDto = ModelUtils.getHabitTranslationDto();
        habitTranslationDto.setLanguageCode("ua");
        HabitTranslation habitTranslation = ModelUtils.getHabitTranslationForServiceTest();

        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(habitRepo.save(customHabitMapper.convert(addCustomHabitDtoRequest))).thenReturn(habit);
        when(tagsRepo.findById(20L)).thenReturn(Optional.of(tag));
        when(habitTranslationMapper.mapAllToList(addCustomHabitDtoRequest.getHabitTranslations(), "ua"))
            .thenReturn(List.of(habitTranslation));
        when(habitTranslationMapper.mapAllToList(addCustomHabitDtoRequest.getHabitTranslations(), "en"))
            .thenReturn(List.of(habitTranslation));
        when(languageRepo.findByCode("ua")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> habitService.addCustomHabit(addCustomHabitDtoRequest, image, "taras@gmail.com"));

        verify(userRepo).findByEmail(user.getEmail());
        verify(habitRepo).save(customHabitMapper.convert(addCustomHabitDtoRequest));
        verify(customHabitMapper, times(3)).convert(addCustomHabitDtoRequest);
        verify(tagsRepo).findById(20L);
        verify(habitTranslationMapper, times(1)).mapAllToList(List.of(habitTranslationDto), "ua");
        verify(habitTranslationMapper, times(0)).mapAllToList(List.of(habitTranslationDto), "en");
        verify(languageRepo).findByCode(anyString());
    }

    @Test
    void addCustomHabitNoSuchElementExceptionWithNotExistingLanguageCodeEn() throws IOException {
        User user = ModelUtils.getUser();
        Tag tag = ModelUtils.getTagHabitForServiceTest();
        Language languageUa = ModelUtils.getLanguageUa();
        Habit habit = ModelUtils.getCustomHabitForServiceTest();
        MultipartFile image = ModelUtils.getFile();
        String imageToEncode = Base64.getEncoder().encodeToString(image.getBytes());
        habit.setTags(Set.of(tag));
        habit.setUserId(1L);
        habit.setImage(imageToEncode);
        CustomHabitDtoRequest addCustomHabitDtoRequest =
            ModelUtils.getAddCustomHabitDtoRequestForServiceTest();
        HabitTranslationDto habitTranslationDto = ModelUtils.getHabitTranslationDto();
        habitTranslationDto.setLanguageCode("ua");
        HabitTranslation habitTranslationUa = ModelUtils.getHabitTranslationForServiceTest();

        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(habitRepo.save(customHabitMapper.convert(addCustomHabitDtoRequest))).thenReturn(habit);
        when(tagsRepo.findById(20L)).thenReturn(Optional.of(tag));
        when(habitTranslationMapper.mapAllToList(addCustomHabitDtoRequest.getHabitTranslations(), "ua"))
            .thenReturn(List.of(habitTranslationUa));
        when(habitTranslationMapper.mapAllToList(addCustomHabitDtoRequest.getHabitTranslations(), "en"))
            .thenReturn(List.of(habitTranslationUa));
        when(languageRepo.findByCode("ua")).thenReturn(Optional.of(languageUa));
        when(languageRepo.findByCode("en")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> habitService.addCustomHabit(addCustomHabitDtoRequest, image, "taras@gmail.com"));

        verify(userRepo).findByEmail(user.getEmail());
        verify(habitRepo).save(customHabitMapper.convert(addCustomHabitDtoRequest));
        verify(customHabitMapper, times(3)).convert(addCustomHabitDtoRequest);
        verify(tagsRepo).findById(20L);

        verify(habitTranslationMapper, times(1)).mapAllToList(List.of(habitTranslationDto), "ua");
        verify(habitTranslationMapper, times(1)).mapAllToList(List.of(habitTranslationDto), "en");
        verify(languageRepo, times(2)).findByCode(anyString());
    }

    @Test
    void addCustomHabitThrowUserNotFoundException() {
        CustomHabitDtoRequest addCustomHabitDtoRequest =
            ModelUtils.getAddCustomHabitDtoRequestForServiceTest();
        MultipartFile image = ModelUtils.getFile();
        when(userRepo.findByEmail("user@gmail.com")).thenReturn(Optional.empty());
        when(habitRepo.save(customHabitMapper.convert(addCustomHabitDtoRequest))).thenReturn(nullable(Habit.class));

        assertThrows(WrongEmailException.class,
            () -> habitService.addCustomHabit(addCustomHabitDtoRequest, image, "user@gmail.com"));

        verify(userRepo).findByEmail("user@gmail.com");
        verify(customHabitMapper).convert(addCustomHabitDtoRequest);
    }

    @Test
    void getFriendsAssignedToHabitProfilePicturesTest() {
        Long habitAssignId = 1L;
        Long userId = 2L;
        Long friendId = 3L;
        User friend = ModelUtils.getUser();
        friend.setId(friendId);
        friend.setProfilePicturePath("test");
        UserProfilePictureDto friendProfilePicture = UserProfilePictureDto.builder()
            .id(friend.getId())
            .name(friend.getName())
            .profilePicturePath(friend.getProfilePicturePath())
            .build();

        when(userRepo.existsById(userId)).thenReturn(true);
        when(habitAssignRepo.existsById(habitAssignId)).thenReturn(true);
        when(habitInvitationService.getInvitedFriendsIdsTrackingHabitList(userId, habitAssignId))
            .thenReturn(List.of(friendId));
        when(userRepo.findAllById(List.of(friendId))).thenReturn(List.of(friend));
        when(modelMapper.map(friend, UserProfilePictureDto.class)).thenReturn(friendProfilePicture);

        List<UserProfilePictureDto> list = habitService.getFriendsAssignedToHabitProfilePictures(habitAssignId, userId);
        assertFalse(list.isEmpty());
        assertEquals(friendProfilePicture, list.getFirst());

        verify(userRepo).existsById(userId);
        verify(habitAssignRepo).existsById(habitAssignId);
        verify(habitInvitationService).getInvitedFriendsIdsTrackingHabitList(userId, habitAssignId);
        verify(userRepo).findAllById(List.of(friendId));
        verify(modelMapper).map(friend, UserProfilePictureDto.class);
    }

    @Test
    void getFriendsAssignedToHabitProfilePicturesWhenUserNotFoundTest() {
        Long habitId = 1L;
        Long userId = 2L;

        when(userRepo.existsById(userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> habitService.getFriendsAssignedToHabitProfilePictures(habitId, userId));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + userId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(habitRepo, never()).existsById(anyLong());
        verify(userRepo, never()).getFriendsAssignedToHabit(anyLong(), anyLong());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void getFriendsAssignedToHabitProfilePicturesWhenHabitNotFoundTest() {
        Long habitAssignId = 1L;
        Long userId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(habitRepo.existsById(userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> habitService.getFriendsAssignedToHabitProfilePictures(habitAssignId, userId));

        assertEquals(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(habitAssignRepo).existsById(habitAssignId);
        verify(userRepo, never()).getFriendsAssignedToHabit(anyLong(), anyLong());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void updateCustomHabitTest() throws IOException {
        User user = ModelUtils.getUser();
        user.setRole(Role.ROLE_ADMIN);
        Tag tag = ModelUtils.getTagHabitForServiceTest();
        Language languageUa = ModelUtils.getLanguageUa();
        Language languageEn = ModelUtils.getLanguage();
        Habit habit = ModelUtils.getCustomHabitForServiceTest();
        MultipartFile image = ModelUtils.getFile();
        String imageToEncode = Base64.getEncoder().encodeToString(image.getBytes());
        habit.setTags(Set.of(tag));
        habit.setUserId(1L);
        habit.setImage(imageToEncode);
        CustomToDoListItem customToDoListItem = ModelUtils.getCustomToDoListItemForServiceTest();

        CustomHabitDtoRequest customHabitDtoRequest =
            ModelUtils.getAddCustomHabitDtoRequestWithImage();
        CustomHabitDtoResponse customHabitDtoResponse = ModelUtils.getAddCustomHabitDtoResponse();
        customHabitDtoResponse.setImage(imageToEncode);

        HabitTranslationDto habitTranslationDto = ModelUtils.getHabitTranslationDto();

        List<HabitTranslationDto> habitTranslationDtoList = List.of(
            habitTranslationDto.setLanguageCode("en"),
            habitTranslationDto.setLanguageCode("ua"));

        HabitTranslation habitTranslationUa = ModelUtils.getHabitTranslationForServiceTest();
        List<HabitTranslation> habitTranslationList = List.of(
            habitTranslationUa.setLanguage(languageEn),
            habitTranslationUa.setLanguage(languageUa));

        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(habitRepo.save(customHabitMapper.convert(customHabitDtoRequest))).thenReturn(habit);
        when(tagsRepo.findById(20L)).thenReturn(Optional.of(tag));
        when(customToDoListItemRepo.findAllByUserIdAndHabitId(anyLong(), anyLong()))
            .thenReturn(List.of(customToDoListItem));
        when(modelMapper.map(habit, CustomHabitDtoResponse.class)).thenReturn(customHabitDtoResponse);
        when(customToDoListItemRepo.findAllByHabitIdAndIsDefaultTrue(anyLong()))
            .thenReturn(List.of(customToDoListItem));
        when(habitTranslationRepo.findAllByHabit(habit)).thenReturn(habitTranslationList);
        when(habitTranslationDtoMapper.mapAllToList(habitTranslationList)).thenReturn(habitTranslationDtoList);
        when(habitRepo.save(habit)).thenReturn(habit);
        when(fileService.upload(image)).thenReturn(imageToEncode);

        assertEquals(customHabitDtoResponse,
            habitService.updateCustomHabit(customHabitDtoRequest, 1L, "taras@gmail.com", image));

        verify(habitRepo).findById(anyLong());
        verify(userRepo).findByEmail(user.getEmail());
        verify(habitRepo).save(any());
        verify(customHabitMapper).convert(customHabitDtoRequest);
        verify(tagsRepo).findById(20L);
        verify(customToDoListItemRepo).findAllByUserIdAndHabitId(anyLong(), anyLong());
        verify(modelMapper).map(habit, CustomHabitDtoResponse.class);
        verify(habitTranslationRepo, times(2)).findAllByHabit(habit);
        verify(habitTranslationDtoMapper).mapAllToList(habitTranslationList);
    }

    @Test
    void updateCustomHabitThrowsUserNotFoundException() {
        CustomHabitDtoRequest customHabitDtoRequest =
            ModelUtils.getAddCustomHabitDtoRequestForServiceTest();
        when(userRepo.findByEmail("user@gmail.com")).thenReturn(Optional.empty());
        when(habitRepo.save(customHabitMapper.convert(customHabitDtoRequest))).thenReturn(nullable(Habit.class));

        assertThrows(WrongEmailException.class,
            () -> habitService.updateCustomHabit(customHabitDtoRequest, 1L, "user@gmail.com", null));

        verify(userRepo).findByEmail("user@gmail.com");
        verify(customHabitMapper).convert(customHabitDtoRequest);
    }

    @Test
    void updateCustomHabitThrowsUserHasNoPermissionToAccessException() {
        CustomHabitDtoRequest customHabitDtoRequest =
            ModelUtils.getAddCustomHabitDtoRequestWithImage();
        User user = ModelUtils.getUser();
        String email = user.getEmail();
        user.setRole(Role.ROLE_USER);

        Habit habit = ModelUtils.getCustomHabitForServiceTest();

        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(UserHasNoPermissionToAccessException.class,
            () -> habitService.updateCustomHabit(customHabitDtoRequest, 1L, email, null));

        verify(userRepo).findByEmail(anyString());
        verify(habitRepo).findById(anyLong());
    }

    @Test
    void updateCustomHabitWithNewCustomToDoListItemToUpdateTest() throws IOException {
        User user = ModelUtils.getTestUser();
        user.setRole(Role.ROLE_ADMIN);
        Tag tag = ModelUtils.getTagHabitForServiceTest();
        Habit habit = ModelUtils.getCustomHabitForServiceTest();
        MultipartFile image = ModelUtils.getFile();
        String imageToEncode = Base64.getEncoder().encodeToString(image.getBytes());
        habit.setUserId(1L);
        habit.setImage(imageToEncode);
        CustomToDoListItem newItem = ModelUtils.getCustomToDoListItemForUpdate();
        newItem.setId(null);

        CustomHabitDtoRequest customHabitDtoRequest = ModelUtils
            .getCustomHabitDtoRequestWithNewCustomToDoListItem();
        CustomHabitDtoResponse customHabitDtoResponse = ModelUtils.getAddCustomHabitDtoResponse();
        when(customToDoListMapper.mapAllToList(any()))
            .thenReturn(List.of(newItem));
        when(customToDoListItemRepo.save(any())).thenReturn(ModelUtils.getCustomToDoListItemForUpdate());
        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(tagsRepo.findById(20L)).thenReturn(Optional.of(tag));
        when(habitRepo.save(habit)).thenReturn(habit);
        when(fileService.upload(image)).thenReturn(imageToEncode);
        when(modelMapper.map(habit, CustomHabitDtoResponse.class)).thenReturn(customHabitDtoResponse);

        assertEquals(customHabitDtoResponse,
            habitService.updateCustomHabit(customHabitDtoRequest, 1L, "user@email.com", image));

        verify(customToDoListItemRepo).findAllByUserIdAndHabitId(2L, 1L);
        verify(customToDoListItemRepo).save(any());
        verify(habitRepo).findById(anyLong());
        verify(userRepo).findByEmail(user.getEmail());
        verify(habitRepo).save(any());
        verify(tagsRepo).findById(20L);
        verify(modelMapper).map(habit, CustomHabitDtoResponse.class);
        verify(habitTranslationRepo).findAllByHabit(habit);
    }

    @Test
    void updateCustomHabitWithComplexityToUpdateTest() throws IOException {
        User user = ModelUtils.getTestUser();
        user.setRole(Role.ROLE_ADMIN);

        Tag tag = ModelUtils.getTagHabitForServiceTest();
        Habit habit = ModelUtils.getCustomHabitForServiceTest();
        MultipartFile image = ModelUtils.getFile();
        String imageToEncode = Base64.getEncoder().encodeToString(image.getBytes());
        habit.setUserId(1L);
        habit.setImage(imageToEncode);
        habit.setTags(Set.of(tag));

        CustomHabitDtoRequest customHabitDtoRequest = ModelUtils.getСustomHabitDtoRequestWithComplexityAndDuration();
        CustomHabitDtoResponse customHabitDtoResponse = ModelUtils.getAddCustomHabitDtoResponse();

        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(habitRepo.save(customHabitMapper.convert(customHabitDtoRequest))).thenReturn(habit);
        when(modelMapper.map(habit, CustomHabitDtoResponse.class)).thenReturn(customHabitDtoResponse);
        when(habitRepo.save(habit)).thenReturn(habit);

        assertEquals(customHabitDtoResponse,
            habitService.updateCustomHabit(customHabitDtoRequest, 1L, "user@email.com", null));

        verify(habitRepo).findById(anyLong());
        verify(userRepo).findByEmail(user.getEmail());
        verify(habitRepo).save(any());
        verify(modelMapper).map(habit, CustomHabitDtoResponse.class);
        verify(habitTranslationRepo).findAllByHabit(habit);
    }

    @Test
    void updateCustomHabitThrowsUserHasNoPermissionToAccessExceptionWithDiffrentUserId() {
        CustomHabitDtoRequest customHabitDtoRequest =
            ModelUtils.getAddCustomHabitDtoRequestWithImage();
        User user = ModelUtils.getTestUser();
        String email = user.getEmail();
        user.setRole(Role.ROLE_USER);

        Habit habit = ModelUtils.getCustomHabitForServiceTest();
        habit.setUserId(1L);

        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(UserHasNoPermissionToAccessException.class,
            () -> habitService.updateCustomHabit(customHabitDtoRequest, 1L, email, null));

        assertNotEquals(user.getId(), habit.getUserId());
        verify(userRepo).findByEmail(anyString());
        verify(habitRepo).findById(anyLong());
    }

    @Test
    void deleteCustomHabitSuccessTest() {
        Long customHabitId = 1L;
        Habit toDelete = ModelUtils.getHabitWithCustom();
        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        User user = ModelUtils.getUser();
        toDelete.setUserId(1L);
        when(habitRepo.findByIdAndIsCustomHabitIsTrue(customHabitId))
            .thenReturn(Optional.of(toDelete));
        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(habitRepo.findHabitAssignByHabitIdAndHabitOwnerId(customHabitId, 1L))
            .thenReturn(List.of(habitAssign.getId()));

        habitService.deleteCustomHabit(customHabitId, user.getEmail());

        verify(habitRepo).findByIdAndIsCustomHabitIsTrue(customHabitId);
        verify(userRepo).findByEmail(user.getEmail());
        verify(habitRepo).findHabitAssignByHabitIdAndHabitOwnerId(customHabitId, 1L);
    }

    @Test
    void deleteCustomHabitThrowsNotFoundExceptionTest() {
        Long customHabitId = 1L;
        String userEmail = "email@gmail.com";

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> habitService.deleteCustomHabit(customHabitId, userEmail));

        assertEquals(ErrorMessage.CUSTOM_HABIT_NOT_FOUND + customHabitId, exception.getMessage());
        verify(habitRepo, times(0)).save(any(Habit.class));
    }

    @Test
    void deleteCustomHabitThrowsWrongEmailExceptionTest() {
        Long customHabitId = 1L;
        String userEmail = "email@gmail.com";
        Habit toDelete = ModelUtils.getHabitWithCustom();
        toDelete.setUserId(1L);
        when(habitRepo.findByIdAndIsCustomHabitIsTrue(customHabitId))
            .thenReturn(Optional.of(toDelete));

        WrongEmailException exception = assertThrows(WrongEmailException.class,
            () -> habitService.deleteCustomHabit(customHabitId, userEmail));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_EMAIL + userEmail, exception.getMessage());
        verify(habitRepo, times(0)).save(any(Habit.class));
        verify(habitRepo).findByIdAndIsCustomHabitIsTrue(customHabitId);
    }

    @Test
    void checkAccessOfOwnerToCustomHabitThrowsUserHasNoPermissionToAccessExceptionTest() {
        Long customHabitId = 1L;
        Habit toDelete = ModelUtils.getHabitWithCustom();
        User user = ModelUtils.getUser();
        String userEmail = user.getEmail();
        toDelete.setUserId(4L);
        when(habitRepo.findByIdAndIsCustomHabitIsTrue(customHabitId))
            .thenReturn(Optional.of(toDelete));
        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        UserHasNoPermissionToAccessException exception = assertThrows(UserHasNoPermissionToAccessException.class,
            () -> habitService.deleteCustomHabit(customHabitId, userEmail));

        assertEquals(ErrorMessage.USER_HAS_NO_PERMISSION, exception.getMessage());

        verify(habitRepo).findByIdAndIsCustomHabitIsTrue(customHabitId);
        verify(userRepo).findByEmail(user.getEmail());
        verify(habitRepo, times(0)).save(any(Habit.class));
    }

    @Test
    void likeTest() {
        UserVO userVO = getUserVO();
        User user = getUser();
        User habitAuthor = getUser().setId(2L);
        Habit habit = getHabit().setUserId(2L);
        RatingPoints ratingPoints = RatingPoints.builder().id(1L).name("LIKE_COMMENT_OR_REPLY").points(1).build();

        when(ratingPointsRepo.findByNameOrThrow("LIKE_COMMENT_OR_REPLY")).thenReturn(ratingPoints);
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(userRepo.findById(habit.getUserId())).thenReturn(Optional.of(habitAuthor));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);

        habitService.like(habit.getId(), userVO);

        assertTrue(habit.getUsersLiked().stream().anyMatch(u -> u.getId().equals(userVO.getId())));

        verify(modelMapper).map(userVO, User.class);
        verify(habitRepo).findById(habit.getId());
        verify(userRepo).findById(habitAuthor.getId());
    }

    @Test
    void removeLikeTest() {
        UserVO userVO = getUserVO();
        User user = getUser();
        User habitAuthor = getUser().setId(2L);
        Habit habit = getHabit().setUserId(2L);
        habit.getUsersLiked().add(user);
        RatingPoints ratingPoints = RatingPoints.builder().id(1L).name("UNDO_LIKE_HABIT").points(-1).build();

        when(ratingPointsRepo.findByNameOrThrow("UNDO_LIKE_HABIT")).thenReturn(ratingPoints);
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(userRepo.findById(habit.getUserId())).thenReturn(Optional.of(habitAuthor));

        habitService.like(habit.getId(), userVO);
        assertFalse(habit.getUsersLiked().stream().anyMatch(u -> u.getId().equals(userVO.getId())));

        verify(habitRepo).findById(habit.getId());
        verify(userRepo).findById(habitAuthor.getId());
    }

    @Test
    void removeLikeRemoveIfTest() {
        User user = getUser();
        Habit habit = getHabit().setUserId(2L);
        habit.getUsersLiked().add(user);

        UserVO userVO = getUserVO();
        userVO.setName("New Name");

        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(userRepo.findById(habit.getUserId())).thenReturn(Optional.of(user));
        habitService.like(habit.getId(), userVO);
        assertFalse(habit.getUsersLiked().stream().anyMatch(u -> u.getId().equals(userVO.getId())));
        verify(habitRepo).findById(habit.getId());
    }

    @Test
    void likeHabitHabitNotFoundTest() {
        UserVO userVO = getUserVO();
        Habit habit = getHabit();
        Long habitId = habit.getId();

        when(habitRepo.findById(habit.getId())).thenReturn(Optional.empty());

        NotFoundException exception =
            assertThrows(NotFoundException.class, () -> habitService.like(habitId, userVO));
        assertEquals(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habit.getId(), exception.getMessage());

        verify(habitRepo).findById(habit.getId());
    }

    @Test
    void likeHabitUserNotFoundTest() {
        UserVO userVO = getUserVO();
        Habit habit = getHabit();
        Long habitId = habit.getId();
        User user = getUser();
        habit.setUserId(3L);

        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(userRepo.findById(habit.getUserId())).thenReturn(Optional.empty());

        NotFoundException exception =
            assertThrows(NotFoundException.class, () -> habitService.like(habitId, userVO));
        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + user.getId(), exception.getMessage());

        verify(habitRepo).findById(habit.getId());
        verify(userRepo).findById(habit.getUserId());
    }

    @Test
    void dislikeTest() {
        UserVO userVO = getUserVO();
        User user = getUser();
        User habitAuthor = getUser().setId(2L);
        Habit habit = getHabit().setUserId(2L);

        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(userRepo.findById(habit.getUserId())).thenReturn(Optional.of(habitAuthor));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);

        habitService.dislike(habit.getId(), userVO);

        assertTrue(habit.getUsersDisliked().stream().anyMatch(u -> u.getId().equals(userVO.getId())));

        verify(modelMapper).map(userVO, User.class);
        verify(habitRepo).findById(habit.getId());
        verify(userRepo).findById(habitAuthor.getId());
    }

    @Test
    void dislikeOwnTest() {
        UserVO userVO = getUserVO();
        User user = getUser();
        Habit habit = getHabit().setUserId(user.getId());
        Long habitId = habit.getId();
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> habitService.dislike(habitId, userVO));
    }

    @Test
    void removeDislikeTest() {
        UserVO userVO = getUserVO();
        User user = getUser();
        User habitAuthor = getUser().setId(2L);
        Habit habit = getHabit().setUserId(2L);
        habit.getUsersDisliked().add(user);

        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(userRepo.findById(habit.getUserId())).thenReturn(Optional.of(habitAuthor));

        habitService.dislike(habit.getId(), userVO);
        assertFalse(habit.getUsersLiked().stream().anyMatch(u -> u.getId().equals(userVO.getId())));

        verify(habitRepo).findById(habit.getId());
        verify(userRepo).findById(habitAuthor.getId());
    }

    @Test
    void removeDislikeRemoveIfTest() {
        User user = getUser();
        User habitAuthor = getUser().setId(2L);
        Habit habit = getHabit().setUserId(2L);
        habit.getUsersDisliked().add(user);

        UserVO userVO = getUserVO();
        userVO.setName("New Name");

        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(userRepo.findById(habit.getUserId())).thenReturn(Optional.of(habitAuthor));
        habitService.dislike(habit.getId(), userVO);
        assertFalse(habit.getUsersLiked().stream().anyMatch(u -> u.getId().equals(userVO.getId())));
        verify(habitRepo).findById(habit.getId());
    }

    @Test
    void dislikeHabitHabitNotFoundTest() {
        UserVO userVO = getUserVO();
        Habit habit = getHabit();
        Long habitId = habit.getId();

        when(habitRepo.findById(habit.getId())).thenReturn(Optional.empty());

        NotFoundException exception =
            assertThrows(NotFoundException.class, () -> habitService.dislike(habitId, userVO));
        assertEquals(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habit.getId(), exception.getMessage());

        verify(habitRepo).findById(habit.getId());
    }

    @Test
    void dislikeHabitUserNotFoundTest() {
        UserVO userVO = getUserVO();
        Habit habit = getHabit();
        Long habitId = habit.getId();
        User user = getUser();
        habit.setUserId(3L);

        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(userRepo.findById(habit.getUserId())).thenReturn(Optional.empty());

        NotFoundException exception =
            assertThrows(NotFoundException.class, () -> habitService.dislike(habitId, userVO));
        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + user.getId(), exception.getMessage());

        verify(habitRepo).findById(habit.getId());
        verify(userRepo).findById(habit.getUserId());
    }

    @Test
    void givenHabitLikedByUser_whenDislikedByUser_shouldRemoveLikeAndAddDislike() {
        UserVO userVO = getUserVO();
        User user = getUser();
        User habitAuthor = getUser().setId(2L);
        Habit habit = getHabit().setUserId(2L);
        habit.setUsersLiked(new HashSet<>(Set.of(user)));
        habit.setUsersDisliked(new HashSet<>());

        when(habitRepo.findById(anyLong())).thenReturn(Optional.of(habit));
        when(userRepo.findById(habit.getUserId())).thenReturn(Optional.of(habitAuthor));

        habitService.dislike(1L, userVO);

        assertEquals(0, habit.getUsersLiked().size());
        assertEquals(1, habit.getUsersDisliked().size());
    }

    @Test
    void givenHabitDislikedByUser_whenLikedByUser_shouldRemoveDislikeAndAddLike() {
        UserVO userVO = getUserVO();
        User user = getUser();
        User habitAuthor = getUser().setId(2L);
        Habit habit = getHabit().setUserId(2L);
        habit.setUsersLiked(new HashSet<>());
        habit.setUsersDisliked(new HashSet<>(Set.of(user)));

        when(habitRepo.findById(anyLong())).thenReturn(Optional.of(habit));
        when(userRepo.findById(habit.getUserId())).thenReturn(Optional.of(habitAuthor));

        habitService.like(1L, userVO);

        assertEquals(0, habit.getUsersDisliked().size());
        assertEquals(1, habit.getUsersLiked().size());
    }

    @Test
    void addToFavoritesTest() {
        Habit habit = ModelUtils.getHabit();
        User user = ModelUtils.getUser();
        user.setId(2L);

        when(habitRepo.findById(any())).thenReturn(Optional.of(habit));
        when(userRepo.findByEmail(TestConst.EMAIL)).thenReturn(Optional.of(user));
        when(habitRepo.save(habit)).thenReturn(habit);

        habitService.addToFavorites(1L, TestConst.EMAIL);

        verify(habitRepo).findById(any());
        verify(userRepo).findByEmail(TestConst.EMAIL);
        verify(habitRepo).save(habit);
    }

    @Test
    void addToFavoritesThrowsExceptionWhenHabitNotFoundTest() {
        when(habitRepo.findById(any())).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> habitService.addToFavorites(1L, TestConst.EMAIL));
        verify(habitRepo).findById(any());
    }

    @Test
    void addToFavoritesThrowsExceptionWhenUserNotFoundTest() {
        when(userRepo.findById(any())).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> habitService.addToFavorites(1L, TestConst.EMAIL));
        verify(habitRepo).findById(any());
    }

    @Test
    void addToFavoritesThrowsExceptionWhenUserHasAlreadyAddedHabitToFavoritesTest() {
        User user = ModelUtils.getUser();
        Habit habit = ModelUtils.getHabit().setFollowers((Set.of(user)));

        when(habitRepo.findById(any())).thenReturn(Optional.of(habit));
        when(userRepo.findByEmail(TestConst.EMAIL)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> habitService.addToFavorites(1L, TestConst.EMAIL));

        verify(habitRepo).findById(any());
        verify(userRepo).findByEmail(TestConst.EMAIL);
    }

    @Test
    void removeFromFavoritesTest() {
        User user = ModelUtils.getUser();
        Habit habit = ModelUtils.getHabit();
        habit.getFollowers().add(user);
        when(habitRepo.findById(any())).thenReturn(Optional.of(habit));
        when(userRepo.findByEmail(TestConst.EMAIL)).thenReturn(Optional.of(user));
        when(habitRepo.save(habit)).thenReturn(habit);

        habitService.removeFromFavorites(1L, TestConst.EMAIL);

        verify(habitRepo).findById(any());
        verify(userRepo).findByEmail(TestConst.EMAIL);
        verify(habitRepo).save(habit);
    }

    @Test
    void removeFromFavoritesThrowsExceptionWhenHabitNotFoundTest() {
        when(habitRepo.findById(any())).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> habitService.removeFromFavorites(1L, TestConst.EMAIL));
        verify(habitRepo).findById(any());
    }

    @Test
    void removeFromFavoritesThrowsExceptionWhenUserNotFoundTest() {
        when(userRepo.findById(any())).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> habitService.removeFromFavorites(1L, TestConst.EMAIL));
        verify(habitRepo).findById(any());
    }

    @Test
    void removeFromFavoritesThrowsExceptionWhenHabitIsNotInFavoritesTest() {
        Habit habit = ModelUtils.getHabit();
        User user = ModelUtils.getUser();
        user.setId(2L);

        when(habitRepo.findById(any())).thenReturn(Optional.of(habit));
        when(userRepo.findByEmail(TestConst.EMAIL)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> habitService.removeFromFavorites(1L, TestConst.EMAIL));

        verify(habitRepo).findById(any());
        verify(userRepo).findByEmail(TestConst.EMAIL);
    }

    @Test
    void testGetAllFavoriteHabitsByLanguageCode() {
        UserVO userVO = new UserVO();
        userVO.setId(1L);

        Pageable pageable = PageRequest.of(0, 1);
        String languageCode = "en";

        HabitDto habitDto = getHabitDto().setHabitTranslation(getHabitTranslationDto());
        Habit habit = getHabit().setIsCustomHabit(false);
        HabitTranslation habitTranslation = getHabitTranslation().setHabit(habit);

        Page<HabitTranslation> habitTranslationPage = new PageImpl<>(List.of(habitTranslation), pageable, 1);

        when(habitTranslationRepo.findMyFavoriteHabits(pageable, 1L, languageCode))
            .thenReturn(habitTranslationPage);
        when(modelMapper.map(habitTranslation, HabitDto.class)).thenReturn(habitDto);
        when(habitTranslationRepo.getHabitTranslationByUaLanguage(habitTranslation.getHabit().getId()))
            .thenReturn(habitTranslation);
        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));

        PageableDto<HabitDto> result = habitService.getAllFavoriteHabitsByLanguageCode(userVO, pageable, languageCode);

        assertNotNull(result);
        assertEquals(1, result.getPage().size());
        verify(habitTranslationRepo, times(1)).findMyFavoriteHabits(pageable, 1L, languageCode);
    }

    @Test
    void testGetAllFavoriteHabitsByLanguageCodeEmptyPage() {
        UserVO userVO = getUserVO();
        Pageable pageable = PageRequest.of(0, 1);
        String languageCode = "en";

        Page<HabitTranslation> habitTranslationPage = Page.empty(pageable);
        when(habitTranslationRepo.findMyFavoriteHabits(pageable, 1L, languageCode)).thenReturn(habitTranslationPage);

        PageableDto<HabitDto> result = habitService.getAllFavoriteHabitsByLanguageCode(userVO, pageable, languageCode);

        assertNotNull(result);
        assertTrue(result.getPage().isEmpty());
        verify(habitTranslationRepo, times(1)).findMyFavoriteHabits(pageable, 1L, languageCode);
    }

    @Test
    void testFindAllFriendsOfUserNoNameProvidedNoInvitations() {
        Pageable pageable = mock(Pageable.class);
        Long habitId = 100L;
        UserVO userVO = getUserVO();

        List<Tuple> tuples = getUserFriendInviteHabitDtoTuple2();

        when(habitInvitationRepo.findUserFriendsWithHabitInvites(1L, "", habitId, pageable))
            .thenReturn(tuples);

        PageableDto<UserFriendHabitInviteDto> result =
            habitService.findAllFriendsOfUser(userVO, null, pageable, habitId);

        assertNotNull(result);
        assertEquals(2, result.getPage().size());
        assertFalse(result.getPage().get(0).getHasInvitation());
        assertFalse(result.getPage().get(1).getHasInvitation());
        assertFalse(result.getPage().get(0).getHasAcceptedInvitation());
        assertFalse(result.getPage().get(1).getHasAcceptedInvitation());
        assertEquals("John", result.getPage().get(0).getName());
        assertEquals("Ivan", result.getPage().get(1).getName());
        assertEquals("john@example.com", result.getPage().get(0).getEmail());
        assertEquals("ivan@example.com", result.getPage().get(1).getEmail());
        assertEquals(2L, result.getPage().get(0).getId());
        assertEquals(3L, result.getPage().get(1).getId());
        assertEquals("/image/path/john.png", result.getPage().get(0).getProfilePicturePath());
        assertEquals("/image/path/ivan.png", result.getPage().get(1).getProfilePicturePath());
        verify(habitInvitationRepo).findUserFriendsWithHabitInvites(1L, "", habitId, pageable);
    }

    @Test
    void testFindAllFriendsOfUserNameProvidedWithInvitations() {
        Pageable pageable = mock(Pageable.class);
        Long habitId = 100L;
        UserVO userVO = getUserVO();
        List<Tuple> tuples = getUserFriendInviteHabitDtoTuple1();

        when(habitInvitationRepo.findUserFriendsWithHabitInvites(1L, "Jo", habitId, pageable))
            .thenReturn(tuples);

        PageableDto<UserFriendHabitInviteDto> result =
            habitService.findAllFriendsOfUser(userVO, "Jo", pageable, habitId);

        assertNotNull(result);
        assertTrue(result.getPage().getFirst().getHasInvitation());
        assertTrue(result.getPage().getFirst().getHasAcceptedInvitation());
        assertEquals("John", result.getPage().getFirst().getName());
        assertEquals("john@example.com", result.getPage().getFirst().getEmail());
        assertEquals(2L, result.getPage().getFirst().getId());
        assertEquals("/image/path/john.png", result.getPage().getFirst().getProfilePicturePath());
        verify(habitInvitationRepo).findUserFriendsWithHabitInvites(userVO.getId(), "Jo", habitId, pageable);
    }

    @Test
    void testFindAllFriendsOfUserNoFriendsFound() {
        Pageable pageable = mock(Pageable.class);
        Long habitId = 100L;
        UserVO userVO = getUserVO();

        when(habitInvitationRepo.findUserFriendsWithHabitInvites(1L, "", habitId, pageable))
            .thenReturn(List.of());

        PageableDto<UserFriendHabitInviteDto> result =
            habitService.findAllFriendsOfUser(userVO, null, pageable, habitId);

        assertNotNull(result);
        assertTrue(result.getPage().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        verify(habitInvitationRepo).findUserFriendsWithHabitInvites(userVO.getId(), "", habitId, pageable);
    }

    @Test
    void testFindAllFriendsOfUserWithEmptyNameAndPageable() {
        Pageable pageable = mock(Pageable.class);
        Long habitId = 100L;
        UserVO userVO = getUserVO();
        when(habitInvitationRepo.findUserFriendsWithHabitInvites(1L, "", habitId, pageable))
            .thenReturn(List.of());
        PageableDto<UserFriendHabitInviteDto> result = habitService.findAllFriendsOfUser(userVO, "", pageable, habitId);

        assertNotNull(result);
        assertTrue(result.getPage().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        verify(habitInvitationRepo).findUserFriendsWithHabitInvites(userVO.getId(), "", habitId, pageable);
    }
}
