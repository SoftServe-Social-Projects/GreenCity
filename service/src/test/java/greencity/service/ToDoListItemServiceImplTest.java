package greencity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.dto.todolistitem.ToDoListItemDto;
import greencity.dto.todolistitem.ToDoListItemManagementDto;
import greencity.dto.todolistitem.ToDoListItemPostDto;
import greencity.dto.todolistitem.ToDoListItemRequestDto;
import greencity.dto.todolistitem.ToDoListItemResponseDto;
import greencity.dto.user.UserToDoListItemResponseDto;
import greencity.entity.HabitAssign;
import greencity.entity.Language;
import greencity.entity.ToDoListItem;
import greencity.entity.User;
import greencity.entity.UserToDoListItem;
import greencity.entity.localization.ToDoListItemTranslation;
import greencity.enums.EmailNotification;
import greencity.enums.Role;
import greencity.enums.ToDoListItemStatus;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotDeletedException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.ToDoListItemNotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.exception.exceptions.UserHasNoToDoListItemsException;
import greencity.exception.exceptions.UserToDoListItemStatusNotUpdatedException;
import greencity.exception.exceptions.WrongIdException;
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
import org.modelmapper.TypeToken;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static greencity.enums.UserStatus.ACTIVATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
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
    @InjectMocks
    private ToDoListItemServiceImpl toDoListItemService;
    @Mock
    private final ToDoListItem toDoListItem =
        ToDoListItem.builder().id(1L).translations(ModelUtils.getToDoListItemTranslations()).build();

    private final List<LanguageTranslationDTO> languageTranslationDTOS =
        Collections.singletonList(ModelUtils.getLanguageTranslationDTO());
    private final ToDoListItemPostDto toDoListItemPostDto =
        new ToDoListItemPostDto(languageTranslationDTOS, new ToDoListItemRequestDto(1L));

    private HabitAssign habitAssign;
    private User user = User.builder()
        .id(1L)
        .name("Test Testing")
        .email("test@gmail.com")
        .role(Role.ROLE_USER)
        .userStatus(ACTIVATED)
        .emailNotification(EmailNotification.DISABLED)
        .lastActivityTime(LocalDateTime.of(2020, 10, 10, 20, 10, 10))
        .dateOfRegistration(LocalDateTime.now())
        .socialNetworks(new ArrayList<>())
        .build();

    private String language = "uk";

    private List<ToDoListItemTranslation> toDoListItemTranslations = Arrays.asList(
        ToDoListItemTranslation.builder()
            .id(1L)
            .language(new Language(1L, language, Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList()))
            .content("TEST")
            .toDoListItem(
                new ToDoListItem(1L, Collections.emptyList(), Collections.emptySet(), Collections.emptyList()))
            .build(),
        ToDoListItemTranslation.builder()
            .id(2L)
            .language(new Language(1L, language, Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList()))
            .content("TEST")
            .toDoListItem(
                new ToDoListItem(2L, Collections.emptyList(), Collections.emptySet(), Collections.emptyList()))
            .build());

    List<ToDoListItemRequestDto> toDoListItemRequestDtos =
        Arrays.asList(new ToDoListItemRequestDto(1L), new ToDoListItemRequestDto(2L),
            new ToDoListItemRequestDto(3L));

    private Long userId = user.getId();

    @BeforeEach
    void setUp() {
        habitAssign = ModelUtils.getHabitAssign();
    }

    @Test
    void saveUserToDoListItemTest() {
        ObjectMapper mapper = new ObjectMapper();
        UserToDoListItem userToDoListItem =
            mapper.convertValue(toDoListItemRequestDtos.getFirst(), UserToDoListItem.class);
        when(habitAssignRepo.findByHabitIdAndUserId(1L, userId))
            .thenReturn(Optional.of(habitAssign));
        when(userToDoListItemRepo.getToDoListItemsIdForHabit(habitAssign.getHabit().getId()))
            .thenReturn(Collections.singletonList(1L));
        when(userToDoListItemRepo.getAllAssignedToDoListItems(habitAssign.getId()))
            .thenReturn(Collections.singletonList(2L));
        when(modelMapper.map(toDoListItemRequestDtos.getFirst(), UserToDoListItem.class))
            .thenReturn(userToDoListItem);
        getUserToDoListItemTest();
        userToDoListItem.setHabitAssign(habitAssign);
        toDoListItemService
            .saveUserToDoListItems(userId, 1L, Collections.singletonList(toDoListItemRequestDtos.getFirst()),
                "en");
        verify(userToDoListItemRepo).saveAll(Collections.singletonList(userToDoListItem));
    }

    @Test
    void saveUserToDoListItemThorowsNotFoundException() {
        when(habitAssignRepo.findByHabitIdAndUserId(1L, userId))
            .thenReturn(Optional.of(habitAssign));
        when(userToDoListItemRepo.getToDoListItemsIdForHabit(habitAssign.getHabit().getId()))
            .thenReturn(Collections.singletonList(1L));
        List<ToDoListItemRequestDto> toDoListItemRequestDto =
            Collections.singletonList(toDoListItemRequestDtos.get(2));
        assertThrows(NotFoundException.class, () -> toDoListItemService
            .saveUserToDoListItems(userId, 1L, toDoListItemRequestDto, "en"));
    }

    @Test
    void saveUserToDoListItemThorowsWrongIdException() {
        when(habitAssignRepo.findByHabitIdAndUserId(1L, userId))
            .thenReturn(Optional.of(habitAssign));
        when(userToDoListItemRepo.getToDoListItemsIdForHabit(habitAssign.getHabit().getId()))
            .thenReturn(Collections.singletonList(1L));
        when(userToDoListItemRepo.getAllAssignedToDoListItems(habitAssign.getId()))
            .thenReturn(Collections.singletonList(1L));
        List<ToDoListItemRequestDto> toDoListItemRequestDto =
            Collections.singletonList(toDoListItemRequestDtos.getFirst());
        assertThrows(WrongIdException.class, () -> toDoListItemService
            .saveUserToDoListItems(userId, 1L, toDoListItemRequestDto, "en"));
    }

    @Test
    void saveUserToDoListItemThrowException() {

        List<ToDoListItemRequestDto> toDoListItemRequestDto =
            Collections.singletonList(toDoListItemRequestDtos.getFirst());

        assertThrows(UserHasNoToDoListItemsException.class, () -> toDoListItemService
            .saveUserToDoListItems(userId, 1L, toDoListItemRequestDto, "en"));
    }

    @Test
    void saveUserToDoListItemWithEmptyList() {
        List<ToDoListItemRequestDto> dtoList = null;
        Long habitId = 1L;
        String languageEn = "en";
        UserToDoListItem userToDoListItem =
            UserToDoListItem.builder().id(1L).status(ToDoListItemStatus.ACTIVE).build();

        List<UserToDoListItemResponseDto> expected =
            List.of(ModelUtils.getUserToDoListItemResponseDto());

        when(habitAssignRepo.findByHabitIdAndUserId(habitId, userId))
            .thenReturn(Optional.of(habitAssign));
        when(userToDoListItemRepo.findAllByHabitAssingId(habitAssign.getId())).thenReturn(Collections.singletonList(
            userToDoListItem));
        when(modelMapper.map(userToDoListItem, UserToDoListItemResponseDto.class)).thenReturn(expected.get(0));
        when(toDoListItemTranslationRepo.findByLangAndUserToDoListItemId(languageEn, 1L))
            .thenReturn(ToDoListItemTranslation.builder().id(1L).build());

        List<UserToDoListItemResponseDto> actual = toDoListItemService
            .saveUserToDoListItems(userId, habitId, dtoList, languageEn);

        assertEquals(expected, actual);
    }

    @Test
    void findAllTest() {
        List<ToDoListItemDto> toDoListItemDto = toDoListItemTranslations
            .stream()
            .map(translation -> new ToDoListItemDto(translation.getToDoListItem().getId(),
                translation.getContent(), ToDoListItemStatus.ACTIVE.toString()))
            .collect(Collectors.toList());

        when(modelMapper.map(toDoListItemTranslations.getFirst(), ToDoListItemDto.class))
            .thenReturn(toDoListItemDto.get(0));
        when(modelMapper.map(toDoListItemTranslations.get(1), ToDoListItemDto.class))
            .thenReturn(toDoListItemDto.get(1));
        when(toDoListItemTranslationRepo.findAllByLanguageCode(AppConstant.DEFAULT_LANGUAGE_CODE))
            .thenReturn(toDoListItemTranslations);

        assertEquals(toDoListItemService.findAll(AppConstant.DEFAULT_LANGUAGE_CODE), toDoListItemDto);
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
    void updateUserToDoListItemStatusWithNonExistentItemIdTest() {
        assertThrows(NullPointerException.class, () -> toDoListItemService
            .updateUserToDoListItemStatus(userId, 2L, "en"));
    }

    @Test
    void updateUserToDoListItemStatusWithActiveItemStateTest() {
        UserToDoListItem userToDoListItem = ModelUtils.getPredefinedUserToDoListItem();
        when(userToDoListItemRepo.getReferenceById(userToDoListItem.getId())).thenReturn(userToDoListItem);
        when(modelMapper.map(any(), eq(UserToDoListItemResponseDto.class)))
            .thenReturn(new UserToDoListItemResponseDto(2L, null, ToDoListItemStatus.DONE));
        when(toDoListItemTranslationRepo.findByLangAndUserToDoListItemId(language,
            userToDoListItem.getId())).thenReturn(toDoListItemTranslations.getFirst());
        UserToDoListItemResponseDto userToDoListItemResponseDto =
            toDoListItemService.updateUserToDoListItemStatus(userId, userToDoListItem.getId(), "uk");

        assertEquals(ToDoListItemStatus.DONE, userToDoListItem.getStatus());
        assertEquals(userToDoListItemResponseDto.getId(),
            new UserToDoListItemResponseDto(2L, toDoListItemTranslations.getFirst().getContent(),
                ToDoListItemStatus.DONE).getId());
        verify(userToDoListItemRepo).save(userToDoListItem);
    }

    @Test
    void updateUserToDoListItemStatusWithDoneItemStateTest() {
        UserToDoListItem userToDoListItem =
            new UserToDoListItem(1L, null, null, ToDoListItemStatus.DONE, null);
        when(userToDoListItemRepo.getReferenceById(userToDoListItem.getId())).thenReturn(userToDoListItem);
        Long userToDoListItemId = userToDoListItem.getId();
        assertThrows(UserToDoListItemStatusNotUpdatedException.class,
            () -> toDoListItemService.updateUserToDoListItemStatus(userId, userToDoListItemId, "en"));
        assertNotEquals(ToDoListItemStatus.ACTIVE, userToDoListItem.getStatus());
    }

    @Test
    void updateUserToDoListItemStatusTest() {
        UserToDoListItem userToDoListItem = ModelUtils.getPredefinedUserToDoListItem();
        when(userToDoListItemRepo.getAllByUserToDoListIdAndUserId(1L, 2L))
            .thenReturn(List.of(userToDoListItem));
        when(modelMapper.map(userToDoListItem, UserToDoListItemResponseDto.class))
            .thenReturn(UserToDoListItemResponseDto.builder()
                .id(1L)
                .status(ToDoListItemStatus.DONE)
                .build());
        when(toDoListItemTranslationRepo.findByLangAndUserToDoListItemId("en", 1L))
            .thenReturn(ModelUtils.getToDoListItemTranslation());

        List<UserToDoListItemResponseDto> result = toDoListItemService
            .updateUserToDoListItemStatus(2L, 1L, "en", "DONE");

        assertEquals(ToDoListItemStatus.DONE, result.getFirst().getStatus());

        verify(userToDoListItemRepo).getAllByUserToDoListIdAndUserId(1L, 2L);
        verify(modelMapper).map(userToDoListItem, UserToDoListItemResponseDto.class);
        verify(toDoListItemTranslationRepo).findByLangAndUserToDoListItemId("en", 1L);
    }

    @Test
    void updateUserToDoListItemStatusShouldThrowNotFoundExceptionTest() {
        when(userToDoListItemRepo.getAllByUserToDoListIdAndUserId(1L, 2L))
            .thenReturn(null);

        Exception thrown = assertThrows(NotFoundException.class, () -> toDoListItemService
            .updateUserToDoListItemStatus(2L, 1L, "en", "DONE"));

        assertEquals(ErrorMessage.USER_TO_DO_LIST_ITEM_NOT_FOUND_BY_USER_ID, thrown.getMessage());
        verify(userToDoListItemRepo).getAllByUserToDoListIdAndUserId(1L, 2L);
    }

    @Test
    void updateUserToDoListItemStatusShouldThrowBadRequestExceptionTest() {
        when(userToDoListItemRepo.getAllByUserToDoListIdAndUserId(1L, 2L))
            .thenReturn(List.of(ModelUtils.getPredefinedUserToDoListItem()));

        Exception thrown = assertThrows(BadRequestException.class, () -> toDoListItemService
            .updateUserToDoListItemStatus(2L, 1L, "en", "Wrong Status"));

        assertEquals(ErrorMessage.INCORRECT_INPUT_ITEM_STATUS, thrown.getMessage());
        verify(userToDoListItemRepo).getAllByUserToDoListIdAndUserId(1L, 2L);
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
        when(modelMapper.map(object.get(), ToDoListItemResponseDto.class))
            .thenReturn(new ToDoListItemResponseDto());

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
    void deleteUserToDoListItems() {
        String ids = "1,2,3,4,5,6";
        List<Long> expected = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L);
        UserToDoListItem userToDoListItem =
            new UserToDoListItem(1L, null, toDoListItem, ToDoListItemStatus.ACTIVE, null);

        when(userToDoListItemRepo.findById(anyLong())).thenReturn(Optional.of(userToDoListItem));

        assertEquals(expected, toDoListItemService.deleteUserToDoListItems(ids));
        verify(userToDoListItemRepo, times(6)).delete(userToDoListItem);
    }

    @Test
    void deleteUserToDoListItemsFailed() {
        String ids = "1,2,3,4,5,6";
        UserToDoListItem userToDoListItem =
            new UserToDoListItem(1L, null, toDoListItem, ToDoListItemStatus.ACTIVE, null);

        when(userToDoListItemRepo.findById(anyLong())).thenReturn(Optional.of(userToDoListItem));
        when(userToDoListItemRepo.findById(3L)).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> toDoListItemService.deleteUserToDoListItems(ids));
    }

    @Test
    void getUserToDoListItemTest() {
        UserToDoListItem userToDoListItem =
            UserToDoListItem.builder().id(1L).status(ToDoListItemStatus.ACTIVE).build();
        when(habitAssignRepo.findByHabitIdAndUserId(userId, 1L))
            .thenReturn(Optional.of(habitAssign));
        when(userToDoListItemRepo.findAllByHabitAssingId(habitAssign.getId())).thenReturn(Collections.singletonList(
            userToDoListItem));
        when(modelMapper.map(userToDoListItem, UserToDoListItemResponseDto.class))
            .thenReturn(UserToDoListItemResponseDto.builder().id(1L).build());
        when(toDoListItemTranslationRepo.findByLangAndUserToDoListItemId("en", 1L))
            .thenReturn(ToDoListItemTranslation.builder().id(1L).build());
        assertEquals(1L, toDoListItemService.getUserToDoList(userId, 1L, "en").get(0).getId());
    }

    @Test
    void getEmptyUserToDoListItemsTest() {
        when(habitAssignRepo.findByHabitIdAndUserId(userId, 1L))
            .thenReturn(Optional.of(habitAssign));
        when(userToDoListItemRepo.findAllByHabitAssingId(habitAssign.getId())).thenReturn(Collections.emptyList());

        assertTrue(toDoListItemService.getUserToDoList(userId, 1L, "en").isEmpty());
    }

    @Test
    void getUserToDoListItemWithNullTest() {
        Long habitId = 1L;
        String languageEn = "en";
        List<UserToDoListItemResponseDto> expected = Collections.emptyList();

        when(habitAssignRepo.findByHabitIdAndUserId(userId, 1L))
            .thenReturn(Optional.empty());

        List<UserToDoListItemResponseDto> actual = toDoListItemService
            .getUserToDoList(userId, habitId, languageEn);
        assertEquals(expected, actual);
    }

    @Test
    void getUserToDoListItemWithStatusInProgressTest() {
        UserToDoListItem item = UserToDoListItem
            .builder().id(1L).status(ToDoListItemStatus.INPROGRESS).build();

        UserToDoListItemResponseDto itemResponseDto = UserToDoListItemResponseDto
            .builder().id(1L).status(ToDoListItemStatus.INPROGRESS).build();

        when(userToDoListItemRepo.findUserToDoListItemsByHabitAssignIdAndStatusInProgress(1L))
            .thenReturn(List.of(
                item));
        when(modelMapper.map(item, UserToDoListItemResponseDto.class))
            .thenReturn(itemResponseDto);
        when(toDoListItemTranslationRepo.findByLangAndUserToDoListItemId("en", 1L))
            .thenReturn(ToDoListItemTranslation.builder().id(1L).build());

        assertEquals(List.of(itemResponseDto), toDoListItemService
            .getUserToDoListItemsByHabitAssignIdAndStatusInProgress(1L, "en"));

        verify(userToDoListItemRepo).findUserToDoListItemsByHabitAssignIdAndStatusInProgress(anyLong());
        verify(toDoListItemTranslationRepo).findByLangAndUserToDoListItemId(any(), anyLong());
        verify(modelMapper).map(any(), any());
    }

    @Test
    void deleteUserToDoListItemByItemIdAndUserIdAndHabitIdTest() {
        when(habitAssignRepo.findByHabitIdAndUserId(1L, userId))
            .thenReturn(Optional.of(habitAssign));
        toDoListItemService.deleteUserToDoListItemByItemIdAndUserIdAndHabitId(1L, userId, 1L);
        verify(userToDoListItemRepo).deleteByToDoListItemIdAndHabitAssignId(1L, 1L);
    }

    @Test
    void deleteUserShollingListItemByItemIdAndUserIdAndHabitIdTestThorows() {
        assertThrows(NotFoundException.class,
            () -> toDoListItemService.deleteUserToDoListItemByItemIdAndUserIdAndHabitId(1L, userId, 1L));
    }

    @Test
    void getUserToDoListItemIfThereAreNoItems() {
        when(habitAssignRepo.findByHabitIdAndUserId(userId, 1L))
            .thenReturn(Optional.of(habitAssign));
        when(userToDoListItemRepo.findAllByHabitAssingId(habitAssign.getId())).thenReturn(Collections.emptyList());

        assertEquals(Collections.emptyList(),
            toDoListItemService.getUserToDoList(userId, 1L, "en"));
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

        when(toDoListItemRepo.getAllToDoListItemIdByHabitIdISContained(1L)).thenReturn(listID);
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
    void findInProgressByUserIdAndLanguageCodeTest() {
        when(toDoListItemService.findInProgressByUserIdAndLanguageCode(1L, "ua"))
            .thenReturn(new ArrayList<>());
        assertEquals(0, toDoListItemRepo.findInProgressByUserIdAndLanguageCode(1L, "ua").size());
    }

    @Test
    void getUserToDoListByHabitAssignIdTest() {
        Long habitAssignId = 2L;
        Long userId3 = 3L;
        Long userToDoListItemId = 4L;
        Long toDoListItemTranslationId = 5L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;
        String text = "text";

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId3);

        UserToDoListItem userToDoListItem = ModelUtils.getUserToDoListItem();
        userToDoListItem.setId(userToDoListItemId);
        userToDoListItem.setStatus(ToDoListItemStatus.ACTIVE);

        UserToDoListItemResponseDto userToDoListItemResponseDto =
            ModelUtils.getUserToDoListItemResponseDto();
        userToDoListItemResponseDto.setId(userToDoListItemId);

        ToDoListItemTranslation toDoListItemTranslation = ModelUtils.getToDoListItemTranslation();
        toDoListItemTranslation.setId(toDoListItemTranslationId);
        toDoListItemTranslation.setContent(text);

        UserToDoListItemResponseDto expectedDto = ModelUtils.getUserToDoListItemResponseDto();
        expectedDto.setId(userToDoListItemId);
        expectedDto.setText(text);

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));
        when(userToDoListItemRepo.findAllByHabitAssingId(habitAssignId)).thenReturn(Collections.singletonList(
            userToDoListItem));
        when(modelMapper.map(userToDoListItem, UserToDoListItemResponseDto.class))
            .thenReturn(userToDoListItemResponseDto);
        when(toDoListItemTranslationRepo.findByLangAndUserToDoListItemId(languageDefault,
            userToDoListItemId))
            .thenReturn(toDoListItemTranslation);

        List<UserToDoListItemResponseDto> actualDtoList = toDoListItemService
            .getUserToDoListByHabitAssignId(userId3, habitAssignId, languageDefault);

        assertNotNull(actualDtoList);
        assertEquals(1, actualDtoList.size());
        assertEquals(expectedDto, actualDtoList.getFirst());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userToDoListItemRepo).findAllByHabitAssingId(habitAssignId);
        verify(modelMapper).map(userToDoListItem, UserToDoListItemResponseDto.class);
        verify(toDoListItemTranslationRepo).findByLangAndUserToDoListItemId(languageDefault,
            userToDoListItemId);
    }

    @Test
    void getUserToDoListByHabitAssignIdReturnEmptyListTest() {
        Long habitAssignId = 2L;
        Long userId3 = 3L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId3);

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));
        when(userToDoListItemRepo.findAllByHabitAssingId(habitAssignId)).thenReturn(Collections.emptyList());

        assertEquals(Collections.emptyList(),
            toDoListItemService.getUserToDoListByHabitAssignId(userId3, habitAssignId, languageDefault));

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userToDoListItemRepo).findAllByHabitAssingId(habitAssignId);
        verify(modelMapper, times(0)).map(any(), any());
        verify(toDoListItemTranslationRepo, times(0)).findByLangAndUserToDoListItemId(any(), anyLong());
    }

    @Test
    void getUserToDoListByHabitAssignIdThrowsExceptionWhenHabitAssignNotExists() {
        Long habitAssignId = 2L;
        Long userId3 = 3L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> toDoListItemService
            .getUserToDoListByHabitAssignId(userId3, habitAssignId, languageDefault));

        assertEquals(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userToDoListItemRepo, times(0)).findAllByHabitAssingId(anyLong());
        verify(modelMapper, times(0)).map(any(), any());
        verify(toDoListItemTranslationRepo, times(0)).findByLangAndUserToDoListItemId(any(), anyLong());
    }

    @Test
    void getUserToDoListByHabitAssignIdThrowsExceptionWhenHabitAssignNotBelongsToUser() {
        long habitAssignId = 2L;
        long userId3 = 3L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId3 + 1);

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));

        UserHasNoPermissionToAccessException exception =
            assertThrows(UserHasNoPermissionToAccessException.class, () -> toDoListItemService
                .getUserToDoListByHabitAssignId(userId3, habitAssignId, languageDefault));

        assertEquals(ErrorMessage.USER_HAS_NO_PERMISSION, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userToDoListItemRepo, times(0)).findAllByHabitAssingId(anyLong());
        verify(modelMapper, times(0)).map(any(), any());
        verify(toDoListItemTranslationRepo, times(0)).findByLangAndUserToDoListItemId(any(), anyLong());
    }
}
