package greencity.webcontroller;

import greencity.annotations.CurrentUser;
import greencity.annotations.ValidLanguage;
import greencity.dto.econews.EcoNewsDto;
import greencity.dto.habit.HabitAssignDto;
import greencity.dto.place.PlaceVO;
import greencity.dto.user.UserVO;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.service.*;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Controller
@AllArgsConstructor
@RequestMapping("/management/users/{id}")
public class ManagementUserPersonalPageController {
    private final HabitAssignService habitAssignService;
    private final EcoNewsService ecoNewsService;
    private final PlaceService placeService;
    private final UserService userService;

    /**
     * Method that returns management page of a {@link UserVO}.
     *
     * @param id    Path variable - id of user
     * @param model Model that will be configured and returned to user.
     *
     * @return View template path {@link String}.
     */
    @GetMapping
    public String getUserById(
        Model model,
        @PathVariable Long id,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        UserVO user = userService.findById(id);

        List<HabitAssignDto> acquiredHabits = habitAssignService
            .getAllHabitAssignsByUserIdAndStatusAcquired(id, locale.getLanguage());
        List<HabitAssignDto> inProgressHabits = habitAssignService
            .findInprogressHabitAssignsOnDateContent(id, LocalDate.now(), locale.getLanguage());
        List<HabitAssignDto> cancelledHabits = habitAssignService
            .getAllHabitAssignsByUserIdAndCancelledStatus(id, locale.getLanguage());
        List<HabitAssignDto> customHabits = habitAssignService
            .getAllCustomHabitAssignsByUserId(id, locale.getLanguage());
        List<EcoNewsDto> publishedEcoNews = ecoNewsService.getAllByUser(user);
        List<PlaceVO> createdEcoPlaces = placeService.getAllCreatedPlacesByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("acquiredHabits", acquiredHabits);
        model.addAttribute("inProgressHabits", inProgressHabits);
        model.addAttribute("cancelledHabits", cancelledHabits);
        model.addAttribute("customHabits", customHabits);
        model.addAttribute("publishedEcoNews", publishedEcoNews);
        model.addAttribute("createdEcoPlaces", createdEcoPlaces);

        return "core/management_user_personal_page";
    }

    /**
     * Method that updates status of a {@link UserVO}.
     *
     * @param id          Path variable - id of user
     * @param userStatus  Status that has to be set to user
     * @param currentUser {@link UserVO} of current user
     *
     * @return View template path {@link String}.
     */
    @PostMapping(value = "/updateUserStatus")
    public String updateUserStatus(@PathVariable Long id, @RequestParam(name = "userStatus") String userStatus,
        @CurrentUser UserVO currentUser) {
        UserStatus status = UserStatus.valueOf(userStatus.toUpperCase());
        userService.updateStatus(id, status, currentUser.getEmail());
        return "redirect:/management/users/{id}";
    }

    /**
     * Method that updates role of a {@link UserVO}.
     *
     * @param id          Path variable - id of user
     * @param userRole    Role that has to be set to user
     * @param currentUser {@link UserVO} of current user
     *
     * @return View template path {@link String}.
     */
    @PostMapping(value = "/updateUserRole")
    public String updateUserRole(@PathVariable Long id, @RequestParam(name = "userRole") String userRole,
        @CurrentUser UserVO currentUser) {
        Role role = Role.valueOf("ROLE_" + userRole.toUpperCase());
        userService.updateRole(id, role, currentUser.getEmail());
        return "redirect:/management/users/{id}";
    }
}