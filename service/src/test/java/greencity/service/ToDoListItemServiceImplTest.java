package greencity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.dto.shoppinglistitem.ShoppingListItemDto;
import greencity.dto.shoppinglistitem.ShoppingListItemManagementDto;
import greencity.dto.shoppinglistitem.ShoppingListItemPostDto;
import greencity.dto.shoppinglistitem.ShoppingListItemRequestDto;
import greencity.dto.shoppinglistitem.ShoppingListItemResponseDto;
import greencity.dto.user.UserShoppingListItemResponseDto;
import greencity.entity.HabitAssign;
import greencity.entity.Language;
import greencity.entity.ShoppingListItem;
import greencity.entity.User;
import greencity.entity.UserShoppingListItem;
import greencity.entity.localization.ShoppingListItemTranslation;
import greencity.enums.EmailNotification;
import greencity.enums.Role;
import greencity.enums.ToDoListItemStatus;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotDeletedException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.ShoppingListItemNotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.exception.exceptions.UserHasNoShoppingListItemsException;
import greencity.exception.exceptions.UserShoppingListItemStatusNotUpdatedException;
import greencity.exception.exceptions.WrongIdException;
import greencity.repository.HabitAssignRepo;
import greencity.repository.ShoppingListItemRepo;
import greencity.repository.ShoppingListItemTranslationRepo;
import greencity.repository.UserShoppingListItemRepo;
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
class ShoppingListItemServiceImplTest {
    @Mock
    private ShoppingListItemTranslationRepo shoppingListItemTranslationRepo;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    UserShoppingListItemRepo userShoppingListItemRepo;
    @Mock
    HabitAssignRepo habitAssignRepo;
    @Mock
    private ShoppingListItemRepo shoppingListItemRepo;
    @InjectMocks
    private ShoppingListItemServiceImpl shoppingListItemService;
    @Mock
    private final ShoppingListItem shoppingListItem =
        ShoppingListItem.builder().id(1L).translations(ModelUtils.getShoppingListItemTranslations()).build();

    private final List<LanguageTranslationDTO> languageTranslationDTOS =
        Collections.singletonList(ModelUtils.getLanguageTranslationDTO());
    private final ShoppingListItemPostDto shoppingListItemPostDto =
        new ShoppingListItemPostDto(languageTranslationDTOS, new ShoppingListItemRequestDto(1L));

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

    private List<ShoppingListItemTranslation> shoppingListItemTranslations = Arrays.asList(
        ShoppingListItemTranslation.builder()
            .id(1L)
            .language(new Language(1L, language, Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList()))
            .content("TEST")
            .shoppingListItem(
                new ShoppingListItem(1L, Collections.emptyList(), Collections.emptySet(), Collections.emptyList()))
            .build(),
        ShoppingListItemTranslation.builder()
            .id(2L)
            .language(new Language(1L, language, Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList()))
            .content("TEST")
            .shoppingListItem(
                new ShoppingListItem(2L, Collections.emptyList(), Collections.emptySet(), Collections.emptyList()))
            .build());

    List<ShoppingListItemRequestDto> shoppingListItemRequestDtos =
        Arrays.asList(new ShoppingListItemRequestDto(1L), new ShoppingListItemRequestDto(2L),
            new ShoppingListItemRequestDto(3L));

    private Long userId = user.getId();

    @BeforeEach
    void setUp() {
        habitAssign = ModelUtils.getHabitAssign();
    }

    @Test
    void saveUserShoppingListItemTest() {
        ObjectMapper mapper = new ObjectMapper();
        UserShoppingListItem userShoppingListItem =
            mapper.convertValue(shoppingListItemRequestDtos.getFirst(), UserShoppingListItem.class);
        when(habitAssignRepo.findByHabitIdAndUserId(1L, userId))
            .thenReturn(Optional.of(habitAssign));
        when(userShoppingListItemRepo.getShoppingListItemsIdForHabit(habitAssign.getHabit().getId()))
            .thenReturn(Collections.singletonList(1L));
        when(userShoppingListItemRepo.getAllAssignedShoppingListItems(habitAssign.getId()))
            .thenReturn(Collections.singletonList(2L));
        when(modelMapper.map(shoppingListItemRequestDtos.getFirst(), UserShoppingListItem.class))
            .thenReturn(userShoppingListItem);
        getUserShoppingListItemTest();
        userShoppingListItem.setHabitAssign(habitAssign);
        shoppingListItemService
            .saveUserShoppingListItems(userId, 1L, Collections.singletonList(shoppingListItemRequestDtos.getFirst()),
                "en");
        verify(userShoppingListItemRepo).saveAll(Collections.singletonList(userShoppingListItem));
    }

