package greencity.dto.breaktime;

import java.time.LocalTime;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"startTime", "endTime"})
public class BreakTimeDto {
    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;
}
