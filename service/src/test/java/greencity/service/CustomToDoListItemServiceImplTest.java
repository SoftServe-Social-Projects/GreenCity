package greencity.service;

import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
import greencity.dto.shoppinglistitem.BulkSaveCustomShoppingListItemDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemResponseDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemSaveRequestDto;
import greencity.dto.user.UserVO;
import greencity.entity.CustomShoppingListItem;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.ShoppingListItem;
import greencity.entity.User;
import greencity.entity.UserShoppingListItem;
import greencity.enums.EmailNotification;
import greencity.enums.Role;
import greencity.enums.ToDoListItemStatus;
import greencity.enums.UserStatus;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.CustomShoppingListItemNotSavedException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.repository.CustomShoppingListItemRepo;
import greencity.repository.HabitAssignRepo;
import greencity.repository.UserShoppingListItemRepo;
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
class CustomShoppingListItemServiceImplTest {
    @Mock
    private CustomShoppingListItemRepo customShoppingListItemRepo;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private RestClient restClient;
    @Mock
    private HabitAssignRepo habitAssignRepo;

    @Mock
    private UserShoppingListItemRepo userShoppingListItemRepo;

    @InjectMocks
    private CustomShoppingListItemServiceImpl customShoppingListItemService;

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
            .customShoppingListItems(new ArrayList<>())
            .build();

    private final Habit habit = Habit.builder()
        .id(1L)
        .build();

    private final CustomShoppingListItem item =
        CustomShoppingListItem.builder()
            .id(1L)
            .habit(habit)
            .user(user)
            .text("item")
            .status(ToDoListItemStatus.ACTIVE)
            .build();

    @Test
    void findAll() {
        CustomShoppingListItemResponseDto dtoExpected = new CustomShoppingListItemResponseDto(3L, "text",
            ToDoListItemStatus.ACTIVE);
        when(customShoppingListItemRepo.findAll()).thenReturn(Collections.singletonList(item));
        when(modelMapper.map(any(), any())).thenReturn(dtoExpected);

        assertEquals(dtoExpected.getText(), customShoppingListItemService.findAll().get(0).getText());
    }

    @Test
    void findById() {
        CustomShoppingListItemResponseDto expected = new CustomShoppingListItemResponseDto(3L,
            "text", ToDoListItemStatus.ACTIVE);
        when(customShoppingListItemRepo.findById(1L)).thenReturn(Optional.of(item));
        when(modelMapper.map(any(), eq(CustomShoppingListItemResponseDto.class)))
            .thenReturn(expected);

        assertEquals(expected.getText(), customShoppingListItemService.findById(1L).getText());
    }

    @Test
    void FindAllByUserAndHabit() {
        CustomShoppingListItemResponseDto dtoExpected = new CustomShoppingListItemResponseDto(3L, "text",
            ToDoListItemStatus.ACTIVE);
        when(customShoppingListItemRepo.findAllByUserIdAndHabitId(1L, 1L))
            .thenReturn(Collections.singletonList(item));
        when(modelMapper.map(any(CustomShoppingListItem.class), eq(CustomShoppingListItemResponseDto.class)))
            .thenReturn(dtoExpected);
        assertEquals(dtoExpected.getText(), customShoppingListItemService
            .findAllByUserAndHabit(1L, 1L).get(0).getText());
    }

    @Test
    void findAllAvailableCustomShoppingListItems() {
        List<CustomShoppingListItem> items = new ArrayList<>();
        items.add(item);
        when(customShoppingListItemRepo.findAllAvailableCustomShoppingListItemsForUserId(anyLong(), anyLong()))
            .thenReturn(items);
        when(modelMapper.map(items, new TypeToken<List<CustomShoppingListItemResponseDto>>() {
        }.getType())).thenReturn(items);

        assertEquals(items, customShoppingListItemService.findAllAvailableCustomShoppingListItems(1L, 1L));
    }

