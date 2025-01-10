package greencity.dto.breaktime;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"startTime", "endTime"})
public class BreakTimeDto {
    @NotNull
    @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING)
    @Schema(type = "java.lang.String")
    private LocalTime startTime;

    @NotNull
    @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING)
    @Schema(type = "java.lang.String")
    private LocalTime endTime;
}
