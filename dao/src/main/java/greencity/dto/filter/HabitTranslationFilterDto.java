package greencity.dto.filter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HabitTranslationFilterDto {
    @NotNull
    private Long userId;
    @NotBlank
    private String languageCode;
    private List<String> tags;
    private List<Integer> complexities;
    private Boolean isCustom;
}