    @Test
    void findAllCustomShoppingListItemsWithStatusInProgressTest() {
        CustomShoppingListItem itemInProgress = ModelUtils.getCustomShoppingListItemWithStatusInProgress();
        CustomShoppingListItemResponseDto itemResponseDto =
            ModelUtils.getCustomShoppingListItemResponseDtoWithStatusInProgress();

        when(customShoppingListItemRepo.findAllCustomShoppingListItemsForUserIdAndHabitIdInProgress(anyLong(),
            anyLong()))
            .thenReturn(List.of(itemInProgress));
        when(modelMapper.map(itemInProgress, CustomShoppingListItemResponseDto.class)).thenReturn(itemResponseDto);

        assertEquals(List.of(itemResponseDto), customShoppingListItemService
            .findAllCustomShoppingListItemsWithStatusInProgress(1L, 3L));

        verify(customShoppingListItemRepo).findAllCustomShoppingListItemsForUserIdAndHabitIdInProgress(anyLong(),
            anyLong());
        verify(modelMapper).map(any(), any());
    }

    @Test
    void saveEmptyBulkSaveCustomShoppingListItemDtoTest() {
        UserVO userVO = ModelUtils.getUserVO();
        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        when(restClient.findById(1L)).thenReturn(userVO);
        when(habitAssignRepo.findById(anyLong())).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        List<CustomShoppingListItem> items = user.getCustomShoppingListItems();
        when(customShoppingListItemRepo.saveAll(any())).thenReturn(items);
        List<CustomShoppingListItemResponseDto> saveResult = customShoppingListItemService.save(
            new BulkSaveCustomShoppingListItemDto(Collections.emptyList()),
            1L, 1L);
        assertTrue(saveResult.isEmpty());
        assertTrue(user.getCustomShoppingListItems().isEmpty());
    }

    @Test
    void saveNonExistentBulkSaveCustomShoppingListItemDtoTest() {
        CustomShoppingListItemSaveRequestDto dtoToSave = new CustomShoppingListItemSaveRequestDto("foo");
        CustomShoppingListItem customShoppingListItem =
            new CustomShoppingListItem(1L, dtoToSave.getText(), null, null, null, null);
        UserVO userVO = ModelUtils.getUserVO();
        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        when(restClient.findById(1L)).thenReturn(userVO);
        when(habitAssignRepo.findById(anyLong())).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(modelMapper.map(dtoToSave, CustomShoppingListItem.class)).thenReturn(customShoppingListItem);
        when(modelMapper.map(customShoppingListItem, CustomShoppingListItemResponseDto.class))
            .thenReturn(new CustomShoppingListItemResponseDto(1L, "bar", ToDoListItemStatus.DONE));
        List<CustomShoppingListItemResponseDto> saveResult = customShoppingListItemService.save(
            new BulkSaveCustomShoppingListItemDto(Collections.singletonList(dtoToSave)),
            1L, 1L);
        assertEquals(user.getCustomShoppingListItems().get(0), customShoppingListItem);
        assertEquals("bar", saveResult.getFirst().getText());
    }

    @Test
    void saveDuplicatedBulkSaveCustomShoppingListItemDtoTest() {
        CustomShoppingListItemSaveRequestDto dtoToSave = new CustomShoppingListItemSaveRequestDto("foo");
        CustomShoppingListItem customShoppingListItem =
            new CustomShoppingListItem(1L, dtoToSave.getText(), user, habit, null, null);
        user.setCustomShoppingListItems(Collections.singletonList(customShoppingListItem));
        UserVO userVO = ModelUtils.getUserVO();
        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        when(restClient.findById(1L)).thenReturn(userVO);
        when(habitAssignRepo.findById(anyLong())).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        when(modelMapper.map(dtoToSave, CustomShoppingListItem.class)).thenReturn(customShoppingListItem);
        BulkSaveCustomShoppingListItemDto bulkSave =
            new BulkSaveCustomShoppingListItemDto(Collections.singletonList(dtoToSave));
        Assertions.assertThrows(CustomShoppingListItemNotSavedException.class,
            () -> customShoppingListItemService.save(bulkSave, 1L, 1L));
    }

