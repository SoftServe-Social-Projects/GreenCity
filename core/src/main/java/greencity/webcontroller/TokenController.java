package greencity.webcontroller;

import greencity.annotations.ValidAccessToken;
import greencity.constant.HttpStatuses;
import greencity.security.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
@RequestMapping("/token")
public class TokenController {
    private final TokenService tokenService;

    /**
     * Method that gets access token from client and set it in cookies.
     *
     * @param accessToken {@link String}
     * @param response    {@link HttpServletResponse}
     * @return html view of management page.
     */
    @Operation(summary = "Pass token to cookies")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST)
    })
    @GetMapping
    public String passTokenToCookies(@RequestParam @ValidAccessToken String accessToken, HttpServletResponse response) {
        tokenService.passTokenToCookies(accessToken, response);
        return "redirect:/management";
    }
}
