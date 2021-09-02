package greencity.webcontroller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class ManagementController {
    @Value("${greencityuser.server.address}")
    private String greenCityUserServerAddress;

    /**
     * Returns index page.
     *
     * @return model
     * @author Dovganyuk Taras
     */

    @GetMapping("/management")
    public String goToIndex() {
        return "core/index";
    }

    /**
     * Redirect to logIn page.
     *
     * @author Ihor Volianskyi
     */

    @GetMapping("/")
    public String redirectLogin() {
        return "redirect:/management/login";
    }

    /**
     * Redirect to logIn page on GreenCityUser or redirect to management page if you
     * already sign in.
     *
     * @author Ihor Volianskyi
     */

    @GetMapping("/management/login")
    public String login() {
        boolean anonymousUser =
            SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().equals("anonymousUser");
        if (anonymousUser) {
            return "redirect:" + greenCityUserServerAddress + "/management/login";
        } else {
            return "redirect:/management";
        }
    }
}
