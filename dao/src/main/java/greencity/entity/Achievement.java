package greencity.entity;

import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(
    exclude = {"userAchievements"})
@Table(name = "achievements")
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, length = 300)
    private String name;
    @Column(nullable = false, length = 300)
    private String nameEng;

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "achievement", fetch = FetchType.LAZY)
    private List<UserAchievement> userAchievements;

    @ManyToOne
    private AchievementCategory achievementCategory;

    @Column(nullable = false)
    private Integer condition;
}
