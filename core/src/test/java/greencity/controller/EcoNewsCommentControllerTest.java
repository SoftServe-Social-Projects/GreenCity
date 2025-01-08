package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.config.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.PageableDto;
import greencity.dto.comment.AddCommentDtoRequest;
import greencity.dto.comment.CommentDto;
import greencity.dto.user.UserVO;
import greencity.enums.ArticleType;
import greencity.exception.exceptions.NotFoundException;
import greencity.service.CommentService;
import greencity.service.UserService;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Locale;
import static greencity.ModelUtils.getPageableCommentDtos;
import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserVO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ContextConfiguration
@Import(SecurityConfig.class)
class EcoNewsCommentControllerTest {
    private static final String ECONEWS_LINK = "/eco-news";
    private final Principal principal = getPrincipal();
    private MockMvc mockMvc;
    @InjectMocks
    private EcoNewsCommentController ecoNewsCommentController;
    @Mock
    private CommentService commentService;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(ecoNewsCommentController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .build();
    }

    @Test
    @SneakyThrows
    void saveTest() {
        UserVO userVO = getUserVO();
        Locale locale = Locale.of("en");
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        String content = """
            {
              "text": "string",
              "parentCommentId": "100"
            }
            """;

        MockMultipartFile jsonFile = new MockMultipartFile(
            "request",
            "",
            "application/json",
            content.getBytes());

        MockMultipartFile imageFile = new MockMultipartFile(
            "images",
            "image.jpg",
            "image/jpeg",
            "image data".getBytes());

        mockMvc.perform(multipart(ECONEWS_LINK + "/{ecoNewsId}/comments", 1)
            .file(jsonFile)
            .file(imageFile)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .content(content))
            .andExpect(status().isCreated());

        ObjectMapper mapper = new ObjectMapper();
        AddCommentDtoRequest addCommentDtoRequest =
            mapper.readValue(content, AddCommentDtoRequest.class);

        verify(userService).findByEmail("test@gmail.com");
        verify(commentService).save(ArticleType.ECO_NEWS, 1L, addCommentDtoRequest,
            new MultipartFile[] {imageFile}, userVO, locale);
        verify(commentService).save(eq(ArticleType.ECO_NEWS),
            eq(1L),
            eq(addCommentDtoRequest),
            any(MultipartFile[].class),
            eq(userVO),
            eq(Locale.of("en")));
    }

