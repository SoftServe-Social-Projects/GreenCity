package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import greencity.ModelUtils;
import greencity.dto.habit.CustomHabitDtoRequest;
import greencity.dto.user.UserVO;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.repository.HabitTranslationRepo;
import greencity.service.HabitService;
import greencity.service.TagsService;
import greencity.service.UserService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import static greencity.ModelUtils.getPrincipal;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HabitControllerTest {

    private MockMvc mockMvc;

    @Mock
    HabitService habitService;

    @Mock
    TagsService tagsService;

    @Mock
    private UserService userService;

    @InjectMocks
    HabitController habitController;

    @Mock
    private Validator mockValidator;

    @Mock
    private HabitTranslationRepo habitTranslationRepo;

    private static final String habitLink = "/habit";

    private final Principal principal = getPrincipal();

    private final ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(habitController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
            .setValidator(mockValidator)
            .build();
    }

    @Test
    void getAll() throws Exception {
        int pageNumber = 1;
        int pageSize = 20;
        Locale locale = Locale.of("en");
        UserVO userVO = new UserVO();
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        mockMvc.perform(get(habitLink + "?page=1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitService).getAllHabitsByLanguageCode(userVO, pageable, locale.getLanguage());
    }

    @Test
    void getMyHabits() throws Exception {
        int pageNumber = 1;
        int pageSize = 20;
        Locale locale = Locale.of("en");
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        UserVO userVO = new UserVO();

        mockMvc.perform(get(habitLink + "/my")
            .param("page", String.valueOf(pageNumber))
            .param("size", String.valueOf(pageSize))
            .param("lang", String.valueOf(locale))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(habitService).getMyHabits(userVO.getId(), pageable, locale.getLanguage());
    }

    @Test
    void getAllHabitsOfFriend() throws Exception {
        int pageNumber = 1;
        int pageSize = 20;
        Locale locale = Locale.of("en");
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        UserVO userVO = new UserVO();
        Long friendId = 1L;

        mockMvc.perform(get(habitLink + "/all/{friendId}", friendId)
            .param("page", String.valueOf(pageNumber))
            .param("size", String.valueOf(pageSize))
            .param("lang", String.valueOf(locale))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(habitService).getAllHabitsOfFriend(userVO.getId(), friendId, pageable, locale.getLanguage());
    }

    @Test
    void getAllMutualHabitsWithFriend() throws Exception {
        int pageNumber = 1;
        int pageSize = 20;
        Locale locale = Locale.of("en");
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        UserVO userVO = new UserVO();
        Long friendId = 1L;

        mockMvc.perform(get(habitLink + "/allMutualHabits/{friendId}", friendId)
            .param("page", String.valueOf(pageNumber))
            .param("size", String.valueOf(pageSize))
            .param("lang", String.valueOf(locale))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(habitService).getAllMutualHabitsWithFriend(userVO.getId(), friendId, pageable, locale.getLanguage());
    }

    @Test
    void getAllByTagsAndLanguageCode() throws Exception {
        int pageNumber = 1;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Locale locale = Locale.of("en");
        List<String> tags = Arrays.asList("News", "Education");
        boolean excludeAssigned = false;

        mockMvc.perform(get(habitLink + "/tags/search?page=" + pageNumber +
            "&lang=" + locale.getLanguage() + "&tags=News,Education" + "&excludeAssigned=" + excludeAssigned)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitService).getAllByTagsAndLanguageCode(pageable, tags, locale.getLanguage(), excludeAssigned, null);
    }

    @Test
    void findAllHabitsTags() throws Exception {
        Locale locale = Locale.of("en");
        Gson gson = new Gson();

        mockMvc.perform(get(habitLink + "/tags")
            .content(gson.toJson(locale))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(tagsService).findAllHabitsTags(locale.getLanguage());
    }

    private Pageable createPageRequest() {
        int pageNumber = 0;
        int pageSize = 20;
        return PageRequest.of(pageNumber, pageSize);
    }

    @Test
    void findByDifferentParameters() throws Exception {
        Locale locale = Locale.of("en");
        Gson gson = new Gson();
        UserVO userVO = new UserVO();

        mockMvc.perform(get(habitLink + "/search")
            .param("tags", "reusable")
            .param("complexities", "1")
            .param("isCustomHabit", "true")
            .content(gson.toJson(locale))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitService).getAllByDifferentParameters(userVO, createPageRequest(), Optional.of(List.of("reusable")),
            Optional.of(true),
            Optional.of(List.of(1)),
            locale.getLanguage());
    }

    @Test
    void findByDifferentParametersWithComplexityAndTags() throws Exception {
        Locale locale = Locale.of("en");
        Gson gson = new Gson();
        UserVO userVO = new UserVO();
        mockMvc.perform(get(habitLink + "/search")
            .param("tags", "reusable")
            .param("complexities", "1")
            .content(gson.toJson(locale))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitService).getAllByDifferentParameters(userVO, createPageRequest(), Optional.of(List.of("reusable")),
            Optional.empty(),
            Optional.of(List.of(1)),
            locale.getLanguage());
    }

    @Test
    void findByDifferentParametersWithComplexityAndIsCustomHabit() throws Exception {
        Locale locale = Locale.of("en");
        Gson gson = new Gson();
        UserVO userVO = new UserVO();
        mockMvc.perform(get(habitLink + "/search")
            .param("complexities", "1")
            .param("isCustomHabit", "true")
            .content(gson.toJson(locale))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitService).getAllByDifferentParameters(userVO, createPageRequest(), Optional.empty(),
            Optional.of(true),
            Optional.of(List.of(1)),
            locale.getLanguage());
    }

    @Test
    void findByDifferentParametersWithTagsAndIsCustomHabit() throws Exception {
        Locale locale = Locale.of("en");
        Gson gson = new Gson();
        UserVO userVO = new UserVO();
        mockMvc.perform(get(habitLink + "/search")
            .param("tags", "reusable")
            .param("isCustomHabit", "true")
            .content(gson.toJson(locale))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitService).getAllByDifferentParameters(userVO, createPageRequest(), Optional.of(List.of("reusable")),
            Optional.of(true),
            Optional.empty(),
            locale.getLanguage());
    }

    @Test
    void findByDifferentParametersWithComplexity() throws Exception {
        Locale locale = Locale.of("en");
        Gson gson = new Gson();
        UserVO userVO = new UserVO();
        mockMvc.perform(get(habitLink + "/search")
            .param("complexities", "1")
            .content(gson.toJson(locale))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitService).getAllByDifferentParameters(userVO, createPageRequest(), Optional.empty(),
            Optional.empty(),
            Optional.of(List.of(1)),
            locale.getLanguage());
    }

    @Test
    void findByDifferentParametersWithIsCustomHabit() throws Exception {
        Locale locale = Locale.of("en");
        Gson gson = new Gson();
        UserVO userVO = new UserVO();
        mockMvc.perform(get(habitLink + "/search")
            .param("isCustomHabit", "true")
            .content(gson.toJson(locale))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitService).getAllByDifferentParameters(userVO, createPageRequest(), Optional.empty(),
            Optional.of(true),
            Optional.empty(),
            locale.getLanguage());
    }

    @Test
    void findByDifferentParametersWithTags() throws Exception {
        Locale locale = Locale.of("en");
        Gson gson = new Gson();
        UserVO userVO = new UserVO();
        mockMvc.perform(get(habitLink + "/search")
            .param("tags", "reusable")
            .content(gson.toJson(locale))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitService).getAllByDifferentParameters(userVO, createPageRequest(), Optional.of(List.of("reusable")),
            Optional.empty(),
            Optional.empty(),
            locale.getLanguage());
    }

    @Test
    void findByDifferentParametersBadRequest() throws Exception {
        Locale locale = Locale.of("en");
        Gson gson = new Gson();
        mockMvc.perform(get(habitLink + "/search")
            .content(gson.toJson(locale))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void findByDifferentParametersBadRequestWithEmptyList() throws Exception {
        Locale locale = Locale.of("en");
        Gson gson = new Gson();
        mockMvc.perform(get(habitLink + "/search")
            .param("tags", "")
            .content(gson.toJson(locale))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getHabitById() throws Exception {

        mockMvc.perform(get(habitLink + "/{id}", 1L))
            .andExpect(status().isOk());

        verify(habitService).getByIdAndLanguageCode(1L, "en");
    }

    @Test
    void getToDoListItems() throws Exception {

        mockMvc.perform(get(habitLink + "/{id}/to-do-list", 1L))
            .andExpect(status().isOk());

        verify(habitService).getToDoListForHabit(1L, "en");
    }

    @Test
    void postCustomHabit() throws Exception {
        CustomHabitDtoRequest dto = ModelUtils.getAddCustomHabitDtoRequest();

        objectMapper.findAndRegisterModules();

        String requestedJson = objectMapper.writeValueAsString(dto);

        MockMultipartFile jsonFile =
            new MockMultipartFile("request", "", "application/json", requestedJson.getBytes());

        mockMvc.perform(multipart(habitLink + "/custom")
            .file(jsonFile)
            .principal(principal)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isCreated());
        verify(habitService).addCustomHabit(dto, null, principal.getName());
    }

    @Test
    void getFriendsAssignedToHabitProfilePictures() throws Exception {
        Long habitId = 1L;

        mockMvc.perform(get(habitLink + "/{habitId}/friends/profile-pictures", habitId))
            .andExpect(status().isOk());

        verify(habitService).getFriendsAssignedToHabitProfilePictures(habitId, null);
    }

    @Test
    @SneakyThrows
    void updateCustomHabit() {
        Long habitId = 1L;
        byte[] imageContent = "sampleImageData".getBytes();
        MockMultipartFile imageFile = new MockMultipartFile("image", imageContent);
        CustomHabitDtoRequest dto = ModelUtils.getAddCustomHabitDtoRequest();

        objectMapper.findAndRegisterModules();

        String requestedJson = objectMapper.writeValueAsString(dto);
        MockMultipartFile jsonFile = new MockMultipartFile("request", "",
            "application/json", requestedJson.getBytes());

        MockMultipartHttpServletRequestBuilder builder =
            MockMvcRequestBuilders.multipart(habitLink + "/update/{habitId}", habitId);
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });
        mockMvc.perform(builder
            .file(jsonFile)
            .file(imageFile)
            .principal(principal)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk());

        verify(habitService).updateCustomHabit(dto, habitId, principal.getName(), imageFile);
    }

    @Test
    void deleteHabitAssignTest() throws Exception {
        Long customHabitId = 1L;
        mockMvc.perform(delete("/habit/delete/{customHabitId}", customHabitId)
            .principal(principal)).andExpect(status().isOk());

        verify(habitService).deleteCustomHabit(customHabitId, principal.getName());
    }

    @Test
    @SneakyThrows
    void likeTest() {
        Long habitId = 1L;

        UserVO userVO = new UserVO();

        mockMvc.perform(post(habitLink + "/like")
            .param("habitId", habitId.toString())
            .principal(getPrincipal()))
            .andExpect(status().isOk());

        verify(habitService).like(habitId, userVO);
    }

    @Test
    @SneakyThrows
    void dislikeTest() {
        Long habitId = 1L;

        UserVO userVO = new UserVO();

        mockMvc.perform(post(habitLink + "/dislike")
            .param("habitId", habitId.toString())
            .principal(getPrincipal()))
            .andExpect(status().isOk());

        verify(habitService).dislike(habitId, userVO);
    }

    @Test
    @SneakyThrows
    void addToFavoritesTest() {
        Long habitId = 1L;
        mockMvc.perform(post(habitLink + "/{habitId}/favorites", habitId)
            .principal(principal))
            .andExpect(status().isOk());
        verify(habitService).addToFavorites(habitId, principal.getName());
    }

    @Test
    @SneakyThrows
    void removeFromFavoritesTest() {
        Long habitId = 1L;
        mockMvc.perform(delete(habitLink + "/{habitId}/favorites", habitId)
            .principal(principal))
            .andExpect(status().isOk());
        verify(habitService).removeFromFavorites(habitId, principal.getName());
    }

    @Test
    @SneakyThrows
    void getAllFavoritesTest() {
        UserVO userVO = new UserVO();
        Pageable pageable = PageRequest.of(0, 10);
        String languageCode = "en";

        mockMvc.perform(get(habitLink + "/favorites")
            .principal(getPrincipal())
            .param("page", "0")
            .param("size", "10")
            .locale(Locale.forLanguageTag(languageCode)))
            .andExpect(status().isOk());

        verify(habitService).getAllFavoriteHabitsByLanguageCode(userVO, pageable, languageCode);
    }

    @Test
    @SneakyThrows
    void findAllFriendsOfUserToBeInvitedTest() {
        Long habitId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        UserVO userVO = new UserVO();

        mockMvc.perform(get(habitLink + "/friends")
            .principal(getPrincipal())
            .param("habitId", habitId.toString())
            .param("page", "0")
            .param("size", "10"))
            .andExpect(status().isOk());

        verify(habitService).findAllFriendsOfUser(userVO, null, pageable, habitId);
    }
}
