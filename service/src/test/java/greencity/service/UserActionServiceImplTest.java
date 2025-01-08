package greencity.service;

import greencity.ModelUtils;
import greencity.dto.useraction.UserActionVO;
import greencity.entity.AchievementCategory;
import greencity.entity.Habit;
import greencity.entity.UserAction;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.AchievementCategoryRepo;
import greencity.repository.HabitRepo;
import greencity.repository.UserActionRepo;
import greencity.repository.UserRepo;
import java.util.Collections;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class UserActionServiceImplTest {
    @InjectMocks
    private UserActionServiceImpl userActionService;
    @Mock
    private UserActionRepo userActionRepo;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private UserRepo userRepo;
    @Mock
    private HabitRepo habitRepo;
    @Mock
    private AchievementCategoryRepo achievementCategoryRepo;

    @Test
    void updateUserActions() {
        UserAction userAction = ModelUtils.getUserAction();
        UserActionVO userActionVO = ModelUtils.getUserActionVO();
        when(userActionRepo.findById(1L)).thenReturn(Optional.of(userAction));
        when(userActionRepo.save(userAction)).thenReturn(userAction);
        when(modelMapper.map(userAction, UserActionVO.class)).thenReturn(userActionVO);
        assertEquals(userActionVO, userActionService.updateUserActions(userActionVO));
    }

    @Test
    void updateUserActionsNull() {
        UserActionVO userActionVO = ModelUtils.getUserActionVO();
        when(userActionRepo.findById(1L)).thenReturn(Optional.empty());
        assertNull(userActionService.updateUserActions(userActionVO));
    }

    @Test
    void findUserActionByUserId() {
        UserAction userAction = ModelUtils.getUserAction();
        UserActionVO userActionVO = ModelUtils.getUserActionVO();
        when(userActionRepo.findByUserIdAndAchievementCategoryId(1L, 1L)).thenReturn(userAction);
        when(modelMapper.map(userAction, UserActionVO.class)).thenReturn(userActionVO);
        assertEquals(userActionVO, userActionService.findUserAction(1L, 1L));
    }

    @Test
    void findUserAction() {
        UserAction userAction = ModelUtils.getUserAction();
        UserActionVO userActionVO = ModelUtils.getUserActionVO();
        when(userActionRepo.findByUserIdAndAchievementCategoryIdAndHabitId(1L, 1L, 1L)).thenReturn(userAction);
        when(modelMapper.map(userAction, UserActionVO.class)).thenReturn(userActionVO);
        assertEquals(userActionVO, userActionService.findUserAction(1L, 1L, 1L));
    }

    @Test
    void findUserAction_null() {
        when(userActionRepo.findByUserIdAndAchievementCategoryIdAndHabitId(1L, 1L, 1L)).thenReturn(null);
        assertEquals(null, userActionService.findUserAction(1L, 1L, 1L));
    }

    @Test
    void findUserAction_null2() {
        when(userActionRepo.findByUserIdAndAchievementCategoryId(1L, 1L)).thenReturn(null);
        assertEquals(null, userActionService.findUserAction(1L, 1L));
    }

    @Test
    void createUserActionByUserId_NoSuchCategory() {
        assertThrows(NotFoundException.class,
            () -> userActionService.createUserAction(1L, 1L, null));
    }

    @Test
    void createUserActionByUserIdNull() {
        UserActionVO userActionVO = ModelUtils.getUserActionVO();
        when(userRepo.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getUser()));
        when(achievementCategoryRepo.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAchievementCategory()));
        UserAction userAction = UserAction.builder()
            .user(ModelUtils.getUser())
            .achievementCategory(AchievementCategory.builder()
                .id(1L)
                .name("HABIT")
                .title("Набудь Звички")
                .titleEn("Acquire Habits")
                .achievementList(Collections.emptyList())
                .build())
            .count(0)
            .build();

        when(modelMapper.map(userAction, UserActionVO.class)).thenReturn(userActionVO);
        UserActionVO resultUserActionVO = userActionService.createUserAction(1L, 1L, null);
        assertEquals(userActionVO, resultUserActionVO);
        verify(userActionRepo).save(any());

    }

    @Test
    void createUserActionAndHabitId() {
        UserActionVO userActionVO = ModelUtils.getUserActionVO();
        Habit habit = ModelUtils.getHabit();
        habit.setId(3L);
        AchievementCategory achievementCategory = ModelUtils.getAchievementCategory();
        achievementCategory.setId(2L);
        UserAction userAction = UserAction.builder()
            .user(ModelUtils.getUser())
            .achievementCategory(achievementCategory)
            .habit(habit)
            .count(0)
            .build();
        when(userRepo.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getUser()));
        when(achievementCategoryRepo.findById(anyLong())).thenReturn(Optional.of(achievementCategory));
        when(habitRepo.findById(3L)).thenReturn(Optional.of(habit));
        when(modelMapper.map(userAction, UserActionVO.class)).thenReturn(userActionVO);

        UserActionVO resultUserActionVO = userActionService.createUserAction(1L, 2L, 3L);

        assertEquals(userActionVO, resultUserActionVO);

        verify(userActionRepo).save(any());
        verify(habitRepo).findById(3L);
        verify(modelMapper).map(userAction, UserActionVO.class);
        verify(userRepo).findById(anyLong());
        verify(achievementCategoryRepo).findById(anyLong());
    }

    @Test
    void createUserActionAndHabitId_NullHabit() {
        Habit habit = ModelUtils.getHabit();
        habit.setId(3L);
        AchievementCategory achievementCategory = ModelUtils.getAchievementCategory();
        achievementCategory.setId(2L);
        when(userRepo.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getUser()));
        when(achievementCategoryRepo.findById(anyLong())).thenReturn(Optional.of(achievementCategory));

        assertThrows(NotFoundException.class, () -> userActionService.createUserAction(1L, 2L, 3L));

        verify(userRepo).findById(anyLong());
        verify(achievementCategoryRepo).findById(anyLong());
    }

    @Test
    void save() {
        UserAction userAction = ModelUtils.getUserAction();
        UserActionVO userActionVO = ModelUtils.getUserActionVO();
        when(modelMapper.map(userActionVO, UserAction.class)).thenReturn(userAction);
        when(userActionRepo.save(userAction)).thenReturn(userAction);
        when(modelMapper.map(userAction, UserActionVO.class)).thenReturn(userActionVO);
        assertEquals(userActionVO, userActionService.save(userActionVO));
    }
}
