package greencity.dto.commitinfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for successful commit information response.
 */
@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommitInfoSuccessDto implements CommitInfoDto {
    private String commitHash;
    private String commitDate;
}
