package greencity.webcontroller;

import greencity.annotations.CurrentUser;
import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.PageableDetailedDto;
import greencity.dto.user.UserFilterDto;
import greencity.dto.genericresponse.GenericResponseDto;
import greencity.dto.user.UserFilterDtoRequest;
import greencity.dto.user.UserFilterDtoResponse;
import greencity.dto.user.UserManagementDto;
import greencity.dto.user.UserManagementVO;
import greencity.dto.user.UserManagementViewDto;
import greencity.dto.user.UserVO;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.service.FilterService;
import greencity.service.HabitAssignService;
import greencity.service.UserService;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.client.HttpClientErrorException;
import static greencity.dto.genericresponse.GenericResponseDto.buildGenericResponseDto;

@Validated
@Controller
@AllArgsConstructor
@RequestMapping("/management/users")
public class ManagementUserController {
    private final ModelMapper modelMapper;
    private final RestClient restClient;
    private final UserService userService;
    private final HabitAssignService habitAssignService;
    private final FilterService filterService;

    /**
     * Method that returns management page with all {@link UserVO}.
     *
     * @param request  request for searching related data
     * @param model    Model that will be configured and returned to user.
     * @param pageable {@link Pageable}.
     *
     * @return View template path {@link String}.
     *
     * @author Vasyl Zhovnir, Anton Bondar
     */
    @GetMapping
    public String getAllUsers(@ModelAttribute UserFilterDto request, @CurrentUser UserVO currentUser, Model model,
        @Parameter(hidden = true) Pageable pageable) {
        PageableDetailedDto<UserManagementVO> users = userService.getAllUsersByCriteria(request, pageable);
        List<UserFilterDtoResponse> filters = filterService.getAllFilters(currentUser.getId());

        model.addAttribute("request", request);
        model.addAttribute("users", users);
        model.addAttribute("paging", pageable);
        model.addAttribute("filters", filters);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isFirst", users.isFirst());
        model.addAttribute("isLast", users.isLast());
        model.addAttribute("pageNumbers", users.getPageNumbers());
        model.addAttribute("pageSize", pageable.getPageSize());
        model.addAttribute("pageNumber", users.getPageNumber());
        model.addAttribute("sortModel", users.getSortModel());
        model.addAttribute("previousPage", Math.max(0, users.getPageNumber() - 1));
        model.addAttribute("nextPage", Math.min(users.getTotalPages() - 1, users.getPageNumber() + 1));

        return "core/management_user";
    }