    @Test
    void saveUserShoppingListItemThorowsNotFoundException() {
        when(habitAssignRepo.findByHabitIdAndUserId(1L, userId))
            .thenReturn(Optional.of(habitAssign));
        when(userShoppingListItemRepo.getShoppingListItemsIdForHabit(habitAssign.getHabit().getId()))
            .thenReturn(Collections.singletonList(1L));
        List<ShoppingListItemRequestDto> shoppingListItemRequestDto =
            Collections.singletonList(shoppingListItemRequestDtos.get(2));
        assertThrows(NotFoundException.class, () -> shoppingListItemService
            .saveUserShoppingListItems(userId, 1L, shoppingListItemRequestDto, "en"));
    }

    @Test
    void saveUserShoppingListItemThorowsWrongIdException() {
        when(habitAssignRepo.findByHabitIdAndUserId(1L, userId))
            .thenReturn(Optional.of(habitAssign));
        when(userShoppingListItemRepo.getShoppingListItemsIdForHabit(habitAssign.getHabit().getId()))
            .thenReturn(Collections.singletonList(1L));
        when(userShoppingListItemRepo.getAllAssignedShoppingListItems(habitAssign.getId()))
            .thenReturn(Collections.singletonList(1L));
        List<ShoppingListItemRequestDto> shoppingListItemRequestDto =
            Collections.singletonList(shoppingListItemRequestDtos.getFirst());
        assertThrows(WrongIdException.class, () -> shoppingListItemService
            .saveUserShoppingListItems(userId, 1L, shoppingListItemRequestDto, "en"));
    }

    @Test
    void saveUserShoppingListItemThrowException() {

        List<ShoppingListItemRequestDto> shoppingListItemRequestDto =
            Collections.singletonList(shoppingListItemRequestDtos.getFirst());

        assertThrows(UserHasNoShoppingListItemsException.class, () -> shoppingListItemService
            .saveUserShoppingListItems(userId, 1L, shoppingListItemRequestDto, "en"));
    }

    @Test
    void saveUserShoppingListItemWithEmptyList() {
        List<ShoppingListItemRequestDto> dtoList = null;
        Long habitId = 1L;
        String languageEn = "en";
        UserShoppingListItem userShoppingListItem =
            UserShoppingListItem.builder().id(1L).status(ToDoListItemStatus.ACTIVE).build();

        List<UserShoppingListItemResponseDto> expected =
            List.of(ModelUtils.getUserShoppingListItemResponseDto());

        when(habitAssignRepo.findByHabitIdAndUserId(habitId, userId))
            .thenReturn(Optional.of(habitAssign));
        when(userShoppingListItemRepo.findAllByHabitAssingId(habitAssign.getId())).thenReturn(Collections.singletonList(
            userShoppingListItem));
        when(modelMapper.map(userShoppingListItem, UserShoppingListItemResponseDto.class)).thenReturn(expected.get(0));
        when(shoppingListItemTranslationRepo.findByLangAndUserShoppingListItemId(languageEn, 1L))
            .thenReturn(ShoppingListItemTranslation.builder().id(1L).build());

        List<UserShoppingListItemResponseDto> actual = shoppingListItemService
            .saveUserShoppingListItems(userId, habitId, dtoList, languageEn);

        assertEquals(expected, actual);
    }

