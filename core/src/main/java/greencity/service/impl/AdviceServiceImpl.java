package greencity.service.impl;

import greencity.constant.ErrorMessage;
import greencity.dto.advice.AdviceDto;
import greencity.dto.advice.AdviceVO;
import greencity.dto.advice.AdvicePostDto;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.entity.Advice;
import greencity.entity.Habit;
import greencity.exception.exceptions.NotDeletedException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.NotUpdatedException;
import greencity.exception.exceptions.WrongIdException;
import greencity.repository.AdviceRepo;
import greencity.repository.AdviceTranslationRepo;
import greencity.repository.HabitRepo;
import greencity.service.AdviceService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link AdviceService}.
 *
 * @author Vitaliy Dzen
 */
@Service
@AllArgsConstructor
public class AdviceServiceImpl implements AdviceService {
    private final AdviceRepo adviceRepo;
    private final HabitRepo habitRepo;
    private final AdviceTranslationRepo adviceTranslationRepo;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LanguageTranslationDTO> getAllAdvices() {
        return modelMapper.map(adviceTranslationRepo.findAll(), new TypeToken<List<LanguageTranslationDTO>>() {
        }.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LanguageTranslationDTO getRandomAdviceByHabitIdAndLanguage(Long id, String language) {
        return modelMapper.map(adviceTranslationRepo.getRandomAdviceTranslationByHabitIdAndLanguage(language, id)
            .orElseThrow(() ->
                new NotFoundException(ErrorMessage.ADVICE_NOT_FOUND_BY_ID + id)), LanguageTranslationDTO.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AdviceDto getAdviceById(Long id) {
        return modelMapper.map(adviceRepo.findById(id).orElseThrow(() ->
            new NotFoundException(ErrorMessage.ADVICE_NOT_FOUND_BY_ID + id)), AdviceDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AdviceDto getAdviceByName(String language, String name) {
        return modelMapper.map(adviceTranslationRepo
            .findAdviceTranslationByLanguageCodeAndAdvice(language, name).orElseThrow(() ->
                new NotFoundException(ErrorMessage.ADVICE_NOT_FOUND_BY_NAME + name)), AdviceDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AdviceVO save(AdvicePostDto advicePostDTO) {
        Advice saved = adviceRepo.save(modelMapper.map(advicePostDTO, Advice.class));
        return modelMapper.map(saved, AdviceVO.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AdviceVO update(AdvicePostDto adviceDto, Long id) {
        Advice advice = adviceRepo.findById(id)
            .map(employee -> {
                Habit habit = habitRepo.findById(adviceDto.getHabit().getId())
                    .orElseThrow(() -> new WrongIdException(ErrorMessage.HABIT_NOT_FOUND_BY_ID));
                employee.setHabit(habit);
                return adviceRepo.save(employee);
            })
            .orElseThrow(() -> new NotUpdatedException(ErrorMessage.ADVICE_NOT_UPDATED));
        return modelMapper.map(advice, AdviceVO.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long delete(Long id) {
        try {
            adviceRepo.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotDeletedException(ErrorMessage.ADVICE_NOT_DELETED);
        }
        return id;
    }

    /*@Override
    public void deleteAllByHabit(Habit habit) {
    }*/
}