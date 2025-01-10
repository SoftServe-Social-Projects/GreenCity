package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.annotations.CurrentUser;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableDto;
import greencity.dto.favoriteplace.FavoritePlaceDto;
import greencity.dto.filter.FilterPlacesApiDto;
import greencity.dto.filter.FilterPlaceDto;
import greencity.dto.place.PlaceAddDto;
import greencity.dto.place.PlaceInfoDto;
import greencity.dto.place.PlaceUpdateDto;
import greencity.dto.place.PlaceWithUserDto;
import greencity.dto.place.PlaceByBoundsDto;
import greencity.dto.place.AdminPlaceDto;
import greencity.dto.place.UpdatePlaceStatusWithUserEmailDto;
import greencity.dto.place.UpdatePlaceStatusDto;
import greencity.dto.place.PlaceVO;
import greencity.dto.place.BulkUpdatePlaceStatusDto;
import greencity.dto.place.FilterPlaceCategory;
import greencity.dto.place.PlaceResponse;
import greencity.dto.place.AddPlaceDto;
import greencity.dto.user.UserVO;
import greencity.enums.PlaceStatus;
import greencity.service.FavoritePlaceService;
import greencity.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestPart;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/place")
@AllArgsConstructor
public class PlaceController {
    private final FavoritePlaceService favoritePlaceService;
    /**
     * Autowired PlaceService instance.
     */
    private final PlaceService placeService;
    private final ModelMapper modelMapper;

