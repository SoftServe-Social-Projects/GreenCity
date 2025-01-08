package greencity.webcontroller;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.ratingstatistics.RatingPointsDto;
import greencity.enums.Status;
import greencity.service.RatingPointsService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

class ManagementRatingCalculationControllerTest {
    @Mock
    private RatingPointsService ratingPointsService;

    @InjectMocks
    private ManagementRatingCalculationController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

    }

    @Test
    void getRatingPoints_ShouldReturnManagementPage_WhenQueryIsNull() throws Exception {
        Pageable pageable = PageRequest.of(0, 3);
        PageableAdvancedDto<RatingPointsDto> ratingPointsDtoPageableDto =
            new PageableAdvancedDto<>(Collections.singletonList(new RatingPointsDto()),
                3, 0, 3, 1, false, true, true, false);
        when(ratingPointsService.getAllRatingPointsByPage(pageable)).thenReturn(ratingPointsDtoPageableDto);

        this.mockMvc.perform(get("/management/rating/calculation")
            .param("page", "0")
            .param("size", "3"))
            .andExpect(view().name("core/management_rating_calculation"))
            .andExpect(model().attribute("ratings", ratingPointsDtoPageableDto))
            .andExpect(status().isOk());

        verify(ratingPointsService, times(1)).getAllRatingPointsByPage(pageable);
    }

    @Test
    void getRatingPoints_ShouldReturnManagementPage_WhenQueryIsProvided() throws Exception {
        Pageable pageable = PageRequest.of(0, 3);
        String query = "test";
        PageableAdvancedDto<RatingPointsDto> ratingPointsDtoPageableDto =
            new PageableAdvancedDto<>(Collections.singletonList(new RatingPointsDto()),
                3, 0, 3, 1, false, true, true, false);
        when(ratingPointsService.searchBy(pageable, query, Status.ACTIVE)).thenReturn(ratingPointsDtoPageableDto);

        this.mockMvc.perform(get("/management/rating/calculation")
            .param("page", "0")
            .param("size", "3")
            .param("query", query))
            .andExpect(view().name("core/management_rating_calculation"))
            .andExpect(model().attribute("ratings", ratingPointsDtoPageableDto))
            .andExpect(status().isOk());

        verify(ratingPointsService, times(1)).searchBy(pageable, query, Status.ACTIVE);
        verify(ratingPointsService, never()).getAllRatingPointsByPage(any(Pageable.class));
    }

    @Test
    void delete_ShouldReturnOk() throws Exception {
        doNothing().when(ratingPointsService).deleteRatingPoints(anyLong());

        mockMvc.perform(delete("/management/rating/calculation/{id}", 1))
            .andExpect(status().isOk());

        verify(ratingPointsService, times(1)).deleteRatingPoints(1L);
    }

    @Test
    void updateRatingPoints_ShouldReturnUpdatedRatingPoints() throws Exception {
        RatingPointsDto ratingPointsDto = new RatingPointsDto();
        when(ratingPointsService.updateRatingPoints(any(RatingPointsDto.class))).thenReturn(ratingPointsDto);

        mockMvc.perform(put("/management/rating/calculation")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{ \"name\": \"Updated Rating\" }"))
            .andExpect(status().isOk());

        verify(ratingPointsService, times(1)).updateRatingPoints(any(RatingPointsDto.class));
    }

    @Test
    void getDeletedRatingPoints_ShouldReturnDeletedRatingsPage_WhenQueryIsNull() throws Exception {
        Pageable pageable = PageRequest.of(0, 3);
        PageableAdvancedDto<RatingPointsDto> ratingPointsDtoPageableDto =
            new PageableAdvancedDto<>(Collections.singletonList(new RatingPointsDto()),
                3, 0, 3, 1, false, true, true, false);
        when(ratingPointsService.getDeletedRatingPoints(pageable)).thenReturn(ratingPointsDtoPageableDto);

        this.mockMvc.perform(get("/management/rating/calculation/deleted")
            .param("page", "0")
            .param("size", "3"))
            .andExpect(view().name("core/management_rating_deleted"))
            .andExpect(model().attribute("ratings", ratingPointsDtoPageableDto))
            .andExpect(status().isOk());

        verify(ratingPointsService, times(1)).getDeletedRatingPoints(pageable);
    }

    @Test
    void getDeletedRatingPoints_ShouldReturnDeletedRatingsPage_WhenQueryIsProvided() throws Exception {
        Pageable pageable = PageRequest.of(0, 3);
        String query = "test";
        PageableAdvancedDto<RatingPointsDto> ratingPointsDtoPageableDto =
            new PageableAdvancedDto<>(Collections.singletonList(new RatingPointsDto()),
                3, 0, 3, 1, false, true, true, false);
        when(ratingPointsService.searchBy(pageable, query, Status.DELETE)).thenReturn(ratingPointsDtoPageableDto);

        this.mockMvc.perform(get("/management/rating/calculation/deleted")
            .param("page", "0")
            .param("size", "3")
            .param("query", query))
            .andExpect(view().name("core/management_rating_deleted"))
            .andExpect(model().attribute("ratings", ratingPointsDtoPageableDto))
            .andExpect(status().isOk());

        verify(ratingPointsService, times(1)).searchBy(pageable, query, Status.DELETE);
        verify(ratingPointsService, never()).getDeletedRatingPoints(any(Pageable.class));
    }

    @Test
    void restoreDeletedRatingPoints_ShouldReturnOk() throws Exception {
        doNothing().when(ratingPointsService).restoreDeletedRatingPoints(anyLong());

        mockMvc.perform(put("/management/rating/calculation/restore/{id}", 1))
            .andExpect(status().isOk());

        verify(ratingPointsService, times(1)).restoreDeletedRatingPoints(1L);
    }
}