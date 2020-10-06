package greencity.entity;

import greencity.dto.user.RegistrationStatisticsDtoResponse;
import greencity.entity.enums.EmailNotification;
import greencity.entity.enums.ROLE;
import greencity.entity.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@SqlResultSetMapping(
    name = "monthsStatisticsMapping",
    classes = {
        @ConstructorResult(
            targetClass = RegistrationStatisticsDtoResponse.class,
            columns = {
                @ColumnResult(name = "month", type = Integer.class),
                @ColumnResult(name = "count", type = Long.class)
            }
        )
    }
)
@NamedNativeQuery(name = "User.findAllRegistrationMonths",
    query = "SELECT EXTRACT(MONTH FROM date_of_registration) - 1 as month, count(date_of_registration) FROM users "
        + "WHERE EXTRACT(YEAR from date_of_registration) = EXTRACT(YEAR FROM CURRENT_DATE) "
        + "GROUP BY month",
    resultSetMapping = "monthsStatisticsMapping")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "users")
@EqualsAndHashCode(
    exclude = {"lastVisit", "places", "comments", "verifyEmail", "addedPlaces", "favoritePlaces", "ownSecurity",
        "refreshTokenKey", "verifyEmail", "estimates", "restorePasswordEmail", "addedEcoNews", "addedTipsAndTricks"})
@ToString(
    exclude = {"places", "comments", "verifyEmail", "addedPlaces", "favoritePlaces", "ownSecurity", "refreshTokenKey",
        "verifyEmail", "estimates", "restorePasswordEmail", "addedEcoNews", "addedTipsAndTricks"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(unique = true, nullable = false, length = 50)
    private String email;

    @Enumerated(value = EnumType.ORDINAL)
    @Column(nullable = false)
    private ROLE role;

    @Enumerated(value = EnumType.ORDINAL)
    private UserStatus userStatus;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "HH:mm")
    private LocalDateTime lastVisit;

    @Column(nullable = false)
    private LocalDateTime dateOfRegistration;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<Place> places = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<EcoNewsComment> ecoNewsComments = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<FavoritePlace> favoritePlaces = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    private List<Place> addedPlaces = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    private List<EcoNews> addedEcoNews = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    private List<TipsAndTricks> addedTipsAndTricks = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.PERSIST)
    private OwnSecurity ownSecurity;

    @OneToOne(mappedBy = "user", cascade = CascadeType.PERSIST)
    private VerifyEmail verifyEmail;

    @OneToOne(mappedBy = "user")
    private RestorePasswordEmail restorePasswordEmail;

    @OneToMany(mappedBy = "user")
    private List<Estimate> estimates = new ArrayList<>();
    @Enumerated(value = EnumType.ORDINAL)
    private EmailNotification emailNotification;

    @Column(name = "refresh_token_key", nullable = false)
    private String refreshTokenKey;

    @ManyToMany(mappedBy = "users")
    private List<Habit> habits = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserGoal> userGoals = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<CustomGoal> customGoals = new ArrayList<>();

    @Column(name = "profile_picture")
    private String profilePicturePath;

    @ManyToMany(mappedBy = "usersLiked")
    private Set<EcoNewsComment> ecoNewsCommentsLiked;

    @OneToMany
    @JoinTable(name = "users_friends",
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "friend_id", referencedColumnName = "id"))
    private List<User> userFriends = new ArrayList<>();

    @Column(name = "rating")
    private Double rating;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "city")
    private String city;

    @Column(name = "user_credo")
    private String userCredo;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(name = "social_networks")
    private List<SocialNetwork> socialNetworks;

    @Column(name = "show_location")
    private Boolean showLocation;

    @Column(name = "show_eco_place")
    private Boolean showEcoPlace;

    @Column(name = "show_shopping_list")
    private Boolean showShoppingList;

    @Column(name = "last_activity_time")
    private LocalDateTime lastActivityTime;

    @OneToMany(mappedBy = "user")
    private List<HabitStatus> habitStatuses = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<RatingStatistics> ratingStatistics = new ArrayList<>();
}
