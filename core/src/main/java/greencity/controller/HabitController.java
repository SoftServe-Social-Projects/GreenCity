package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.ApiPageable;
import greencity.annotations.ApiPageableWithLocale;
import greencity.annotations.CurrentUser;
import greencity.annotations.ImageValidation;
import greencity.annotations.ValidLanguage;
import greencity.constant.HttpStatuses;
import greencity.constant.SwaggerExampleModel;
import greencity.dto.PageableDto;
import greencity.dto.friends.UserFriendHabitInviteDto;
import greencity.dto.habit.*;
import greencity.dto.todolistitem.ToDoListItemDto;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.dto.user.UserProfilePictureDto;
import greencity.dto.user.UserVO;
import greencity.exception.exceptions.BadRequestException;
import greencity.service.HabitService;
import greencity.service.TagsService;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/habit")
public class HabitController {
    private final HabitService habitService;
    private final TagsService tagsService;

    /**
     * Method finds {@link HabitVO} by given id with locale translation.
     *
     * @param id     of {@link HabitVO}.
     * @param locale {@link Locale} with needed language code.
     * @return {@link HabitDto}.
     */
    @Operation(summary = "Find habit by id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND))),
    })
    @GetMapping("/{id}")
    @ApiLocale
    public ResponseEntity<HabitDto> getHabitById(@PathVariable Long id,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitService.getByIdAndLanguageCode(id, locale.getLanguage()));
    }

    /**
     * Method finds all default and custom with created by current user and his
     * friends habits that available for tracking for specific language.
     *
     * @param pageable {@link Pageable} instance.
     * @return Pageable of {@link HabitTranslationDto}.
     */
    @Operation(summary = "Find all habits.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @GetMapping
    @ApiPageable(dtoClass = HabitDto.class)
    public ResponseEntity<PageableDto<HabitDto>> getAll(
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) Pageable pageable,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK).body(
            habitService.getAllHabitsByLanguageCode(userVO, pageable, locale.getLanguage()));
    }

    /**
     * Method finds all habits created by the current user that are available for
     * tracking for a specific language.
     *
     * @param pageable {@link Pageable} instance.
     * @return Pageable of {@link HabitTranslationDto}.
     */
    @Operation(summary = "Find all habits created by the current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @GetMapping("/my")
    @ApiPageable(dtoClass = HabitDto.class)
    public ResponseEntity<PageableDto<HabitDto>> getMyHabits(@Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) Pageable pageable,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK).body(
            habitService.getMyHabits(userVO.getId(), pageable, locale.getLanguage()));
    }

    /**
     * Endpoint to get all habits (default and custom) of a specified friend for the
     * current user. This method retrieves all habits that are either default or
     * created by the friend.
     *
     * @param friendId the ID of the friend whose habits are to be retrieved.
     * @param userVO   the current user obtained from the authentication context.
     * @param pageable the pagination information.
     * @return {@link ResponseEntity} containing a pageable list of
     *         {@link HabitDto}.
     */
    @GetMapping("/all/{friendId}")
    public ResponseEntity<PageableDto<HabitDto>> getAllHabitsOfFriend(
        @PathVariable Long friendId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) Pageable pageable,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK).body(
            habitService.getAllHabitsOfFriend(userVO.getId(), friendId, pageable, locale.getLanguage()));
    }

    /**
     * Endpoint to get all mutual habits shared between the current user and a
     * specified friend. This method retrieves habits that both users have in common
     * and are either in progress or acquired.
     *
     * @param friendId the ID of the friend with whom to find mutual habits.
     * @param userVO   the current user obtained from the authentication context.
     * @param pageable the pagination information.
     * @return {@link ResponseEntity} containing a pageable list of
     *         {@link HabitDto}.
     */
    @GetMapping("/allMutualHabits/{friendId}")
    public ResponseEntity<PageableDto<HabitDto>> getAllMutualHabitsWithFriend(
        @PathVariable Long friendId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) Pageable pageable,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK).body(
            habitService.getAllMutualHabitsWithFriend(userVO.getId(), friendId, pageable, locale.getLanguage()));
    }

    /**
     * Method finds toDoList for habit in specific language.
     *
     * @param locale {@link Locale} with needed language code.
     * @param id     {@link Long} with needed habit id.
     * @return List of {@link ToDoListItemDto}.
     */
    @Operation(summary = "Get to-do list.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
    })
    @GetMapping("{id}/to-do-list")
    @ApiLocale
    public ResponseEntity<List<ToDoListItemDto>> getToDoListItems(
        @PathVariable Long id,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK).body(
            habitService.getToDoListForHabit(id, locale.getLanguage()));
    }

    /**
     * Method finds all habits by tags and language code.
     *
     * @param locale          {@link Locale} with needed language code.
     * @param pageable        {@link Pageable} instance.
     * @param tags            {@link List} of {@link String}
     * @param excludeAssigned {@link boolean} flag to determine whether to exclude
     *                        habits already assigned to the current user.
     * @return Pageable of {@link HabitDto}.
     */
    @Operation(summary = "Find all habits by tags and language code.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
    })
    @GetMapping("/tags/search")
    @ApiPageableWithLocale
    public ResponseEntity<PageableDto<HabitDto>> getAllByTagsAndLanguageCode(
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) @ValidLanguage Locale locale,
        @RequestParam List<String> tags,
        @RequestParam boolean excludeAssigned,
        @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(
            habitService.getAllByTagsAndLanguageCode(pageable, tags, locale.getLanguage(), excludeAssigned,
                userVO.getId()));
    }

    /**
     * Method finds all habits by tags, isCustomHabit, complexities and language
     * code.
     *
     * @param locale        {@link Locale} with needed language code.
     * @param pageable      {@link Pageable} instance.
     * @param tags          {@link List} of {@link String}.
     * @param isCustomHabit {@link Boolean} value.
     * @param complexities  {@link List} of {@link Integer}.
     * @return Pageable of {@link HabitDto} instance.
     */
    @Operation(summary = "Find all habits by tags, isCustomHabit, complexities.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @GetMapping("/search")
    @ApiPageableWithLocale
    public ResponseEntity<PageableDto<HabitDto>> getAllByDifferentParameters(
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) @ValidLanguage Locale locale,
        @RequestParam(required = false, name = "tags") Optional<List<String>> tags,
        @RequestParam(required = false, name = "isCustomHabit") Optional<Boolean> isCustomHabit,
        @RequestParam(required = false, name = "complexities") Optional<List<Integer>> complexities,
        @Parameter(hidden = true) Pageable pageable) throws BadRequestException {
        if (isValid(tags, isCustomHabit, complexities)) {
            return ResponseEntity.status(HttpStatus.OK).body(
                habitService.getAllByDifferentParameters(userVO, pageable, tags,
                    isCustomHabit, complexities, locale.getLanguage()));
        } else {
            throw new BadRequestException("You should enter at least one parameter");
        }
    }

    /**
     * Method checks if at least one of the input parameters (tags, isCustomHabit,
     * complexities) is present.
     *
     * @param tags          {@link List} of {@link String}.
     * @param isCustomHabit {@link Boolean} value.
     * @param complexities  {@link List} of {@link Integer}.
     *
     * @author Lilia Mokhnatska
     */
    private boolean isValid(Optional<List<String>> tags, Optional<Boolean> isCustomHabit,
        Optional<List<Integer>> complexities) {
        return ((tags.isPresent() && !tags.get().isEmpty()) || isCustomHabit.isPresent()
            || (complexities.isPresent() && !complexities.get().isEmpty()));
    }

    /**
     * The method which returns all habit's tags.
     *
     * @return list of {@link String} (tag's names).
     * @author Markiyan Derevetskyi
     */
    @Operation(summary = "Find all habits tags")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
    })
    @GetMapping("/tags")
    @ApiLocale
    public ResponseEntity<List<String>> findAllHabitsTags(@Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK).body(tagsService.findAllHabitsTags(locale.getLanguage()));
    }

    /**
     * Method for creating Custom Habit.
     *
     * @param request {@link CustomHabitDtoRequest} - new custom habit dto.
     * @return dto {@link CustomHabitDtoResponse}
     *
     * @author Lilia Mokhnatska.
     */
    @Operation(summary = "Add new custom habit.")
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = CustomHabitDtoResponse.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @PostMapping(value = "/custom", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomHabitDtoResponse> addCustomHabit(
        @Parameter(example = SwaggerExampleModel.ADD_CUSTOM_HABIT_REQUEST,
            required = true) @RequestPart @Valid CustomHabitDtoRequest request,
        @Parameter(description = "Image of habit") @ImageValidation @RequestPart(required = false) MultipartFile image,
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(habitService.addCustomHabit(request, image, principal.getName()));
    }

    /**
     * Retrieves the list of profile pictures of the user's friends who are
     * associated with a specified habit assignment and have an invitation
     * relationship with the user. This includes friends who either invited the user
     * or were invited by the user for the specific habit assignment.
     *
     * @param habitAssignId The ID of the habit assignment.
     * @param userVO        The current user, represented as a {@link UserVO}.
     * @return A {@link ResponseEntity} containing a list of
     *         {@link UserProfilePictureDto} objects representing the profile
     *         pictures of the friends associated with the habit.
     */
    @Operation(summary = "Retrieves the list of profile pictures of the user's friends "
        + "who are associated with a specified habit assignment through invitations.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND))),
    })
    @GetMapping("/{habitAssignId}/friends/profile-pictures")
    public ResponseEntity<List<UserProfilePictureDto>> getFriendsAssignedToHabitProfilePictures(
        @PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitService.getFriendsAssignedToHabitProfilePictures(habitAssignId, userVO.getId()));
    }

    /**
     * Method for updating Custom Habit.
     *
     * @param request {@link CustomHabitDtoRequest} - custom habit dto.
     * @return dto {@link CustomHabitDtoResponse}
     *
     * @author Olena Sotnik.
     */
    @Operation(summary = "Update new custom habit.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = CustomHabitDtoResponse.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PutMapping(value = "/update/{habitId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomHabitDtoResponse> updateCustomHabit(@PathVariable Long habitId,
        @RequestPart @Valid CustomHabitDtoRequest request, @Parameter(hidden = true) Principal principal,
        @Parameter(description = "Image of habit") @ImageValidation @RequestPart(
            required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitService.updateCustomHabit(request, habitId, principal.getName(), image));
    }

    /**
     * Method for deleting Custom Habit by habitId.
     *
     * @param customHabitId - id of custom habit that will be deleted
     * @author Olena Sotnik.
     */
    @Operation(summary = "Deleting of custom habit by habitId for its owner.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = CustomHabitDtoResponse.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @DeleteMapping(value = "/delete/{customHabitId}")
    public ResponseEntity<Object> deleteCustomHabit(@PathVariable Long customHabitId,
        @Parameter(hidden = true) Principal principal) {
        habitService.deleteCustomHabit(customHabitId, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method to like/unlike certain {@link HabitDto} habit specified by id.
     *
     * @param habitId of {@link HabitDto} to like/unlike
     */
    @Operation(summary = "Like/unlike habit.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/like")
    public void like(@RequestParam("habitId") Long habitId, @Parameter(hidden = true) @CurrentUser UserVO user) {
        habitService.like(habitId, user);
    }

    /**
     * Method to dislike or remove dislike for certain {@link HabitDto} habit
     * specified by id.
     *
     * @param habitId of {@link HabitDto} to like/dislike
     */
    @Operation(summary = "Dislike/remove dislike from a habit.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/dislike")
    public void dislike(@RequestParam("habitId") Long habitId, @Parameter(hidden = true) @CurrentUser UserVO user) {
        habitService.dislike(habitId, user);
    }

    /**
     * Method for adding a habit to favorites by habitId.
     */
    @Operation(summary = "Add a habit to favorites")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/{habitId}/favorites")
    public ResponseEntity<Object> addToFavorites(@PathVariable Long habitId,
        @Parameter(hidden = true) Principal principal) {
        habitService.addToFavorites(habitId, principal.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * Method for removing a habit from favorites by habit id.
     */
    @Operation(summary = "Remove a habit from favorites")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @DeleteMapping("/{habitId}/favorites")
    public ResponseEntity<Object> removeFromFavorites(@PathVariable Long habitId,
        @Parameter(hidden = true) Principal principal) {
        habitService.removeFromFavorites(habitId, principal.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * Method finds all favorite habits.
     *
     * @param pageable {@link Pageable} instance.
     * @return Pageable of {@link HabitTranslationDto}.
     */
    @Operation(summary = "Find all favorite habits.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })

    @GetMapping("/favorites")
    @ApiPageable(dtoClass = HabitDto.class)
    public ResponseEntity<PageableDto<HabitDto>> getAllFavorites(
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) Pageable pageable,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK).body(
            habitService.getAllFavoriteHabitsByLanguageCode(userVO, pageable, locale.getLanguage()));
    }

    /**
     * Retrieves a paginated list of friends who can be invited to a specific habit.
     * Optionally filters by friend name.
     *
     * @param page    The pagination information (page number, size).
     * @param name    Optional name filter for friends.
     * @param habitId The ID of the habit for which friends are being invited.
     * @param userVO  The current user's details.
     * @return A paginated list of friends (UserFriendHabitInviteDto) to be invited.
     */
    @Operation(summary = "Find all friends to be invited")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @GetMapping("/friends")
    @ApiPageable(dtoClass = UserFriendHabitInviteDto.class)
    public ResponseEntity<PageableDto<UserFriendHabitInviteDto>> findAllFriendsOfUserToBeInvited(
        @Parameter(hidden = true) Pageable page,
        @RequestParam(required = false) @Nullable String name,
        @RequestParam Long habitId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(habitService.findAllFriendsOfUser(userVO, name, page, habitId));
    }
}
