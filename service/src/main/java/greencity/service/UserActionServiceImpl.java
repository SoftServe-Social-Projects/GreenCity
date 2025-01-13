package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.useraction.UserActionVO;
import greencity.entity.UserAction;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.AchievementCategoryRepo;
import greencity.repository.HabitRepo;
import greencity.repository.UserActionRepo;
import greencity.repository.UserRepo;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserActionServiceImpl implements UserActionService {
    private final ModelMapper modelMapper;
    private final UserRepo userRepo;
    private final AchievementCategoryRepo achievementCategoryRepo;
    private final HabitRepo habitRepo;
    private final UserActionRepo userActionRepo;

    @Override
    public UserActionVO save(UserActionVO userActionVO) {
        UserAction save = userActionRepo.save(modelMapper.map(userActionVO, UserAction.class));
        return modelMapper.map(save, UserActionVO.class);
    }

    /**
     * {@inheritDoc}
     *
     * @author Orest Mamchuk
     */
    @Transactional
    @Override
    public UserActionVO updateUserActions(UserActionVO userActionVO) {
        Optional<UserAction> byId = userActionRepo.findById(userActionVO.getId());
        if (byId.isPresent()) {
            UserAction toUpdate = byId.get();
            toUpdate.setCount(userActionVO.getCount());
            return modelMapper.map(userActionRepo.save(toUpdate), UserActionVO.class);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @author Oksana Spodaryk
     */
    @Transactional
    @Override
    public UserActionVO findUserAction(Long userId, Long categoryId) {
        UserAction userAction = userActionRepo.findByUserIdAndAchievementCategoryId(userId, categoryId);
        return userAction != null ? modelMapper.map(userAction, UserActionVO.class) : null;
    }

    /**
     * {@inheritDoc}
     *
     * @author Oksana Spodaryk
     */
    @Transactional
    @Override
    public UserActionVO findUserAction(Long userId, Long categoryId,
        Long habitId) {
        UserAction userAction =
            userActionRepo.findByUserIdAndAchievementCategoryIdAndHabitId(userId, categoryId, habitId);
        return userAction != null ? modelMapper.map(userAction, UserActionVO.class) : null;
    }

    /**
     * {@inheritDoc}
     *
     * @author Oksana Spodaryk
     */
    @Override
    public UserActionVO createUserAction(Long userId, Long categoryId, Long habitId) {
        UserAction userAction = UserAction.builder()
            .user(userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + userId)))
            .count(0)
            .achievementCategory(achievementCategoryRepo.findById(categoryId).orElseThrow(
                () -> new NotFoundException(ErrorMessage.ACHIEVEMENT_CATEGORY_NOT_FOUND_BY_ID + categoryId)))
            .build();
        if (habitId != null) {
            userAction.setHabit(habitRepo.findById(habitId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId)));
        }
        userActionRepo.save(userAction);
        return modelMapper.map(userAction, UserActionVO.class);
    }
}
