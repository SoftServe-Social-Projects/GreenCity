package greencity.entity;

import greencity.enums.Status;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "rating_points")
public class RatingPoints {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "int default 1")
    private int points;

    @Column(name = "status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Status status = Status.ACTIVE;

    /**
     * Constructor that accepts only the name and sets points depending on the name.
     * If the name starts with "UNDO_", the points will be set to -1; otherwise,
     * points will be set to 1.
     *
     * @param name the name of the RatingPoints entity
     */
    public RatingPoints(String name) {
        this.name = name;
        this.points = name.startsWith("UNDO_") ? -1 : 1;
    }
}
