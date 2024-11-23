package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.user.UserToDoListItemRequestDto;
import greencity.dto.user.UserToDoListItemRequestWithStatusDto;
import greencity.dto.user.UserToDoListItemResponseDto;
import greencity.entity.CustomToDoListItem;
import greencity.entity.HabitAssign;
import greencity.entity.UserToDoListItem;
import greencity.entity.localization.ToDoListItemTranslation;
import greencity.enums.UserToDoListItemStatus;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.repository.CustomToDoListItemRepo;
import greencity.repository.HabitAssignRepo;
import greencity.repository.ToDoListItemTranslationRepo;
import greencity.repository.UserToDoListItemRepo;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserToDoListItemResponseDto> findAllForHabitAssign(Long habitAssignId, Long userId, String language) {
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId).orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId));

        if (!habitAssign.getUser().getId().equals(userId)) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }

        return getItemsByHabitAssignId(habitAssignId, language);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public List<UserToDoListItemResponseDto> saveUserToDoListItems(Long habitAssignId, List<UserToDoListItemRequestDto> userToDoListItems, Long userId, String language) {
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId).orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId));

        if (!habitAssign.getUser().getId().equals(userId)) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }

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
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId).orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId));

        if (!habitAssign.getUser().getId().equals(userId)) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }

        List<UserToDoListItem> itemsToDelete = userToDoListItemRepo.findAllById(itemIds);
        itemsToDelete.forEach(item -> {
            if (!item.getHabitAssign().getId().equals(habitAssignId)) {
                throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
            }
        });
        userToDoListItemRepo.deleteAll(itemsToDelete);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public List<UserToDoListItemResponseDto> changeStatusesUserToDoListItems(Long habitAssignId, List<UserToDoListItemRequestWithStatusDto> userToDoListItems, Long userId, String language) {
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId).orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId));

        if (!habitAssign.getUser().getId().equals(userId)) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }

        List<UserToDoListItem> toSave = new ArrayList<>();
        for (UserToDoListItemRequestWithStatusDto userItemRequest : userToDoListItems) {
            UserToDoListItem userItem;
            if (userItemRequest.getIsCustomItem()) {
               userItem = userToDoListItemRepo.getCustomToDoItemIdByHabitAssignIdAndItemId(habitAssignId, userItemRequest.getTargetId())
                        .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_TO_DO_LIST_ITEM_NOT_FOUND));
            } else {
                userItem = userToDoListItemRepo.getToDoItemIdByHabitAssignIdAndItemId(habitAssignId, userItemRequest.getTargetId())
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
        if (userToDoListItem.getIsCustomItem()) {
            CustomToDoListItem customItem = customToDoListItemRepo.getReferenceById(userToDoListItem.getTargetId());
            responseDto.setText(customItem.getText());
        } else {
            ToDoListItemTranslation itemTranslation = toDoListItemTranslationRepo.findByLangAndToDoListItemId(language, userToDoListItem.getTargetId());
            responseDto.setText(itemTranslation.getContent());
        }
        return responseDto;
    }

    private List<UserToDoListItemResponseDto> getItemsByHabitAssignId(Long habitAssignId, String language) {
        return userToDoListItemRepo.findAllByHabitAssingId(habitAssignId).stream()
                .map(userToDoListItem -> buildResponseDto(userToDoListItem, language))
                .toList();
    }
}
