package greencity.dto.commitinfo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for commit information response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CommitInfoDto {
    @NotNull
    private String commitHash;
    @NotNull
    private String commitDate;
}
