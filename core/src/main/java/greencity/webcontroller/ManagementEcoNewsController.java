package greencity.webcontroller;

import greencity.annotations.ApiLocale;
import greencity.annotations.CurrentUser;
import greencity.annotations.ImageValidation;
import greencity.annotations.ValidLanguage;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.econews.*;
import greencity.dto.factoftheday.FactOfTheDayTranslationVO;
import greencity.dto.genericresponse.GenericResponseDto;
import greencity.dto.tag.TagDto;
import greencity.dto.user.UserVO;
import greencity.service.EcoNewsService;
import greencity.service.TagsService;
import java.util.Set;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import static greencity.dto.genericresponse.GenericResponseDto.buildGenericResponseDto;

@Controller
@RequiredArgsConstructor
@RequestMapping("/management/eco-news")
public class ManagementEcoNewsController {
    private final EcoNewsService ecoNewsService;
    private final TagsService tagsService;

    /**
     * Method that returns management page with all {@link EcoNewsVO}.
     *
     * @param model    Model that will be configured and returned to user.
     * @param pageable {@link Pageable}.
     * @return View template path {@link String}.
     */
    @ApiLocale
    @GetMapping
    public String getAllEcoNews(@RequestParam(required = false, name = "query") String query,
        Model model,
        @Parameter(hidden = true) Pageable pageable,
        EcoNewsViewDto ecoNewsViewDto,
        @Parameter(hidden = true) Locale locale) {
        PageableAdvancedDto<EcoNewsDto> allEcoNews = ecoNewsService
            .getFilteredDataForManagementByPage(query, pageable, ecoNewsViewDto, locale);
        model.addAttribute("pageable", allEcoNews);
        if (!ecoNewsViewDto.isEmpty()) {
            model.addAttribute("fields", ecoNewsViewDto);
        } else {
            model.addAttribute("fields", new EcoNewsViewDto());
        }
        if (query != null && !query.isEmpty()) {
            model.addAttribute("query", query);
        }
        Sort sort = pageable.getSort();
        StringBuilder orderUrl = new StringBuilder();
        if (!sort.isEmpty()) {
            boolean isFirstSortProperty = true;
            for (Sort.Order order : sort) {
                if (isFirstSortProperty) {
                    orderUrl.append(order.getProperty()).append(",").append(order.getDirection());
                    isFirstSortProperty = false;
                } else {
                    orderUrl.append("&sort=").append(order.getProperty()).append(",").append(order.getDirection());
                }
            }
            model.addAttribute("sortModel", orderUrl.toString());
        }
        model.addAttribute("ecoNewsTag", tagsService.findAllEcoNewsTags(locale.getLanguage()));
        model.addAttribute("pageSize", pageable.getPageSize());
        return "core/management_eco_news";
    }

    /**
     * Method which delete {@link EcoNewsVO} and {@link FactOfTheDayTranslationVO}
     * by given id.
     *
     * @param id of Eco New
     * @return {@link ResponseEntity}
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Long> delete(@RequestParam("id") Long id,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        ecoNewsService.delete(id, user);
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }

    /**
     * Method for deleting {@link EcoNewsVO} by given id.
     *
     * @param listId list of IDs.
     * @return {@link ResponseEntity}
     */
    @DeleteMapping("/deleteAll")
    public ResponseEntity<List<Long>> deleteAll(@RequestBody List<Long> listId) {
        ecoNewsService.deleteAll(listId);
        return ResponseEntity.status(HttpStatus.OK).body(listId);
    }

    /**
     * Method finds {@link EcoNewsVO} with all translations by given id.
     *
     * @param id of {@link EcoNewsVO}.
     * @return {@link EcoNewsDto}.
     */
    @Operation(summary = "Find econews by id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = EcoNewsDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @ApiLocale
    @GetMapping("/find/{id}")
    public ResponseEntity<EcoNewsDto> getEcoNewsById(@PathVariable Long id,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ecoNewsService.findDtoByIdAndLanguage(id, locale.getLanguage()));
    }

