package greencity.dto.openhours;

import com.fasterxml.jackson.annotation.JsonFormat;
import greencity.dto.breaktime.BreakTimeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"openTime", "closeTime"})
public class OpeningHoursDto {
    @NotNull
    @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING)
    @Schema(type = "java.lang.String")
    private LocalTime openTime;

    @NotNull
    @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING)
    @Schema(type = "java.lang.String")
    private LocalTime closeTime;

    @NotNull
    private DayOfWeek weekDay;

    private BreakTimeDto breakTime;
}
