package greencity.dto.econews;

import greencity.constant.ServiceValidationConstants;
import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AddEcoNewsDtoRequest {
    @NotEmpty
    @Size(min = 1, max = 170)
    private String title;

    @NotEmpty
    @Size(min = 20, max = 63206)
    private String text;

    @NotEmpty(message = ServiceValidationConstants.MIN_AMOUNT_OF_TAGS)
    private List<String> tags;

    private String source;

    private String shortInfo;
}
