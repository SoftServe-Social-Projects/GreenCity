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
    private RestClient restClient;
    private HabitAssignRepo habitAssignRepo;

    /**
     * {@inheritDoc}
     *
     * @author Bogdan Kuzenko.
     */
    @Transactional
    @Override
    public List<CustomToDoListItemResponseDto> save(List<CustomToDoListItemSaveRequestDto> dtoList, Long userId,
                                                    Long habitAssignId) {
        UserVO userVO = restClient.findById(userId);
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitAssignId));
        User user = modelMapper.map(userVO, User.class);
        List<String> errorMessages = findDuplicates(dtoList, user, habitAssign.getHabit());
        if (!errorMessages.isEmpty()) {
            throw new CustomToDoListItemNotSavedException(
                ErrorMessage.CUSTOM_TO_DO_LIST_ITEM_WHERE_NOT_SAVED + errorMessages);
        }
        List<CustomToDoListItem> items = user.getCustomToDoListItems();
        for (CustomToDoListItem item : items) {
            item.setHabit(habitAssign.getHabit());
        }
        customToDoListItemRepo.saveAll(items);
        return user.getCustomToDoListItems().stream()
            .map(customToDoListItem -> modelMapper.map(customToDoListItem,
                CustomToDoListItemResponseDto.class))
            .toList();
    }

    /**
     * Method for finding duplicates {@link CustomToDoListItem} in user data before
     * saving.
     *
     * @param dto  {@link CustomToDoListItemSaveRequestDto}`s for saving and finding
     *             duplicates.
     * @param user {@link User} for whom to-do list item are will saving.
     * @return list with the text of {@link CustomToDoListItem} which is duplicated.
     * @author Bogdan Kuzenko.
     */
    private List<String> findDuplicates(List<CustomToDoListItemSaveRequestDto> dto,
        User user, Habit habit) {
        List<String> errorMessages = new ArrayList<>();
        for (CustomToDoListItemSaveRequestDto el : dto) {
            CustomToDoListItem customToDoListItem = modelMapper.map(el, CustomToDoListItem.class);
            List<CustomToDoListItem> duplicate = user.getCustomToDoListItems().stream()
                .filter(o -> o.getText().equals(customToDoListItem.getText()) && o.getHabit().getId().equals(habit.getId())).toList();
            if (duplicate.isEmpty()) {
                customToDoListItem.setUser(user);
                customToDoListItem.setHabit(habit);
                user.getCustomToDoListItems().add(customToDoListItem);
                habit.setCustomToDoListItems(user.getCustomToDoListItems());
            } else {
                errorMessages.add(customToDoListItem.getText());
            }
        }
        return errorMessages;
    }

    /**
     * {@inheritDoc}
     *
     * @author Bogdan Kuzenko.
     */
    @Transactional
    @Override
    public CustomToDoListItemResponseDto updateItemStatus(Long userId, Long itemId, String itemStatus) {
        CustomToDoListItem customToDoListItem =
            customToDoListItemRepo.findByUserIdAndItemId(userId, itemId);
        if (Arrays.stream(ToDoListItemStatus.values()).map(Enum::name).toList().contains(itemStatus.toUpperCase())) {
            customToDoListItem.setStatus(ToDoListItemStatus.valueOf(itemStatus.toUpperCase()));
            return modelMapper.map(customToDoListItemRepo.save(customToDoListItem),
                CustomToDoListItemResponseDto.class);
        }
        throw new BadRequestException(ErrorMessage.INCORRECT_INPUT_ITEM_STATUS);
    }

    /**
     * {@inheritDoc}
     *
     * @author Volodia Lesko.
     */
    @Override
    public void updateItemStatusToDone(Long userId, Long itemId) {
        Long userToDoListItemId = userToDoListItemRepo.getCustomToDoItemIdByUserAndItemId(userId, itemId)
            .orElseThrow(() -> new NotFoundException(CUSTOM_TO_DO_LIST_ITEM_NOT_FOUND_BY_ID + " " + itemId));
        UserToDoListItem userToDoListItem = userToDoListItemRepo.getReferenceById(userToDoListItemId);
        userToDoListItem.setStatus(UserToDoListItemStatus.DONE);
        userToDoListItemRepo.save(userToDoListItem);
    }

    /**
     * {@inheritDoc}
     *
     * @author Bogdan Kuzenko.
     */
    @Transactional
    @Override
    public List<Long> bulkDelete(List<Long> ids) {
        List<Long> deleted = new ArrayList<>();
        for (Long id : ids) {
            deleted.add(delete(id));
        }
        return deleted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CustomToDoListItemResponseDto> findAllAvailableCustomToDoListItems(Long userId, Long habitId) {
        List<CustomToDoListItem> defaultCustomItems = customToDoListItemRepo.getAllCustomToDoListItemIdByHabitIdIsContained(habitId)
                .stream()
                .map(id -> customToDoListItemRepo.getReferenceById(id))
                .toList();
        List<CustomToDoListItem> customItemsByUser = customToDoListItemRepo.findAllAvailableCustomToDoListItemsForUserId(userId, habitId);
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

    /**
     * Method for deleting custom to-do list item by id.
     *
     * @param id {@link CustomToDoListItem} id.
     * @return id of deleted {@link CustomToDoListItem}
     * @author Bogdan Kuzenko.
     */
    private Long delete(Long id) {
        try {
            customToDoListItemRepo.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(CUSTOM_TO_DO_LIST_ITEM_NOT_FOUND_BY_ID + " " + id);
        }
        return id;
    }

    /**
     * Method returns all user's custom to-do items by status(if is defined).
     *
     * @param userId user id.
     * @return list of {@link CustomToDoListItemVO}
     * @author Max Bohonko.
     */
    @Override
    public List<CustomToDoListItemResponseDto> findAllUsersCustomToDoListItemsByStatus(Long userId,
        String status) {
        List<CustomToDoListItem> customToDoListItems;
        if (status != null
            && Arrays.stream(ToDoListItemStatus.values())
                .anyMatch(itemStatus -> itemStatus.toString().equals(status))) {
            customToDoListItems = customToDoListItemRepo.findAllByUserIdAndStatus(userId, status);
        } else {
            customToDoListItems = customToDoListItemRepo.findAllByUserId(userId);
        }
        return customToDoListItems.stream()
            .map(item -> modelMapper.map(item, CustomToDoListItemResponseDto.class)).toList();
    }
}
