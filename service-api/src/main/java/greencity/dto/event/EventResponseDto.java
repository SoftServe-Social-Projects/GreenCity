package greencity.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import greencity.enums.EventType;
import jakarta.validation.constraints.Max;
import org.springframework.lang.Nullable;
import java.time.LocalDate;
import java.util.List;

public record EventResponseDto(
    Long id,
    EventInformationDto eventInformation,
    EventAuthorDto organizer,
    LocalDate creationDate,
    Boolean isOpen,
    @Max(7) List<EventDateInformationDto> dates,
    @Nullable String titleImage,
    @Nullable @Max(4) List<String> additionalImages,
    EventType type,
    @JsonProperty("isSubscribed") boolean isSubscribed,
    @JsonProperty("isFavorite") boolean isFavorite,
    Boolean isRelevant,
    Integer likes,
    Integer dislikes,
    Integer countComments,
    @JsonProperty("isOrganizedByFriend") boolean isOrganizedByFriend,
    Double eventRate,
    Integer currentUserGrade) {
}
