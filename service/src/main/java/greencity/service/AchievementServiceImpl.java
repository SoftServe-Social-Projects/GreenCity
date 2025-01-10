package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.achievement.AchievementManagementDto;
import greencity.dto.achievement.AchievementPostDto;
import greencity.dto.achievement.AchievementVO;
import greencity.dto.achievement.ActionDto;
import greencity.dto.habit.HabitVO;
import greencity.entity.Achievement;
import greencity.entity.AchievementCategory;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.HabitTranslation;
import greencity.entity.UserAchievement;
import greencity.entity.UserAction;
import greencity.enums.AchievementStatus;
import greencity.exception.exceptions.BadCategoryRequestException;
import greencity.exception.exceptions.NotDeletedException;
import greencity.exception.exceptions.NotUpdatedException;
import greencity.exception.exceptions.WrongIdException;
import greencity.repository.AchievementCategoryRepo;
import greencity.repository.AchievementRepo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import greencity.repository.HabitAssignRepo;
import greencity.repository.HabitTranslationRepo;
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
    private final HabitAssignRepo habitAssignRepo;
    private final HabitTranslationRepo habitTranslationRepo;
    private final RatingPointsService ratingPointsService;

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
        ratingPointsService.createRatingPoints(achievement.getTitle());
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
        Long searchAchievementCategoryId =
            achievementCategoryId != null ? findCategoryById(achievementCategoryId).getId() : null;
        List<AchievementVO> achievements = switch (achievementStatus) {
            case ACHIEVED -> findAllAchieved(userId, searchAchievementCategoryId);
            case UNACHIEVED -> findUnachieved(userId, searchAchievementCategoryId);
            case null, default -> findAllAchievementsWithAnyStatus(userId, searchAchievementCategoryId);
        };
        return setAchievementsProgress(userId, achievementCategoryId, achievements);
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
        if (!achievement.getTitle().equals(achievementManagementDto.getTitle())) {
            ratingPointsService.updateRatingPointsName(achievement.getTitle(), achievementManagementDto.getTitle());
        }
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
        Long userId = userService.findByEmail(principalEmail).getId();
        Long searchAchievementCategoryId =
            achievementCategoryId != null ? findCategoryById(achievementCategoryId).getId() : null;
        List<AchievementVO> achievements = switch (achievementStatus) {
            case ACHIEVED -> findAllAchieved(userId, searchAchievementCategoryId);
            case UNACHIEVED -> findUnachieved(userId, searchAchievementCategoryId);
            case null, default -> findAllAchievementsWithAnyStatus(userId, searchAchievementCategoryId);
        };
        return achievements.size();
    }

    private AchievementCategory findCategoryByName(String name) {
        return achievementCategoryRepo.findByName(name)
            .orElseThrow(() -> new BadCategoryRequestException(ErrorMessage.CATEGORY_NOT_FOUND_BY_NAME));
    }

    private AchievementCategory findCategoryById(Long id) {
        return achievementCategoryRepo.findById(id)
            .orElseThrow(() -> new WrongIdException(ErrorMessage.CATEGORY_NOT_FOUND_BY_ID + id));
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
        List<AchievementVO> achievements = new ArrayList<>();
        achievements.addAll(findAllAchieved(userId, achievementCategoryId));
        achievements.addAll(findUnachieved(userId, achievementCategoryId));
        return achievements;
    }

    private List<AchievementVO> findAllAchieved(Long userId, Long achievementCategoryId) {
        List<UserAchievement> achievements;
        if (achievementCategoryId == null) {
            achievements = userAchievementRepo.getUserAchievementByUserId(userId);
        } else {
            achievements =
                userAchievementRepo.findAllByUserIdAndAchievement_AchievementCategoryId(userId, achievementCategoryId);
        }
        return achievements.stream()
            .map(userAchievement -> {
                Long achievementId = userAchievement.getAchievement().getId();
                Achievement achievement = achievementRepo.findById(achievementId).orElse(null);
                AchievementVO achievementVO = mapToVO(achievement);
                if (userAchievement.getHabit() != null) {
                    HabitTranslation translationUa =
                        habitTranslationRepo.getHabitTranslationByUaLanguage(userAchievement.getHabit().getId());
                    HabitTranslation translationEn =
                        habitTranslationRepo.getHabitTranslationByEnLanguage(userAchievement.getHabit().getId());
                    achievementVO.setHabit(mapHabitToVO(userAchievement.getHabit()));
                    achievementVO.setName(achievementVO.getName() + " " + translationUa.getName());
                    achievementVO.setNameEng(achievementVO.getName() + " " + translationEn.getName());
                }
                return achievementVO;
            })
            .toList();
    }

    private List<AchievementVO> findUnachieved(Long userId, Long achievementCategoryId) {
        List<Achievement> achievements;
        if (achievementCategoryId == null) {
            achievements = achievementRepo.searchAchievementsUnAchieved(userId);
        } else {
            achievements = achievementRepo.searchAchievementsUnAchievedByCategory(userId, achievementCategoryId);
        }
        List<AchievementVO> achievementsToReturn = new ArrayList<>(achievements.stream()
            .map(this::mapToVO)
            .toList());

        Long habitAchievementCategory = findCategoryByName("HABIT").getId();
        if (achievementCategoryId == null || achievementCategoryId.equals(habitAchievementCategory)) {
            findUnachievedHabitAchievements(userId, habitAchievementCategory, achievementsToReturn);
        }

        return achievementsToReturn;
    }

    private void findUnachievedHabitAchievements(Long userId, Long categoryId, List<AchievementVO> achievements) {
        List<AchievementVO> unachievedHabitAchievements = new ArrayList<>();
        List<HabitAssign> habitInProgress = habitAssignRepo.findAllInProgressHabitAssignsRelatedToUser(userId);
        List<Integer> habitAchievementsDurations = achievementRepo.findAllByAchievementCategoryId(categoryId)
            .stream()
            .map(Achievement::getCondition)
            .toList();
        Map<Integer, List<HabitAssign>> achievementConditionUserHabitMap = new HashMap<>();
        for (Integer duration : habitAchievementsDurations) {
            achievementConditionUserHabitMap.put(duration, habitInProgress
                .stream()
                .filter(habitAssign -> habitAssign.getDuration().equals(duration)
                    || habitAssign.getDuration().compareTo(duration) < 0)
                .toList());
        }
        for (Integer duration : habitAchievementsDurations) {
            AchievementVO achievementByDuration = findByCategoryIdAndCondition(categoryId, duration);
            if (!achievementConditionUserHabitMap.get(duration).isEmpty()) {
                achievements.remove(achievementByDuration);
            }
            for (HabitAssign habitAssign : achievementConditionUserHabitMap.get(duration)) {
                HabitTranslation translationUa =
                    habitTranslationRepo.getHabitTranslationByUaLanguage(habitAssign.getHabit().getId());
                HabitTranslation translationEn =
                    habitTranslationRepo.getHabitTranslationByEnLanguage(habitAssign.getHabit().getId());
                AchievementVO achievementByHabit = AchievementVO.builder()
                    .id(achievementByDuration.getId())
                    .title(achievementByDuration.getTitle())
                    .name(achievementByDuration.getName() + " " + translationUa.getName())
                    .nameEng(achievementByDuration.getNameEng() + " " + translationEn.getName())
                    .achievementCategory(achievementByDuration.getAchievementCategory())
                    .condition(achievementByDuration.getCondition())
                    .habit(mapHabitToVO(habitAssign.getHabit()))
                    .build();
                unachievedHabitAchievements.add(achievementByHabit);
            }
        }
        achievements.addAll(unachievedHabitAchievements);
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

    private HabitVO mapHabitToVO(Habit habit) {
        return modelMapper.map(habit, HabitVO.class);
    }

    private List<AchievementVO> setAchievementsProgress(Long userId, Long achievementCategoryId,
        List<AchievementVO> achievements) {
        if (achievementCategoryId == null) {
            return setAchievementsProgressForAllCategories(userId, achievements);
        } else {
            return setAchievementsProgressForOneCategory(userId, achievementCategoryId, achievements);
        }
    }

    private List<AchievementVO> setAchievementsProgressForAllCategories(Long userId, List<AchievementVO> achievements) {
        List<UserAction> allActions = userActionRepo.findAllByUserId(userId);
        return achievements.stream().map(achievementVO -> {
            int achievementCategoryProgress = 0;
            for (UserAction action : allActions) {
                if ((action.getHabit() == null && action.getAchievementCategory().getId()
                    .equals(achievementVO.getAchievementCategory().getId()))
                    || (action.getHabit() != null && achievementVO.getHabit() != null
                        && action.getHabit().getId().equals(achievementVO.getHabit().getId()))) {
                    achievementCategoryProgress = action.getCount();
                    break;
                }
            }
            return achievementVO.setProgress(achievementCategoryProgress);
        })
            .toList();
    }

    private List<AchievementVO> setAchievementsProgressForOneCategory(Long userId, Long categoryId,
        List<AchievementVO> achievements) {
        Long habitAchievementCategory = findCategoryByName("HABIT").getId();
        if (categoryId.equals(habitAchievementCategory)) {
            return achievements.stream()
                .map(achievementVO -> {
                    int achievementCategoryProgress = 0;
                    if (achievementVO.getHabit() != null) {
                        UserAction achievementAction =
                            userActionRepo.findByUserIdAndAchievementCategoryIdAndHabitId(userId,
                                categoryId, achievementVO.getHabit().getId());
                        achievementCategoryProgress = achievementAction != null ? achievementAction.getCount() : 0;
                    }
                    return achievementVO.setProgress(achievementCategoryProgress);
                })
                .toList();
        } else {
            UserAction achievementAction =
                userActionRepo.findByUserIdAndAchievementCategoryId(userId, categoryId);
            int achievementCategoryProgress = achievementAction != null ? achievementAction.getCount() : 0;
            return achievements.stream()
                .map(achievementVO -> achievementVO.setProgress(achievementCategoryProgress))
                .toList();
        }
    }
}
