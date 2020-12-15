package greencity.webcontroller;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.genericresponse.GenericResponseDto;
import greencity.dto.language.LanguageDTO;
import greencity.dto.tag.TagPostDto;
import greencity.dto.tag.TagVO;
import greencity.dto.tag.TagViewDto;
import greencity.service.LanguageService;
import greencity.service.TagsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import static greencity.dto.genericresponse.GenericResponseDto.buildGenericResponseDto;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/management/tags")
public class ManagementTagsController {
    private final TagsService tagsService;
    private final LanguageService languageService;

    /**
     * Method accepts request to find all tags from client.
     *
     * @param model    {@link Model}
     * @param pageable {@link Pageable}
     * @param filter   {@link String} - used for filtering tags (not required).
     * @return path to html view {@link String}
     */
    @GetMapping
    public String findAll(Model model, Pageable pageable,
        @RequestParam(required = false) String filter) {
        PageableAdvancedDto<TagVO> tags = tagsService.findAll(pageable, filter);
        List<LanguageDTO> languages = languageService.getAllLanguages();

        model.addAttribute("tags", tags);
        model.addAttribute("languages", languages);

        return "core/management_tags";
    }

    /**
     * Method accepts request to save new tag from client.
     *
     * @param tagPostDto {@link TagPostDto}
     * @return saved tag
     */
    @ResponseBody
    @PostMapping
    public GenericResponseDto save(@Valid @RequestBody TagPostDto tagPostDto,
        BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            tagsService.save(tagPostDto);
        }

        return buildGenericResponseDto(bindingResult);
    }

    /**
     * Method accepts request to find tag by id from client.
     *
     * @param id - {@link Long}
     * @return found tag
     */
    @GetMapping("/{id}")
    public ResponseEntity<TagVO> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(tagsService.findById(id));
    }

    /**
     * Method accepts request to delete all tags by ids from client.
     *
     * @param ids - {@link List} of {@link Long}
     * @return ids of deleted tags - {@link List} of {@link Long}
     */
    @DeleteMapping
    public ResponseEntity<List<Long>> bulkDelete(@RequestBody List<Long> ids) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(tagsService.bulkDelete(ids));
    }

    /**
     * Method accepts request to delete tag by id from client.
     *
     * @param id - {@link Long}
     * @return id of deleted tag - {@link Long}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(tagsService.deleteById(id));
    }

    /**
     * Method accepts request to update tag from client.
     *
     * @param tagPostDto {@link TagPostDto}
     * @param id         {@link Long}
     * @return updated tag
     */
    @PutMapping("/{id}")
    @ResponseBody
    public GenericResponseDto updateTag(@Valid @RequestBody TagPostDto tagPostDto, BindingResult bindingResult,
        @PathVariable Long id) {
        if (!bindingResult.hasErrors()) {
            tagsService.update(tagPostDto, id);
        }

        return buildGenericResponseDto(bindingResult);
    }

    /**
     * Method accepts request to search tags by several values.
     *
     * @param model      {@link Model}
     * @param pageable   {@link Pageable}
     * @param tagViewDto {@link TagViewDto} - stores values.
     * @return path to html view.
     */
    @PostMapping("/search")
    public String search(Model model, @ApiIgnore Pageable pageable, TagViewDto tagViewDto) {
        PageableAdvancedDto<TagVO> foundTags = tagsService.search(pageable, tagViewDto);

        model.addAttribute("tags", foundTags);
        model.addAttribute("languages", languageService.getAllLanguages());
        model.addAttribute("fields", tagViewDto);

        return "core/management_tags";
    }
}
