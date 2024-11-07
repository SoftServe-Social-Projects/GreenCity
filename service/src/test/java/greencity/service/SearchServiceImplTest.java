package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.search.SearchNewsDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

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

    @Mock
    private ModelMapper modelMapper;

    @Test
    void searchEcoNewsTest() {
        PageRequest pageRequest = PageRequest.of(0, 2);
        List<SearchNewsDto> searchDto =
            Arrays.asList(
                new SearchNewsDto(1L, "title", null),
                new SearchNewsDto(2L, "title", null));
        PageableDto<SearchNewsDto> pageableDto =
            new PageableDto<>(searchDto, searchDto.size(), 0, 1);

        when(ecoNewsService.search(pageRequest, "title", "en")).thenReturn(pageableDto);

        List<SearchNewsDto> expected = pageableDto.getPage();
        List<SearchNewsDto> actual = searchService.searchAllNews(pageRequest, "title", "en").getPage();

        assertEquals(expected, actual);
    }
}