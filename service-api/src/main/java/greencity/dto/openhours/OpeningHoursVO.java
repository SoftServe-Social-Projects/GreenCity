package greencity.dto.openhours;

import greencity.dto.breaktime.BreakTimeVO;
import greencity.dto.place.PlaceVO;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(exclude = {"openTime", "closeTime", "breakTime"})
@Builder
public class OpeningHoursVO {
    private Long id;
    private LocalTime openTime;
    private LocalTime closeTime;
    private DayOfWeek weekDay;
    private BreakTimeVO breakTime;
    private PlaceVO place;
}
