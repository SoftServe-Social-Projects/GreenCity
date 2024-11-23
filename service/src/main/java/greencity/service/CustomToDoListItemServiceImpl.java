package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.entity.CustomToDoListItem;
import greencity.entity.HabitAssign;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.repository.CustomToDoListItemRepo;
import greencity.repository.HabitAssignRepo;
import java.util.ArrayList;
import java.util.List;
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
    private ModelMapper modelMapper;
    private HabitAssignRepo habitAssignRepo;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CustomToDoListItemResponseDto> findAllHabitCustomToDoList(Long userId, Long habitId) {
        List<CustomToDoListItem> defaultCustomItems =
            customToDoListItemRepo.getAllCustomToDoListItemIdByHabitIdIsContained(habitId)
                .stream()
                .map(id -> customToDoListItemRepo.getReferenceById(id))
                .toList();
        List<CustomToDoListItem> customItemsByUser =
            customToDoListItemRepo.findAllAvailableCustomToDoListItemsForUserId(userId, habitId);
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
        List<CustomToDoListItemResponseDto> allHabitCustomItems = findAllHabitCustomToDoList(userId, habitAssignId);
        List<CustomToDoListItemResponseDto> addedItems = getCustomToDoListByHabitAssignId(userId, habitAssignId);
        allHabitCustomItems.removeAll(addedItems);
        return allHabitCustomItems;
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

        if (!habitAssign.getUser().getId().equals(userId)) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }

        List<CustomToDoListItem> customToDoListItemList =
            customToDoListItemRepo.findAllByHabitAssignId(habitAssignId);

        return customToDoListItemList
            .stream().map(item -> modelMapper.map(item, CustomToDoListItemResponseDto.class))
            .toList();
    }
}
