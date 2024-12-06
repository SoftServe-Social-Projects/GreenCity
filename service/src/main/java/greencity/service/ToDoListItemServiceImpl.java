package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.dto.todolistitem.ToDoListItemResponseDto;
import greencity.dto.todolistitem.ToDoListItemManagementDto;
import greencity.dto.todolistitem.ToDoListItemPostDto;
import greencity.dto.todolistitem.ToDoListItemResponseWithTranslationDto;
import greencity.dto.todolistitem.ToDoListItemViewDto;
import greencity.dto.user.UserVO;
import greencity.entity.Habit;
import greencity.entity.ToDoListItem;
import greencity.entity.HabitAssign;
import greencity.entity.localization.ToDoListItemTranslation;
import greencity.enums.Role;
import greencity.exception.exceptions.NotDeletedException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.ToDoListItemNotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.filters.ToDoListItemSpecification;
import greencity.filters.SearchCriteria;
import greencity.repository.HabitRepo;
import greencity.repository.ToDoListItemRepo;
import greencity.repository.ToDoListItemTranslationRepo;
import greencity.repository.HabitAssignRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ToDoListItemServiceImpl implements ToDoListItemService {
    private final ToDoListItemTranslationRepo toDoListItemTranslationRepo;
    private final ToDoListItemRepo toDoListItemRepo;
    private final HabitAssignRepo habitAssignRepo;
    private final HabitRepo habitRepo;
    private final UserService userService;
    private final ModelMapper modelMapper;

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
    public ToDoListItemResponseWithTranslationDto findToDoListItemById(Long id) {
        Optional<ToDoListItem> item = toDoListItemRepo.findById(id);
        if (item.isPresent()) {
            return modelMapper.map(item.get(), ToDoListItemResponseWithTranslationDto.class);
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
                .toList();
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
            .toList();
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
    @Override
    public List<ToDoListItemResponseDto> findAllHabitToDoList(Long habitId, String language) {
        Habit habit = habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId));
        List<Long> habitToDoItemIds = toDoListItemRepo.getAllToDoListItemIdByHabitIdIsContained(habit.getId());
        return habitToDoItemIds.stream()
            .map(toDoListItemRepo::getReferenceById)
            .map(toDoListItem -> {
                ToDoListItemResponseDto responseDto =
                    modelMapper.map(toDoListItem, ToDoListItemResponseDto.class);
                setTextForToDoListItem(responseDto, language);
                return responseDto;
            })
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ToDoListItemResponseDto> findAvailableToDoListForHabitAssign(Long userId, Long habitAssignId,
        String language) {
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId));
        List<ToDoListItemResponseDto> addedItems =
            getToDoListByHabitAssignId(userId, habitAssignId, language);
        List<ToDoListItemResponseDto> allActiveHabitItems =
            findAllHabitToDoList(habitAssign.getHabit().getId(), language);
        return allActiveHabitItems.stream()
            .filter(item -> !addedItems.contains(item))
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ToDoListItemResponseDto> getToDoListByHabitAssignId(Long userId, Long habitAssignId,
        String language) {
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId));
        UserVO user = userService.findById(userId);
        if (!habitAssign.getUser().getId().equals(userId) && !user.getRole().equals(Role.ROLE_ADMIN)) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }

        List<ToDoListItemResponseDto> itemsDtos = getAllToDoListItemsForUser(habitAssign);
        itemsDtos.forEach(el -> setTextForToDoListItem(el, language));
        return itemsDtos;
    }

    @Override
    public List<ToDoListItemManagementDto> getToDoListByHabitId(Long habitId) {
        List<Long> idList =
            toDoListItemRepo.getAllToDoListItemIdByHabitIdIsContained(habitId);
        List<ToDoListItem> toDoListItems;
        if (!idList.isEmpty()) {
            toDoListItems = toDoListItemRepo.getToDoListByListOfId(idList);
        } else {
            toDoListItems = new ArrayList<>();
        }

        return toDoListItems.stream()
            .map(listItem -> modelMapper.map(listItem, ToDoListItemManagementDto.class))
            .toList();
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
                .toList();
        return getPagebleAdvancedDto(toDoListItemManagementDtos, toDoListItems);
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
     * {@link ToDoListItemViewDto}.
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
            .toList();
        return getPagebleAdvancedDto(toDoListItemManagementDtos, pages);
    }

    private List<ToDoListItemResponseDto> getAllToDoListItemsForUser(HabitAssign habitAssign) {
        return toDoListItemRepo
            .findAllByHabitAssignId(habitAssign.getId())
            .stream()
            .map(toDoListItem -> modelMapper.map(toDoListItem, ToDoListItemResponseDto.class))
            .toList();
    }

    /**
     * Method for setting text either for UserToDoListItem with localization.
     *
     * @param dto {@link ToDoListItemResponseDto}
     */
    private void setTextForToDoListItem(ToDoListItemResponseDto dto, String language) {
        String text =
            toDoListItemTranslationRepo.findByLangAndToDoListItemId(language, dto.getId()).getContent();
        dto.setText(text);
    }
}
