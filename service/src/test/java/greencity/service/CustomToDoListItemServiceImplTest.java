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
    void findAllHabitCustomToDoList() {
        List<CustomToDoListItem> items = new ArrayList<>();
        items.add(item);
        when(customToDoListItemRepo.findAllAvailableCustomToDoListItemsForUserId(anyLong(), anyLong()))
            .thenReturn(items);
        when(modelMapper.map(items, new TypeToken<List<CustomToDoListItemResponseDto>>() {
        }.getType())).thenReturn(items);

        assertEquals(items, customToDoListItemService.findAllHabitCustomToDoList(1L, 1L));
    }

    @Test
    void findAllHabitByHabitCustomToDoListAssignId() {
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
            .getCustomToDoListByHabitAssignId(userId, habitAssignId);

        assertNotNull(actualDtoList);
        assertEquals(1, actualDtoList.size());
        assertEquals(expectedDto, actualDtoList.getFirst());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(customToDoListItemRepo).findAllAvailableCustomToDoListItemsForUserId(userId, habitId);
        verify(modelMapper).map(item, CustomToDoListItemResponseDto.class);
    }

    @Test
    void findAllHabitByHabitAssignIdThrowsExceptionWhenHabitCustomToDoListAssignNotExists() {
        Long habitAssignId = 2L;
        Long userId = 3L;

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> customToDoListItemService
            .getCustomToDoListByHabitAssignId(userId, habitAssignId));

        assertEquals(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(customToDoListItemRepo, times(0)).findAllAvailableCustomToDoListItemsForUserId(anyLong(),
            anyLong());
        verify(modelMapper, times(0)).map(any(), any());
    }

    @Test
    void findAllCustomToDoListItemsByHabitAssignIdThrowsExceptionWhenHabitAssignNotBelongsToUserForHabit() {
        long habitAssignId = 2L;
        long userId = 3L;

        HabitAssign habitAssign = ModelUtils.getHabitAssign();
        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId + 1);

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));

        UserHasNoPermissionToAccessException exception =
            assertThrows(UserHasNoPermissionToAccessException.class, () -> customToDoListItemService
                .getCustomToDoListByHabitAssignId(userId, habitAssignId));

        assertEquals(ErrorMessage.USER_HAS_NO_PERMISSION, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(customToDoListItemRepo, times(0)).findAllAvailableCustomToDoListItemsForUserId(anyLong(),
            anyLong());
        verify(modelMapper, times(0)).map(any(), any());
    }
}
