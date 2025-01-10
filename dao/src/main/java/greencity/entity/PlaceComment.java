package greencity.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"comments", "photos"})
@EntityListeners(AuditingEntityListener.class)
@Table(name = "place_comments")
public class PlaceComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @ManyToOne
    private User user;

    @ManyToOne
    private Place place;

    @ManyToOne
    private PlaceComment parentComment;

    @Builder.Default
    @OneToMany(mappedBy = "parentComment", cascade = {CascadeType.ALL})
    private List<PlaceComment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "comment", cascade = {CascadeType.ALL})
    private List<Photo> photos = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    private Estimate estimate;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime modifiedDate;
}
