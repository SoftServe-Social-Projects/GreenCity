package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.annotations.ImageValidation;
import greencity.annotations.ValidEcoNewsDtoRequest;
import greencity.constant.HttpStatuses;
import greencity.constant.SwaggerExampleModel;
import greencity.dto.PageableDto;
import greencity.dto.econews.AddEcoNewsDtoRequest;
import greencity.dto.econews.AddEcoNewsDtoResponse;
import greencity.dto.econews.EcoNewsDto;
import greencity.entity.EcoNews;
import greencity.entity.Tag;
import greencity.service.EcoNewsService;
import greencity.service.TagsService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

@Validated
@RestController
@RequestMapping("/econews")
@RequiredArgsConstructor
public class EcoNewsController {
    private final EcoNewsService ecoNewsService;
    private final TagsService tagService;

    /**
     * Method for getting three last eco news.
     *
     * @return list of {@link EcoNewsDto} instances.
     * @author Yuriy Olkhovskyi.
     */
    @ApiOperation(value = "Get three last eco news.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
    })
    @GetMapping("/newest")
    public ResponseEntity<List<EcoNewsDto>> getThreeLastEcoNews() {
        return ResponseEntity.status(HttpStatus.OK).body(ecoNewsService.getThreeLastEcoNews());
    }

    /**
     * Method for creating {@link EcoNews}.
     *
     * @param addEcoNewsDtoRequest - dto for {@link EcoNews} entity.
     * @return dto {@link AddEcoNewsDtoResponse} instance.
     * @author Yuriy Olkhovskyi & Kovaliv Taras.
     */


    @ApiOperation(value = "Add new eco news.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = HttpStatuses.CREATED,
                    response = AddEcoNewsDtoResponse.class),
            @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
    })
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<AddEcoNewsDtoResponse> save(
            @ApiParam(value = SwaggerExampleModel.addEcoNewsRequest, required = true)
            @RequestPart @ValidEcoNewsDtoRequest AddEcoNewsDtoRequest addEcoNewsDtoRequest,
            @ApiParam(value = "Image of eco news")
            @ImageValidation
            @RequestPart(required = false) MultipartFile image,
            @ApiIgnore Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ecoNewsService.save(addEcoNewsDtoRequest, image, principal.getName()));
    }

    /**
     * Method for getting eco news by id.
     *
     * @return {@link EcoNewsDto} instance.
     * @author Kovaliv Taras
     */
    @ApiOperation(value = "Get eco news by id.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
    })
    @GetMapping("/{id}")
    public ResponseEntity<EcoNewsDto> getEcoNewsById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(ecoNewsService.findDtoById(id));
    }

    /**
     * Method for getting all eco news by page.
     *
     * @return PageableDto of {@link EcoNewsDto} instances.
     * @author Yuriy Olkhovskyi & Kovaliv Taras.
     */
    @ApiOperation(value = "Find all eco news by page.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
    })
    @GetMapping("")
    @ApiPageable
    public ResponseEntity<PageableDto<EcoNewsDto>> findAll(@ApiIgnore Pageable page) {
        return ResponseEntity.status(HttpStatus.OK).body(ecoNewsService.findAll(page));
    }

    /**
     * Method for deleting {@link EcoNews} by its id.
     *
     * @param econewsId {@link EcoNews} id which will be deleted.
     * @return id of deleted {@link EcoNews}.
     * @author Yuriy Olkhovskyi.
     */
    @ApiOperation(value = "Delete eco news.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK),
            @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @DeleteMapping("/{econewsId}")
    public ResponseEntity<Object> delete(@PathVariable Long econewsId) {
        ecoNewsService.delete(econewsId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for getting all eco news by tags.
     *
     * @return list of {@link EcoNewsDto} instances.
     * @author Kovaliv Taras.
     */
    @ApiOperation(value = "Get eco news by tags")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
    })
    @GetMapping("/tags")
    @ApiPageable
    public ResponseEntity<PageableDto<EcoNewsDto>> getEcoNews(
            @ApiIgnore Pageable page,
            @ApiParam(value = "Tags to filter (if do not input tags get all)")
            @RequestParam(required = false) List<String> tags
    ) {
        if (tags == null || tags.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(
                    ecoNewsService.findAll(page));
        }
        return ResponseEntity.status(HttpStatus.OK).body(ecoNewsService.find(page, tags));
    }

    /**
     * Method for getting three eco news for recommendations widget.
     *
     * @return list of three recommended {@link EcoNewsDto} instances.
     * @author Yurii Zhurakovskyi.
     */
    @ApiOperation(value = "Get three recommended eco news.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
    })
    @GetMapping("/recommended")
    public ResponseEntity<List<EcoNewsDto>> getThreeRecommendedEcoNews(
            @RequestParam(required = true) Long openedEcoNewsId

    ) {
        List<EcoNewsDto> threeRecommendedEcoNews = ecoNewsService.getThreeRecommendedEcoNews(openedEcoNewsId);
        return ResponseEntity.status(HttpStatus.OK).body(threeRecommendedEcoNews);
    }

    /**
     * The method which returns all EcoNews {@link Tag}s.
     *
     * @return list of {@link String} (tag's names).
     * @author Kovaliv Taras
     */
    @ApiOperation(value = "Find all eco news tags")
    @GetMapping("/tags/all")
    public ResponseEntity<List<String>> findAllEcoNewsTags() {
        return ResponseEntity.status(HttpStatus.OK).body(tagService.findAllEcoNewsTags());
    }
}
