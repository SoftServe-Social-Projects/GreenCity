package greencity.service;

import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.todolistitem.CustomToDoListItemSaveRequestDto;
import greencity.dto.user.UserVO;
import greencity.entity.CustomToDoListItem;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.User;
import greencity.entity.UserToDoListItem;
import greencity.enums.EmailNotification;
import greencity.enums.Role;
import greencity.enums.ToDoListItemStatus;
import greencity.enums.UserStatus;
import greencity.enums.UserToDoListItemStatus;
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
    void saveEmptyBulkSaveCustomToDoListItemDtoTest() {
        UserVO userVO = ModelUtils.getUserVO();
        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        when(restClient.findById(1L)).thenReturn(userVO);
        when(habitAssignRepo.findById(anyLong())).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        List<CustomToDoListItem> items = user.getCustomToDoListItems();
        when(customToDoListItemRepo.saveAll(any())).thenReturn(items);
        List<CustomToDoListItemResponseDto> saveResult = customToDoListItemService.save(
                Collections.emptyList(), 1L, 1L);
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
            .thenReturn(new CustomToDoListItemResponseDto(1L, "bar", ToDoListItemStatus.ACTIVE.toString(), true));
        List<CustomToDoListItemResponseDto> saveResult = customToDoListItemService.save(
            Collections.singletonList(dtoToSave), 1L, 1L);
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
        List<CustomToDoListItemSaveRequestDto> bulkSave = Collections.singletonList(dtoToSave);
        Assertions.assertThrows(CustomToDoListItemNotSavedException.class,
            () -> customToDoListItemService.save(bulkSave, 1L, 1L));
    }

    @Test
    void saveFailedOnHabitFindBy() {
        when(habitAssignRepo.findById(anyLong())).thenThrow(NotFoundException.class);
        CustomToDoListItemSaveRequestDto dtoToSave = new CustomToDoListItemSaveRequestDto("foo");
        List<CustomToDoListItemSaveRequestDto> bulkSave = Collections.singletonList(dtoToSave);
        assertThrows(NotFoundException.class, () -> customToDoListItemService.save(bulkSave, 1L, 1L));
    }

    @Test
    void updateItemStatus() {
        CustomToDoListItem customToDoListItem =
            new CustomToDoListItem(1L, "test", null, null, ToDoListItemStatus.ACTIVE, true);
        CustomToDoListItemResponseDto test =
            new CustomToDoListItemResponseDto(1L, "test", ToDoListItemStatus.ACTIVE.toString(), true);
        when(customToDoListItemRepo.findByUserIdAndItemId(64L, 1L)).thenReturn(customToDoListItem);
        when(customToDoListItemRepo.save(customToDoListItem)).thenReturn(customToDoListItem);
        when(modelMapper.map(customToDoListItem, CustomToDoListItemResponseDto.class)).thenReturn(test);
        assertEquals(test, customToDoListItemService.updateItemStatus(64L, 1L, "DONE"));
        CustomToDoListItem customToDoListItem1 =
            new CustomToDoListItem(2L, "test", null, null, ToDoListItemStatus.ACTIVE, true);
        CustomToDoListItemResponseDto test1 =
            new CustomToDoListItemResponseDto(2L, "test", ToDoListItemStatus.ACTIVE.toString(), true);
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
    void bulkDeleteWithNonExistentIdTest() {
        doThrow(new EmptyResultDataAccessException(1)).when(customToDoListItemRepo).deleteById(1L);
        Assertions
            .assertThrows(NotFoundException.class,
                () -> customToDoListItemService.bulkDelete(List.of(1L)));
    }

    @Test
    void bulkDeleteWithExistentIdTest() {
        doNothing().when(customToDoListItemRepo).deleteById(anyLong());
        ArrayList<Long> expectedResult = new ArrayList<>();
        expectedResult.add(1L);
        expectedResult.add(2L);
        expectedResult.add(3L);
        List<Long> bulkDeleteResult = customToDoListItemService.bulkDelete(List.of(1L, 2L, 3L));
        assertEquals(expectedResult, bulkDeleteResult);
    }

    @Test
    void updateItemStatusToDone() {
        Long userToDoListItemId = 1L;
        UserToDoListItem userToDoListItem =
            new UserToDoListItem(1L, ModelUtils.getHabitAssignWithUserToDoListItem(), 1L, false,
                    UserToDoListItemStatus.INPROGRESS, LocalDateTime.now());
        when(userToDoListItemRepo.getCustomToDoItemIdByUserAndItemId(1L, 1L))
            .thenReturn(Optional.of(userToDoListItemId));
        when(userToDoListItemRepo.getReferenceById(userToDoListItemId)).thenReturn(userToDoListItem);
        customToDoListItemService.updateItemStatusToDone(1L, 1L);
        userToDoListItem.setStatus(UserToDoListItemStatus.DONE);
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
        expectedDto.setStatus(ToDoListItemStatus.ACTIVE.toString());

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