    @Test
    void findAllTest() {
        List<ShoppingListItemDto> shoppingListItemDto = shoppingListItemTranslations
            .stream()
            .map(translation -> new ShoppingListItemDto(translation.getShoppingListItem().getId(),
                translation.getContent(), ToDoListItemStatus.ACTIVE.toString()))
            .collect(Collectors.toList());

        when(modelMapper.map(shoppingListItemTranslations.getFirst(), ShoppingListItemDto.class))
            .thenReturn(shoppingListItemDto.get(0));
        when(modelMapper.map(shoppingListItemTranslations.get(1), ShoppingListItemDto.class))
            .thenReturn(shoppingListItemDto.get(1));
        when(shoppingListItemTranslationRepo.findAllByLanguageCode(AppConstant.DEFAULT_LANGUAGE_CODE))
            .thenReturn(shoppingListItemTranslations);

        assertEquals(shoppingListItemService.findAll(AppConstant.DEFAULT_LANGUAGE_CODE), shoppingListItemDto);
    }

    @Test
    void saveShoppingListItemTest() {
        when((modelMapper.map(shoppingListItemPostDto, ShoppingListItem.class))).thenReturn(shoppingListItem);
        when(modelMapper.map(shoppingListItem.getTranslations(),
            new TypeToken<List<LanguageTranslationDTO>>() {
            }.getType())).thenReturn(languageTranslationDTOS);
        List<LanguageTranslationDTO> res = shoppingListItemService.saveShoppingListItem(shoppingListItemPostDto);
        assertEquals(languageTranslationDTOS.getFirst().getContent(), res.getFirst().getContent());
    }

    @Test
    void updateTest() {
        when(shoppingListItemRepo.findById(shoppingListItemPostDto.getShoppingListItem().getId()))
            .thenReturn(Optional.of(shoppingListItem));
        when(modelMapper.map(shoppingListItem.getTranslations(),
            new TypeToken<List<LanguageTranslationDTO>>() {
            }.getType())).thenReturn(languageTranslationDTOS);
        List<LanguageTranslationDTO> res = shoppingListItemService.update(shoppingListItemPostDto);
        assertEquals(languageTranslationDTOS.getFirst().getContent(), res.getFirst().getContent());
    }

    @Test
    void updateThrowsTest() {
        assertThrows(ShoppingListItemNotFoundException.class,
            () -> shoppingListItemService.update(shoppingListItemPostDto));
    }

    @Test
    void updateUserShoppingListItemStatusWithNonExistentItemIdTest() {
        assertThrows(NullPointerException.class, () -> shoppingListItemService
            .updateUserShopingListItemStatus(userId, 2L, "en"));
    }

    @Test
    void updateUserShoppingListItemStatusWithActiveItemStateTest() {
        UserShoppingListItem userShoppingListItem = ModelUtils.getPredefinedUserShoppingListItem();
        when(userShoppingListItemRepo.getReferenceById(userShoppingListItem.getId())).thenReturn(userShoppingListItem);
        when(modelMapper.map(any(), eq(UserShoppingListItemResponseDto.class)))
            .thenReturn(new UserShoppingListItemResponseDto(2L, null, ToDoListItemStatus.DONE));
        when(shoppingListItemTranslationRepo.findByLangAndUserShoppingListItemId(language,
            userShoppingListItem.getId())).thenReturn(shoppingListItemTranslations.getFirst());
        UserShoppingListItemResponseDto userShoppingListItemResponseDto =
            shoppingListItemService.updateUserShopingListItemStatus(userId, userShoppingListItem.getId(), "uk");

        assertEquals(ToDoListItemStatus.DONE, userShoppingListItem.getStatus());
        assertEquals(userShoppingListItemResponseDto.getId(),
            new UserShoppingListItemResponseDto(2L, shoppingListItemTranslations.getFirst().getContent(),
                ToDoListItemStatus.DONE).getId());
        verify(userShoppingListItemRepo).save(userShoppingListItem);
    }

