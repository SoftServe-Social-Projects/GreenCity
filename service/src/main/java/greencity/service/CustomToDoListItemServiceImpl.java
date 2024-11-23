package greencity.service;

import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.todolistitem.CustomToDoListItemSaveRequestDto;
import greencity.dto.todolistitem.CustomToDoListItemVO;
import greencity.dto.user.UserVO;
import greencity.entity.CustomToDoListItem;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.User;
import greencity.entity.UserToDoListItem;
import greencity.enums.ToDoListItemStatus;
import greencity.enums.UserToDoListItemStatus;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.CustomToDoListItemNotSavedException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.repository.CustomToDoListItemRepo;
import greencity.repository.HabitAssignRepo;
import greencity.repository.UserToDoListItemRepo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static greencity.constant.ErrorMessage.CUSTOM_TO_DO_LIST_ITEM_NOT_FOUND_BY_ID;

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
    private UserToDoListItemRepo userToDoListItemRepo;
    private ModelMapper modelMapper;
    private HabitAssignRepo habitAssignRepo;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CustomToDoListItemResponseDto> findAllCustomToDoListItemsForHabit(Long userId, Long habitId) {
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
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CustomToDoListItemResponseDto> findAvailableCustomToDoListItemsForHabitAssign(Long userId, Long habitAssignId) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CustomToDoListItemResponseDto> findAllAvailableCustomToDoListItemsByHabitAssignId(Long userId,
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
            .collect(Collectors.toList());
    }
}
