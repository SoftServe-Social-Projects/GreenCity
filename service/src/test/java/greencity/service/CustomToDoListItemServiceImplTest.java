package greencity.service;

import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
import greencity.dto.todolistitem.BulkSaveCustomToDoListItemDto;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.todolistitem.CustomToDoListItemSaveRequestDto;
import greencity.dto.user.UserVO;
import greencity.entity.CustomToDoListItem;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.ToDoListItem;
import greencity.entity.User;
import greencity.entity.UserToDoListItem;
import greencity.enums.EmailNotification;
import greencity.enums.Role;
import greencity.enums.ToDoListItemStatus;
import greencity.enums.UserStatus;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.CustomToDoListItemNotSavedException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.repository.CustomToDoListItemRepo;
import greencity.repository.HabitAssignRepo;
import greencity.repository.UserToDoListItemRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.dao.EmptyResultDataAccessException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomToDoListItemServiceImplTest {
    @Mock
    private CustomToDoListItemRepo customToDoListItemRepo;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private RestClient restClient;
    @Mock
    private HabitAssignRepo habitAssignRepo;

    @Mock
    private UserToDoListItemRepo userToDoListItemRepo;

    @InjectMocks
    private CustomToDoListItemServiceImpl customToDoListItemService;

    private final User user =
        User.builder()
            .id(1L)
            .name("Test Testing")
            .email("test@gmail.com")
            .role(Role.ROLE_USER)
            .userStatus(UserStatus.ACTIVATED)
            .emailNotification(EmailNotification.DISABLED)
            .lastActivityTime(LocalDateTime.now())
            .dateOfRegistration(LocalDateTime.now())
            .customToDoListItems(new ArrayList<>())
            .build();

    private final Habit habit = Habit.builder()
        .id(1L)
        .build();

    private final CustomToDoListItem item =
        CustomToDoListItem.builder()
            .id(1L)
            .habit(habit)
            .user(user)
            .text("item")
            .status(ToDoListItemStatus.ACTIVE)
            .build();

    @Test
    void findAll() {
        CustomToDoListItemResponseDto dtoExpected = new CustomToDoListItemResponseDto(3L, "text",
            ToDoListItemStatus.ACTIVE);
        when(customToDoListItemRepo.findAll()).thenReturn(Collections.singletonList(item));
        when(modelMapper.map(any(), any())).thenReturn(dtoExpected);

        assertEquals(dtoExpected.getText(), customToDoListItemService.findAll().get(0).getText());
    }

    @Test
    void findById() {
        CustomToDoListItemResponseDto expected = new CustomToDoListItemResponseDto(3L,
            "text", ToDoListItemStatus.ACTIVE);
        when(customToDoListItemRepo.findById(1L)).thenReturn(Optional.of(item));
        when(modelMapper.map(any(), eq(CustomToDoListItemResponseDto.class)))
            .thenReturn(expected);

        assertEquals(expected.getText(), customToDoListItemService.findById(1L).getText());
    }

    @Test
    void FindAllByUserAndHabit() {
        CustomToDoListItemResponseDto dtoExpected = new CustomToDoListItemResponseDto(3L, "text",
            ToDoListItemStatus.ACTIVE);
        when(customToDoListItemRepo.findAllByUserIdAndHabitId(1L, 1L))
            .thenReturn(Collections.singletonList(item));
        when(modelMapper.map(any(CustomToDoListItem.class), eq(CustomToDoListItemResponseDto.class)))
            .thenReturn(dtoExpected);
        assertEquals(dtoExpected.getText(), customToDoListItemService
            .findAllByUserAndHabit(1L, 1L).get(0).getText());
    }

    @Test
    void findAllAvailableCustomToDoListItems() {
        List<CustomToDoListItem> items = new ArrayList<>();
        items.add(item);
        when(customToDoListItemRepo.findAllAvailableCustomToDoListItemsForUserId(anyLong(), anyLong()))
            .thenReturn(items);
        when(modelMapper.map(items, new TypeToken<List<CustomToDoListItemResponseDto>>() {
        }.getType())).thenReturn(items);

        assertEquals(items, customToDoListItemService.findAllAvailableCustomToDoListItems(1L, 1L));
    }

    @Test
    void findAllCustomToDoListItemsWithStatusInProgressTest() {
        CustomToDoListItem itemInProgress = ModelUtils.getCustomToDoListItemWithStatusInProgress();
        CustomToDoListItemResponseDto itemResponseDto =
            ModelUtils.getCustomToDoListItemResponseDtoWithStatusInProgress();

        when(customToDoListItemRepo.findAllCustomToDoListItemsForUserIdAndHabitIdInProgress(anyLong(),
            anyLong()))
            .thenReturn(List.of(itemInProgress));
        when(modelMapper.map(itemInProgress, CustomToDoListItemResponseDto.class)).thenReturn(itemResponseDto);

        assertEquals(List.of(itemResponseDto), customToDoListItemService
            .findAllCustomToDoListItemsWithStatusInProgress(1L, 3L));

        verify(customToDoListItemRepo).findAllCustomToDoListItemsForUserIdAndHabitIdInProgress(anyLong(),
            anyLong());
        verify(modelMapper).map(any(), any());
    }

    @Test
    void saveEmptyBulkSaveCustomToDoListItemDtoTest() {
        UserVO userVO = ModelUtils.getUserVO();
        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        when(restClient.findById(1L)).thenReturn(userVO);
        when(habitAssignRepo.findById(anyLong())).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        List<CustomToDoListItem> items = user.getCustomToDoListItems();
        when(customToDoListItemRepo.saveAll(any())).thenReturn(items);
        List<CustomToDoListItemResponseDto> saveResult = customToDoListItemService.save(
            new BulkSaveCustomToDoListItemDto(Collections.emptyList()),
            1L, 1L);
        assertTrue(saveResult.isEmpty());
        assertTrue(user.getCustomToDoListItems().isEmpty());
    }

    @Test
    void saveNonExistentBulkSaveCustomToDoListItemDtoTest() {
        CustomToDoListItemSaveRequestDto dtoToSave = new CustomToDoListItemSaveRequestDto("foo");
        CustomToDoListItem customToDoListItem =
            new CustomToDoListItem(1L, dtoToSave.getText(), null, null, null, null);
        UserVO userVO = ModelUtils.getUserVO();
        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        when(restClient.findById(1L)).thenReturn(userVO);
        when(habitAssignRepo.findById(anyLong())).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(modelMapper.map(dtoToSave, CustomToDoListItem.class)).thenReturn(customToDoListItem);
        when(modelMapper.map(customToDoListItem, CustomToDoListItemResponseDto.class))
            .thenReturn(new CustomToDoListItemResponseDto(1L, "bar", ToDoListItemStatus.DONE));
        List<CustomToDoListItemResponseDto> saveResult = customToDoListItemService.save(
            new BulkSaveCustomToDoListItemDto(Collections.singletonList(dtoToSave)),
            1L, 1L);
        assertEquals(user.getCustomToDoListItems().get(0), customToDoListItem);
        assertEquals("bar", saveResult.getFirst().getText());
    }

    @Test
    void saveDuplicatedBulkSaveCustomToDoListItemDtoTest() {
        CustomToDoListItemSaveRequestDto dtoToSave = new CustomToDoListItemSaveRequestDto("foo");
        CustomToDoListItem customToDoListItem =
            new CustomToDoListItem(1L, dtoToSave.getText(), user, habit, null, null);
        user.setCustomToDoListItems(Collections.singletonList(customToDoListItem));
        UserVO userVO = ModelUtils.getUserVO();
        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        when(restClient.findById(1L)).thenReturn(userVO);
        when(habitAssignRepo.findById(anyLong())).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(modelMapper.map(dtoToSave, CustomToDoListItem.class)).thenReturn(customToDoListItem);
        BulkSaveCustomToDoListItemDto bulkSave =
            new BulkSaveCustomToDoListItemDto(Collections.singletonList(dtoToSave));
        Assertions.assertThrows(CustomToDoListItemNotSavedException.class,
            () -> customToDoListItemService.save(bulkSave, 1L, 1L));
    }

    @Test
    void saveFailedOnHabitFindBy() {
        when(habitAssignRepo.findById(anyLong())).thenThrow(NotFoundException.class);
        CustomToDoListItemSaveRequestDto dtoToSave = new CustomToDoListItemSaveRequestDto("foo");
        BulkSaveCustomToDoListItemDto bulkSave =
            new BulkSaveCustomToDoListItemDto(Collections.singletonList(dtoToSave));
        assertThrows(NotFoundException.class, () -> customToDoListItemService.save(bulkSave, 1L, 1L));
    }

    @Test
    void findAllTest() {
        CustomToDoListItem customToDoListItem =
            new CustomToDoListItem(1L, "foo", null, null, ToDoListItemStatus.DONE, null);
        when(customToDoListItemRepo.findAll()).thenReturn(Collections.singletonList(customToDoListItem));
        when(modelMapper.map(customToDoListItem, CustomToDoListItemResponseDto.class))
            .thenReturn(new CustomToDoListItemResponseDto(customToDoListItem.getId(),
                customToDoListItem.getText(), customToDoListItem.getStatus()));
        List<CustomToDoListItemResponseDto> findAllResult = customToDoListItemService.findAll();
        assertEquals("foo", findAllResult.getFirst().getText());
        assertEquals(1L, (long) findAllResult.getFirst().getId());
    }

    @Test
    void findByNullIdTest() {
        Assertions
            .assertThrows(NotFoundException.class,
                () -> customToDoListItemService.findById(null));
    }

    @Test
    void findByIdTest() {
        CustomToDoListItem customToDoListItem =
            new CustomToDoListItem(1L, "foo", null, null, ToDoListItemStatus.DONE, null);
        when(customToDoListItemRepo.findById(anyLong())).thenReturn(java.util.Optional.of(customToDoListItem));
        when(modelMapper.map(customToDoListItem, CustomToDoListItemResponseDto.class))
            .thenReturn(new CustomToDoListItemResponseDto(customToDoListItem.getId(),
                customToDoListItem.getText(), customToDoListItem.getStatus()));
        CustomToDoListItemResponseDto findByIdResult = customToDoListItemService.findById(1L);
        assertEquals("foo", findByIdResult.getText());
        assertEquals(1L, (long) findByIdResult.getId());
    }

    @Test
    void updateItemStatus() {
        CustomToDoListItem customToDoListItem =
            new CustomToDoListItem(1L, "test", null, null, ToDoListItemStatus.DONE, null);
        CustomToDoListItemResponseDto test =
            new CustomToDoListItemResponseDto(1L, "test", ToDoListItemStatus.DONE);
        when(customToDoListItemRepo.findByUserIdAndItemId(64L, 1L)).thenReturn(customToDoListItem);
        when(customToDoListItemRepo.save(customToDoListItem)).thenReturn(customToDoListItem);
        when(modelMapper.map(customToDoListItem, CustomToDoListItemResponseDto.class)).thenReturn(test);
        assertEquals(test, customToDoListItemService.updateItemStatus(64L, 1L, "DONE"));
        CustomToDoListItem customToDoListItem1 =
            new CustomToDoListItem(2L, "test", null, null, ToDoListItemStatus.ACTIVE, null);
        CustomToDoListItemResponseDto test1 =
            new CustomToDoListItemResponseDto(2L, "test", ToDoListItemStatus.ACTIVE);
        when(customToDoListItemRepo.findByUserIdAndItemId(12L, 2L)).thenReturn(customToDoListItem1);
        when(customToDoListItemRepo.save(customToDoListItem1)).thenReturn(customToDoListItem1);
        when(modelMapper.map(customToDoListItem1, CustomToDoListItemResponseDto.class)).thenReturn(test1);
        assertEquals(test1, customToDoListItemService.updateItemStatus(12L, 2L, "ACTIVE"));
        when(customToDoListItemRepo.findByUserIdAndItemId(any(), anyLong())).thenReturn(null);
        Exception thrown1 = assertThrows(NotFoundException.class,
            () -> customToDoListItemService.updateItemStatus(64L, 1L, "DONE"));
        assertEquals(ErrorMessage.CUSTOM_TO_DO_LIST_ITEM_NOT_FOUND_BY_ID, thrown1.getMessage());
        when(customToDoListItemRepo.findByUserIdAndItemId(12L, 2L)).thenReturn(customToDoListItem1);
        Exception thrown2 = assertThrows(BadRequestException.class,
            () -> customToDoListItemService.updateItemStatus(12L, 2L, "NOTDONE"));
        assertEquals(ErrorMessage.INCORRECT_INPUT_ITEM_STATUS, thrown2.getMessage());
    }

    @Test
    void findAllByUserWithNullIdTest() {
        Assertions
            .assertThrows(NotFoundException.class,
                () -> customToDoListItemService.findAllByUserAndHabit(null, null));
    }

    @Test
    void findAllByUserWithNonExistentIdTest() {
        when(customToDoListItemRepo.findAllByUserIdAndHabitId(1L, 1L)).thenReturn(Collections.emptyList());
        Assertions
            .assertThrows(NotFoundException.class,
                () -> customToDoListItemService.findAllByUserAndHabit(1L, 1L));
    }

    @Test
    void findAllByUserWithExistentIdTest() {
        CustomToDoListItem customToDoListItem =
            new CustomToDoListItem(1L, "foo", user, habit, ToDoListItemStatus.DONE, null);
        CustomToDoListItemResponseDto customToDoListItemResponseDto =
            new CustomToDoListItemResponseDto(customToDoListItem.getId(), customToDoListItem.getText(),
                customToDoListItem.getStatus());
        when(customToDoListItemRepo.findAllByUserIdAndHabitId(user.getId(), habit.getId()))
            .thenReturn(Collections.singletonList(customToDoListItem));
        when(modelMapper.map(customToDoListItem, CustomToDoListItemResponseDto.class))
            .thenReturn(customToDoListItemResponseDto);
        List<CustomToDoListItemResponseDto> findAllByUserResult =
            customToDoListItemService.findAllByUserAndHabit(user.getId(), habit.getId());
        assertEquals(findAllByUserResult.getFirst().getId(), customToDoListItemResponseDto.getId());
        assertEquals(findAllByUserResult.getFirst().getText(), customToDoListItemResponseDto.getText());
    }

    @Test
    void bulkDeleteWithNonExistentIdTest() {
        doThrow(new EmptyResultDataAccessException(1)).when(customToDoListItemRepo).deleteById(1L);
        Assertions
            .assertThrows(NotFoundException.class,
                () -> customToDoListItemService.bulkDelete("1"));
    }

    @Test
    void bulkDeleteWithExistentIdTest() {
        doNothing().when(customToDoListItemRepo).deleteById(anyLong());
        ArrayList<Long> expectedResult = new ArrayList<>();
        expectedResult.add(1L);
        expectedResult.add(2L);
        expectedResult.add(3L);
        List<Long> bulkDeleteResult = customToDoListItemService.bulkDelete("1,2,3");
        assertEquals(expectedResult, bulkDeleteResult);
    }

    @Test
    void updateItemStatusToDone() {
        ToDoListItem toDoListItem = ModelUtils.getToDoListItem();
        Long userToDoListItemId = 1L;
        UserToDoListItem userToDoListItem =
            new UserToDoListItem(1L, ModelUtils.getHabitAssignWithUserToDoListItem(), toDoListItem,
                ToDoListItemStatus.ACTIVE, LocalDateTime.now());
        when(userToDoListItemRepo.getByUserAndItemId(1L, 1L)).thenReturn(Optional.of(userToDoListItemId));
        when(userToDoListItemRepo.getReferenceById(userToDoListItemId)).thenReturn(userToDoListItem);
        customToDoListItemService.updateItemStatusToDone(1L, 1L);
        userToDoListItem.setStatus(ToDoListItemStatus.DONE);
        verify(userToDoListItemRepo).save(userToDoListItem);
    }

    @Test
    void findAllUsersCustomToDoListItemsByStatusWithStatus() {
        when(customToDoListItemRepo.findAllByUserIdAndStatus(1L, "INPROGRESS"))
            .thenReturn(List.of(ModelUtils.getCustomToDoListItem()));
        when(modelMapper.map(ModelUtils.getCustomToDoListItem(), CustomToDoListItemResponseDto.class))
            .thenReturn(ModelUtils.getCustomToDoListItemResponseDto());

        assertTrue(customToDoListItemService.findAllUsersCustomToDoListItemsByStatus(1L, "INPROGRESS")
            .contains(ModelUtils.getCustomToDoListItemResponseDto()));
    }

    @Test
    void findAllUsersCustomToDoListItemsByStatusWithoutStatus() {
        when(customToDoListItemRepo.findAllByUserId(1L))
            .thenReturn(List.of(ModelUtils.getCustomToDoListItem()));
        when(modelMapper.map(ModelUtils.getCustomToDoListItem(), CustomToDoListItemResponseDto.class))
            .thenReturn(ModelUtils.getCustomToDoListItemResponseDto());

        assertTrue(customToDoListItemService.findAllUsersCustomToDoListItemsByStatus(1L, null)
            .contains(ModelUtils.getCustomToDoListItemResponseDto()));
    }

    @Test
    void findAllAvailableCustomToDoListItemsByHabitAssignId() {
        Long habitId = 1L;
        Long habitAssignId = 2L;
        Long userId = 3L;

        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId);

        List<CustomToDoListItem> items = new ArrayList<>();
        items.add(item);

        CustomToDoListItemResponseDto expectedDto = ModelUtils.getCustomToDoListItemResponseDto();
        expectedDto.setText("item");
        expectedDto.setStatus(ToDoListItemStatus.ACTIVE);

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));
        when(customToDoListItemRepo.findAllAvailableCustomToDoListItemsForUserId(userId, habitId))
            .thenReturn(items);
        when(modelMapper.map(item, CustomToDoListItemResponseDto.class)).thenReturn(expectedDto);

        List<CustomToDoListItemResponseDto> actualDtoList = customToDoListItemService
            .findAllAvailableCustomToDoListItemsByHabitAssignId(userId, habitAssignId);

        assertNotNull(actualDtoList);
        assertEquals(1, actualDtoList.size());
        assertEquals(expectedDto, actualDtoList.getFirst());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(customToDoListItemRepo).findAllAvailableCustomToDoListItemsForUserId(userId, habitId);
        verify(modelMapper).map(item, CustomToDoListItemResponseDto.class);
    }

    @Test
    void findAllAvailableCustomToDoListItemsByHabitAssignIdThrowsExceptionWhenHabitAssignNotExists() {
        Long habitAssignId = 2L;
        Long userId = 3L;

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> customToDoListItemService
            .findAllAvailableCustomToDoListItemsByHabitAssignId(userId, habitAssignId));

        assertEquals(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(customToDoListItemRepo, times(0)).findAllAvailableCustomToDoListItemsForUserId(anyLong(),
            anyLong());
        verify(modelMapper, times(0)).map(any(), any());
    }

    @Test
    void findAllAvailableCustomToDoListItemsByHabitAssignIdThrowsExceptionWhenHabitAssignNotBelongsToUser() {
        long habitAssignId = 2L;
        long userId = 3L;

        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId + 1);

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));

        UserHasNoPermissionToAccessException exception =
            assertThrows(UserHasNoPermissionToAccessException.class, () -> customToDoListItemService
                .findAllAvailableCustomToDoListItemsByHabitAssignId(userId, habitAssignId));

        assertEquals(ErrorMessage.USER_HAS_NO_PERMISSION, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(customToDoListItemRepo, times(0)).findAllAvailableCustomToDoListItemsForUserId(anyLong(),
            anyLong());
        verify(modelMapper, times(0)).map(any(), any());
    }
}
