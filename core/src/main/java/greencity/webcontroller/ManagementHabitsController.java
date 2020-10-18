package greencity.webcontroller;

import greencity.annotations.ImageValidation;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableDto;
import greencity.dto.genericresponse.GenericResponseDto;
import greencity.dto.habit.HabitDto;
import greencity.entity.Habit;
import greencity.service.HabitService;
import greencity.service.LanguageService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

@Controller
@AllArgsConstructor
@RequestMapping("/management/habits")
public class ManagementHabitsController {
    private final HabitService habitService;
    private final LanguageService languageService;

    /**
     * Returns management page with all {@link Habit}'s.
     *
     * @param model    {@link Model} that will be configured and returned to user.
     * @param pageable {@link Pageable}.
     * @return View template path {@link String}.
     */
    @GetMapping
    public String findAllHabits(Model model, @ApiIgnore Pageable pageable) {
        PageableDto<HabitDto> allHabits = habitService.getAllHabitsDto(pageable);
        model.addAttribute("pageable", allHabits);
        model.addAttribute("languages", languageService.getAllLanguages());
        return "core/management_user_habits";
    }

    /**
     * Method saves {@link Habit} with translations.
     *
     * @param habitDto      {@link HabitDto}.
     * @param bindingResult {@link BindingResult}.
     * @param file          of {@link MultipartFile}.
     * @return {@link GenericResponseDto} with result of operation and errors fields.
     */
    @ApiOperation(value = "Save habit with translations.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = GenericResponseDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @ResponseBody
    @PostMapping("/save")
    public GenericResponseDto save(@Valid @RequestPart HabitDto habitDto,
                                   BindingResult bindingResult,
                                   @ImageValidation
                                   @RequestParam(required = false, name = "file") MultipartFile file) {
        if (!bindingResult.hasErrors()) {
            habitService.saveHabitAndTranslations(habitDto, file);
        }
        return GenericResponseDto.buildGenericResponseDto(bindingResult);
    }

    /**
     * Method updates {@link Habit} with translations.
     *
     * @param habitDto      {@link HabitDto}.
     * @param bindingResult {@link BindingResult}.
     * @param file          of {@link MultipartFile}.
     * @return {@link GenericResponseDto} with result of operation and errors fields.
     */
    @ApiOperation(value = "Update habit with translations.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GenericResponseDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @ResponseBody
    @PutMapping("/update")
    public GenericResponseDto update(@Valid @RequestPart HabitDto habitDto,
                                     BindingResult bindingResult,
                                     @ImageValidation
                                     @RequestParam(required = false, name = "file") MultipartFile file) {
        if (!bindingResult.hasErrors()) {
            habitService.update(habitDto, file);
        }
        return GenericResponseDto.buildGenericResponseDto(bindingResult);
    }

    /**
     * Method deletes {@link Habit} by id.
     *
     * @param id {@link HabitDto}'s id.
     * @return {@link ResponseEntity}.
     */
    @ApiOperation(value = "Delete habit by id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @DeleteMapping("/delete")
    public ResponseEntity<Long> delete(@RequestParam("id") Long id) {
        habitService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }

    /**
     * Method deletes all {@link Habit}'s by given id's.
     *
     * @param listId {@link List} of id's.
     * @return {@link ResponseEntity}.
     */
    @ApiOperation(value = "Delete all habits by given id's.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @DeleteMapping("/deleteAll")
    public ResponseEntity<List<Long>> deleteAll(@RequestBody List<Long> listId) {
        habitService.deleteAll(listId);
        return ResponseEntity.status(HttpStatus.OK).body(listId);
    }
}
