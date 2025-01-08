package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.todolistitem.BulkSaveCustomToDoListItemDto;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.enums.ToDoListItemStatus;
import greencity.service.CustomToDoListItemService;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static greencity.ModelUtils.getPrincipal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomToDoListItemControllerTest {
    private MockMvc mockMvc;

    @Mock
    CustomToDoListItemService customToDoListItemService;

    @InjectMocks
    CustomToDoListItemController customController;
    ObjectMapper objectMapper;

    private Principal principal = getPrincipal();

    private static final String customLink = "/custom/to-do-list-items";

    private CustomToDoListItemResponseDto dto;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(customController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
        objectMapper = new ObjectMapper();

        dto = new CustomToDoListItemResponseDto(3L, "text",
            ToDoListItemStatus.ACTIVE);
    }

    @Test
    void getAllAvailableCustomToDoListItems() throws Exception {
        Long id = 1L;
        this.mockMvc.perform(get(customLink + "/" + id + "/" + id)
            .principal(principal)).andExpect(status().isOk());
        when(customToDoListItemService.findAllAvailableCustomToDoListItems(1L, 1L))
            .thenReturn(Collections.singletonList(dto));
        verify(customToDoListItemService).findAllAvailableCustomToDoListItems(id, id);
        assertEquals(dto,
            customController.getAllAvailableCustomToDoListItems(id, id).getBody().get(0));
    }

    @Test
    void save() throws Exception {
        Long id = 1L;
        BulkSaveCustomToDoListItemDto bulkSaveCustomToDoListItemDto = new BulkSaveCustomToDoListItemDto();
        String content = objectMapper.writeValueAsString(bulkSaveCustomToDoListItemDto);
        this.mockMvc.perform(post(customLink + "/" + id + "/" + id + "/" + "custom-to-do-list-items")
            .content(content)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
        when(customToDoListItemService.save(bulkSaveCustomToDoListItemDto, id, id))
            .thenReturn(Collections.singletonList(dto));
        verify(customToDoListItemService).save(bulkSaveCustomToDoListItemDto, id, id);
        assertEquals(dto, customController
            .saveUserCustomToDoListItems(bulkSaveCustomToDoListItemDto, id, id).getBody().get(0));
    }

    @Test
    void updateItemStatus() throws Exception {
        this.mockMvc.perform(patch(customLink + "/{userId}/custom-to-do-list-items/?itemId=1&status=DONE", 1)
            .principal(principal))
            .andExpect(status().isOk());
        verify(customToDoListItemService).updateItemStatus(1L, 1L, "DONE");
    }

    @Test
    void delete() throws Exception {
        String ids = "1,2";
        this.mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .delete(customLink + "/{userId}/custom-to-do-list-items", 1)
            .param("ids", ids)).andExpect(status().isOk());
        verify(customToDoListItemService).bulkDelete(ids);
    }

    @Test
    void updateItemStatusToDoneTest() throws Exception {
        this.mockMvc.perform(patch(customLink + "/{userId}/done", 1)
            .param("userId", "1")
            .param("itemId", "1"))
            .andExpect(status().isOk());
        verify(customToDoListItemService).updateItemStatusToDone(1L, 1L);
    }

    @Test
    void getAllCustomToDoItemsByStatus() throws Exception {
        Long id = 1L;
        this.mockMvc.perform(get(customLink + "/" + id + "/custom-to-do-list-items")).andExpect(status().isOk());
        when(customToDoListItemService.findAllUsersCustomToDoListItemsByStatus(anyLong(), anyString()))
            .thenReturn(Collections.singletonList(dto));

        assertEquals(dto,
            customController.getAllCustomToDoItemsByStatus(id, "ACTIVE").getBody().get(0));
    }
}
