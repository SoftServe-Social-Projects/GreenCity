package greencity.controller;

import greencity.converters.UserArgumentResolver;
import greencity.exception.exceptions.BadRequestException;
import greencity.service.SearchService;
import greencity.service.UserService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {
    private MockMvc mockMvc;
    @Mock
    private SearchService searchService;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private SearchController searchController;

    private static final String mainSearchLink = "/search";
    private static final String ecoNewsSearchLinkPart = "/eco-news";
    private static final String eventsSearchLinkPart = "/events";
    private static final String placesSearchLinkPart = "/places";

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(searchController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .build();
    }

    @Test
    void searchEcoNewsTest() throws Exception {
        mockMvc.perform(get(mainSearchLink +
            ecoNewsSearchLinkPart + "?searchQuery={query}", "Eco news title")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void searchEventsTest() throws Exception {
        mockMvc.perform(get(mainSearchLink +
            eventsSearchLinkPart + "?searchQuery={query}", "Events title")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void searchPlacesTest() throws Exception {
        mockMvc.perform(get(mainSearchLink +
            placesSearchLinkPart + "?searchQuery={query}", "Places title")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void searchPlacesThrowExceptionTest() {
        ServletException exception = assertThrows(ServletException.class, () -> {
            mockMvc.perform(get(mainSearchLink +
                placesSearchLinkPart + "?searchQuery={query}&isFavorite=true", "Places title")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        });

        assertInstanceOf(BadRequestException.class, exception.getCause());
        assertEquals("isFavorite param require authenticated user", exception.getCause().getMessage());
    }
}