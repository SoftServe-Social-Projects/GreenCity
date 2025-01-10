package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.CurrentUser;
import greencity.annotations.ValidLanguage;
import greencity.constant.AppConstant;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.habit.HabitAssignCustomPropertiesDto;
import greencity.dto.habit.HabitAssignDto;
import greencity.dto.habit.HabitAssignManagementDto;
import greencity.dto.habit.HabitAssignStatDto;
import greencity.dto.habit.HabitAssignUserDurationDto;
import greencity.dto.habit.HabitAssignVO;
import greencity.dto.habit.HabitDto;
import greencity.dto.habit.HabitVO;
import greencity.dto.habit.HabitsDateEnrollmentDto;
import greencity.dto.habit.HabitAssignPreviewDto;
import greencity.dto.habit.UserToDoAndCustomToDoListsDto;
import greencity.dto.habit.HabitWorkingDaysDto;
import greencity.dto.habitstatuscalendar.HabitStatusCalendarDto;
import greencity.dto.user.UserVO;
import greencity.service.HabitAssignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/habit/assign")
public class HabitAssignController {
    private final HabitAssignService habitAssignService;

    @Value("${client.address}")
    private String redirectUrl;

    /**
     * Method which assigns habit for {@link UserVO} with default props.
     *
     * @param habitId {@link HabitVO} id.
     * @param userVO  {@link UserVO} instance.
     * @return {@link ResponseEntity}.
     */
    @Operation(summary = "Assign habit with default properties for current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = HabitAssignManagementDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @PostMapping("/{habitId}")
    public ResponseEntity<HabitAssignManagementDto> assignDefault(@PathVariable Long habitId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(habitAssignService.assignDefaultHabitForUser(habitId, userVO));
    }

    /**
     * Method which assigns habit for {@link UserVO} with custom props.
     *
     * @param habitId                        {@link HabitVO} id.
     * @param userVO                         {@link UserVO} instance.
     * @param habitAssignCustomPropertiesDto {@link HabitAssignCustomPropertiesDto}
     *                                       instance.
     * @return {@link ResponseEntity}.
     */
    @Operation(summary = "Assign habit with custom properties for current user and his friends.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED, content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = HabitAssignManagementDto.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @PostMapping("/{habitId}/custom")
    public ResponseEntity<List<HabitAssignManagementDto>> assignCustom(@PathVariable Long habitId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Valid @RequestBody HabitAssignCustomPropertiesDto habitAssignCustomPropertiesDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(habitAssignService.assignCustomHabitForUser(habitId, userVO, habitAssignCustomPropertiesDto));
    }

    /**
     * Method that update duration of HabitAssign and HabitAssignStatus from
     * REQUESTED to INPROGRESS.
     *
     * @param habitAssignId {@link HabitAssignVO} id.
     * @param userVO        {@link UserVO} instance.
     * @param duration      {@link Integer} with needed duration.
     */
    @Operation(summary = "Update duration of HabitAssign and HabitAssignStatus")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @ApiLocale
    @PutMapping("{habitAssignId}/update-status-and-duration")
    public ResponseEntity<HabitAssignUserDurationDto> updateStatusAndDurationOfHabitAssign(
        @PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @RequestParam @Min(AppConstant.MIN_DAYS_DURATION) @Max(AppConstant.MAX_DAYS_DURATION) Integer duration) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService.updateStatusAndDurationOfHabitAssign(habitAssignId, userVO.getId(), duration));
    }

