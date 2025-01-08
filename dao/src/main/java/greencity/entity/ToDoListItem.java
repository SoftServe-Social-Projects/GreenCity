package greencity.entity;

import greencity.entity.localization.ToDoListItemTranslation;
import java.util.List;
import java.util.Set;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.OneToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(
    exclude = {"userToDoListItems"})
@EqualsAndHashCode(exclude = {"userToDoListItems", "translations"})
@Table(name = "to_do_list_items")
@Builder
public class ToDoListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "toDoListItem", fetch = FetchType.LAZY)
    private List<UserToDoListItem> userToDoListItems;

    @ManyToMany(mappedBy = "toDoListItems", fetch = FetchType.LAZY)
    private Set<Habit> habits;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "toDoListItem", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<ToDoListItemTranslation> translations;
}
