package greencity.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.google.maps.model.PlacesSearchResult;
import greencity.dto.filter.FilterPlacesApiDto;
import greencity.dto.location.LocationDto;
import greencity.exception.exceptions.PlaceAlreadyExistsException;
import org.apache.commons.lang3.ArrayUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import greencity.dto.location.LocationVO;
import greencity.dto.openhours.OpeningHoursDto;
import greencity.dto.openhours.OpeningHoursVO;
import greencity.dto.place.AddPlaceDto;
import greencity.dto.place.AdminPlaceDto;
import greencity.dto.place.BulkUpdatePlaceStatusDto;
import greencity.dto.place.FilterPlaceCategory;
import greencity.dto.place.PlaceAddDto;
import greencity.dto.place.PlaceByBoundsDto;
import greencity.dto.place.PlaceInfoDto;
import greencity.dto.place.PlaceResponse;
import greencity.dto.place.PlaceUpdateDto;
import greencity.dto.place.PlaceVO;
import greencity.dto.place.UpdatePlaceStatusDto;
import greencity.dto.user.UserVO;
import greencity.entity.Category;
import greencity.entity.DiscountValue;
import greencity.entity.Location;
import greencity.entity.OpeningHours;
import greencity.entity.Place;
import greencity.entity.Specification;
import greencity.entity.User;
import greencity.repository.FavoritePlaceRepo;
import greencity.enums.PlaceStatus;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.PlaceStatusException;
import greencity.exception.exceptions.UserBlockedException;
import greencity.message.SendChangePlaceStatusEmailMessage;
import greencity.repository.CategoryRepo;
import greencity.repository.PlaceRepo;
import greencity.repository.UserRepo;
import greencity.repository.options.PlaceFilter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import static greencity.constant.AppConstant.CONSTANT_OF_FORMULA_HAVERSINE_KM;

/**
 * The class provides implementation of the {@code PlaceService}.
 */
@Slf4j
@Service
@AllArgsConstructor
public class PlaceServiceImpl implements PlaceService {
    private final PlaceRepo placeRepo;
    private final ModelMapper modelMapper;
    private final CategoryService categoryService;
    private final LocationService locationService;
    private final SpecificationService specificationService;
    private final RestClient restClient;
    private final OpenHoursService openingHoursService;
    private final DiscountService discountService;
    private final NotificationService notificationService;
    private final ZoneId datasourceTimezone;
    private final ProposePlaceService proposePlaceService;
    private final CategoryRepo categoryRepo;
    private final GoogleApiService googleApiService;
    private final UserRepo userRepo;
    private final FavoritePlaceRepo favoritePlaceRepo;

    /**
     * {@inheritDoc}
     *
     * @author Roman Zahorui
     */
    @Override
    public PageableDto<AdminPlaceDto> getPlacesByStatus(PlaceStatus placeStatus, Pageable pageable) {
        Page<Place> places = placeRepo.findAllByStatusOrderByModifiedDateDesc(placeStatus, pageable);
        List<AdminPlaceDto> list = places.stream()
            .map(place -> modelMapper.map(place, AdminPlaceDto.class))
            .collect(Collectors.toList());
        return new PageableDto<>(list, places.getTotalElements(), places.getPageable().getPageNumber(),
            places.getTotalPages());
    }