    /**
     * Method which updates duration of habit assigned for user.
     *
     * @param habitAssignId {@link HabitVO} id.
     * @param userVO        {@link UserVO} instance.
     * @param duration      {@link Integer} with needed duration.
     * @return {@link ResponseEntity}.
     */
    @PutMapping("/{habitAssignId}/update-habit-duration")
    @Operation(summary = "Update duration of habit with habitAssignId for user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitAssignUserDurationDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    public ResponseEntity<HabitAssignUserDurationDto> updateHabitAssignDuration(
        @PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @RequestParam @Min(AppConstant.MIN_DAYS_DURATION) @Max(AppConstant.MAX_DAYS_DURATION) Integer duration) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService.updateUserHabitInfoDuration(habitAssignId, userVO.getId(), duration));
    }

    /**
     * Method returns {@link HabitAssignDto} by its id, current user id and specific
     * language.
     *
     * @param habitAssignId {@link HabitAssignVO} id.
     * @param userVO        {@link UserVO}.
     * @param locale        needed language code.
     * @return {@link HabitAssignDto}.
     */
    @Operation(summary = "Get habit assign.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitAssignDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @ApiLocale
    @GetMapping("/{habitAssignId}")
    public ResponseEntity<HabitAssignDto> getHabitAssign(@PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO, @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService.getByHabitAssignIdAndUserId(habitAssignId, userVO.getId(), locale.getLanguage()));
    }

    /**
     * Method for finding all inprogress, acquired {@link HabitAssignDto}'s for
     * current user.
     *
     * @param userVO {@link UserVO} instance.
     * @param locale needed language code.
     * @return list of {@link HabitAssignDto}.
     */
    @Operation(summary = "Get (inprogress, acquired) assigned habits for current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = HabitAssignDto.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @ApiLocale
    @GetMapping("/allForCurrentUser")
    public ResponseEntity<List<HabitAssignDto>> getCurrentUserHabitAssignsByIdAndAcquired(
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService
                .getAllHabitAssignsByUserIdAndStatusNotCancelled(userVO.getId(), locale.getLanguage()));
    }

    /**
     * Method for finding all inprogress, acquired {@link HabitAssignDto}'s for user
     * by id.
     *
     * @param userId   the {@code User} id of the other user to find habit
     *                 assignments with.
     * @param pageable the {@link Pageable} object for pagination information.
     * @return list of {@link HabitAssignDto}.
     */
    @Operation(summary = "Get (inprogress, acquired) assigned habits for user by id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = HabitAssignDto.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @ApiLocale
    @GetMapping("/allUser/{userId}")
    public ResponseEntity<PageableAdvancedDto<HabitAssignPreviewDto>> getUserHabitAssignsByIdAndAcquired(
        @PathVariable Long userId,
        @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService
                .getAllByUserIdAndStatusNotCancelled(userId, pageable));
    }

    /**
     * Finds all mutual in-progress and acquired {@link HabitAssignPreviewDto} for
     * the current user and another specified user, with pagination.
     *
     * @param userId   the {@code User} id of the other user to find mutual habit
     *                 assignments with.
     * @param userVO   {@link UserVO} instance representing the current user.
     * @param pageable the {@link Pageable} object for pagination information.
     * @return a {@link ResponseEntity} containing a {@link PageableAdvancedDto}
     *         with a list of {@link HabitAssignPreviewDto} representing the found
     *         mutual habit assignments and pagination information.
     */
    @Operation(summary = "Get all mutual (inprogress, acquired) assigned habits for current user with another user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @GetMapping("/allMutualHabits/{userId}")
    public ResponseEntity<PageableAdvancedDto<HabitAssignPreviewDto>> getAllMutualHabitsWithUser(
        @PathVariable Long userId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService
                .getAllMutualHabitAssignsWithUserAndStatusNotCancelled(userId, userVO.getId(), pageable));
    }

    /**
     * Finds all mutual in-progress and acquired {@link HabitAssignPreviewDto} for
     * user made by current user, with pagination.
     *
     * @param userId   the {@code User} id of the other user to find habit
     *                 assignments with.
     * @param userVO   {@link UserVO} instance representing the current user.
     * @param pageable the {@link Pageable} object for pagination information.
     * @return a {@link ResponseEntity} containing a {@link PageableAdvancedDto}
     *         with a list of {@link HabitAssignPreviewDto} representing the found
     *         assignments and pagination information.
     */
    @Operation(summary = "Get all (inprogress, acquired) assigned habits for user made by current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @GetMapping("/myHabits/{userId}")
    public ResponseEntity<PageableAdvancedDto<HabitAssignPreviewDto>> getMyHabitsOfCurrentUser(
        @PathVariable Long userId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService
                .getMyHabitsOfCurrentUserAndStatusNotCancelled(userId, userVO.getId(), pageable));
    }

    /**
     * Method that return UserToDoList and CustomToDoList.
     *
     * @param habitAssignId {@link HabitAssignVO} id.
     * @param userVO        {@link UserVO} instance.
     * @param locale        needed language code.
     * @return User To-Dog List and Custom To-Do List.
     */
    @Operation(summary = "Get user to-do and custom to-do lists by habitAssignId")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = UserToDoAndCustomToDoListsDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @ApiLocale
    @GetMapping("{habitAssignId}/allUserAndCustomList")
    public ResponseEntity<UserToDoAndCustomToDoListsDto> getUserToDoAndCustomToDoLists(
        @PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService
                .getUserToDoAndCustomToDoLists(userVO.getId(), habitAssignId, locale.getLanguage()));
    }

    /**
     * Method that update UserToDoList and CustomToDo List.
     *
     * @param habitAssignId {@link HabitAssignVO} id.
     * @param userVO        {@link UserVO} instance.
     * @param locale        needed language code.
     * @param listsDto      {@link UserToDoAndCustomToDoListsDto} instance.
     */
    @Operation(summary = "Update user and custom to-do lists",
        description = """
            If the item is already present in the db, the method updates it
            If item is not present in the db and id is null, the method attempts to add it to the user
            If some items from db are not present in the lists,
            the method deletes them (except for items with DISABLED status).""")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @ApiLocale
    @PutMapping("{habitAssignId}/allUserAndCustomList")
    public ResponseEntity<ResponseEntity.BodyBuilder> updateUserAndCustomToDoLists(
        @PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) @ValidLanguage Locale locale,
        @Valid @RequestBody UserToDoAndCustomToDoListsDto listsDto) {
        habitAssignService.fullUpdateUserAndCustomToDoLists(userVO.getId(), habitAssignId, listsDto,
            locale.getLanguage());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method that return list of UserToDoLists and CustomToDoLists for current
     * user, specific language and INPROGRESS status.
     *
     * @param userVO {@link UserVO} instance.
     * @param locale needed language code.
     * @return List of User To-Do Lists and Custom To-Do Lists.
     */
    @Operation(summary = "Get list of user to-do list items and custom to-do list items with status INPROGRESS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = UserToDoAndCustomToDoListsDto.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @ApiLocale
    @GetMapping("/allUserAndCustomToDoListsInprogress")
    public ResponseEntity<List<UserToDoAndCustomToDoListsDto>> getListOfUserAndCustomToDoListsInprogress(
        @Parameter(hidden = true) @CurrentUser UserVO userVO, @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService
                .getListOfUserAndCustomToDoListsWithStatusInprogress(userVO.getId(), locale.getLanguage()));
    }

    /**
     * Method to return all inprogress, acquired {@link HabitAssignDto} by it's
     * {@link HabitVO} id.
     *
     * @param habitId {@link HabitVO} id.
     * @param locale  needed language code.
     * @return {@link List} of {@link HabitAssignDto}.
     */
    @Operation(summary = "Get all inprogress, acquired assigns by certain habit.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = HabitAssignDto.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @ApiLocale
    @GetMapping("/{habitId}/all")
    public ResponseEntity<List<HabitAssignDto>> getAllHabitAssignsByHabitIdAndAcquired(@PathVariable Long habitId,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService.getAllHabitAssignsByHabitIdAndStatusNotCancelled(habitId,
                locale.getLanguage()));
    }

    /**
     * Method to return {@link HabitAssignVO} by it's {@link HabitVO} id.
     *
     * @param habitId {@link HabitVO} id.
     * @param userVO  {@link UserVO} user.
     * @param locale  needed language code.
     * @return {@link HabitAssignDto} instance.
     */
    @Operation(summary = "Get inprogress or acquired assign by habit id for current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitAssignDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @ApiLocale
    @GetMapping("/{habitId}/active")
    public ResponseEntity<HabitAssignDto> getHabitAssignByHabitId(
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @PathVariable Long habitId,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService
                .findHabitAssignByUserIdAndHabitId(userVO.getId(), habitId, locale.getLanguage()));
    }

    /**
     * Method to return {@link HabitDto} with more it's information by
     * {@link HabitAssignVO} id.
     *
     * @param habitAssignId {@link HabitAssignVO} id.
     * @param userVO        {@link UserVO} user.
     * @param locale        needed language code.
     * @return {@link HabitDto} instance.
     */
    @Operation(summary = "Get habit assign by habit assign id for current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @ApiLocale
    @GetMapping("/{habitAssignId}/more")
    public ResponseEntity<HabitDto> getUsersHabitByHabitAssignId(
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @PathVariable Long habitAssignId,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService
                .findHabitByUserIdAndHabitAssignId(userVO.getId(), habitAssignId, locale.getLanguage()));
    }

    /**
     * Method to update inprogress, acquired {@link HabitAssignVO} for it's
     * {@link HabitVO} id and current user.
     *
     * @param habitAssignId      {@link HabitAssignVO} id.
     * @param habitAssignStatDto {@link HabitAssignStatDto} instance.
     * @return {@link HabitAssignManagementDto}.
     */
    @Operation(summary = "Update inprogress, acquired user habit assign acquired or cancelled status.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitAssignDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PatchMapping("/{habitAssignId}")
    public ResponseEntity<HabitAssignManagementDto> updateAssignByHabitId(
        @PathVariable Long habitAssignId, @Valid @RequestBody HabitAssignStatDto habitAssignStatDto) {
        return ResponseEntity.status(HttpStatus.OK).body(habitAssignService
            .updateStatusByHabitAssignId(habitAssignId, habitAssignStatDto));
    }

    /**
     * Method to enroll {@link HabitAssignVO} for current date.
     *
     * @param habitAssignId - id of {@link HabitAssignVO}.
     * @param userVO        {@link UserVO} user.
     * @param date          - {@link LocalDate} we want to enroll.
     * @param locale        - needed language code.
     * @return {@link HabitStatusCalendarDto}.
     */
    @Operation(summary = "Enroll habit assign by habitAssignId for current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitAssignDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @ApiLocale
    @PostMapping("/{habitAssignId}/enroll/{date}")
    public ResponseEntity<HabitAssignDto> enrollHabit(@PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @PathVariable(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService.enrollHabit(habitAssignId, userVO.getId(), date, locale.getLanguage()));
    }

    /**
     * Method to unenroll {@link HabitAssignVO} for defined date.
     *
     * @param habitAssignId - id of {@link HabitAssignVO}.
     * @param userVO        {@link UserVO} user.
     * @param date          - {@link LocalDate} we want to unenroll.
     * @return {@link HabitAssignDto} instance.
     */
    @Operation(summary = "Unenroll assigned habit for a specific day.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitAssignDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/{habitAssignId}/unenroll/{date}")
    public ResponseEntity<HabitAssignDto> unenrollHabit(@PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @PathVariable(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService.unenrollHabit(habitAssignId, userVO.getId(), date));
    }

    /**
     * Method to find all inprogress {@link HabitAssignVO} on certain
     * {@link LocalDate}.
     *
     * @param userVO {@link UserVO} user.
     * @param date   {@link LocalDate} date to check if there is in progress
     *               assigns.
     * @param locale needed language code.
     * @return {@link HabitAssignDto} instance.
     */
    @Operation(summary = "Get inprogress user habit assigns on certain date.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = HabitAssignDto.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
    })
    @ApiLocale
    @GetMapping("/active/{date}")
    public ResponseEntity<List<HabitAssignDto>> getInprogressHabitAssignOnDate(
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @PathVariable(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService
                .findInprogressHabitAssignsOnDate(userVO.getId(), date, locale.getLanguage()));
    }

    /**
     * Method to find all user inprogress activities {@link HabitsDateEnrollmentDto}
     * between the specified {@link LocalDate}s.
     *
     * @param userVO {@link UserVO} user.
     * @param from   The start {@link LocalDate} to retrieve from
     * @param to     The end {@link LocalDate} to retrieve to
     * @param locale needed language code.
     * @return {@link HabitsDateEnrollmentDto} instance.
     */
    @Operation(summary = "Get user inprogress activities between the specified dates.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = HabitsDateEnrollmentDto.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
    })
    @ApiLocale
    @GetMapping("/activity/{from}/to/{to}")
    public ResponseEntity<List<HabitsDateEnrollmentDto>> getHabitAssignBetweenDates(
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @PathVariable(value = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @PathVariable(value = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService
                .findHabitAssignsBetweenDates(userVO.getId(), from, to, locale.getLanguage()));
    }

    /**
     * Method delete habit assign {@link HabitAssignVO} for current {@link UserVO}
     * by habitAssignId.
     *
     * @param habitAssignId - id of {@link HabitAssignVO}.
     * @param userVO        - {@link UserVO} user.
     */
    @Operation(summary = "Delete habit assign by habitAssignId for current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @DeleteMapping("/delete/{habitAssignId}")
    public ResponseEntity<ResponseEntity.BodyBuilder> deleteHabitAssign(@PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        habitAssignService.deleteHabitAssign(habitAssignId, userVO.getId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method updates value progressNotificationHasDisplayed in HabitAssign to true
     * {@link Boolean}.
     *
     * @param habitAssignId {@link HabitAssignVO} id.
     * @param userVO        {@link UserVO}.
     */
    @Operation(summary = "Update value progressNotificationHasDisplayed to true.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PutMapping("{habitAssignId}/updateProgressNotificationHasDisplayed")
    public ResponseEntity<ResponseEntity.BodyBuilder> updateProgressNotificationHasDisplayed(
        @PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        habitAssignService.updateProgressNotificationHasDisplayed(habitAssignId, userVO.getId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method send request to assign on habit via email notification.
     *
     * @param habitId    - id of {@link HabitAssignVO}.
     * @param friendsIds - list of ids of user friends {@link UserVO} to invite.
     * @param userVO     - user who send request {@link UserVO}.
     * @param locale     - current language
     *                   {@link greencity.dto.language.LanguageVO}.
     */
    @Operation(summary = "Inviting friends on habit with email notification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/{habitId}/invite")
    public ResponseEntity<ResponseEntity.BodyBuilder> inviteFriendRequest(@PathVariable Long habitId,
        @Parameter(description = "List of friends ids to invite") @RequestParam List<Long> friendsIds,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        habitAssignService.inviteFriendForYourHabitWithEmailNotification(userVO, friendsIds, habitId, locale);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method to confirm invite from friend request.
     *
     * @param habitAssignId - id {@link HabitAssignVO}.
     */
    @Operation(summary = "Confirm invite for habit by email link")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "302", description = HttpStatuses.FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.FOUND))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/confirm/{habitAssignId}")
    public ResponseEntity<Void> confirmInvitation(@PathVariable Long habitAssignId) {
        habitAssignService.confirmHabitInvitation(habitAssignId);
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(redirectUrl + "/#/profile"))
            .build();
    }

    @Operation(summary = "Retrieve all friends' habit working days for a specific habit assignment.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = HabitWorkingDaysDto.class)))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/{habitAssignId}/friends/habit-duration-info")
    public ResponseEntity<List<HabitWorkingDaysDto>> getFriendsHabitsStreak(@PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity
            .ok(habitAssignService.getAllHabitsWorkingDaysInfoForCurrentUserFriends(userVO.getId(), habitAssignId));
    }
}
