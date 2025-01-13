package greencity.entity;

import java.util.HashSet;
import java.util.Set;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "eco_news")
@ToString(exclude = {"author", "tags"})
@EqualsAndHashCode(exclude = {"author", "tags"})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EcoNews {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private ZonedDateTime creationDate;

    @Column
    private String imagePath;

    @Column
    private String source;

    @Column
    private String shortInfo;

    @ManyToOne
    private User author;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private boolean hidden = false;

    @ManyToMany
    private List<Tag> tags;

    @ManyToMany
    @Builder.Default
    @JoinTable(
        name = "eco_news_users_likes",
        joinColumns = @JoinColumn(name = "eco_news_id"),
        inverseJoinColumns = @JoinColumn(name = "users_id"))
    private Set<User> usersLikedNews = new HashSet<>();

    @ManyToMany
    @Builder.Default
    @JoinTable(
        name = "eco_news_users_dislikes",
        joinColumns = @JoinColumn(name = "eco_news_id"),
        inverseJoinColumns = @JoinColumn(name = "users_id"))
    private Set<User> usersDislikedNews = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @Builder.Default
    @JoinTable(name = "eco_news_followers",
        joinColumns = @JoinColumn(name = "eco_news_id"),
        inverseJoinColumns = @JoinColumn(name = "users_id"))
    private Set<User> followers = new HashSet<>();
}
