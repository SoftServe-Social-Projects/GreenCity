package greencity.dto.todolistitem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class CustomToDoListItemResponseDto {
    @Min(1)
    private Long id;
    @NotEmpty
    private String text;
    @NotEmpty
    private String status;
    private boolean isDefault;
}
