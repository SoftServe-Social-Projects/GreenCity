package greencity.controller;

import greencity.constant.HttpStatuses;
import greencity.dto.specification.SpecificationNameDto;
import greencity.service.SpecificationService;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/specification")
@AllArgsConstructor
public class SpecificationController {
    private SpecificationService specificationService;

    /**
     * The method which returns all {@code SpecificationNameDto}.
     *
     * @return list of {@code SpecificationNameDto}.
     * @author Kateryna Horokh
     */
    @Operation(summary = "Find all tags by type and language code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK)
    })
    @GetMapping
    public ResponseEntity<List<SpecificationNameDto>> findAllSpecification() {
        return ResponseEntity.status(HttpStatus.OK).body(specificationService.findAllSpecificationDto());
    }
}
