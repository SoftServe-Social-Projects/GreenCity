package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.ApiPageable;
import greencity.annotations.CurrentUser;
import greencity.annotations.ImageValidation;
import greencity.annotations.ValidEcoNewsDtoRequest;
import greencity.annotations.ValidLanguage;
import greencity.constant.ErrorMessage;
import greencity.constant.HttpStatuses;
import greencity.constant.SwaggerExampleModel;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.econews.AddEcoNewsDtoRequest;
import greencity.dto.econews.AddEcoNewsDtoResponse;
import greencity.dto.econews.EcoNewContentSourceDto;
import greencity.dto.econews.EcoNewsDto;
import greencity.dto.econews.EcoNewsGenericDto;
import greencity.dto.econews.EcoNewsVO;
import greencity.dto.econews.UpdateEcoNewsDto;
import greencity.dto.tag.TagDto;
import greencity.dto.tag.TagVO;
import greencity.dto.user.UserVO;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.WrongIdException;
import greencity.service.EcoNewsService;
import greencity.service.TagsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/eco-news")
@RequiredArgsConstructor
public class EcoNewsController {
    private final EcoNewsService ecoNewsService;
    private final TagsService tagService;

    /**
     * Method for creating {@link EcoNewsVO}.
     *
     * @param addEcoNewsDtoRequest - dto for {@link EcoNewsVO} entity.
     * @return dto {@link AddEcoNewsDtoResponse} instance.
     * @author Yuriy Olkhovskyi & Kovaliv Taras.
     */
    @Operation(summary = "Add new eco news.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = EcoNewsGenericDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EcoNewsGenericDto> save(
        @Parameter(description = SwaggerExampleModel.ADD_ECO_NEWS_REQUEST,
            required = true) @RequestPart @ValidEcoNewsDtoRequest AddEcoNewsDtoRequest addEcoNewsDtoRequest,
        @Parameter(description = "Image of eco news") @ImageValidation @RequestPart(
            required = false) MultipartFile image,
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ecoNewsService.saveEcoNews(addEcoNewsDtoRequest, image, principal.getName()));
    }

    /**
     * Method for adding an eco new to favorites by ecoNewsId.
     */
    @Operation(summary = "Add an eco new to favorites")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/{ecoNewsId}/favorites")
    public ResponseEntity<Object> addToFavorites(@PathVariable Long ecoNewsId,
        @Parameter(hidden = true) Principal principal) {
        ecoNewsService.addToFavorites(ecoNewsId, principal.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * Method for removing an eco new from favorites by ecoNewsId.
     */
    @Operation(summary = "Remove an eco news from favorites")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @DeleteMapping("/{ecoNewsId}/favorites")
    public ResponseEntity<Object> removeFromFavorites(@PathVariable Long ecoNewsId,
        @Parameter(hidden = true) Principal principal) {
        ecoNewsService.removeFromFavorites(ecoNewsId, principal.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * Method for updating {@link EcoNewsVO}.
     *
     * @param updateEcoNewsDto - dto for {@link EcoNewsVO} entity.
     * @return dto {@link EcoNewsDto} instance.
     */
    @Operation(summary = "Update eco news")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = EcoNewsGenericDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @PutMapping(path = "/{ecoNewsId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EcoNewsGenericDto> update(
        @Parameter(description = SwaggerExampleModel.UPDATE_ECO_NEWS,
            required = true) @Valid @RequestPart UpdateEcoNewsDto updateEcoNewsDto,
        @Parameter(description = "Image of eco news") @ImageValidation @RequestPart(
            required = false) MultipartFile image,
        @Parameter(hidden = true) @CurrentUser UserVO user,
        @PathVariable Long ecoNewsId) {
        if (!ecoNewsId.equals(updateEcoNewsDto.getId())) {
            throw new WrongIdException(ErrorMessage.ECO_NEWS_ID_IN_PATH_PARAM_AND_ENTITY_NOT_EQUAL);
        }
        return ResponseEntity.status(HttpStatus.OK).body(ecoNewsService.update(updateEcoNewsDto, image, user));
    }

    /**
     * Method for getting eco news by id.
     *
     * @return {@link EcoNewsDto} instance.
     * @author Kovaliv Taras
     */
    @Operation(summary = "Get eco news by id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @ApiLocale
    @GetMapping("/{ecoNewsId}")
    public ResponseEntity<EcoNewsDto> getEcoNewsById(
        @PathVariable Long ecoNewsId,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ecoNewsService.findDtoByIdAndLanguage(ecoNewsId, locale.getLanguage()));
    }

    /**
     * Method for getting eco news by page.
     *
     * @return PageableDto of {@link EcoNewsDto} instances.
     * @author Yuriy Olkhovskyi & Kovaliv Taras.
     */
    @Operation(summary = "Find eco news by page.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST)))
    })
    @ApiPageable
    @GetMapping
    public ResponseEntity<PageableAdvancedDto<EcoNewsGenericDto>> findAll(
        @Parameter(hidden = true) Pageable page,
        @Parameter(description = "Tags to filter (if do not input tags get all)") @RequestParam(
            required = false) List<String> tags,
        @RequestParam(required = false) String title,
        @RequestParam(required = false, name = "author-id") Long authorId,
        @Parameter(description = "Search for favorite news") @RequestParam(required = false, name = "favorite",
            defaultValue = "false") boolean favorite,
        @Parameter(hidden = true) Principal principal) {
        String userEmail = principal != null ? principal.getName() : null;

        return ResponseEntity.status(HttpStatus.OK).body(
            ecoNewsService.find(page, tags, title, authorId, favorite, userEmail));
    }

    /**
     * Method for deleting {@link EcoNewsVO} by its id.
     *
     * @param ecoNewsId {@link EcoNewsVO} id which will be deleted.
     * @return id of deleted {@link EcoNewsVO}.
     * @author Yuriy Olkhovskyi.
     */
    @Operation(summary = "Delete eco news.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @DeleteMapping("/{ecoNewsId}")
    public ResponseEntity<Object> delete(
        @PathVariable Long ecoNewsId,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        ecoNewsService.delete(ecoNewsId, user);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for getting three eco news for recommendations widget.
     *
     * @return list of three recommended {@link EcoNewsDto} instances.
     * @author Yurii Zhurakovskyi.
     */
    @Operation(summary = "Get three recommended eco news.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK)
    })
    @GetMapping("/{ecoNewsId}/recommended")
    public ResponseEntity<List<EcoNewsDto>> getThreeRecommendedEcoNews(
        @PathVariable Long ecoNewsId) {
        List<EcoNewsDto> threeRecommendedEcoNews = ecoNewsService.getThreeRecommendedEcoNews(ecoNewsId);
        return ResponseEntity.status(HttpStatus.OK).body(threeRecommendedEcoNews);
    }

    /**
     * The method which returns all EcoNews {@link TagVO}s.
     *
     * @return list of {@link String} (tag's names).
     * @author Kovaliv Taras
     */
    @Operation(summary = "Find all eco news tags")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
    })
    @ApiLocale
    @GetMapping("/tags")
    public ResponseEntity<List<TagDto>> findAllEcoNewsTags(@Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK).body(tagService.findAllEcoNewsTags(locale.getLanguage()));
    }

    /**
     * The method find count of published eco news.
     *
     * @return count of published eco news.
     * @author Mamchuk Orest
     */
    @Operation(summary = "Find count of published eco news")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @GetMapping("/count")
    public ResponseEntity<Long> findAmountOfPublishedNews(
        @RequestParam(required = false, name = "author-id") Long authorId) {
        return ResponseEntity.status(HttpStatus.OK).body(ecoNewsService.getAmountOfPublishedNews(authorId));
    }

    /**
     * Method to like EcoNews.
     */
    @Operation(summary = "Like eco news")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @PostMapping("/{ecoNewsId}/likes")
    public void like(@PathVariable Long ecoNewsId, @Parameter(hidden = true) @CurrentUser UserVO user) {
        ecoNewsService.like(user, ecoNewsId);
    }

    /**
     * Method to dislike EcoNews.
     */
    @Operation(description = "Dislike eco news")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED)))
    })
    @PostMapping("/{ecoNewsId}/dislikes")
    public void dislike(@PathVariable Long ecoNewsId, @Parameter(hidden = true) @CurrentUser UserVO user) {
        ecoNewsService.dislike(user, ecoNewsId);
    }

    /**
     * Method to get amount of likes by eco news id.
     *
     * @return count of likes for eco news;
     */
    @Operation(description = "Count likes by id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/{ecoNewsId}/likes/count")
    public ResponseEntity<Integer> countLikesForEcoNews(@PathVariable Long ecoNewsId) {
        return ResponseEntity.status(HttpStatus.OK).body(ecoNewsService.countLikesForEcoNews(ecoNewsId));
    }

    /**
     * Method to get amount of dislikes by eco news id.
     *
     * @return count of dislikes for eco news;
     */
    @Operation(description = "Count dislikes by id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/{ecoNewsId}/dislikes/count")
    public ResponseEntity<Integer> countDislikesForEcoNews(@PathVariable Long ecoNewsId) {
        return ResponseEntity.status(HttpStatus.OK).body(ecoNewsService.countDislikesForEcoNews(ecoNewsId));
    }

    /**
     * Check if user liked news.
     *
     * @return user liked news or not.
     */
    @Operation(summary = "Check if user liked news")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(examples = @ExampleObject(HttpStatuses.NOT_FOUND)))
    })
    @GetMapping("/{ecoNewsId}/likes/{userId}")
    public ResponseEntity<Boolean> checkNewsIsLikedByUser(
        @PathVariable("ecoNewsId") Long ecoNewsId,
        @PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(ecoNewsService.checkNewsIsLikedByUser(ecoNewsId, userId));
    }

    /**
     * Method for getting some fields in eco news by id.
     *
     * @return dto {@link EcoNewContentSourceDto}.
     */
    @Operation(summary = "Get content and source in eco news by id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND,
            content = @Content(schema = @Schema(implementation = NotFoundException.class)))
    })
    @GetMapping("/{ecoNewsId}/summary")
    public ResponseEntity<EcoNewContentSourceDto> getContentAndSourceForEcoNewsById(@PathVariable Long ecoNewsId) {
        return ResponseEntity.status(HttpStatus.OK).body(ecoNewsService.getContentAndSourceForEcoNewsById(ecoNewsId));
    }
}
