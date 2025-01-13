package greencity.dto.achievementcategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
@Builder
@EqualsAndHashCode
public class AchievementCategoryTranslationDto {
    @NotEmpty
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String titleEn;

    @NotEmpty
    private Integer totalQuantity;

    @NotEmpty
    private Integer achieved;
}
