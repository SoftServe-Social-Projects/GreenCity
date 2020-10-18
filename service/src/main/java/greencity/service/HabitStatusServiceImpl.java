package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.habit.HabitAssignDto;
import greencity.dto.habitstatus.HabitStatusDto;
import greencity.dto.habitstatus.UpdateHabitStatusDto;
import greencity.dto.habitstatuscalendar.HabitStatusCalendarVO;
import greencity.entity.HabitAssign;
import greencity.entity.HabitStatus;
import greencity.entity.HabitStatusCalendar;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotDeletedException;
import greencity.exception.exceptions.NotUpdatedException;
import greencity.exception.exceptions.WrongIdException;
import greencity.repository.HabitStatusRepo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class HabitStatusServiceImpl implements HabitStatusService {
    private final HabitStatusRepo habitStatusRepo;
    private final HabitStatusCalendarService habitStatusCalendarService;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitStatusDto getById(Long habitAssignId) {
        return modelMapper.map(habitStatusRepo.findById(habitAssignId)
                .orElseThrow(() -> new WrongIdException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId)),
            HabitStatusDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveStatusByHabitAssign(HabitAssignDto habitAssign) {
        HabitStatus habitStatus = HabitStatus.builder()
            .habitStreak(0)
            .habitAssign(modelMapper.map(habitAssign, HabitAssign.class))
            .workingDays(0)
            .lastEnrollmentDate(LocalDateTime.now())
            .build();

        habitStatusRepo.save(habitStatus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitStatusDto findStatusByHabitAssignId(Long habitAssignId) {
        return modelMapper.map(habitStatusRepo.findByHabitAssignId(habitAssignId)
                .orElseThrow(() -> new WrongIdException(ErrorMessage.NO_STATUS_FOR_SUCH_HABIT_ASSIGN)),
            HabitStatusDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitStatusDto findActiveStatusByUserIdAndHabitId(Long userId, Long habitId) {
        return modelMapper.map(habitStatusRepo.findByUserIdAndHabitId(userId, habitId)
                .orElseThrow(() -> new WrongIdException(ErrorMessage.USER_HAS_NO_STATUS_FOR_SUCH_HABIT)),
            HabitStatusDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitStatusDto findStatusByUserIdAndHabitIdAndCreateDate(Long userId, Long habitId, ZonedDateTime dateTime) {
        return modelMapper.map(habitStatusRepo.findByUserIdAndHabitIdAndCreateDate(userId, habitId, dateTime)
                .orElseThrow(() -> new WrongIdException(ErrorMessage.USER_HAS_NO_STATUS_FOR_SUCH_HABIT)),
            HabitStatusDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitStatusDto enrollHabit(Long habitAssignId) {
        HabitStatus habitStatus = habitStatusRepo.findByHabitAssignId(habitAssignId)
            .orElseThrow(() -> new WrongIdException(ErrorMessage.USER_HAS_NO_STATUS_FOR_SUCH_HABIT));
        int workingDays = habitStatus.getWorkingDays();
        habitStatus.setWorkingDays(++workingDays);
        LocalDate todayDate = LocalDate.now();
        HabitStatusCalendar habitCalendar;
        LocalDate lastEnrollmentDate =
            habitStatusCalendarService.findTopByEnrollDateAndHabitStatus(
                modelMapper.map(habitStatus, HabitStatusDto.class));
        long intervalBetweenDates = 0;

        if (lastEnrollmentDate != null) {
            intervalBetweenDates = Period.between(lastEnrollmentDate, todayDate).getDays();
        }

        if ((intervalBetweenDates == 1) || lastEnrollmentDate == null) {
            int habitStreak = habitStatus.getHabitStreak();
            habitStatus.setHabitStreak(++habitStreak);
            habitCalendar = new HabitStatusCalendar(todayDate, habitStatus);
            habitStatusCalendarService.save(modelMapper.map(habitCalendar, HabitStatusCalendarVO.class));
        } else if (intervalBetweenDates > 1) {
            habitStatus.setHabitStreak(1);
            habitCalendar = new HabitStatusCalendar(todayDate, habitStatus);
            habitStatusCalendarService.save(modelMapper.map(habitCalendar, HabitStatusCalendarVO.class));
        } else {
            throw new BadRequestException(ErrorMessage.HABIT_HAS_BEEN_ALREADY_ENROLLED);
        }

        habitStatus.setLastEnrollmentDate(LocalDateTime.now());

        return modelMapper.map(habitStatusRepo.save(habitStatus), HabitStatusDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unenrollHabit(LocalDate date, Long habitAssignId) {
        HabitStatus habitStatus = habitStatusRepo.findByHabitAssignId(habitAssignId)
            .orElseThrow(() -> new WrongIdException(ErrorMessage.USER_HAS_NO_STATUS_FOR_SUCH_HABIT));
        int daysStreak = checkHabitStreakAfterDate(date, habitStatus);
        habitStatus.setHabitStreak(daysStreak + 1);
        int workingDays = habitStatus.getWorkingDays();

        if (workingDays == 0) {
            habitStatus.setWorkingDays(0);
        } else {
            habitStatus.setWorkingDays(--workingDays);
        }

        habitStatusRepo.save(habitStatus);
        HabitStatusCalendarVO habitStatusCalendar =
            habitStatusCalendarService
                .findHabitStatusCalendarByEnrollDateAndHabitStatus(
                    date, modelMapper.map(habitStatus, HabitStatusDto.class));

        if (habitStatusCalendar != null) {
            habitStatusCalendarService.delete(habitStatusCalendar);
        } else {
            throw new BadRequestException(ErrorMessage.HABIT_IS_NOT_ENROLLED);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitStatusDto enrollHabitInDate(Long habitAssignId, LocalDate date) {
        HabitStatus habitStatus = habitStatusRepo.findByHabitAssignId(habitAssignId)
            .orElseThrow(() -> new WrongIdException(ErrorMessage.USER_HAS_NO_STATUS_FOR_SUCH_HABIT));
        HabitStatusCalendarVO habitCalendarOnDate =
            habitStatusCalendarService.findHabitStatusCalendarByEnrollDateAndHabitStatus(date,
                modelMapper.map(habitStatus, HabitStatusDto.class));

        int daysStreakAfterDate = checkHabitStreakAfterDate(date, habitStatus);
        int daysStreakBeforeDate = checkHabitStreakBeforeDate(date, habitStatus);

        if (daysStreakBeforeDate != 0) {
            daysStreakBeforeDate += 1;
        }

        if (habitCalendarOnDate == null) {
            HabitStatusCalendar habitStatusCalendar = new HabitStatusCalendar(date, habitStatus);
            habitStatusCalendarService.save(modelMapper.map(habitStatusCalendar, HabitStatusCalendarVO.class));

            if (Period.between(date, LocalDate.now()).getDays() == daysStreakAfterDate + 1) {
                if ((daysStreakAfterDate + daysStreakBeforeDate) == 1) {
                    habitStatus.setHabitStreak(daysStreakAfterDate + daysStreakBeforeDate + 1);
                } else {
                    habitStatus.setHabitStreak(daysStreakAfterDate + daysStreakBeforeDate + 2);
                }
            }

            habitStatus.setWorkingDays(habitStatus.getWorkingDays() + 1);
            return modelMapper.map(habitStatusRepo.save(habitStatus), HabitStatusDto.class);
        } else {
            throw new BadRequestException(ErrorMessage.HABIT_HAS_BEEN_ALREADY_IN_THAT_DAY);
        }
    }

    private int checkHabitStreakAfterDate(LocalDate dateTime, HabitStatus habitStatus) {
        int daysStreak = 0;

        List<LocalDate> enrollDates = habitStatusCalendarService.findEnrolledDatesAfter(dateTime,
            modelMapper.map(habitStatus, HabitStatusDto.class));
        Collections.sort(enrollDates);

        for (int i = 0; i < enrollDates.size() - 1; i++) {
            if (Period.between(enrollDates.get(i), enrollDates.get(i + 1)).getDays() == 1) {
                daysStreak++;
            } else {
                daysStreak = 0;
            }
        }
        return daysStreak;
    }

    private int checkHabitStreakBeforeDate(LocalDate dateTime, HabitStatus habitStatus) {
        int daysStreak = 0;

        List<LocalDate> enrollDates = habitStatusCalendarService.findEnrolledDatesBefore(dateTime,
            modelMapper.map(habitStatus, HabitStatusDto.class));
        Collections.sort(enrollDates);
        Collections.reverse(enrollDates);

        for (int i = 0; i < enrollDates.size() - 1; i++) {
            if (Period.between(enrollDates.get(i + 1), enrollDates.get(i)).getDays() == 1) {
                daysStreak++;
            } else {
                return daysStreak;
            }
        }

        return daysStreak;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void deleteStatusByHabitAssignId(Long habitAssignId) {
        HabitStatus habitStatus = habitStatusRepo.findByHabitAssignId(habitAssignId)
            .orElseThrow(() -> new NotDeletedException(ErrorMessage.STATUS_OF_HABIT_ASSIGN_NOT_DELETED));
        habitStatusCalendarService.deleteAllByHabitStatus(modelMapper.map(habitStatus, HabitStatusDto.class));
        habitStatusRepo.deleteByHabitAssignId(habitAssignId);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void deleteActiveStatusByUserIdAndHabitId(Long userId, Long habitId) {
        HabitStatus habitStatus = habitStatusRepo.findByUserIdAndHabitId(userId, habitId)
            .orElseThrow(() -> new NotDeletedException(ErrorMessage.STATUS_OF_HABIT_ASSIGN_NOT_DELETED));
        habitStatusCalendarService.deleteAllByHabitStatus(modelMapper.map(habitStatus, HabitStatusDto.class));
        habitStatusRepo.deleteByUserIdAndHabitId(userId, habitId);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void deleteStatusByUserIdAndHabitIdAndAssignCreateDate(Long userId, Long habitId,
                                                                  ZonedDateTime zonedDateTime) {
        HabitStatus habitStatus = habitStatusRepo.findByUserIdAndHabitId(userId, habitId)
            .orElseThrow(() -> new NotDeletedException(ErrorMessage.STATUS_OF_HABIT_ASSIGN_NOT_DELETED));
        habitStatusCalendarService.deleteAllByHabitStatus(modelMapper.map(habitStatus, HabitStatusDto.class));
        habitStatusRepo.deleteByUserIdAndHabitId(userId, habitId);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public HabitStatusDto update(Long habitAssignId, UpdateHabitStatusDto dto) {
        HabitStatus updatable = habitStatusRepo.findByHabitAssignId(habitAssignId)
            .orElseThrow(() -> new NotUpdatedException(ErrorMessage.STATUS_OF_HABIT_ASSIGN_NOT_UPDATED));

        updatable.setHabitStreak(dto.getHabitStreak());
        updatable.setLastEnrollmentDate(dto.getLastEnrollmentDate());
        updatable.setWorkingDays(dto.getWorkingDays());

        return modelMapper.map(habitStatusRepo.save(updatable), HabitStatusDto.class);
    }
}