    /**
     * {@inheritDoc}
     *
     * @author Marian Datsko
     */
    @Transactional
    @Override
    public PlaceVO save(PlaceAddDto dto, String email) {
        UserVO user = restClient.findByEmail(email);
        if (user.getUserStatus().equals(UserStatus.BLOCKED)) {
            throw new UserBlockedException(ErrorMessage.USER_HAS_BLOCKED_STATUS);
        }
        log.info(LogMessage.IN_SAVE, dto.getName(), email);

        proposePlaceService.checkLocationValues(dto.getLocation());
        if (dto.getOpeningHoursList() != null) {
            proposePlaceService.checkInputTime(dto.getOpeningHoursList());
        }
        PlaceVO placeVO = modelMapper.map(dto, PlaceVO.class);
        setUserToPlaceByEmail(email, placeVO);
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
     * Method for getting {@link User} and set this {@link User} to place.
     *
     * @param email   - String, user's email.
     * @param placeVO - {@link Place} entity.
     * @return user - {@link User}.
     * @author Kateryna Horokh
     */
    private UserVO setUserToPlaceByEmail(String email, PlaceVO placeVO) {
        UserVO userVO = restClient.findByEmail(email);
        placeVO.setAuthor(userVO);
        if (userVO.getRole() == Role.ROLE_ADMIN || userVO.getRole() == Role.ROLE_MODERATOR) {
            placeVO.setStatus(PlaceStatus.APPROVED);
            notificationService.sendImmediatelyReport(placeVO);
        }
        return userVO;
    }

    /**
     * {@inheritDoc}
     *
     * @author Kateryna Horokh
     */
    @Transactional
    @Override
    public PlaceVO update(PlaceUpdateDto dto) {
        log.info(LogMessage.IN_UPDATE, dto.getName());

        Category updatedCategory = modelMapper.map(
            categoryService.findByName(dto.getCategory().getName()), Category.class);
        Place updatedPlace = findPlaceById(dto.getId());
        locationService.update(updatedPlace.getLocation().getId(),
            modelMapper.map(dto.getLocation(), LocationVO.class));
        updatedPlace.setName(dto.getName());
        updatedPlace.setCategory(updatedCategory);
        placeRepo.save(updatedPlace);

        updateOpening(dto.getOpeningHoursList(), updatedPlace);
        updateDiscount(dto.getDiscountValues(), updatedPlace);

        return modelMapper.map(updatedPlace, PlaceVO.class);
    }

    /**
     * Method for updating set of {@link DiscountValue} and save with new
     * {@link Category} and {@link Place}.
     *
     * @param discounts    - set of {@link DiscountValue}.
     * @param updatedPlace - {@link Place} entity.
     * @author Kateryna Horokh
     */
    private void updateDiscount(Set<DiscountValueDto> discounts, Place updatedPlace) {
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
     * @author Kateryna Horokh
     */
    private void updateOpening(Set<OpeningHoursDto> hoursUpdateDtoSet, Place updatedPlace) {
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
     *
     * @author Nazar Vladyka
     */
    @Override
    public void deleteById(Long id) {
        log.info(LogMessage.IN_DELETE_BY_ID, id);

        updateStatus(id, PlaceStatus.DELETED);
    }

    /**
     * {@inheritDoc}
     *
     * @author Nazar Vladyka
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
     *
     * @author Nazar Vladyka.
     */
    @Override
    public List<PlaceVO> findAll() {
        log.info(LogMessage.IN_FIND_ALL);

        return modelMapper.map(placeRepo.findAll(), new TypeToken<List<PlaceVO>>() {
        }.getType());
    }

    /**
     * {@inheritDoc}
     *
     * @author Olena Petryshak.
     * @author Olena Sotnik.
     *
     */
    @Override
    public PageableDto<AdminPlaceDto> findAll(Pageable pageable, String email) {
        log.info(LogMessage.IN_FIND_ALL);

        Page<Place> pages = placeRepo.findAll(pageable);
        List<AdminPlaceDto> placeDtos =
            pages.stream().map(place -> modelMapper.map(place, AdminPlaceDto.class)).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(placeDtos) && !email.isBlank()) {
            setIsFavoriteToAdminPlaceDto(placeDtos, email);
        }
        return new PageableDto<>(placeDtos, pages.getTotalElements(), pageable.getPageNumber(), pages.getTotalPages());
    }

    /**
     * {@inheritDoc}
     *
     * @author Nazar Vladyka
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
            notificationService.sendImmediatelyReport(modelMapper.map(updatable, PlaceVO.class));
        }
        if (oldStatus.equals(PlaceStatus.PROPOSED)) {
            restClient.changePlaceStatus(new SendChangePlaceStatusEmailMessage(updatable.getAuthor().getName(),
                updatable.getName(), updatable.getStatus().toString().toLowerCase(),
                updatable.getAuthor().getEmail()));
        }
        return modelMapper.map(placeRepo.save(updatable), UpdatePlaceStatusDto.class);
    }

    /**
     * {@inheritDoc}
     *
     * @author Nazar Vladyka
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
     *
     * @author Nazar Vladyka.
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
     *
     * @author Marian Milian
     */
    @Override
    public Optional<PlaceVO> findByIdOptional(Long id) {
        return placeRepo.findById(id).map(place -> modelMapper.map(place, PlaceVO.class));
    }

    /**
     * {@inheritDoc}
     *
     * @author Dmytro Dovhal
     */
    @Override
    public PlaceInfoDto getInfoById(Long id) {
        Place place =
            placeRepo
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.PLACE_NOT_FOUND_BY_ID + id));
        PlaceInfoDto placeInfoDto = modelMapper.map(place, PlaceInfoDto.class);
        placeInfoDto.setRate(placeRepo.getAverageRate(id));
        return placeInfoDto;
    }

