package greencity.entity;

import greencity.enums.FactOfDayStatus;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "habit_fact_translations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitFactTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Language language;

    @Enumerated(value = EnumType.ORDINAL)
    private FactOfDayStatus factOfDayStatus;

    @ManyToOne
    private HabitFact habitFact;

    @Column(nullable = false, unique = true, length = 300)
    private String content;
}
