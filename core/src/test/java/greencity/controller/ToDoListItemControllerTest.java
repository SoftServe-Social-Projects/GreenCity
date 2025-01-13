package greencity.controller;

import greencity.dto.todolistitem.ToDoListItemRequestDto;
import greencity.service.ToDoListItemService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static greencity.ModelUtils.getPrincipal;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.validation.Validator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ToDoListItemControllerTest {
    private static final String toDoListItemLink = "/user/to-do-list-items";
    private MockMvc mockMvc;
    @InjectMocks
    private ToDoListItemController toDoListItemController;
    @Mock
    private ToDoListItemService toDoListItemService;
    @Mock
    private Validator mockValidator;

    private Principal principal = getPrincipal();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(toDoListItemController)
            .setValidator(mockValidator)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void bulkDeleteUserToDoListItemTest() throws Exception {
        mockMvc.perform(delete(toDoListItemLink + "/user-to-do-list-items?ids=1,2", 1))
            .andExpect(status().isOk());

        verify(toDoListItemService).deleteUserToDoListItems("1,2");
    }

    @Test
    void updateUserToDoListItemStatusWithLanguageParamTest() throws Exception {
        mockMvc.perform(patch(toDoListItemLink + "/{userToDoListItemId}", 1, 1)
            .locale(Locale.of("ua")))
            .andExpect(status().isCreated());

        verify(toDoListItemService).updateUserToDoListItemStatus(null, 1L, "ua");
    }

    @Test
    void updateUserToDoListItemStatus() throws Exception {
        mockMvc.perform(patch(toDoListItemLink
            + "/{toDoListItemId}/status/{status}", 1, "DONE")
            .locale(Locale.of("en")))
            .andExpect(status().isOk());

        verify(toDoListItemService)
            .updateUserToDoListItemStatus(null, 1L, "en", "DONE");
    }

    @Test
    void updateUserToDoListItemStatusWithoutLanguageParamTest() throws Exception {
        mockMvc.perform(patch(toDoListItemLink + "/{userToDoListItemId}", 1, 1))
            .andExpect(status().isCreated());

        verify(toDoListItemService).updateUserToDoListItemStatus(null, 1L, "en");
    }

    @Test
    void saveUserToDoListItemWithoutLanguageParamTest() throws Exception {
        String content = """
            [
                {
                    "id": 1
                }
            ]
            """;

        mockMvc.perform(post(toDoListItemLink + "?habitId=1&lang=en", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content))
            .andExpect(status().isCreated());

        ToDoListItemRequestDto dto = new ToDoListItemRequestDto(1L);
        verify(toDoListItemService).saveUserToDoListItems(null, 1L, Collections.singletonList(dto), "en");
    }

    @Test
    void getUserToDoListItemsWithLanguageParamTest() throws Exception {
        mockMvc.perform(get(toDoListItemLink + "/habits/1/to-do-list?lang=en", 1))
            .andExpect(status().isOk());

        verify(toDoListItemService).getUserToDoList(null, 1L, "en");
    }

    @Test
    void getUserToDoListItemWithoutLanguageParamTest() throws Exception {
        mockMvc.perform(get(toDoListItemLink + "/habits/1/to-do-list", 1))
            .andExpect(status().isOk());

        verify(toDoListItemService).getUserToDoList(null, 1L, "en");
    }

    @Test
    void deleteTest() throws Exception {

        mockMvc.perform(delete(toDoListItemLink)
            .param("toDoListItemId", "1")
            .param("habitId", "1"))
            .andExpect(status().isOk());

        verify(toDoListItemService).deleteUserToDoListItemByItemIdAndUserIdAndHabitId(1L, null, 1L);
    }

    @Test
    void findAllByUserTest() throws Exception {
        Long id = 1L;
        this.mockMvc.perform(get(toDoListItemLink + "/" + id + "/" + "get-all-inprogress")
            .param("lang", "ua")
            .principal(principal)).andExpect(status().isOk());
        when(toDoListItemService.findInProgressByUserIdAndLanguageCode(id, "ua"))
            .thenReturn(new ArrayList<>());
        verify(toDoListItemService).findInProgressByUserIdAndLanguageCode(id, "ua");
        assertTrue(toDoListItemController.findInProgressByUserId(id, "ua").getBody().isEmpty());
    }
}