    @Test
    void updateUserShoppingListItemStatusWithDoneItemStateTest() {
        UserShoppingListItem userShoppingListItem =
            new UserShoppingListItem(1L, null, null, ToDoListItemStatus.DONE, null);
        when(userShoppingListItemRepo.getReferenceById(userShoppingListItem.getId())).thenReturn(userShoppingListItem);
        Long userShoppingListItemId = userShoppingListItem.getId();
        assertThrows(UserShoppingListItemStatusNotUpdatedException.class,
            () -> shoppingListItemService.updateUserShopingListItemStatus(userId, userShoppingListItemId, "en"));
        assertNotEquals(ToDoListItemStatus.ACTIVE, userShoppingListItem.getStatus());
    }

    @Test
    void updateUserShoppingListItemStatusTest() {
        UserShoppingListItem userShoppingListItem = ModelUtils.getPredefinedUserShoppingListItem();
        when(userShoppingListItemRepo.getAllByUserShoppingListIdAndUserId(1L, 2L))
            .thenReturn(List.of(userShoppingListItem));
        when(modelMapper.map(userShoppingListItem, UserShoppingListItemResponseDto.class))
            .thenReturn(UserShoppingListItemResponseDto.builder()
                .id(1L)
                .status(ToDoListItemStatus.DONE)
                .build());
        when(shoppingListItemTranslationRepo.findByLangAndUserShoppingListItemId("en", 1L))
            .thenReturn(ModelUtils.getShoppingListItemTranslation());

        List<UserShoppingListItemResponseDto> result = shoppingListItemService
            .updateUserShoppingListItemStatus(2L, 1L, "en", "DONE");

        assertEquals(ToDoListItemStatus.DONE, result.getFirst().getStatus());

        verify(userShoppingListItemRepo).getAllByUserShoppingListIdAndUserId(1L, 2L);
        verify(modelMapper).map(userShoppingListItem, UserShoppingListItemResponseDto.class);
        verify(shoppingListItemTranslationRepo).findByLangAndUserShoppingListItemId("en", 1L);
    }

    @Test
    void updateUserShoppingListItemStatusShouldThrowNotFoundExceptionTest() {
        when(userShoppingListItemRepo.getAllByUserShoppingListIdAndUserId(1L, 2L))
            .thenReturn(null);

        Exception thrown = assertThrows(NotFoundException.class, () -> shoppingListItemService
            .updateUserShoppingListItemStatus(2L, 1L, "en", "DONE"));

        assertEquals(ErrorMessage.USER_SHOPPING_LIST_ITEM_NOT_FOUND_BY_USER_ID, thrown.getMessage());
        verify(userShoppingListItemRepo).getAllByUserShoppingListIdAndUserId(1L, 2L);
    }

    @Test
    void updateUserShoppingListItemStatusShouldThrowBadRequestExceptionTest() {
        when(userShoppingListItemRepo.getAllByUserShoppingListIdAndUserId(1L, 2L))
            .thenReturn(List.of(ModelUtils.getPredefinedUserShoppingListItem()));

        Exception thrown = assertThrows(BadRequestException.class, () -> shoppingListItemService
            .updateUserShoppingListItemStatus(2L, 1L, "en", "Wrong Status"));

        assertEquals(ErrorMessage.INCORRECT_INPUT_ITEM_STATUS, thrown.getMessage());
        verify(userShoppingListItemRepo).getAllByUserShoppingListIdAndUserId(1L, 2L);
    }

    @Test
    void deleteTest() {
        shoppingListItemService.delete(1L);
        verify(shoppingListItemRepo).deleteById(1L);
    }

    @Test
    void deleteTestFailed() {
        doThrow(EmptyResultDataAccessException.class).when(shoppingListItemRepo).deleteById(300000L);

        assertThrows(NotDeletedException.class, () -> shoppingListItemService.delete(300000L));
    }

    @Test
    void findShoppingListItemByIdTest() {
        Optional<ShoppingListItem> object = Optional.of(shoppingListItem);
        when(shoppingListItemRepo.findById(anyLong())).thenReturn(object);
        when(modelMapper.map(object.get(), ShoppingListItemResponseDto.class))
            .thenReturn(new ShoppingListItemResponseDto());

        assertNotNull(shoppingListItemService.findShoppingListItemById(30L));
    }

