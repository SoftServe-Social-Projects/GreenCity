package greencity.service.impl;

import greencity.constant.ErrorMessage;
import greencity.constant.LogMessage;
import greencity.entity.OpeningHours;
import greencity.entity.Place;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.OpenHoursRepo;
import greencity.service.BreakTimeService;
import greencity.service.OpenHoursService;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * The class provides implementation of the {@code OpenHoursService}.
 */
@Slf4j
@AllArgsConstructor
@Service
public class OpenHoursServiceImpl implements OpenHoursService {
    /**
     * Autowired repository.
     */
    private OpenHoursRepo hoursRepo;

    private BreakTimeService breakTimeService;

    /**
     * {@inheritDoc}
     *
     * @author Roman Zahorui
     */
    public List<OpeningHours> getOpenHoursByPlace(Place place) {
        return hoursRepo.findAllByPlace(place);
    }

    /**
     * {@inheritDoc}
     *
     * @author Kateryna Horokh
     */
    @Override
    public OpeningHours save(OpeningHours hours) {
        log.info(LogMessage.IN_SAVE);

        if (hours.getOpenTime().getHour() > hours.getCloseTime().getHour()) {
            throw new BadRequestException(ErrorMessage.CLOSE_TIME_LATE_THAN_OPEN_TIME);
        }

        if (hours.getBreakTime() != null) {
            if (hours.getBreakTime().getStartTime().getHour() > hours.getOpenTime().getHour()
                && hours.getBreakTime().getEndTime().getHour() < hours.getCloseTime().getHour()) {
                breakTimeService.save(hours.getBreakTime());
            } else {
                throw new BadRequestException(ErrorMessage.WRONG_BREAK_TIME);
            }
        }
        return hoursRepo.save(hours);
    }

    /**
     * {@inheritDoc}
     *
     * @author Nazar Vladyka
     */
    @Override
    public List<OpeningHours> findAll() {
        log.info(LogMessage.IN_FIND_ALL);

        return hoursRepo.findAll();
    }

    /**
     * {@inheritDoc}
     *
     * @author Nazar Vladyka
     */
    @Override
    public OpeningHours findById(Long id) {
        log.info(LogMessage.IN_FIND_BY_ID, id);

        return hoursRepo
            .findById(id)
            .orElseThrow(
                () -> new NotFoundException(ErrorMessage.OPEN_HOURS_NOT_FOUND_BY_ID + id));
    }

    /**
     * {@inheritDoc}
     *
     * @author Nazar Vladyka
     */
    @Override
    public OpeningHours update(Long id, OpeningHours updatedHours) {
        log.info(LogMessage.IN_UPDATE);

        OpeningHours updatable = findById(id);

        updatable.setOpenTime(updatedHours.getOpenTime());
        updatable.setCloseTime(updatedHours.getCloseTime());
        updatable.setWeekDay(updatedHours.getWeekDay());
        updatable.setPlace(updatedHours.getPlace());

        return hoursRepo.save(updatable);
    }

    /**
     * {@inheritDoc}
     *
     * @author Nazar Vladyka
     */
    @Override
    public Long deleteById(Long id) {
        log.info(LogMessage.IN_DELETE_BY_ID, id);

        hoursRepo.delete(findById(id));
        return id;
    }

    /**
     * {@inheritDoc}
     *
     * @author Kateryna Horokh
     */
    @Override
    public Set<OpeningHours> findAllByPlaceId(Long placeId) {
        return hoursRepo.findAllByPlaceId(placeId);
    }

    /**
     * {@inheritDoc}
     *
     * @author Kateryna Horokh
     */
    @Override
    public void deleteAllByPlaceId(Long placeId) {
        hoursRepo.deleteAllByPlaceId(placeId);
    }
}
