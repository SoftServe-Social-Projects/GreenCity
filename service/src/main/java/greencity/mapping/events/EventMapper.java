package greencity.mapping.events;

import greencity.dto.event.AddressDto;
import greencity.dto.event.EventDateInformationDto;
import greencity.dto.event.EventInformationDto;
import greencity.dto.event.EventResponseDto;
import greencity.dto.tag.TagUaEnDto;
import greencity.entity.event.Event;
import greencity.entity.event.EventImages;
import greencity.entity.localization.TagTranslation;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class for converting {@link Event} into {@link EventResponseDto}.
 */
@Component
public class EventMapper extends AbstractConverter<Event, EventResponseDto> {
    private static final String LANGUAGE_UA = "ua";
    private static final String LANGUAGE_EN = "en";
    private static final int MAX_ADDITIONAL_IMAGES = 4;

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
            .isOpen(event.isOpen())
            .tags(event.getTags().stream()
                .map(tag -> TagUaEnDto.builder()
                    .id(tag.getId())
                    .nameUa(tag.getTagTranslations().stream()
                        .filter(tt -> LANGUAGE_EN.equals(tt.getLanguage().getCode()))
                        .findFirst()
                        .map(TagTranslation::getName)
                        .orElse(null))
                    .nameEn(tag.getTagTranslations().stream()
                        .filter(tt -> LANGUAGE_UA.equals(tt.getLanguage().getCode()))
                        .findFirst()
                        .map(TagTranslation::getName)
                        .orElse(null))
                    .build())
                .collect(Collectors.toList()))
            .build();

        List<EventDateInformationDto> dateInformation = event.getDates().stream()
            .map(date -> EventDateInformationDto.builder()
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

        String titleImage = event.getTitleImage();
        List<String> additionalImages = event.getAdditionalImages().stream()
            .map(EventImages::getLink)
            .toList();

        return EventResponseDto.builder()
            .eventInformation(eventInformation)
            .dates(dateInformation)
            .titleImage(event.getTitleImage())
            .additionalImages(event.getAdditionalImages().stream()
                .map(EventImages::getLink)
                .limit(MAX_ADDITIONAL_IMAGES)
                .collect(Collectors.toList()))
            .build();
    }
}
