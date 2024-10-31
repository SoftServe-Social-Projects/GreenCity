package greencity.webcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.language.LanguageDTO;
import greencity.dto.shoppinglistitem.ShoppingListItemManagementDto;
import greencity.dto.shoppinglistitem.ShoppingListItemPostDto;
import greencity.dto.shoppinglistitem.ShoppingListItemViewDto;
import greencity.service.HabitShoppingListItemServiceImpl;
import greencity.service.LanguageService;
import greencity.service.ShoppingListItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ManagementShoppingListItemsControllerTest {
    private static final String managementShoppingListLink = "/management/shopping-list-items";

    private MockMvc mockMvc;

    @InjectMocks
    private ManagementShoppingListItemsController managementShoppingListItemsController;

    @Mock
    private ShoppingListItemService shoppingListItemService;

    @Mock
    private HabitShoppingListItemServiceImpl habitShoppingListItemService;

    @Mock
    private LanguageService languageService;

    @Mock
    private Validator mockValidator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(managementShoppingListItemsController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setValidator(mockValidator)
            .build();
    }

    @Test
    void getAllShoppingListItemsTest() throws Exception {
        int page = 0;
        int size = 10;
        Pageable paging = PageRequest.of(page, size, Sort.by("id").ascending());
        List<ShoppingListItemManagementDto> shoppingListItemManagementDtos =
            Collections.singletonList(new ShoppingListItemManagementDto());
        PageableAdvancedDto<ShoppingListItemManagementDto> managementDtoPageableDto =
            new PageableAdvancedDto<>(shoppingListItemManagementDtos, 1, 0, 1, 1,
                true, true, true, true);
        when(shoppingListItemService.findShoppingListItemsForManagementByPage(paging))
            .thenReturn(managementDtoPageableDto);
        this.mockMvc.perform(get(managementShoppingListLink)
            .param("page", "0")
            .param("size", "10"))
            .andExpect(view().name("core/management_shopping_list_items"))
            .andExpect(status().isOk());

        verify(shoppingListItemService).findShoppingListItemsForManagementByPage(paging);
    }

    @Test
    void saveTest() throws Exception {
        ShoppingListItemPostDto shoppingListItemPostDto = ModelUtils.getShoppingListItemPostDto();
        String itemGtoJson = objectMapper.writeValueAsString(shoppingListItemPostDto);

        mockMvc.perform(post(managementShoppingListLink)
            .content(itemGtoJson)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(shoppingListItemService).saveShoppingListItem(shoppingListItemPostDto);
    }

    @Test
    void getShoppingListItemByIdTest() throws Exception {
        Long itemId = 1L;
        mockMvc.perform(get(managementShoppingListLink + "/" + itemId))
            .andExpect(status().isOk());

        verify(shoppingListItemService).findShoppingListItemById(itemId);
    }

    @Test
    void deleteTest() throws Exception {
        Long habitFactId = 1L;
        mockMvc.perform(delete(managementShoppingListLink + "/" + habitFactId))
            .andExpect(status().isOk());

        verify(shoppingListItemService).delete(habitFactId);
    }

    @Test
    void deleteAllTest() throws Exception {
        List<Long> ids = Arrays.asList(1L, 2L);
        mockMvc.perform(delete(managementShoppingListLink + "/deleteAll")
            .content("[1,2]")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(shoppingListItemService).deleteAllShoppingListItemsByListOfId(ids);
    }

    @Test
    void updateHabitFactsTest() throws Exception {
        ShoppingListItemPostDto shoppingListItemPostDto = ModelUtils.getShoppingListItemPostDto();
        long id = 1L;
        String itemGtoJson = objectMapper.writeValueAsString(shoppingListItemPostDto);

        mockMvc.perform(put(managementShoppingListLink + "/" + id)
            .content(itemGtoJson)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(shoppingListItemService).update(shoppingListItemPostDto);
    }

    @Test
    void filterDataTest() throws Exception {
        Pageable pageable = PageRequest.of(0, 3);
        ShoppingListItemViewDto shoppingListItemViewDto = new ShoppingListItemViewDto();
        List<ShoppingListItemManagementDto> list = Collections.singletonList(new ShoppingListItemManagementDto());
        PageableAdvancedDto<ShoppingListItemManagementDto> pageableDto = new PageableAdvancedDto<>(list, 3, 0, 3,
            0, false, true, true, false);
        when(shoppingListItemService.getFilteredDataForManagementByPage(pageable, shoppingListItemViewDto))
            .thenReturn(pageableDto);
        when(languageService.getAllLanguages()).thenReturn(Collections.singletonList(ModelUtils.getLanguageDTO()));
        this.mockMvc.perform(post(managementShoppingListLink + "/filter")
            .param("page", "0")
            .param("size", "3"))
            .andExpect(model().attribute("shoppingListItems", pageableDto))
            .andExpect(model().attribute("languages", languageService.getAllLanguages()))
            .andExpect(model().attribute("fields", shoppingListItemViewDto))
            .andExpect(view().name("core/management_shopping_list_items"))
            .andExpect(status().isOk());
        verify(shoppingListItemService).getFilteredDataForManagementByPage(pageable, shoppingListItemViewDto);
        verify(languageService, times(2)).getAllLanguages();
    }

    @Test
    void getAllShoppingListItemsSearchByQueryTest() throws Exception {
        Pageable paging = PageRequest.of(0, 3, Sort.by("id").ascending());
        List<ShoppingListItemManagementDto> shoppingListItemManagementDtos =
            Collections.singletonList(new ShoppingListItemManagementDto());
        PageableAdvancedDto<ShoppingListItemManagementDto> managementDtoPageableDto =
            new PageableAdvancedDto<>(shoppingListItemManagementDtos, 1, 0, 1, 1,
                true, true, true, true);
        when(shoppingListItemService.searchBy(paging, "query")).thenReturn(managementDtoPageableDto);
        when(languageService.getAllLanguages()).thenReturn(Collections.singletonList(new LanguageDTO()));
        this.mockMvc.perform(get(managementShoppingListLink + "?query=query")
            .param("page", "0")
            .param("size", "3"))
            .andExpect(model().attribute("shoppingListItems", managementDtoPageableDto))
            .andExpect(model().attribute("languages", languageService.getAllLanguages()))
            .andExpect(view().name("core/management_shopping_list_items"))
            .andExpect(status().isOk());
        verify(shoppingListItemService).searchBy(paging, "query");
        verify(languageService, times(2)).getAllLanguages();
    }

    @Test
    void unlinkShoppingListItems() throws Exception {
        List<Long> shopIds = List.of(1L, 3L);
        Long habitId = 1L;

        mockMvc.perform(delete(managementShoppingListLink + "/unlink/" + habitId)
            .content(String.valueOf(shopIds))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(habitShoppingListItemService).unlinkShoppingListItems(shopIds, habitId);
    }
}
