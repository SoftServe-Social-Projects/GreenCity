package greencity.dto.tag;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TagUaEnDto {
    private Long id;
    private String nameUa;
    private String nameEn;
}
