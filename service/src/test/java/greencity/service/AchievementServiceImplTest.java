package greencity.service;

import greencity.ModelUtils;

import static greencity.ModelUtils.getAchievement;
import static greencity.ModelUtils.getAchievementCategory;
import static greencity.ModelUtils.getAchievementCategoryVO;
import static greencity.ModelUtils.getAchievementVO;
import static greencity.ModelUtils.getHabitAssign;
import static greencity.ModelUtils.getHabitTranslation;
import static greencity.ModelUtils.getHabitTranslationUa;
import static greencity.ModelUtils.getHabitVO;
import static greencity.ModelUtils.getUserAction;
import static greencity.ModelUtils.getUserVO;
import static greencity.ModelUtils.getUserAchievement;
import static greencity.ModelUtils.getActionDto;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.achievement.AchievementManagementDto;
import greencity.dto.achievement.AchievementPostDto;
import greencity.dto.achievement.AchievementVO;
import greencity.dto.achievementcategory.AchievementCategoryVO;
import greencity.dto.habit.HabitVO;
import greencity.entity.Achievement;
import greencity.entity.AchievementCategory;
import static greencity.enums.AchievementStatus.ACHIEVED;
import static greencity.enums.AchievementStatus.UNACHIEVED;

import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.UserAchievement;
import greencity.entity.UserAction;
import greencity.exception.exceptions.NotDeletedException;
import greencity.exception.exceptions.NotUpdatedException;
import greencity.repository.AchievementCategoryRepo;
import greencity.repository.AchievementRepo;
import greencity.repository.HabitAssignRepo;
import greencity.repository.HabitTranslationRepo;
import greencity.repository.UserAchievementRepo;
import greencity.repository.UserActionRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.modelmapper.ModelMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AchievementServiceImplTest {
    @Mock
    private AchievementRepo achievementRepo;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private UserAchievementRepo userAchievementRepo;
    @InjectMocks
    private AchievementServiceImpl achievementService;
    @Mock
    private UserService userService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private AchievementCategoryRepo achievementCategoryRepo;
    @Mock
    private UserActionRepo userActionRepo;
    @Mock
    private HabitAssignRepo habitAssignRepo;
    @Mock
    private HabitTranslationRepo habitTranslationRepo;
    @Mock
    private RatingPointsService ratingPointsService;

    @Test
    void findAllWithEmptyListTest() {
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        when(userAchievementRepo.getUserAchievementByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(achievementRepo.searchAchievementsUnAchieved(anyLong())).thenReturn(Collections.emptyList());
        when(achievementCategoryRepo.findByName("HABIT")).thenReturn(Optional.of(getAchievementCategory()));
        when(habitAssignRepo.findAllInProgressHabitAssignsRelatedToUser(anyLong())).thenReturn(Collections.emptyList());
        when(achievementRepo.findAllByAchievementCategoryId(anyLong())).thenReturn(Collections.emptyList());
        when(userActionRepo.findAllByUserId(anyLong())).thenReturn(Collections.emptyList());
        List<AchievementVO> findAllResult = achievementService.findAllByTypeAndCategory("email@gmail.com", null, null);
        assertTrue(findAllResult.isEmpty());
        verify(userService).findByEmail("email@gmail.com");
        verify(userAchievementRepo).getUserAchievementByUserId(anyLong());
        verify(achievementRepo).searchAchievementsUnAchieved(anyLong());
        verify(achievementCategoryRepo).findByName("HABIT");
        verify(habitAssignRepo).findAllInProgressHabitAssignsRelatedToUser(anyLong());
        verify(achievementRepo).findAllByAchievementCategoryId(anyLong());
        verify(userActionRepo).findAllByUserId(anyLong());
    }

    @Test
    void findAllWithWrongAchievementCategoryIdListTest() {
        Long invalidId = 0L;
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        when(achievementCategoryRepo.findById(invalidId)).thenThrow(WrongThreadException.class);
        assertThrows(WrongThreadException.class,
            () -> achievementService.findAllByTypeAndCategory("email@gmail.com", null, invalidId));
    }

    @Test
    void findAllWithOneValueInRepoTest() {
        Achievement achievement = ModelUtils.getAchievement();
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        when(userAchievementRepo.getUserAchievementByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(achievementRepo.searchAchievementsUnAchieved(anyLong()))
            .thenReturn(Collections.singletonList(achievement));
        when(achievementCategoryRepo.findByName("HABIT")).thenReturn(Optional.of(getAchievementCategory()));
        when(habitAssignRepo.findAllInProgressHabitAssignsRelatedToUser(anyLong())).thenReturn(Collections.emptyList());
        when(achievementRepo.findAllByAchievementCategoryId(anyLong())).thenReturn(Collections.emptyList());
        when(modelMapper.map(achievement, AchievementVO.class)).thenReturn(ModelUtils.getAchievementVO());
        when(userActionRepo.findAllByUserId(anyLong())).thenReturn(Collections.emptyList());
        List<AchievementVO> findAllResult = achievementService.findAllByTypeAndCategory("email@gmail.com", null, null);
        assertEquals(1L, (long) findAllResult.getFirst().getId());
        verify(userService).findByEmail("email@gmail.com");
        verify(userAchievementRepo).getUserAchievementByUserId(anyLong());
        verify(achievementRepo).searchAchievementsUnAchieved(anyLong());
        verify(achievementCategoryRepo).findByName("HABIT");
        verify(habitAssignRepo).findAllInProgressHabitAssignsRelatedToUser(anyLong());
        verify(achievementRepo).findAllByAchievementCategoryId(anyLong());
        verify(modelMapper).map(achievement, AchievementVO.class);
        verify(userActionRepo).findAllByUserId(anyLong());
    }

    @Test
    void findAllByPageableTest() {
        Pageable pageable = PageRequest.of(0, 2);
        Achievement achievement = ModelUtils.getAchievement();
        AchievementVO achievementVO = ModelUtils.getAchievementVO();
        Page<Achievement> pages = new PageImpl<>(Collections.singletonList(achievement), pageable, 10);
        when(achievementRepo.findAll(pageable)).thenReturn(pages);
        when(modelMapper.map(achievement, AchievementVO.class)).thenReturn(achievementVO);
        PageableAdvancedDto<AchievementVO> pageableAdvancedDto = achievementService.findAll(pageable);
        assertEquals(10, pageableAdvancedDto.getTotalElements());
    }

    @Test
    void findAllACHIEVEDInRepoTest() {
        Achievement achievement = getAchievement();
        AchievementCategory achievementCategory = getAchievementCategory();
        AchievementVO achievementVO = getAchievementVO();
        AchievementCategoryVO achievementCategoryVO = getAchievementCategoryVO();
        achievementVO.setAchievementCategory(achievementCategoryVO);
        achievement.setAchievementCategory(achievementCategory);
        List<UserAction> userActions = List.of(getUserAction());
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        when(userAchievementRepo.getUserAchievementByUserId(anyLong()))
            .thenReturn(List.of(ModelUtils.getUserAchievement()));
        when(achievementRepo.findById(anyLong())).thenReturn(Optional.of(achievement));
        when(habitTranslationRepo.getHabitTranslationByUaLanguage(anyLong())).thenReturn(getHabitTranslationUa());
        when(habitTranslationRepo.getHabitTranslationByEnLanguage(anyLong())).thenReturn(getHabitTranslation());
        when(modelMapper.map(achievement, AchievementVO.class))
            .thenReturn(achievementVO);
        when(userActionRepo.findAllByUserId(anyLong())).thenReturn(userActions);
        List<AchievementVO> findAllResult =
            achievementService.findAllByTypeAndCategory("email@gmail.com", ACHIEVED, null);
        assertEquals(1L, (long) findAllResult.getFirst().getId());
        verify(userService).findByEmail("email@gmail.com");
        verify(userAchievementRepo).getUserAchievementByUserId(anyLong());
        verify(achievementRepo).findById(anyLong());
        verify(habitTranslationRepo).getHabitTranslationByUaLanguage(anyLong());
        verify(habitTranslationRepo).getHabitTranslationByEnLanguage(anyLong());
        verify(modelMapper).map(achievement, AchievementVO.class);
        verify(userActionRepo).findAllByUserId(anyLong());
    }

    @Test
    void findAllACHIEVEDWithCategoryIdInRepoTest() {
        Achievement achievement = getAchievement();
        AchievementCategory achievementCategory = getAchievementCategory();
        AchievementVO achievementVO = getAchievementVO();
        AchievementCategoryVO achievementCategoryVO = getAchievementCategoryVO();
        achievementVO.setAchievementCategory(achievementCategoryVO);
        achievement.setAchievementCategory(achievementCategory);
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        when(achievementCategoryRepo.findById(anyLong())).thenReturn(Optional.of(achievementCategory));
        when(userAchievementRepo.findAllByUserIdAndAchievement_AchievementCategoryId(anyLong(), anyLong()))
            .thenReturn(List.of(ModelUtils.getUserAchievement()));
        when(achievementRepo.findById(anyLong())).thenReturn(Optional.of(achievement));
        when(modelMapper.map(achievement, AchievementVO.class))
            .thenReturn(achievementVO);
        when(habitTranslationRepo.getHabitTranslationByUaLanguage(anyLong())).thenReturn(getHabitTranslationUa());
        when(habitTranslationRepo.getHabitTranslationByEnLanguage(anyLong())).thenReturn(getHabitTranslation());
        when(achievementCategoryRepo.findByName("HABIT")).thenReturn(Optional.of(achievementCategory));
        List<AchievementVO> findAllResult =
            achievementService.findAllByTypeAndCategory("email@gmail.com", ACHIEVED, achievementCategory.getId());
        assertEquals(1L, (long) findAllResult.getFirst().getId());
        verify(userService).findByEmail("email@gmail.com");
        verify(achievementCategoryRepo).findById(anyLong());
        verify(userAchievementRepo).findAllByUserIdAndAchievement_AchievementCategoryId(anyLong(), anyLong());
        verify(achievementRepo).findById(anyLong());
        verify(modelMapper).map(achievement, AchievementVO.class);
        verify(habitTranslationRepo).getHabitTranslationByUaLanguage(anyLong());
        verify(habitTranslationRepo).getHabitTranslationByEnLanguage(anyLong());
        verify(achievementCategoryRepo).findByName("HABIT");
    }

    @Test
    void findAllUNACHIEVEDInRepoTest() {
        AchievementCategoryVO achievementCategoryVO = getAchievementCategoryVO();
        AchievementCategory habitCategory = getAchievementCategory();
        habitCategory.setId(2L);
        AchievementCategoryVO habitCategoryVO = getAchievementCategoryVO();
        habitCategoryVO.setId(2L);
        Achievement achievement = getAchievement();
        AchievementVO achievementVO = getAchievementVO();
        achievementVO.setAchievementCategory(achievementCategoryVO);
        Achievement habitAchievement = getAchievement();
        habitAchievement.setId(2L);
        AchievementVO habitAchievementVO = getAchievementVO();
        habitAchievementVO.setId(2L);
        habitAchievementVO.setAchievementCategory(new AchievementCategoryVO(5L, "name"));
        List<Achievement> achievements = List.of(achievement, habitAchievement);
        UserAction userAction = getUserAction();
        userAction.setHabit(null);
        UserAction habitUserAction = getUserAction();
        habitUserAction.setAchievementCategory(habitCategory);
        habitUserAction.setCount(5);
        HabitAssign habitAssign = getHabitAssign();
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        when(achievementRepo.searchAchievementsUnAchieved(anyLong()))
            .thenReturn(achievements);
        when(modelMapper.map(achievement, AchievementVO.class))
            .thenReturn(achievementVO);
        when(modelMapper.map(habitAchievement, AchievementVO.class))
            .thenReturn(habitAchievementVO);
        when(achievementCategoryRepo.findByName("HABIT")).thenReturn(Optional.of(habitCategory));
        when(habitAssignRepo.findAllInProgressHabitAssignsRelatedToUser(anyLong())).thenReturn(List.of(habitAssign));
        when(achievementRepo.findAllByAchievementCategoryId(habitCategory.getId()))
            .thenReturn(List.of(habitAchievement));
        when(achievementRepo.findByAchievementCategoryIdAndCondition(anyLong(), anyInt()))
            .thenReturn(Optional.of(habitAchievement));
        when(habitTranslationRepo.getHabitTranslationByUaLanguage(habitAssign.getHabit().getId()))
            .thenReturn(getHabitTranslationUa());
        when(habitTranslationRepo.getHabitTranslationByEnLanguage(habitAssign.getHabit().getId()))
            .thenReturn(getHabitTranslation());
        when(modelMapper.map(habitAssign.getHabit(), HabitVO.class)).thenReturn(getHabitVO());
        when(userActionRepo.findAllByUserId(anyLong())).thenReturn(List.of(userAction, habitUserAction));
        List<AchievementVO> findAllResult =
            achievementService.findAllByTypeAndCategory("email@gmail.com", UNACHIEVED, null);
        assertEquals(2, findAllResult.size());
        assertEquals(1L, (long) findAllResult.getFirst().getId());
        assertEquals(2L, (long) findAllResult.get(1).getId());
        assertEquals(userAction.getCount(), findAllResult.getFirst().getProgress());
        assertEquals(habitUserAction.getCount(), findAllResult.get(1).getProgress());
        verify(userService).findByEmail("email@gmail.com");
        verify(achievementRepo).searchAchievementsUnAchieved(anyLong());
        verify(modelMapper).map(achievement, AchievementVO.class);
        verify(modelMapper, times(2)).map(habitAchievement, AchievementVO.class);
        verify(achievementCategoryRepo).findByName("HABIT");
        verify(habitAssignRepo).findAllInProgressHabitAssignsRelatedToUser(anyLong());
        verify(achievementRepo).findAllByAchievementCategoryId(anyLong());
        verify(achievementRepo).findByAchievementCategoryIdAndCondition(anyLong(), anyInt());
        verify(habitTranslationRepo).getHabitTranslationByUaLanguage(habitAssign.getHabit().getId());
        verify(habitTranslationRepo).getHabitTranslationByEnLanguage(habitAssign.getHabit().getId());
        verify(modelMapper).map(habitAssign.getHabit(), HabitVO.class);
        verify(userActionRepo).findAllByUserId(anyLong());
    }

    @Test
    void findAllUNACHIEVEDWithCategoryIdInRepoTest() {
        AchievementCategory achievementCategory = getAchievementCategory();
        AchievementCategory habitAchievement = getAchievementCategory();
        habitAchievement.setId(2L);
        UserAction userAction = getUserAction();
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        when(achievementCategoryRepo.findById(anyLong())).thenReturn(Optional.of(achievementCategory));
        when(achievementRepo.searchAchievementsUnAchievedByCategory(anyLong(), anyLong()))
            .thenReturn(List.of(ModelUtils.getAchievement()));
        when(modelMapper.map(ModelUtils.getAchievement(), AchievementVO.class))
            .thenReturn(ModelUtils.getAchievementVO());
        when(achievementCategoryRepo.findByName("HABIT")).thenReturn(Optional.of(habitAchievement));
        when(userActionRepo.findByUserIdAndAchievementCategoryId(anyLong(), eq(achievementCategory.getId())))
            .thenReturn(userAction);
        List<AchievementVO> findAllResult =
            achievementService.findAllByTypeAndCategory("email@gmail.com", UNACHIEVED, achievementCategory.getId());
        assertEquals(1L, (long) findAllResult.getFirst().getId());
        assertEquals(userAction.getCount(), findAllResult.getFirst().getProgress());
        verify(userService).findByEmail("email@gmail.com");
        verify(achievementCategoryRepo).findById(anyLong());
        verify(achievementRepo).searchAchievementsUnAchievedByCategory(anyLong(), anyLong());
        verify(modelMapper).map(getAchievement(), AchievementVO.class);
        verify(achievementCategoryRepo, times(2)).findByName("HABIT");
        verify(userActionRepo).findByUserIdAndAchievementCategoryId(anyLong(), eq(achievementCategory.getId()));
    }

    @Test
    void findAllUNACHIEVEDWithCategoryIdInRepoWithoutUserActionTest() {
        AchievementCategory achievementCategory = getAchievementCategory();
        AchievementCategory habitAchievement = getAchievementCategory();
        habitAchievement.setId(2L);
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        when(achievementCategoryRepo.findById(anyLong())).thenReturn(Optional.of(achievementCategory));
        when(achievementRepo.searchAchievementsUnAchievedByCategory(anyLong(), anyLong()))
            .thenReturn(List.of(ModelUtils.getAchievement()));
        when(modelMapper.map(ModelUtils.getAchievement(), AchievementVO.class))
            .thenReturn(ModelUtils.getAchievementVO());
        when(achievementCategoryRepo.findByName("HABIT")).thenReturn(Optional.of(habitAchievement));
        when(userActionRepo.findByUserIdAndAchievementCategoryId(anyLong(), eq(achievementCategory.getId())))
            .thenReturn(null);
        List<AchievementVO> findAllResult =
            achievementService.findAllByTypeAndCategory("email@gmail.com", UNACHIEVED, achievementCategory.getId());
        assertEquals(1L, (long) findAllResult.getFirst().getId());
        assertEquals(0, findAllResult.getFirst().getProgress());
        verify(userService).findByEmail("email@gmail.com");
        verify(achievementCategoryRepo).findById(anyLong());
        verify(achievementRepo).searchAchievementsUnAchievedByCategory(anyLong(), anyLong());
        verify(modelMapper).map(getAchievement(), AchievementVO.class);
        verify(achievementCategoryRepo, times(2)).findByName("HABIT");
        verify(userActionRepo).findByUserIdAndAchievementCategoryId(anyLong(), eq(achievementCategory.getId()));
    }

    @Test
    void findAllUNACHIEVEDWithHabitCategoryTwoValueInRepoTest() {
        AchievementCategory achievementCategory = getAchievementCategory();
        Achievement achievement = ModelUtils.getAchievement();
        achievement.setCondition(2);
        AchievementVO achievementVO = ModelUtils.getAchievementVO();
        achievementVO.setHabit(ModelUtils.getHabitVO());
        achievementVO.setCondition(2);
        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        habitAssign.setDuration(achievement.getCondition());
        Habit habit = habitAssign.getHabit();
        HabitAssign habitAssign2 = ModelUtils.getHabitAssign();
        habitAssign2.setDuration(achievement.getCondition() - 1);
        Habit habit2 = ModelUtils.getHabit();
        habit2.setId(2L);
        habitAssign2.setHabit(habit2);
        HabitVO habitVO = ModelUtils.getHabitVO();
        HabitVO habitVO2 = ModelUtils.getHabitVO();
        habitVO2.setId(2L);
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        when(achievementCategoryRepo.findById(anyLong())).thenReturn(Optional.of(achievementCategory));
        when(achievementRepo.searchAchievementsUnAchievedByCategory(anyLong(), anyLong()))
            .thenReturn(List.of(achievement));
        when(modelMapper.map(achievement, AchievementVO.class))
            .thenReturn(achievementVO);
        when(achievementCategoryRepo.findByName("HABIT")).thenReturn(Optional.of(achievementCategory));
        when(habitAssignRepo.findAllInProgressHabitAssignsRelatedToUser(anyLong()))
            .thenReturn(List.of(habitAssign, habitAssign2));
        when(achievementRepo.findAllByAchievementCategoryId(anyLong())).thenReturn(List.of(achievement));
        when(achievementRepo.findByAchievementCategoryIdAndCondition(anyLong(), anyInt()))
            .thenReturn(Optional.of(achievement));
        when(habitTranslationRepo.getHabitTranslationByUaLanguage(anyLong())).thenReturn(getHabitTranslationUa());
        when(habitTranslationRepo.getHabitTranslationByEnLanguage(anyLong())).thenReturn(getHabitTranslation());
        when(modelMapper.map(habit, HabitVO.class)).thenReturn(habitVO);
        when(modelMapper.map(habit2, HabitVO.class)).thenReturn(habitVO2);
        when(userActionRepo.findByUserIdAndAchievementCategoryIdAndHabitId(anyLong(), anyLong(), eq(habit.getId())))
            .thenReturn(ModelUtils.getUserAction());
        when(userActionRepo.findByUserIdAndAchievementCategoryIdAndHabitId(anyLong(), anyLong(), eq(habit2.getId())))
            .thenReturn(null);
        List<AchievementVO> findAllResult =
            achievementService.findAllByTypeAndCategory("email@gmail.com", UNACHIEVED, achievementCategory.getId());
        assertEquals(2, findAllResult.size());
        assertEquals(1L, (long) findAllResult.getFirst().getId());
        assertEquals(1L, (long) findAllResult.get(1).getId());
        verify(userService).findByEmail("email@gmail.com");
        verify(achievementCategoryRepo).findById(anyLong());
        verify(achievementRepo).searchAchievementsUnAchievedByCategory(anyLong(), anyLong());
        verify(modelMapper, times(2)).map(achievement, AchievementVO.class);
        verify(achievementCategoryRepo, times(2)).findByName("HABIT");
        verify(habitAssignRepo).findAllInProgressHabitAssignsRelatedToUser(anyLong());
        verify(achievementRepo).findAllByAchievementCategoryId(anyLong());
        verify(achievementRepo).findByAchievementCategoryIdAndCondition(anyLong(), anyInt());
        verify(habitTranslationRepo, times(2)).getHabitTranslationByUaLanguage(anyLong());
        verify(habitTranslationRepo, times(2)).getHabitTranslationByEnLanguage(anyLong());
        verify(modelMapper).map(habit, HabitVO.class);
        verify(modelMapper).map(habit2, HabitVO.class);
        verify(userActionRepo).findByUserIdAndAchievementCategoryIdAndHabitId(anyLong(), anyLong(), eq(habit.getId()));
        verify(userActionRepo).findByUserIdAndAchievementCategoryIdAndHabitId(anyLong(), anyLong(), eq(habit2.getId()));
    }

    @Test
    void findAllAnyStatusWithCategoryIdTest() {
        AchievementCategory achievementCategory = getAchievementCategory();
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        when(achievementCategoryRepo.findById(anyLong())).thenReturn(Optional.of(achievementCategory));
        when(userAchievementRepo.findAllByUserIdAndAchievement_AchievementCategoryId(anyLong(), anyLong()))
            .thenReturn(Collections.emptyList());
        when(achievementRepo.searchAchievementsUnAchievedByCategory(anyLong(), anyLong()))
            .thenReturn(List.of(ModelUtils.getAchievement()));
        when(modelMapper.map(ModelUtils.getAchievement(), AchievementVO.class))
            .thenReturn(ModelUtils.getAchievementVO());
        when(achievementCategoryRepo.findByName("HABIT")).thenReturn(Optional.of(getAchievementCategory()));
        when(habitAssignRepo.findAllInProgressHabitAssignsRelatedToUser(anyLong())).thenReturn(Collections.emptyList());
        when(achievementRepo.findAllByAchievementCategoryId(anyLong())).thenReturn(List.of(getAchievement()));
        List<AchievementVO> findAllResult =
            achievementService.findAllByTypeAndCategory("email@gmail.com", null, achievementCategory.getId());
        assertEquals(1L, (long) findAllResult.getFirst().getId());
        verify(userService).findByEmail("email@gmail.com");
        verify(achievementCategoryRepo).findById(anyLong());
        verify(userAchievementRepo).findAllByUserIdAndAchievement_AchievementCategoryId(anyLong(), anyLong());
        verify(achievementRepo).searchAchievementsUnAchievedByCategory(anyLong(), anyLong());
        verify(modelMapper).map(getAchievement(), AchievementVO.class);
        verify(achievementCategoryRepo, times(2)).findByName("HABIT");
        verify(habitAssignRepo).findAllInProgressHabitAssignsRelatedToUser(anyLong());
        verify(achievementRepo).findAllByAchievementCategoryId(anyLong());
    }

    @Test
    void countAllAchievementsWithEmptyListTest() {
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        when(userAchievementRepo.getUserAchievementByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(achievementRepo.searchAchievementsUnAchieved(anyLong())).thenReturn(Collections.emptyList());
        when(achievementCategoryRepo.findByName("HABIT")).thenReturn(Optional.of(getAchievementCategory()));
        when(habitAssignRepo.findAllInProgressHabitAssignsRelatedToUser(anyLong())).thenReturn(Collections.emptyList());
        when(achievementRepo.findAllByAchievementCategoryId(anyLong())).thenReturn(Collections.emptyList());
        Integer result = achievementService.findAchievementCountByTypeAndCategory("email@gmail.com", null, null);
        assertEquals(0, result);
        verify(userService).findByEmail("email@gmail.com");
        verify(userAchievementRepo).getUserAchievementByUserId(anyLong());
        verify(achievementRepo).searchAchievementsUnAchieved(anyLong());
        verify(achievementCategoryRepo).findByName("HABIT");
        verify(habitAssignRepo).findAllInProgressHabitAssignsRelatedToUser(anyLong());
        verify(achievementRepo).findAllByAchievementCategoryId(anyLong());
    }

    @Test
    void countAllAchievementsWithOneValueInRepoTest() {
        Achievement achievement = ModelUtils.getAchievement();
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        when(userAchievementRepo.getUserAchievementByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(achievementRepo.searchAchievementsUnAchieved(anyLong()))
            .thenReturn(Collections.singletonList(achievement));
        when(achievementCategoryRepo.findByName("HABIT")).thenReturn(Optional.of(getAchievementCategory()));
        when(habitAssignRepo.findAllInProgressHabitAssignsRelatedToUser(anyLong())).thenReturn(Collections.emptyList());
        when(achievementRepo.findAllByAchievementCategoryId(anyLong())).thenReturn(Collections.emptyList());
        when(modelMapper.map(achievement, AchievementVO.class)).thenReturn(ModelUtils.getAchievementVO());
        Integer result = achievementService.findAchievementCountByTypeAndCategory("email@gmail.com", null, null);
        assertEquals(1, result);
        verify(userService).findByEmail("email@gmail.com");
        verify(userAchievementRepo).getUserAchievementByUserId(anyLong());
        verify(achievementRepo).searchAchievementsUnAchieved(anyLong());
        verify(achievementCategoryRepo).findByName("HABIT");
        verify(habitAssignRepo).findAllInProgressHabitAssignsRelatedToUser(anyLong());
        verify(achievementRepo).findAllByAchievementCategoryId(anyLong());
        verify(modelMapper).map(achievement, AchievementVO.class);
    }

    @Test
    void countAchievedAchievementsWithCategoryIdTest() {
        Achievement achievement = ModelUtils.getAchievement();
        AchievementVO achievementVO = ModelUtils.getAchievementVO();
        UserAchievement userAchievement = ModelUtils.getUserAchievement();
        userAchievement.setHabit(null);
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        when(achievementCategoryRepo.findById(anyLong())).thenReturn(Optional.of(getAchievementCategory()));
        when(userAchievementRepo.findAllByUserIdAndAchievement_AchievementCategoryId(anyLong(), anyLong()))
            .thenReturn(List.of(userAchievement));
        when(achievementRepo.findById(anyLong())).thenReturn(Optional.of(achievement));
        when(modelMapper.map(achievement, AchievementVO.class)).thenReturn(achievementVO);
        Integer result = achievementService.findAchievementCountByTypeAndCategory("email@gmail.com", ACHIEVED, 1L);
        assertEquals(1, result);
        verify(userService).findByEmail("email@gmail.com");
        verify(achievementCategoryRepo).findById(anyLong());
        verify(userAchievementRepo).findAllByUserIdAndAchievement_AchievementCategoryId(anyLong(), anyLong());
        verify(achievementRepo).findById(anyLong());
        verify(modelMapper).map(achievement, AchievementVO.class);
    }

    @Test
    void countUnachievedAchievementsWithCategoryIdTest() {
        Achievement achievement = ModelUtils.getAchievement();
        AchievementVO achievementVO = ModelUtils.getAchievementVO();
        UserAchievement userAchievement = ModelUtils.getUserAchievement();
        userAchievement.setHabit(null);
        AchievementCategory achievementCategory = ModelUtils.getAchievementCategory();
        AchievementCategory habitCategory = ModelUtils.getAchievementCategory().setId(0L);
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        when(achievementCategoryRepo.findById(anyLong())).thenReturn(Optional.of(achievementCategory));
        when(achievementRepo.searchAchievementsUnAchievedByCategory(anyLong(), anyLong()))
            .thenReturn(List.of(achievement));
        when(modelMapper.map(achievement, AchievementVO.class)).thenReturn(achievementVO);
        when(achievementCategoryRepo.findByName("HABIT")).thenReturn(Optional.of(habitCategory));
        Integer result = achievementService.findAchievementCountByTypeAndCategory("email@gmail.com", UNACHIEVED, 1L);
        assertEquals(1, result);
        verify(userService).findByEmail("email@gmail.com");
        verify(achievementCategoryRepo).findById(anyLong());
        verify(achievementRepo).searchAchievementsUnAchievedByCategory(anyLong(), anyLong());
        verify(modelMapper).map(achievement, AchievementVO.class);
        verify(achievementCategoryRepo).findByName("HABIT");
    }

    @Test
    void saveTest() {
        Achievement achievement = ModelUtils.getAchievement();
        AchievementCategory achievementCategory = ModelUtils.getAchievementCategory();
        AchievementPostDto achievementPostDto = ModelUtils.getAchievementPostDto();
        AchievementVO achievementVO = ModelUtils.getAchievementVO();

        when(modelMapper.map(achievementPostDto, Achievement.class)).thenReturn(achievement);
        when(achievementCategoryRepo.findByName("Test")).thenReturn(Optional.of(achievementCategory));
        when(achievementRepo.save(achievement)).thenReturn(achievement);
        when(modelMapper.map(achievement, AchievementVO.class)).thenReturn(achievementVO);
        when(ratingPointsService.createRatingPoints(achievement.getTitle())).thenReturn(new ArrayList<>());
        AchievementVO expected = achievementService.save(achievementPostDto);
        assertEquals(expected, achievementVO);
    }

    @Test
    void updateTest() {
        Achievement achievement = ModelUtils.getAchievement();
        AchievementPostDto achievementPostDto = ModelUtils.getAchievementPostDto();
        AchievementManagementDto achievementManagementDto = ModelUtils.getAchievementManagementDto();
        when(achievementRepo.findById(1L)).thenReturn(Optional.of(achievement));
        when(achievementCategoryRepo.findByName(achievementManagementDto.getAchievementCategory().getName()))
            .thenReturn(Optional.of(ModelUtils.getAchievementCategory()));
        when(achievementRepo.save(achievement)).thenReturn(achievement);
        when(modelMapper.map(achievement, AchievementPostDto.class)).thenReturn(achievementPostDto);
        AchievementPostDto expected = achievementService.update(achievementManagementDto);
        assertEquals(expected, achievementPostDto);
    }

    @Test
    void updateWithUnknownId() {
        AchievementManagementDto achievementManagementDto = ModelUtils.getAchievementManagementDto();
        when(achievementRepo.findById(achievementManagementDto.getId()))
            .thenThrow(
                new NotUpdatedException(ErrorMessage.ACHIEVEMENT_NOT_FOUND_BY_ID + achievementManagementDto.getId()));
        assertThrows(NotUpdatedException.class, () -> achievementService.update(achievementManagementDto));
    }

    @Test
    void deleteTest() {
        Achievement achievement = ModelUtils.getAchievement();
        doNothing().when(achievementRepo).deleteById(1L);
        achievementRepo.deleteById(1L);
        verify(achievementRepo, times(1)).deleteById(1L);
        long expected = achievementService.delete(1L);
        assertEquals(expected, achievement.getId());
    }

    @Test
    void deleteWithNonExistingId() {
        doThrow(EmptyResultDataAccessException.class).when(achievementRepo).deleteById(345L);
        assertThrows(NotDeletedException.class, () -> achievementService.delete(345L));
    }

    @Test
    void deleteAll() {
        List<Long> listId = Arrays.asList(1L, 2L, 3L, 4L);
        listId.forEach(l -> {
            doNothing().when(achievementRepo).deleteById(l);
            achievementRepo.deleteById(l);
            verify(achievementRepo, times(1)).deleteById(l);
        });
        achievementService.deleteAll(listId);
    }

    @Test
    void findByCategoryIdAndCondition() {
        Achievement achievement = ModelUtils.getAchievement();
        AchievementVO achievementVO = ModelUtils.getAchievementVO();
        achievement.setAchievementCategory(ModelUtils.getAchievementCategory());
        achievementVO.setAchievementCategory(ModelUtils.getAchievementCategoryVO());
        when(achievementRepo.findByAchievementCategoryIdAndCondition(1L, 1)).thenReturn(Optional.of(achievement));
        when(modelMapper.map(achievement, AchievementVO.class)).thenReturn(achievementVO);
        assertEquals(achievementVO, achievementService.findByCategoryIdAndCondition(1L, 1));
    }

    @Test
    void achieveTest() {
        var userAchievement = getUserAchievement();
        var dto = getActionDto();
        when(userAchievementRepo.getUserAchievementByUserId(dto.getUserId())).thenReturn(List.of(userAchievement));
        achievementService.achieve(dto);

        verify(messagingTemplate).convertAndSend("/topic/" + dto.getUserId() + "/notification", true);
        verify(userAchievementRepo).getUserAchievementByUserId(dto.getUserId());
    }

    @Test
    void searchAchievementByTest_WithValidQuery() {
        String query = "test query";
        Pageable paging = PageRequest.of(0, 10);
        List<Achievement> achievementList = Collections.singletonList(ModelUtils.getAchievement());
        Page<Achievement> achievementPage = new PageImpl<>(achievementList, paging, 1);

        when(achievementRepo.searchAchievementsBy(paging, query)).thenReturn(achievementPage);

        PageableAdvancedDto<AchievementVO> result = achievementService.searchAchievementBy(paging, query);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getPage().size());

        verify(achievementRepo, times(1)).searchAchievementsBy(paging, query);
    }
}
