package greencity.entity;

import greencity.entity.localization.ToDoListItemTranslation;
import java.util.List;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "languages")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(
    exclude = {"adviceTranslations", "toDoListItemTranslations", "habitTranslations", "factOfTheDayTranslations"})
@ToString(
    exclude = {"adviceTranslations", "toDoListItemTranslations", "habitTranslations", "factOfTheDayTranslations"})
@Builder
public class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 35)
    private String code;

    @OneToMany(mappedBy = "language", fetch = FetchType.LAZY)
    private List<HabitTranslation> habitTranslations;

    @OneToMany(mappedBy = "language", fetch = FetchType.LAZY)
    private List<ToDoListItemTranslation> toDoListItemTranslations;

    @OneToMany(mappedBy = "language", fetch = FetchType.LAZY)
    private List<FactOfTheDayTranslation> factOfTheDayTranslations;
}
