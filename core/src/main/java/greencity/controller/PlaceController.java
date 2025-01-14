package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.annotations.CurrentUser;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableDto;
import greencity.dto.favoriteplace.FavoritePlaceDto;
import greencity.dto.filter.FilterPlaceDto;
import greencity.dto.filter.FilterPlacesApiDto;
import greencity.dto.place.AddPlaceDto;
import greencity.dto.place.AdminPlaceDto;
import greencity.dto.place.BulkUpdatePlaceStatusDto;
import greencity.dto.place.FilterPlaceCategory;
import greencity.dto.place.PlaceAddDto;
import greencity.dto.place.PlaceByBoundsDto;
import greencity.dto.place.PlaceInfoDto;
import greencity.dto.place.PlaceResponse;
import greencity.dto.place.PlaceUpdateDto;
import greencity.dto.place.PlaceVO;
import greencity.dto.place.PlaceWithUserDto;
import greencity.dto.place.UpdatePlaceStatusDto;
import greencity.dto.user.UserVO;
import greencity.enums.PlaceStatus;
import greencity.service.FavoritePlaceService;
import greencity.service.PlaceService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import javax.validation.constraints.NotNull;
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
     * @author Kateryna Horokh
     */
    @ApiOperation(value = "Propose new place.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = PlaceWithUserDto.class),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
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
     * @author Kateryna Horokh
     */
    @ApiOperation(value = "Update place")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PlaceUpdateDto.class),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
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
     * @author Dmytro Dovhal
     */
    @ApiOperation(value = "Get info about place")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PlaceInfoDto.class),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
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
     * @author Zakhar Skaletskyi
     */
    @ApiOperation(value = "Get info about favourite place")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
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
     * @author Zakhar Skaletskyi
     */
    @ApiOperation(value = "Save place as favourite.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = FavoritePlaceDto.class),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/save/favorite/")
    public ResponseEntity<FavoritePlaceDto> saveAsFavoritePlace(
        @Valid @RequestBody FavoritePlaceDto favoritePlaceDto, @ApiIgnore Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(favoritePlaceService.save(favoritePlaceDto, principal.getName()));
    }

    /**
     * The method which return a list {@code PlaceByBoundsDto} with information
     * about place, location depends on the map bounds.
     *
     * @param filterPlaceDto Contains South-West and North-East bounds of map .
     * @return a list of {@code PlaceByBoundsDto}
     * @author Marian Milian
     */
    @ApiOperation(value = "Get list of places by Map Bounds.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PlaceByBoundsDto.class),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
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
     * @author Roman Zahorui
     */
    @ApiOperation(value = "Get places by status(APPROVED, PROPOSED, DECLINED, DELETED).")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PageableDto.class),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/{status}")
    @ApiPageable
    public ResponseEntity<PageableDto<AdminPlaceDto>> getPlacesByStatus(
        @PathVariable PlaceStatus status,
        @ApiIgnore Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(placeService.getPlacesByStatus(status, pageable));
    }

    /**
     * The method which return a list {@code PlaceByBoundsDto} filtered by values
     * contained in the incoming {@link FilterPlaceDto} object.
     *
     * @param filterDto contains all information about the filtering of the list.
     * @return a list of {@code PlaceByBoundsDto}
     * @author Roman Zahorui
     */
    @ApiOperation(value = "Return a list places filtered by values contained "
        + "in the incoming FilterPlaceDto object")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = FilterPlaceDto.class),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/filter")
    public ResponseEntity<List<PlaceByBoundsDto>> getFilteredPlaces(
        @Valid @RequestBody FilterPlaceDto filterDto) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(placeService.getPlacesByFilter(filterDto));
    }

    /**
     * The method which return a list of {@link PlaceByBoundsDto} filtered by values
     * contained in the incoming {@link FilterPlacesApiDto} object.
     * {@link PlaceByBoundsDto} are retrieved from Google Places API
     *
     * @param filterDto contains all information about the filtering of the list.
     * @return a list of {@code PlaceByBoundsDto}
     */
    @ApiOperation(value = "Return a list places from Google Geocoding API filtered by values contained "
            + "in the incoming FilterPlaceDto object")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK, response = FilterPlacesApiDto.class),
            @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
            @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/filter/api")
    public ResponseEntity<List<PlaceByBoundsDto>> getFilteredPlacesFromApi(
            @Valid @RequestBody FilterPlacesApiDto filterDto,
            @ApiIgnore @CurrentUser UserVO userVO) {
        return ResponseEntity.ok().body(placeService.getPlacesByFilter(filterDto, userVO));
    }

    /**
     * The method which update {@link PlaceVO} status.
     *
     * @param dto - {@link UpdatePlaceStatusDto} with place id and updated
     *            {@link PlaceStatus}.
     * @return response object with {@link UpdatePlaceStatusDto} and OK status if
     *         everything is ok.
     * @author Nazar Vladyka
     */
    @ApiOperation(value = "Update status of place")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = UpdatePlaceStatusDto.class),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
    })
    @PatchMapping("/status")
    public ResponseEntity<UpdatePlaceStatusDto> updateStatus(@Valid @RequestBody UpdatePlaceStatusDto dto) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(placeService.updateStatus(dto.getId(), dto.getStatus()));
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
     * @author Rostyslav Khasanov
     */
    @ApiOperation(value = "Return a list places filtered by values contained "
        + "in the incoming FilterPlaceDto object")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PageableDto.class),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/filter/predicate")
    @ApiPageable
    public ResponseEntity<PageableDto<AdminPlaceDto>> filterPlaceBySearchPredicate(
        @Valid @RequestBody FilterPlaceDto filterDto,
        @ApiIgnore Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(placeService.filterPlaceBySearchPredicate(filterDto, pageable));
    }

    /**
     * Controller to get place info.
     *
     * @param id place
     * @return response {@link PlaceUpdateDto} object.
     * @author Kateryna Horokh
     */
    @ApiOperation(value = "Get place by id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PlaceUpdateDto.class),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
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
     * @author Nazar Vladyka
     */
    @ApiOperation(value = "Bulk update place statuses")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = UpdatePlaceStatusDto[].class),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
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
     * @author Nazar Vladyka
     */
    @ApiOperation(value = "Get array of available place statuses")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PlaceStatus[].class),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
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
     * @author Nazar Vladyka
     */
    @ApiOperation(value = "Delete place")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = Long.class),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
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
     * @author Nazar Vladyka
     */
    @ApiOperation(value = "Bulk delete places")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = Long.class),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
    })
    @DeleteMapping
    public ResponseEntity<Long> bulkDelete(
        @ApiParam(value = "Ids of places separated by a comma \n e.g. 1,2", required = true) @RequestParam String ids) {
        return ResponseEntity.status(HttpStatus.OK).body(placeService.bulkDelete(Arrays.stream(ids.split(","))
            .map(Long::valueOf)
            .collect(Collectors.toList())));
    }

    /**
     * Controller to get information about places categories.
     */
    @ApiOperation(value = "Return all place categories to filter")
    @GetMapping("/v2/filteredPlacesCategories")
    public ResponseEntity<List<FilterPlaceCategory>> allFilterPlaceCategories() {
        return ResponseEntity.status(HttpStatus.OK).body(placeService.getAllPlaceCategories());
    }

    /**
     * Controller to save new place from UI.
     */
    @ApiOperation(value = "Create new place from Ui")
    @PostMapping("/v2/save")
    public ResponseEntity<PlaceResponse> saveEcoPlaceFromUi(@RequestBody AddPlaceDto dto,
        @ApiIgnore Principal principal) {
        return ResponseEntity.status(HttpStatus.OK).body(placeService.addPlaceFromUi(dto, principal.getName()));
    }

    /**
     * The method which returns all places with info if place isFavorite for
     * loggedIn User.
     *
     * @return pageableDto of {@link AdminPlaceDto}.
     * @author Olena Sotnik.
     */
    @ApiOperation(value = "Get all places with info if place isFavorite")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @ApiPageable
    @GetMapping("all")
    public ResponseEntity<PageableDto<AdminPlaceDto>> getAllPlaces(@ApiIgnore Pageable page,
        @ApiIgnore Principal principal) {
        return ResponseEntity.status(HttpStatus.OK).body(placeService.findAll(page, principal.getName()));
    }
}
