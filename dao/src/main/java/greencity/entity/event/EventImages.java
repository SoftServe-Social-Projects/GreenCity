package greencity.entity.event;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "events_images")
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class EventImages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @NonNull
    private String link;

    @ManyToOne
    private Event event;
}
