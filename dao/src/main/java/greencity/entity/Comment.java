package greencity.entity;

import greencity.enums.ArticleType;
import greencity.enums.CommentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "comments")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ArticleType articleType;

    @Column(nullable = false)
    private Long articleId;

    @Column(nullable = false)
    @Size(min = 1, max = 8000)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime modifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @Builder.Default
    @OneToMany(mappedBy = "parentComment", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Transient
    @Builder.Default
    private boolean currentUserLiked = false;

    @Transient
    @Builder.Default
    private boolean currentUserDisliked = false;

    @ManyToMany
    @JoinTable(
        name = "comments_users_likes",
        joinColumns = @JoinColumn(name = "comment_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> usersLiked = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "comments_users_dislikes",
        joinColumns = @JoinColumn(name = "comment_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> usersDisliked = new HashSet<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CommentStatus status;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL)
    private List<CommentImages> additionalImages = new ArrayList<>();
}
