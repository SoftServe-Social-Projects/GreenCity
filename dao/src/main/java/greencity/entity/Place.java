package greencity.entity;

import greencity.enums.PlaceStatus;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.IntegerJdbcType;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(
    exclude = {"discountValues", "author", "openingHoursList", "comments", "photos",
        "location", "favoritePlaces", "category", "webPages", "status"})
@ToString(
    exclude = {"discountValues", "author", "openingHoursList", "comments", "photos",
        "location", "favoritePlaces", "category", "webPages", "status"})
@Table(name = "places")
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    private String description;

    @Column(unique = true, length = 15)
    private String phone;

    @Column(unique = true, length = 50)
    private String email;

    @Builder.Default
    @OneToMany(mappedBy = "place", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<PlaceComment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "place", cascade = CascadeType.PERSIST)
    private List<Photo> photos = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "place", cascade = CascadeType.PERSIST)
    private Set<DiscountValue> discountValues = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Location location;

    @Builder.Default
    @OneToMany(mappedBy = "place")
    private List<FavoritePlace> favoritePlaces = new ArrayList<>();

    @Builder.Default
    @ManyToMany(mappedBy = "places")
    private List<WebPage> webPages = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.ALL)
    private Category category;

    @Builder.Default
    @OneToMany(mappedBy = "place")
    private List<Estimate> estimates = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "place", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<OpeningHours> openingHoursList = new HashSet<>();

    @ManyToOne
    private User author;

    @Column(name = "modified_date")
    private ZonedDateTime modifiedDate;

    @Builder.Default
    @Enumerated(value = EnumType.ORDINAL)
    @Column(name = "status", columnDefinition = "int4")
    @JdbcType(IntegerJdbcType.class)
    private PlaceStatus status = PlaceStatus.PROPOSED;
}