    @Test
    void saveFailedOnHabitFindBy() {
        when(habitAssignRepo.findById(anyLong())).thenThrow(NotFoundException.class);
        CustomShoppingListItemSaveRequestDto dtoToSave = new CustomShoppingListItemSaveRequestDto("foo");
        BulkSaveCustomShoppingListItemDto bulkSave =
            new BulkSaveCustomShoppingListItemDto(Collections.singletonList(dtoToSave));
        assertThrows(NotFoundException.class, () -> customShoppingListItemService.save(bulkSave, 1L, 1L));
    }

    @Test
    void findAllTest() {
        CustomShoppingListItem customShoppingListItem =
            new CustomShoppingListItem(1L, "foo", null, null, ToDoListItemStatus.DONE, null);
        when(customShoppingListItemRepo.findAll()).thenReturn(Collections.singletonList(customShoppingListItem));
        when(modelMapper.map(customShoppingListItem, CustomShoppingListItemResponseDto.class))
            .thenReturn(new CustomShoppingListItemResponseDto(customShoppingListItem.getId(),
                customShoppingListItem.getText(), customShoppingListItem.getStatus()));
        List<CustomShoppingListItemResponseDto> findAllResult = customShoppingListItemService.findAll();
        assertEquals("foo", findAllResult.getFirst().getText());
        assertEquals(1L, (long) findAllResult.getFirst().getId());
    }

    @Test
    void findByNullIdTest() {
        Assertions
            .assertThrows(NotFoundException.class,
                () -> customShoppingListItemService.findById(null));
    }

    @Test
    void findByIdTest() {
        CustomShoppingListItem customShoppingListItem =
            new CustomShoppingListItem(1L, "foo", null, null, ToDoListItemStatus.DONE, null);
        when(customShoppingListItemRepo.findById(anyLong())).thenReturn(java.util.Optional.of(customShoppingListItem));
        when(modelMapper.map(customShoppingListItem, CustomShoppingListItemResponseDto.class))
            .thenReturn(new CustomShoppingListItemResponseDto(customShoppingListItem.getId(),
                customShoppingListItem.getText(), customShoppingListItem.getStatus()));
        CustomShoppingListItemResponseDto findByIdResult = customShoppingListItemService.findById(1L);
        assertEquals("foo", findByIdResult.getText());
        assertEquals(1L, (long) findByIdResult.getId());
    }

    @Test
    void updateItemStatus() {
        CustomShoppingListItem customShoppingListItem =
            new CustomShoppingListItem(1L, "test", null, null, ToDoListItemStatus.DONE, null);
        CustomShoppingListItemResponseDto test =
            new CustomShoppingListItemResponseDto(1L, "test", ToDoListItemStatus.DONE);
        when(customShoppingListItemRepo.findByUserIdAndItemId(64L, 1L)).thenReturn(customShoppingListItem);
        when(customShoppingListItemRepo.save(customShoppingListItem)).thenReturn(customShoppingListItem);
        when(modelMapper.map(customShoppingListItem, CustomShoppingListItemResponseDto.class)).thenReturn(test);
        assertEquals(test, customShoppingListItemService.updateItemStatus(64L, 1L, "DONE"));
        CustomShoppingListItem customShoppingListItem1 =
            new CustomShoppingListItem(2L, "test", null, null, ToDoListItemStatus.ACTIVE, null);
        CustomShoppingListItemResponseDto test1 =
            new CustomShoppingListItemResponseDto(2L, "test", ToDoListItemStatus.ACTIVE);
        when(customShoppingListItemRepo.findByUserIdAndItemId(12L, 2L)).thenReturn(customShoppingListItem1);
        when(customShoppingListItemRepo.save(customShoppingListItem1)).thenReturn(customShoppingListItem1);
        when(modelMapper.map(customShoppingListItem1, CustomShoppingListItemResponseDto.class)).thenReturn(test1);
        assertEquals(test1, customShoppingListItemService.updateItemStatus(12L, 2L, "ACTIVE"));
        when(customShoppingListItemRepo.findByUserIdAndItemId(any(), anyLong())).thenReturn(null);
        Exception thrown1 = assertThrows(NotFoundException.class,
            () -> customShoppingListItemService.updateItemStatus(64L, 1L, "DONE"));
        assertEquals(ErrorMessage.CUSTOM_SHOPPING_LIST_ITEM_NOT_FOUND_BY_ID, thrown1.getMessage());
        when(customShoppingListItemRepo.findByUserIdAndItemId(12L, 2L)).thenReturn(customShoppingListItem1);
        Exception thrown2 = assertThrows(BadRequestException.class,
            () -> customShoppingListItemService.updateItemStatus(12L, 2L, "NOTDONE"));
        assertEquals(ErrorMessage.INCORRECT_INPUT_ITEM_STATUS, thrown2.getMessage());
    }

