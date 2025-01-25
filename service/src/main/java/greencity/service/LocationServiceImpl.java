package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.constant.LogMessage;
import greencity.dto.location.LocationVO;
import greencity.entity.Location;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.LocationRepo;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

/**
 * Service implementation for Location entity.
 *
 * @version 1.0
 */
@Service
@AllArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {
    private final LocationRepo locationRepo;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     *
     * @author Nazar Vladyka
     */
    @Override
    public List<LocationVO> findAll() {
        log.info(LogMessage.IN_FIND_ALL);

        return modelMapper.map(locationRepo.findAll(), new TypeToken<List<LocationVO>>() {
        }.getType());
    }

    /**
     * {@inheritDoc}
     *
     * @author Nazar Vladyka
     */
    @Override
    public LocationVO findById(Long id) {
        log.info(LogMessage.IN_FIND_BY_ID, id);

        Location location = locationRepo
            .findById(id)
            .orElseThrow(
                () -> new NotFoundException(ErrorMessage.LOCATION_NOT_FOUND_BY_ID + id));
        return modelMapper.map(location, LocationVO.class);
    }

    /**
     * {@inheritDoc}
     *
     * @author Nazar Vladyka
     */
    @Override
    public LocationVO save(LocationVO location) {
        log.info(LogMessage.IN_SAVE, location);
        Location savedLocation = locationRepo.save(modelMapper.map(location, Location.class));
        return modelMapper.map(savedLocation, LocationVO.class);
    }

    /**
     * {@inheritDoc}
     *
     * @author Nazar Vladyka
     */
    @Override
    public LocationVO update(Long id, LocationVO location) {
        log.info(LogMessage.IN_UPDATE, location);

        Location updatable = locationRepo.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.LOCATION_NOT_FOUND_BY_ID + id));

        updatable.setLat(location.getLat());
        updatable.setLng(location.getLng());
        updatable.setAddress(location.getAddress());
        updatable.setAddressUa(location.getAddressUa());
        Location savedLocation = locationRepo.save(updatable);

        return modelMapper.map(savedLocation, LocationVO.class);
    }

    /**
     * {@inheritDoc}
     *
     * @author Nazar Vladyka
     */
    @Override
    public Long deleteById(Long id) {
        log.info(LogMessage.IN_DELETE_BY_ID, id);

        locationRepo.delete(modelMapper.map(findById(id), Location.class));
        return id;
    }

    /**
     * {@inheritDoc}
     *
     * @author Kateryna Horokh
     */
    @Override
    public Optional<LocationVO> findByLatAndLng(Double lat, Double lng) {
        Optional<Location> byLatAndLng = locationRepo.findByLatAndLng(lat, lng);
        if (byLatAndLng.isPresent()) {
            Location location = byLatAndLng.get();
            LocationVO locationVO = modelMapper.map(location, LocationVO.class);
            return Optional.of(locationVO);
        }
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     *
     * @author Hrenevych Ivan
     */
    @Override
    public boolean existsByLatAndLng(Double lat, Double lng) {
        return locationRepo.existsByLatAndLng(lat, lng);
    }
}
