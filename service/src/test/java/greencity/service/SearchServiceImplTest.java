package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.search.SearchEventsDto;
import greencity.dto.search.SearchNewsDto;
import greencity.dto.search.SearchPlacesDto;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class SearchServiceImplTest {
    @InjectMocks
    private SearchServiceImpl searchService;

    @Mock
    private EcoNewsService ecoNewsService;

    @Mock
    private EventService eventService;

    @Mock
    private PlaceService placeService;

    @Test
    void searchEcoNewsTest() {
        PageRequest pageRequest = PageRequest.of(0, 2);
        List<SearchNewsDto> searchDto =
            Arrays.asList(
                new SearchNewsDto(1L, "title", null),
                new SearchNewsDto(2L, "title", null));
        PageableDto<SearchNewsDto> pageableDto =
            new PageableDto<>(searchDto, searchDto.size(), 0, 1);

        when(ecoNewsService.search(pageRequest, "title", null, null)).thenReturn(pageableDto);

        List<SearchNewsDto> expected = pageableDto.getPage();
        List<SearchNewsDto> actual = searchService.searchAllNews(pageRequest, "title", null, null).getPage();

        assertEquals(expected, actual);
    }

    @Test
    void searchEventsTest() {
        PageRequest pageRequest = PageRequest.of(0, 2);
        List<SearchEventsDto> searchDto = List.of(
            new SearchEventsDto(1L, "title", null),
            new SearchEventsDto(2L, "title", null));
        PageableDto<SearchEventsDto> pageableDto = new PageableDto<>(searchDto, searchDto.size(), 0, 1);

        when(eventService.search(pageRequest, "title", null, null)).thenReturn(pageableDto);

        List<SearchEventsDto> expected = pageableDto.getPage();
        List<SearchEventsDto> actual = searchService.searchAllEvents(pageRequest, "title", null, null).getPage();

        assertEquals(expected, actual);
    }

    @Test
    void searchPlacesTest() {
        PageRequest pageRequest = PageRequest.of(0, 2);
        List<SearchPlacesDto> searchDto = List.of(
            new SearchPlacesDto(1L, "title", "category"),
            new SearchPlacesDto(2L, "title", "category"));
        PageableDto<SearchPlacesDto> pageableDto = new PageableDto<>(searchDto, searchDto.size(), 0, 1);

        when(placeService.search(pageRequest, "title", null, null)).thenReturn(pageableDto);

        List<SearchPlacesDto> expected = pageableDto.getPage();
        List<SearchPlacesDto> actual = searchService.searchAllPlaces(pageRequest, "title", null, null).getPage();

        assertEquals(expected, actual);
    }
}