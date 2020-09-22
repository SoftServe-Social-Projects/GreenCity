package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import static greencity.ModelUtils.getUser;
import greencity.config.SecurityConfig;
import greencity.dto.tipsandtrickscomment.AddTipsAndTricksCommentDtoRequest;
import greencity.entity.User;
import greencity.service.TipsAndTricksCommentService;
import greencity.service.UserService;
import java.security.Principal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ContextConfiguration
@Import(SecurityConfig.class)
class TipsAndTricksCommentControllerTest {
    private static final String tipsAndTricksCommentLink = "/tipsandtricks/comments";
    private MockMvc mockMvc;
    @InjectMocks
    private TipsAndTricksCommentController tipsAndTricksCommentController;
    @Mock
    private TipsAndTricksCommentService tipsAndTricksCommentService;
    @Mock
    private UserService userService;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(tipsAndTricksCommentController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void saveTest() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("Liam.Johnson@gmail.com");
        User user = getUser();
        when(userService.findByEmail(anyString())).thenReturn(user);

        String content = "{\n" +
            "  \"parentCommentId\": 0,\n" +
            "  \"text\": \"string\"\n" +
            "}";

        mockMvc.perform(post(tipsAndTricksCommentLink + "/{tipsAndTricksId}", 1)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content))
            .andExpect(status().isCreated());

        ObjectMapper mapper = new ObjectMapper();
        AddTipsAndTricksCommentDtoRequest addTipsAndTricksCommentDtoRequest =
            mapper.readValue(content, AddTipsAndTricksCommentDtoRequest.class);

        verify(userService).findByEmail(eq("Liam.Johnson@gmail.com"));
        verify(tipsAndTricksCommentService).save(eq(1L), eq(addTipsAndTricksCommentDtoRequest), eq(user));
    }

    @Test
    void saveBadRequestTest() throws Exception {
        mockMvc.perform(post(tipsAndTricksCommentLink + "/{tipsAndTricksId}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void findAllTest() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("Liam.Johnson@gmail.com");
        User user = getUser();
        when(userService.findByEmail(anyString())).thenReturn(user);

        int pageNumber = 5;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        mockMvc.perform(get(tipsAndTricksCommentLink + "?tipsAndTricksId=1&page=5")
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(eq("Liam.Johnson@gmail.com"));
        verify(tipsAndTricksCommentService).findAllComments(eq(pageable), eq(user), eq(1L));
    }

    @Test
    void getCountOfCommentsTest() throws Exception {
        mockMvc.perform(get(tipsAndTricksCommentLink + "/count/comments?id=1"))
            .andExpect(status().isOk());

        verify(tipsAndTricksCommentService).countComments(eq(1L));
    }

    @Test
    void findAllRepliesTest() throws Exception {
        mockMvc.perform(get(tipsAndTricksCommentLink + "/replies/{parentCommentId}", 1))
            .andExpect(status().isOk());

        verify(tipsAndTricksCommentService).findAllReplies(eq(1L));
    }

    @Test
    void deleteTest() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("Liam.Johnson@gmail.com");
        User user = getUser();
        when(userService.findByEmail(anyString())).thenReturn(user);

        mockMvc.perform(delete(tipsAndTricksCommentLink + "?id=1")
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(eq("Liam.Johnson@gmail.com"));
        verify(tipsAndTricksCommentService).deleteById(eq(1L), eq(user));
    }

    @Test
    void updateTest() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("Liam.Johnson@gmail.com");
        User user = getUser();
        when(userService.findByEmail(anyString())).thenReturn(user);

        mockMvc.perform(patch(tipsAndTricksCommentLink + "?id=1&text=text")
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(eq("Liam.Johnson@gmail.com"));
        verify(tipsAndTricksCommentService).update(eq("text"), eq(1L), eq(user));
    }

    @Test
    void likeTest() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("Liam.Johnson@gmail.com");
        User user = getUser();
        when(userService.findByEmail(anyString())).thenReturn(user);

        mockMvc.perform(post(tipsAndTricksCommentLink + "/like?id=1")
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail(eq("Liam.Johnson@gmail.com"));
        verify(tipsAndTricksCommentService).like(eq(1L), eq(user));
    }

    @Test
    void getCountOfLikesTest() throws Exception {
        mockMvc.perform(get(tipsAndTricksCommentLink + "/count/likes?id=1"))
            .andExpect(status().isOk());

        verify(tipsAndTricksCommentService).countLikes(eq(1L));
    }

    @Test
    void getCountOfRepliesTest() throws Exception {
        mockMvc.perform(get(tipsAndTricksCommentLink + "/count/replies?parentCommentId=1"))
            .andExpect(status().isOk());

        verify(tipsAndTricksCommentService).countReplies(eq(1L));
    }
}
