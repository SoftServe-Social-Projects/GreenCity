package greencity.dto.commitinfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for error response.
 */
@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommitInfoErrorDto implements CommitInfoDto {
    private String error;
}
