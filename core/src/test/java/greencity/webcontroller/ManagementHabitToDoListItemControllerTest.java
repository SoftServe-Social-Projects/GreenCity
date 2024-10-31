package greencity.webcontroller;

import com.google.gson.Gson;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.shoppinglistitem.ShoppingListItemManagementDto;
import greencity.service.ShoppingListItemService;
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
class ManagementHabitShoppingListItemControllerTest {

    private static final String shoppingManagementLink = "/management/habit-shopping-list";

    private MockMvc mockMvc;
    @Mock
    private ShoppingListItemService shoppingListItemService;
    @Mock
    private HabitService habitService;

    @InjectMocks
    private ManagementHabitShoppingListItemController itemController;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(itemController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void getAllShoppingListItemsTest() throws Exception {

        Pageable pageable = PageRequest.of(0, 5);
        ShoppingListItemManagementDto shoppingListItemManagementDto = new ShoppingListItemManagementDto();
        shoppingListItemManagementDto.setId(1L);

        List<ShoppingListItemManagementDto> dtoList = Collections.singletonList(shoppingListItemManagementDto);

        PageableAdvancedDto<ShoppingListItemManagementDto> itemManagementDTO = new PageableAdvancedDto<>();
        itemManagementDTO.setPage(dtoList);
        itemManagementDTO.setCurrentPage(0);
        itemManagementDTO.setTotalElements(5);
        itemManagementDTO.setNumber(1);

        when(shoppingListItemService.findAllShoppingListItemsForManagementPageNotContained(1L, pageable))
            .thenReturn(itemManagementDTO);
        when(shoppingListItemService.getShoppingListByHabitId(1L)).thenReturn(dtoList);

        this.mockMvc.perform(get(shoppingManagementLink + "?habitId=1")
            .param("page", "0")
            .param("size", "5"))
            .andExpect(view().name("core/management_habit_shopping_list_item"))
            .andExpect(model().attribute("shoppingListItems", itemManagementDTO))
            .andExpect(model().attribute("habitId", 1L))
            .andExpect(model().attribute("currentShoppingListItems", dtoList))
            .andExpect(status().isOk());
        verify(shoppingListItemService).findAllShoppingListItemsForManagementPageNotContained(1L, pageable);
    }

    @Test
    void deleteAllShoppingListItem() throws Exception {
        List<Long> listID = Arrays.asList(1L, 2L);
        Gson gson = new Gson();
        String json = gson.toJson(listID);
        when(habitService.deleteAllShoppingListItemsByListOfId(1L, listID)).thenReturn(listID);
        this.mockMvc.perform(MockMvcRequestBuilders.delete(shoppingManagementLink + "/deleteAll/?habitId=1")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitService).deleteAllShoppingListItemsByListOfId(1L, listID);
    }

    @Test
    void addAllShoppingListItemTest() throws Exception {
        List<Long> listID = Arrays.asList(1L, 2L);
        Gson gson = new Gson();
        String json = gson.toJson(listID);
        when(habitService.addAllShoppingListItemsByListOfId(1L, listID)).thenReturn(listID);

        this.mockMvc.perform(MockMvcRequestBuilders.post(shoppingManagementLink + "/addAll/?habitId=1")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitService).addAllShoppingListItemsByListOfId(1L, listID);
    }

    @Test
    void deleteShoppingListItemTest() throws Exception {
        doNothing().when(habitService).deleteShoppingListItem(1L, 1L);
        this.mockMvc.perform(MockMvcRequestBuilders.delete(shoppingManagementLink + "/delete/?habitId=1&itemId=1"))
            .andExpect(status().isOk());
        verify(habitService).deleteShoppingListItem(1L, 1L);

    }

    @Test
    void addShoppingListItemToHabit() throws Exception {
        doNothing().when(habitService).addShoppingListItemToHabit(1L, 1L);
        this.mockMvc.perform(MockMvcRequestBuilders.post(shoppingManagementLink + "/add/?habitId=1&itemId=1"))
            .andExpect(status().isOk());
        verify(habitService).addShoppingListItemToHabit(1L, 1L);
    }
}
