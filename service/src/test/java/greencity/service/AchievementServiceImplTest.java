package greencity.service;

import greencity.ModelUtils;
import static greencity.ModelUtils.getUserVO;
import static greencity.ModelUtils.getUserAchievement;
import static greencity.ModelUtils.getActionDto;
import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.achievement.AchievementManagementDto;
import greencity.dto.achievement.AchievementPostDto;
import greencity.dto.achievement.AchievementVO;
import greencity.dto.achievement.UserAchievementVO;
import greencity.dto.achievementcategory.AchievementCategoryVO;
import greencity.dto.user.UserVO;
import greencity.dto.useraction.UserActionVO;
import greencity.entity.Achievement;
import greencity.entity.AchievementCategory;
import static greencity.enums.AchievementStatus.ACHIEVED;
import static greencity.enums.AchievementStatus.UNACHIEVED;
import greencity.exception.exceptions.NotDeletedException;
import greencity.exception.exceptions.NotUpdatedException;
import greencity.repository.AchievementCategoryRepo;
import greencity.repository.AchievementRepo;
import greencity.repository.UserAchievementRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
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
import java.util.ArrayList;
import java.util.Arrays;
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
    private RestClient restClient;
    @Mock
    private UserActionService userActionService;
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

    @Test
    void findAllWithEmptyListTest() {
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        when(achievementRepo.findAll()).thenReturn(Collections.emptyList());
        List<AchievementVO> findAllResult = achievementService.findAllByType("email@gmail.com", null);
        assertTrue(findAllResult.isEmpty());
    }

    @Test
    void findAllWithOneValueInRepoTest() {
        Achievement achievement = ModelUtils.getAchievement();
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        when(achievementRepo.findAll()).thenReturn(Collections.singletonList(achievement));
        when(modelMapper.map(achievement, AchievementVO.class)).thenReturn(ModelUtils.getAchievementVO());
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        List<AchievementVO> findAllResult = achievementService.findAllByType("email@gmail.com", null);
        assertEquals(1L, (long) findAllResult.getFirst().getId());
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
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        when(userAchievementRepo.getUserAchievementByUserId(anyLong()))
            .thenReturn(List.of(ModelUtils.getUserAchievement()));
        when(achievementRepo.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAchievement()));
        when(modelMapper.map(ModelUtils.getAchievement(), AchievementVO.class))
            .thenReturn(ModelUtils.getAchievementVO());
        List<AchievementVO> findAllResult = achievementService.findAllByType("email@gmail.com", ACHIEVED);
        assertEquals(1L, (long) findAllResult.getFirst().getId());
        verify(userService).findByEmail("email@gmail.com");
        verify(userAchievementRepo).getUserAchievementByUserId(anyLong());
        verify(modelMapper).map(ModelUtils.getAchievement(), AchievementVO.class);
    }

    @Test
    void findAllUNACHIEVEDInRepoTest() {
        when(userService.findByEmail("email@gmail.com")).thenReturn(getUserVO());
        when(achievementRepo.searchAchievementsUnAchieved(anyLong()))
            .thenReturn(List.of(ModelUtils.getAchievement()));
        when(modelMapper.map(ModelUtils.getAchievement(), AchievementVO.class))
            .thenReturn(ModelUtils.getAchievementVO());
        List<AchievementVO> findAllResult = achievementService.findAllByType("email@gmail.com", UNACHIEVED);
        assertEquals(1L, (long) findAllResult.getFirst().getId());
        verify(userService).findByEmail("email@gmail.com");
        verify(achievementRepo).searchAchievementsUnAchieved(anyLong());
    }

    @Test
    void saveTest() {
        Achievement achievement = ModelUtils.getAchievement();
        AchievementCategory achievementCategory = ModelUtils.getAchievementCategory();
        AchievementPostDto achievementPostDto = ModelUtils.getAchievementPostDto();
        AchievementCategoryVO achievementCategoryVO = ModelUtils.getAchievementCategoryVO();
        AchievementVO achievementVO = ModelUtils.getAchievementVO();
        UserVO userVO = getUserVO();
        UserAchievementVO userAchievement = ModelUtils.getUserAchievementVO();
        List<UserAchievementVO> userAchievements = new ArrayList<>();
        userAchievements.add(userAchievement);
        userVO.setUserAchievements(userAchievements);
        UserActionVO userActionVO = ModelUtils.getUserActionVO();
        List<UserActionVO> userActionVOS = new ArrayList<>();
        userActionVOS.add(userActionVO);
        userVO.setUserActions(userActionVOS);
        when(modelMapper.map(achievementPostDto, Achievement.class)).thenReturn(achievement);
        when(achievementCategoryRepo.findByName("Test")).thenReturn(Optional.of(achievementCategory));
        when(achievementRepo.save(achievement)).thenReturn(achievement);
        when(modelMapper.map(achievement, AchievementVO.class)).thenReturn(achievementVO);
        when(restClient.findAll()).thenReturn(Collections.singletonList(userVO));
        when(userActionService.findUserAction(1L, 1L)).thenReturn(null);

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
}
