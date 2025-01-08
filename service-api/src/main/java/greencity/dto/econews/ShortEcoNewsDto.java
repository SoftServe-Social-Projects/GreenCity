package greencity.dto.econews;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ShortEcoNewsDto {
    private Long ecoNewsId;
    private String imagePath;
    private String title;
    private String text;
}