    /**
     * {@inheritDoc}
     *
     * @author Kateryna Horokh
     */
    @Override
    public PlaceUpdateDto getInfoForUpdatingById(Long id) {
        Place place = placeRepo
            .findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.PLACE_NOT_FOUND_BY_ID + id));
        return modelMapper.map(place, PlaceUpdateDto.class);
    }

    /**
     * {@inheritDoc}
     *
     * @author Olena Petryshak
     */
    @Override
    public PageableDto<AdminPlaceDto> searchBy(Pageable pageable, String searchQuery) {
        Page<Place> pages = placeRepo.searchBy(pageable, searchQuery);
        List<AdminPlaceDto> adminPlaceDtos = pages.stream()
            .map(place -> modelMapper.map(place, AdminPlaceDto.class))
            .collect(Collectors.toList());
        return new PageableDto<>(
            adminPlaceDtos,
            pages.getTotalElements(),
            pageable.getPageNumber(),
            pages.getTotalPages());
    }

    /**
     * {@inheritDoc}
     *
     * @author Marian Milian
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
     *
     * @author Zakhar Skaletskyi
     */
    @Override
    public boolean existsById(Long id) {
        log.info(LogMessage.IN_EXISTS_BY_ID, id);
        return placeRepo.existsById(id);
    }

    /**
     * {@inheritDoc}
     *
     * @author Zakhar Skaletskyi
     */
    @Override
    public Double averageRate(Long id) {
        log.info(LogMessage.IN_AVERAGE_RATE, id);
        return placeRepo.getAverageRate(id);
    }

    /**
     * {@inheritDoc}
     *
     * @author Roman Zahorui
     */
    @Override
    public List<PlaceByBoundsDto> getPlacesByFilter(FilterPlaceDto filterDto) {
        List<Place> list =
            ArrayUtils.isNotEmpty(filterDto.getCategories()) ? placeRepo.findPlaceByCategory(filterDto.getCategories())
                : placeRepo.findAll(new PlaceFilter(filterDto));
        list = getPlacesByDistanceFromUser(filterDto, list);
        return list.stream()
            .map(place -> modelMapper.map(place, PlaceByBoundsDto.class))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PlaceByBoundsDto> getPlacesByFilter(FilterPlacesApiDto filterDto, UserVO userVO) {
        List<PlacesSearchResult> fromPlacesApi = googleApiService.getResultFromPlacesApi(filterDto, userVO);
        return fromPlacesApi.stream()
                .map(el -> new PlaceByBoundsDto(
                        System.currentTimeMillis(),
                        el.name,
                        new LocationDto(System.currentTimeMillis(), el.geometry.location.lat, el.geometry.location.lng,
                                el.vicinity)))
                .collect(Collectors.toList());
    }

    /**
     * Method that filtering places by distance.
     *
     * @param filterDto - {@link FilterPlaceDto} DTO.
     * @param placeList - {@link List} of {@link Place} that will be filtered.
     * @return {@link List} of {@link Place} - list of filtered {@link Place}s.
     * @author Nazar Stasyuk
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
            throw new PlaceStatusException(String.format(
                ErrorMessage.PLACE_STATUS_NOT_DIFFERENT, placeId, updatedStatus));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Rostyslav Khasanov
     */
    @Override
    public PageableDto<AdminPlaceDto> filterPlaceBySearchPredicate(FilterPlaceDto filterDto, Pageable pageable) {
        Page<Place> list = placeRepo.findAll(new PlaceFilter(filterDto), pageable);
        List<AdminPlaceDto> adminPlaceDtos =
            list.getContent().stream()
                .map(user -> modelMapper.map(user, AdminPlaceDto.class))
                .collect(Collectors.toList());
        return new PageableDto<>(
            adminPlaceDtos,
            list.getTotalElements(),
            list.getPageable().getPageNumber(),
            list.getTotalPages());
    }

    /**
     * {@inheritDoc}
     *
     * @author Nazar Vladyka
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

    /**
     * {@inheritDoc}
     */
    @Override
    public PlaceResponse addPlaceFromUi(AddPlaceDto dto, String email) {
        PlaceResponse placeResponse = modelMapper.map(dto, PlaceResponse.class);
        User user = userRepo.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User with email " + email + " doesn't exist"));
        if (user.getUserStatus().equals(UserStatus.BLOCKED)) {
            throw new UserBlockedException(ErrorMessage.USER_HAS_BLOCKED_STATUS);
        }
        List<GeocodingResult> geocodingResults = googleApiService.getResultFromGeoCode(dto.getLocationName());
        double lat = geocodingResults.get(0).geometry.location.lat;
        double lng = geocodingResults.get(0).geometry.location.lng;
        if (locationService.existsByLatAndLng(lat, lng)) {
            throw new PlaceAlreadyExistsException(ErrorMessage.PLACE_ALREADY_EXISTS);
        }
        placeResponse.setLocationAddressAndGeoDto(initializeGeoCodingResults(geocodingResults));
        Place place = modelMapper.map(placeResponse, Place.class);
        place.setCategory(categoryRepo.findCategoryByName(dto.getCategoryName()));
        place.setAuthor(user);
        place.setLocation(modelMapper.map(placeResponse.getLocationAddressAndGeoDto(), Location.class));

        return modelMapper.map(placeRepo.save(place), PlaceResponse.class);
    }

    private AddPlaceLocation initializeGeoCodingResults(
        List<GeocodingResult> geocodingResults) {
        GeocodingResult ukrLang = geocodingResults.get(0);
        GeocodingResult engLang = geocodingResults.get(1);
        return AddPlaceLocation.builder()
            .address(ukrLang.formattedAddress)
            .addressEng(engLang.formattedAddress)
            .lat(ukrLang.geometry.location.lat)
            .lng(ukrLang.geometry.location.lng)
            .build();
    }

    private void setIsFavoriteToAdminPlaceDto(List<AdminPlaceDto> placeDtos, String email) {
        List<Long> favoritePlacesLocationIds = favoritePlaceRepo.findAllFavoritePlaceLocationIdsByUserEmail(email);
        placeDtos.forEach(dto -> {
            boolean isFavorite = favoritePlacesLocationIds.stream()
                .anyMatch(locationId -> locationId.equals(dto.getLocation().getId()));
            dto.setIsFavorite(isFavorite);
        });
    }
}
