package greencity.entity;

import greencity.entity.localization.AdviceTranslation;
import java.util.List;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "advices")
public class Advice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = {CascadeType.REMOVE, CascadeType.REFRESH}, mappedBy = "advice", fetch = FetchType.LAZY)
    private List<AdviceTranslation> translations;

    @ManyToOne
    private HabitDictionary habitDictionary;
}
