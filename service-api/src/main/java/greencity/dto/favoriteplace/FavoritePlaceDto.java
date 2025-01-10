package greencity.dto.favoriteplace;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class FavoritePlaceDto {
    @NotBlank
    @Length(max = 30)
    private String name;
    private Long placeId;
}
