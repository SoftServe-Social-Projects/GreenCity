package greencity.dto.place;

import greencity.constant.ServiceValidationConstants;
import greencity.dto.category.CategoryDto;
import java.io.Serializable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class PlaceNotificationDto implements Serializable {
    @NotBlank
    @Size(max = ServiceValidationConstants.PLACE_NAME_MAX_LENGTH)
    private String name;

    @Valid
    private CategoryDto category;
}
