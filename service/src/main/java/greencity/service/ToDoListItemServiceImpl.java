package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.dto.todolistitem.ToDoListItemDto;
import greencity.dto.todolistitem.ToDoListItemManagementDto;
import greencity.dto.todolistitem.ToDoListItemPostDto;
import greencity.dto.todolistitem.ToDoListItemRequestDto;
import greencity.dto.todolistitem.ToDoListItemResponseDto;
import greencity.dto.todolistitem.ToDoListItemViewDto;
import greencity.dto.user.UserToDoListItemResponseDto;
import greencity.entity.ToDoListItem;
import greencity.entity.HabitAssign;
import greencity.entity.UserToDoListItem;
import greencity.entity.localization.ToDoListItemTranslation;
import greencity.enums.ToDoListItemStatus;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotDeletedException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.ToDoListItemNotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.exception.exceptions.UserHasNoToDoListItemsException;
import greencity.exception.exceptions.UserToDoListItemStatusNotUpdatedException;
import greencity.exception.exceptions.WrongIdException;
import greencity.filters.ToDoListItemSpecification;
import greencity.filters.SearchCriteria;
import greencity.repository.ToDoListItemRepo;
import greencity.repository.ToDoListItemTranslationRepo;
import greencity.repository.HabitAssignRepo;
import greencity.repository.UserToDoListItemRepo;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;

