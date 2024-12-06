package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.converters.UserArgumentResolver;
import greencity.dto.todolistitem.ToDoListItemResponseDto;
import greencity.dto.user.UserVO;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.ToDoListItemService;
import java.security.Principal;
import java.util.List;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getToDoListItemResponseDto;
import static greencity.ModelUtils.getUserVO;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ToDoListItemControllerTest {
    private MockMvc mockMvc;

    @InjectMocks
    private ToDoListItemController toDoListItemController;

    @Mock
    private ToDoListItemService toDoListItemService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Validator mockValidator;

    private static final String toDoListItemLink = "/habits/to-do-list-items";

    private ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    private final Principal principal = getPrincipal();

    private ToDoListItemResponseDto dto;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(toDoListItemController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
            .setValidator(mockValidator)
            .build();

        dto = getToDoListItemResponseDto();
    }

    @Test
    void getAllToDoListItemsForHabitIsOk() throws Exception {
        Long habitId = 1L;
        List<ToDoListItemResponseDto> expected = List.of(dto);
        when(toDoListItemService.findAllHabitToDoList(eq(habitId), anyString()))
            .thenReturn(expected);
        this.mockMvc.perform(get(toDoListItemLink + "/" + habitId)
            .principal(principal)).andExpect(status().isOk());
        verify(toDoListItemService).findAllHabitToDoList(eq(habitId), anyString());
    }

    @Test
    void getAllToDoListItemsForHabitIsNotFound() throws Exception {
        Long habitId = 1L;
        when(toDoListItemService.findAllHabitToDoList(eq(habitId), anyString()))
            .thenThrow(NotFoundException.class);
        this.mockMvc.perform(get(toDoListItemLink + "/" + habitId)
            .principal(principal)).andExpect(status().isNotFound());
        verify(toDoListItemService).findAllHabitToDoList(eq(habitId), anyString());
    }

    @Test
    void findAvailableToDoListForHabitAssignIsOk() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        List<ToDoListItemResponseDto> expected = List.of(dto);
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(
            toDoListItemService.findAvailableToDoListForHabitAssign(eq(userVO.getId()), eq(habitAssignId), anyString()))
            .thenReturn(expected);
        this.mockMvc.perform(get(toDoListItemLink + "/assign/" + habitAssignId)
            .principal(principal)).andExpect(status().isOk());
        verify(toDoListItemService).findAvailableToDoListForHabitAssign(eq(userVO.getId()), eq(habitAssignId),
            anyString());
    }

    @Test
    void findAvailableToDoListForHabitAssignIsNotFound() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(
            toDoListItemService.findAvailableToDoListForHabitAssign(eq(userVO.getId()), eq(habitAssignId), anyString()))
            .thenThrow(NotFoundException.class);
        this.mockMvc.perform(get(toDoListItemLink + "/assign/" + habitAssignId)
            .principal(principal)).andExpect(status().isNotFound());
        verify(toDoListItemService).findAvailableToDoListForHabitAssign(eq(userVO.getId()), eq(habitAssignId),
            anyString());
    }
}
