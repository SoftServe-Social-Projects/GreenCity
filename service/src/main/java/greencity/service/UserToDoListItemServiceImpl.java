package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.user.UserToDoListItemRequestDto;
import greencity.dto.user.UserToDoListItemRequestWithStatusDto;
import greencity.dto.user.UserToDoListItemResponseDto;
import greencity.dto.user.UserVO;
import greencity.entity.CustomToDoListItem;
import greencity.entity.HabitAssign;
import greencity.entity.ToDoListItem;
import greencity.entity.UserToDoListItem;
import greencity.entity.localization.ToDoListItemTranslation;
import greencity.enums.Role;
import greencity.enums.UserToDoListItemStatus;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.repository.CustomToDoListItemRepo;
import greencity.repository.HabitAssignRepo;
import greencity.repository.ToDoListItemRepo;
import greencity.repository.ToDoListItemTranslationRepo;
import greencity.repository.UserToDoListItemRepo;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The class provides implementation of the {@code UserToDoListItemService}.
 */
@Service
@AllArgsConstructor
public class UserToDoListItemServiceImpl implements UserToDoListItemService {
    private final UserToDoListItemRepo userToDoListItemRepo;
    private final HabitAssignRepo habitAssignRepo;
    private final CustomToDoListItemRepo customToDoListItemRepo;
    private final ToDoListItemTranslationRepo toDoListItemTranslationRepo;
    private final ToDoListItemRepo toDoListItemRepo;
    private final UserService userService;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserToDoListItemResponseDto> findAllForHabitAssign(Long habitAssignId, Long userId, String language) {
        checkUserPermission(habitAssignId, userId);

        return getItemsByHabitAssignId(habitAssignId, language);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public List<UserToDoListItemResponseDto> saveUserToDoListItems(Long habitAssignId,
        List<UserToDoListItemRequestDto> userToDoListItems, Long userId, String language) {
        checkUserPermission(habitAssignId, userId);
        userToDoListItems.forEach(item -> checkItemReferenceExist(item.getTargetId(), item.getIsCustomItem()));
        HabitAssign habitAssign = habitAssignRepo.getReferenceById(habitAssignId);
        List<UserToDoListItem> toSave = userToDoListItems.stream()
            .map(userToDoListItemRequestDto -> modelMapper.map(userToDoListItemRequestDto, UserToDoListItem.class))
            .toList();
        toSave.forEach(item -> item.setStatus(UserToDoListItemStatus.INPROGRESS));
        toSave.forEach(item -> item.setHabitAssign(habitAssign));
        userToDoListItemRepo.saveAll(toSave);
        return getItemsByHabitAssignId(habitAssignId, language);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUserToDoListItems(Long habitAssignId, List<Long> itemIds, Long userId) {
        checkUserPermission(habitAssignId, userId);

        List<UserToDoListItem> itemsToDelete = userToDoListItemRepo.findAllById(itemIds);
        itemsToDelete.forEach(item -> {
            if (!item.getHabitAssign().getId().equals(habitAssignId)) {
                throw new BadRequestException(ErrorMessage.USER_TO_DO_LIST_ITEMS_NOT_RELATED_TO_PROVIDED_HABIT_ASSIGN);
            }
        });
        userToDoListItemRepo.deleteAll(itemsToDelete);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public List<UserToDoListItemResponseDto> changeStatusesUserToDoListItems(Long habitAssignId,
        List<UserToDoListItemRequestWithStatusDto> userToDoListItems, Long userId, String language) {
        checkUserPermission(habitAssignId, userId);

        List<UserToDoListItem> toSave = new ArrayList<>();
        for (UserToDoListItemRequestWithStatusDto userItemRequest : userToDoListItems) {
            UserToDoListItem userItem;
            boolean isCustomItem = userItemRequest.getIsCustomItem();
            if (isCustomItem) {
                userItem = userToDoListItemRepo
                    .getCustomToDoItemIdByHabitAssignIdAndItemId(habitAssignId, userItemRequest.getTargetId())
                    .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_TO_DO_LIST_ITEM_NOT_FOUND));
            } else {
                userItem = userToDoListItemRepo
                    .getToDoItemIdByHabitAssignIdAndItemId(habitAssignId, userItemRequest.getTargetId())
                    .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_TO_DO_LIST_ITEM_NOT_FOUND));
            }
            userItem.setStatus(userItemRequest.getStatus());
            toSave.add(userItem);
        }
        userToDoListItemRepo.saveAll(toSave);
        return getItemsByHabitAssignId(habitAssignId, language);
    }

    private UserToDoListItemResponseDto buildResponseDto(UserToDoListItem userToDoListItem, String language) {
        UserToDoListItemResponseDto responseDto = modelMapper.map(userToDoListItem, UserToDoListItemResponseDto.class);
        boolean isCustomItem = userToDoListItem.getIsCustomItem();
        if (isCustomItem) {
            CustomToDoListItem customItem = customToDoListItemRepo.getReferenceById(userToDoListItem.getTargetId());
            responseDto.setText(customItem.getText());
        } else {
            ToDoListItemTranslation itemTranslation =
                toDoListItemTranslationRepo.findByLangAndToDoListItemId(language, userToDoListItem.getTargetId());
            responseDto.setText(itemTranslation.getContent());
        }
        return responseDto;
    }

    private List<UserToDoListItemResponseDto> getItemsByHabitAssignId(Long habitAssignId, String language) {
        return userToDoListItemRepo.findAllByHabitAssingId(habitAssignId).stream()
            .map(userToDoListItem -> buildResponseDto(userToDoListItem, language))
            .toList();
    }

    private void checkUserPermission(Long habitAssignId, Long userId) {
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId));
        UserVO user = userService.findById(userId);
        if (!habitAssign.getUser().getId().equals(userId) && !user.getRole().equals(Role.ROLE_ADMIN)) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }
    }

    private void checkItemReferenceExist(Long targetId, boolean isCustom) {
        if (isCustom) {
            Optional<CustomToDoListItem> item = customToDoListItemRepo.findById(targetId);
            if (item.isEmpty()) {
                throw new NotFoundException(ErrorMessage.CUSTOM_TO_DO_LIST_ITEM_NOT_FOUND_BY_ID + targetId);
            }
        } else {
            Optional<ToDoListItem> item = toDoListItemRepo.findById(targetId);
            if (item.isEmpty()) {
                throw new NotFoundException(ErrorMessage.TO_DO_LIST_ITEM_NOT_FOUND_BY_ID + targetId);
            }
        }
    }
}
