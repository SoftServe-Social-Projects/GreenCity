package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserVO;

import greencity.ModelUtils;
import greencity.converters.UserArgumentResolver;
import greencity.dto.econews.AddEcoNewsDtoRequest;
import greencity.dto.econews.EcoNewsDto;
import greencity.dto.user.UserVO;
import greencity.entity.EcoNews;
import greencity.entity.User;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.EcoNewsService;
import greencity.service.TagsService;
import greencity.service.UserService;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EcoNewsControllerTest {
    private static final String ecoNewsLink = "/eco-news";
    private MockMvc mockMvc;
    @InjectMocks
    private EcoNewsController ecoNewsController;
    @Mock
    private EcoNewsService ecoNewsService;
    @Mock
    private TagsService tagsService;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ObjectMapper objectMapper;

    private final Principal principal = getPrincipal();

    private ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(ecoNewsController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
            .build();
    }

    @Test
    void saveTest() throws Exception {
        String json = """
                {
                    "title": "title",
                    "tags": ["news"],
                    "text": "content content content",
                    "source": ""
                }
            """;
        MockMultipartFile jsonFile =
            new MockMultipartFile("addEcoNewsDtoRequest", "", "application/json", json.getBytes());

        this.mockMvc.perform(multipart(ecoNewsLink)
            .file(jsonFile)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isCreated());

        ObjectMapper mapper = new ObjectMapper();
        AddEcoNewsDtoRequest addEcoNewsDtoRequest = mapper.readValue(json, AddEcoNewsDtoRequest.class);

        verify(ecoNewsService)
            .saveEcoNews(eq(addEcoNewsDtoRequest), isNull(), eq("test@gmail.com"));
    }

    @Test
    void saveBadRequestTest() throws Exception {
        mockMvc.perform(post(ecoNewsLink)
            .content("{}")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getEcoNewsById() throws Exception {
        mockMvc.perform(get(ecoNewsLink + "/{ecoNewsId}", 1))
            .andExpect(status().isOk());

        verify(ecoNewsService).findDtoByIdAndLanguage(1L, "en");
    }

    @Test
    void findFavoritesTest() {
        // Arrange
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        String email = "user@example.com";

        // Mocking the user
        User user = new User();
        user.setId(1L);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        // Mocking the ecoNews page
        EcoNews ecoNews = new EcoNews();
        Page<EcoNews> ecoNewsPage = new PageImpl<>(Collections.singletonList(ecoNews), pageable, 1);

        when(ecoNewsRepo.findAll(any(), any(Pageable.class))).thenReturn(ecoNewsPage);
        when(modelMapper.map(any(), eq(EcoNewsDto.class))).thenReturn(new EcoNewsDto());

        // Act
        PageableAdvancedDto<EcoNewsDto> result = ecoNewsService.find(pageable, null, null, null, true, email);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(ecoNewsRepo, times(1)).findAll(any(), any(Pageable.class));
        verify(modelMapper, times(1)).map(any(), eq(EcoNewsDto.class));
    }

    @Test
    void findAllTest() throws Exception {
        int pageNumber = 1;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        mockMvc.perform(get(ecoNewsLink + "?page=1"))
            .andExpect(status().isOk());

        verify(ecoNewsService).find(pageable, null, null, null, false, null);
    }

    @Test
    void findAllByUserTest() throws Exception {
        int pageNumber = 1;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        mockMvc.perform(get(ecoNewsLink + "?author-id=1&page=1"))
            .andExpect(status().isOk());

        verify(ecoNewsService).find(pageable, null, null, 1L, false, null);
    }

    @Test
    void getPredicateTest() {
        // Arrange
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<EcoNews> root = mock(Root.class);

        // Mocking the user
        User user = new User();
        user.setId(1L);

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));

        // Calling the actual method
        Predicate predicate = ecoNewsService.getPredicate(root, cb, null, null, null, true, "user@example.com");

        // Verifying the result
        assertNotNull(predicate);
        verify(root).join("followers");
    }

    @Test
    void deleteTest() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(delete(ecoNewsLink + "/{econewsId}", 1)
            .principal(principal))
            .andExpect(status().isOk());

        verify(ecoNewsService).delete(1L, userVO);
    }

    @Test
    void getThreeRecommendedEcoNewsTest() throws Exception {
        mockMvc.perform(get(ecoNewsLink + "/{ecoNewsId}/recommended", 1L))
            .andExpect(status().isOk());

        verify(ecoNewsService).getThreeRecommendedEcoNews(1L);
    }

    @Test
    void findAllEcoNewsTagsTest() throws Exception {
        String language = "en";
        mockMvc.perform(get(ecoNewsLink + "/tags?lang=" + language))
            .andExpect(status().isOk());

        verify(tagsService).findAllEcoNewsTags(language);
    }

    @Test
    void likeTest() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(post(ecoNewsLink + "/{ecoNewsId}/likes", 1)
            .principal(principal))
            .andExpect(status().isOk());

        verify(ecoNewsService).like(userVO, 1L);
    }

    @Test
    void dislikeTest() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        mockMvc.perform(post(ecoNewsLink + "/{ecoNewsId}/dislikes", 1)
            .principal(principal))
            .andExpect(status().isOk());
        verify(ecoNewsService).dislike(userVO, 1L);

    }

    @Test
    void countLikesForEcoNewsTest() throws Exception {
        mockMvc.perform(get(ecoNewsLink + "/{ecoNewsId}/likes/count", 1L))
            .andExpect(status().isOk());

        verify(ecoNewsService).countLikesForEcoNews(1L);
    }

    @Test
    void countDislikesForEcoNewsTest() throws Exception {
        mockMvc.perform(get(ecoNewsLink + "/{ecoNewsId}/dislikes/count", 1L))
            .andExpect(status().isOk());

        verify(ecoNewsService).countDislikesForEcoNews(1L);
    }

    @Test
    void checkNewsIsLikedByUserTest() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(get(ecoNewsLink + "/{ecoNewsId}/likes/{userId}", 1, 1)
            .principal(principal))
            .andExpect(status().isOk());

        verify(ecoNewsService).checkNewsIsLikedByUser(1L, userVO.getId());
    }

    @Test
    void findAmountOfPublishedNews() throws Exception {
        mockMvc.perform(get(ecoNewsLink + "/count")
            .param("author-id", "1"))
            .andExpect(status().isOk());

        verify(ecoNewsService).getAmountOfPublishedNews(1L);
    }

    @Test
    void getContentAndSourceForEcoNewsById() throws Exception {
        mockMvc.perform(get(ecoNewsLink + "/{ecoNewsId}/summary", 1L))
            .andExpect(status().isOk());

        verify(ecoNewsService).getContentAndSourceForEcoNewsById(1L);

    }

    @Test
    void getContentAndSourceForEcoNewsByIdNot_Found_Request() throws Exception {
        Mockito.when(ecoNewsService.getContentAndSourceForEcoNewsById(1L)).thenThrow(NotFoundException.class);

        mockMvc.perform(get(ecoNewsLink + "/{ecoNewsId}/summary", 1L))
            .andExpect(status().isNotFound());

        verify(ecoNewsService).getContentAndSourceForEcoNewsById(1L);
    }

    @Test
    void addToFavoritesTest() throws Exception {
        mockMvc.perform(post(ecoNewsLink + "/{ecoNewsId}/favorites", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ecoNewsService).addToFavorites(1L, principal.getName());
    }

    @Test
    void addToFavoritesNotFoundTest() throws Exception {
        doThrow(new NotFoundException("Resource not found")).when(ecoNewsService).addToFavorites(anyLong(),
            anyString());

        mockMvc.perform(post(ecoNewsLink + "/{ecoNewsId}/favorites", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(ecoNewsService).addToFavorites(1L, principal.getName());
    }

    @Test
    void addToFavoritesBadRequestTest() throws Exception {
        doThrow(new IllegalArgumentException("Bad request")).when(ecoNewsService).addToFavorites(anyLong(),
            anyString());

        mockMvc.perform(post(ecoNewsLink + "/{ecoNewsId}/favorites", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(ecoNewsService).addToFavorites(1L, principal.getName());
    }

    @Test
    void removeFromFavoritesNotFoundTest() throws Exception {
        doThrow(new NotFoundException("Resource not found")).when(ecoNewsService).removeFromFavorites(anyLong(),
            anyString());

        mockMvc.perform(delete(ecoNewsLink + "/{ecoNewsId}/favorites", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(ecoNewsService).removeFromFavorites(1L, principal.getName());
    }

    @Test
    void removeFromFavoritesBadRequestTest() throws Exception {
        doThrow(new IllegalArgumentException("Bad request")).when(ecoNewsService).removeFromFavorites(anyLong(),
            anyString());

        mockMvc.perform(delete(ecoNewsLink + "/{ecoNewsId}/favorites", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(ecoNewsService).removeFromFavorites(1L, principal.getName());
    }

    @Test
    void removeFromFavoritesTest() throws Exception {
        mockMvc.perform(delete(ecoNewsLink + "/{ecoNewsId}/favorites", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ecoNewsService).removeFromFavorites(1L, principal.getName());
    }

    @Test
    void getFavoritesTest() throws Exception {
        List<EcoNewsDto> ecoNewsDtoList = List.of(ModelUtils.getEcoNewsDto(), ModelUtils.getEcoNewsDto());

        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(ecoNewsService.getFavorites(anyString())).thenReturn(ecoNewsDtoList);

        mockMvc.perform(get(ecoNewsLink + "/favorites")
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        verify(ecoNewsService).getFavorites(principal.getName());
    }

    @Test
    void getFavorites_NotFoundTest() throws Exception {
        UserVO userVO = getUserVO();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(ecoNewsService.getFavorites(anyString())).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get(ecoNewsLink + "/favorites")
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(ecoNewsService).getFavorites(principal.getName());
    }
}