    /**
     * The controller which returns new proposed {@code Place} from user.
     *
     * @param dto - Place dto for adding with all parameters.
     * @return new {@code Place}.
     */
    @Operation(summary = "Propose new place.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = PlaceWithUserDto.class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN)))
    })
    @PostMapping("/propose")
    public ResponseEntity<PlaceWithUserDto> proposePlace(
        @Valid @RequestBody PlaceAddDto dto, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(modelMapper.map(placeService.save(dto, principal.getName()), PlaceWithUserDto.class));
    }

    /**
     * The controller which returns new updated {@code Place}.
     *
     * @param dto - Place dto for updating with all parameters.
     * @return new {@code Place}.
     */
    @Operation(summary = "Update place")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PlaceUpdateDto.class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PutMapping("/update")
    public ResponseEntity<PlaceUpdateDto> updatePlace(
        @Valid @RequestBody PlaceUpdateDto dto) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(modelMapper.map(placeService.update(dto), PlaceUpdateDto.class));
    }

    /**
     * The method to get place info.
     *
     * @param id place
     * @return info about place
     */
    @Operation(summary = "Get info about place")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PlaceInfoDto.class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/info/{id}")
    public ResponseEntity<Object> getInfo(@NotNull @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(placeService.getInfoById(id));
    }

    /**
     * The method to get {@code FavoritePlace}. as {@code Place} info. Parameter
     * principal are ignored because Spring automatically provide the Principal
     * object.
     *
     * @param placeId - {@code Place} id
     * @return info about {@code Place} with name as in {@code FavoritePlace}
     */
    @Operation(summary = "Get info about favourite place")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/info/favorite/{placeId}")
    public ResponseEntity<PlaceInfoDto> getFavoritePlaceInfo(@PathVariable Long placeId) {
        return ResponseEntity.status(HttpStatus.OK).body(favoritePlaceService.getInfoFavoritePlace(placeId));
    }

    /**
     * The method to save {@link PlaceVO} to {@link UserVO}'s favorite list.
     * Parameter principal are ignored because Spring automatically provide the
     * Principal object.
     *
     * @param favoritePlaceDto -{@link FavoritePlaceDto}
     * @return principal - user e,ail
     */
    @Operation(summary = "Save place as favourite.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = FavoritePlaceDto.class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/save/favorite/")
    public ResponseEntity<FavoritePlaceDto> saveAsFavoritePlace(
        @Valid @RequestBody FavoritePlaceDto favoritePlaceDto, @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(favoritePlaceService.save(favoritePlaceDto, principal.getName()));
    }

    /**
     * The method which return a list {@code PlaceByBoundsDto} with information
     * about place, location depends on the map bounds.
     *
     * @param filterPlaceDto Contains South-West and North-East bounds of map .
     * @return a list of {@code PlaceByBoundsDto}
     */
    @Operation(summary = "Get list of places by Map Bounds.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PlaceByBoundsDto.class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/getListPlaceLocationByMapsBounds")
    public ResponseEntity<List<PlaceByBoundsDto>> getListPlaceLocationByMapsBounds(
        @Valid @RequestBody FilterPlaceDto filterPlaceDto) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(placeService.findPlacesByMapsBounds(filterPlaceDto));
    }

    /**
     * The method parse the string param to PlaceStatus value. Parameter pageable
     * ignored because swagger ui shows the wrong params, instead they are explained
     * in the {@link ApiPageable}.
     *
     * @param status   a string represents {@link PlaceStatus} enum value.
     * @param pageable pageable configuration.
     * @return response {@link PageableDto} object. Contains a list of
     *         {@link AdminPlaceDto}.
     */
    @Operation(summary = "Get places by status(APPROVED, PROPOSED, DECLINED, DELETED).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PageableDto.class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/{status}")
    @ApiPageable
    public ResponseEntity<PageableDto<AdminPlaceDto>> getPlacesByStatus(
        @PathVariable PlaceStatus status,
        @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(placeService.getPlacesByStatus(status, pageable));
    }

    /**
     * The method which return a list {@code PlaceByBoundsDto} filtered by values
     * contained in the incoming {@link FilterPlaceDto} object.
     *
     * @param filterDto contains all information about the filtering of the list.
     * @return a list of {@code PlaceByBoundsDto}
     */
    @Operation(summary = "Return a list places filtered by values contained "
        + "in the incoming FilterPlaceDto object")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = FilterPlaceDto.class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/filter")
    public ResponseEntity<List<PlaceByBoundsDto>> getFilteredPlaces(
        @Valid @RequestBody FilterPlaceDto filterDto,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity.ok().body(placeService.getPlacesByFilter(filterDto, userVO));
    }

    @Operation(summary = "Return a list places from Google Geocoding API filtered by values contained "
        + "in the incoming FilterPlaceDto object")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(example = FilterPlacesApiDto.defaultJson))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/filter/api")
    public ResponseEntity<List<PlaceByBoundsDto>> getFilteredPlacesFromApi(
        @Schema(
            description = "Filters for places from API",
            name = "FilterPlacesApiDto",
            type = "object",
            example = FilterPlacesApiDto.defaultJson) @Valid @RequestBody FilterPlacesApiDto filterDto,
        @CurrentUser @Parameter(hidden = true) UserVO userVO) {
        return ResponseEntity.ok().body(placeService.getPlacesByFilter(filterDto, userVO));
    }

    /**
     * The method which update {@link PlaceVO} status.
     *
     * @param dto - {@link UpdatePlaceStatusDto} with place id and updated
     *            {@link PlaceStatus}.
     * @return response object with {@link UpdatePlaceStatusDto} and OK status if
     *         everything is ok.
     */
    @Operation(summary = "Update status of place")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = UpdatePlaceStatusDto.class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN)))
    })
    @PatchMapping("/status")
    public ResponseEntity<UpdatePlaceStatusWithUserEmailDto> updateStatus(
        @Valid @RequestBody UpdatePlaceStatusWithUserEmailDto dto) {
        return ResponseEntity.ok(placeService.updatePlaceStatus(dto));
    }

    /**
     * The method which return a list {@link PageableDto} filtered by values
     * contained in the incoming {@link FilterPlaceDto} object. Parameter pageable
     * ignored because swagger ui shows the wrong params, instead they are explained
     * in the {@link ApiPageable}.
     *
     * @param filterDto contains all information about the filtering of the list.
     * @param pageable  pageable configuration.
     * @return a list of {@link PageableDto}
     */
    @Operation(summary = "Return a list places filtered by values contained "
        + "in the incoming FilterPlaceDto object")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PageableDto.class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/filter/predicate")
    @ApiPageable
    public ResponseEntity<PageableDto<AdminPlaceDto>> filterPlaceBySearchPredicate(
        @Valid @RequestBody FilterPlaceDto filterDto,
        @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(placeService.filterPlaceBySearchPredicate(filterDto, pageable));
    }

    /**
     * Controller to get place info.
     *
     * @param id place
     * @return response {@link PlaceUpdateDto} object.
     */
    @Operation(summary = "Get place by id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PlaceUpdateDto.class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/about/{id}")
    public ResponseEntity<PlaceUpdateDto> getPlaceById(@NotNull @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(placeService.getInfoForUpdatingById(id));
    }

    /**
     * The method which update array of {@link PlaceVO}'s from DB.
     *
     * @param dto - {@link BulkUpdatePlaceStatusDto} with {@link PlaceVO}'s id's and
     *            updated {@link PlaceStatus}
     * @return list of {@link UpdatePlaceStatusDto} with updated {@link PlaceVO}'s
     *         and {@link PlaceStatus}'s
     */
    @Operation(summary = "Bulk update place statuses")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = UpdatePlaceStatusDto.class)))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN)))
    })
    @PatchMapping("/statuses")
    public ResponseEntity<List<UpdatePlaceStatusDto>> bulkUpdateStatuses(
        @Valid @RequestBody BulkUpdatePlaceStatusDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(
            placeService.updateStatuses(dto));
    }

    /**
     * The method which return array of {@link PlaceStatus}.
     *
     * @return array of statuses
     */
    @Operation(summary = "Get array of available place statuses")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = PlaceStatus.class)))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/statuses")
    public ResponseEntity<List<PlaceStatus>> getStatuses() {
        return ResponseEntity.status(HttpStatus.OK).body(placeService.getStatuses());
    }

    /**
     * The method which delete {@link PlaceVO} from DB(change {@link PlaceStatus} to
     * DELETED).
     *
     * @param id - {@link PlaceVO} id
     */
    @Operation(summary = "Delete place")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN)))
    })
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        placeService.deleteById(id);
        ResponseEntity.status(HttpStatus.OK);
    }

    /**
     * The method which delete array of {@link PlaceVO}'s from DB(change
     * {@link PlaceStatus} to DELETED).
     *
     * @param ids - list of id's of {@link PlaceVO}'s, splited by "," which need to
     *            be deleted
     * @return count of deleted {@link PlaceVO}'s
     */
    @Operation(summary = "Bulk delete places")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER,
            content = @Content(examples = @ExampleObject(HttpStatuses.SEE_OTHER))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN)))
    })
    @DeleteMapping
    public ResponseEntity<Long> bulkDelete(
        @Parameter(description = "Ids of places separated by a comma \n e.g. 1,2",
            required = true) @RequestParam String ids) {
        return ResponseEntity.status(HttpStatus.OK).body(placeService.bulkDelete(Arrays.stream(ids.split(","))
            .map(Long::valueOf)
            .collect(Collectors.toList())));
    }

    /**
     * Controller to get information about places categories.
     */
    @Operation(summary = "Return all place categories to filter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @GetMapping("/v2/filteredPlacesCategories")
    public ResponseEntity<List<FilterPlaceCategory>> allFilterPlaceCategories() {
        return ResponseEntity.status(HttpStatus.OK).body(placeService.getAllPlaceCategories());
    }

    /**
     * Controller to save new place from UI.
     */
    @Operation(summary = "Create new place from Ui")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @PostMapping(value = "/v2/save",
        consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<PlaceResponse> saveEcoPlaceFromUi(@Parameter(required = true) @RequestPart AddPlaceDto dto,
        @Parameter(hidden = true) Principal principal,
        @RequestPart(required = false) @Nullable MultipartFile[] images) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(placeService.addPlaceFromUi(dto, principal.getName(), images));
    }

    /**
     * The method which returns all places with info if place isFavorite for
     * loggedIn User.
     *
     * @return pageableDto of {@link AdminPlaceDto}.
     */
    @Operation(summary = "Get all places with info if place isFavorite")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @ApiPageable
    @GetMapping("all")
    public ResponseEntity<PageableDto<AdminPlaceDto>> getAllPlaces(@Parameter(hidden = true) Pageable page,
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.status(HttpStatus.OK).body(placeService.findAll(page, principal));
    }
}
