package greencity.service;

import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import greencity.client.RestClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.event.*;
import greencity.dto.tag.TagVO;
import greencity.entity.*;
import greencity.entity.event.Event;
import greencity.entity.event.EventDateLocation;
import greencity.entity.event.EventImages;
import greencity.enums.Role;
import greencity.enums.TagType;
import greencity.exception.exceptions.BadRequestException;
import greencity.repository.EventRepo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepo eventRepo;
    private final ModelMapper modelMapper;
    private final RestClient restClient;
    private final FileService fileService;
    private final TagsService tagService;
    private final GoogleApiService googleApiService;
    private static final String DEFAULT_TITLE_IMAGE_PATH = AppConstant.DEFAULT_HABIT_IMAGE;

    @Override
    public EventDto save(AddEventDtoRequest addEventDtoRequest, String email,
        MultipartFile[] images) {
        addAddressesToLocation(addEventDtoRequest.getDatesLocations());
        Event toSave = modelMapper.map(addEventDtoRequest, Event.class);
        User organizer = modelMapper.map(restClient.findByEmail(email), User.class);
        toSave.setOrganizer(organizer);
        if (images != null && images.length > 0 && images[0] != null) {
            toSave.setTitleImage(fileService.upload(images[0]));
            List<EventImages> eventImages = new ArrayList<>();
            for (int i = 1; i < images.length; i++) {
                if (images[i] != null) {
                    eventImages.add(EventImages.builder().event(toSave).link(fileService.upload(images[i])).build());
                }
            }
            toSave.setAdditionalImages(eventImages);
        } else {
            toSave.setTitleImage(DEFAULT_TITLE_IMAGE_PATH);
        }

        List<TagVO> tagVOs = tagService.findTagsWithAllTranslationsByNamesAndType(
            addEventDtoRequest.getTags(), TagType.EVENT);

        toSave.setTags(modelMapper.map(tagVOs,
            new TypeToken<List<Tag>>() {
            }.getType()));

        return modelMapper.map(eventRepo.save(toSave), EventDto.class);
    }

    @Override
    public void delete(Long eventId, String email) {
        User user = modelMapper.map(restClient.findByEmail(email), User.class);
        Event toDelete = eventRepo.getOne(eventId);
        List<String> eventImages = new ArrayList<>();
        eventImages.add(toDelete.getTitleImage());
        if (toDelete.getAdditionalImages() != null) {
            eventImages
                .addAll(toDelete.getAdditionalImages().stream().map(EventImages::getLink).collect(Collectors.toList()));
        }

        if (toDelete.getOrganizer().getId().equals(user.getId())) {
            deleteImagesFromServer(eventImages);
            eventRepo.delete(toDelete);
        } else {
            throw new BadRequestException(ErrorMessage.NOT_EVENT_ORGANIZER);
        }
    }

    @Override
    public EventDto getEvent(Long eventId) {
        Event event = eventRepo.getOne(eventId);
        return modelMapper.map(event, EventDto.class);
    }

    @Override
    public PageableAdvancedDto<EventDto> getAll(Pageable page) {
        Page<Event> pages = eventRepo.findAllByOrderByIdDesc(page);
        return buildPageableAdvancedDto(pages);
    }

    private PageableAdvancedDto<EventDto> buildPageableAdvancedDto(Page<Event> eventsPage) {
        List<EventDto> eventDtos = eventsPage.stream()
            .map(event -> modelMapper.map(event, EventDto.class))
            .collect(Collectors.toList());

        return new PageableAdvancedDto<>(
            eventDtos,
            eventsPage.getTotalElements(),
            eventsPage.getPageable().getPageNumber(),
            eventsPage.getTotalPages(),
            eventsPage.getNumber(),
            eventsPage.hasPrevious(),
            eventsPage.hasNext(),
            eventsPage.isFirst(),
            eventsPage.isLast());
    }

    @Override
    public void addAttender(Long eventId, String email) {
        Event event = eventRepo.getOne(eventId);
        User currentUser = modelMapper.map(restClient.findByEmail(email), User.class);

        if (event.getAttenders().stream().noneMatch(attender -> attender.getId().equals(currentUser.getId()))) {
            event.getAttenders().add(currentUser);
            eventRepo.save(event);
        } else {
            throw new BadRequestException(ErrorMessage.YOU_ARE_EVENT_ORGANIZER);
        }
    }

    @Override
    public void removeAttender(Long eventId, String email) {
        Event event = eventRepo.getOne(eventId);
        User currentUser = modelMapper.map(restClient.findByEmail(email), User.class);

        event.setAttenders(event.getAttenders().stream().filter(user -> !user.getId().equals(currentUser.getId()))
            .collect(Collectors.toSet()));

        eventRepo.save(event);
    }

    @Override
    public PageableAdvancedDto<EventDto> searchEventsBy(Pageable paging, String query) {
        Page<Event> page = eventRepo.searchEventsBy(paging, query);
        return buildPageableAdvancedDto(page);
    }

    /**
     * {@inheritDoc}
     *
     * @return EventDto
     */
    @Override
    @Transactional
    public EventDto update(UpdateEventDto eventDto, String email, MultipartFile[] images) {
        Event toUpdate = eventRepo.getOne(eventDto.getId());
        User organizer = modelMapper.map(restClient.findByEmail(email), User.class);
        if (organizer.getRole() != Role.ROLE_ADMIN && organizer.getRole() != Role.ROLE_MODERATOR
            && !organizer.getId().equals(toUpdate.getOrganizer().getId())) {
            throw new BadRequestException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }
        enhanceWithNewData(toUpdate, eventDto, images);
        return modelMapper.map(eventRepo.save(toUpdate), EventDto.class);
    }

    private void enhanceWithNewData(Event toUpdate, UpdateEventDto updateEventDto, MultipartFile[] images) {
        if (updateEventDto.getTitle() != null) {
            toUpdate.setTitle(updateEventDto.getTitle());
        }
        if (updateEventDto.getDescription() != null) {
            toUpdate.setDescription(updateEventDto.getDescription());
        }
        if (updateEventDto.getIsOpen() != null) {
            toUpdate.setOpen(updateEventDto.getIsOpen());
        }

        if (updateEventDto.getTags() != null) {
            toUpdate.setTags(modelMapper.map(tagService
                .findTagsWithAllTranslationsByNamesAndType(updateEventDto.getTags(), TagType.EVENT),
                new TypeToken<List<Tag>>() {
                }.getType()));
        }

        updateImages(toUpdate, updateEventDto, images);

        if (updateEventDto.getDatesLocations() != null) {
            addAddressesToLocation(updateEventDto.getDatesLocations());
            eventRepo.deleteEventDateLocationsByEventId(toUpdate.getId());
            toUpdate.setDates(updateEventDto.getDatesLocations().stream()
                .map(d -> modelMapper.map(d, EventDateLocation.class))
                .map(d -> {
                    d.setEvent(toUpdate);
                    return d;
                })
                .collect(Collectors.toList()));
        }
    }

    private void updateImages(Event toUpdate, UpdateEventDto updateEventDto, MultipartFile[] images) {
        if (ArrayUtils.isEmpty(images) && updateEventDto.getImagesToDelete() == null) {
            changeOldImagesWithoutRemovingAndAdding(toUpdate, updateEventDto);
        } else if (images == null || images.length == 0) {
            deleteOldImages(toUpdate, updateEventDto);
        } else if (updateEventDto.getImagesToDelete() == null) {
            addNewImages(toUpdate, updateEventDto, images);
        } else {
            deleteImagesFromServer(updateEventDto.getImagesToDelete());
            addNewImages(toUpdate, updateEventDto, images);
        }
    }

    private void changeOldImagesWithoutRemovingAndAdding(Event toUpdate, UpdateEventDto updateEventDto) {
        if (updateEventDto.getTitleImage() != null) {
            toUpdate.setTitleImage(updateEventDto.getTitleImage());
        } else {
            toUpdate.setTitleImage(DEFAULT_TITLE_IMAGE_PATH);
        }
        if (updateEventDto.getAdditionalImages() != null) {
            updateEventDto.getAdditionalImages().forEach(img -> toUpdate
                .setAdditionalImages(List.of(EventImages.builder().link(img).event(toUpdate).build())));
        } else {
            toUpdate.setAdditionalImages(null);
        }
    }

    private void deleteOldImages(Event toUpdate, UpdateEventDto updateEventDto) {
        deleteImagesFromServer(updateEventDto.getImagesToDelete());
        if (updateEventDto.getTitleImage() != null) {
            toUpdate.setTitleImage(updateEventDto.getTitleImage());
            if (updateEventDto.getAdditionalImages() != null) {
                toUpdate.setAdditionalImages(updateEventDto.getAdditionalImages().stream()
                    .map(url -> EventImages.builder().event(toUpdate).link(url).build())
                    .collect(Collectors.toList()));
            } else {
                toUpdate.setAdditionalImages(null);
            }
        } else {
            toUpdate.setTitleImage(DEFAULT_TITLE_IMAGE_PATH);
        }
    }

    private void deleteImagesFromServer(List<String> images) {
        images.stream().filter(img -> !img.equals(DEFAULT_TITLE_IMAGE_PATH)).forEach(fileService::delete);
    }

    private void addNewImages(Event toUpdate, UpdateEventDto updateEventDto, MultipartFile[] images) {
        int imagesCounter = 0;
        if (updateEventDto.getTitleImage() != null) {
            toUpdate.setTitleImage(updateEventDto.getTitleImage());
        } else {
            toUpdate.setTitleImage(fileService.upload(images[imagesCounter++]));
        }
        List<String> additionalImagesStr = new ArrayList<>();
        if (updateEventDto.getAdditionalImages() != null) {
            additionalImagesStr.addAll(updateEventDto.getAdditionalImages());
        }
        for (int i = imagesCounter; i < images.length; i++) {
            additionalImagesStr.add(fileService.upload(images[imagesCounter++]));
        }
        if (!additionalImagesStr.isEmpty()) {
            toUpdate.setAdditionalImages(additionalImagesStr.stream().map(url -> EventImages.builder()
                .event(toUpdate).link(url).build()).collect(Collectors.toList()));
        } else {
            toUpdate.setAdditionalImages(null);
        }
    }

    private void addAddressesToLocation(List<EventDateLocationDto> eventDateLocationDtos) {
        for (var date : eventDateLocationDtos) {
            if (date.getCoordinates() != null) {
                CoordinatesDto coordinatesDto = date.getCoordinates();
                List<GeocodingResult> address = googleApiService.getResultFromGeoCodeByCoordinates(
                    new LatLng(coordinatesDto.getLatitude(), coordinatesDto.getLongitude()));
                GeocodingResult resultUa = address.get(0);
                GeocodingResult resultEn = address.get(1);
                coordinatesDto.setAddressUa(resultUa.formattedAddress);
                coordinatesDto.setAddressEn(resultEn.formattedAddress);
                date.setCoordinates(coordinatesDto);
            }
        }
    }
}
