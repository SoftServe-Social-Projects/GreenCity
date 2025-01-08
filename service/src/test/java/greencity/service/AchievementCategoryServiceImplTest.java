package greencity.service;

import greencity.ModelUtils;
import greencity.dto.achievementcategory.AchievementCategoryDto;
import greencity.dto.achievementcategory.AchievementCategoryTranslationDto;
import greencity.dto.achievementcategory.AchievementCategoryVO;
import greencity.dto.user.UserVO;
import greencity.entity.AchievementCategory;
import greencity.exception.exceptions.BadCategoryRequestException;
import greencity.repository.AchievementCategoryRepo;
import greencity.repository.UserAchievementRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AchievementCategoryServiceImplTest {

    @Mock
    private AchievementCategoryRepo achievementCategoryRepo;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UserService userService;

    @Mock
    private AchievementService achievementService;

    @Mock
    private UserAchievementRepo userAchievementRepo;

    @InjectMocks
    private AchievementCategoryServiceImpl achievementCategoryService;

    @Test
    void saveThrowExceptionTest() {
        AchievementCategoryDto achievementCategoryDto = ModelUtils.getAchievementCategoryDto();
        when(achievementCategoryRepo.findByName(achievementCategoryDto.getName()))
            .thenReturn(Optional.of(ModelUtils.getAchievementCategory()));
        assertThrows(BadCategoryRequestException.class, () -> achievementCategoryService.save(achievementCategoryDto));
    }

    @Test
    void saveTest() {
        AchievementCategoryDto achievementCategoryDto = ModelUtils.getAchievementCategoryDto();
        AchievementCategory toSave = ModelUtils.getAchievementCategory();
        AchievementCategoryVO expected = ModelUtils.getAchievementCategoryVO();

        when(achievementCategoryRepo.findByName(achievementCategoryDto.getName())).thenReturn(Optional.empty());
        when(modelMapper.map(achievementCategoryDto, AchievementCategory.class)).thenReturn(toSave);
        when(achievementCategoryRepo.save(toSave)).thenReturn(toSave);
        when(modelMapper.map(toSave, AchievementCategoryVO.class)).thenReturn(expected);

        AchievementCategoryVO actual = achievementCategoryService.save(achievementCategoryDto);

        assertEquals(expected, actual);
    }

    @Test
    void findAllForManagementTest() {
        List<AchievementCategory> list = Collections.singletonList(ModelUtils.getAchievementCategory());
        List<AchievementCategoryVO> expected = Collections.singletonList(ModelUtils.getAchievementCategoryVO());

        when(achievementCategoryRepo.findAll()).thenReturn(list);
        when(modelMapper.map(ModelUtils.getAchievementCategory(), AchievementCategoryVO.class))
            .thenReturn(ModelUtils.getAchievementCategoryVO());

        List<AchievementCategoryVO> actual = achievementCategoryService.findAllForManagement();

        assertEquals(expected, actual);
    }

    @Test
    void findByNameTest() {
        AchievementCategory achievementCategory = ModelUtils.getAchievementCategory();
        AchievementCategoryVO expected = ModelUtils.getAchievementCategoryVO();

        when(achievementCategoryRepo.findByName("Name")).thenReturn(Optional.of(achievementCategory));
        when(modelMapper.map(achievementCategory, AchievementCategoryVO.class)).thenReturn(expected);

        AchievementCategoryVO actual = achievementCategoryService.findByName("Name");

        assertEquals(expected, actual);
    }

    @Test
    void findByNameThrowException() {
        when(achievementCategoryRepo.findByName("Not Exist")).thenReturn(Optional.empty());
        assertThrows(BadCategoryRequestException.class, () -> achievementCategoryService.findByName("Not Exist"));
    }

    @Test
    void findAllWithAtLeastOneAchievementTest() {
        List<AchievementCategory> list = List.of(ModelUtils.getAchievementCategory());
        AchievementCategoryTranslationDto expectedDto = ModelUtils.getAchievementCategoryTranslationDto();
        expectedDto.setAchieved(0);
        expectedDto.setTotalQuantity(0);
        List<AchievementCategoryTranslationDto> expected = List.of(expectedDto);
        UserVO userVO = ModelUtils.getUserVO();

        when(achievementCategoryRepo.findAllWithAtLeastOneAchievement()).thenReturn(list);
        when(modelMapper.map(ModelUtils.getAchievementCategory(), AchievementCategoryTranslationDto.class))
            .thenReturn(ModelUtils.getAchievementCategoryTranslationDto());
        when(userService.findByEmail("email@gmail.com")).thenReturn(userVO);
        when(achievementService.findAchievementCountByTypeAndCategory("email@gmail.com", null, list.getFirst().getId()))
            .thenReturn(0);
        when(userAchievementRepo.findAllByUserIdAndAchievement_AchievementCategoryId(userVO.getId(),
            expectedDto.getId())).thenReturn(Collections.emptyList());

        List<AchievementCategoryTranslationDto> actual =
            achievementCategoryService.findAllWithAtLeastOneAchievement("email@gmail.com");

        assertEquals(expected, actual);
    }
}
