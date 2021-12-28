package greencity.entity;

import greencity.enums.ShoppingListItemStatus;
import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = {"user", "dateCompleted"})
@EqualsAndHashCode(exclude = "dateCompleted")
@Table(name = "custom_shopping_list_items")
@Builder
public class CustomShoppingListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Habit habit;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ShoppingListItemStatus status = ShoppingListItemStatus.ACTIVE;

    @Column
    @DateTimeFormat(pattern = "yyyy-MM-dd-HH-mm-ss.zzz")
    private LocalDateTime dateCompleted;
}
