package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.ValidLanguage;
import greencity.constant.HttpStatuses;
import greencity.dto.habitfact.HabitFactDto;
import greencity.dto.habitfact.HabitFactPostDto;
import greencity.dto.habittranslation.HabitFactTranslationVO;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.entity.Habit;
import greencity.entity.HabitFactTranslation;
import greencity.entity.HabitFact;
import greencity.service.HabitFactTranslationService;
import greencity.service.HabitFactService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Locale;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import static greencity.constant.ErrorMessage.INVALID_HABIT_ID;

@RestController
@RequestMapping("/facts")
@AllArgsConstructor
public class HabitFactController {
    private final HabitFactService habitFactService;
    private final HabitFactTranslationService habitFactTranslationService;
    private final ModelMapper mapper;

    /**
     * The controller which returns random {@link HabitFact} by {@link Habit} id.
     *
     * @param habitId {@link Habit} id.
     * @return {@link HabitFactDto}.
     * @author Vitaliy Dzen
     */
    @ApiOperation("Get random habit fact by habit id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 400, message = INVALID_HABIT_ID),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/random/{habitId}")
    @ApiLocale
    public LanguageTranslationDTO getRandomFactByHabitId(
        @PathVariable Long habitId,
        @ApiIgnore @ValidLanguage Locale locale) {
        return habitFactService.getRandomHabitFactByHabitIdAndLanguage(habitId, locale.getLanguage());
    }

    /**
     * The controller which return today's {@link HabitFact} of the day.
     *
     * @param languageId id of language to display the {@link HabitFact}.
     * @return {@link LanguageTranslationDTO} of today's {@link HabitFact} of the day.
     */
    @ApiOperation("Get habit fact of the day")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/dayFact/{languageId}")
    public LanguageTranslationDTO getHabitFactOfTheDay(
        @PathVariable Long languageId
    ) {
        return habitFactTranslationService.getHabitFactOfTheDay(languageId);
    }


    /**
     * The controller which returns all {@link HabitFact}.
     *
     * @return List of {@link HabitFactDto}.
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
     * @param fact {@link HabitFactPostDto}.
     * @return {@link ResponseEntity}.
     * @author Vitaliy Dzen
     */
    @ApiOperation(value = "Save habit fact")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PostMapping
    public ResponseEntity<List<HabitFactTranslationVO>> save(@Valid @RequestBody HabitFactPostDto fact) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            habitFactTranslationService.saveHabitFactAndFactTranslation(fact));
    }

    /**
     * The controller which update {@link HabitFact}.
     *
     * @param dto    {@link HabitFactPostDto}.
     * @param factId of {@link HabitFact}.
     * @return {@link ResponseEntity}.
     * @author Vitaliy Dzen
     */
    @ApiOperation(value = "Update habit fact")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PutMapping("/{factId}")
    public ResponseEntity<HabitFactPostDto> update(
        @Valid @RequestBody HabitFactPostDto dto, @PathVariable Long factId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(mapper.map(habitFactService.update(dto, factId), HabitFactPostDto.class));
    }

    /**
     * The controller which delete {@link HabitFact}.
     *
     * @param factId of {@link HabitFact}.
     * @return {@link ResponseEntity}.
     * @author Vitaliy Dzen
     */
    @ApiOperation(value = "Delete habit fact")
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
