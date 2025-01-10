package greencity.dto.place;

import greencity.dto.category.CategoryVO;
import greencity.dto.descountvalue.DiscountValueVO;
import greencity.dto.location.LocationVO;
import greencity.dto.openhours.OpeningHoursVO;
import greencity.dto.photo.PhotoVO;
import greencity.dto.user.UserVO;
import greencity.enums.PlaceStatus;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(
    exclude = {"discountValues", "author", "openingHoursList", "photos",
        "location", "category", "status"})
@Builder
public class PlaceVO {
    private Long id;
    private String description;
    private String email;
    private ZonedDateTime modifiedDate;
    private String name;
    private String phone;
    @Builder.Default
    private PlaceStatus status = PlaceStatus.PROPOSED;
    private CategoryVO category;
    @Builder.Default
    private List<PhotoVO> photos = new ArrayList<>();
    @Builder.Default
    private Set<DiscountValueVO> discountValues = new HashSet<>();
    @Builder.Default
    private Set<OpeningHoursVO> openingHoursList = new HashSet<>();
    private LocationVO location;
    private UserVO author;
}
