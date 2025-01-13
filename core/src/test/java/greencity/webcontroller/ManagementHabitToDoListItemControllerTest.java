package greencity.webcontroller;

import com.google.gson.Gson;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.todolistitem.ToDoListItemManagementDto;
import greencity.service.ToDoListItemService;
import greencity.service.HabitService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagementHabitToDoListItemControllerTest {

    private static final String toDoManagementLink = "/management/habit-to-do-list";

    private MockMvc mockMvc;
    @Mock
    private ToDoListItemService toDoListItemService;
    @Mock
    private HabitService habitService;

    @InjectMocks
    private ManagementHabitToDoListItemController itemController;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(itemController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void getAllToDoListItemsTest() throws Exception {

        Pageable pageable = PageRequest.of(0, 5);
        ToDoListItemManagementDto toDoListItemManagementDto = new ToDoListItemManagementDto();
        toDoListItemManagementDto.setId(1L);

        List<ToDoListItemManagementDto> dtoList = Collections.singletonList(toDoListItemManagementDto);

        PageableAdvancedDto<ToDoListItemManagementDto> itemManagementDTO = new PageableAdvancedDto<>();
        itemManagementDTO.setPage(dtoList);
        itemManagementDTO.setCurrentPage(0);
        itemManagementDTO.setTotalElements(5);
        itemManagementDTO.setNumber(1);

        when(toDoListItemService.findAllToDoListItemsForManagementPageNotContained(1L, pageable))
            .thenReturn(itemManagementDTO);
        when(toDoListItemService.getToDoListByHabitId(1L)).thenReturn(dtoList);

        this.mockMvc.perform(get(toDoManagementLink + "?habitId=1")
            .param("page", "0")
            .param("size", "5"))
            .andExpect(view().name("core/management_habit_to_do_list_item"))
            .andExpect(model().attribute("toDoListItems", itemManagementDTO))
            .andExpect(model().attribute("habitId", 1L))
            .andExpect(model().attribute("currentToDoListItems", dtoList))
            .andExpect(status().isOk());
        verify(toDoListItemService).findAllToDoListItemsForManagementPageNotContained(1L, pageable);
    }

    @Test
    void deleteAllToDoListItem() throws Exception {
        List<Long> listID = Arrays.asList(1L, 2L);
        Gson gson = new Gson();
        String json = gson.toJson(listID);
        when(habitService.deleteAllToDoListItemsByListOfId(1L, listID)).thenReturn(listID);
        this.mockMvc.perform(MockMvcRequestBuilders.delete(toDoManagementLink + "/deleteAll/?habitId=1")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitService).deleteAllToDoListItemsByListOfId(1L, listID);
    }

    @Test
    void addAllToDoListItemTest() throws Exception {
        List<Long> listID = Arrays.asList(1L, 2L);
        Gson gson = new Gson();
        String json = gson.toJson(listID);
        when(habitService.addAllToDoListItemsByListOfId(1L, listID)).thenReturn(listID);

        this.mockMvc.perform(MockMvcRequestBuilders.post(toDoManagementLink + "/addAll/?habitId=1")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitService).addAllToDoListItemsByListOfId(1L, listID);
    }

    @Test
    void deleteToDoListItemTest() throws Exception {
        doNothing().when(habitService).deleteToDoListItem(1L, 1L);
        this.mockMvc.perform(MockMvcRequestBuilders.delete(toDoManagementLink + "/delete/?habitId=1&itemId=1"))
            .andExpect(status().isOk());
        verify(habitService).deleteToDoListItem(1L, 1L);

    }

    @Test
    void addToDoListItemToHabit() throws Exception {
        doNothing().when(habitService).addToDoListItemToHabit(1L, 1L);
        this.mockMvc.perform(MockMvcRequestBuilders.post(toDoManagementLink + "/add/?habitId=1&itemId=1"))
            .andExpect(status().isOk());
        verify(habitService).addToDoListItemToHabit(1L, 1L);
    }
}
