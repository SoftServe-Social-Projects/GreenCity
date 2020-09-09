package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableDto;
import greencity.dto.search.SearchNewsDto;
import greencity.dto.search.SearchResponseDto;
import greencity.dto.search.SearchTipsAndTricksDto;
import greencity.service.SearchService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/search")
@AllArgsConstructor
public class SearchController {
    private final SearchService searchService;

    /**
     * Method for search.
     *
     * @param searchQuery query to search.
     * @return list of {@link SearchResponseDto}.
     */
    @ApiOperation(value = "Search.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.OK),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("")
    public ResponseEntity<SearchResponseDto> search(
        @ApiParam(value = "Query to search") @RequestParam String searchQuery) {
        return ResponseEntity.status(HttpStatus.OK).body(searchService.search(searchQuery));
    }

    /**
     * Method for search.
     *
     * @param searchQuery query to search.
     * @return PageableDto of {@link SearchNewsDto} instances.
     */
    @ApiOperation(value = "Search Eco news.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.OK),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/econews")
    @ApiPageable
    public ResponseEntity<PageableDto<SearchNewsDto>> searchEcoNews(
        @ApiIgnore Pageable pageable,
        @ApiParam(value = "Query to search") @RequestParam String searchQuery) {
        return ResponseEntity.status(HttpStatus.OK).body(searchService.searchAllNews(pageable, searchQuery));
    }

    /**
     * Method for search.
     *
     * @param searchQuery query to search.
     * @return PageableDto of {@link SearchNewsDto} instances.
     */
    @ApiOperation(value = "Search Tips&Tricks.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.OK),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/tipsandtricks")
    @ApiPageable
    public ResponseEntity<PageableDto<SearchTipsAndTricksDto>> searchTipsAndTricks(
        @ApiIgnore Pageable pageable,
        @ApiParam(value = "Query to search") @RequestParam String searchQuery) {
        return ResponseEntity.status(HttpStatus.OK).body(searchService.searchAllTipsAndTricks(pageable, searchQuery));
    }
}
