package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.converters.UserArgumentResolver;
import greencity.dto.user.UserToDoListItemRequestDto;
import greencity.dto.user.UserToDoListItemRequestWithStatusDto;
import greencity.dto.user.UserToDoListItemResponseDto;
import greencity.dto.user.UserVO;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.UserService;
import greencity.service.UserToDoListItemService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import java.security.Principal;
import java.util.List;
import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserToDoListItemRequestDto;
import static greencity.ModelUtils.getUserToDoListItemRequestWithStatusDto;
import static greencity.ModelUtils.getUserToDoListItemResponseDto;
import static greencity.ModelUtils.getUserVO;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserToDoListItemControllerTest {
    private MockMvc mockMvc;

    @InjectMocks
    private UserToDoListItemController userToDoListItemController;

    @Mock
    UserToDoListItemService userToDoListItemService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Validator mockValidator;

    private static final String userToDoListItemLink = "/habits/assign/user-to-do-list-items";

    private ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    private final Principal principal = getPrincipal();

    private UserToDoListItemResponseDto dto;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(userToDoListItemController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
            .setValidator(mockValidator)
            .build();

        dto = getUserToDoListItemResponseDto();
    }

    @Test
    void getUserToDoListItemsForHabitAssignIsOk() throws Exception {
        Long habitAssignId = 1L;
        List<UserToDoListItemResponseDto> expected = List.of(dto);
        UserVO userVO = getUserVO();
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(userToDoListItemService.findAllForHabitAssign(eq(habitAssignId), eq(userVO.getId()), anyString()))
            .thenReturn(expected);
        this.mockMvc.perform(get(userToDoListItemLink + "/" + habitAssignId)
            .principal(principal)).andExpect(status().isOk());
        verify(userToDoListItemService).findAllForHabitAssign(eq(habitAssignId), eq(userVO.getId()), anyString());
    }

    @Test
    void getUserToDoListItemsForHabitAssignIsForbidden() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(userToDoListItemService.findAllForHabitAssign(eq(habitAssignId), eq(userVO.getId()), anyString()))
            .thenThrow(UserHasNoPermissionToAccessException.class);
        this.mockMvc.perform(get(userToDoListItemLink + "/" + habitAssignId)
            .principal(principal)).andExpect(status().isForbidden());
        verify(userToDoListItemService).findAllForHabitAssign(eq(habitAssignId), eq(userVO.getId()), anyString());
    }

    @Test
    void getUserToDoListItemsForHabitAssignIsNotFound() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(userToDoListItemService.findAllForHabitAssign(eq(habitAssignId), eq(userVO.getId()), anyString()))
            .thenThrow(NotFoundException.class);
        this.mockMvc.perform(get(userToDoListItemLink + "/" + habitAssignId)
            .principal(principal)).andExpect(status().isNotFound());
        verify(userToDoListItemService).findAllForHabitAssign(eq(habitAssignId), eq(userVO.getId()), anyString());
    }

    @Test
    void saveUserToDoListItemsForHabitAssignIsCreated() throws Exception {
        Long habitAssignId = 1L;
        List<UserToDoListItemResponseDto> expected = List.of(dto);
        UserVO userVO = getUserVO();
        UserToDoListItemRequestDto requestDto = getUserToDoListItemRequestDto();
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(List.of(requestDto));
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(userToDoListItemService.saveUserToDoListItems(eq(habitAssignId), eq(List.of(requestDto)),
            eq(userVO.getId()), anyString()))
            .thenReturn(expected);
        this.mockMvc.perform(post(userToDoListItemLink + "/" + habitAssignId)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)).andExpect(status().isCreated());
        verify(userToDoListItemService).saveUserToDoListItems(eq(habitAssignId), eq(List.of(requestDto)),
            eq(userVO.getId()), anyString());
    }

    @Test
    void saveUserToDoListItemsForHabitAssignIsForbidden() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        UserToDoListItemRequestDto requestDto = getUserToDoListItemRequestDto();
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(List.of(requestDto));
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(userToDoListItemService.saveUserToDoListItems(eq(habitAssignId), eq(List.of(requestDto)),
            eq(userVO.getId()), anyString()))
            .thenThrow(UserHasNoPermissionToAccessException.class);
        this.mockMvc.perform(post(userToDoListItemLink + "/" + habitAssignId)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)).andExpect(status().isForbidden());
        verify(userToDoListItemService).saveUserToDoListItems(eq(habitAssignId), eq(List.of(requestDto)),
            eq(userVO.getId()), anyString());
    }

    @Test
    void saveUserToDoListItemsForHabitAssignIsNotFound() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        UserToDoListItemRequestDto requestDto = getUserToDoListItemRequestDto();
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(List.of(requestDto));
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(userToDoListItemService.saveUserToDoListItems(eq(habitAssignId), eq(List.of(requestDto)),
            eq(userVO.getId()), anyString()))
            .thenThrow(NotFoundException.class);
        this.mockMvc.perform(post(userToDoListItemLink + "/" + habitAssignId)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)).andExpect(status().isNotFound());
        verify(userToDoListItemService).saveUserToDoListItems(eq(habitAssignId), eq(List.of(requestDto)),
            eq(userVO.getId()), anyString());
    }

    @Test
    void deleteUserToDoListItemsForHabitAssignIsOk() throws Exception {
        Long habitAssignId = 1L;
        List<Long> itemIds = List.of(1L, 2L);
        UserVO userVO = getUserVO();
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(itemIds);
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        doNothing().when(userToDoListItemService).deleteUserToDoListItems(habitAssignId, itemIds, userVO.getId());
        this.mockMvc.perform(delete(userToDoListItemLink + "/" + habitAssignId)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)).andExpect(status().isOk());
        verify(userToDoListItemService).deleteUserToDoListItems(habitAssignId, itemIds, userVO.getId());
    }

    @Test
    void deleteUserToDoListItemsForHabitAssignIsForbidden() throws Exception {
        Long habitAssignId = 1L;
        List<Long> itemIds = List.of(1L, 2L);
        UserVO userVO = getUserVO();
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(itemIds);
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        doThrow(UserHasNoPermissionToAccessException.class).when(userToDoListItemService)
            .deleteUserToDoListItems(habitAssignId, itemIds, userVO.getId());
        this.mockMvc.perform(delete(userToDoListItemLink + "/" + habitAssignId)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)).andExpect(status().isForbidden());
        verify(userToDoListItemService).deleteUserToDoListItems(habitAssignId, itemIds, userVO.getId());
    }

    @Test
    void deleteUserToDoListItemsForHabitAssignIsNoFound() throws Exception {
        Long habitAssignId = 1L;
        List<Long> itemIds = List.of(1L, 2L);
        UserVO userVO = getUserVO();
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(itemIds);
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        doThrow(NotFoundException.class).when(userToDoListItemService).deleteUserToDoListItems(habitAssignId, itemIds,
            userVO.getId());
        this.mockMvc.perform(delete(userToDoListItemLink + "/" + habitAssignId)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)).andExpect(status().isNotFound());
        verify(userToDoListItemService).deleteUserToDoListItems(habitAssignId, itemIds, userVO.getId());
    }

    @Test
    void changeStatusUserToDoListItemsIsOk() throws Exception {
        Long habitAssignId = 1L;
        List<UserToDoListItemResponseDto> expected = List.of(dto);
        UserVO userVO = getUserVO();
        UserToDoListItemRequestWithStatusDto requestDto = getUserToDoListItemRequestWithStatusDto();
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(List.of(requestDto));
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(userToDoListItemService.changeStatusesUserToDoListItems(eq(habitAssignId), eq(List.of(requestDto)),
            eq(userVO.getId()), anyString()))
            .thenReturn(expected);
        this.mockMvc.perform(patch(userToDoListItemLink + "/" + habitAssignId)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)).andExpect(status().isOk());
        verify(userToDoListItemService).changeStatusesUserToDoListItems(eq(habitAssignId), eq(List.of(requestDto)),
            eq(userVO.getId()), anyString());
    }

    @Test
    void changeStatusUserToDoListItemsIsForbidden() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        UserToDoListItemRequestWithStatusDto requestDto = getUserToDoListItemRequestWithStatusDto();
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(List.of(requestDto));
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(userToDoListItemService.changeStatusesUserToDoListItems(eq(habitAssignId), eq(List.of(requestDto)),
            eq(userVO.getId()), anyString()))
            .thenThrow(UserHasNoPermissionToAccessException.class);
        this.mockMvc.perform(patch(userToDoListItemLink + "/" + habitAssignId)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)).andExpect(status().isForbidden());
        verify(userToDoListItemService).changeStatusesUserToDoListItems(eq(habitAssignId), eq(List.of(requestDto)),
            eq(userVO.getId()), anyString());
    }

    @Test
    void changeStatusUserToDoListItemsIsNotFound() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        UserToDoListItemRequestWithStatusDto requestDto = getUserToDoListItemRequestWithStatusDto();
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(List.of(requestDto));
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(userToDoListItemService.changeStatusesUserToDoListItems(eq(habitAssignId), eq(List.of(requestDto)),
            eq(userVO.getId()), anyString()))
            .thenThrow(NotFoundException.class);
        this.mockMvc.perform(patch(userToDoListItemLink + "/" + habitAssignId)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)).andExpect(status().isNotFound());
        verify(userToDoListItemService).changeStatusesUserToDoListItems(eq(habitAssignId), eq(List.of(requestDto)),
            eq(userVO.getId()), anyString());
    }
}