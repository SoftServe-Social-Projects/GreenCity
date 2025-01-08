package greencity.dto.placecomment;

import greencity.constant.ServiceValidationConstants;
import greencity.dto.photo.PhotoAddDto;
import greencity.dto.rate.EstimateAddDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceCommentRequestDto {
    @NotBlank
    @Length(min = ServiceValidationConstants.COMMENT_MIN_LENGTH, max = ServiceValidationConstants.COMMENT_MAX_LENGTH)
    private String text;
    @Valid
    private EstimateAddDto estimate;
    @Valid
    @Size(max = 3, message = ServiceValidationConstants.BAD_PHOTO_LIST_REQUEST)
    private List<PhotoAddDto> photos;
}