    @Test
    void findAllByUserWithNullIdTest() {
        Assertions
            .assertThrows(NotFoundException.class,
                () -> customShoppingListItemService.findAllByUserAndHabit(null, null));
    }

    @Test
    void findAllByUserWithNonExistentIdTest() {
        when(customShoppingListItemRepo.findAllByUserIdAndHabitId(1L, 1L)).thenReturn(Collections.emptyList());
        Assertions
            .assertThrows(NotFoundException.class,
                () -> customShoppingListItemService.findAllByUserAndHabit(1L, 1L));
    }

    @Test
    void findAllByUserWithExistentIdTest() {
        CustomShoppingListItem customShoppingListItem =
            new CustomShoppingListItem(1L, "foo", user, habit, ToDoListItemStatus.DONE, null);
        CustomShoppingListItemResponseDto customShoppingListItemResponseDto =
            new CustomShoppingListItemResponseDto(customShoppingListItem.getId(), customShoppingListItem.getText(),
                customShoppingListItem.getStatus());
        when(customShoppingListItemRepo.findAllByUserIdAndHabitId(user.getId(), habit.getId()))
            .thenReturn(Collections.singletonList(customShoppingListItem));
        when(modelMapper.map(customShoppingListItem, CustomShoppingListItemResponseDto.class))
            .thenReturn(customShoppingListItemResponseDto);
        List<CustomShoppingListItemResponseDto> findAllByUserResult =
            customShoppingListItemService.findAllByUserAndHabit(user.getId(), habit.getId());
        assertEquals(findAllByUserResult.getFirst().getId(), customShoppingListItemResponseDto.getId());
        assertEquals(findAllByUserResult.getFirst().getText(), customShoppingListItemResponseDto.getText());
    }

    @Test
    void bulkDeleteWithNonExistentIdTest() {
        doThrow(new EmptyResultDataAccessException(1)).when(customShoppingListItemRepo).deleteById(1L);
        Assertions
            .assertThrows(NotFoundException.class,
                () -> customShoppingListItemService.bulkDelete("1"));
    }

    @Test
    void bulkDeleteWithExistentIdTest() {
        doNothing().when(customShoppingListItemRepo).deleteById(anyLong());
        ArrayList<Long> expectedResult = new ArrayList<>();
        expectedResult.add(1L);
        expectedResult.add(2L);
        expectedResult.add(3L);
        List<Long> bulkDeleteResult = customShoppingListItemService.bulkDelete("1,2,3");
        assertEquals(expectedResult, bulkDeleteResult);
    }

    @Test
    void updateItemStatusToDone() {
        ShoppingListItem shoppingListItem = ModelUtils.getShoppingListItem();
        Long userShoppingListItemId = 1L;
        UserShoppingListItem userShoppingListItem =
            new UserShoppingListItem(1L, ModelUtils.getHabitAssignWithUserShoppingListItem(), shoppingListItem,
                ToDoListItemStatus.ACTIVE, LocalDateTime.now());
        when(userShoppingListItemRepo.getByUserAndItemId(1L, 1L)).thenReturn(Optional.of(userShoppingListItemId));
        when(userShoppingListItemRepo.getReferenceById(userShoppingListItemId)).thenReturn(userShoppingListItem);
        customShoppingListItemService.updateItemStatusToDone(1L, 1L);
        userShoppingListItem.setStatus(ToDoListItemStatus.DONE);
        verify(userShoppingListItemRepo).save(userShoppingListItem);
    }