    @Test
    void findShoppingListItemByIdTestFailed() {
        assertThrows(ShoppingListItemNotFoundException.class,
            () -> shoppingListItemService.findShoppingListItemById(30L));
    }

    @Test
    void getAllFactsOfTheDay() {
        int pageNumber = 0;
        int pageSize = 1;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<ShoppingListItem> shoppingListItems = Collections.singletonList(shoppingListItem);
        Page<ShoppingListItem> page = new PageImpl<>(shoppingListItems, pageable, shoppingListItems.size());

        List<ShoppingListItemManagementDto> dtoList = Collections.singletonList(
            shoppingListItems.stream().map(g -> (ShoppingListItemManagementDto.builder().id(g.getId())).build())
                .findFirst().get());
        PageableAdvancedDto<ShoppingListItemManagementDto> expected = new PageableAdvancedDto<>(dtoList, dtoList.size(),
            0, 1, 0, false, false, true, true);

        when(shoppingListItemRepo.findAll(pageable)).thenReturn(page);
        when(modelMapper.map(shoppingListItems.getFirst(), ShoppingListItemManagementDto.class))
            .thenReturn(dtoList.get(0));

        PageableAdvancedDto<ShoppingListItemManagementDto> actual =
            shoppingListItemService.findShoppingListItemsForManagementByPage(pageable);

        assertEquals(expected, actual);
    }

    @Test
    void searchBy() {
        int pageNumber = 0;
        int pageSize = 1;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<ShoppingListItem> shoppingListItems = Collections.singletonList(shoppingListItem);
        Page<ShoppingListItem> page = new PageImpl<>(shoppingListItems, pageable, shoppingListItems.size());

        List<ShoppingListItemManagementDto> dtoList = Collections.singletonList(
            shoppingListItems.stream().map(g -> (ShoppingListItemManagementDto.builder().id(g.getId())).build())
                .findFirst().get());
        PageableAdvancedDto<ShoppingListItemManagementDto> expected = new PageableAdvancedDto<>(dtoList, dtoList.size(),
            0, 1, 0, false, false, true, true);

        when(shoppingListItemRepo.searchBy(pageable, "uk")).thenReturn(page);
        when(modelMapper.map(shoppingListItems.getFirst(), ShoppingListItemManagementDto.class))
            .thenReturn(dtoList.getFirst());

        PageableAdvancedDto<ShoppingListItemManagementDto> actual = shoppingListItemService.searchBy(pageable, "uk");

        assertEquals(expected, actual);
    }

    @Test
    void deleteAllShoppingListItemByListOfId() {
        List<Long> idsToBeDeleted = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L);

