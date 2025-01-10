package greencity.controller;

import greencity.constant.HttpStatuses;
import greencity.dto.socialnetwork.SocialNetworkImageVO;
import greencity.service.SocialNetworkImageService;
import greencity.service.SocialNetworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/social-networks")
public class SocialNetworkController {
    private final SocialNetworkImageService socialNetworkImageService;
    private final SocialNetworkService socialNetworkService;

    /**
     * Method for finding social network image.
     *
     * @param url social network url.
     * @return {@link SocialNetworkImageVO}
     */
    @Operation(summary = "Get social network image")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
    })
    @GetMapping("/image")
    public ResponseEntity<SocialNetworkImageVO> getSocialNetworkImageByUrl(@RequestParam String url) {
        return ResponseEntity.status(HttpStatus.OK).body(socialNetworkImageService.getSocialNetworkImageByUrl(url));
    }

    /**
     * Method for delete social network by id.
     *
     * @param id social network url.
     * @return {@link SocialNetworkImageVO}
     */
    @Operation(summary = "Delete social network by id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST,
            content = @Content(examples = @ExampleObject(HttpStatuses.BAD_REQUEST))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(examples = @ExampleObject(HttpStatuses.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN,
            content = @Content(examples = @ExampleObject(HttpStatuses.FORBIDDEN))),
    })
    @DeleteMapping("")
    public ResponseEntity<Long> deleteSocialNetwork(@RequestParam Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(socialNetworkService.delete(id));
    }
}
