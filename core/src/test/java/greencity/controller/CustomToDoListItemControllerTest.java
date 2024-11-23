package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.user.UserVO;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.security.Principal;
import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserVO;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
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

    private final Principal principal = getPrincipal();

    private static final String customLink = "/habits/custom-to-do-list-items";

    private CustomToDoListItemResponseDto dto;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(customController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
        objectMapper = new ObjectMapper();

        dto = new CustomToDoListItemResponseDto(3L, "text",
            ToDoListItemStatus.ACTIVE.toString(), true);
    }

    @Test
    void getAllAvailableCustomToDoListItems() throws Exception {
        Long habitId = 1L;
        UserVO userVO = getUserVO();
        this.mockMvc.perform(get(customLink + "/" + habitId)
            .principal(principal)).andExpect(status().isOk());
        when(customToDoListItemService.findAllCustomToDoListItemsForHabit(anyLong(), habitId))
            .thenReturn(Collections.singletonList(dto));
        verify(customToDoListItemService).findAllCustomToDoListItemsForHabit(anyLong(), habitId);
        assertEquals(dto,
            customController.getAllCustomToDoListItemsForHabit(userVO, habitId).getBody().get(0));
    }
}
