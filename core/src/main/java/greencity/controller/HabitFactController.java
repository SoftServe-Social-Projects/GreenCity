package greencity.controller;

import static greencity.constant.ErrorMessage.INVALID_HABIT_ID;

import greencity.constant.AppConstant;
import greencity.constant.HttpStatuses;
import greencity.dto.fact.HabitFactDTO;
import greencity.dto.fact.HabitFactPostDTO;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.entity.FactTranslation;
import greencity.entity.HabitFact;
import greencity.service.FactTranslationService;
import greencity.service.HabitFactService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/facts")
@AllArgsConstructor
public class HabitFactController {
    private HabitFactService habitFactService;
    private FactTranslationService factTranslationService;
    private ModelMapper mapper;

    /**
     * The controller which returns random {@link HabitFact} by HabitDictionary factId.
     *
     * @param habitId HabitDictionary
     * @return {@link HabitFactDTO}
     * @author Vitaliy Dzen
     */
    @ApiOperation("Get random fact by habit id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 400, message = INVALID_HABIT_ID),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/random/{habitId}")
    public LanguageTranslationDTO getRandomFactByHabitId(
        @PathVariable Long habitId,
        @ApiParam(value = "Code of the needed language.", defaultValue = AppConstant.DEFAULT_LANGUAGE_CODE)
        @RequestParam(required = false, defaultValue = AppConstant.DEFAULT_LANGUAGE_CODE) String language) {
        return habitFactService.getRandomHabitFactByHabitIdAndLanguage(habitId, language);
    }

    /**
     * The controller which returns all {@link HabitFact}.
     *
     * @return List of {@link HabitFactDTO}
     * @author Vitaliy Dzen
     */
    @ApiOperation("Get all facts")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping
    public List<LanguageTranslationDTO> getAll() {
        return habitFactService.getAllHabitFacts();
    }

    /**
     * The controller which save {@link HabitFact}.
     *
     * @param fact {@link HabitFactPostDTO}
     * @return {@link ResponseEntity}
     * @author Vitaliy Dzen
     */
    @ApiOperation(value = "Save fact")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PostMapping
    public ResponseEntity<List<FactTranslation>> save(@Valid @RequestBody HabitFactPostDTO fact) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            factTranslationService.saveHabitFactAndFactTranslation(fact));
    }

    /**
     * The controller which update {@link HabitFact}.
     *
     * @param dto    {@link HabitFactPostDTO}
     * @param factId of {@link HabitFact}
     * @return {@link ResponseEntity}
     * @author Vitaliy Dzen
     */
    @ApiOperation(value = "Update fact")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PutMapping("/{factId}")
    public ResponseEntity<HabitFactPostDTO> update(
        @Valid @RequestBody HabitFactPostDTO dto, @PathVariable Long factId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(mapper.map(habitFactService.update(dto, factId), HabitFactPostDTO.class));
    }

    /**
     * The controller which delete {@link HabitFact}.
     *
     * @param factId of {@link HabitFact}
     * @return {@link ResponseEntity}
     * @author Vitaliy Dzen
     */
    @ApiOperation(value = "Delete fact")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @DeleteMapping("/{factId}")
    public ResponseEntity<Object> delete(@PathVariable Long factId) {
        habitFactService.delete(factId);
        return ResponseEntity.ok().build();
    }
}
