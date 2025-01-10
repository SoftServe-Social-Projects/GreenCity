package greencity.dto.openhours;

import greencity.dto.breaktime.BreakTimeDto;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OpenHoursDto implements Comparable<OpenHoursDto> {
    private Long id;
    private LocalTime openTime;
    private LocalTime closeTime;
    private DayOfWeek weekDay;
    private BreakTimeDto breakTime;

    @Override
    public int compareTo(OpenHoursDto openHoursDto) {
        return this.weekDay.compareTo(openHoursDto.weekDay);
    }
}
