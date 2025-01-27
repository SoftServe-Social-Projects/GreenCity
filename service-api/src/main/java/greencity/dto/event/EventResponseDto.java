package greencity.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import greencity.enums.EventType;
import jakarta.validation.constraints.Max;
import lombok.Builder;
import lombok.Data;
import org.springframework.lang.Nullable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class EventResponseDto {
    private Long id;

    private EventInformationDto eventInformation;

    private EventAuthorDto organizer;

    private LocalDate creationDate;

    private Boolean isOpen;

    @Max(7)
    private List<EventDateInformationDto> dates;

    @Nullable
    private String titleImage;

    @Nullable
    @Max(4)
    private List<String> additionalImages;

    private EventType type;

    @JsonProperty("isSubscribed")
    private boolean isSubscribed;

    @JsonProperty("isFavorite")
    private boolean isFavorite;

    private Boolean isRelevant;

    private Integer likes;

    private Integer dislikes;

    private Integer countComments;

    @JsonProperty("isOrganizedByFriend")
    private boolean isOrganizedByFriend;

    private Double eventRate;

    private Double currentUserGrade;
}
