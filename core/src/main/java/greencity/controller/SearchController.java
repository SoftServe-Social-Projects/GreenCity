package greencity.controller;

import greencity.annotations.ApiPageableWithLocale;
import greencity.annotations.ValidLanguage;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableDto;
import greencity.dto.search.SearchEventsDto;
import greencity.dto.search.SearchNewsDto;
import greencity.dto.search.SearchPlacesDto;
import greencity.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    /**
     * Method for searching eco news.
     *
     * @param searchQuery query to search.
     * @return PageableDto of {@link SearchNewsDto} instances.
     */
    @Operation(summary = "Search Eco News.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST)))
    })
    @GetMapping("/eco-news")
    @ApiPageableWithLocale
    public ResponseEntity<PageableDto<SearchNewsDto>> searchEcoNews(
        @Parameter(hidden = true) Pageable pageable,
        @Parameter(description = "Query to search") @RequestParam String searchQuery,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.ok().body(searchService.searchAllNews(pageable, searchQuery, locale.getLanguage()));
    }

    /**
     * Method for searching events.
     *
     * @param searchQuery query to search.
     * @return PageableDto of {@link SearchEventsDto} instances.
     */
    @Operation(summary = "Search Events.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST)))
    })
    @GetMapping("/events")
    @ApiPageableWithLocale
    public ResponseEntity<PageableDto<SearchEventsDto>> searchEvents(
        @Parameter(hidden = true) Pageable pageable,
        @Parameter(description = "Query to search") @RequestParam String searchQuery) {
        return ResponseEntity.ok().body(searchService.searchAllEvents(pageable, searchQuery));
    }

    /**
     * Method for searching places.
     *
     * @param searchQuery query to search.
     * @return PageableDto of {@link SearchPlacesDto} instances.
     */
    @Operation(summary = "Search Places.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST)))
    })
    @GetMapping("/places")
    @ApiPageableWithLocale
    public ResponseEntity<PageableDto<SearchPlacesDto>> searchPlaces(
        @Parameter(hidden = true) Pageable pageable,
        @Parameter(description = "Query to search") @RequestParam String searchQuery) {
        return ResponseEntity.ok().body(searchService.searchAllPlaces(pageable, searchQuery));
    }
}
