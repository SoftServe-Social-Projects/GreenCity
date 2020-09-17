package greencity.controller;

import greencity.ModelUtils;
import greencity.dto.favoriteplace.FavoritePlaceDto;
import greencity.service.FavoritePlaceService;
import java.security.Principal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FavoritePlaceControllerTest {

    private MockMvc mockMvc;

    @Mock
    FavoritePlaceService favoritePlaceService;

    @Mock
    ModelMapper modelMapper;

    @InjectMocks
    FavoritePlaceController favoritePlaceController;

    private static final String favoritePlaceLink = "/favorite_place";
    private static final Principal principal = ModelUtils.getPrincipal();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(favoritePlaceController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    public void findAllByUserEmail() throws Exception {
        mockMvc.perform(get(favoritePlaceLink + "/")
            .principal(ModelUtils.getPrincipal())
        ).andExpect(status().isOk());

        verify(favoritePlaceService, times(1)).findAllByUserEmail(eq("test@gmail.com"));
    }

    @Test
    public void updateTest() throws Exception {
        FavoritePlaceDto favoritePlaceDto = ModelUtils.getFavoritePlaceDto();

        when(
            modelMapper.map(favoritePlaceService.update(favoritePlaceDto, principal.getName()), FavoritePlaceDto.class))
            .thenReturn(favoritePlaceDto);

        mockMvc.perform(put(favoritePlaceLink + "/")
            .content("{\n" +
                "  \"name\": \"string\",\n" +
                "  \"placeId\": 1\n" +
                "}")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        verify(favoritePlaceService, times(1)).update(favoritePlaceDto, principal.getName());
    }

    @Test
    public void deleteByUserEmailAndPlaceId() throws Exception {
        mockMvc.perform(delete(favoritePlaceLink + "/{placeId}", 1)
            .principal(principal))
            .andExpect(status().isOk());

        verify(favoritePlaceService, times(1))
            .deleteByUserEmailAndPlaceId(1L, principal.getName());
    }

    @Test
    public void getFavoritePlaceWithCoordinate() throws Exception {
        mockMvc.perform(get(favoritePlaceLink + "/favorite/{placeId}", 1)
            .principal(principal))
            .andExpect(status().isOk());

        verify(favoritePlaceService, times(1)).getFavoritePlaceWithLocation(1L, principal.getName());
    }
}