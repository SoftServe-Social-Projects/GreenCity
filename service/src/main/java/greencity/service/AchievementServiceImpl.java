package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.achievement.AchievementManagementDto;
import greencity.dto.achievement.AchievementPostDto;
import greencity.dto.achievement.AchievementVO;
import greencity.dto.achievement.ActionDto;
import greencity.entity.Achievement;
import greencity.entity.AchievementCategory;
import greencity.entity.UserAchievement;
import greencity.entity.UserAction;
import greencity.enums.AchievementStatus;
import greencity.exception.exceptions.BadCategoryRequestException;
import greencity.exception.exceptions.NotDeletedException;
import greencity.exception.exceptions.NotUpdatedException;
import greencity.repository.AchievementCategoryRepo;
import greencity.repository.AchievementRepo;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import greencity.repository.UserAchievementRepo;
import greencity.repository.UserActionRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@EnableCaching
public class AchievementServiceImpl implements AchievementService {
    private final AchievementRepo achievementRepo;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final UserAchievementRepo userAchievementRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private final AchievementCategoryRepo achievementCategoryRepo;
    private final UserActionRepo userActionRepo;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void achieve(ActionDto user) {
        List<UserAchievement> userAchievements = userAchievementRepo.getUserAchievementByUserId(user.getUserId())
            .stream()
            .filter(userAchievement -> !userAchievement.isNotified())
            .toList();
        messagingTemplate.convertAndSend("/topic/" + user.getUserId() + "/notification",
            !userAchievements.isEmpty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AchievementVO save(AchievementPostDto achievementPostDto) {
        Achievement achievement = modelMapper.map(achievementPostDto, Achievement.class);
        AchievementCategory achievementCategory =
            findCategoryByName(achievementPostDto.getAchievementCategory().getName());
        populateAchievement(achievement, achievementPostDto, achievementCategory);
        return mapToVO(achievementRepo.save(achievement));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableAdvancedDto<AchievementVO> findAll(Pageable page) {
        Page<Achievement> pages = achievementRepo.findAll(page);
        return createPageable(pages);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AchievementVO> findAllByTypeAndCategory(String principalEmail, AchievementStatus achievementStatus,
        Long achievementCategoryId) {
        Long userId = userService.findByEmail(principalEmail).getId();
        return switch (achievementStatus) {
            case ACHIEVED -> findAllAchieved(userId, achievementCategoryId);
            case UNACHIEVED -> findUnachieved(userId, achievementCategoryId);
            case null, default -> findAllAchievementsWithAnyStatus(userId, achievementCategoryId);
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableAdvancedDto<AchievementVO> searchAchievementBy(Pageable paging, String query) {
        Page<Achievement> page = achievementRepo.searchAchievementsBy(paging, query);
        return createPageable(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long delete(Long id) {
        try {
            achievementRepo.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotDeletedException(ErrorMessage.ACHIEVEMENT_NOT_DELETED);
        }
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll(List<Long> listId) {
        listId.forEach(achievementRepo::deleteById);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AchievementPostDto update(AchievementManagementDto achievementManagementDto) {
        Achievement achievement = achievementRepo.findById(achievementManagementDto.getId())
            .orElseThrow(() -> new NotUpdatedException(ErrorMessage.ACHIEVEMENT_NOT_FOUND_BY_ID
                + achievementManagementDto.getId()));

        AchievementCategory achievementCategory = findCategoryByName(
            achievementManagementDto.getAchievementCategory().getName());
        populateAchievement(achievement, achievementManagementDto, achievementCategory);

        return modelMapper.map(achievementRepo.save(achievement), AchievementPostDto.class);
    }

    @Override
    @Transactional
    public AchievementVO findByCategoryIdAndCondition(Long categoryId, Integer condition) {
        return achievementRepo.findByAchievementCategoryIdAndCondition(categoryId, condition)
            .map(this::mapToVO)
            .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer findAchievementCountByTypeAndCategory(String principalEmail, AchievementStatus achievementStatus,
        Long achievementCategoryId) {
        return findAllByTypeAndCategory(principalEmail, achievementStatus, achievementCategoryId).size();
    }

    private AchievementCategory findCategoryByName(String name) {
        return achievementCategoryRepo.findByName(name)
            .orElseThrow(() -> new BadCategoryRequestException(ErrorMessage.CATEGORY_NOT_FOUND_BY_NAME));
    }

    private void populateAchievement(Achievement achievement, AchievementPostDto achievementPostDto,
        AchievementCategory achievementCategory) {
        achievement.setTitle(achievementPostDto.getTitle());
        achievement.setName(achievementPostDto.getName());
        achievement.setNameEng(achievementPostDto.getNameEng());
        achievement.setAchievementCategory(achievementCategory);
        achievement.setCondition(achievementPostDto.getCondition());
    }

    private List<AchievementVO> findAllAchievementsWithAnyStatus(Long userId, Long achievementCategoryId) {
        if (achievementCategoryId == null) {
            List<UserAction> allActions = userActionRepo.findAllByUserId(userId);
            return achievementRepo.findAll()
                .stream()
                .map(this::mapToVO)
                .map(achievementVO -> {
                    int achievementCategoryProgress = 0;
                    for (UserAction action : allActions) {
                        if (action.getAchievementCategory().getId()
                            .equals(achievementVO.getAchievementCategory().getId())) {
                            achievementCategoryProgress = action.getCount();
                            break;
                        }
                    }
                    return achievementVO.setProgress(achievementCategoryProgress);
                })
                .toList();
        } else {
            UserAction achievementAction =
                userActionRepo.findByUserIdAndAchievementCategoryId(userId, achievementCategoryId);
            int achievementCategoryProgress = achievementAction != null ? achievementAction.getCount() : 0;
            return achievementRepo.findAllByAchievementCategoryId(achievementCategoryId)
                .stream()
                .map(this::mapToVO)
                .map(achievementVO -> achievementVO.setProgress(achievementCategoryProgress))
                .toList();
        }
    }

    private List<AchievementVO> findAllAchieved(Long userId, Long achievementCategoryId) {
        if (achievementCategoryId == null) {
            List<UserAction> allActions = userActionRepo.findAllByUserId(userId);
            return userAchievementRepo.getUserAchievementByUserId(userId)
                .stream()
                .map(userAchievement -> userAchievement.getAchievement().getId())
                .map(achievementRepo::findById)
                .flatMap(Optional::stream)
                .map(this::mapToVO)
                .map(achievementVO -> {
                    int achievementCategoryProgress = 0;
                    for (UserAction action : allActions) {
                        if (action.getAchievementCategory().getId()
                            .equals(achievementVO.getAchievementCategory().getId())) {
                            achievementCategoryProgress = action.getCount();
                            break;
                        }
                    }
                    return achievementVO.setProgress(achievementCategoryProgress);
                })
                .toList();
        } else {
            UserAction achievementAction =
                userActionRepo.findByUserIdAndAchievementCategoryId(userId, achievementCategoryId);
            int achievementCategoryProgress = achievementAction != null ? achievementAction.getCount() : 0;
            return userAchievementRepo
                .findAllByUserIdAndAchievement_AchievementCategoryId(userId, achievementCategoryId)
                .stream()
                .map(userAchievement -> userAchievement.getAchievement().getId())
                .map(achievementRepo::findById)
                .flatMap(Optional::stream)
                .map(this::mapToVO)
                .map(achievementVO -> achievementVO.setProgress(achievementCategoryProgress))
                .toList();
        }
    }

    private List<AchievementVO> findUnachieved(Long userId, Long achievementCategoryId) {
        if (achievementCategoryId == null) {
            List<UserAction> allActions = userActionRepo.findAllByUserId(userId);
            return achievementRepo.searchAchievementsUnAchieved(userId)
                .stream()
                .map(this::mapToVO)
                .map(achievementVO -> {
                    int achievementCategoryProgress = 0;
                    for (UserAction action : allActions) {
                        if (action.getAchievementCategory().getId()
                            .equals(achievementVO.getAchievementCategory().getId())) {
                            achievementCategoryProgress = action.getCount();
                            break;
                        }
                    }
                    return achievementVO.setProgress(achievementCategoryProgress);
                })
                .toList();
        } else {
            UserAction achievementAction =
                userActionRepo.findByUserIdAndAchievementCategoryId(userId, achievementCategoryId);
            int achievementCategoryProgress = achievementAction != null ? achievementAction.getCount() : 0;
            return achievementRepo.searchAchievementsUnAchievedByCategory(userId, achievementCategoryId)
                .stream()
                .map(this::mapToVO)
                .map(achievementVO -> achievementVO.setProgress(achievementCategoryProgress))
                .toList();
        }
    }

    private PageableAdvancedDto<AchievementVO> createPageable(Page<Achievement> pages) {
        List<AchievementVO> achievementVOS = pages
            .stream()
            .map(this::mapToVO)
            .collect(Collectors.toList());
        return new PageableAdvancedDto<>(
            achievementVOS,
            pages.getTotalElements(),
            pages.getPageable().getPageNumber(),
            pages.getTotalPages(),
            pages.getNumber(),
            pages.hasPrevious(),
            pages.hasNext(),
            pages.isFirst(),
            pages.isLast());
    }

    private AchievementVO mapToVO(Achievement achievement) {
        return modelMapper.map(achievement, AchievementVO.class);
    }
}
