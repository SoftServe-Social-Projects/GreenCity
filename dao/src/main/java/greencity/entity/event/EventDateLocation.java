package greencity.entity.event;

import jakarta.annotation.Nullable;
import lombok.*;
import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "events_dates_locations")
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class EventDateLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(nullable = false)
    @NonNull
    private ZonedDateTime startDate;

    @Column(nullable = false)
    @NonNull
    private ZonedDateTime finishDate;

    @Embedded
    @Nullable
    private Address address;

    @Column
    @Nullable
    private String onlineLink;
}
