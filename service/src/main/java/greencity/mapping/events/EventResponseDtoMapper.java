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
import java.util.stream.Collectors;

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
        EventInformationDto eventInformation = EventInformationDto.builder()
            .title(event.getTitle())
            .description(event.getDescription())
            .tags(event.getTags().stream()
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
                .toList())
            .build();

        User organizer = event.getOrganizer();

        List<EventDateInformationDto> dateInformation = event.getDates().stream()
            .map(date -> EventDateInformationDto.builder()
                .id(date.getId())
                .startDate(date.getStartDate())
                .finishDate(date.getFinishDate())
                .onlineLink(date.getOnlineLink())
                .coordinates(AddressDto.builder()
                    .latitude(date.getAddress().getLatitude())
                    .longitude(date.getAddress().getLongitude())
                    .streetEn(date.getAddress().getStreetEn())
                    .streetUa(date.getAddress().getStreetUa())
                    .houseNumber(date.getAddress().getHouseNumber())
                    .cityEn(date.getAddress().getCityEn())
                    .cityUa(date.getAddress().getCityUa())
                    .regionEn(date.getAddress().getRegionEn())
                    .regionUa(date.getAddress().getRegionUa())
                    .countryEn(date.getAddress().getCountryEn())
                    .countryUa(date.getAddress().getCountryUa())
                    .formattedAddressEn(date.getAddress().getFormattedAddressEn())
                    .formattedAddressUa(date.getAddress().getFormattedAddressUa())
                    .build())
                .build())
            .collect(Collectors.toList());

        return EventResponseDto.builder()
            .id(event.getId())
            .eventInformation(eventInformation)
            .organizer(EventAuthorDto.builder()
                .id(organizer.getId())
                .name(organizer.getName())
                .organizerRating(organizer.getEventOrganizerRating())
                .email(organizer.getEmail())
                .build())
            .creationDate(event.getCreationDate())
            .isOpen(event.isOpen())
            .dates(dateInformation)
            .titleImage(event.getTitleImage())
            .isRelevant(EventUtils.isRelevant(event.getDates()))
            .likes(event.getUsersLikedEvents().size())
            .dislikes(event.getUsersDislikedEvents().size())
            .countComments(commentService.countCommentsForEvent(event.getId()))
            .type(event.getType())
            .eventRate(EventUtils.calculateEventRate(event.getEventGrades()))
            .additionalImages(event.getAdditionalImages().stream()
                .map(EventImages::getLink)
                .limit(MAX_ADDITIONAL_IMAGES)
                .toList())
            .build();
    }
}
