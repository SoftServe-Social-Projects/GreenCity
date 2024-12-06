package greencity.service;

import greencity.ModelUtils;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.dto.todolistitem.ToDoListItemResponseDto;
import greencity.dto.todolistitem.ToDoListItemManagementDto;
import greencity.dto.todolistitem.ToDoListItemPostDto;
import greencity.dto.todolistitem.ToDoListItemRequestDto;
import greencity.dto.todolistitem.ToDoListItemResponseWithTranslationDto;
import greencity.dto.user.UserVO;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.ToDoListItem;
import greencity.entity.localization.ToDoListItemTranslation;
import greencity.exception.exceptions.NotDeletedException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.ToDoListItemNotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.repository.HabitAssignRepo;
import greencity.repository.HabitRepo;
import greencity.repository.ToDoListItemRepo;
import greencity.repository.ToDoListItemTranslationRepo;
import greencity.repository.UserToDoListItemRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static greencity.ModelUtils.getHabit;
import static greencity.ModelUtils.getHabitAssign;
import static greencity.ModelUtils.getLanguageTranslationDTO;
import static greencity.ModelUtils.getToDoListItem;
import static greencity.ModelUtils.getToDoListItemResponseDto;
import static greencity.ModelUtils.getToDoListItemTranslation;
import static greencity.ModelUtils.getUserVO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToDoListItemServiceImplTest {
    @Mock
    private ToDoListItemTranslationRepo toDoListItemTranslationRepo;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    UserToDoListItemRepo userToDoListItemRepo;

    @Mock
    HabitAssignRepo habitAssignRepo;

    @Mock
    private ToDoListItemRepo toDoListItemRepo;

    @Mock
    private HabitRepo habitRepo;

    @Mock
    UserService userService;

    @InjectMocks
    private ToDoListItemServiceImpl toDoListItemService;

    private ToDoListItem toDoListItem;

    private List<LanguageTranslationDTO> languageTranslationDTOS;
    private ToDoListItemPostDto toDoListItemPostDto;

    private HabitAssign habitAssign;

    private UserVO userVO;

    private Habit habit;

    private ToDoListItemResponseDto toDoListItemResponseDto;

    private ToDoListItemTranslation toDoListItemTranslation;

    @BeforeEach
    void setUp() {
        toDoListItem = getToDoListItem();
        languageTranslationDTOS =
            Collections.singletonList(getLanguageTranslationDTO());
        toDoListItemPostDto =
            new ToDoListItemPostDto(languageTranslationDTOS, new ToDoListItemRequestDto(1L));
        userVO = getUserVO();
        habit = getHabit();
        habitAssign = getHabitAssign();
        habitAssign.getUser().setId(userVO.getId());
        toDoListItemResponseDto = getToDoListItemResponseDto();
        toDoListItemTranslation = ModelUtils.getToDoListItemTranslation();
    }

    @Test
    void saveToDoListItemTest() {
        when((modelMapper.map(toDoListItemPostDto, ToDoListItem.class))).thenReturn(toDoListItem);
        when(modelMapper.map(toDoListItem.getTranslations(),
            new TypeToken<List<LanguageTranslationDTO>>() {
            }.getType())).thenReturn(languageTranslationDTOS);
        List<LanguageTranslationDTO> res = toDoListItemService.saveToDoListItem(toDoListItemPostDto);
        assertEquals(languageTranslationDTOS.getFirst().getContent(), res.getFirst().getContent());
    }

    @Test
    void updateTest() {
        when(toDoListItemRepo.findById(toDoListItemPostDto.getToDoListItem().getId()))
            .thenReturn(Optional.of(toDoListItem));
        when(modelMapper.map(toDoListItem.getTranslations(),
            new TypeToken<List<LanguageTranslationDTO>>() {
            }.getType())).thenReturn(languageTranslationDTOS);
        List<LanguageTranslationDTO> res = toDoListItemService.update(toDoListItemPostDto);
        assertEquals(languageTranslationDTOS.getFirst().getContent(), res.getFirst().getContent());
    }

    @Test
    void updateThrowsTest() {
        assertThrows(ToDoListItemNotFoundException.class,
            () -> toDoListItemService.update(toDoListItemPostDto));
    }

    @Test
    void deleteTest() {
        toDoListItemService.delete(1L);
        verify(toDoListItemRepo).deleteById(1L);
    }

    @Test
    void deleteTestFailed() {
        doThrow(EmptyResultDataAccessException.class).when(toDoListItemRepo).deleteById(300000L);

        assertThrows(NotDeletedException.class, () -> toDoListItemService.delete(300000L));
    }

    @Test
    void findToDoListItemByIdTest() {
        Optional<ToDoListItem> object = Optional.of(toDoListItem);
        when(toDoListItemRepo.findById(anyLong())).thenReturn(object);
        when(modelMapper.map(object.get(), ToDoListItemResponseWithTranslationDto.class))
            .thenReturn(new ToDoListItemResponseWithTranslationDto());

        assertNotNull(toDoListItemService.findToDoListItemById(30L));
    }

    @Test
    void findToDoListItemByIdTestFailed() {
        assertThrows(ToDoListItemNotFoundException.class,
            () -> toDoListItemService.findToDoListItemById(30L));
    }

    @Test
    void getAllFactsOfTheDay() {
        int pageNumber = 0;
        int pageSize = 1;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<ToDoListItem> toDoListItems = Collections.singletonList(toDoListItem);
        Page<ToDoListItem> page = new PageImpl<>(toDoListItems, pageable, toDoListItems.size());

        List<ToDoListItemManagementDto> dtoList = Collections.singletonList(
            toDoListItems.stream().map(g -> (ToDoListItemManagementDto.builder().id(g.getId())).build())
                .findFirst().get());
        PageableAdvancedDto<ToDoListItemManagementDto> expected = new PageableAdvancedDto<>(dtoList, dtoList.size(),
            0, 1, 0, false, false, true, true);

        when(toDoListItemRepo.findAll(pageable)).thenReturn(page);
        when(modelMapper.map(toDoListItems.getFirst(), ToDoListItemManagementDto.class))
            .thenReturn(dtoList.get(0));

        PageableAdvancedDto<ToDoListItemManagementDto> actual =
            toDoListItemService.findToDoListItemsForManagementByPage(pageable);

        assertEquals(expected, actual);
    }

    @Test
    void searchBy() {
        int pageNumber = 0;
        int pageSize = 1;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<ToDoListItem> toDoListItems = Collections.singletonList(toDoListItem);
        Page<ToDoListItem> page = new PageImpl<>(toDoListItems, pageable, toDoListItems.size());

        List<ToDoListItemManagementDto> dtoList = Collections.singletonList(
            toDoListItems.stream().map(g -> (ToDoListItemManagementDto.builder().id(g.getId())).build())
                .findFirst().get());
        PageableAdvancedDto<ToDoListItemManagementDto> expected = new PageableAdvancedDto<>(dtoList, dtoList.size(),
            0, 1, 0, false, false, true, true);

        when(toDoListItemRepo.searchBy(pageable, "uk")).thenReturn(page);
        when(modelMapper.map(toDoListItems.getFirst(), ToDoListItemManagementDto.class))
            .thenReturn(dtoList.getFirst());

        PageableAdvancedDto<ToDoListItemManagementDto> actual = toDoListItemService.searchBy(pageable, "uk");

        assertEquals(expected, actual);
    }

    @Test
    void deleteAllToDoListItemByListOfId() {
        List<Long> idsToBeDeleted = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L);

        toDoListItemService.deleteAllToDoListItemsByListOfId(idsToBeDeleted);
        verify(toDoListItemRepo, times(6)).deleteById(anyLong());
    }

    @Test
    void getToDoListItemByHabitIdTest() {
        List<Long> listID = Collections.singletonList(1L);
        List<ToDoListItem> toDoListItemList = Collections.singletonList(toDoListItem);
        ToDoListItemManagementDto toDoListItemManagementDto = ToDoListItemManagementDto.builder()
            .id(1L)
            .build();
        List<ToDoListItemManagementDto> toDoListItemManagementDtos =
            Collections.singletonList(toDoListItemManagementDto);

        when(toDoListItemRepo.getAllToDoListItemIdByHabitIdIsContained(1L)).thenReturn(listID);
        when(toDoListItemRepo.getToDoListByListOfId(listID)).thenReturn(toDoListItemList);
        when(modelMapper.map(toDoListItem, ToDoListItemManagementDto.class)).thenReturn(
            toDoListItemManagementDto);
        assertEquals(toDoListItemManagementDtos, toDoListItemService.getToDoListByHabitId(1L));

    }

    @Test
    void findAllToDoListItemForManagementPageNotContainedTest() {
        int pageNumber = 0;
        int pageSize = 1;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<ToDoListItem> toDoListItemList = Collections.singletonList(toDoListItem);
        List<Long> listID = Collections.singletonList(1L);
        Page<ToDoListItem> page = new PageImpl<>(toDoListItemList, pageable, toDoListItemList.size());
        ToDoListItemManagementDto toDoListItemManagementDto = ToDoListItemManagementDto.builder()
            .id(1L)
            .build();
        List<ToDoListItemManagementDto> dtoList = Collections.singletonList(toDoListItemManagementDto);
        PageableAdvancedDto<ToDoListItemManagementDto> expected = new PageableAdvancedDto<>(dtoList, dtoList.size(),
            0, 1, 0, false, false, true, true);

        when(toDoListItemRepo.getAllToDoListItemsByHabitIdNotContained(1L)).thenReturn(listID);
        when(toDoListItemRepo.getToDoListByListOfIdPageable(listID, pageable)).thenReturn(page);
        when(modelMapper.map(toDoListItem, ToDoListItemManagementDto.class)).thenReturn(
            toDoListItemManagementDto);
        PageableAdvancedDto<ToDoListItemManagementDto> actual =
            toDoListItemService.findAllToDoListItemsForManagementPageNotContained(1L, pageable);
        assertEquals(expected, actual);
    }

    @Test
    void getToDoListByHabitAssignIdTest() {
        Long habitAssignId = 2L;
        Long userId3 = 3L;
        Long toDoListItemId = 4L;
        Long toDoListItemTranslationId = 5L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId3);
        userVO.setId(userId3);
        toDoListItem.setId(toDoListItemId);
        toDoListItemResponseDto.setId(toDoListItemId);
        toDoListItemTranslation.setId(toDoListItemTranslationId);

        List<ToDoListItemResponseDto> expected = List.of(toDoListItemResponseDto);

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId3)).thenReturn(userVO);
        when(toDoListItemRepo.findAllByHabitAssignId(habitAssignId)).thenReturn(Collections.singletonList(
            toDoListItem));
        when(modelMapper.map(toDoListItem, ToDoListItemResponseDto.class))
            .thenReturn(toDoListItemResponseDto);
        when(toDoListItemTranslationRepo.findByLangAndToDoListItemId(languageDefault,
            toDoListItemId))
            .thenReturn(toDoListItemTranslation);

        List<ToDoListItemResponseDto> actualDtoList = toDoListItemService
            .getToDoListByHabitAssignId(userId3, habitAssignId, languageDefault);

        assertNotNull(actualDtoList);
        assertEquals(1, actualDtoList.size());
        assertEquals(expected, actualDtoList);

        verify(habitAssignRepo).findById(habitAssignId);
        verify(toDoListItemRepo).findAllByHabitAssignId(habitAssignId);
        verify(modelMapper).map(toDoListItem, ToDoListItemResponseDto.class);
        verify(toDoListItemTranslationRepo).findByLangAndToDoListItemId(languageDefault,
            toDoListItemId);
    }

    @Test
    void getToDoListByHabitAssignIdReturnEmptyListTest() {
        Long habitAssignId = 2L;
        Long userId3 = 3L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId3);

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));
        when(toDoListItemRepo.findAllByHabitAssignId(habitAssignId)).thenReturn(Collections.emptyList());

        assertEquals(Collections.emptyList(),
            toDoListItemService.getToDoListByHabitAssignId(userId3, habitAssignId, languageDefault));

        verify(habitAssignRepo).findById(habitAssignId);
        verify(toDoListItemRepo).findAllByHabitAssignId(habitAssignId);
        verify(modelMapper, times(0)).map(any(), any());
        verify(toDoListItemTranslationRepo, times(0)).findByLangAndToDoListItemId(any(), anyLong());
    }

    @Test
    void getToDoListByHabitAssignIdThrowsExceptionWhenHabitAssignNotExists() {
        Long habitAssignId = 2L;
        Long userId3 = 3L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> toDoListItemService
            .getToDoListByHabitAssignId(userId3, habitAssignId, languageDefault));

        assertEquals(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userToDoListItemRepo, times(0)).findAllByHabitAssingId(anyLong());
        verify(modelMapper, times(0)).map(any(), any());
        verify(toDoListItemTranslationRepo, times(0)).findByLangAndToDoListItemId(any(), anyLong());
    }

    @Test
    void getUserToDoListByHabitAssignIdThrowsExceptionWhenHabitAssignNotBelongsTo() {
        long habitAssignId = 2L;
        long userId3 = 3L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId3 + 1);
        userVO.setId(userId3);

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId3)).thenReturn(userVO);

        UserHasNoPermissionToAccessException exception =
            assertThrows(UserHasNoPermissionToAccessException.class, () -> toDoListItemService
                .getToDoListByHabitAssignId(userId3, habitAssignId, languageDefault));

        assertEquals(ErrorMessage.USER_HAS_NO_PERMISSION, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userToDoListItemRepo, times(0)).findAllByHabitAssingId(anyLong());
        verify(modelMapper, times(0)).map(any(), any());
        verify(toDoListItemTranslationRepo, times(0)).findByLangAndToDoListItemId(any(), anyLong());
    }

    @Test
    void findAllHabitToDoList() {
        Long habitId = 2L;
        Long itemId = 3L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;
        habit.setId(habitId);
        toDoListItem.setId(itemId);
        toDoListItemResponseDto.setId(itemId);

        when(habitRepo.findById(habitId)).thenReturn(Optional.of(habit));
        when(toDoListItemRepo.getAllToDoListItemIdByHabitIdIsContained(habitId)).thenReturn(List.of(itemId));
        when(toDoListItemRepo.getReferenceById(itemId)).thenReturn(toDoListItem);
        when(modelMapper.map(toDoListItem, ToDoListItemResponseDto.class))
            .thenReturn(toDoListItemResponseDto);
        when(toDoListItemTranslationRepo.findByLangAndToDoListItemId(languageDefault, itemId))
            .thenReturn(toDoListItemTranslation);

        ToDoListItemResponseDto expectedDto = getToDoListItemResponseDto();
        expectedDto.setId(itemId);
        expectedDto.setText(toDoListItemTranslation.getContent());

        List<ToDoListItemResponseDto> result =
            toDoListItemService.findAllHabitToDoList(habitId, languageDefault);
        assertEquals(1, result.size());
        assertEquals(List.of(expectedDto), result);
    }

    @Test
    void findAllHabitToDoListThrowsExceptionWhenHabitNotExist() {
        Long habitId = 2L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        when(habitRepo.findById(habitId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> toDoListItemService.findAllHabitToDoList(habitId, languageDefault));
    }

    @Test
    void findAvailableToDoListForHabitAssign() {
        Long habitId = 2L;
        Long itemId = 3L;
        Long itemId2 = 2L;
        Long habitAssignId = 2L;
        Long userId = 3L;
        Long toDoListItemTranslationId = 5L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        habit.setId(habitId);
        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId);
        habitAssign.getHabit().setId(habitId);
        userVO.setId(userId);
        toDoListItemResponseDto.setId(itemId);
        toDoListItemTranslation.setId(toDoListItemTranslationId);
        toDoListItem.setId(itemId);

        ToDoListItem toDoListItem2 = getToDoListItem();
        toDoListItem2.setId(itemId2);
        ToDoListItemTranslation toDoListItemTranslation2 = getToDoListItemTranslation();
        ToDoListItemResponseDto toDoListItemResponseDto2 = getToDoListItemResponseDto();
        toDoListItemResponseDto2.setId(itemId2);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId)).thenReturn(userVO);
        when(toDoListItemRepo.findAllByHabitAssignId(habitAssignId)).thenReturn(List.of(toDoListItem));
        when(modelMapper.map(toDoListItem, ToDoListItemResponseDto.class))
            .thenReturn(toDoListItemResponseDto);
        when(modelMapper.map(toDoListItem2, ToDoListItemResponseDto.class))
            .thenReturn(toDoListItemResponseDto2);

        when(habitRepo.findById(habitId)).thenReturn(Optional.of(habit));
        when(toDoListItemRepo.getAllToDoListItemIdByHabitIdIsContained(habitId)).thenReturn(List.of(itemId, itemId2));
        when(toDoListItemRepo.getReferenceById(itemId)).thenReturn(toDoListItem);
        when(toDoListItemRepo.getReferenceById(itemId2)).thenReturn(toDoListItem2);
        when(toDoListItemTranslationRepo.findByLangAndToDoListItemId(languageDefault, itemId))
            .thenReturn(toDoListItemTranslation);
        when(toDoListItemTranslationRepo.findByLangAndToDoListItemId(languageDefault, itemId2))
            .thenReturn(toDoListItemTranslation2);

        List<ToDoListItemResponseDto> result =
            toDoListItemService.findAvailableToDoListForHabitAssign(userId, habitAssignId, languageDefault);
        assertEquals(1, result.size());
        assertEquals(List.of(toDoListItemResponseDto2), result);
    }

    @Test
    void findAvailableToDoListForHabitAssignAllItemsAdded() {
        Long habitId = 2L;
        Long itemId = 3L;
        Long itemId2 = 2L;
        Long habitAssignId = 2L;
        Long userId = 3L;
        Long toDoListItemTranslationId = 5L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        habit.setId(habitId);
        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId);
        habitAssign.getHabit().setId(habitId);
        userVO.setId(userId);
        toDoListItemResponseDto.setId(itemId);
        toDoListItemTranslation.setId(toDoListItemTranslationId);
        toDoListItem.setId(itemId);

        ToDoListItem toDoListItem2 = getToDoListItem();
        toDoListItem2.setId(itemId2);
        ToDoListItemTranslation toDoListItemTranslation2 = getToDoListItemTranslation();
        ToDoListItemResponseDto toDoListItemResponseDto2 = getToDoListItemResponseDto();
        toDoListItemResponseDto2.setId(itemId2);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId)).thenReturn(userVO);
        when(toDoListItemRepo.findAllByHabitAssignId(habitAssignId)).thenReturn(List.of(toDoListItem, toDoListItem2));
        when(modelMapper.map(toDoListItem, ToDoListItemResponseDto.class))
            .thenReturn(toDoListItemResponseDto);
        when(modelMapper.map(toDoListItem2, ToDoListItemResponseDto.class))
            .thenReturn(toDoListItemResponseDto2);

        when(habitRepo.findById(habitId)).thenReturn(Optional.of(habit));
        when(toDoListItemRepo.getAllToDoListItemIdByHabitIdIsContained(habitId)).thenReturn(List.of(itemId, itemId2));
        when(toDoListItemRepo.getReferenceById(itemId)).thenReturn(toDoListItem);
        when(toDoListItemRepo.getReferenceById(itemId2)).thenReturn(toDoListItem2);
        when(toDoListItemTranslationRepo.findByLangAndToDoListItemId(languageDefault, itemId))
            .thenReturn(toDoListItemTranslation);
        when(toDoListItemTranslationRepo.findByLangAndToDoListItemId(languageDefault, itemId2))
            .thenReturn(toDoListItemTranslation2);

        List<ToDoListItemResponseDto> result =
            toDoListItemService.findAvailableToDoListForHabitAssign(userId, habitAssignId, languageDefault);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAvailableToDoListForHabitAssignNoItemsAdded() {
        Long habitId = 2L;
        Long itemId = 3L;
        Long itemId2 = 2L;
        Long habitAssignId = 2L;
        Long userId = 3L;
        Long toDoListItemTranslationId = 5L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        habit.setId(habitId);
        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId);
        habitAssign.getHabit().setId(habitId);
        userVO.setId(userId);
        toDoListItemResponseDto.setId(itemId);
        toDoListItemTranslation.setId(toDoListItemTranslationId);
        toDoListItem.setId(itemId);

        ToDoListItem toDoListItem2 = getToDoListItem();
        toDoListItem2.setId(itemId2);
        ToDoListItemTranslation toDoListItemTranslation2 = getToDoListItemTranslation();
        ToDoListItemResponseDto toDoListItemResponseDto2 = getToDoListItemResponseDto();
        toDoListItemResponseDto2.setId(itemId2);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId)).thenReturn(userVO);
        when(toDoListItemRepo.findAllByHabitAssignId(habitAssignId)).thenReturn(List.of());
        when(modelMapper.map(toDoListItem, ToDoListItemResponseDto.class))
            .thenReturn(toDoListItemResponseDto);
        when(modelMapper.map(toDoListItem2, ToDoListItemResponseDto.class))
            .thenReturn(toDoListItemResponseDto2);

        when(habitRepo.findById(habitId)).thenReturn(Optional.of(habit));
        when(toDoListItemRepo.getAllToDoListItemIdByHabitIdIsContained(habitId)).thenReturn(List.of(itemId, itemId2));
        when(toDoListItemRepo.getReferenceById(itemId)).thenReturn(toDoListItem);
        when(toDoListItemRepo.getReferenceById(itemId2)).thenReturn(toDoListItem2);
        when(toDoListItemTranslationRepo.findByLangAndToDoListItemId(languageDefault, itemId))
            .thenReturn(toDoListItemTranslation);
        when(toDoListItemTranslationRepo.findByLangAndToDoListItemId(languageDefault, itemId2))
            .thenReturn(toDoListItemTranslation2);

        List<ToDoListItemResponseDto> result =
            toDoListItemService.findAvailableToDoListForHabitAssign(userId, habitAssignId, languageDefault);
        assertEquals(2, result.size());
        assertEquals(List.of(toDoListItemResponseDto, toDoListItemResponseDto2), result);
    }

    @Test
    void findAvailableToDoListForHabitAssignEmptyLists() {
        Long habitId = 2L;
        Long itemId = 3L;
        Long habitAssignId = 2L;
        Long userId = 3L;
        Long toDoListItemTranslationId = 5L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        habit.setId(habitId);
        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId);
        habitAssign.getHabit().setId(habitId);
        userVO.setId(userId);
        toDoListItemResponseDto.setId(itemId);
        toDoListItemTranslation.setId(toDoListItemTranslationId);
        toDoListItem.setId(itemId);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId)).thenReturn(userVO);
        when(toDoListItemRepo.findAllByHabitAssignId(habitAssignId)).thenReturn(List.of());

        when(habitRepo.findById(habitId)).thenReturn(Optional.of(habit));
        when(toDoListItemRepo.getAllToDoListItemIdByHabitIdIsContained(habitId)).thenReturn(List.of());

        List<ToDoListItemResponseDto> result =
            toDoListItemService.findAvailableToDoListForHabitAssign(userId, habitAssignId, languageDefault);
        assertTrue(result.isEmpty());
    }
}
