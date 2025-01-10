package greencity.webcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.language.LanguageDTO;
import greencity.dto.todolistitem.ToDoListItemManagementDto;
import greencity.dto.todolistitem.ToDoListItemPostDto;
import greencity.dto.todolistitem.ToDoListItemViewDto;
import greencity.service.HabitToDoListItemServiceImpl;
import greencity.service.LanguageService;
import greencity.service.ToDoListItemService;
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
class ManagementToDoListItemsControllerTest {
    private static final String managementToDoListLink = "/management/to-do-list-items";

    private MockMvc mockMvc;

    @InjectMocks
    private ManagementToDoListItemsController managementToDoListItemsController;

    @Mock
    private ToDoListItemService toDoListItemService;

    @Mock
    private HabitToDoListItemServiceImpl habitToDoListItemService;

    @Mock
    private LanguageService languageService;

    @Mock
    private Validator mockValidator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(managementToDoListItemsController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setValidator(mockValidator)
            .build();
    }

    @Test
    void getAllToDoListItemsTest() throws Exception {
        int page = 0;
        int size = 10;
        Pageable paging = PageRequest.of(page, size, Sort.by("id").ascending());
        List<ToDoListItemManagementDto> toDoListItemManagementDtos =
            Collections.singletonList(new ToDoListItemManagementDto());
        PageableAdvancedDto<ToDoListItemManagementDto> managementDtoPageableDto =
            new PageableAdvancedDto<>(toDoListItemManagementDtos, 1, 0, 1, 1,
                true, true, true, true);
        when(toDoListItemService.findToDoListItemsForManagementByPage(paging))
            .thenReturn(managementDtoPageableDto);
        this.mockMvc.perform(get(managementToDoListLink)
            .param("page", "0")
            .param("size", "10"))
            .andExpect(view().name("core/management_to_do_list_items"))
            .andExpect(status().isOk());

        verify(toDoListItemService).findToDoListItemsForManagementByPage(paging);
    }

    @Test
    void saveTest() throws Exception {
        ToDoListItemPostDto toDoListItemPostDto = ModelUtils.getToDoListItemPostDto();
        String itemGtoJson = objectMapper.writeValueAsString(toDoListItemPostDto);

        mockMvc.perform(post(managementToDoListLink)
            .content(itemGtoJson)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(toDoListItemService).saveToDoListItem(toDoListItemPostDto);
    }

    @Test
    void getToDoListItemByIdTest() throws Exception {
        Long itemId = 1L;
        mockMvc.perform(get(managementToDoListLink + "/" + itemId))
            .andExpect(status().isOk());

        verify(toDoListItemService).findToDoListItemById(itemId);
    }

    @Test
    void deleteTest() throws Exception {
        Long habitFactId = 1L;
        mockMvc.perform(delete(managementToDoListLink + "/" + habitFactId))
            .andExpect(status().isOk());

        verify(toDoListItemService).delete(habitFactId);
    }

    @Test
    void deleteAllTest() throws Exception {
        List<Long> ids = Arrays.asList(1L, 2L);
        mockMvc.perform(delete(managementToDoListLink + "/deleteAll")
            .content("[1,2]")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(toDoListItemService).deleteAllToDoListItemsByListOfId(ids);
    }

    @Test
    void updateHabitFactsTest() throws Exception {
        ToDoListItemPostDto toDoListItemPostDto = ModelUtils.getToDoListItemPostDto();
        long id = 1L;
        String itemGtoJson = objectMapper.writeValueAsString(toDoListItemPostDto);

        mockMvc.perform(put(managementToDoListLink + "/" + id)
            .content(itemGtoJson)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(toDoListItemService).update(toDoListItemPostDto);
    }

    @Test
    void filterDataTest() throws Exception {
        Pageable pageable = PageRequest.of(0, 3);
        ToDoListItemViewDto toDoListItemViewDto = new ToDoListItemViewDto();
        List<ToDoListItemManagementDto> list = Collections.singletonList(new ToDoListItemManagementDto());
        PageableAdvancedDto<ToDoListItemManagementDto> pageableDto = new PageableAdvancedDto<>(list, 3, 0, 3,
            0, false, true, true, false);
        when(toDoListItemService.getFilteredDataForManagementByPage(pageable, toDoListItemViewDto))
            .thenReturn(pageableDto);
        when(languageService.getAllLanguages()).thenReturn(Collections.singletonList(ModelUtils.getLanguageDTO()));
        this.mockMvc.perform(post(managementToDoListLink + "/filter")
            .param("page", "0")
            .param("size", "3"))
            .andExpect(model().attribute("toDoListItems", pageableDto))
            .andExpect(model().attribute("languages", languageService.getAllLanguages()))
            .andExpect(model().attribute("fields", toDoListItemViewDto))
            .andExpect(view().name("core/management_to_do_list_items"))
            .andExpect(status().isOk());
        verify(toDoListItemService).getFilteredDataForManagementByPage(pageable, toDoListItemViewDto);
        verify(languageService, times(2)).getAllLanguages();
    }

    @Test
    void getAllToDoListItemsSearchByQueryTest() throws Exception {
        Pageable paging = PageRequest.of(0, 3, Sort.by("id").ascending());
        List<ToDoListItemManagementDto> toDoListItemManagementDtos =
            Collections.singletonList(new ToDoListItemManagementDto());
        PageableAdvancedDto<ToDoListItemManagementDto> managementDtoPageableDto =
            new PageableAdvancedDto<>(toDoListItemManagementDtos, 1, 0, 1, 1,
                true, true, true, true);
        when(toDoListItemService.searchBy(paging, "query")).thenReturn(managementDtoPageableDto);
        when(languageService.getAllLanguages()).thenReturn(Collections.singletonList(new LanguageDTO()));
        this.mockMvc.perform(get(managementToDoListLink + "?query=query")
            .param("page", "0")
            .param("size", "3"))
            .andExpect(model().attribute("toDoListItems", managementDtoPageableDto))
            .andExpect(model().attribute("languages", languageService.getAllLanguages()))
            .andExpect(view().name("core/management_to_do_list_items"))
            .andExpect(status().isOk());
        verify(toDoListItemService).searchBy(paging, "query");
        verify(languageService, times(2)).getAllLanguages();
    }

    @Test
    void unlinkToDoListItems() throws Exception {
        List<Long> toDoIds = List.of(1L, 3L);
        Long habitId = 1L;

        mockMvc.perform(delete(managementToDoListLink + "/unlink/" + habitId)
            .content(String.valueOf(toDoIds))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(habitToDoListItemService).unlinkToDoListItems(toDoIds, habitId);
    }
}
