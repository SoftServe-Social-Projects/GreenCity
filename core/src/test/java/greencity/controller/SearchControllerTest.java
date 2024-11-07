package greencity.controller;

import greencity.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {
    private MockMvc mockMvc;
    @Mock
    private SearchService searchService;
    @InjectMocks
    private SearchController searchController;
    @Mock
    private Validator mockValidator;

    private static final String mainSearchLink = "/search";
    private static final String ecoNewsSearchLinkPart = "/eco-news";
    private static final String eventsSearchLinkPart = "/events";
    private static final String placesSearchLinkPart = "/places";

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(searchController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setValidator(mockValidator)
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
}