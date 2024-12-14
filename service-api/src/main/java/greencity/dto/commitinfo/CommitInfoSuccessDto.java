package greencity.dto.commitinfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for successful commit information response.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommitInfoSuccessDto extends CommitInfoDto {
    private String commitHash;
    private String commitDate;
}
