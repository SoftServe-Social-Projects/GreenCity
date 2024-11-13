package greencity.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import greencity.enums.UserToDoListItemStatus;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "user_to_do_list")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserToDoListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private HabitAssign habitAssign;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private Boolean isCustomItem;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private UserToDoListItemStatus status = UserToDoListItemStatus.INPROGRESS;

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
        UserToDoListItem that = (UserToDoListItem) o;
        return Objects.equals(id, that.id) && Objects.equals(habitAssign, that.habitAssign)
            && Objects.equals(targetId, that.targetId) && Objects.equals(isCustomItem, that.isCustomItem)
            && status == that.status && Objects.equals(dateCompleted, that.dateCompleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, habitAssign, targetId, isCustomItem, status, dateCompleted);
    }
}
