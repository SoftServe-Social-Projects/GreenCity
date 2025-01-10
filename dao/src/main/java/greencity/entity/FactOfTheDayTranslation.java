package greencity.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "fact_of_the_day_translations")
@SuperBuilder
@EqualsAndHashCode(callSuper = true, exclude = "factOfTheDay")
@NoArgsConstructor
public class FactOfTheDayTranslation extends Translation {
    @Getter
    @Setter
    @ManyToOne
    private FactOfTheDay factOfTheDay;
}