    @Test
    void findAllUsersCustomShoppingListItemsByStatusWithStatus() {
        when(customShoppingListItemRepo.findAllByUserIdAndStatus(1L, "INPROGRESS"))
            .thenReturn(List.of(ModelUtils.getCustomShoppingListItem()));
        when(modelMapper.map(ModelUtils.getCustomShoppingListItem(), CustomShoppingListItemResponseDto.class))
            .thenReturn(ModelUtils.getCustomShoppingListItemResponseDto());

        assertTrue(customShoppingListItemService.findAllUsersCustomShoppingListItemsByStatus(1L, "INPROGRESS")
            .contains(ModelUtils.getCustomShoppingListItemResponseDto()));
    }

    @Test
    void findAllUsersCustomShoppingListItemsByStatusWithoutStatus() {
        when(customShoppingListItemRepo.findAllByUserId(1L))
            .thenReturn(List.of(ModelUtils.getCustomShoppingListItem()));
        when(modelMapper.map(ModelUtils.getCustomShoppingListItem(), CustomShoppingListItemResponseDto.class))
            .thenReturn(ModelUtils.getCustomShoppingListItemResponseDto());

        assertTrue(customShoppingListItemService.findAllUsersCustomShoppingListItemsByStatus(1L, null)
            .contains(ModelUtils.getCustomShoppingListItemResponseDto()));
    }

    @Test
    void findAllAvailableCustomShoppingListItemsByHabitAssignId() {
        Long habitId = 1L;
        Long habitAssignId = 2L;
        Long userId = 3L;

        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId);

        List<CustomShoppingListItem> items = new ArrayList<>();
        items.add(item);

        CustomShoppingListItemResponseDto expectedDto = ModelUtils.getCustomShoppingListItemResponseDto();
        expectedDto.setText("item");
        expectedDto.setStatus(ToDoListItemStatus.ACTIVE);

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));
        when(customShoppingListItemRepo.findAllAvailableCustomShoppingListItemsForUserId(userId, habitId))
            .thenReturn(items);
        when(modelMapper.map(item, CustomShoppingListItemResponseDto.class)).thenReturn(expectedDto);

        List<CustomShoppingListItemResponseDto> actualDtoList = customShoppingListItemService
            .findAllAvailableCustomShoppingListItemsByHabitAssignId(userId, habitAssignId);

        assertNotNull(actualDtoList);
        assertEquals(1, actualDtoList.size());
        assertEquals(expectedDto, actualDtoList.getFirst());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(customShoppingListItemRepo).findAllAvailableCustomShoppingListItemsForUserId(userId, habitId);
        verify(modelMapper).map(item, CustomShoppingListItemResponseDto.class);
    }

    @Test
    void findAllAvailableCustomShoppingListItemsByHabitAssignIdThrowsExceptionWhenHabitAssignNotExists() {
        Long habitAssignId = 2L;
        Long userId = 3L;

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> customShoppingListItemService
            .findAllAvailableCustomShoppingListItemsByHabitAssignId(userId, habitAssignId));

        assertEquals(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(customShoppingListItemRepo, times(0)).findAllAvailableCustomShoppingListItemsForUserId(anyLong(),
            anyLong());
        verify(modelMapper, times(0)).map(any(), any());
    }

    @Test
    void findAllAvailableCustomShoppingListItemsByHabitAssignIdThrowsExceptionWhenHabitAssignNotBelongsToUser() {
        long habitAssignId = 2L;
        long userId = 3L;

        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId + 1);

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));

        UserHasNoPermissionToAccessException exception =
            assertThrows(UserHasNoPermissionToAccessException.class, () -> customShoppingListItemService
                .findAllAvailableCustomShoppingListItemsByHabitAssignId(userId, habitAssignId));

        assertEquals(ErrorMessage.USER_HAS_NO_PERMISSION, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(customShoppingListItemRepo, times(0)).findAllAvailableCustomShoppingListItemsForUserId(anyLong(),
            anyLong());
        verify(modelMapper, times(0)).map(any(), any());
    }
}