    @Test
    @SneakyThrows
    void saveBadRequestTest() {
        mockMvc.perform(post(ECONEWS_LINK + "/{ecoNewsId}/comments", 1)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void getEcoNewsCommentByIdTest() {
        String content = """
            {
              "text": "string"
            }
            """;
        mockMvc.perform(get(ECONEWS_LINK + "/comments/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void getAllActiveCommentsTest() {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        int pageNumber = 5;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        mockMvc.perform(get(ECONEWS_LINK + "/{ecoNewsId}/comments/active?page=5&ecoNewsId=1", 1)
            .principal(principal))
            .andExpect(status().isOk());

        verify(userService).findByEmail("test@gmail.com");
        verify(commentService).getAllActiveComments(pageable, userVO, 1L, ArticleType.ECO_NEWS);
    }

    @Test
    @SneakyThrows
    void countCommentsForEcoNewsTest() {
        mockMvc.perform(get(ECONEWS_LINK + "/{ecoNewsId}/comments/count", 1))
            .andExpect(status().isOk());

        verify(commentService).countCommentsForEcoNews(1L);
    }

    @Test
    @SneakyThrows
    void getAllActiveRepliesTest() {
        Long parentCommentId = 1L;
        int pageNumber = 0;
        int pageSize = 20;

        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        PageableDto<CommentDto> commentReplies = getPageableCommentDtos();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        String expectedJson = objectMapper.writeValueAsString(commentReplies);

        when(commentService.getAllActiveReplies(pageable, parentCommentId, userVO))
            .thenReturn(commentReplies);

        mockMvc.perform(get(ECONEWS_LINK + "/comments/{parentCommentId}/replies/active", parentCommentId)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
        verify(commentService).getAllActiveReplies(pageable, parentCommentId, userVO);
        verify(userService).findByEmail(principal.getName());
    }

    @Test
    @SneakyThrows
    void getAllActiveRepliesWithNotValidIdBadRequestTest() {
        String notValidId = "id";
        mockMvc.perform(get(ECONEWS_LINK + "/comments/{parentCommentId}/replies/active", notValidId))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void getAllActiveRepliesWithNonexistentIdNotFoundTest() {
        Long parentCommentId = 1L;

        int pageNumber = 0;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        String errorMessage = "ErrorMessage";

        doThrow(new NotFoundException(errorMessage))
            .when(commentService)
            .getAllActiveReplies(pageable, parentCommentId, userVO);

        Assertions.assertThatThrownBy(
            () -> mockMvc.perform(get(ECONEWS_LINK + "/comments/{parentCommentId}/replies/active",
                parentCommentId).principal(principal)).andExpect(status().isNotFound()))
            .hasCause(new NotFoundException(errorMessage));

        verify(userService).findByEmail(anyString());
        verify(commentService).getAllActiveReplies(pageable, parentCommentId, userVO);
    }

    @Test
    @SneakyThrows
    void getCountOfActiveRepliesTest() {
        Long parentCommentId = 1L;
        int repliesAmount = 10;
        String expectedResponse = "<Integer>10</Integer>";
        when(commentService.countAllActiveReplies(parentCommentId)).thenReturn(repliesAmount);

        mockMvc.perform(get(ECONEWS_LINK + "/comments/{parentCommentId}/replies/active/count", parentCommentId))
            .andExpect(status().isOk())
            .andExpect(content().xml(expectedResponse));

        verify(commentService).countAllActiveReplies(parentCommentId);
    }

    @Test
    @SneakyThrows
    void getCountOfActiveRepliesBadRequestTest() {
        String notValidId = "id";
        mockMvc.perform(get(ECONEWS_LINK + "/comments/{parentCommentId}/replies/active/count", notValidId))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void getCountOfActiveRepliesNotFoundTest() {
        Long parentCommentId = 1L;
        String errorMessage = "ErrorMessage";

        doThrow(new NotFoundException(errorMessage))
            .when(commentService)
            .countAllActiveReplies(parentCommentId);

        Assertions.assertThatThrownBy(() -> mockMvc.perform(
            get(ECONEWS_LINK + "/comments/{parentCommentId}/replies/active/count", parentCommentId))
            .andExpect(status().isNotFound()))
            .hasCause(new NotFoundException(errorMessage));

        verify(commentService).countAllActiveReplies(parentCommentId);
    }

    @Test
    @SneakyThrows
    void likeTest() {
        String commentId = "1";
        Long numericCommentId = Long.valueOf(commentId);

        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(post(ECONEWS_LINK + "/comments/like")
            .param("commentId", commentId)
            .principal(principal))
            .andExpect(status().isOk());

        verify(commentService).like(numericCommentId, userVO, null);
    }

    @Test
    @SneakyThrows
    void likeWithNotValidIdBadRequestTest() {
        String notValidId = "id";

        mockMvc.perform(post(ECONEWS_LINK + "/comments/like")
            .param("commentId", notValidId)
            .principal(principal))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void likeNotFoundTest() {
        Long commentId = 1L;
        String commentIdParam = "1";

        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        String errorMessage = "ErrorMessage";

        doThrow(new NotFoundException(errorMessage))
            .when(commentService)
            .like(commentId, userVO, null);

        Assertions.assertThatThrownBy(
            () -> mockMvc.perform(post(ECONEWS_LINK + "/comments/like")
                .param("commentId", commentIdParam)
                .principal(principal))
                .andExpect(status().isNotFound()))
            .hasCause(new NotFoundException(errorMessage));

        verify(userService).findByEmail(anyString());
    }

    @Test
    @SneakyThrows
    void updateTest() {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        String content = "string";

        mockMvc.perform(patch(ECONEWS_LINK + "/comments")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .param("commentId", "1")
            .content(content))
            .andExpect(status().isOk());

        verify(userService).findByEmail("test@gmail.com");
        verify(commentService).update(content, 1L, userVO);
    }

    @Test
    @SneakyThrows
    void deleteTest() {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);

        mockMvc.perform(delete(ECONEWS_LINK + "/comments/{id}", 1)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(userService).findByEmail("test@gmail.com");
        verify(commentService).delete(1L, userVO);
    }

    @Test
    @SneakyThrows
    void dislikeTest() {
        String commentId = "1";
        Long numericCommentId = Long.valueOf(commentId);

        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(post(ECONEWS_LINK + "/comments/dislike")
            .param("commentId", commentId)
            .principal(principal))
            .andExpect(status().isOk());

        verify(commentService).dislike(numericCommentId, userVO, null);
    }

    @Test
    @SneakyThrows
    void dislikeWithNotValidIdBadRequestTest() {
        String notValidId = "id";

        mockMvc.perform(post(ECONEWS_LINK + "/comments/dislike")
            .param("commentId", notValidId)
            .principal(principal))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void dislikeNotFoundTest() {
        Long commentId = 1L;
        String commentIdParam = "1";

        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        String errorMessage = "ErrorMessage";

        doThrow(new NotFoundException(errorMessage))
            .when(commentService)
            .dislike(commentId, userVO, null);

        Assertions.assertThatThrownBy(
            () -> mockMvc.perform(post(ECONEWS_LINK + "/comments/dislike")
                .param("commentId", commentIdParam)
                .principal(principal))
                .andExpect(status().isNotFound()))
            .hasCause(new NotFoundException(errorMessage));

        verify(userService).findByEmail(anyString());
    }

}
