package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.CurrentUser;
import greencity.annotations.ValidLanguage;
import greencity.constant.HttpStatuses;
import greencity.dto.habit.HabitAssignVO;
import greencity.dto.habit.HabitVO;
import greencity.dto.habitstatistic.GetHabitStatisticDto;
import greencity.dto.habitstatistic.AddHabitStatisticDto;
import greencity.dto.habitstatistic.HabitItemsAmountStatisticDto;
import greencity.dto.habitstatistic.HabitStatisticDto;
import greencity.dto.habitstatistic.UpdateHabitStatisticDto;
import greencity.dto.user.UserVO;
import greencity.service.HabitStatisticService;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import java.util.List;
import java.util.Locale;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/habit/statistic")
public class HabitStatisticController {
    private final HabitStatisticService habitStatisticService;

    /**
     * Method for finding all {@link HabitStatisticDto} by {@link HabitVO id}.
     *
     * @param habitId {@link HabitVO} id.
     * @return list of {@link HabitStatisticDto} instances.
     */
    @Operation(summary = "Find all statistics by habit id.")
    @GetMapping("/{habitId}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = GetHabitStatisticDto.class))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    public ResponseEntity<GetHabitStatisticDto> findAllByHabitId(
        @PathVariable Long habitId) {
        return ResponseEntity.status(HttpStatus.OK).body(habitStatisticService.findAllStatsByHabitId(habitId));
    }

    /**
     * Method for finding {@link HabitStatisticDto} by {@link HabitAssignVO id}.
     *
     * @param habitAssignId {@link HabitAssignVO} id.
     * @return list of {@link HabitStatisticDto} instances.
     */
    @Operation(summary = "Find all statistics by habit assign id.")
    @GetMapping("/assign/{habitAssignId}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = HabitStatisticDto.class)))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    public ResponseEntity<List<HabitStatisticDto>> findAllStatsByHabitAssignId(
        @PathVariable Long habitAssignId) {
        return ResponseEntity.status(HttpStatus.OK).body(
            habitStatisticService.findAllStatsByHabitAssignId(habitAssignId));
    }

    /**
     * Method for creating {@link HabitStatisticDto} by {@link HabitVO} id that is
     * assigned for current user.
     *
     * @param addHabitStatisticDto dto for {@link HabitStatisticDto} entity.
     * @param userVO               {@link UserVO} instance.
     * @param habitId              {@link HabitVO} id.
     * @return dto {@link AddHabitStatisticDto} instance.
     * @author Yuriy Olkhovskyi.
     */
    @Operation(summary = "add statistic by habit id that is assigned for current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = HabitStatisticDto.class))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @PostMapping("/{habitId}")
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ResponseEntity<HabitStatisticDto> saveHabitStatistic(
        @Valid @RequestBody AddHabitStatisticDto addHabitStatisticDto,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @PathVariable Long habitId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(habitStatisticService.saveByHabitIdAndUserId(habitId, userVO.getId(), addHabitStatisticDto));
    }

    /**
     * Method for updating {@link HabitStatisticDto} by its id.
     *
     * @param id                         {@link HabitStatisticDto} id.
     * @param habitStatisticForUpdateDto {@link UpdateHabitStatisticDto} with habit
     *                                   statistic id and updated rate and amount of
     *                                   items.
     * @return {@link UpdateHabitStatisticDto} instance.
     */
    @Operation(summary = "Update habit statistic.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitStatisticDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<UpdateHabitStatisticDto> updateStatistic(
        @PathVariable Long id,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Valid @RequestBody UpdateHabitStatisticDto habitStatisticForUpdateDto) {
        return ResponseEntity.status(HttpStatus.OK).body(habitStatisticService
            .update(id, userVO.getId(), habitStatisticForUpdateDto));
    }

    /**
     * Returns statistics for all not taken habit items in the system for today.
     * Data is returned as an array of key-value-pairs mapped to
     * {@link HabitItemsAmountStatisticDto}, where key is the name of habit item and
     * value is not taken amount of these items. Language of habit items is defined
     * by the `language` parameter.
     *
     * @param locale - Name of habit item localization language(e.x. "en" or "ua").
     * @return {@link List} of {@link HabitItemsAmountStatisticDto}s contain those
     *         key-value pairs.
     */
    @Operation(summary = "Get today's statistic for all habit items.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = HabitItemsAmountStatisticDto.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST)))
    })
    @GetMapping("/todayStatisticsForAllHabitItems")
    @ApiLocale
    public ResponseEntity<List<HabitItemsAmountStatisticDto>> getTodayStatisticsForAllHabitItems(
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitStatisticService.getTodayStatisticsForAllHabitItems(locale.getLanguage()));
    }

    /**
     * Method for getting amount of acquired {@link HabitVO} by {@link UserVO} id.
     *
     * @param userId {@link UserVO} id.
     * @return amount of acquired habits by {@link UserVO} id.
     * @author Mamchuk Orest
     */
    @Operation(summary = "Get amount of acquired habit")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @GetMapping("acquired/count")
    public ResponseEntity<Long> findAmountOfAcquiredHabits(@RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitStatisticService.getAmountOfAcquiredHabitsByUserId(userId));
    }

    /**
     * Method for getting amount of in progress {@link HabitVO} by {@link UserVO}
     * id.
     *
     * @param userId {@link UserVO} id.
     * @return amount of acquired habits by {@link UserVO} id.
     * @author Mamchuk Orest
     */
    @Operation(summary = "Get amount of in progress habit")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @GetMapping("in-progress/count")
    public ResponseEntity<Long> findAmountOfHabitsInProgress(@RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitStatisticService.getAmountOfHabitsInProgressByUserId(userId));
    }
}
