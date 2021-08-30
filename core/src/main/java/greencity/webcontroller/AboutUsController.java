package greencity.webcontroller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/management")
@RequiredArgsConstructor
public class AboutUsController {
    /**
     * Show About Us page.
     *
     * @param model model
     * @return {@link String} path to the template part.
     * @author Vira Maksymets
     */
    @GetMapping("/aboutus")
    public String aboutUs(Model model) {
        return "core/about_us";
    }
}
