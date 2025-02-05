package greencity.mapping.events;

import greencity.dto.event.AddressDto;
import greencity.dto.event.EventAuthorDto;
import greencity.dto.event.EventDateInformationDto;
import greencity.dto.event.EventInformationDto;
import greencity.dto.event.EventResponseDto;
import greencity.dto.tag.TagUaEnDto;
import greencity.entity.User;
import greencity.entity.event.Event;
import greencity.entity.event.EventImages;
import greencity.entity.localization.TagTranslation;
import greencity.service.CommentService;
import greencity.utils.EventUtils;
import org.modelmapper.AbstractConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

/**
 * Mapper class for converting {@link Event} into {@link EventResponseDto}.
 */
@Component
public class EventResponseDtoMapper extends AbstractConverter<Event, EventResponseDto> {
    private static final String LANGUAGE_UA = "ua";
    private static final String LANGUAGE_EN = "en";
    private static final int MAX_ADDITIONAL_IMAGES = 4;

    private final CommentService commentService;

    @Autowired
    public EventResponseDtoMapper(@Lazy CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Converts an {@link Event} into {@link EventResponseDto}.
     *
     * @param event the source object to convert
     * @return the converted {@link EventResponseDto} object
     */
    @Override
    protected EventResponseDto convert(Event event) {
        EventInformationDto eventInformation = new EventInformationDto(
            event.getTitle(),
            event.getDescription(),
            event.getTags().stream()
                .map(tag -> TagUaEnDto.builder()
                    .id(tag.getId())
                    .nameUa(tag.getTagTranslations().stream()
                        .filter(tt -> LANGUAGE_UA.equals(tt.getLanguage().getCode()))
                        .findFirst()
                        .map(TagTranslation::getName)
                        .orElse(null))
                    .nameEn(tag.getTagTranslations().stream()
                        .filter(tt -> LANGUAGE_EN.equals(tt.getLanguage().getCode()))
                        .findFirst()
                        .map(TagTranslation::getName)
                        .orElse(null))
                    .build())
                .toList());

        User organizer = event.getOrganizer();

        List<EventDateInformationDto> dateInformation = event.getDates().stream()
            .map(date -> new EventDateInformationDto(
                date.getId(),
                Optional.ofNullable(date.getAddress())
                    .map(address -> AddressDto.builder()
                        .latitude(address.getLatitude())
                        .longitude(address.getLongitude())
                        .streetEn(address.getStreetEn())
                        .streetUa(address.getStreetUa())
                        .houseNumber(address.getHouseNumber())
                        .cityEn(address.getCityEn())
                        .cityUa(address.getCityUa())
                        .regionEn(address.getRegionEn())
                        .regionUa(address.getRegionUa())
                        .countryEn(address.getCountryEn())
                        .countryUa(address.getCountryUa())
                        .formattedAddressEn(address.getFormattedAddressEn())
                        .formattedAddressUa(address.getFormattedAddressUa())
                        .build())
                    .orElse(null),
                date.getStartDate(),
                date.getFinishDate(),
                date.getOnlineLink()))
            .toList();

        return new EventResponseDto(
            event.getId(),
            eventInformation,
            new EventAuthorDto(
                organizer.getId(),
                organizer.getName(),
                organizer.getEventOrganizerRating(),
                organizer.getEmail()),
            event.getCreationDate(),
            event.isOpen(),
            dateInformation,
            event.getTitleImage(),
            event.getAdditionalImages().stream()
                .map(EventImages::getLink)
                .limit(MAX_ADDITIONAL_IMAGES)
                .toList(),
            event.getType(),
            false,
            false,
            EventUtils.isRelevant(event.getDates()),
            event.getUsersLikedEvents().size(),
            event.getUsersDislikedEvents().size(),
            commentService.countCommentsForEvent(event.getId()),
            false,
            EventUtils.calculateEventRate(event.getEventGrades()),
            null);
    }
}
