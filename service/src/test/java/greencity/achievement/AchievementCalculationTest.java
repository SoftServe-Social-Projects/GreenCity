package greencity.achievement;

import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.dto.achievement.AchievementVO;
import greencity.dto.achievementcategory.AchievementCategoryVO;
import greencity.dto.user.UserVO;
import greencity.dto.useraction.UserActionVO;
import greencity.entity.User;
import greencity.entity.UserAchievement;
import greencity.enums.AchievementCategoryType;
import greencity.enums.AchievementAction;
import greencity.enums.RatingCalculationEnum;
import greencity.exception.exceptions.NotFoundException;
import greencity.rating.RatingCalculation;
import greencity.repository.AchievementCategoryRepo;
import greencity.repository.AchievementRepo;
import greencity.repository.UserAchievementRepo;
import greencity.repository.UserRepo;
import greencity.repository.HabitRepo;
import greencity.service.UserService;
import greencity.service.UserActionService;
import greencity.service.AchievementService;
import greencity.service.AchievementCategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import greencity.entity.Achievement;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collections;
import java.util.Optional;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementCalculationTest {
    @Mock
    private HabitRepo habitRepo;
    @Mock
    private RestClient restClient;
    @Mock
    private UserActionService userActionService;
    @Mock
    private AchievementService achievementService;
    @Mock
    private AchievementCategoryService achievementCategoryService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private UserAchievementRepo userAchievementRepo;
    @Mock
    private AchievementRepo achievementRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private AchievementCategoryRepo achievementCategoryRepo;
    @Mock
    private RatingCalculationEnum ratingCalculationEnum;
    @InjectMocks
    private AchievementCalculation achievementCalculation;
    @Mock
    private UserService userService;
    @Mock
    private RatingCalculation ratingCalculation;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void calculateAchievement_reasonNull() {
        AchievementCategoryVO achievementCategoryVO = new AchievementCategoryVO(1L, "ACHIEVEMENT");
        AchievementCategoryVO achievementCategoryVO2 = new AchievementCategoryVO(2L, "CREATE_NEWS");
        UserVO userVO = ModelUtils.getUserVO();
        UserActionVO userActionVO = new UserActionVO(1L, userVO, achievementCategoryVO, 0);
        User user = ModelUtils.getUser();
        Achievement achievement = ModelUtils.getAchievement();
        achievement.setTitle("bla bla");
        UserAchievement userAchievement = ModelUtils.getUserAchievement();
        user.setUserAchievements(Collections.singletonList(userAchievement));
        AchievementVO achievementVO =
            new AchievementVO(1L, "CREATED_5_NEWS", "CREATED_5_NEWS", "CREATED_5_NEWS", new AchievementCategoryVO(), 1);
        when(achievementCategoryService.findByName(AchievementCategoryType.CREATE_NEWS.name()))
            .thenReturn(achievementCategoryVO);
        when(userActionService.findUserAction(any(), any())).thenReturn(userActionVO);
        when(achievementRepo.findByAchievementCategoryIdAndCondition(anyLong(), any()))
            .thenReturn(Optional.of(achievement));
        when(achievementCategoryService.findByName("CREATE_NEWS")).thenReturn(achievementCategoryVO2);
        when(achievementService.findByCategoryIdAndCondition(2L, 1)).thenReturn(achievementVO);

        assertThrows(NotFoundException.class, () -> achievementCalculation.calculateAchievement(userVO,
            AchievementCategoryType.CREATE_NEWS, AchievementAction.ASSIGN));
        verify(achievementCategoryService).findByName(AchievementCategoryType.CREATE_NEWS.name());
        verify(userActionService).findUserAction(any(), any());
        verify(achievementRepo).findByAchievementCategoryIdAndCondition(anyLong(), any());
        verify(achievementCategoryService).findByName("CREATE_NEWS");
        verify(achievementService).findByCategoryIdAndCondition(2L, 1);
    }

    @Test
    void calculateAchievement() {
        AchievementCategoryVO achievementCategoryVO = new AchievementCategoryVO(1L, "ACHIEVEMENT");
        AchievementCategoryVO achievementCategoryVO2 = new AchievementCategoryVO(2L, "CREATE_NEWS");
        UserVO userVO = ModelUtils.getUserVO();
        UserActionVO userActionVO = new UserActionVO(1L, userVO, achievementCategoryVO, 0);
        User user = ModelUtils.getUser();
        Achievement achievement = ModelUtils.getAchievement();
        UserAchievement userAchievement = ModelUtils.getUserAchievement();
        user.setUserAchievements(Collections.singletonList(userAchievement));
        AchievementVO achievementVO =
            new AchievementVO(1L, "CREATED_5_NEWS", "CREATED_5_NEWS", "CREATED_5_NEWS", new AchievementCategoryVO(), 1);
        when(achievementCategoryService.findByName(AchievementCategoryType.CREATE_NEWS.name()))
            .thenReturn(achievementCategoryVO);
        when(userActionService.findUserAction(any(), any())).thenReturn(userActionVO);
        when(achievementRepo.findByAchievementCategoryIdAndCondition(anyLong(), any()))
            .thenReturn(Optional.of(achievement));
        when(achievementCategoryService.findByName("ACHIEVEMENT")).thenReturn(achievementCategoryVO);
        when(achievementCategoryService.findByName("CREATE_NEWS")).thenReturn(achievementCategoryVO2);
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(achievementService.findByCategoryIdAndCondition(2L, 1)).thenReturn(achievementVO);

        achievementCalculation.calculateAchievement(userVO, AchievementCategoryType.CREATE_NEWS,
            AchievementAction.ASSIGN);

        assertEquals(2, userActionVO.getCount());
        verify(achievementCategoryService).findByName(AchievementCategoryType.CREATE_NEWS.name());
        verify(userActionService, times(2)).findUserAction(any(), any());
        verify(achievementRepo).findByAchievementCategoryIdAndCondition(anyLong(), any());
        verify(achievementCategoryService).findByName("ACHIEVEMENT");
        verify(achievementCategoryService).findByName("CREATE_NEWS");
        verify(modelMapper).map(userVO, User.class);
        verify(achievementService).findByCategoryIdAndCondition(2L, 1);
    }

    @Test
    void calculateAchievement_CountNegetive() {
        AchievementCategoryVO achievementCategoryVO = new AchievementCategoryVO(1L, "ACHIEVEMENT");
        AchievementCategoryVO achievementCategoryVO2 = new AchievementCategoryVO(2L, "CREATE_NEWS");
        UserVO userVO = ModelUtils.getUserVO();
        UserActionVO userActionVO = new UserActionVO(1L, userVO, achievementCategoryVO, -100);
        User user = ModelUtils.getUser();
        Achievement achievement = ModelUtils.getAchievement();
        UserAchievement userAchievement = ModelUtils.getUserAchievement();
        user.setUserAchievements(Collections.singletonList(userAchievement));
        AchievementVO achievementVO =
            new AchievementVO(1L, "CREATED_5_NEWS", "CREATED_5_NEWS", "CREATED_5_NEWS", new AchievementCategoryVO(), 1);
        when(achievementCategoryService.findByName(AchievementCategoryType.CREATE_NEWS.name()))
            .thenReturn(achievementCategoryVO);
        when(userActionService.findUserAction(any(), any())).thenReturn(userActionVO);
        when(achievementRepo.findByAchievementCategoryIdAndCondition(anyLong(), any()))
            .thenReturn(Optional.of(achievement));
        when(achievementCategoryService.findByName("ACHIEVEMENT")).thenReturn(achievementCategoryVO);
        when(achievementCategoryService.findByName("CREATE_NEWS")).thenReturn(achievementCategoryVO2);
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(achievementService.findByCategoryIdAndCondition(2L, 0)).thenReturn(achievementVO);
        achievementCalculation.calculateAchievement(userVO, AchievementCategoryType.CREATE_NEWS,
            AchievementAction.ASSIGN);
        assertEquals(1, userActionVO.getCount());
        verify(achievementCategoryService).findByName(AchievementCategoryType.CREATE_NEWS.name());
        verify(userActionService, times(2)).findUserAction(any(), any());
        verify(achievementRepo).findByAchievementCategoryIdAndCondition(anyLong(), any());
        verify(achievementCategoryService).findByName("ACHIEVEMENT");
        verify(achievementCategoryService).findByName("CREATE_NEWS");
        verify(modelMapper).map(userVO, User.class);
        verify(achievementService).findByCategoryIdAndCondition(2L, 0);
    }

    @Test
    void calculateAchievement_Null_AchievementAction() {
        AchievementCategoryVO achievementCategoryVO = new AchievementCategoryVO(1L, "ACHIEVEMENT");
        UserVO userVO = ModelUtils.getUserVO();
        UserActionVO userActionVO = new UserActionVO(1L, userVO, achievementCategoryVO, 0);
        User user = ModelUtils.getUser();
        UserAchievement userAchievement = ModelUtils.getUserAchievement();
        user.setUserAchievements(Collections.singletonList(userAchievement));
        when(achievementCategoryService.findByName(AchievementCategoryType.CREATE_NEWS.name()))
            .thenReturn(achievementCategoryVO);
        when(userActionService.findUserAction(any(), any())).thenReturn(userActionVO);
        achievementCalculation.calculateAchievement(userVO, AchievementCategoryType.CREATE_NEWS,
            null);
        assertEquals(0, userActionVO.getCount());
        verify(achievementCategoryService).findByName(AchievementCategoryType.CREATE_NEWS.name());
        verify(userActionService).findUserAction(any(), any());
    }

    @Test
    void calculateAchievement_UNDO() {
        AchievementCategoryVO achievementCategoryVO = new AchievementCategoryVO(1L, "ACHIEVEMENT");
        AchievementCategoryVO achievementCategoryVO2 = new AchievementCategoryVO(2L, "CREATE_NEWS");
        UserVO userVO = ModelUtils.getUserVO();
        UserActionVO userActionVO = new UserActionVO(1L, userVO, achievementCategoryVO, 0);
        User user = ModelUtils.getUser();
        Achievement achievement = ModelUtils.getAchievement();
        UserAchievement userAchievement = ModelUtils.getUserAchievement();
        user.setUserAchievements(Collections.singletonList(userAchievement));
        AchievementVO achievementVO =
            new AchievementVO(1L, "CREATED_5_NEWS", "CREATED_5_NEWS", "CREATED_5_NEWS", new AchievementCategoryVO(), 1);
        when(achievementCategoryService.findByName(AchievementCategoryType.CREATE_NEWS.name()))
            .thenReturn(achievementCategoryVO);
        when(userActionService.findUserAction(any(), any())).thenReturn(userActionVO);
        when(achievementCategoryService.findByName("ACHIEVEMENT")).thenReturn(achievementCategoryVO);
        when(achievementCategoryService.findByName("CREATE_NEWS")).thenReturn(achievementCategoryVO2);
        when(achievementRepo.findUnAchieved(1L, 2L)).thenReturn(Arrays.asList(ModelUtils.getAchievement()));
        when(achievementRepo.findUnAchieved(1L, 1L)).thenReturn(Collections.emptyList());

        achievementCalculation.calculateAchievement(userVO, AchievementCategoryType.CREATE_NEWS,
            AchievementAction.DELETE);

        assertEquals(0, userActionVO.getCount());
        verify(achievementCategoryService).findByName(AchievementCategoryType.CREATE_NEWS.name());
        verify(userActionService, times(2)).findUserAction(any(), any());
        verify(achievementRepo).findUnAchieved(1L, 1L);
        verify(achievementRepo).findUnAchieved(1L, 2L);
        verify(achievementCategoryService).findByName("ACHIEVEMENT");
        verify(achievementCategoryService).findByName("CREATE_NEWS");
    }

    @Test
    void calculateAchievement_achievemmentNull() {
        AchievementCategoryVO achievementCategoryVO = ModelUtils.getAchievementCategoryVO();
        AchievementCategoryVO achievementCategoryVO2 = ModelUtils.getAchievementCategoryVO();
        achievementCategoryVO2.setId(2L);
        UserActionVO userActionVO = ModelUtils.getUserActionVO();
        int count = userActionVO.getCount();
        UserActionVO userActionVO2 = ModelUtils.getUserActionVO();
        userActionVO2.setId(2L);
        User user = ModelUtils.getUser();
        UserAchievement userAchievement = ModelUtils.getUserAchievement();
        user.setUserAchievements(Collections.singletonList(userAchievement));
        when(achievementCategoryService.findByName(AchievementCategoryType.CREATE_NEWS.name()))
            .thenReturn(achievementCategoryVO);
        when(userActionService.findUserAction(1L, 1L)).thenReturn(userActionVO);
        achievementCalculation.calculateAchievement(ModelUtils.getUserVO(), AchievementCategoryType.CREATE_NEWS,
            AchievementAction.ASSIGN);
        assertEquals(count + 1, userActionVO.getCount());
        verify(achievementCategoryService).findByName(AchievementCategoryType.CREATE_NEWS.name());
        verify(userActionService).findUserAction(1L, 1L);
    }

    @Test
    void calculateAchievement_reasonNullHabit() {
        AchievementCategoryVO achievementCategoryVO = new AchievementCategoryVO(1L, "ACHIEVEMENT");
        AchievementCategoryVO achievementCategoryVO2 = new AchievementCategoryVO(2L, "CREATE_NEWS");
        UserVO userVO = ModelUtils.getUserVO();
        UserActionVO userActionVO = new UserActionVO(1L, userVO, achievementCategoryVO, 0);
        User user = ModelUtils.getUser();
        Achievement achievement = ModelUtils.getAchievement();
        achievement.setTitle("bla bla");
        UserAchievement userAchievement = ModelUtils.getUserAchievement();
        user.setUserAchievements(Collections.singletonList(userAchievement));
        AchievementVO achievementVO =
            new AchievementVO(1L, "CREATED_5_NEWS", "CREATED_5_NEWS", "CREATED_5_NEWS", new AchievementCategoryVO(), 1);
        when(achievementCategoryService.findByName(AchievementCategoryType.CREATE_NEWS.name()))
            .thenReturn(achievementCategoryVO);
        when(userActionService.findUserAction(any(), any(), any())).thenReturn(userActionVO);
        when(achievementRepo.findByAchievementCategoryIdAndCondition(anyLong(), any()))
            .thenReturn(Optional.of(achievement));
        when(achievementCategoryService.findByName("CREATE_NEWS")).thenReturn(achievementCategoryVO2);
        when(achievementService.findByCategoryIdAndCondition(2L, 1)).thenReturn(achievementVO);
        assertThrows(NotFoundException.class, () -> achievementCalculation.calculateAchievement(userVO,
            AchievementCategoryType.CREATE_NEWS, AchievementAction.ASSIGN, 1L));
        verify(achievementCategoryService).findByName(AchievementCategoryType.CREATE_NEWS.name());
        verify(userActionService).findUserAction(any(), any(), any());
        verify(achievementRepo).findByAchievementCategoryIdAndCondition(anyLong(), any());
        verify(achievementCategoryService).findByName("CREATE_NEWS");
        verify(achievementService).findByCategoryIdAndCondition(2L, 1);
    }

    @Test
    void calculateAchievemenHabit() {
        AchievementCategoryVO achievementCategoryVO = new AchievementCategoryVO(1L, "ACHIEVEMENT");
        AchievementCategoryVO achievementCategoryVO2 = new AchievementCategoryVO(2L, "CREATE_NEWS");
        UserVO userVO = ModelUtils.getUserVO();
        UserActionVO userActionVO = new UserActionVO(1L, userVO, achievementCategoryVO, 0);
        User user = ModelUtils.getUser();
        Achievement achievement = ModelUtils.getAchievement();
        UserAchievement userAchievement = ModelUtils.getUserAchievement();
        user.setUserAchievements(Collections.singletonList(userAchievement));
        AchievementVO achievementVO =
            new AchievementVO(1L, "CREATED_5_NEWS", "CREATED_5_NEWS", "CREATED_5_NEWS", new AchievementCategoryVO(), 1);
        when(achievementCategoryService.findByName(AchievementCategoryType.CREATE_NEWS.name()))
            .thenReturn(achievementCategoryVO);
        when(userActionService.findUserAction(any(), any(), any())).thenReturn(userActionVO);
        when(achievementRepo.findByAchievementCategoryIdAndCondition(anyLong(), any()))
            .thenReturn(Optional.of(achievement));
        when(achievementCategoryService.findByName("ACHIEVEMENT")).thenReturn(achievementCategoryVO);
        when(achievementCategoryService.findByName("CREATE_NEWS")).thenReturn(achievementCategoryVO2);
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(achievementService.findByCategoryIdAndCondition(2L, 1)).thenReturn(achievementVO);
        when(habitRepo.findById(1L)).thenReturn(Optional.of(ModelUtils.getHabit()));
        when(userActionService.createUserAction(any(), any(), any()))
            .thenReturn(userActionVO);
        achievementCalculation.calculateAchievement(userVO, AchievementCategoryType.CREATE_NEWS,
            AchievementAction.ASSIGN, 1L);

        assertEquals(2, userActionVO.getCount());
        verify(achievementCategoryService).findByName(AchievementCategoryType.CREATE_NEWS.name());
        verify(userActionService).findUserAction(any(), any());
        verify(achievementRepo).findByAchievementCategoryIdAndCondition(anyLong(), any());
        verify(achievementCategoryService).findByName("ACHIEVEMENT");
        verify(achievementCategoryService).findByName("CREATE_NEWS");
        verify(modelMapper).map(userVO, User.class);
        verify(achievementService).findByCategoryIdAndCondition(2L, 1);
    }

    @Test
    void calculateAchievement_Null_AchievementActionHabit() {
        AchievementCategoryVO achievementCategoryVO = new AchievementCategoryVO(1L, "ACHIEVEMENT");
        UserVO userVO = ModelUtils.getUserVO();
        UserActionVO userActionVO = new UserActionVO(1L, userVO, achievementCategoryVO, 0);
        User user = ModelUtils.getUser();
        UserAchievement userAchievement = ModelUtils.getUserAchievement();
        user.setUserAchievements(Collections.singletonList(userAchievement));
        when(achievementCategoryService.findByName(AchievementCategoryType.CREATE_NEWS.name()))
            .thenReturn(achievementCategoryVO);
        when(userActionService.findUserAction(any(), any(), any())).thenReturn(userActionVO);
        achievementCalculation.calculateAchievement(userVO, AchievementCategoryType.CREATE_NEWS,
            null, 1L);
        assertEquals(0, userActionVO.getCount());
        verify(achievementCategoryService).findByName(AchievementCategoryType.CREATE_NEWS.name());
        verify(userActionService).findUserAction(any(), any(), any());
    }

    @Test
    void calculateAchievement_UNDOHabit() {
        AchievementCategoryVO achievementCategoryVO = new AchievementCategoryVO(1L, "ACHIEVEMENT");
        AchievementCategoryVO achievementCategoryVO2 = new AchievementCategoryVO(2L, "CREATE_NEWS");
        UserVO userVO = ModelUtils.getUserVO();
        UserActionVO userActionVO = new UserActionVO(1L, userVO, achievementCategoryVO, 0);
        User user = ModelUtils.getUser();
        UserAchievement userAchievement = ModelUtils.getUserAchievement();
        user.setUserAchievements(Collections.singletonList(userAchievement));
        when(achievementCategoryService.findByName(AchievementCategoryType.CREATE_NEWS.name()))
            .thenReturn(achievementCategoryVO);
        when(userActionService.findUserAction(any(), any(), any())).thenReturn(userActionVO);
        when(achievementCategoryService.findByName("ACHIEVEMENT")).thenReturn(achievementCategoryVO);
        when(achievementCategoryService.findByName("CREATE_NEWS")).thenReturn(achievementCategoryVO2);
        when(achievementRepo.findUnAchieved(1L, 2L, 1L)).thenReturn(Arrays.asList(ModelUtils.getAchievement()));
        when(achievementRepo.findUnAchieved(1L, 1L)).thenReturn(Collections.emptyList());
        when(userActionService.createUserAction(any(), any(), any()))
            .thenReturn(userActionVO);
        achievementCalculation.calculateAchievement(userVO, AchievementCategoryType.CREATE_NEWS,
            AchievementAction.DELETE, 1L);

        assertEquals(0, userActionVO.getCount());
        verify(achievementCategoryService).findByName(AchievementCategoryType.CREATE_NEWS.name());
        verify(userActionService).findUserAction(any(), any());
        verify(achievementRepo).findUnAchieved(1L, 1L);
        verify(achievementRepo).findUnAchieved(1L, 2L, 1L);
        verify(achievementCategoryService).findByName("ACHIEVEMENT");
        verify(achievementCategoryService).findByName("CREATE_NEWS");
    }

    @Test
    void calculateAchievement_achievemmentNullHabit() {
        AchievementCategoryVO achievementCategoryVO = ModelUtils.getAchievementCategoryVO();
        AchievementCategoryVO achievementCategoryVO2 = ModelUtils.getAchievementCategoryVO();
        achievementCategoryVO2.setId(2L);
        UserActionVO userActionVO = ModelUtils.getUserActionVO();
        int count = userActionVO.getCount();
        UserActionVO userActionVO2 = ModelUtils.getUserActionVO();
        userActionVO2.setId(2L);
        User user = ModelUtils.getUser();
        UserAchievement userAchievement = ModelUtils.getUserAchievement();
        user.setUserAchievements(Collections.singletonList(userAchievement));
        when(achievementCategoryService.findByName(AchievementCategoryType.CREATE_NEWS.name()))
            .thenReturn(achievementCategoryVO);
        when(userActionService.findUserAction(1L, 1L, 1L)).thenReturn(userActionVO);
        achievementCalculation.calculateAchievement(ModelUtils.getUserVO(), AchievementCategoryType.CREATE_NEWS,
            AchievementAction.ASSIGN, 1L);
        assertEquals(count + 1, userActionVO.getCount());
        verify(achievementCategoryService).findByName(AchievementCategoryType.CREATE_NEWS.name());
        verify(userActionService).findUserAction(1L, 1L, 1L);
    }
}
