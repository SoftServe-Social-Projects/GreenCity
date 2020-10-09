package greencity.service.impl;

import greencity.entity.HabitStatus;
import greencity.entity.HabitStatusCalendar;
import greencity.repository.HabitStatusCalendarRepo;
import greencity.service.HabitStatusCalendarService;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class HabitStatusCalendarServiceImpl implements HabitStatusCalendarService {
    private final HabitStatusCalendarRepo habitStatusCalendarRepo;

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(HabitStatusCalendar habitStatusCalendar) {
        habitStatusCalendarRepo.save(habitStatusCalendar);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitStatusCalendar findHabitStatusCalendarByEnrollDateAndHabitStatus(LocalDate date,
                                                                                 HabitStatus habitStatus) {
        return habitStatusCalendarRepo.findHabitStatusCalendarByEnrollDateAndHabitStatus(date, habitStatus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(HabitStatusCalendar habitStatusCalendar) {
        habitStatusCalendarRepo.delete(habitStatusCalendar);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDate findTopByEnrollDateAndHabitStatus(HabitStatus habitStatus) {
        return habitStatusCalendarRepo.findTopByEnrollDateAndHabitStatus(habitStatus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LocalDate> findEnrolledDatesAfter(LocalDate dateTime, HabitStatus habitStatus) {
        List<HabitStatusCalendar> habitStatusCalendars =
            habitStatusCalendarRepo.findAllByEnrollDateAfterAndHabitStatus(dateTime, habitStatus);
        List<LocalDate> dates = new LinkedList<>();
        habitStatusCalendars.forEach(habitStatusCalendar -> dates.add(habitStatusCalendar.getEnrollDate()));

        return dates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LocalDate> findEnrolledDatesBefore(LocalDate dateTime, HabitStatus habitStatus) {
        List<HabitStatusCalendar> habitStatusCalendars =
            habitStatusCalendarRepo.findAllByEnrollDateBeforeAndHabitStatus(dateTime, habitStatus);
        List<LocalDate> dates = new LinkedList<>();
        habitStatusCalendars.forEach(habitStatusCalendar -> dates.add(habitStatusCalendar.getEnrollDate()));

        return dates;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void deleteAllByHabitStatus(HabitStatus habitStatus) {
        habitStatusCalendarRepo.deleteAllByHabitStatusId(habitStatus.getId());
    }
}
