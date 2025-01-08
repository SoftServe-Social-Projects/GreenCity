package greencity.service;

import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
import greencity.dto.todolistitem.BulkSaveCustomToDoListItemDto;
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
import org.modelmapper.TypeToken;
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
    public List<CustomToDoListItemResponseDto> save(BulkSaveCustomToDoListItemDto bulkSave, Long userId,
        Long habitAssignId) {
        UserVO userVO = restClient.findById(userId);
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitAssignId));
        User user = modelMapper.map(userVO, User.class);
        List<CustomToDoListItemSaveRequestDto> dto = bulkSave.getCustomToDoListItemSaveRequestDtoList();
        List<String> errorMessages = findDuplicates(dto, user, habitAssign.getHabit());
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
            .collect(Collectors.toList());
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
                .filter(o -> o.getText().equals(customToDoListItem.getText())).toList();
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
    public List<CustomToDoListItemResponseDto> findAll() {
        return customToDoListItemRepo.findAll().stream()
            .map(customToDoListItem -> modelMapper.map(customToDoListItem,
                CustomToDoListItemResponseDto.class))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     *
     * @author Bogdan Kuzenko.
     */
    @Transactional
    @Override
    public CustomToDoListItemResponseDto findById(Long id) {
        return modelMapper.map(findOne(id), CustomToDoListItemResponseDto.class);
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
        if (customToDoListItem == null) {
            throw new NotFoundException(CUSTOM_TO_DO_LIST_ITEM_NOT_FOUND_BY_ID);
        }
        if (itemStatus.equalsIgnoreCase(ToDoListItemStatus.DONE.name())) {
            customToDoListItem.setStatus(ToDoListItemStatus.DONE);
            return modelMapper.map(customToDoListItemRepo.save(customToDoListItem),
                CustomToDoListItemResponseDto.class);
        }
        if (itemStatus.equalsIgnoreCase(ToDoListItemStatus.ACTIVE.name())) {
            customToDoListItem.setStatus(ToDoListItemStatus.ACTIVE);
            return modelMapper.map(customToDoListItemRepo.save(customToDoListItem),
                CustomToDoListItemResponseDto.class);
        }
        if (itemStatus.equalsIgnoreCase(ToDoListItemStatus.DISABLED.name())) {
            customToDoListItem.setStatus(ToDoListItemStatus.DISABLED);
            return modelMapper.map(customToDoListItemRepo.save(customToDoListItem),
                CustomToDoListItemResponseDto.class);
        }
        if (itemStatus.equalsIgnoreCase(ToDoListItemStatus.INPROGRESS.name())) {
            customToDoListItem.setStatus(ToDoListItemStatus.INPROGRESS);
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
        Long userToDoListItemId = userToDoListItemRepo.getByUserAndItemId(userId, itemId)
            .orElseThrow(() -> new NotFoundException(CUSTOM_TO_DO_LIST_ITEM_NOT_FOUND_BY_ID + " " + itemId));
        UserToDoListItem userToDoListItem = userToDoListItemRepo.getReferenceById(userToDoListItemId);
        userToDoListItem.setStatus(ToDoListItemStatus.DONE);
        userToDoListItemRepo.save(userToDoListItem);
    }

    /**
     * {@inheritDoc}
     *
     * @author Bogdan Kuzenko.
     */
    @Transactional
    @Override
    public List<CustomToDoListItemResponseDto> findAllByUserAndHabit(Long userId, Long habitId) {
        List<CustomToDoListItemResponseDto> customToDoListItems =
            customToDoListItemRepo.findAllByUserIdAndHabitId(userId, habitId).stream()
                .map(customToDoListItem -> modelMapper.map(customToDoListItem,
                    CustomToDoListItemResponseDto.class))
                .collect(Collectors.toList());
        if (!customToDoListItems.isEmpty()) {
            return customToDoListItems;
        } else {
            throw new NotFoundException(ErrorMessage.CUSTOM_TO_DO_LIST_ITEM_NOT_FOUND);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Bogdan Kuzenko.
     */
    @Transactional
    @Override
    public List<Long> bulkDelete(String ids) {
        List<Long> arrayIds = Arrays
            .stream(ids.split(","))
            .map(Long::valueOf)
            .toList();

        List<Long> deleted = new ArrayList<>();
        for (Long id : arrayIds) {
            deleted.add(delete(id));
        }
        return deleted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CustomToDoListItemResponseDto> findAllAvailableCustomToDoListItems(Long userId, Long habitId) {
        return modelMapper.map(
            customToDoListItemRepo.findAllAvailableCustomToDoListItemsForUserId(userId, habitId),
            new TypeToken<List<CustomToDoListItemResponseDto>>() {
            }.getType());
    }

    @Override
    public List<CustomToDoListItemResponseDto> findAllCustomToDoListItemsWithStatusInProgress(Long userId,
        Long habitId) {
        return customToDoListItemRepo
            .findAllCustomToDoListItemsForUserIdAndHabitIdInProgress(userId, habitId)
            .stream()
            .map(customToDoListItem -> modelMapper.map(customToDoListItem,
                CustomToDoListItemResponseDto.class))
            .collect(Collectors.toList());
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

        Long habitId = habitAssign.getHabit().getId();

        List<CustomToDoListItem> customToDoListItemList =
            customToDoListItemRepo.findAllAvailableCustomToDoListItemsForUserId(userId, habitId);

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
     * Method for get one custom to-do list item by id.
     *
     * @param id a value of {@link Long}
     * @return {@link CustomToDoListItem}
     * @author Bogdan Kuzenko.
     */
    private CustomToDoListItem findOne(Long id) {
        return customToDoListItemRepo.findById(id)
            .orElseThrow(() -> new NotFoundException(CUSTOM_TO_DO_LIST_ITEM_NOT_FOUND_BY_ID + " " + id));
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
            .map(item -> modelMapper.map(item, CustomToDoListItemResponseDto.class)).collect(Collectors.toList());
    }
}