        shoppingListItemService.deleteAllShoppingListItemsByListOfId(idsToBeDeleted);
        verify(shoppingListItemRepo, times(6)).deleteById(anyLong());
    }

    @Test
    void deleteUserShoppingListItems() {
        String ids = "1,2,3,4,5,6";
        List<Long> expected = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L);
        UserShoppingListItem userShoppingListItem =
            new UserShoppingListItem(1L, null, shoppingListItem, ToDoListItemStatus.ACTIVE, null);

        when(userShoppingListItemRepo.findById(anyLong())).thenReturn(Optional.of(userShoppingListItem));

        assertEquals(expected, shoppingListItemService.deleteUserShoppingListItems(ids));
        verify(userShoppingListItemRepo, times(6)).delete(userShoppingListItem);
    }

    @Test
    void deleteUserShoppingListItemsFailed() {
        String ids = "1,2,3,4,5,6";
        UserShoppingListItem userShoppingListItem =
            new UserShoppingListItem(1L, null, shoppingListItem, ToDoListItemStatus.ACTIVE, null);

        when(userShoppingListItemRepo.findById(anyLong())).thenReturn(Optional.of(userShoppingListItem));
        when(userShoppingListItemRepo.findById(3L)).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> shoppingListItemService.deleteUserShoppingListItems(ids));
    }

    @Test
    void getUserShoppingListItemTest() {
        UserShoppingListItem userShoppingListItem =
            UserShoppingListItem.builder().id(1L).status(ToDoListItemStatus.ACTIVE).build();
        when(habitAssignRepo.findByHabitIdAndUserId(userId, 1L))
            .thenReturn(Optional.of(habitAssign));
        when(userShoppingListItemRepo.findAllByHabitAssingId(habitAssign.getId())).thenReturn(Collections.singletonList(
            userShoppingListItem));
        when(modelMapper.map(userShoppingListItem, UserShoppingListItemResponseDto.class))
            .thenReturn(UserShoppingListItemResponseDto.builder().id(1L).build());
        when(shoppingListItemTranslationRepo.findByLangAndUserShoppingListItemId("en", 1L))
            .thenReturn(ShoppingListItemTranslation.builder().id(1L).build());
        assertEquals(1L, shoppingListItemService.getUserShoppingList(userId, 1L, "en").get(0).getId());
    }

    @Test
    void getEmptyUserShoppingListItemsTest() {
        when(habitAssignRepo.findByHabitIdAndUserId(userId, 1L))
            .thenReturn(Optional.of(habitAssign));
        when(userShoppingListItemRepo.findAllByHabitAssingId(habitAssign.getId())).thenReturn(Collections.emptyList());

        assertTrue(shoppingListItemService.getUserShoppingList(userId, 1L, "en").isEmpty());
    }

    @Test
    void getUserShoppingListItemWithNullTest() {
        Long habitId = 1L;
        String languageEn = "en";
        List<UserShoppingListItemResponseDto> expected = Collections.emptyList();

        when(habitAssignRepo.findByHabitIdAndUserId(userId, 1L))
            .thenReturn(Optional.empty());

        List<UserShoppingListItemResponseDto> actual = shoppingListItemService
            .getUserShoppingList(userId, habitId, languageEn);
        assertEquals(expected, actual);
    }

    @Test
    void getUserShoppingListItemWithStatusInProgressTest() {
        UserShoppingListItem item = UserShoppingListItem
            .builder().id(1L).status(ToDoListItemStatus.INPROGRESS).build();

        UserShoppingListItemResponseDto itemResponseDto = UserShoppingListItemResponseDto
            .builder().id(1L).status(ToDoListItemStatus.INPROGRESS).build();

        when(userShoppingListItemRepo.findUserShoppingListItemsByHabitAssignIdAndStatusInProgress(1L))
            .thenReturn(List.of(
                item));
        when(modelMapper.map(item, UserShoppingListItemResponseDto.class))
            .thenReturn(itemResponseDto);
        when(shoppingListItemTranslationRepo.findByLangAndUserShoppingListItemId("en", 1L))
            .thenReturn(ShoppingListItemTranslation.builder().id(1L).build());

        assertEquals(List.of(itemResponseDto), shoppingListItemService
            .getUserShoppingListItemsByHabitAssignIdAndStatusInProgress(1L, "en"));

        verify(userShoppingListItemRepo).findUserShoppingListItemsByHabitAssignIdAndStatusInProgress(anyLong());
        verify(shoppingListItemTranslationRepo).findByLangAndUserShoppingListItemId(any(), anyLong());
        verify(modelMapper).map(any(), any());
    }

    @Test
    void deleteUserShoppingListItemByItemIdAndUserIdAndHabitIdTest() {
        when(habitAssignRepo.findByHabitIdAndUserId(1L, userId))
            .thenReturn(Optional.of(habitAssign));
        shoppingListItemService.deleteUserShoppingListItemByItemIdAndUserIdAndHabitId(1L, userId, 1L);
        verify(userShoppingListItemRepo).deleteByShoppingListItemIdAndHabitAssignId(1L, 1L);
    }

    @Test
    void deleteUserShollingListItemByItemIdAndUserIdAndHabitIdTestThorows() {
        assertThrows(NotFoundException.class,
            () -> shoppingListItemService.deleteUserShoppingListItemByItemIdAndUserIdAndHabitId(1L, userId, 1L));
    }

    @Test
    void getUserShoppingListItemIfThereAreNoItems() {
        when(habitAssignRepo.findByHabitIdAndUserId(userId, 1L))
            .thenReturn(Optional.of(habitAssign));
        when(userShoppingListItemRepo.findAllByHabitAssingId(habitAssign.getId())).thenReturn(Collections.emptyList());

        assertEquals(Collections.emptyList(),
            shoppingListItemService.getUserShoppingList(userId, 1L, "en"));
    }

    @Test
    void getShoppingListItemByHabitIdTest() {
        List<Long> listID = Collections.singletonList(1L);
        List<ShoppingListItem> shoppingListItemList = Collections.singletonList(shoppingListItem);
        ShoppingListItemManagementDto shoppingListItemManagementDto = ShoppingListItemManagementDto.builder()
            .id(1L)
            .build();
        List<ShoppingListItemManagementDto> shoppingListItemManagementDtos =
            Collections.singletonList(shoppingListItemManagementDto);

        when(shoppingListItemRepo.getAllShoppingListItemIdByHabitIdISContained(1L)).thenReturn(listID);
        when(shoppingListItemRepo.getShoppingListByListOfId(listID)).thenReturn(shoppingListItemList);
        when(modelMapper.map(shoppingListItem, ShoppingListItemManagementDto.class)).thenReturn(
            shoppingListItemManagementDto);
        assertEquals(shoppingListItemManagementDtos, shoppingListItemService.getShoppingListByHabitId(1L));

    }

    @Test
    void findAllShoppingListItemForManagementPageNotContainedTest() {
        int pageNumber = 0;
        int pageSize = 1;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<ShoppingListItem> shoppingListItemList = Collections.singletonList(shoppingListItem);
        List<Long> listID = Collections.singletonList(1L);
        Page<ShoppingListItem> page = new PageImpl<>(shoppingListItemList, pageable, shoppingListItemList.size());
        ShoppingListItemManagementDto shoppingListItemManagementDto = ShoppingListItemManagementDto.builder()
            .id(1L)
            .build();
        List<ShoppingListItemManagementDto> dtoList = Collections.singletonList(shoppingListItemManagementDto);
        PageableAdvancedDto<ShoppingListItemManagementDto> expected = new PageableAdvancedDto<>(dtoList, dtoList.size(),
            0, 1, 0, false, false, true, true);

        when(shoppingListItemRepo.getAllShoppingListItemsByHabitIdNotContained(1L)).thenReturn(listID);
        when(shoppingListItemRepo.getShoppingListByListOfIdPageable(listID, pageable)).thenReturn(page);
        when(modelMapper.map(shoppingListItem, ShoppingListItemManagementDto.class)).thenReturn(
            shoppingListItemManagementDto);
        PageableAdvancedDto<ShoppingListItemManagementDto> actual =
            shoppingListItemService.findAllShoppingListItemsForManagementPageNotContained(1L, pageable);
        assertEquals(expected, actual);
    }

    @Test
    void findInProgressByUserIdAndLanguageCodeTest() {
        when(shoppingListItemService.findInProgressByUserIdAndLanguageCode(1L, "ua"))
            .thenReturn(new ArrayList<>());
        assertEquals(0, shoppingListItemRepo.findInProgressByUserIdAndLanguageCode(1L, "ua").size());
    }

    @Test
    void getUserShoppingListByHabitAssignIdTest() {
        Long habitAssignId = 2L;
        Long userId3 = 3L;
        Long userShoppingListItemId = 4L;
        Long shoppingListItemTranslationId = 5L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;
        String text = "text";

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId3);

        UserShoppingListItem userShoppingListItem = ModelUtils.getUserShoppingListItem();
        userShoppingListItem.setId(userShoppingListItemId);
        userShoppingListItem.setStatus(ToDoListItemStatus.ACTIVE);

        UserShoppingListItemResponseDto userShoppingListItemResponseDto =
            ModelUtils.getUserShoppingListItemResponseDto();
        userShoppingListItemResponseDto.setId(userShoppingListItemId);

        ShoppingListItemTranslation shoppingListItemTranslation = ModelUtils.getShoppingListItemTranslation();
        shoppingListItemTranslation.setId(shoppingListItemTranslationId);
        shoppingListItemTranslation.setContent(text);

        UserShoppingListItemResponseDto expectedDto = ModelUtils.getUserShoppingListItemResponseDto();
        expectedDto.setId(userShoppingListItemId);
        expectedDto.setText(text);

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));
        when(userShoppingListItemRepo.findAllByHabitAssingId(habitAssignId)).thenReturn(Collections.singletonList(
            userShoppingListItem));
        when(modelMapper.map(userShoppingListItem, UserShoppingListItemResponseDto.class))
            .thenReturn(userShoppingListItemResponseDto);
        when(shoppingListItemTranslationRepo.findByLangAndUserShoppingListItemId(languageDefault,
            userShoppingListItemId))
            .thenReturn(shoppingListItemTranslation);

        List<UserShoppingListItemResponseDto> actualDtoList = shoppingListItemService
            .getUserShoppingListByHabitAssignId(userId3, habitAssignId, languageDefault);

        assertNotNull(actualDtoList);
        assertEquals(1, actualDtoList.size());
        assertEquals(expectedDto, actualDtoList.getFirst());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userShoppingListItemRepo).findAllByHabitAssingId(habitAssignId);
        verify(modelMapper).map(userShoppingListItem, UserShoppingListItemResponseDto.class);
        verify(shoppingListItemTranslationRepo).findByLangAndUserShoppingListItemId(languageDefault,
            userShoppingListItemId);
    }

    @Test
    void getUserShoppingListByHabitAssignIdReturnEmptyListTest() {
        Long habitAssignId = 2L;
        Long userId3 = 3L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId3);

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));
        when(userShoppingListItemRepo.findAllByHabitAssingId(habitAssignId)).thenReturn(Collections.emptyList());

        assertEquals(Collections.emptyList(),
            shoppingListItemService.getUserShoppingListByHabitAssignId(userId3, habitAssignId, languageDefault));

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userShoppingListItemRepo).findAllByHabitAssingId(habitAssignId);
        verify(modelMapper, times(0)).map(any(), any());
        verify(shoppingListItemTranslationRepo, times(0)).findByLangAndUserShoppingListItemId(any(), anyLong());
    }

    @Test
    void getUserShoppingListByHabitAssignIdThrowsExceptionWhenHabitAssignNotExists() {
        Long habitAssignId = 2L;
        Long userId3 = 3L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> shoppingListItemService
            .getUserShoppingListByHabitAssignId(userId3, habitAssignId, languageDefault));

        assertEquals(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userShoppingListItemRepo, times(0)).findAllByHabitAssingId(anyLong());
        verify(modelMapper, times(0)).map(any(), any());
        verify(shoppingListItemTranslationRepo, times(0)).findByLangAndUserShoppingListItemId(any(), anyLong());
    }

    @Test
    void getUserShoppingListByHabitAssignIdThrowsExceptionWhenHabitAssignNotBelongsToUser() {
        long habitAssignId = 2L;
        long userId3 = 3L;
        String languageDefault = AppConstant.DEFAULT_LANGUAGE_CODE;

        habitAssign.setId(habitAssignId);
        habitAssign.getUser().setId(userId3 + 1);

        when(habitAssignRepo.findById(habitAssignId))
            .thenReturn(Optional.of(habitAssign));

        UserHasNoPermissionToAccessException exception =
            assertThrows(UserHasNoPermissionToAccessException.class, () -> shoppingListItemService
                .getUserShoppingListByHabitAssignId(userId3, habitAssignId, languageDefault));

        assertEquals(ErrorMessage.USER_HAS_NO_PERMISSION, exception.getMessage());

        verify(habitAssignRepo).findById(habitAssignId);
        verify(userShoppingListItemRepo, times(0)).findAllByHabitAssingId(anyLong());
        verify(modelMapper, times(0)).map(any(), any());
        verify(shoppingListItemTranslationRepo, times(0)).findByLangAndUserShoppingListItemId(any(), anyLong());
    }
}
