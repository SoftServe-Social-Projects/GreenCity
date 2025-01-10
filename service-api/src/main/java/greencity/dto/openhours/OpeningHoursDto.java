package greencity.dto.openhours;

import greencity.dto.breaktime.BreakTimeDto;
import java.time.DayOfWeek;
import java.time.LocalTime;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Data;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"openTime", "closeTime"})
public class OpeningHoursDto {
    @NotNull
    private LocalTime openTime;

    @NotNull
    private LocalTime closeTime;

    @NotNull
    private DayOfWeek weekDay;

    private BreakTimeDto breakTime;
}
