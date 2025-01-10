package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.achievementcategory.AchievementCategoryDto;
import greencity.dto.achievementcategory.AchievementCategoryTranslationDto;
import greencity.dto.achievementcategory.AchievementCategoryVO;
import greencity.dto.user.UserVO;
import greencity.entity.AchievementCategory;
import greencity.exception.exceptions.BadCategoryRequestException;
import greencity.repository.AchievementCategoryRepo;
import greencity.repository.UserAchievementRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class AchievementCategoryServiceImpl implements AchievementCategoryService {
    private final AchievementCategoryRepo achievementCategoryRepo;
    private final UserAchievementRepo userAchievementRepo;
    private final UserService userService;
    private final AchievementService achievementService;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public AchievementCategoryVO save(AchievementCategoryDto achievementCategoryDto) {
        achievementCategoryRepo.findByName(achievementCategoryDto.getName())
            .ifPresent(category -> {
                throw new BadCategoryRequestException(ErrorMessage.CATEGORY_ALREADY_EXISTS_BY_THIS_NAME);
            });
        AchievementCategory achievementCategoryToSave =
            modelMapper.map(achievementCategoryDto, AchievementCategory.class);
        return mapToVO(achievementCategoryRepo.save(achievementCategoryToSave));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AchievementCategoryTranslationDto> findAllWithAtLeastOneAchievement(String email) {
        UserVO user = userService.findByEmail(email);
        return achievementCategoryRepo.findAllWithAtLeastOneAchievement().stream()
            .map(achievementCategory -> modelMapper.map(achievementCategory, AchievementCategoryTranslationDto.class))
            .map(achievementCategory -> {
                Long achievementCategoryId = achievementCategory.getId();
                achievementCategory
                    .setTotalQuantity(
                        achievementService.findAchievementCountByTypeAndCategory(email, null, achievementCategoryId));
                achievementCategory.setAchieved(userAchievementRepo
                    .findAllByUserIdAndAchievement_AchievementCategoryId(user.getId(), achievementCategoryId).size());
                return achievementCategory;
            })
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AchievementCategoryVO> findAllForManagement() {
        return achievementCategoryRepo.findAll().stream().map(this::mapToVO).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AchievementCategoryVO findByName(String name) {
        AchievementCategory achievementCategory = achievementCategoryRepo.findByName(name)
            .orElseThrow(() -> new BadCategoryRequestException(ErrorMessage.CATEGORY_NOT_FOUND_BY_NAME));
        return mapToVO(achievementCategory);
    }

    private AchievementCategoryVO mapToVO(AchievementCategory entity) {
        return modelMapper.map(entity, AchievementCategoryVO.class);
    }
}
