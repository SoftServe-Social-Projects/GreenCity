package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.ValidLanguage;
import greencity.constant.HttpStatuses;
import greencity.dto.tag.NewTagDto;
import greencity.dto.tag.TagDto;
import greencity.enums.TagType;
import greencity.service.TagsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Locale;

@RestController
@AllArgsConstructor
@RequestMapping("/tags")
public class TagsController {
    private final TagsService tagsService;

    /**
     * The method which returns all tags' names by type and language code.
     *
     * @param locale {@link Locale}
     * @param type   {@link TagType}
     * @return list of {@link TagDto} (tag's names).
     * @author Markiyan Derevetskyi
     */
    @Operation(summary = "Find all tags by type and language code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK)
    })
    @GetMapping("/search")
    @ApiLocale
    public ResponseEntity<List<TagDto>> findByTypeAndLanguageCode(
        @Parameter(hidden = true) @ValidLanguage Locale locale,
        @RequestParam TagType type) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(tagsService.findByTypeAndLanguageCode(type, locale.getLanguage()));
    }

    /**
     * The method which returns all tags' names by type and language code.
     *
     * @param type {@link TagType}
     * @return list of {@link NewTagDto} (tag's names).
     */
    @Operation(summary = "Find all tags by type and language code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK)
    })
    @GetMapping("/v2/search")
    public ResponseEntity<List<NewTagDto>> findByType(@RequestParam TagType type) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(tagsService.findByType(type));
    }
}
