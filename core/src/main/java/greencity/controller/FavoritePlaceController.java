package greencity.controller;

import greencity.constant.HttpStatuses;
import greencity.dto.favoriteplace.FavoritePlaceDto;
import greencity.dto.favoriteplace.FavoritePlaceVO;
import greencity.dto.place.PlaceByBoundsDto;
import greencity.dto.place.PlaceVO;
import greencity.dto.user.UserVO;
import greencity.service.FavoritePlaceService;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import java.security.Principal;
import java.util.List;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/favorite_place/")
@AllArgsConstructor
public class FavoritePlaceController {
    private final FavoritePlaceService favoritePlaceService;

    /**
     * Update {@link FavoritePlaceVO} name for {@link UserVO}. Parameter principal
     * are ignored because Spring automatically provide the Principal object.
     *
     * @param favoritePlaceDto - dto for {@link FavoritePlaceVO} entity
     * @param principal        - Principal with user email
     * @return {@link FavoritePlaceDto} instance
     * @author Zakhar Skaletskyi
     */
    @Operation(summary = "Update user favorite place.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = FavoritePlaceDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PutMapping
    public ResponseEntity<FavoritePlaceDto> update(@Valid @RequestBody FavoritePlaceDto favoritePlaceDto,
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.status(HttpStatus.OK).body(favoritePlaceService
            .update(favoritePlaceDto, principal.getName()));
    }

    /**
     * Find all {@link PlaceByBoundsDto} by {@link UserVO} email. Parameter
     * principal are ignored because Spring automatically provide the Principal
     * object .
     *
     * @param principal - Principal with {@link UserVO} email
     * @return list of {@link PlaceByBoundsDto}
     * @author Zakhar Skaletskyi
     */
    @Operation(summary = "Get list favorite places by user email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaceByBoundsDto.class)))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @GetMapping
    public ResponseEntity<List<PlaceByBoundsDto>> findAllByUserEmail(@Parameter(hidden = true) Principal principal) {
        return ResponseEntity.status(HttpStatus.OK).body(favoritePlaceService.findAllByUserEmail(principal.getName()));
    }

    /**
     * Delete {@link FavoritePlaceVO} by {@link UserVO} email and {@link PlaceVO} id
     * Parameter principal are ignored because Spring automatically provide the
     * Principal object.
     *
     * @param placeId   - {@link PlaceVO} id
     * @param principal - Principal with {@link UserVO} email
     * @return id of deleted {@link FavoritePlaceVO}
     * @author Zakhar Skaletskyi
     */
    @Operation(summary = "Delete favorite place by userEmail")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @DeleteMapping("/{placeId}")
    public ResponseEntity<Long> deleteByUserEmailAndPlaceId(@PathVariable Long placeId,
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.status(HttpStatus.OK).body(favoritePlaceService
            .deleteByUserEmailAndPlaceId(placeId, principal.getName()));
    }

    /**
     * Controller to get {@link PlaceByBoundsDto} coordinates, id and name.
     * Parameter principal is ignored because Spring automatically provide the
     * Principal object.
     *
     * @param placeId   - {@link PlaceVO} id
     * @param principal - Principal with {@link UserVO} email
     * @return info about {@link PlaceVO} with name from {@link PlaceByBoundsDto}
     * @author Zakhar Skaletskyi
     */
    @Operation(summary = "Get favoritePlace with Coordinate")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PlaceByBoundsDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/favorite/{placeId}")
    public ResponseEntity<PlaceByBoundsDto> getFavoritePlaceWithCoordinate(@PathVariable Long placeId,
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.status(HttpStatus.OK).body(favoritePlaceService
            .getFavoritePlaceWithLocation(placeId, principal.getName()));
    }
}
