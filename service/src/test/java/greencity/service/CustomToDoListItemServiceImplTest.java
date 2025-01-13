package greencity.service;

import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.user.UserVO;
import greencity.entity.CustomToDoListItem;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.enums.Role;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.repository.CustomToDoListItemRepo;
import greencity.repository.HabitAssignRepo;
import greencity.repository.HabitRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import java.util.List;
import java.util.Optional;
import static greencity.ModelUtils.getCustomToDoListItem;
import static greencity.ModelUtils.getCustomToDoListItemResponseDto;
import static greencity.ModelUtils.getCustomToDoListItemResponseDtoWithId2;
import static greencity.ModelUtils.getCustomToDoListItemWithId2;
import static greencity.ModelUtils.getHabit;
import static greencity.ModelUtils.getHabitAssign;
import static greencity.ModelUtils.getUserVO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private HabitAssignRepo habitAssignRepo;

    @Mock
    private HabitRepo habitRepo;

    @Mock
    private UserService userService;

    @InjectMocks
    private CustomToDoListItemServiceImpl customToDoListItemService;

    private UserVO userVO;
    private UserVO adminUser;

    private Habit habit;
    private HabitAssign habitAssign;

    private CustomToDoListItem item;
    private CustomToDoListItemResponseDto responseItem;

    private CustomToDoListItem item2;
    private CustomToDoListItemResponseDto responseItem2;

    @BeforeEach
    void setUp() {
        adminUser = getUserVO();
        adminUser.setId(1L);
        userVO = getUserVO();
        userVO.setId(2L);

        habit = getHabit();
        habitAssign = getHabitAssign();
        habitAssign.getUser().setId(2L);
        habitAssign.setHabit(habit);

        item = getCustomToDoListItem();
        item.getUser().setId(1L);
        responseItem = getCustomToDoListItemResponseDto();

        item2 = getCustomToDoListItemWithId2();
        item2.getUser().setId(2L);
        responseItem2 = getCustomToDoListItemResponseDtoWithId2();

    }

    @Test
    void findAllHabitCustomToDoList() {
        Long habitId = habit.getId();
        Long itemId = item.getId();
        Long userId = userVO.getId();

        when(habitRepo.findById(habitId)).thenReturn(Optional.of(habit));
        when(customToDoListItemRepo.getAllCustomToDoListItemIdByHabitIdIsContained(habitId))
            .thenReturn(List.of(itemId));
        when(customToDoListItemRepo.getReferenceById(itemId)).thenReturn(item);
        when(customToDoListItemRepo.findAllAvailableCustomToDoListItemsForUserId(userId, habitId))
            .thenReturn(List.of());
        when(modelMapper.map(item, CustomToDoListItemResponseDto.class)).thenReturn(responseItem);

        List<CustomToDoListItemResponseDto> result =
            customToDoListItemService.findAllHabitCustomToDoList(userId, habitId);
        assertEquals(List.of(responseItem), result);
        assertEquals(1, result.size());

        verify(habitRepo).findById(habitId);
        verify(customToDoListItemRepo).getAllCustomToDoListItemIdByHabitIdIsContained(habitId);
        verify(customToDoListItemRepo).getReferenceById(itemId);
        verify(customToDoListItemRepo).findAllAvailableCustomToDoListItemsForUserId(userId, habitId);
        verify(modelMapper).map(item, CustomToDoListItemResponseDto.class);
    }

    @Test
    void findAllHabitCustomToDoListWithUserItems() {
        Long habitId = habit.getId();
        Long itemId = item.getId();
        Long userId = userVO.getId();

        when(habitRepo.findById(habitId)).thenReturn(Optional.of(habit));
        when(customToDoListItemRepo.getAllCustomToDoListItemIdByHabitIdIsContained(habitId))
            .thenReturn(List.of(itemId));
        when(customToDoListItemRepo.getReferenceById(itemId)).thenReturn(item);
        when(customToDoListItemRepo.findAllAvailableCustomToDoListItemsForUserId(userId, habitId))
            .thenReturn(List.of(item2));
        when(modelMapper.map(item, CustomToDoListItemResponseDto.class)).thenReturn(responseItem);
        when(modelMapper.map(item2, CustomToDoListItemResponseDto.class)).thenReturn(responseItem2);

        List<CustomToDoListItemResponseDto> result =
            customToDoListItemService.findAllHabitCustomToDoList(userId, habitId);
        assertEquals(List.of(responseItem, responseItem2), result);
        assertEquals(2, result.size());

        verify(habitRepo).findById(habitId);
        verify(customToDoListItemRepo).getAllCustomToDoListItemIdByHabitIdIsContained(habitId);
        verify(customToDoListItemRepo).getReferenceById(itemId);
        verify(customToDoListItemRepo).findAllAvailableCustomToDoListItemsForUserId(userId, habitId);
        verify(modelMapper).map(item, CustomToDoListItemResponseDto.class);
        verify(modelMapper).map(item2, CustomToDoListItemResponseDto.class);
    }

    @Test
    void findAllHabitCustomToDoListWithDuplicates() {
        Long habitId = habit.getId();
        Long itemId = item.getId();
        Long userId = userVO.getId();
        item.getUser().setId(userId);

        when(habitRepo.findById(habitId)).thenReturn(Optional.of(habit));
        when(customToDoListItemRepo.getAllCustomToDoListItemIdByHabitIdIsContained(habitId))
            .thenReturn(List.of(itemId));
        when(customToDoListItemRepo.getReferenceById(itemId)).thenReturn(item);
        when(customToDoListItemRepo.findAllAvailableCustomToDoListItemsForUserId(userId, habitId))
            .thenReturn(List.of(item));
        when(modelMapper.map(item, CustomToDoListItemResponseDto.class)).thenReturn(responseItem);

        List<CustomToDoListItemResponseDto> result =
            customToDoListItemService.findAllHabitCustomToDoList(userId, habitId);
        assertEquals(List.of(responseItem), result);
        assertEquals(1, result.size());

        verify(habitRepo).findById(habitId);
        verify(customToDoListItemRepo).getAllCustomToDoListItemIdByHabitIdIsContained(habitId);
        verify(customToDoListItemRepo).getReferenceById(itemId);
        verify(customToDoListItemRepo).findAllAvailableCustomToDoListItemsForUserId(userId, habitId);
        verify(modelMapper, times(2)).map(item, CustomToDoListItemResponseDto.class);
    }

    @Test
    void findAllHabitCustomToDoListWithEmptyDefaultItems() {
        Long habitId = habit.getId();
        Long userId = userVO.getId();
        item.getUser().setId(userId);

        when(habitRepo.findById(habitId)).thenReturn(Optional.of(habit));
        when(customToDoListItemRepo.getAllCustomToDoListItemIdByHabitIdIsContained(habitId))
            .thenReturn(List.of());
        when(customToDoListItemRepo.findAllAvailableCustomToDoListItemsForUserId(userId, habitId))
            .thenReturn(List.of(item, item2));
        when(modelMapper.map(item, CustomToDoListItemResponseDto.class)).thenReturn(responseItem);
        when(modelMapper.map(item2, CustomToDoListItemResponseDto.class)).thenReturn(responseItem2);

        List<CustomToDoListItemResponseDto> result =
            customToDoListItemService.findAllHabitCustomToDoList(userId, habitId);
        assertEquals(List.of(responseItem, responseItem2), result);
        assertEquals(2, result.size());

        verify(habitRepo).findById(habitId);
        verify(customToDoListItemRepo).getAllCustomToDoListItemIdByHabitIdIsContained(habitId);
        verify(customToDoListItemRepo).findAllAvailableCustomToDoListItemsForUserId(userId, habitId);
        verify(modelMapper).map(item, CustomToDoListItemResponseDto.class);
        verify(modelMapper).map(item2, CustomToDoListItemResponseDto.class);
    }

    @Test
    void findAllHabitCustomToDoListWithEmptyList() {
        Long habitId = habit.getId();
        Long userId = userVO.getId();

        when(habitRepo.findById(habitId)).thenReturn(Optional.of(habit));
        when(customToDoListItemRepo.getAllCustomToDoListItemIdByHabitIdIsContained(habitId))
            .thenReturn(List.of());
        when(customToDoListItemRepo.findAllAvailableCustomToDoListItemsForUserId(userId, habitId))
            .thenReturn(List.of());

        List<CustomToDoListItemResponseDto> result =
            customToDoListItemService.findAllHabitCustomToDoList(userId, habitId);
        assertTrue(result.isEmpty());

        verify(habitRepo).findById(habitId);
        verify(customToDoListItemRepo).getAllCustomToDoListItemIdByHabitIdIsContained(habitId);
        verify(customToDoListItemRepo).findAllAvailableCustomToDoListItemsForUserId(userId, habitId);
    }

    @Test
    void findAllHabitCustomToDoListWithWrongHabitId() {
        Long habitId = habit.getId();
        Long userId = userVO.getId();

        when(habitRepo.findById(habitId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> customToDoListItemService.findAllHabitCustomToDoList(userId, habitId));

        verify(habitRepo).findById(habitId);
    }

    @Test
    void getCustomToDoListByHabitAssignId() {
        Long habitAssignId = habitAssign.getId();
        Long userId = userVO.getId();

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId)).thenReturn(userVO);
        when(customToDoListItemRepo.findAllByHabitAssignId(habitAssignId))
            .thenReturn(List.of(item));
        when(modelMapper.map(item, CustomToDoListItemResponseDto.class)).thenReturn(responseItem);

        List<CustomToDoListItemResponseDto> result = customToDoListItemService
            .getCustomToDoListByHabitAssignId(userId, habitAssignId);

        assertEquals(List.of(responseItem), result);
        assertEquals(1, result.size());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userService).findById(userId);
        verify(customToDoListItemRepo).findAllByHabitAssignId(habitAssignId);
        verify(modelMapper).map(item, CustomToDoListItemResponseDto.class);
    }

    @Test
    void getCustomToDoListByHabitAssignIdWithWrongHabitAssignId() {
        Long habitAssignId = habitAssign.getId();
        Long userId = userVO.getId();

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> customToDoListItemService
            .getCustomToDoListByHabitAssignId(userId, habitAssignId));

        verify(habitAssignRepo).findById(habitAssignId);
    }

    @Test
    void getCustomToDoListByHabitAssignIdWithUserHasNoPermission() {
        Long habitAssignId = habitAssign.getId();
        Long userId = userVO.getId();
        habitAssign.getUser().setId(userId + 1);

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId)).thenReturn(userVO);

        assertThrows(UserHasNoPermissionToAccessException.class, () -> customToDoListItemService
            .getCustomToDoListByHabitAssignId(userId, habitAssignId));

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userService).findById(userId);
    }

    @Test
    void getCustomToDoListByHabitAssignIdWithAdminUser() {
        adminUser.setRole(Role.ROLE_ADMIN);
        Long habitAssignId = habitAssign.getId();
        Long userId = adminUser.getId();
        habitAssign.getUser().setId(userId + 1);

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId)).thenReturn(adminUser);
        when(customToDoListItemRepo.findAllByHabitAssignId(habitAssignId))
            .thenReturn(List.of(item));
        when(modelMapper.map(item, CustomToDoListItemResponseDto.class)).thenReturn(responseItem);

        List<CustomToDoListItemResponseDto> result = customToDoListItemService
            .getCustomToDoListByHabitAssignId(userId, habitAssignId);

        assertEquals(List.of(responseItem), result);
        assertEquals(1, result.size());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userService).findById(userId);
        verify(customToDoListItemRepo).findAllByHabitAssignId(habitAssignId);
        verify(modelMapper).map(item, CustomToDoListItemResponseDto.class);
    }

    @Test
    void findAvailableCustomToDoListForHabitAssign() {
        Long habitId = habitAssign.getHabit().getId();
        Long habitAssignId = habitAssign.getId();
        Long userId = userVO.getId();

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId)).thenReturn(userVO);
        when(customToDoListItemRepo.findAllByHabitAssignId(habitAssignId)).thenReturn(List.of(item2));

        when(habitAssignRepo.getReferenceById(habitAssignId)).thenReturn(habitAssign);
        when(habitRepo.findById(habitId)).thenReturn(Optional.of(habit));
        when(customToDoListItemRepo.getAllCustomToDoListItemIdByHabitIdIsContained(habitId))
            .thenReturn(List.of(item.getId()));
        when(customToDoListItemRepo.getReferenceById(item.getId())).thenReturn(item);
        when(customToDoListItemRepo.findAllAvailableCustomToDoListItemsForUserId(userId, habitId))
            .thenReturn(List.of(item2));
        when(modelMapper.map(item, CustomToDoListItemResponseDto.class)).thenReturn(responseItem);
        when(modelMapper.map(item2, CustomToDoListItemResponseDto.class)).thenReturn(responseItem2);

        List<CustomToDoListItemResponseDto> result =
            customToDoListItemService.findAvailableCustomToDoListForHabitAssign(userId, habitAssignId);
        assertEquals(List.of(responseItem), result);
        assertEquals(1, result.size());
    }

    @Test
    void findAvailableCustomToDoListForHabitAssignWithEmptyAdded() {
        Long habitId = habitAssign.getHabit().getId();
        Long habitAssignId = habitAssign.getId();
        Long userId = userVO.getId();

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId)).thenReturn(userVO);
        when(customToDoListItemRepo.findAllByHabitAssignId(habitAssignId)).thenReturn(List.of());

        when(habitAssignRepo.getReferenceById(habitAssignId)).thenReturn(habitAssign);
        when(habitRepo.findById(habitId)).thenReturn(Optional.of(habit));
        when(customToDoListItemRepo.getAllCustomToDoListItemIdByHabitIdIsContained(habitId))
            .thenReturn(List.of(item.getId()));
        when(customToDoListItemRepo.getReferenceById(item.getId())).thenReturn(item);
        when(customToDoListItemRepo.findAllAvailableCustomToDoListItemsForUserId(userId, habitId))
            .thenReturn(List.of(item2));
        when(modelMapper.map(item, CustomToDoListItemResponseDto.class)).thenReturn(responseItem);
        when(modelMapper.map(item2, CustomToDoListItemResponseDto.class)).thenReturn(responseItem2);

        List<CustomToDoListItemResponseDto> result =
            customToDoListItemService.findAvailableCustomToDoListForHabitAssign(userId, habitAssignId);
        assertEquals(List.of(responseItem, responseItem2), result);
        assertEquals(2, result.size());
    }

    @Test
    void findAvailableCustomToDoListForHabitAssignWithAllAdded() {
        Long habitId = habitAssign.getHabit().getId();
        Long habitAssignId = habitAssign.getId();
        Long userId = userVO.getId();

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId)).thenReturn(userVO);
        when(customToDoListItemRepo.findAllByHabitAssignId(habitAssignId)).thenReturn(List.of(item, item2));

        when(habitAssignRepo.getReferenceById(habitAssignId)).thenReturn(habitAssign);
        when(habitRepo.findById(habitId)).thenReturn(Optional.of(habit));
        when(customToDoListItemRepo.getAllCustomToDoListItemIdByHabitIdIsContained(habitId))
            .thenReturn(List.of(item.getId()));
        when(customToDoListItemRepo.getReferenceById(item.getId())).thenReturn(item);
        when(customToDoListItemRepo.findAllAvailableCustomToDoListItemsForUserId(userId, habitId))
            .thenReturn(List.of(item2));
        when(modelMapper.map(item, CustomToDoListItemResponseDto.class)).thenReturn(responseItem);
        when(modelMapper.map(item2, CustomToDoListItemResponseDto.class)).thenReturn(responseItem2);

        List<CustomToDoListItemResponseDto> result =
            customToDoListItemService.findAvailableCustomToDoListForHabitAssign(userId, habitAssignId);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAvailableCustomToDoListForHabitAssignWithEmptyLists() {
        Long habitId = habitAssign.getHabit().getId();
        Long habitAssignId = habitAssign.getId();
        Long userId = userVO.getId();

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId)).thenReturn(userVO);
        when(customToDoListItemRepo.findAllByHabitAssignId(habitAssignId)).thenReturn(List.of());

        when(habitAssignRepo.getReferenceById(habitAssignId)).thenReturn(habitAssign);
        when(habitRepo.findById(habitId)).thenReturn(Optional.of(habit));
        when(customToDoListItemRepo.getAllCustomToDoListItemIdByHabitIdIsContained(habitId)).thenReturn(List.of());
        when(customToDoListItemRepo.findAllAvailableCustomToDoListItemsForUserId(userId, habitId))
            .thenReturn(List.of());

        List<CustomToDoListItemResponseDto> result =
            customToDoListItemService.findAvailableCustomToDoListForHabitAssign(userId, habitAssignId);
        assertTrue(result.isEmpty());
    }
}
