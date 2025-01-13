package greencity.service;

import greencity.constant.AppConstant;
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
import static greencity.ModelUtils.getHabitAssign;
import static greencity.ModelUtils.getToDoListItem;
import static greencity.ModelUtils.getToDoListItemTranslation;
import static greencity.ModelUtils.getUserToDoListItem;
import static greencity.ModelUtils.getUserToDoListItemRequestDto;
import static greencity.ModelUtils.getUserToDoListItemRequestWithStatusDto;
import static greencity.ModelUtils.getUserToDoListItemResponseDto;
import static greencity.ModelUtils.getUserVO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserToDoListItemServiceImplTest {
    @InjectMocks
    private UserToDoListItemServiceImpl userToDoListItemService;

    @Mock
    private UserToDoListItemRepo userToDoListItemRepo;

    @Mock
    private HabitAssignRepo habitAssignRepo;

    @Mock
    private CustomToDoListItemRepo customToDoListItemRepo;

    @Mock
    private ToDoListItemTranslationRepo toDoListItemTranslationRepo;

    @Mock
    private ToDoListItemRepo toDoListItemRepo;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    private HabitAssign habitAssign;

    private UserVO userVO;

    private UserToDoListItem userItemFromToDo;

    private UserToDoListItemResponseDto userItemResponseFromToDo;

    private UserToDoListItem userItemFromCustomToDo;

    private UserToDoListItemResponseDto userItemResponseFromCustomToDo;

    private CustomToDoListItem customToDoListItem;

    private ToDoListItemTranslation toDoListItemTranslation;

    private ToDoListItem toDoListItem;

    private UserToDoListItemRequestDto userItemRequestFromToDo;

    private UserToDoListItemRequestDto userItemRequestFromCustomToDo;

    private UserToDoListItemRequestWithStatusDto userItemRequestWithStatusFromToDo;

    private UserToDoListItemRequestWithStatusDto userItemRequestWithStatusFromCustomToDo;

    @BeforeEach
    void setUp() {
        habitAssign = getHabitAssign();
        userVO = getUserVO();

        customToDoListItem = getCustomToDoListItem();
        userItemResponseFromCustomToDo = getUserToDoListItemResponseDto();
        userItemResponseFromCustomToDo.setIsCustomItem(true);
        userItemResponseFromCustomToDo.setTargetId(customToDoListItem.getId());
        userItemFromCustomToDo = getUserToDoListItem();
        userItemFromCustomToDo.setIsCustomItem(true);
        userItemFromCustomToDo.setId(2L);
        userItemFromCustomToDo.setTargetId(customToDoListItem.getId());

        toDoListItem = getToDoListItem();
        toDoListItemTranslation = getToDoListItemTranslation();
        userItemResponseFromToDo = getUserToDoListItemResponseDto();
        userItemResponseFromToDo.setIsCustomItem(false);
        userItemResponseFromToDo.setTargetId(toDoListItem.getId());
        userItemFromToDo = getUserToDoListItem();
        userItemFromToDo.setIsCustomItem(false);
        userItemFromToDo.setId(4L);
        userItemFromToDo.setTargetId(toDoListItem.getId());

        userItemRequestFromToDo = getUserToDoListItemRequestDto();
        userItemRequestFromToDo.setIsCustomItem(false);
        userItemRequestFromCustomToDo = getUserToDoListItemRequestDto();
        userItemRequestFromCustomToDo.setIsCustomItem(true);

        userItemRequestWithStatusFromToDo = getUserToDoListItemRequestWithStatusDto();
        userItemRequestWithStatusFromToDo.setIsCustomItem(false);
        userItemRequestWithStatusFromCustomToDo = getUserToDoListItemRequestWithStatusDto();
        userItemRequestWithStatusFromCustomToDo.setIsCustomItem(true);
    }

    @Test
    void findAllForHabitAssign() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId);
        userVO.setId(userId);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId)).thenReturn(userVO);
        when(userToDoListItemRepo.findAllByHabitAssingId(habitAssignId))
            .thenReturn(List.of(userItemFromToDo, userItemFromCustomToDo));
        when(modelMapper.map(userItemFromToDo, UserToDoListItemResponseDto.class)).thenReturn(userItemResponseFromToDo);
        when(modelMapper.map(userItemFromCustomToDo, UserToDoListItemResponseDto.class))
            .thenReturn(userItemResponseFromCustomToDo);
        when(customToDoListItemRepo.getReferenceById(userItemFromCustomToDo.getTargetId()))
            .thenReturn(customToDoListItem);
        when(toDoListItemTranslationRepo.findByLangAndToDoListItemId(languageDefault, userItemFromToDo.getTargetId()))
            .thenReturn(toDoListItemTranslation);

        List<UserToDoListItemResponseDto> result =
            userToDoListItemService.findAllForHabitAssign(habitAssignId, userId, languageDefault);

        UserToDoListItemResponseDto customExpected = getUserToDoListItemResponseDto();
        customExpected.setIsCustomItem(true);
        customExpected.setTargetId(customToDoListItem.getId());
        customExpected.setText(customToDoListItem.getText());

        UserToDoListItemResponseDto toDoExpected = getUserToDoListItemResponseDto();
        toDoExpected.setIsCustomItem(false);
        toDoExpected.setText(toDoListItemTranslation.getContent());
        toDoExpected.setTargetId(userItemFromToDo.getTargetId());

        assertEquals(2, result.size());
        assertTrue(result.contains(customExpected));
        assertTrue(result.contains(toDoExpected));

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userService).findById(userId);
        verify(userToDoListItemRepo).findAllByHabitAssingId(habitAssignId);
        verify(modelMapper).map(userItemFromToDo, UserToDoListItemResponseDto.class);
        verify(modelMapper).map(userItemFromCustomToDo, UserToDoListItemResponseDto.class);
        verify(customToDoListItemRepo).getReferenceById(userItemFromCustomToDo.getTargetId());
        verify(toDoListItemTranslationRepo).findByLangAndToDoListItemId(languageDefault,
            userItemFromToDo.getTargetId());
    }

    @Test
    void findAllForHabitAssignWithWrongHabitId() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;
        habitAssign.setId(habitAssignId + 1);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> userToDoListItemService.findAllForHabitAssign(habitAssignId, userId, languageDefault));

        verify(habitAssignRepo).findById(habitAssignId);

        verifyNoInteractions(userService);
        verifyNoInteractions(userToDoListItemRepo);
        verifyNoInteractions(customToDoListItemRepo);
        verifyNoInteractions(toDoListItemTranslationRepo);
    }

    @Test
    void findAllForHabitAssignWithUserHasNoPermission() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId + 1);
        userVO.setId(userId);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId)).thenReturn(userVO);

        assertThrows(UserHasNoPermissionToAccessException.class,
            () -> userToDoListItemService.findAllForHabitAssign(habitAssignId, userId, languageDefault));

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userService).findById(userId);

        verifyNoInteractions(userToDoListItemRepo);
        verifyNoInteractions(customToDoListItemRepo);
        verifyNoInteractions(toDoListItemTranslationRepo);
    }

    @Test
    void findAllForHabitAssignWithAdminUser() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId + 1);
        userVO.setId(userId);
        userVO.setRole(Role.ROLE_ADMIN);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId)).thenReturn(userVO);
        when(userToDoListItemRepo.findAllByHabitAssingId(habitAssignId)).thenReturn(List.of(userItemFromCustomToDo));
        when(modelMapper.map(userItemFromCustomToDo, UserToDoListItemResponseDto.class))
            .thenReturn(userItemResponseFromCustomToDo);
        when(customToDoListItemRepo.getReferenceById(userItemFromCustomToDo.getTargetId()))
            .thenReturn(customToDoListItem);

        List<UserToDoListItemResponseDto> result =
            userToDoListItemService.findAllForHabitAssign(habitAssignId, userId, languageDefault);

        UserToDoListItemResponseDto customExpected = getUserToDoListItemResponseDto();
        customExpected.setIsCustomItem(true);
        customExpected.setTargetId(customToDoListItem.getId());
        customExpected.setText(customToDoListItem.getText());

        assertEquals(1, result.size());
        assertTrue(result.contains(customExpected));

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userService).findById(userId);
        verify(userToDoListItemRepo).findAllByHabitAssingId(habitAssignId);
        verify(modelMapper).map(userItemFromCustomToDo, UserToDoListItemResponseDto.class);
        verify(customToDoListItemRepo).getReferenceById(userItemFromCustomToDo.getTargetId());
    }

    @Test
    void saveUserToDoListItems() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        Long toDoItemId = 5L;
        Long customItemId = 4L;
        userItemRequestFromToDo.setTargetId(toDoItemId);
        userItemFromToDo.setTargetId(toDoItemId);
        userItemFromToDo.setStatus(UserToDoListItemStatus.INPROGRESS);
        userItemFromToDo.setHabitAssign(habitAssign);
        userItemResponseFromToDo.setTargetId(toDoItemId);
        userItemRequestFromCustomToDo.setTargetId(customItemId);
        userItemFromCustomToDo.setTargetId(customItemId);
        userItemFromCustomToDo.setStatus(UserToDoListItemStatus.INPROGRESS);
        userItemFromCustomToDo.setHabitAssign(habitAssign);
        userItemResponseFromCustomToDo.setTargetId(customItemId);
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId);
        userVO.setId(userId);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId)).thenReturn(userVO);
        when(customToDoListItemRepo.findById(customItemId)).thenReturn(Optional.of(customToDoListItem));
        when(toDoListItemRepo.findById(toDoItemId)).thenReturn(Optional.of(toDoListItem));
        when(habitAssignRepo.getReferenceById(habitAssignId)).thenReturn(habitAssign);
        when(modelMapper.map(userItemRequestFromToDo, UserToDoListItem.class)).thenReturn(userItemFromToDo);
        when(modelMapper.map(userItemRequestFromCustomToDo, UserToDoListItem.class)).thenReturn(userItemFromCustomToDo);
        when(userToDoListItemRepo.findAllByHabitAssingId(habitAssignId))
            .thenReturn(List.of(userItemFromToDo, userItemFromCustomToDo));
        when(modelMapper.map(userItemFromToDo, UserToDoListItemResponseDto.class)).thenReturn(userItemResponseFromToDo);
        when(modelMapper.map(userItemFromCustomToDo, UserToDoListItemResponseDto.class))
            .thenReturn(userItemResponseFromCustomToDo);
        when(customToDoListItemRepo.getReferenceById(customItemId)).thenReturn(customToDoListItem);
        when(toDoListItemTranslationRepo.findByLangAndToDoListItemId(languageDefault, toDoItemId))
            .thenReturn(toDoListItemTranslation);

        List<UserToDoListItemResponseDto> result = userToDoListItemService.saveUserToDoListItems(habitAssignId,
            List.of(userItemRequestFromToDo, userItemRequestFromCustomToDo), userId, languageDefault);
        assertEquals(2, result.size());
        assertEquals(List.of(userItemResponseFromToDo, userItemResponseFromCustomToDo), result);

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userService).findById(userId);
        verify(customToDoListItemRepo).findById(customItemId);
        verify(toDoListItemRepo).findById(toDoItemId);
        verify(habitAssignRepo).getReferenceById(habitAssignId);
        verify(modelMapper).map(userItemRequestFromToDo, UserToDoListItem.class);
        verify(modelMapper).map(userItemRequestFromCustomToDo, UserToDoListItem.class);
        verify(userToDoListItemRepo).saveAll(List.of(userItemFromToDo, userItemFromCustomToDo));
        verify(userToDoListItemRepo).findAllByHabitAssingId(habitAssignId);
        verify(modelMapper).map(userItemFromToDo, UserToDoListItemResponseDto.class);
        verify(modelMapper).map(userItemFromCustomToDo, UserToDoListItemResponseDto.class);
        verify(customToDoListItemRepo).getReferenceById(userItemFromCustomToDo.getTargetId());
        verify(toDoListItemTranslationRepo).findByLangAndToDoListItemId(languageDefault,
            userItemFromToDo.getTargetId());
    }

    @Test
    void deleteUserToDoListItems() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        List<Long> idsToDelete = List.of(1L, 2L);
        List<UserToDoListItem> listToDelete = List.of(userItemFromToDo, userItemFromCustomToDo);

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId);
        userVO.setId(userId);
        userItemFromToDo.getHabitAssign().setId(habitAssignId);
        userItemFromCustomToDo.getHabitAssign().setId(habitAssignId);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId)).thenReturn(userVO);
        when(userToDoListItemRepo.findAllById(idsToDelete)).thenReturn(listToDelete);

        userToDoListItemService.deleteUserToDoListItems(habitAssignId, idsToDelete, userId);

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userService).findById(userId);
        verify(userToDoListItemRepo).findAllById(idsToDelete);
        verify(userToDoListItemRepo).deleteAll(listToDelete);
    }

    @Test
    void deleteUserToDoListItemsWithWrongItemsIds() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        List<Long> idsToDelete = List.of(1L, 2L);
        List<UserToDoListItem> listToDelete = List.of(userItemFromToDo, userItemFromCustomToDo);

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId);
        userVO.setId(userId);
        userItemFromToDo.getHabitAssign().setId(habitAssignId + 1);
        userItemFromCustomToDo.getHabitAssign().setId(habitAssignId + 1);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId)).thenReturn(userVO);
        when(userToDoListItemRepo.findAllById(idsToDelete)).thenReturn(listToDelete);

        assertThrows(BadRequestException.class,
            () -> userToDoListItemService.deleteUserToDoListItems(habitAssignId, idsToDelete, userId));

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userService).findById(userId);
        verify(userToDoListItemRepo).findAllById(idsToDelete);
        verify(userToDoListItemRepo, times(0)).deleteAll(listToDelete);
    }

    @Test
    void changeStatusesUserToDoListItems() {
        Long habitAssignId = 2L;
        Long userId = 3L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId);
        userVO.setId(userId);

        when(habitAssignRepo.findById(habitAssignId)).thenReturn(Optional.of(habitAssign));
        when(userService.findById(userId)).thenReturn(userVO);
        when(userToDoListItemRepo.getCustomToDoItemIdByHabitAssignIdAndItemId(
            habitAssignId, userItemRequestWithStatusFromCustomToDo.getTargetId()))
            .thenReturn(Optional.of(userItemFromCustomToDo));
        when(userToDoListItemRepo.getToDoItemIdByHabitAssignIdAndItemId(
            habitAssignId, userItemRequestWithStatusFromToDo.getTargetId())).thenReturn(Optional.of(userItemFromToDo));
        when(userToDoListItemRepo.findAllByHabitAssingId(habitAssignId))
            .thenReturn(List.of(userItemFromToDo, userItemFromCustomToDo));
        when(modelMapper.map(userItemFromToDo, UserToDoListItemResponseDto.class)).thenReturn(userItemResponseFromToDo);
        when(modelMapper.map(userItemFromCustomToDo, UserToDoListItemResponseDto.class))
            .thenReturn(userItemResponseFromCustomToDo);
        when(customToDoListItemRepo.getReferenceById(userItemResponseFromCustomToDo.getTargetId()))
            .thenReturn(customToDoListItem);
        when(toDoListItemTranslationRepo.findByLangAndToDoListItemId(languageDefault,
            userItemResponseFromToDo.getTargetId())).thenReturn(toDoListItemTranslation);
        List<UserToDoListItemResponseDto> expected = List.of(userItemResponseFromToDo, userItemResponseFromCustomToDo);
        List<UserToDoListItemResponseDto> result = userToDoListItemService.changeStatusesUserToDoListItems(
            habitAssignId, List.of(userItemRequestWithStatusFromToDo, userItemRequestWithStatusFromCustomToDo), userId,
            languageDefault);

        assertEquals(2, result.size());
        assertEquals(expected, result);

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userService).findById(userId);
        verify(userToDoListItemRepo).getCustomToDoItemIdByHabitAssignIdAndItemId(habitAssignId,
            userItemRequestWithStatusFromCustomToDo.getTargetId());
        verify(userToDoListItemRepo).getToDoItemIdByHabitAssignIdAndItemId(habitAssignId,
            userItemRequestWithStatusFromToDo.getTargetId());
        verify(userToDoListItemRepo).saveAll(List.of(userItemFromToDo, userItemFromCustomToDo));
        verify(userToDoListItemRepo).findAllByHabitAssingId(habitAssignId);
        verify(modelMapper).map(userItemFromToDo, UserToDoListItemResponseDto.class);
        verify(modelMapper).map(userItemFromCustomToDo, UserToDoListItemResponseDto.class);
        verify(customToDoListItemRepo).getReferenceById(userItemFromCustomToDo.getTargetId());
        verify(toDoListItemTranslationRepo).findByLangAndToDoListItemId(languageDefault,
            userItemFromToDo.getTargetId());
    }
}