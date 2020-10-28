package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.config.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.econewscomment.AddEcoNewsCommentDtoRequest;
import greencity.dto.user.UserVO;
import greencity.entity.User;
import greencity.service.UserService;
import java.security.Principal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static greencity.ModelUtils.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ContextConfiguration
@Import(SecurityConfig.class)
class EcoNewsCommentControllerTest {
    private static final String ecoNewsCommentControllerLink = "/econews/comments";
    private MockMvc mockMvc;

    @InjectMocks
    private EcoNewsCommentController ecoNewsCommentController;

    @Mock
    private EcoNewsCommentService ecoNewsCommentService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    private Principal principal = getPrincipal();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(ecoNewsCommentController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .build();
    }

    @Test
    void save() throws Exception {
        User user = getUser();
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        String content = "{\n"
            + "  \"parentCommentId\": 0,\n"
            + "  \"text\": \"string\"\n"
            + "}";

        mockMvc.perform(post(ecoNewsCommentControllerLink + "/{econewsId}", 1)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content))
            .andExpect(status().isCreated());

        ObjectMapper mapper = new ObjectMapper();
        AddEcoNewsCommentDtoRequest addEcoNewsCommentDtoRequest =
            mapper.readValue(content, AddEcoNewsCommentDtoRequest.class);

        verify(userService).findByEmail(eq("test@gmail.com"));
        verify(ecoNewsCommentService).save(eq(1L), eq(addEcoNewsCommentDtoRequest), eq(userVO));
    }

    @Test
    void saveBadRequestTest() throws Exception {
        mockMvc.perform(post(ecoNewsCommentControllerLink + "/{econewsId}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void findAll() throws Exception {
        User user = getUser();
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        int pageNumber = 5;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        mockMvc.perform(get(ecoNewsCommentControllerLink + "?ecoNewsId=1&page=5")
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(eq("test@gmail.com"));
        verify(ecoNewsCommentService).findAllComments(eq(pageable), eq(userVO), eq(1L));
    }

    @Test
    void getAllActiveComments() throws Exception {
        User user = getUser();
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        int pageNumber = 5;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        mockMvc.perform(get(ecoNewsCommentControllerLink + "/active?ecoNewsId=1&page=5")
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(eq("test@gmail.com"));
        verify(ecoNewsCommentService).getAllActiveComments(eq(pageable), eq(userVO), eq(1L));
    }

    @Test
    void getCountOfComments() throws Exception {
        mockMvc.perform(get(ecoNewsCommentControllerLink + "/count/comments/{ecoNewsId}", 1))
            .andExpect(status().isOk());

        verify(ecoNewsCommentService).countOfComments(eq(1L));
    }

    @Test
    void findAllReplies() throws Exception {
        User user = getUser();
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        int pageNumber = 5;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        mockMvc.perform(get(ecoNewsCommentControllerLink + "/replies/{parentCommentId}?page=5&size=20", 1)
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(eq("test@gmail.com"));
        verify(ecoNewsCommentService).findAllReplies(eq(pageable), eq(1L), eq(userVO));
    }

    @Test
    void findAllActiveReplies() throws Exception {
        User user = getUser();
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        int pageNumber = 5;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        mockMvc.perform(get(ecoNewsCommentControllerLink + "/replies/active/{parentCommentId}?page=5&size=20", 1)
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(eq("test@gmail.com"));
        verify(ecoNewsCommentService).findAllActiveReplies(eq(pageable), eq(1L), eq(userVO));
    }

    @Test
    void getCountOfReplies() throws Exception {
        mockMvc.perform(get(ecoNewsCommentControllerLink + "/count/replies/{parentCommentId}", 1))
            .andExpect(status().isOk());

        verify(ecoNewsCommentService).countReplies(eq(1L));
    }

    @Test
    void deleteTest() throws Exception {
        User user = getUser();
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(delete(ecoNewsCommentControllerLink + "?id=1")
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(eq("test@gmail.com"));
        verify(ecoNewsCommentService).deleteById(eq(1L), eq(userVO));
    }

    @Test
    void update() throws Exception {
        User user = getUser();
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(patch(ecoNewsCommentControllerLink + "?id=1&text=text")
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(eq("test@gmail.com"));
        verify(ecoNewsCommentService).update(eq("text"), eq(1L), eq(userVO));
    }

    @Test
    void like() throws Exception {
        User user = getUser();
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(post(ecoNewsCommentControllerLink + "/like?id=1")
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(eq("test@gmail.com"));
        verify(ecoNewsCommentService).like(eq(1L), eq(userVO));
    }

    @Test
    void getCountOfLikes() throws Exception {
        mockMvc.perform(get(ecoNewsCommentControllerLink + "/count/likes?id=1"))
            .andExpect(status().isOk());

        verify(ecoNewsCommentService).countLikes(eq(1L));
    }
}
