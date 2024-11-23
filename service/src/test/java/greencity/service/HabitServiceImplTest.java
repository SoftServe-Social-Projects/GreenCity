package greencity.service;

import greencity.ModelUtils;
import greencity.achievement.AchievementCalculation;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableDto;
import greencity.dto.habit.CustomHabitDtoRequest;
import greencity.dto.habit.CustomHabitDtoResponse;
import greencity.dto.habit.HabitDto;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.dto.todolistitem.CustomToDoListItemRequestDto;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.todolistitem.ToDoListItemResponseWithStatusDto;
import greencity.dto.user.UserProfilePictureDto;
import greencity.dto.user.UserVO;
import greencity.entity.CustomToDoListItem;
import greencity.entity.Habit;
import greencity.entity.HabitTranslation;
import greencity.entity.Language;
import greencity.entity.Tag;
import greencity.entity.User;
import greencity.entity.HabitAssign;
import greencity.entity.RatingPoints;
import greencity.entity.localization.ToDoListItemTranslation;
import greencity.enums.HabitAssignStatus;
import greencity.enums.Role;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoFriendWithIdException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.exception.exceptions.WrongEmailException;
import greencity.mapping.CustomToDoListResponseDtoMapper;
import greencity.mapping.HabitTranslationDtoMapper;
import greencity.rating.RatingCalculation;
import greencity.repository.HabitAssignRepo;
import greencity.repository.HabitRepo;
import greencity.repository.HabitTranslationRepo;
import greencity.repository.RatingPointsRepo;
import greencity.repository.ToDoListItemTranslationRepo;
import greencity.mapping.CustomHabitMapper;
import greencity.mapping.CustomToDoListMapper;
import greencity.mapping.HabitTranslationMapper;
import greencity.repository.CustomToDoListItemRepo;
import greencity.repository.LanguageRepo;
import greencity.repository.TagsRepo;
import greencity.repository.UserRepo;
import greencity.repository.options.HabitTranslationFilter;
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
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;