@RequiredArgsConstructor
@Service
public class ToDoListItemServiceImpl implements ToDoListItemService {
    private final ToDoListItemTranslationRepo toDoListItemTranslationRepo;
    private final ToDoListItemRepo toDoListItemRepo;
    private final ModelMapper modelMapper;
    private final UserToDoListItemRepo userToDoListItemRepo;
    private final HabitAssignRepo habitAssignRepo;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ToDoListItemDto> findAll(String language) {
        return toDoListItemTranslationRepo
            .findAllByLanguageCode(language)
            .stream()
            .map(g -> modelMapper.map(g, ToDoListItemDto.class))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LanguageTranslationDTO> saveToDoListItem(ToDoListItemPostDto item) {
        ToDoListItem savedToDoListItem = modelMapper.map(item, ToDoListItem.class);
        savedToDoListItem.getTranslations().forEach(a -> a.setToDoListItem(savedToDoListItem));
        toDoListItemRepo.save(savedToDoListItem);
        return modelMapper.map(savedToDoListItem.getTranslations(),
            new TypeToken<List<LanguageTranslationDTO>>() {
            }.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LanguageTranslationDTO> update(ToDoListItemPostDto toDoListItemPostDto) {
        Optional<ToDoListItem> optionalItem =
            toDoListItemRepo.findById(toDoListItemPostDto.getToDoListItem().getId());
        if (optionalItem.isPresent()) {
            ToDoListItem updatedToDoListItem = optionalItem.get();
            updateTranslations(updatedToDoListItem.getTranslations(), toDoListItemPostDto.getTranslations());
            toDoListItemRepo.save(updatedToDoListItem);
            return modelMapper.map(updatedToDoListItem.getTranslations(),
                new TypeToken<List<LanguageTranslationDTO>>() {
                }.getType());
        } else {
            throw new ToDoListItemNotFoundException(ErrorMessage.TO_DO_LIST_ITEM_NOT_FOUND_BY_ID);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ToDoListItemResponseDto findToDoListItemById(Long id) {
        Optional<ToDoListItem> item = toDoListItemRepo.findById(id);
        if (item.isPresent()) {
            return modelMapper.map(item.get(), ToDoListItemResponseDto.class);
        } else {
            throw new ToDoListItemNotFoundException(ErrorMessage.TO_DO_LIST_ITEM_NOT_FOUND_BY_ID);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long delete(Long itemId) {
        try {
            toDoListItemRepo.deleteById(itemId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotDeletedException(ErrorMessage.TO_DO_LIST_ITEM_NOT_DELETED);
        }
        return itemId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableAdvancedDto<ToDoListItemManagementDto> findToDoListItemsForManagementByPage(
        Pageable pageable) {
        Page<ToDoListItem> toDoListItems = toDoListItemRepo.findAll(pageable);
        List<ToDoListItemManagementDto> toDoListItemManagementDtos =
            toDoListItems.getContent().stream()
                .map(item -> modelMapper.map(item, ToDoListItemManagementDto.class))
                .collect(Collectors.toList());
        return getPagebleAdvancedDto(toDoListItemManagementDtos, toDoListItems);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> deleteAllToDoListItemsByListOfId(List<Long> listId) {
        listId.forEach(this::delete);
        return listId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableAdvancedDto<ToDoListItemManagementDto> searchBy(Pageable paging, String query) {
        Page<ToDoListItem> toDoListItems = toDoListItemRepo.searchBy(paging, query);
        List<ToDoListItemManagementDto> toDoListItemManagementDtos = toDoListItems.stream()
            .map(item -> modelMapper.map(item, ToDoListItemManagementDto.class))
            .collect(Collectors.toList());
        return getPagebleAdvancedDto(toDoListItemManagementDtos, toDoListItems);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableAdvancedDto<ToDoListItemManagementDto> getFilteredDataForManagementByPage(Pageable pageable,
        ToDoListItemViewDto dto) {
        Page<ToDoListItem> pages = toDoListItemRepo.findAll(getSpecification(dto), pageable);
        return getPagesFilteredPages(pages);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public List<UserToDoListItemResponseDto> saveUserToDoListItems(Long userId, Long habitId,
        List<ToDoListItemRequestDto> dtoList,
        String language) {
        if (dtoList != null) {
            Optional<HabitAssign> habitAssign = habitAssignRepo.findByHabitIdAndUserId(habitId, userId);
            if (habitAssign.isPresent()) {
                saveToDoListItemsForHabitAssign(habitAssign.get(), dtoList);
            } else {
                throw new UserHasNoToDoListItemsException(ErrorMessage.USER_HAS_NO_TO_DO_LIST_ITEMS);
            }
        }
        return getUserToDoList(userId, habitId, language);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public List<UserToDoListItemResponseDto> getUserToDoList(Long userId, Long habitId, String language) {
        Optional<HabitAssign> habitAssign = habitAssignRepo.findByHabitIdAndUserId(habitId, userId);
        if (habitAssign.isPresent()) {
            List<UserToDoListItemResponseDto> itemsDtos = getAllUserToDoListItems(habitAssign.get());
            itemsDtos.forEach(el -> setTextForUserToDoListItem(el, language));
            return itemsDtos;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserToDoListItemResponseDto> getUserToDoListByHabitAssignId(Long userId, Long habitAssignId,
        String language) {
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId));

        if (!habitAssign.getUser().getId().equals(userId)) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }

        List<UserToDoListItemResponseDto> itemsDtos = getAllUserToDoListItems(habitAssign);
        itemsDtos.forEach(el -> setTextForUserToDoListItem(el, language));
        return itemsDtos;
    }

    @Transactional
    @Override
    public List<UserToDoListItemResponseDto> getUserToDoListItemsByHabitAssignIdAndStatusInProgress(
        Long habitAssignId, String language) {
        return userToDoListItemRepo
            .findUserToDoListItemsByHabitAssignIdAndStatusInProgress(habitAssignId)
            .stream()
            .map(userToDoListItem -> converterUserToDoListItemResponseDto(userToDoListItem, language))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUserToDoListItemByItemIdAndUserIdAndHabitId(Long itemId, Long userId, Long habitId) {
        userToDoListItemRepo.deleteByToDoListItemIdAndHabitAssignId(itemId,
            getHabitAssignByHabitIdAndUserIdAndSuspendedFalse(userId, habitId).getId());
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public UserToDoListItemResponseDto updateUserToDoListItemStatus(Long userId, Long itemId, String language) {
        UserToDoListItem userToDoListItem;
        userToDoListItem = userToDoListItemRepo.getReferenceById(itemId);
        if (isActive(userToDoListItem)) {
            changeStatusToDone(userToDoListItem);
        } else {
            throw new UserToDoListItemStatusNotUpdatedException(
                ErrorMessage.USER_TO_DO_LIST_ITEMS_STATUS_IS_ALREADY_DONE + userToDoListItem.getId());
        }
        UserToDoListItemResponseDto updatedUserToDoListItem =
            modelMapper.map(userToDoListItem, UserToDoListItemResponseDto.class);
        setTextForUserToDoListItem(updatedUserToDoListItem, language);
        return updatedUserToDoListItem;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public List<UserToDoListItemResponseDto> updateUserToDoListItemStatus(Long userId,
        Long userToDoListItemId,
        String language,
        String status) {
        String statusUpperCase = status.toUpperCase();
        List<UserToDoListItem> userToDoListItems =
            userToDoListItemRepo.getAllByUserToDoListIdAndUserId(userToDoListItemId, userId);
        if (userToDoListItems == null || userToDoListItems.isEmpty()) {
            throw new NotFoundException(ErrorMessage.USER_TO_DO_LIST_ITEM_NOT_FOUND_BY_USER_ID);
        }
        if (Arrays.stream(ToDoListItemStatus.values()).noneMatch(s -> s.name().equalsIgnoreCase(statusUpperCase))) {
            throw new BadRequestException(ErrorMessage.INCORRECT_INPUT_ITEM_STATUS);
        }
        userToDoListItems.forEach(u -> u.setStatus(ToDoListItemStatus.valueOf(statusUpperCase)));
        userToDoListItemRepo.saveAll(userToDoListItems);
        List<UserToDoListItemResponseDto> userToDoListItemResponseDtoList =
            userToDoListItems.stream()
                .map(u -> modelMapper.map(u, UserToDoListItemResponseDto.class))
                .collect(Collectors.toList());
        userToDoListItemResponseDtoList.forEach(u -> setTextForUserToDoListItem(u, language));
        return userToDoListItemResponseDtoList;
    }

    /**
     * {@inheritDoc}
     *
     * @author Bogdan Kuzenko
     */
    @Transactional
    @Override
    public List<Long> deleteUserToDoListItems(String ids) {
        List<Long> arrayId = Arrays.stream(ids.split(","))
            .map(Long::valueOf)
            .toList();

        List<Long> deleted = new ArrayList<>();
        for (Long id : arrayId) {
            deleted.add(deleteUserToDoListItem(id));
        }
        return deleted;
    }

    @Override
    public List<ToDoListItemManagementDto> getToDoListByHabitId(Long habitId) {
        List<Long> idList =
            toDoListItemRepo.getAllToDoListItemIdByHabitIdISContained(habitId);
        List<ToDoListItem> toDoListItems;
        if (!idList.isEmpty()) {
            toDoListItems = toDoListItemRepo.getToDoListByListOfId(idList);
        } else {
            toDoListItems = new ArrayList<>();
        }

        return toDoListItems.stream()
            .map(listItem -> modelMapper.map(listItem, ToDoListItemManagementDto.class))
            .collect(Collectors.toList());
    }

    @Override
    public PageableAdvancedDto<ToDoListItemManagementDto> findAllToDoListItemsForManagementPageNotContained(
        Long habitId,
        Pageable pageable) {
        List<Long> items =
            toDoListItemRepo.getAllToDoListItemsByHabitIdNotContained(habitId);
        Page<ToDoListItem> toDoListItems =
            toDoListItemRepo
                .getToDoListByListOfIdPageable(items, pageable);
        List<ToDoListItemManagementDto> toDoListItemManagementDtos =
            toDoListItems.getContent().stream()
                .map(item -> modelMapper.map(item, ToDoListItemManagementDto.class))
                .collect(Collectors.toList());
        return getPagebleAdvancedDto(toDoListItemManagementDtos, toDoListItems);
    }

    @Override
    public List<ToDoListItemDto> findInProgressByUserIdAndLanguageCode(Long userId, String code) {
        List<ToDoListItemDto> toDoListItemDtos = toDoListItemRepo
            .findInProgressByUserIdAndLanguageCode(userId, code)
            .stream()
            .map(toDoListItemTranslation -> modelMapper.map(toDoListItemTranslation, ToDoListItemDto.class))
            .collect(Collectors.toList());
        toDoListItemDtos.forEach(x -> x.setStatus(ToDoListItemStatus.INPROGRESS.toString()));
        return toDoListItemDtos;
    }

    private void updateTranslations(List<ToDoListItemTranslation> oldTranslations,
        List<LanguageTranslationDTO> newTranslations) {
        oldTranslations.forEach(itemTranslation -> itemTranslation.setContent(newTranslations.stream()
            .filter(newTranslation -> newTranslation.getLanguage().getCode()
                .equals(itemTranslation.getLanguage().getCode()))
            .findFirst().get()
            .getContent()));
    }

    private PageableAdvancedDto<ToDoListItemManagementDto> getPagebleAdvancedDto(
        List<ToDoListItemManagementDto> toDoListItemManagementDtos, Page<ToDoListItem> toDoListItems) {
        return new PageableAdvancedDto<>(
            toDoListItemManagementDtos,
            toDoListItems.getTotalElements(),
            toDoListItems.getPageable().getPageNumber(),
            toDoListItems.getTotalPages(),
            toDoListItems.getNumber(),
            toDoListItems.hasPrevious(),
            toDoListItems.hasNext(),
            toDoListItems.isFirst(),
            toDoListItems.isLast());
    }

    /**
     * * This method used for build {@link SearchCriteria} depends on
     * {@link ToDoListItemDto}.
     *
     * @param dto used for receive parameters for filters from UI.
     * @return {@link SearchCriteria}.
     */
    private List<SearchCriteria> buildSearchCriteria(ToDoListItemViewDto dto) {
        List<SearchCriteria> criteriaList = new ArrayList<>();
        setValueIfNotEmpty(criteriaList, "id", dto.getId());
        setValueIfNotEmpty(criteriaList, "content", dto.getContent());
        return criteriaList;
    }

    /**
     * Returns {@link ToDoListItemSpecification} for entered filter parameters.
     *
     * @param toDoListItemViewDto contains data from filters
     */
    private ToDoListItemSpecification getSpecification(ToDoListItemViewDto toDoListItemViewDto) {
        List<SearchCriteria> searchCriteria = buildSearchCriteria(toDoListItemViewDto);
        return new ToDoListItemSpecification(searchCriteria);
    }

    /**
     * Method that adds new {@link SearchCriteria}.
     *
     * @param searchCriteria - list of existing {@link SearchCriteria}
     * @param key            - key of field
     * @param value          - value of field
     */
    private void setValueIfNotEmpty(List<SearchCriteria> searchCriteria, String key, String value) {
        if (StringUtils.hasLength(value)) {
            searchCriteria.add(SearchCriteria.builder()
                .key(key)
                .type(key)
                .value(value)
                .build());
        }
    }

    private PageableAdvancedDto<ToDoListItemManagementDto> getPagesFilteredPages(Page<ToDoListItem> pages) {
        List<ToDoListItemManagementDto> toDoListItemManagementDtos = pages.getContent()
            .stream()
            .map(item -> modelMapper.map(item, ToDoListItemManagementDto.class))
            .collect(Collectors.toList());
        return getPagebleAdvancedDto(toDoListItemManagementDtos, pages);
    }

    /**
     * Method save user to-do list item with item dictionary.
     *
     * @param dtoList list {@link ToDoListItemRequestDto} for saving
     * @author Dmytro Khonko
     */
    private void saveToDoListItemsForHabitAssign(HabitAssign habitAssign,
        List<ToDoListItemRequestDto> dtoList) {
        for (ToDoListItemRequestDto el : dtoList) {
            saveUserToDoListItemForToDoList(el, habitAssign);
        }
    }

    private void saveUserToDoListItemForToDoList(ToDoListItemRequestDto dto, HabitAssign habitAssign) {
        if (isAssignedToHabit(dto, habitAssign)) {
            if (isAssignedToUser(dto, habitAssign)) {
                saveUserToDoListItem(dto, habitAssign);
            } else {
                throw new WrongIdException(ErrorMessage.TO_DO_LIST_ITEM_ALREADY_SELECTED + dto.getId());
            }
        } else {
            throw new NotFoundException(ErrorMessage.TO_DO_LIST_ITEM_NOT_ASSIGNED_FOR_THIS_HABIT + dto.getId());
        }
    }

    private boolean isAssignedToHabit(ToDoListItemRequestDto dto, HabitAssign habitAssign) {
        List<Long> ids = userToDoListItemRepo.getToDoListItemsIdForHabit(habitAssign.getHabit().getId());
        return ids.contains(dto.getId());
    }

    private boolean isAssignedToUser(ToDoListItemRequestDto dto, HabitAssign habitAssign) {
        List<Long> assignedIds = userToDoListItemRepo.getAllAssignedToDoListItems(habitAssign.getId());
        return !assignedIds.contains(dto.getId());
    }

    private void saveUserToDoListItem(ToDoListItemRequestDto dto, HabitAssign habitAssign) {
        UserToDoListItem userToDoListItem = modelMapper.map(dto, UserToDoListItem.class);
        userToDoListItem.setHabitAssign(habitAssign);
        habitAssign.getUserToDoListItems().add(userToDoListItem);
        userToDoListItemRepo.saveAll(habitAssign.getUserToDoListItems());
    }

    private List<UserToDoListItemResponseDto> getAllUserToDoListItems(HabitAssign habitAssign) {
        return userToDoListItemRepo
            .findAllByHabitAssingId(habitAssign.getId())
            .stream()
            .map(userToDoListItem -> modelMapper.map(userToDoListItem, UserToDoListItemResponseDto.class))
            .collect(Collectors.toList());
    }

    /**
     * Method for setting text either for UserToDoListItem with localization.
     *
     * @param dto {@link ToDoListItemDto}
     */
    private void setTextForUserToDoListItem(UserToDoListItemResponseDto dto, String language) {
        String text =
            toDoListItemTranslationRepo.findByLangAndUserToDoListItemId(language, dto.getId()).getContent();
        dto.setText(text);
    }

    /**
     * Method converts UserToDoListItem to UserToDoListItemResponseDto and sets text
     * for UserToDoListItemResponseDto with localization.
     *
     * @param userToDoListItem {@link UserToDoListItem}
     * @param language         {@link String}
     */
    private UserToDoListItemResponseDto converterUserToDoListItemResponseDto(
        UserToDoListItem userToDoListItem, String language) {
        UserToDoListItemResponseDto dto =
            modelMapper.map(userToDoListItem, UserToDoListItemResponseDto.class);
        setTextForUserToDoListItem(dto, language);
        return dto;
    }

    private HabitAssign getHabitAssignByHabitIdAndUserIdAndSuspendedFalse(Long userId, Long habitId) {
        Optional<HabitAssign> habitAssign = habitAssignRepo.findByHabitIdAndUserId(habitId, userId);
        if (habitAssign.isPresent()) {
            return habitAssign.get();
        }
        throw new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID);
    }

    private boolean isActive(UserToDoListItem userToDoListItem) {
        try {
            if (ToDoListItemStatus.ACTIVE.equals(userToDoListItem.getStatus())) {
                return true;
            }
        } catch (Exception e) {
            throw new UserToDoListItemStatusNotUpdatedException(
                ErrorMessage.USER_TO_DO_LIST_ITEM_NOT_FOUND + userToDoListItem.getId());
        }
        return false;
    }

    private void changeStatusToDone(UserToDoListItem userToDoListItem) {
        userToDoListItem.setStatus(ToDoListItemStatus.DONE);
        userToDoListItem.setDateCompleted(LocalDateTime.now());
        userToDoListItemRepo.save(userToDoListItem);
    }

    /**
     * Method for deleting user to-do list item by id.
     *
     * @param id {@link UserToDoListItem} id.
     * @return id of deleted {@link UserToDoListItem}
     * @author Bogdan Kuzenko
     */
    private Long deleteUserToDoListItem(Long id) {
        UserToDoListItem userToDoListItem = userToDoListItemRepo
            .findById(id).orElseThrow(() -> new NotFoundException(ErrorMessage.USER_TO_DO_LIST_ITEM_NOT_FOUND + id));
        userToDoListItemRepo.delete(userToDoListItem);
        return id;
    }
}
