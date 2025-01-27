package greencity.webcontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/management")
public class AboutUsController {
    /**
     * Show About Us page.
     *
     * @return {@link String} path to the template part.
     * @author Vira Maksymets
     */
    @GetMapping("/")
    public String aboutUs() {
        return "core/about_us";
    }
}
