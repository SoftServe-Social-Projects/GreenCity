package greencity.service;

import com.google.maps.model.GeocodingResult;
import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
import greencity.constant.LogMessage;
import greencity.dto.PageableDto;
import greencity.dto.discount.DiscountValueDto;
import greencity.dto.discount.DiscountValueVO;
import greencity.dto.filter.FilterDistanceDto;
import greencity.dto.filter.FilterPlaceDto;
import greencity.dto.location.AddPlaceLocation;
import greencity.dto.location.LocationAddressAndGeoForUpdateDto;
import greencity.dto.location.LocationVO;
import greencity.dto.openhours.OpenHoursDto;
import greencity.dto.openhours.OpeningHoursDto;
import greencity.dto.openhours.OpeningHoursVO;
import greencity.dto.place.PlaceByBoundsDto;
import greencity.dto.place.UpdatePlaceStatusWithUserEmailDto;
import greencity.dto.place.AddPlaceDto;
import greencity.dto.place.PlaceResponse;
import greencity.dto.place.FilterPlaceCategory;
import greencity.dto.place.FilterAdminPlaceDto;
import greencity.dto.place.PlaceInfoDto;
import greencity.dto.place.BulkUpdatePlaceStatusDto;
import greencity.dto.place.UpdatePlaceStatusDto;
import greencity.dto.place.AdminPlaceDto;
import greencity.dto.place.PlaceAddDto;
import greencity.dto.place.PlaceUpdateDto;
import greencity.dto.place.PlaceVO;
import greencity.dto.search.SearchPlacesDto;
import greencity.dto.user.UserVO;
import greencity.entity.Category;
import greencity.entity.DiscountValue;
import greencity.entity.Location;
import greencity.entity.OpeningHours;
import greencity.entity.Photo;
import greencity.entity.Place;
import greencity.entity.Specification;
import greencity.entity.User;
import greencity.enums.EmailPreference;
import greencity.enums.EmailPreferencePeriodicity;
import greencity.enums.NotificationType;
import greencity.enums.PlaceStatus;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.PlaceStatusException;
import greencity.exception.exceptions.UserBlockedException;
import greencity.repository.CategoryRepo;
import greencity.repository.FavoritePlaceRepo;
import greencity.repository.PhotoRepo;
import greencity.repository.PlaceRepo;
import greencity.repository.UserRepo;
import greencity.repository.options.PlaceFilter;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import static greencity.constant.AppConstant.CONSTANT_OF_FORMULA_HAVERSINE_KM;

