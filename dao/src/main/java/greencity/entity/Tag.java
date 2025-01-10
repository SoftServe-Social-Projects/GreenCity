package greencity.entity;

import java.util.List;
import java.util.Set;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.OneToMany;
import jakarta.persistence.EnumType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import greencity.entity.event.Event;
import greencity.entity.localization.TagTranslation;
import greencity.enums.TagType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "tags")
@ToString(exclude = {"ecoNews", "habits", "events", "factsOfTheDay"})
@EqualsAndHashCode(exclude = {"ecoNews", "habits", "events", "factsOfTheDay"})
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TagType type;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL)
    private List<TagTranslation> tagTranslations;

    @ManyToMany(mappedBy = "tags")
    private List<EcoNews> ecoNews;

    @ManyToMany(mappedBy = "tags")
    private Set<Habit> habits;

    @ManyToMany(mappedBy = "tags")
    private Set<Event> events;

    @ManyToMany(mappedBy = "tags")
    private Set<FactOfTheDay> factsOfTheDay;
}