import static greencity.ModelUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

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
    private RatingCalculation ratingCalculation;
    @Mock
    private AchievementCalculation achievementCalculation;
    @Mock
    private UserNotificationServiceImpl userNotificationService;

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
    void getToDoListForHabit() {
        ToDoListItemTranslation toDoListItemTranslation = ModelUtils.getToDoListItemTranslation();
        List<ToDoListItemTranslation> toDoListItemTranslations =
            Collections.singletonList(toDoListItemTranslation);
        ToDoListItemResponseWithStatusDto toDoListItemResponseWithStatusDto =
            new ToDoListItemResponseWithStatusDto(1L, "test", "ACTIVE");
        List<ToDoListItemResponseWithStatusDto> toDoListItemResponseWithStatusDtos =
            Collections.singletonList(toDoListItemResponseWithStatusDto);
        when(modelMapper.map(toDoListItemTranslation, ToDoListItemResponseWithStatusDto.class))
            .thenReturn(toDoListItemResponseWithStatusDto);
        when(toDoListItemTranslationRepo.findToDoListByHabitIdAndByLanguageCode("en", 1L))
            .thenReturn(toDoListItemTranslations);
        assertEquals(toDoListItemResponseWithStatusDtos, habitService.getToDoListForHabit(1L, "en"));
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
        CustomToDoListItemRequestDto customToDoListItemRequestDto =
            ModelUtils.getCustomToDoListItemRequestDto();
        CustomToDoListItemResponseDto customToDoListItemResponseDto =
            ModelUtils.getCustomToDoListItemResponseDto();
        CustomToDoListItem customToDoListItem = ModelUtils.getCustomToDoListItemForServiceTest();

        CustomHabitDtoRequest addCustomHabitDtoRequest =
            ModelUtils.getAddCustomHabitDtoRequestForServiceTest();
        addCustomHabitDtoRequest.setImage(imageToEncode);
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
        when(customToDoListMapper.mapAllToList(List.of(customToDoListItemRequestDto)))
            .thenReturn(List.of(customToDoListItem));
        when(modelMapper.map(habit, CustomHabitDtoResponse.class)).thenReturn(addCustomHabitDtoResponse);
        when(customToDoListResponseDtoMapper.mapAllToList(List.of(customToDoListItem)))
            .thenReturn(List.of(customToDoListItemResponseDto));
        when(habitTranslationRepo.findAllByHabit(habit)).thenReturn(habitTranslationList);
        when(habitTranslationDtoMapper.mapAllToList(habitTranslationList)).thenReturn(habitTranslationDtoList);

        when(fileService.upload(image)).thenReturn(imageToEncode);

        assertEquals(addCustomHabitDtoResponse,
            habitService.addCustomHabit(addCustomHabitDtoRequest, null, "taras@gmail.com"));

        verify(userRepo).findByEmail(user.getEmail());
        verify(habitRepo).save(customHabitMapper.convert(addCustomHabitDtoRequest));
        verify(customHabitMapper, times(3)).convert(addCustomHabitDtoRequest);
        verify(tagsRepo).findById(20L);
        verify(habitTranslationMapper, times(2)).mapAllToList(List.of(habitTranslationDto));
        verify(languageRepo, times(2)).findByCode(anyString());
        verify(customToDoListItemRepo).findAllByUserIdAndHabitId(1L, 1L);
        verify(customToDoListMapper).mapAllToList(anyList());
        verify(modelMapper).map(habit, CustomHabitDtoResponse.class);
        verify(customToDoListResponseDtoMapper).mapAllToList(List.of(customToDoListItem));
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
        CustomToDoListItemResponseDto customToDoListItemResponseDto =
            ModelUtils.getCustomToDoListItemResponseDtoForServiceTest();
        CustomToDoListItemRequestDto customToDoListItemRequestDto =
            ModelUtils.getCustomToDoListItemRequestDto();
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
        when(customToDoListMapper.mapAllToList(List.of(customToDoListItemRequestDto)))
            .thenReturn(List.of(customToDoListItem));
        when(modelMapper.map(habit, CustomHabitDtoResponse.class)).thenReturn(addCustomHabitDtoResponse);
        when(customToDoListResponseDtoMapper.mapAllToList(List.of(customToDoListItem)))
            .thenReturn(List.of(customToDoListItemResponseDto));
        when(habitTranslationRepo.findAllByHabit(habit)).thenReturn(habitTranslationList);
        when(habitTranslationDtoMapper.mapAllToList(habitTranslationList)).thenReturn(habitTranslationDtoList);

        when(fileService.upload(image)).thenReturn(imageToEncode);

        assertEquals(addCustomHabitDtoResponse,
            habitService.addCustomHabit(addCustomHabitDtoRequest, image, "taras@gmail.com"));

        verify(userRepo).findByEmail(user.getEmail());
        verify(habitRepo).save(customHabitMapper.convert(addCustomHabitDtoRequest));
        verify(customHabitMapper, times(3)).convert(addCustomHabitDtoRequest);
        verify(tagsRepo).findById(20L);
        verify(habitTranslationMapper, times(2)).mapAllToList(List.of(habitTranslationDto));
        verify(languageRepo, times(2)).findByCode(anyString());
        verify(customToDoListItemRepo).findAllByUserIdAndHabitId(1L, 1L);
        verify(customToDoListMapper).mapAllToList(anyList());
        verify(modelMapper).map(habit, CustomHabitDtoResponse.class);
        verify(customToDoListResponseDtoMapper).mapAllToList(List.of(customToDoListItem));
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
        CustomToDoListItemResponseDto customToDoListItemResponseDto =
            ModelUtils.getCustomToDoListItemResponseDtoForServiceTest();
        CustomToDoListItemRequestDto customToDoListItemRequestDto =
            ModelUtils.getCustomToDoListItemRequestDto();
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
        when(customToDoListMapper.mapAllToList(List.of(customToDoListItemRequestDto)))
            .thenReturn(List.of(customToDoListItem));
        when(modelMapper.map(habit, CustomHabitDtoResponse.class)).thenReturn(addCustomHabitDtoResponse);
        when(customToDoListResponseDtoMapper.mapAllToList(List.of(customToDoListItem)))
            .thenReturn(List.of(customToDoListItemResponseDto));
        when(habitTranslationRepo.findAllByHabit(habit)).thenReturn(habitTranslationList);
        when(habitTranslationDtoMapper.mapAllToList(habitTranslationList)).thenReturn(habitTranslationDtoList);

        when(fileService.upload(image)).thenReturn(imageToEncode);

        assertEquals(addCustomHabitDtoResponse,
            habitService.addCustomHabit(addCustomHabitDtoRequest, null, "taras@gmail.com"));

        verify(userRepo).findByEmail(user.getEmail());
        verify(habitRepo).save(customHabitMapper.convert(addCustomHabitDtoRequest));
        verify(customHabitMapper, times(3)).convert(addCustomHabitDtoRequest);
        verify(tagsRepo).findById(20L);
        verify(habitTranslationMapper, times(2)).mapAllToList(List.of(habitTranslationDto));
        verify(languageRepo, times(2)).findByCode(anyString());
        verify(customToDoListItemRepo).findAllByUserIdAndHabitId(1L, 1L);
        verify(customToDoListMapper).mapAllToList(anyList());
        verify(modelMapper).map(habit, CustomHabitDtoResponse.class);
        verify(customToDoListResponseDtoMapper).mapAllToList(List.of(customToDoListItem));
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
        when(habitTranslationMapper.mapAllToList(List.of(habitTranslationDto)))
            .thenReturn(List.of(habitTranslation));
        when(languageRepo.findByCode("ua")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> habitService.addCustomHabit(addCustomHabitDtoRequest, image, "taras@gmail.com"));

        verify(userRepo).findByEmail(user.getEmail());
        verify(habitRepo).save(customHabitMapper.convert(addCustomHabitDtoRequest));
        verify(customHabitMapper, times(3)).convert(addCustomHabitDtoRequest);
        verify(tagsRepo).findById(20L);
        verify(habitTranslationMapper).mapAllToList(addCustomHabitDtoRequest.getHabitTranslations());
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
        when(habitTranslationMapper.mapAllToList(List.of(habitTranslationDto)))
            .thenReturn(List.of(habitTranslationUa));
        when(languageRepo.findByCode("ua")).thenReturn(Optional.of(languageUa));
        when(languageRepo.findByCode("en")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> habitService.addCustomHabit(addCustomHabitDtoRequest, image, "taras@gmail.com"));

        verify(userRepo).findByEmail(user.getEmail());
        verify(habitRepo).save(customHabitMapper.convert(addCustomHabitDtoRequest));
        verify(customHabitMapper, times(3)).convert(addCustomHabitDtoRequest);
        verify(tagsRepo).findById(20L);

        verify(habitTranslationMapper, times(2)).mapAllToList(addCustomHabitDtoRequest.getHabitTranslations());
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
        Long habitId = 1L;
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
        when(habitRepo.existsById(habitId)).thenReturn(true);
        when(userRepo.getFriendsAssignedToHabit(userId, habitId)).thenReturn(List.of(friend));
        when(modelMapper.map(friend, UserProfilePictureDto.class)).thenReturn(friendProfilePicture);

        List<UserProfilePictureDto> list = habitService.getFriendsAssignedToHabitProfilePictures(habitId, userId);
        assertFalse(list.isEmpty());
        assertEquals(friendProfilePicture, list.getFirst());

        verify(userRepo).existsById(userId);
        verify(habitRepo).existsById(habitId);
        verify(userRepo).getFriendsAssignedToHabit(userId, habitId);
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
        Long habitId = 1L;
        Long userId = 2L;

        when(userRepo.existsById(userId)).thenReturn(true);
        when(habitRepo.existsById(userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> habitService.getFriendsAssignedToHabitProfilePictures(habitId, userId));

        assertEquals(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId, exception.getMessage());

        verify(userRepo).existsById(userId);
        verify(habitRepo).existsById(habitId);
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
        CustomToDoListItemResponseDto customToDoListItemResponseDto =
            ModelUtils.getCustomToDoListItemResponseDtoForServiceTest();
        CustomToDoListItemRequestDto customToDoListItemRequestDto =
            ModelUtils.getCustomToDoListItemRequestDto();
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
        when(customToDoListMapper.mapAllToList(List.of(customToDoListItemRequestDto)))
            .thenReturn(List.of(customToDoListItem));
        when(modelMapper.map(habit, CustomHabitDtoResponse.class)).thenReturn(customHabitDtoResponse);
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
        verify(customToDoListItemRepo, times(2)).findAllByUserIdAndHabitId(anyLong(), anyLong());
        verify(customToDoListMapper).mapAllToList(anyList());
        verify(modelMapper).map(habit, CustomHabitDtoResponse.class);
        verify(customToDoListResponseDtoMapper).mapAllToList(List.of(customToDoListItem));
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

        verify(customToDoListItemRepo, times(2)).findAllByUserIdAndHabitId(2L, 1L);
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
        Habit habit = getHabit().setUserId(user.getId());
        RatingPoints ratingPoints = RatingPoints.builder().id(1L).name("LIKE_COMMENT_OR_REPLY").points(1).build();

        when(ratingPointsRepo.findByNameOrThrow("LIKE_COMMENT_OR_REPLY")).thenReturn(ratingPoints);
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));

        habitService.like(habit.getId(), userVO);

        assertTrue(habit.getUsersLiked().stream().anyMatch(u -> u.getId().equals(userVO.getId())));

        verify(modelMapper).map(userVO, User.class);
        verify(habitRepo).findById(habit.getId());
        verify(userRepo).findById(user.getId());
    }

    @Test
    void removeLikeTest() {
        UserVO userVO = getUserVO();
        User user = getUser();
        Habit habit = getHabit().setUserId(user.getId());
        habit.getUsersLiked().add(user);
        RatingPoints ratingPoints = RatingPoints.builder().id(1L).name("UNDO_LIKE_COMMENT_OR_REPLY").points(-1).build();

        when(ratingPointsRepo.findByNameOrThrow("UNDO_LIKE_COMMENT_OR_REPLY")).thenReturn(ratingPoints);
        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));

        habitService.like(habit.getId(), userVO);
        assertFalse(habit.getUsersLiked().stream().anyMatch(u -> u.getId().equals(userVO.getId())));

        verify(habitRepo).findById(habit.getId());
        verify(userRepo).findById(user.getId());
    }

    @Test
    void removeLikeRemoveIfTest() {
        User user = getUser();
        Habit habit = getHabit();
        habit.getUsersLiked().add(user);

        UserVO userVO = getUserVO();
        userVO.setName("New Name");

        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
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
        habit.setUserId(user.getId());

        when(habitRepo.findById(habit.getId())).thenReturn(Optional.of(habit));
        when(userRepo.findById(habit.getUserId())).thenReturn(Optional.empty());

        NotFoundException exception =
            assertThrows(NotFoundException.class, () -> habitService.like(habitId, userVO));
        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + user.getId(), exception.getMessage());

        verify(habitRepo).findById(habit.getId());
        verify(userRepo).findById(habit.getUserId());
    }
}
