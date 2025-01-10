package greencity.entity;

import java.time.DayOfWeek;
import java.time.LocalTime;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.IntegerJdbcType;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"openTime", "closeTime", "breakTime"})
@Builder
@Table(name = "opening_hours")
public class OpeningHours {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime openTime;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime closeTime;

    @Enumerated
    @JdbcType(IntegerJdbcType.class)
    private DayOfWeek weekDay;

    @OneToOne(cascade = {CascadeType.ALL})
    private BreakTime breakTime;

    @ManyToOne
    private Place place;
}