/**
 * The class provides implementation of the {@code PlaceService}.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PlaceServiceImpl implements PlaceService {
    private final PlaceRepo placeRepo;
    private final ModelMapper modelMapper;
    private final CategoryService categoryService;
    private final LocationService locationService;
    private final SpecificationService specificationService;
    private final OpenHoursService openingHoursService;
    private final UserService userService;
    private final DiscountService discountService;
    private final ZoneId datasourceTimezone;
    private final ProposePlaceService proposePlaceService;
    private final CategoryRepo categoryRepo;
    private final GoogleApiService googleApiService;
    private final UserRepo userRepo;
    private final FavoritePlaceRepo favoritePlaceRepo;
    private final FileService fileService;
    private final UserNotificationService userNotificationService;
    private final RestClient restClient;
    private final PhotoRepo photoRepo;

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<AdminPlaceDto> getPlacesByStatus(PlaceStatus placeStatus, Pageable pageable) {
        Page<Place> places = placeRepo.findAllByStatusOrderByModifiedDateDesc(placeStatus, pageable);
        List<AdminPlaceDto> list = createAdminPageableDtoList(places);
        return new PageableDto<>(list, places.getTotalElements(), places.getPageable().getPageNumber(),
            places.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public PlaceVO save(PlaceAddDto dto, String email) {
        UserVO user = userService.findByEmail(email);
        if (user.getUserStatus().equals(UserStatus.BLOCKED)) {
            throw new UserBlockedException(ErrorMessage.USER_HAS_BLOCKED_STATUS);
        }
        log.info(LogMessage.IN_SAVE, dto.getName(), email);

        proposePlaceService.checkLocationValues(dto.getLocation());
        if (dto.getOpeningHoursList() != null) {
            proposePlaceService.checkInputTime(dto.getOpeningHoursList());
        }
        PlaceVO placeVO = modelMapper.map(dto, PlaceVO.class);
        setUserToPlace(user, placeVO);
        if (placeVO.getDiscountValues() != null) {
            proposePlaceService.saveDiscountValuesWithPlace(placeVO.getDiscountValues(), placeVO);
        }
        if (placeVO.getPhotos() != null) {
            proposePlaceService.savePhotosWithPlace(placeVO.getPhotos(), placeVO);
        }
        Place place = modelMapper.map(placeVO, Place.class);
        place.setCategory(categoryRepo.findByName(dto.getCategory().getName()));
        place.getOpeningHoursList().forEach(openingHours -> openingHours.setPlace(place));

        return modelMapper.map(placeRepo.save(place), PlaceVO.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PlaceVO> getAllCreatedPlacesByUserId(Long userId) {
        return placeRepo.findAllByUserId(userId).stream()
            .map(place -> modelMapper.map(place, PlaceVO.class))
            .collect(Collectors.toList());
    }

    /**
     * Method for setting this {@link User} to place.
     *
     * @param userVO  - {@link User} entity.
     * @param placeVO - {@link Place} entity.
     */
    private void setUserToPlace(UserVO userVO, PlaceVO placeVO) {
        placeVO.setAuthor(userVO);
        if (userVO.getRole() == Role.ROLE_ADMIN || userVO.getRole() == Role.ROLE_MODERATOR) {
            placeVO.setStatus(PlaceStatus.APPROVED);
            List<UserVO> usersId = userService.getUsersIdByEmailPreferenceAndEmailPeriodicity(EmailPreference.PLACES,
                EmailPreferencePeriodicity.IMMEDIATELY);
            userNotificationService.createNewNotificationForPlaceAdded(usersId, placeVO.getId(),
                placeVO.getCategory().getName(), placeVO.getName());
        }
    }

    /**
     * Method for updating set of {@link DiscountValue} and save with new
     * {@link Category} and {@link Place}.
     *
     * @param discounts    - set of {@link DiscountValue}.
     * @param updatedPlace - {@link Place} entity.
     */
    void updateDiscount(Set<DiscountValueDto> discounts, Place updatedPlace) {
        log.info(LogMessage.IN_UPDATE_DISCOUNT_FOR_PLACE);

        Set<DiscountValueVO> discountValuesVO = discountService.findAllByPlaceId(updatedPlace.getId());
        Set<DiscountValue> discountsOld = modelMapper.map(discountValuesVO,
            new TypeToken<Set<DiscountValue>>() {
            }.getType());
        discountService.deleteAllByPlaceId(updatedPlace.getId());
        Set<DiscountValue> newDiscounts = new HashSet<>();
        if (discounts != null) {
            discounts.forEach(d -> {
                DiscountValue discount = modelMapper.map(d, DiscountValue.class);
                discount.setSpecification(modelMapper
                    .map(specificationService.findByName(d.getSpecification().getName()), Specification.class));
                discount.setPlace(updatedPlace);
                discountService.save(modelMapper.map(discount, DiscountValueVO.class));
                newDiscounts.add(discount);
            });
        }
        discountsOld.addAll(newDiscounts);
    }

    /**
     * Method for updating set of {@link OpeningHours} and save with new
     * {@link Place}.
     *
     * @param hoursUpdateDtoSet - set of {@code Discount}.
     * @param updatedPlace      - {@link Place} entity.
     */
    void updateOpening(Set<OpeningHoursDto> hoursUpdateDtoSet, Place updatedPlace) {
        log.info(LogMessage.IN_UPDATE_OPENING_HOURS_FOR_PLACE);
        updatedPlace.setOpeningHoursList(null);
        Set<OpeningHoursVO> openingHoursVO = openingHoursService.findAllByPlaceId(updatedPlace.getId());
        Set<OpeningHours> openingHoursSetOld = modelMapper.map(openingHoursVO,
            new TypeToken<Set<OpeningHours>>() {
            }.getType());
        openingHoursService.deleteAllByPlaceId(updatedPlace.getId());
        Set<OpeningHours> hours = new HashSet<>();
        if (hoursUpdateDtoSet != null) {
            hoursUpdateDtoSet.forEach(h -> {
                OpeningHours openingHours = modelMapper.map(h, OpeningHours.class);
                openingHours.setPlace(updatedPlace);
                openingHoursService.save(modelMapper.map(openingHours, OpeningHoursVO.class));
                hours.add(openingHours);
            });
        }
        openingHoursSetOld.addAll(hours);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(Long id) {
        log.info(LogMessage.IN_DELETE_BY_ID, id);

        updateStatus(id, PlaceStatus.DELETED);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public Long bulkDelete(List<Long> ids) {
        log.info(LogMessage.IN_BULK_DELETE, ids);

        List<UpdatePlaceStatusDto> deletedPlaces =
            updateStatuses(new BulkUpdatePlaceStatusDto(ids, PlaceStatus.DELETED));

        return (long) deletedPlaces.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PlaceVO> findAll() {
        log.info(LogMessage.IN_FIND_ALL);

        return modelMapper.map(placeRepo.findAll(), new TypeToken<List<PlaceVO>>() {
        }.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<AdminPlaceDto> findAll(Pageable pageable, Principal principal) {
        log.info(LogMessage.IN_FIND_ALL);
        Page<Place> pages = placeRepo.findAll(pageable);
        List<AdminPlaceDto> placeDtos = createAdminPageableDtoList(pages);
        if (!CollectionUtils.isEmpty(placeDtos) && principal != null) {
            setIsFavoriteToAdminPlaceDto(placeDtos, principal.getName());
        }
        return new PageableDto<>(placeDtos, pages.getTotalElements(), pageable.getPageNumber(), pages.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdatePlaceStatusDto updateStatus(Long id, PlaceStatus status) {
        log.info(LogMessage.IN_UPDATE_PLACE_STATUS, id, status);
        Place updatable = findPlaceById(id);
        PlaceStatus oldStatus = updatable.getStatus();
        checkPlaceStatuses(oldStatus, status, id);
        updatable.setStatus(status);
        updatable.setModifiedDate(ZonedDateTime.now(datasourceTimezone));
        if (status.equals(PlaceStatus.APPROVED)) {
            List<UserVO> usersId = userService.getUsersIdByEmailPreferenceAndEmailPeriodicity(EmailPreference.PLACES,
                EmailPreferencePeriodicity.IMMEDIATELY);
            userNotificationService.createNewNotificationForPlaceAdded(usersId, updatable.getId(),
                updatable.getCategory().getName(), updatable.getName());
        }
        if (oldStatus.equals(PlaceStatus.PROPOSED)) {
            userNotificationService.createNewNotification(modelMapper.map(updatable.getAuthor(), UserVO.class),
                NotificationType.PLACE_STATUS, updatable.getId(), updatable.getName(),
                updatable.getStatus().name().toLowerCase());
        }
        return modelMapper.map(placeRepo.save(updatable), UpdatePlaceStatusDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public List<UpdatePlaceStatusDto> updateStatuses(BulkUpdatePlaceStatusDto dto) {
        log.info(LogMessage.IN_UPDATE_PLACE_STATUSES, dto);

        List<UpdatePlaceStatusDto> updatedPlaces = new ArrayList<>();
        for (Long id : dto.getIds()) {
            updatedPlaces.add(updateStatus(id, dto.getStatus()));
        }

        return updatedPlaces;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlaceVO findById(Long id) {
        log.info(LogMessage.IN_FIND_BY_ID, id);
        Place place = placeRepo.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.PLACE_NOT_FOUND_BY_ID + id));
        return modelMapper.map(place, PlaceVO.class);
    }

    /**
     * {@inheritDoc}
     */
    private Place findPlaceById(Long id) {
        log.info(LogMessage.IN_FIND_BY_ID, id);
        return placeRepo.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.PLACE_NOT_FOUND_BY_ID + id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PlaceVO> findByIdOptional(Long id) {
        return placeRepo.findById(id)
            .map(place -> modelMapper.map(place, PlaceVO.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlaceInfoDto getInfoById(Long id) {
        Place place = placeRepo.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.PLACE_NOT_FOUND_BY_ID + id));
        PlaceInfoDto placeInfoDto = modelMapper.map(place, PlaceInfoDto.class);
        placeInfoDto.setRate(placeRepo.getAverageRate(id));
        return placeInfoDto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlaceUpdateDto getInfoForUpdatingById(Long id) {
        Place place = placeRepo.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.PLACE_NOT_FOUND_BY_ID + id));
        return modelMapper.map(place, PlaceUpdateDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<AdminPlaceDto> searchBy(Pageable pageable, String searchQuery) {
        Page<Place> pages = placeRepo.searchBy(pageable, searchQuery);
        List<AdminPlaceDto> placeDtos = createAdminPageableDtoList(pages);
        return new PageableDto<>(
            placeDtos,
            pages.getTotalElements(),
            pageable.getPageNumber(),
            pages.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PlaceByBoundsDto> findPlacesByMapsBounds(@Valid FilterPlaceDto filterPlaceDto) {
        List<Place> list = placeRepo.findAll(new PlaceFilter(filterPlaceDto));
        return list.stream()
            .map(place -> modelMapper.map(place, PlaceByBoundsDto.class))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsById(Long id) {
        log.info(LogMessage.IN_EXISTS_BY_ID, id);
        return placeRepo.existsById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double averageRate(Long id) {
        log.info(LogMessage.IN_AVERAGE_RATE, id);
        return placeRepo.getAverageRate(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PlaceByBoundsDto> getPlacesByFilter(FilterPlaceDto filterDto, UserVO userVO) {
        Long userId = userVO == null ? null : userVO.getId();
        List<Place> list =
            ArrayUtils.isNotEmpty(filterDto.getCategories()) ? placeRepo.findPlaceByCategory(filterDto.getCategories())
                : placeRepo.findAll(new PlaceFilter(filterDto, userId));
        list = getPlacesByDistanceFromUser(filterDto, list);
        return list.stream()
            .map(place -> modelMapper.map(place, PlaceByBoundsDto.class))
            .collect(Collectors.toList());
    }

    /**
     * Method that filtering places by distance.
     *
     * @param filterDto - {@link FilterPlaceDto} DTO.
     * @param placeList - {@link List} of {@link Place} that will be filtered.
     * @return {@link List} of {@link Place} - list of filtered {@link Place}s.
     */
    private List<Place> getPlacesByDistanceFromUser(FilterPlaceDto filterDto, List<Place> placeList) {
        FilterDistanceDto distanceFromUserDto = filterDto.getDistanceFromUserDto();
        if (distanceFromUserDto != null
            && distanceFromUserDto.getLat() != null
            && distanceFromUserDto.getLng() != null
            && distanceFromUserDto.getDistance() != null) {
            placeList = placeList.stream().filter(place -> {
                double userLatRad = Math.toRadians(distanceFromUserDto.getLat());
                double userLngRad = Math.toRadians(distanceFromUserDto.getLng());
                double placeLatRad = Math.toRadians(place.getLocation().getLat());
                double placeLngRad = Math.toRadians(place.getLocation().getLng());

                double distance = CONSTANT_OF_FORMULA_HAVERSINE_KM * Math.acos(
                    Math.cos(userLatRad)
                        * Math.cos(placeLatRad)
                        * Math.cos(placeLngRad - userLngRad)
                        + Math.sin(userLatRad)
                            * Math.sin(placeLatRad));
                return distance <= distanceFromUserDto.getDistance();
            }).collect(Collectors.toList());
        }
        return placeList;
    }

    private void checkPlaceStatuses(PlaceStatus currentStatus, PlaceStatus updatedStatus, Long placeId) {
        if (currentStatus.equals(updatedStatus)) {
            log.error(LogMessage.PLACE_STATUS_NOT_DIFFERENT, placeId, updatedStatus);
            throw new PlaceStatusException(ErrorMessage.PLACE_STATUS_NOT_DIFFERENT.formatted(placeId, updatedStatus));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<AdminPlaceDto> filterPlaceBySearchPredicate(FilterPlaceDto filterDto, Pageable pageable) {
        Page<Place> list = placeRepo.findAll(new PlaceFilter(filterDto), pageable);
        List<AdminPlaceDto> placeDtos = createAdminPageableDtoList(list);
        return new PageableDto<>(
            placeDtos,
            list.getTotalElements(),
            list.getPageable().getPageNumber(),
            list.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<AdminPlaceDto> getFilteredPlacesForAdmin(FilterAdminPlaceDto filterDto, Pageable pageable) {
        Page<Place> list = placeRepo.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Optional.ofNullable(filterDto.getId())
                .filter(id -> !id.isEmpty())
                .ifPresent(id -> predicates.add(cb.equal(root.get("id"), id)));
            Optional.ofNullable(filterDto.getName())
                .filter(name -> !name.isEmpty())
                .ifPresent(name -> predicates.add(cb.like(root.get("name"), "%" + name + "%")));
            Optional.ofNullable(filterDto.getStatus())
                .filter(status -> !status.isEmpty())
                .ifPresent(status -> predicates.add(cb.equal(root.get("status"), PlaceStatus.valueOf(status))));
            Optional.ofNullable(filterDto.getAuthor())
                .filter(author -> !author.isEmpty())
                .ifPresent(author -> predicates.add(cb.like(root.join("author").get("name"), "%" + author + "%")));
            Optional.ofNullable(filterDto.getAddress())
                .filter(address -> !address.isEmpty())
                .ifPresent(
                    address -> predicates.add(cb.like(root.join("location").get("address"), "%" + address + "%")));
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
        List<AdminPlaceDto> placeDtos = createAdminPageableDtoList(list);
        return new PageableDto<>(
            placeDtos,
            list.getTotalElements(),
            list.getPageable().getPageNumber(),
            list.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PlaceStatus> getStatuses() {
        return Arrays.asList(PlaceStatus.class.getEnumConstants());
    }

    @Override
    public List<FilterPlaceCategory> getAllPlaceCategories() {
        return modelMapper.map(categoryRepo.findAll(), new TypeToken<List<FilterPlaceCategory>>() {
        }.getType());
    }

    @Override
    public PlaceResponse addPlaceFromUi(AddPlaceDto dto, String email, MultipartFile[] images) {
        PlaceResponse placeResponse = modelMapper.map(dto, PlaceResponse.class);
        User user = userRepo.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_EMAIL + email));
        if (user.getUserStatus().equals(UserStatus.BLOCKED)) {
            throw new UserBlockedException(ErrorMessage.USER_HAS_BLOCKED_STATUS);
        }

        AddPlaceLocation geoDetails = getLocationDetailsFromGeocode(dto.getLocationName());
        placeResponse.setLocationAddressAndGeoDto(geoDetails);

        Place place = modelMapper.map(placeResponse, Place.class);
        place.setCategory(categoryRepo.findCategoryByName(dto.getCategoryName()));
        place.setAuthor(user);
        place.setLocation(modelMapper.map(placeResponse.getLocationAddressAndGeoDto(), Location.class));
        Optional.ofNullable(place.getOpeningHoursList()).orElse(Collections.emptySet())
            .forEach(openingHours -> openingHours.setPlace(place));
        mapMultipartFilesToPhotos(images, place, user);
        return modelMapper.map(placeRepo.save(place), PlaceResponse.class);
    }

    AddPlaceLocation getLocationDetailsFromGeocode(String locationName) {
        List<GeocodingResult> geocodingResults = Optional
            .ofNullable(googleApiService.getResultFromGeoCode(locationName))
            .filter(results -> !results.isEmpty())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.ADDRESS_NOT_FOUND_EXCEPTION + locationName));

        return initializeGeoCodingResults(geocodingResults);
    }

    private AddPlaceLocation initializeGeoCodingResults(
        List<GeocodingResult> geocodingResults) {
        GeocodingResult ukrLang = geocodingResults.getFirst();
        GeocodingResult engLang = geocodingResults.get(1);
        return AddPlaceLocation.builder()
            .address(ukrLang.formattedAddress)
            .addressEng(engLang.formattedAddress)
            .lat(ukrLang.geometry.location.lat)
            .lng(ukrLang.geometry.location.lng)
            .build();
    }

    private void mapMultipartFilesToPhotos(MultipartFile[] images, Place place, User user) {
        if (images != null && images.length > 0 && images[0] != null) {
            List<Photo> newPhotos = new ArrayList<>();
            for (MultipartFile image : images) {
                if (image != null) {
                    Photo newPhoto = Photo.builder()
                        .place(place)
                        .name(fileService.upload(image))
                        .user(user)
                        .build();
                    Photo savedPhoto = photoRepo.save(newPhoto);
                    newPhotos.add(savedPhoto);
                    newPhotos.add(newPhoto);
                }
            }
            place.getPhotos().addAll(newPhotos);
        }
    }

    private void setIsFavoriteToAdminPlaceDto(List<AdminPlaceDto> placeDtos, String email) {
        List<Long> favoritePlacesLocationIds = favoritePlaceRepo.findAllFavoritePlaceLocationIdsByUserEmail(email);
        placeDtos.forEach(dto -> {
            boolean isFavorite = favoritePlacesLocationIds.stream()
                .anyMatch(locationId -> locationId.equals(dto.getLocation().getId()));
            dto.setIsFavorite(isFavorite);
        });
    }

    private List<AdminPlaceDto> createAdminPageableDtoList(Page<Place> places) {
        return places.stream().map(place -> {
            AdminPlaceDto adminPlaceDto = modelMapper.map(place, AdminPlaceDto.class);
            List<String> photoNames = Optional.ofNullable(place.getPhotos())
                .orElse(Collections.emptyList())
                .stream()
                .map(Photo::getName)
                .collect(Collectors.toList());
            List<OpenHoursDto> openingHoursList = place.getOpeningHoursList().stream()
                .map(element -> modelMapper.map(element, OpenHoursDto.class))
                .toList();
            adminPlaceDto.setImages(photoNames);
            adminPlaceDto.setOpeningHoursList(openingHoursList);
            return adminPlaceDto;
        }).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<SearchPlacesDto> search(Pageable pageable, String searchQuery, Boolean isFavorite, Long userId) {
        return getSearchPlacesDtoPageableDto(placeRepo.find(pageable, searchQuery, isFavorite, userId));
    }

    private PageableDto<SearchPlacesDto> getSearchPlacesDtoPageableDto(Page<Place> page) {
        List<SearchPlacesDto> searchEventsDtos = page.stream()
            .map(event -> modelMapper.map(event, SearchPlacesDto.class))
            .toList();

        return new PageableDto<>(
            searchEventsDtos,
            page.getTotalElements(),
            page.getPageable().getPageNumber(),
            page.getTotalPages());
    }

    /**
     * Updates the status of a place, validates the user's existence, and sends a
     * notification if the status changes to APPROVED or DECLINED.
     *
     * @param dto The data transfer object containing place name, user email, and
     *            the new status.
     * @return The updated UpdatePlaceStatusWithUserEmailDto.
     * @throws NotFoundException If the place or user is not found.
     */
    @Override
    public UpdatePlaceStatusWithUserEmailDto updatePlaceStatus(UpdatePlaceStatusWithUserEmailDto dto) {
        Place place = placeRepo.findByNameIgnoreCase(dto.getPlaceName())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.PLACE_NOT_FOUND_BY_NAME + dto.getPlaceName()));

        if (userRepo.findByEmail(dto.getEmail()).isEmpty()) {
            throw new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_EMAIL + dto.getEmail());
        }

        place.setStatus(dto.getNewStatus());
        placeRepo.save(place);

        if (dto.getNewStatus() == PlaceStatus.APPROVED || dto.getNewStatus() == PlaceStatus.DECLINED) {
            restClient.sendEmailNotificationChangesPlaceStatus(dto);
        }
        return dto;
    }

    void updateLocation(PlaceUpdateDto dto, Place updatedPlace, LocationVO updatable) {
        AddPlaceLocation geoDetails = getLocationDetailsFromGeocode(dto.getLocation().getAddress());

        LocationAddressAndGeoForUpdateDto sourceDto = geoDetails != null
            ? new LocationAddressAndGeoForUpdateDto(
                geoDetails.getAddressEng(),
                geoDetails.getLat(),
                geoDetails.getLng(),
                geoDetails.getAddress())
            : dto.getLocation();

        LocationVO updatedLocation = createLocationVO(updatable.getId(), sourceDto);

        locationService.update(updatedPlace.getLocation().getId(), updatedLocation);
    }

    private LocationVO createLocationVO(Long id, LocationAddressAndGeoForUpdateDto dto) {
        return LocationVO.builder()
            .id(id)
            .address(dto.getAddress())
            .lat(dto.getLat())
            .lng(dto.getLng())
            .addressUa(dto.getAddressUa())
            .build();
    }

    private void updatePlaceProperties(PlaceUpdateDto dto, Place updatedPlace, Category updatedCategory) {
        updatedPlace.setName(dto.getName());
        updatedPlace.setCategory(updatedCategory);
        placeRepo.save(updatedPlace);
        updateOpening(dto.getOpeningHoursList(), updatedPlace);
        updateDiscount(dto.getDiscountValues(), updatedPlace);
    }

    @Transactional
    @Override
    public PlaceVO update(PlaceUpdateDto dto) {
        log.info(LogMessage.IN_UPDATE, dto.getName());
        Category updatedCategory = modelMapper.map(
            categoryService.findByName(dto.getCategory().getName()), Category.class);
        Place updatedPlace = findPlaceById(dto.getId());
        LocationVO updatable = locationService.findById(updatedPlace.getLocation().getId());
        updateLocation(dto, updatedPlace, updatable);
        updatePlaceProperties(dto, updatedPlace, updatedCategory);
        return modelMapper.map(updatedPlace, PlaceVO.class);
    }

    @Transactional
    @Override
    public PlaceVO updateFromUI(PlaceUpdateDto dto, MultipartFile[] images, String email) {
        log.info(LogMessage.IN_UPDATE, dto.getName());
        Category updatedCategory = modelMapper.map(
            categoryService.findByName(dto.getCategory().getName()), Category.class);
        Place updatedPlace = findPlaceById(dto.getId());
        LocationVO updatable = locationService.findById(updatedPlace.getLocation().getId());
        updateLocation(dto, updatedPlace, updatable);
        updatePlaceProperties(dto, updatedPlace, updatedCategory);
        Place place = modelMapper.map(updatedPlace, Place.class);
        Optional<User> user = userRepo.findByEmail(email);
        mapMultipartFilesToPhotos(images, place, user.orElse(null));
        placeRepo.save(updatedPlace);
        return modelMapper.map(updatedPlace, PlaceVO.class);
    }
}