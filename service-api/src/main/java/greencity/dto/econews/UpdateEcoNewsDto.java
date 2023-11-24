package greencity.dto.econews;

import greencity.constant.ServiceValidationConstants;
import lombok.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class UpdateEcoNewsDto {
    @NotNull
    @Min(1)
    private Long id;

    @NotEmpty
    @Size(min = 1, max = 170)
    private String title;

    @NotEmpty
    @Size(min = 20, max = 63206)
    private String content;

    private String shortInfo;

    @NotEmpty(message = ServiceValidationConstants.MIN_AMOUNT_OF_TAGS)
    private List<String> tags;

    private String image;

    private String source;
}
