package greencity.service;

import greencity.dto.PageableHabitManagementDto;
import greencity.dto.habit.HabitDto;
import greencity.dto.habit.HabitManagementDto;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ManagementHabitService {
    /**
     * Method finds {@code Habit} with all {@code HabitTranslation}'s by id.
     *
     * @param id {@code Habit} id.
     * @return {@link HabitManagementDto}.
     */
    HabitManagementDto getById(Long id);

    /**
     * Method finds all {@code Habit} with all {@code HabitTranslation}'s.
     *
     * @param pageable - instance of {@link Pageable}.
     * @return list of {@link HabitManagementDto}.
     * @author Dovganyuk Taras
     */
    PageableHabitManagementDto<HabitManagementDto> getAllHabitsDto(String searchReg, Integer durationFrom,
        Integer durationTo, Integer complexity, Boolean withoutImage,
        Boolean withImage,
        Pageable pageable);

    /**
     * Method saves {@code Habit} with it's {@code HabitTranslation}'s.
     *
     * @param habitManagementDto {@link HabitManagementDto}.
     * @param image              {@link MultipartFile} image for habit.
     * @return {@link HabitDto}.
     */
    HabitManagementDto saveHabitAndTranslations(HabitManagementDto habitManagementDto, MultipartFile image);

    /**
     * Method updates {@code Habit} instance.
     *
     * @param habitManagementDto - instance of {@link HabitManagementDto}.
     * @param image              {@link MultipartFile} image for habit.
     */
    void update(HabitManagementDto habitManagementDto, MultipartFile image);

    /**
     * Method deletes {@code Habit} instance by it's id.
     *
     * @param id {@code Habit} id.
     */
    void delete(Long id);

    /**
     * Method deletes all {@code Habit} instances by list of id's.
     *
     * @param listId list of {@code Habit} id's.
     */
    void deleteAll(List<Long> listId);

    /**
     * Updates the `isDeleted` status of a {@code Habit}. If `isDeleted` is
     * {@code null}, it is set to the provided {@code newStatus}. Otherwise, it is
     * updated directly to {@code newStatus}.
     *
     * @param id        the ID of the {@code Habit} to update.
     * @param newStatus the new `isDeleted` status, {@code true} or {@code false}.
     */
    void switchIsDeletedStatus(Long id, Boolean newStatus);

    /**
     * Updates the `isCustom` status of a {@code Habit}. If `isCustom` is
     * {@code null}, it is set to the provided {@code newIsCustomStatus}. Otherwise,
     * it is updated directly to {@code newIsCustomStatus}.
     *
     * @param id                the ID of the {@code Habit} to update.
     * @param newIsCustomStatus the new `isCustom` status, {@code true} or
     *                          {@code false}.
     */
    void switchIsCustomStatus(Long id, Boolean newIsCustomStatus);
}
