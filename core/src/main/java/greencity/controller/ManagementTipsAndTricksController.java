package greencity.controller;

import greencity.dto.tipsandtricks.TipsAndTricksDtoResponse;
import greencity.entity.TipsAndTricks;
import greencity.service.TipsAndTricksService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
@RequestMapping("/management/tipsandtricks")
public class ManagementTipsAndTricksController {
    private TipsAndTricksService tipsAndTricksService;

    /**
     * Method for getting all tips & tricks by page.
     *
     * @param model Model that will be configured.
     * @param page  Page index you want to retrieve.
     * @param size  Number of records per page.
     * @return View template path {@link String}.
     * @author Yurii Savchenko
     */
    @GetMapping
    public String findAll(Model model,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "5") int size) {
        Pageable paging = PageRequest.of(page, size);
        List<TipsAndTricksDtoResponse> tipsAndTricksDtoResponses =
            tipsAndTricksService.findAll(paging).getPage();

        model.addAttribute("tipsAndTricks", tipsAndTricksDtoResponses);
        return "core/management_tips_and_tricks";
    }

    /**
     * Method for getting tips & tricks by id.
     *
     * @param model Model that will be configured.
     * @return View template path {@link String}.
     */
    @GetMapping("/{id}")
    public String getTipsAndTricksById(@PathVariable Long id, Model model) {
        model.addAttribute("tipAndTrick", tipsAndTricksService.findDtoById(id));
        return "core/management_tip_and_trick";
    }

    /**
     * Method for deleting {@link TipsAndTricks} by its id.
     *
     * @param id {@link TipsAndTricks} which will be deleted.
     * @return View template path {@link String}.
     */
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        tipsAndTricksService.delete(id);
        return "redirect:/management/tipsandtricks";
    }
}
