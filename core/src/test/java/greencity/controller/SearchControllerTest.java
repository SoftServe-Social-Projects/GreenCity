package greencity.controller;

import greencity.service.EcoNewsService;
import greencity.service.SearchService;
import greencity.service.TipsAndTricksService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    private MockMvc mockMvc;

    @Mock
    TipsAndTricksService tipsAndTricksService;
    @Mock
    EcoNewsService ecoNewsService;
    @Mock
    SearchService searchService;
    @InjectMocks
    SearchController searchController;

    private static final String mainSearchLink = "/search";
    private static final String ecoNewsSearchLinkPart = "/econews";
    private static final String tipsAndTricksLinkPart = "/tipsandtricks";

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(searchController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void searchEverythingTest() throws Exception {
        mockMvc.perform(get(mainSearchLink + "?searchQuery={query}", "Title")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void searchEcoNewsTest() throws Exception {
        mockMvc.perform(get(mainSearchLink +
            ecoNewsSearchLinkPart + "?searchQuery={query}", "Eco news title")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void searchTipsAndTricksTest() throws Exception {
        mockMvc.perform(get(mainSearchLink +
            tipsAndTricksLinkPart + "?searchQuery={query}", "Tips and tricks title")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

}