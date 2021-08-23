package greencity.entity;

import greencity.enums.ShoppingListItemStatus;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "user_shopping_list")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = {"user"})
@Builder
public class UserShoppingListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private HabitAssign habitAssign;

    @ManyToOne(fetch = FetchType.LAZY)
    private ShoppingListItem shoppingListItem;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ShoppingListItemStatus status = ShoppingListItemStatus.ACTIVE;

    @DateTimeFormat(pattern = "yyyy-MM-dd-HH-mm-ss.zzz")
    private LocalDateTime dateCompleted;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserShoppingListItem that = (UserShoppingListItem) o;
        return Objects.equals(id, that.id) && Objects.equals(habitAssign, that.habitAssign)
            && Objects.equals(shoppingListItem, that.shoppingListItem) && status == that.status
            && Objects.equals(dateCompleted, that.dateCompleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, habitAssign, shoppingListItem, status, dateCompleted);
    }
}
