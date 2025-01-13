package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.converters.UserArgumentResolver;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.user.UserVO;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.CustomToDoListItemService;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.security.Principal;
import java.util.List;
import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserVO;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomToDoListItemControllerTest {
    private MockMvc mockMvc;

    @InjectMocks
    private CustomToDoListItemController customController;

    @Mock
    private CustomToDoListItemService customToDoListItemService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ObjectMapper objectMapper;

    private CustomToDoListItemResponseDto dto;

    private ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    private final Principal principal = getPrincipal();

    private static final String customLink = "/habits/custom-to-do-list-items";

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(customController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
            .build();

        dto = new CustomToDoListItemResponseDto(3L, "text");
    }

    @Test
    void getAllAvailableCustomToDoListItemsIsOk() throws Exception {
        Long habitId = 1L;
        UserVO userVO = getUserVO();
        List<CustomToDoListItemResponseDto> expected = List.of(dto);
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(customToDoListItemService.findAllHabitCustomToDoList(userVO.getId(), habitId))
            .thenReturn(expected);
        this.mockMvc.perform(get(customLink + "/" + habitId)
            .principal(principal)).andExpect(status().isOk());
        verify(customToDoListItemService).findAllHabitCustomToDoList(userVO.getId(), habitId);
    }

    @Test
    void getAllAvailableCustomToDoListItemsIsBadRequest() throws Exception {
        Long habitId = 0L;
        UserVO userVO = getUserVO();
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        this.mockMvc.perform(get(customLink + "/" + habitId)
            .principal(principal)).andExpect(status().isBadRequest());
        verifyNoInteractions(customToDoListItemService);
    }

    @Test
    void getAllAvailableCustomToDoListItemsIsNotFound() throws Exception {
        Long habitId = 1L;
        UserVO userVO = getUserVO();
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(customToDoListItemService.findAllHabitCustomToDoList(userVO.getId(), habitId))
            .thenThrow(NotFoundException.class);
        this.mockMvc.perform(get(customLink + "/" + habitId)
            .principal(principal))
            .andExpect(status().isNotFound());
        verify(customToDoListItemService).findAllHabitCustomToDoList(userVO.getId(), habitId);
    }

    @Test
    void getAllNotAddedCustomToDoListItemsForHabitAssignIsOk() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        List<CustomToDoListItemResponseDto> expected = List.of(dto);
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(customToDoListItemService.findAvailableCustomToDoListForHabitAssign(userVO.getId(), habitAssignId))
            .thenReturn(expected);
        this.mockMvc.perform(get(customLink + "/assign/" + habitAssignId)
            .principal(principal)).andExpect(status().isOk());
        verify(customToDoListItemService).findAvailableCustomToDoListForHabitAssign(userVO.getId(), habitAssignId);
    }

    @Test
    void getAllNotAddedCustomToDoListItemsForHabitAssignIsBadRequest() throws Exception {
        Long habitAssignId = 0L;
        UserVO userVO = getUserVO();
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        this.mockMvc.perform(get(customLink + "/assign/" + habitAssignId)
            .principal(principal)).andExpect(status().isBadRequest());
        verifyNoInteractions(customToDoListItemService);
    }

    @Test
    void getAllNotAddedCustomToDoListItemsForHabitAssignIsForbidden() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(customToDoListItemService.findAvailableCustomToDoListForHabitAssign(userVO.getId(), habitAssignId))
            .thenThrow(UserHasNoPermissionToAccessException.class);
        this.mockMvc.perform(get(customLink + "/assign/" + habitAssignId)
            .principal(principal)).andExpect(status().isForbidden());
        verify(customToDoListItemService).findAvailableCustomToDoListForHabitAssign(userVO.getId(), habitAssignId);
    }

    @Test
    void getAllNotAddedCustomToDoListItemsForHabitAssignIsNotFound() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(customToDoListItemService.findAvailableCustomToDoListForHabitAssign(userVO.getId(), habitAssignId))
            .thenThrow(NotFoundException.class);
        this.mockMvc.perform(get(customLink + "/assign/" + habitAssignId)
            .principal(principal)).andExpect(status().isNotFound());
        verify(customToDoListItemService).findAvailableCustomToDoListForHabitAssign(userVO.getId(), habitAssignId);
    }
}
