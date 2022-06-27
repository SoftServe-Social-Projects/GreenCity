package greencity.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "filters")
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Filter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private String name;

    @Column
    private String type;

    @Column(columnDefinition = "text")
    private String values;

}