    /**
     * Register new user from admin panel.
     *
     * @param userDto dto with info for registering user.
     * @return {@link GenericResponseDto}
     * @author Vasyl Zhovnir
     */
    @PostMapping("/register")
    public ResponseEntity<?> saveUser(@Valid @RequestBody UserManagementDto userDto) {
        try {
            restClient.managementRegisterUser(userDto);
            return ResponseEntity.ok().body(Map.of("message", "User successfully added!"));
        } catch (HttpClientErrorException.BadRequest e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(List.of(Map.of("name", "email", "message", ErrorMessage.USER_WITH_EMAIL_EXIST)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * Method that updates user data.
     *
     * @param userDto dto with updated fields.
     * @return {@link GenericResponseDto}
     * @author Vasyl Zhovnir
     */
    @PutMapping("/")
    @ResponseBody
    public GenericResponseDto updateUser(@Valid @RequestBody UserManagementDto userDto, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            restClient.updateUser(userDto);
        }
        return buildGenericResponseDto(bindingResult);
    }

    /**
     * Method for finding {@link UserVO} by id.
     *
     * @param id of the searched {@link UserVO}.
     * @return dto {@link UserManagementDto} of the {@link UserVO}.
     * @author Vasyl Zhovnir
     */
    @GetMapping("/findById")
    @ResponseBody
    public UserManagementDto findById(@RequestParam("id") Long id) {
        UserVO byId = restClient.findById(id);
        return modelMapper.map(byId, UserManagementDto.class);
    }

    /**
     * Method that finds user's friends {@link UserManagementDto} by given id.
     *
     * @param id {@link Long} - user's id.
     * @return {@link List} of {@link UserManagementDto} instances.
     * @author Markiyan Derevetskyi
     */
    @GetMapping("/{id}/friends")
    @ResponseBody
    public List<UserManagementDto> findFriendsById(@PathVariable Long id) {
        return restClient.findUserFriendsByUserId(id);
    }

    /**
     * Method that change user's Role {@link Role} by given id.
     *
     * @param id   {@link Long} - user's id.
     * @param body map with new user's Role.
     * @author Stepan Tehlivets.
     */
    @PatchMapping("/{id}/role")
    @ResponseBody
    public void changeRole(
        @PathVariable Long id,
        @RequestBody Map<String, String> body) {
        Role role = Role.valueOf(body.get("role"));
        restClient.updateRole(id, role);
    }

    /**
     * Method that change user's Status {@link UserStatus} by given id.
     *
     * @param id   {@link Long} - user's id.
     * @param body map with new user's Status.
     *
     * @author Anton Bondar.
     */
    @PatchMapping("/{id}/status")
    @ResponseBody
    public void changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        UserStatus status = UserStatus.valueOf(body.get("userStatus"));
        restClient.updateStatus(id, status);
    }

    /**
     * Method for setting {@link UserVO}'s status to DEACTIVATED, so the user will
     * not be able to log in into the system.
     *
     * @param id          of the searched {@link UserVO}.
     * @param userReasons {@link List} of {@link String}.
     * @author Vasyl Zhovnir
     */
    @PostMapping("/deactivate")
    public ResponseEntity<ResponseEntity.BodyBuilder> deactivateUser(
        @RequestParam("id") Long id,
        @RequestBody @NotEmpty List<@Size(min = 9) String> userReasons) {
        restClient.deactivateUser(id, userReasons);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for getting {@link String} user language.
     *
     * @param id of the searched {@link UserVO}.
     * @return current user language {@link String}.
     * @author Vlad Pikhotskyi
     */
    @GetMapping("/lang")
    public ResponseEntity<String> getUserLang(@RequestParam("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(restClient.getUserLang(id));
    }

    /**
     * Method for setting {@link UserVO}'s status to ACTIVATE.
     *
     * @param id of the searched {@link UserVO}.
     * @author Vasyl Zhovnir
     */
    @PostMapping("/activate")
    public ResponseEntity<ResponseEntity.BodyBuilder> setActivatedStatus(
        @RequestParam("id") Long id) {
        restClient.setActivatedStatus(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for getting list of {@link String}.
     *
     * @param id        {@link Long} - user's id.
     * @param adminLang {@link String} - current administrator language.
     * @return {@link List} of {@link String} - reasons for deactivation of the
     *         current user.
     * @author Vlad Pikhotskyi
     */
    @GetMapping("/reasons")
    public ResponseEntity<List<String>> getReasonsOfDeactivation(
        @RequestParam("id") Long id,
        @RequestParam("admin") String adminLang) {
        return ResponseEntity.status(HttpStatus.OK).body(restClient.getDeactivationReason(id, adminLang));
    }

    /**
     * Method for setting to a list of {@link UserVO} status DEACTIVATED, so the
     * users will not be able to log in into the system.
     *
     * @param listId {@link List} populated with ids of {@link UserVO} to be
     *               deleted.
     * @return {@link ResponseEntity} containing an array of deactivated user IDs
     *         {@link Long}.
     * @author Vasyl Zhovnir, Anton Bondar
     */
    @PostMapping("/deactivateAll")
    public ResponseEntity<Long[]> deactivateAll(@RequestBody List<Long> listId) {
        return restClient.deactivateAllUsers(listId);
    }

    /**
     * Method accepts request to search users by several values.
     *
     * @param model       {@link Model}
     * @param pageable    {@link Pageable}
     * @param userViewDto {@link UserManagementViewDto} - stores values.
     * @return path to html view.
     */
    @PostMapping("/search")
    public String search(Model model, @Parameter(hidden = true) Pageable pageable, UserManagementViewDto userViewDto) {
        PageableAdvancedDto<UserManagementVO> found = restClient.search(pageable, userViewDto);
        model.addAttribute("users", found);
        model.addAttribute("fields", userViewDto);
        model.addAttribute("paging", pageable);
        return "core/management_user";
    }

    /**
     * Method update to-do item by habitAssign id and toDoListItem id.
     *
     * @param habitId {@link Long} habitAssignId.
     * @param itemId  {@link Long} toDoListItemId.
     */
    @PutMapping(value = "/updateToDoItem/{habitId}/{itemId}")
    @ResponseStatus(value = HttpStatus.OK)
    public void updateToDoItem(@PathVariable("itemId") Long itemId,
        @PathVariable("habitId") Long habitId) {
        habitAssignService.updateToDoItem(habitId, itemId);
    }

    /**
     * Method for creating new filter.
     *
     * @param currentUser current user.
     * @param dto         filter's dto.
     */
    @PostMapping(value = "/filter-save")
    public String saveUserFilter(@CurrentUser UserVO currentUser, UserFilterDtoRequest dto) {
        filterService.save(currentUser.getId(), dto);
        return "redirect:/management/users";
    }

    /**
     * Method for selecting current filter.
     *
     * @param id filter's id.
     * @return return page with filtered users.
     */
    @GetMapping(value = "/select-filter/{id}")
    public String selectFilter(@PathVariable("id") Long id) {
        UserFilterDtoResponse dto = filterService.getFilterById(id);
        return "redirect:/management/users?role=" + dto.getUserRole() + "&query=" + dto.getSearchCriteria() + "&status="
            + dto.getUserStatus() + "&size=20&sort=id,DESC";
    }

    /**
     * Method for deleting filters.
     *
     * @param id user filter {@link Long} filter's id.
     */
    @GetMapping(value = "{id}/delete-filter")
    public String deleteUserFilter(@PathVariable("id") Long id) {
        filterService.deleteFilterById(id);
        return "redirect:/management/users?size=20&sort=id,DESC";
    }
}
