package greencity.service.impl;

import greencity.constant.ErrorMessage;
import greencity.dto.advice.AdviceDTO;
import greencity.dto.advice.AdvicePostDTO;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.entity.Advice;
import greencity.entity.localization.AdviceTranslation;
import greencity.exception.exceptions.NotDeletedException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.NotUpdatedException;
import greencity.repository.AdviceRepo;
import greencity.repository.AdviceTranslationRepo;
import greencity.repository.HabitDictionaryRepo;
import greencity.service.AdviceService;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link AdviceService}.
 *
 * @author Vitaliy Dzen
 */
@Service
public class AdviceServiceImpl implements AdviceService {
    private final AdviceRepo adviceRepo;
    private final HabitDictionaryRepo habitDictionaryRepo;
    private final AdviceTranslationRepo adviceTranslationRepo;

    private final ModelMapper modelMapper;

    /**
     * Constructor with parameters.
     *
     * @author Vitaliy Dzen
     */
    @Autowired
    public AdviceServiceImpl(AdviceRepo adviceRepo, HabitDictionaryRepo habitDictionaryRepo,
                             AdviceTranslationRepo adviceTranslationRepo, ModelMapper modelMapper) {
        this.adviceRepo = adviceRepo;
        this.habitDictionaryRepo = habitDictionaryRepo;
        this.adviceTranslationRepo = adviceTranslationRepo;
        this.modelMapper = modelMapper;
    }

    /**
     * Method finds all {@link Advice}.
     *
     * @return List of all {@link Advice}
     * @author Vitaliy Dzen
     */
    @Override
    public List<LanguageTranslationDTO> getAllAdvices() {
        return modelMapper.map(adviceTranslationRepo.findAll(), new TypeToken<List<LanguageTranslationDTO>>() {
        }.getType());
    }

    /**
     * Method finds random {@link Advice}.
     *
     * @return random {@link Advice}
     * @author Vitaliy Dzen
     */
    @Override
    public LanguageTranslationDTO getRandomAdviceByHabitIdAndLanguage(Long id, String language) {
        return modelMapper.map(adviceTranslationRepo.getRandomAdviceTranslationByHabitIdAndLanguage(language, id)
            .orElseThrow(() ->
                new NotFoundException(ErrorMessage.ADVICE_NOT_FOUND_BY_ID + id)), LanguageTranslationDTO.class);
    }

    /**
     * Method find {@link Advice} by id.
     *
     * @param id of {@link Advice}
     * @return {@link AdviceDTO}
     * @author Vitaliy Dzen
     */
    @Override
    public AdviceDTO getAdviceById(Long id) {
        return modelMapper.map(adviceRepo.findById(id).orElseThrow(() ->
            new NotFoundException(ErrorMessage.ADVICE_NOT_FOUND_BY_ID + id)), AdviceDTO.class);
    }

    /**
     * Method find {@link Advice} by content.
     *
     * @param name of {@link Advice}
     * @return {@link AdviceDTO}
     * @author Vitaliy Dzen
     */
    @Override
    public AdviceDTO getAdviceByName(String language, String name) {
        return modelMapper.map(adviceTranslationRepo
            .findAdviceTranslationByLanguageCodeAndAdvice(language, name).orElseThrow(() ->
                new NotFoundException(ErrorMessage.ADVICE_NOT_FOUND_BY_NAME + name)), AdviceDTO.class);
    }

    /**
     * Method saves new {@link Advice}.
     *
     * @param advicePostDTO {@link AdviceDTO}
     * @return instance of {@link Advice}
     * @author Vitaliy Dzen
     */
    @Override
    public Advice save(AdvicePostDTO advicePostDTO) {
        return adviceRepo.save(modelMapper.map(advicePostDTO, Advice.class));
    }

    /**
     * Method updates {@link Advice}.
     *
     * @param advicePostDTO {@link AdviceDTO} Object
     * @return instance of {@link Advice}
     * @author Vitaliy Dzen
     */
    @Override
    public Advice update(AdvicePostDTO advicePostDTO, Long id) {
        Advice advice = adviceRepo.findById(id).orElseThrow(
            () -> new NotUpdatedException(ErrorMessage.ADVICE_NOT_UPDATED));
        adviceTranslationRepo.deleteAll(advice.getTranslations());
        Advice saveAdvice = Advice.builder()
            .id(id)
            .habitDictionary(habitDictionaryRepo.findById(advicePostDTO.getHabitDictionary().getId())
                .orElseThrow(() -> new NotUpdatedException(ErrorMessage.ADVICE_NOT_UPDATED)))
            .translations(modelMapper.map(advicePostDTO.getTranslations(),
                new TypeToken<List<AdviceTranslation>>() {
                }.getType()))
            .build();
        saveAdvice.getTranslations()
            .forEach(adviceTranslation -> adviceTranslation.setAdvice(saveAdvice));

        adviceRepo.save(saveAdvice);
        adviceTranslationRepo.saveAll(saveAdvice.getTranslations());
        return saveAdvice;
    }

    /**
     * Method delete {@link Advice} by id.
     *
     * @param id Long
     * @return id of deleted {@link Advice}
     * @author Vitaliy Dzen
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
}