    /**
     * Returns management page with single {@link EcoNewsVO}.
     *
     * @param id of {@link EcoNewsVO}.
     * @return {@link EcoNewsDto}.
     */
    @Operation(summary = "Find econew's page by id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = EcoNewsDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @ApiLocale
    @GetMapping("/{id}")
    public String getEcoNewsPage(@PathVariable("id") Long id,
        @Parameter(hidden = true) Locale locale, Model model) {
        EcoNewsDto econew = ecoNewsService.findDtoByIdAndLanguage(id, locale.getLanguage());
        model.addAttribute("econew", econew);
        ZonedDateTime time = econew.getCreationDate();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM dd , yyyy");
        model.addAttribute("time", time.format(format));
        model.addAttribute("ecoNewsTag", tagsService.findAllEcoNewsTags("en"));
        return "core/management_eco_new";
    }

    /**
     * Method for getting all econews tag.
     *
     * @return {@link TagDto} instance.
     */
    @GetMapping("/tags")
    public ResponseEntity<List<TagDto>> getAllEcoNewsTag() {
        return ResponseEntity.status(HttpStatus.OK).body(tagsService.findAllEcoNewsTags("en"));
    }

    /**
     * Method for creating {@link EcoNewsVO}.
     *
     * @param addEcoNewsDtoRequest dto for {@link EcoNewsVO} entity.
     * @param file                 of {@link MultipartFile}
     * @return {@link GenericResponseDto} with of operation and errors fields.
     */
    @Operation(summary = "Save EcoNews.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = GenericResponseDto.class))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @ResponseBody
    @PostMapping("/save")
    public GenericResponseDto saveEcoNews(@Valid @RequestPart AddEcoNewsDtoRequest addEcoNewsDtoRequest,
        BindingResult bindingResult,
        @ImageValidation @RequestParam(required = false, name = "file") MultipartFile file,
        @Parameter(hidden = true) Principal principal) {
        if (!bindingResult.hasErrors()) {
            ecoNewsService.save(addEcoNewsDtoRequest, file, principal.getName());
        }
        return buildGenericResponseDto(bindingResult);
    }

    /**
     * Method which updates {@link EcoNewsVO}.
     *
     * @param ecoNewsDtoManagement of {@link EcoNewsDtoManagement}.
     * @param file                 of {@link MultipartFile}.
     * @return {@link GenericResponseDto} with of operation and errors fields.
     */
    @Operation(summary = "Update Econews.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @ResponseBody
    @PutMapping("/")
    public GenericResponseDto update(@Valid @RequestPart EcoNewsDtoManagement ecoNewsDtoManagement,
        BindingResult bindingResult,
        @ImageValidation @RequestPart(required = false, name = "file") MultipartFile file) {
        if (!bindingResult.hasErrors()) {
            ecoNewsService.update(ecoNewsDtoManagement, file);
        }
        return buildGenericResponseDto(bindingResult);
    }

    /**
     * Method for getting list of users who liked post by post id.
     *
     * @return Set of {@link UserVO} instances.
     */
    @Operation(summary = "Get list of users who liked the post by post id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @ApiLocale
    @GetMapping("/{id}/likes")
    @ResponseBody
    public Set<UserVO> getLikesByEcoNewsId(@PathVariable Long id) {
        return ecoNewsService.findUsersWhoLikedPost(id);
    }

    /**
     * Method for getting list of users who disliked post by post id.
     *
     * @return Set of {@link UserVO} instances.
     */
    @Operation(summary = "Get list of users who disliked the post by post id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @ApiLocale
    @GetMapping("/{id}/dislikes")
    @ResponseBody
    public Set<UserVO> getDislikesByEcoNewsId(@PathVariable Long id) {
        return ecoNewsService.findUsersWhoDislikedPost(id);
    }

    /**
     * Method which hide {@link EcoNewsVO} by given id.
     *
     * @param id of Eco New
     * @return {@link ResponseEntity}
     */
    @PatchMapping("/hide")
    public ResponseEntity<Long> hide(@RequestParam("id") Long id,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        ecoNewsService.setHiddenValue(id, user, true);
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }

    /**
     * Method which make visible {@link EcoNewsVO} by given id.
     *
     * @param id of Eco New
     * @return {@link ResponseEntity}
     */
    @PatchMapping("/show")
    public ResponseEntity<Long> show(@RequestParam("id") Long id,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        ecoNewsService.setHiddenValue(id, user, false);
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }
}
