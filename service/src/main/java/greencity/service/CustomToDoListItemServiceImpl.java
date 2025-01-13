package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.user.UserVO;
import greencity.entity.CustomToDoListItem;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.enums.Role;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.repository.CustomToDoListItemRepo;
import greencity.repository.HabitAssignRepo;
import java.util.ArrayList;
import java.util.List;
import greencity.repository.HabitRepo;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

/**
 * The class provides implementation of the {@code CustomToDoListItemService}.
 */
@Service
@AllArgsConstructor
public class CustomToDoListItemServiceImpl implements CustomToDoListItemService {
    /**
     * Autowired repository.
     */
    private CustomToDoListItemRepo customToDoListItemRepo;
    private HabitAssignRepo habitAssignRepo;
    private HabitRepo habitRepo;
    private UserService userService;
    private ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CustomToDoListItemResponseDto> findAllHabitCustomToDoList(Long userId, Long habitId) {
        Habit habit = habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId));

        List<CustomToDoListItem> defaultCustomItems =
            customToDoListItemRepo.getAllCustomToDoListItemIdByHabitIdIsContained(habit.getId())
                .stream()
                .map(id -> customToDoListItemRepo.getReferenceById(id))
                .toList();
        List<CustomToDoListItem> customItemsByUser =
            customToDoListItemRepo.findAllAvailableCustomToDoListItemsForUserId(userId, habit.getId());
        List<CustomToDoListItem> customItemsToReturn = new ArrayList<>();
        customItemsToReturn.addAll(defaultCustomItems);
        customItemsToReturn.addAll(customItemsByUser);
        return customItemsToReturn.stream()
            .map(customItem -> modelMapper.map(customItem, CustomToDoListItemResponseDto.class))
            .distinct()
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CustomToDoListItemResponseDto> findAvailableCustomToDoListForHabitAssign(Long userId,
        Long habitAssignId) {
        List<CustomToDoListItemResponseDto> addedItems = getCustomToDoListByHabitAssignId(userId, habitAssignId);
        HabitAssign habitAssign = habitAssignRepo.getReferenceById(habitAssignId);
        List<CustomToDoListItemResponseDto> allHabitCustomItems =
            findAllHabitCustomToDoList(userId, habitAssign.getHabit().getId());
        return allHabitCustomItems.stream().filter(item -> !addedItems.contains(item)).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CustomToDoListItemResponseDto> getCustomToDoListByHabitAssignId(Long userId,
        Long habitAssignId) {
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId));
        UserVO user = userService.findById(userId);
        if (!habitAssign.getUser().getId().equals(userId) && !user.getRole().equals(Role.ROLE_ADMIN)) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }

        List<CustomToDoListItem> customToDoListItemList =
            customToDoListItemRepo.findAllByHabitAssignId(habitAssignId);
        return customToDoListItemList
            .stream().map(item -> modelMapper.map(item, CustomToDoListItemResponseDto.class))
            .toList();
    }
}
