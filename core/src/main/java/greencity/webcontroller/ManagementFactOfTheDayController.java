package greencity.webcontroller;

import static greencity.dto.genericresponse.GenericResponseDto.buildGenericResponseDto;
import greencity.annotations.ApiPageable;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableDto;
import greencity.dto.factoftheday.FactOfTheDayDTO;
import greencity.dto.factoftheday.FactOfTheDayPostDTO;
import greencity.dto.factoftheday.FactOfTheDayTranslationVO;
import greencity.dto.factoftheday.FactOfTheDayVO;
import greencity.dto.genericresponse.GenericResponseDto;
import greencity.dto.tag.TagDto;
import greencity.service.FactOfTheDayService;
import greencity.service.LanguageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PutMapping;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/management/factoftheday")
@RequiredArgsConstructor
public class ManagementFactOfTheDayController {
    private final FactOfTheDayService factOfTheDayService;
    private final LanguageService languageService;

    /**
     * Returns management page with all facts of the day.
     *
     * @param model ModelAndView that will be configured and returned to user
     * @return model
     */
    @ApiPageable
    @Operation(summary = "Get management page with facts of the day.")
    @GetMapping("")
    public String getAllFacts(Model model, @Parameter(hidden = true) Pageable pageable) {
        PageableDto<FactOfTheDayDTO> allFactsOfTheDay = factOfTheDayService.getAllFactsOfTheDay(pageable);
        model.addAttribute("pageable", allFactsOfTheDay);
        model.addAttribute("languages", languageService.getAllLanguages());
        return "core/management_fact_of_the_day";
    }

    /**
     * Returns management page with facts of the day that satisfy query.
     *
     * @param query string to search
     * @return model
     */
    @ApiPageable
    @Operation(summary = "Get management page with facts of the day that satisfy query.")
    @GetMapping("/findAll")
    public String findAll(@RequestParam(required = false, name = "query") String query,
        Model model, @Parameter(hidden = true) Pageable pageable) {
        PageableDto<FactOfTheDayDTO> pageableDto = query == null || query.isEmpty()
            ? factOfTheDayService.getAllFactsOfTheDay(pageable)
            : factOfTheDayService.searchBy(pageable, query);
        model.addAttribute("pageable", pageableDto);
        model.addAttribute("languages", languageService.getAllLanguages());
        model.addAttribute("query", query);
        return "core/management_fact_of_the_day";
    }

    /**
     * Method which saves {@link FactOfTheDayVO}.
     *
     * @param factOfTheDayPostDTO of {@link FactOfTheDayPostDTO}
     * @return {@link GenericResponseDto} with of operation and errors fields
     */
    @Operation(summary = "Save fact of the day")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = GenericResponseDto.class))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @ResponseBody
    @PostMapping("/")
    public GenericResponseDto saveFactOfTheDay(@Valid @RequestBody FactOfTheDayPostDTO factOfTheDayPostDTO,
        BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            factOfTheDayService.saveFactOfTheDayAndTranslations(factOfTheDayPostDTO);
        }
        return buildGenericResponseDto(bindingResult);
    }

    /**
     * Method which updates {@link FactOfTheDayVO}.
     *
     * @param factOfTheDayPostDTO of {@link FactOfTheDayPostDTO}
     * @return {@link GenericResponseDto} with of operation and errors fields
     */
    @Operation(summary = "Update fact of the day")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @ResponseBody
    @PutMapping("/")
    public GenericResponseDto updateFactOfTheDay(@Valid @RequestBody FactOfTheDayPostDTO factOfTheDayPostDTO,
        BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            factOfTheDayService.updateFactOfTheDayAndTranslations(factOfTheDayPostDTO);
        }
        return buildGenericResponseDto(bindingResult);
    }

    /**
     * Method which deteles {@link FactOfTheDayVO} and
     * {@link FactOfTheDayTranslationVO} by given id.
     *
     * @param id of Fact of the day
     * @return {@link ResponseEntity}
     */
    @Operation(summary = "Delete Fact of the day")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @DeleteMapping("/")
    public ResponseEntity<Long> delete(@RequestParam("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(factOfTheDayService.deleteFactOfTheDayAndTranslations(id));
    }

    /**
     * Method which deteles {@link FactOfTheDayVO} and
     * {@link FactOfTheDayTranslationVO} by given id.
     *
     * @param listId list of IDs
     * @return {@link ResponseEntity}
     */
    @Operation(summary = "Get all Fact of the day by given IDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @DeleteMapping("/deleteAll")
    public ResponseEntity<List<Long>> deleteAll(@RequestBody List<Long> listId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(factOfTheDayService.deleteAllFactOfTheDayAndTranslations(listId));
    }

    /**
     * Retrieves all tags related to Facts of the Day.
     *
     * @return {@link ResponseEntity} containing a set of {@link TagDto}.
     */
    @Operation(summary = "Retrieve all tags associated with Facts of the Day.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/tags")
    public ResponseEntity<Set<TagDto>> getAllFactOfTheDayTags() {
        return ResponseEntity.ok(factOfTheDayService.getAllFactOfTheDayTags());
    }
}
