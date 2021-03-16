package greencity.controller;

import static greencity.constant.ErrorMessage.INVALID_HABIT_ID;

import greencity.annotations.ApiLocale;
import greencity.annotations.ApiPageableWithLocale;
import greencity.annotations.ValidLanguage;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableDto;
import greencity.dto.habit.HabitVO;
import greencity.dto.habitfact.*;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.service.HabitFactService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import javax.validation.Valid;
import java.util.Locale;

@RestController
@RequestMapping("/facts")
@AllArgsConstructor
public class HabitFactController {
    private final HabitFactService habitFactService;
    private final ModelMapper mapper;

    /**
     * The controller which returns random {@link HabitFactVO} by {@link HabitVO}
     * id.
     *
     * @param habitId {@link HabitVO} id.
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
     * The controller which return today's {@link HabitFactVO} of the day.
     *
     * @param languageId id of language to display the {@link HabitFactVO}.
     * @return {@link LanguageTranslationDTO} of today's {@link HabitFactVO} of the
     *         day.
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
        @PathVariable Long languageId) {
        return habitFactService.getHabitFactOfTheDay(languageId);
    }

    /**
     * The controller which returns all {@link HabitFactVO}.
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
    @ApiPageableWithLocale
    public ResponseEntity<PageableDto<LanguageTranslationDTO>> getAll(@ApiIgnore Pageable page,
        @ApiIgnore @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitFactService.getAllHabitFacts(page, locale.getLanguage()));
    }

    /**
     * The controller which save {@link HabitFactVO}.
     *
     * @param fact {@link HabitFactPostDto}.
     * @return {@link ResponseEntity}.
     * @author Vitaliy Dzen
     */
    @ApiOperation(value = "Save habit fact")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PostMapping
    public ResponseEntity<HabitFactDtoResponse> save(@Valid @RequestBody HabitFactPostDto fact) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            mapper.map(habitFactService.save(fact), HabitFactDtoResponse.class));
    }

    /**
     * The controller which update {@link HabitFactVO}.
     *
     * @param dto {@link HabitFactPostDto}.
     * @param id  of {@link HabitFactVO}.
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
    @PutMapping("/{id}")
    public ResponseEntity<HabitFactPostDto> update(
        @Valid @RequestBody HabitFactUpdateDto dto, @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(mapper.map(habitFactService.update(dto, id), HabitFactPostDto.class));
    }

    /**
     * The controller which delete {@link HabitFactVO}.
     *
     * @param id of {@link HabitFactVO}.
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        habitFactService.delete(id);
        return ResponseEntity.ok().build();
    }
}
