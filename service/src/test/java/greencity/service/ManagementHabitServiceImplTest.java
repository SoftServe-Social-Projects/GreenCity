package greencity.service;

import greencity.ModelUtils;
import greencity.constant.AppConstant;
import greencity.dto.PageableHabitManagementDto;
import greencity.dto.habit.HabitManagementDto;
import greencity.dto.habit.HabitVO;
import greencity.dto.habittranslation.HabitTranslationManagementDto;
import greencity.dto.language.LanguageDTO;
import greencity.entity.Habit;
import greencity.entity.HabitTranslation;
import greencity.entity.Language;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.HabitRepo;
import greencity.repository.HabitTranslationRepo;
import greencity.repository.UserActionRepo;
import greencity.repository.options.HabitFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static greencity.ModelUtils.getHabit;
import static greencity.ModelUtils.getHabitManagementDtoWithDefaultImage;
import static greencity.ModelUtils.getHabitManagementDtoWithoutImage;
import static greencity.ModelUtils.getHabitWithDefaultImage;
import static greencity.ModelUtils.getLanguage;
import static greencity.ModelUtils.getLanguageDTO;
import static greencity.ModelUtils.getSortModel;
import static greencity.ModelUtils.getSortedPageable;
import static greencity.TestConst.LANGUAGE_CODE_EN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ManagementHabitServiceImplTest {

    @Mock
    private HabitRepo habitRepo;
    @Mock
    private HabitTranslationRepo habitTranslationRepo;
    @Mock
    private LanguageService languageService;
    @Mock
    private HabitAssignService habitAssignService;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private ManagementHabitServiceImpl managementHabitService;
    @Mock
    private FileService fileService;
    @Mock
    private UserActionRepo userActionRepo;

    @Test
    void getByIdTest() {
        Habit habit = ModelUtils.getHabit();
        HabitManagementDto habitManagementDto = ModelUtils.gethabitManagementDto();
        when(habitRepo.findById(1L))
            .thenReturn(Optional.of(habit));
        when(modelMapper.map(habit, HabitManagementDto.class))
            .thenReturn(habitManagementDto);
        HabitManagementDto result = managementHabitService.getById(1L);
        assertEquals(1, (long) result.getId());
    }

    @Test
    void getAllHabitsDtoTest() {
        Pageable sortedPageable = getSortedPageable();
        HabitManagementDto habitManagementDto = getHabitManagementDtoWithoutImage();
        List<HabitManagementDto> habitManagementDtos = Collections.singletonList(habitManagementDto);
        List<Habit> habits = Collections.singletonList(ModelUtils.getHabit());
        Page<Habit> listHabits = new PageImpl<>(habits, sortedPageable, habits.size());

        String sorted = getSortModel(sortedPageable);

        PageableHabitManagementDto<HabitManagementDto> result =
            new PageableHabitManagementDto<>(habitManagementDtos, habitManagementDtos.size(), 0, 1, sorted);
        when(modelMapper.map(habits.getFirst(), HabitManagementDto.class)).thenReturn(habitManagementDtos.getFirst());
        when(habitRepo.findAll(any(HabitFilter.class), eq(sortedPageable))).thenReturn(listHabits);
        assertEquals(result, managementHabitService.getAllHabitsDto(null, null, null,
            null, false, false, sortedPageable));
    }

    @Test
    void getAllHabitsDtoUnsortedPageableTest() {
        Pageable unsortedPageable = PageRequest.of(0, 10);
        HabitManagementDto habitManagementDto = getHabitManagementDtoWithoutImage();
        List<HabitManagementDto> habitManagementDtos = Collections.singletonList(habitManagementDto);
        List<Habit> habits = Collections.singletonList(ModelUtils.getHabit());
        Page<Habit> listHabits = new PageImpl<>(habits, unsortedPageable, habits.size());

        String sorted = getSortModel(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")));

        PageableHabitManagementDto<HabitManagementDto> result =
            new PageableHabitManagementDto<>(habitManagementDtos, habitManagementDtos.size(), 0, 1, sorted);
        when(modelMapper.map(habits.getFirst(), HabitManagementDto.class)).thenReturn(habitManagementDtos.getFirst());
        when(habitRepo.findAll(any(HabitFilter.class), eq(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))))
            .thenReturn(listHabits);
        assertEquals(result, managementHabitService.getAllHabitsDto(null, null, null,
            null, false, false, unsortedPageable));
    }

    @Test
    void saveHabitAndTranslationsTest() {
        when(languageService.findByCode("en")).thenReturn(LanguageDTO.builder().id(1L).code("en").build());
        when(modelMapper.map(languageService.findByCode("en"),
            Language.class)).thenReturn(Language.builder().id(1L).code("en").build());
        HabitManagementDto habitManagementDto = HabitManagementDto.builder().id(1L)
            .image(AppConstant.DEFAULT_HABIT_IMAGE)
            .habitTranslations(List.of(
                HabitTranslationManagementDto.builder().habitItem("Item").description("Description").languageCode("en")
                    .name("Name").build()))
            .build();
        Habit habit = Habit.builder()
            .image(AppConstant.DEFAULT_HABIT_IMAGE)
            .isDeleted(false)
            .isCustomHabit(true)
            .habitTranslations(
                habitManagementDto.getHabitTranslations().stream()
                    .map(habitTranslationDto -> HabitTranslation.builder()
                        .description(habitTranslationDto.getDescription())
                        .habitItem(habitTranslationDto.getHabitItem())
                        .name(habitTranslationDto.getName())
                        .language(modelMapper.map(
                            languageService.findByCode(habitTranslationDto.getLanguageCode()),
                            Language.class))
                        .build())
                    .toList())
            .build();
        habit.getHabitTranslations().forEach(habitTranslation -> habitTranslation.setHabit(habit));

        when(habitRepo.save(habit)).thenReturn(habit);
        when(habitTranslationRepo.saveAll(habit.getHabitTranslations())).thenReturn(habit.getHabitTranslations());
        when(modelMapper.map(habit, HabitManagementDto.class)).thenReturn(habitManagementDto);
        assertEquals(habitManagementDto, managementHabitService.saveHabitAndTranslations(habitManagementDto, null));

    }

    @Test
    void updateTest() {
        when(habitRepo.findById(1L)).thenReturn(Optional.of(Habit.builder().id(1L).habitTranslations(List
            .of(HabitTranslation.builder().habitItem("Item").description("Description")
                .language(Language.builder().id(1L).code("en").build()).name("Name").build()))
            .build()));
        HabitManagementDto habitManagementDto = HabitManagementDto.builder().id(1L).image("image")
            .habitTranslations(List.of(
                HabitTranslationManagementDto.builder().habitItem("Item").description("Description")
                    .languageCode("en").name("Name").build()))
            .build();
        Map<String, HabitTranslationManagementDto> managementDtoMap = habitManagementDto.getHabitTranslations().stream()
            .collect(Collectors.toMap(HabitTranslationManagementDto::getLanguageCode,
                Function.identity()));
        Habit habit = habitRepo.findById(1L).orElse(null);
        assert habit != null;
        habit.getHabitTranslations().forEach(
            ht -> enhanceTranslationWithDto(managementDtoMap.get(ht.getLanguage().getCode()), ht));
        when(habitRepo.save(habit)).thenReturn(habit);
        managementHabitService.update(habitManagementDto, null);
        verify(habitRepo, times(1)).save(habit);
    }

    private void enhanceTranslationWithDto(HabitTranslationManagementDto htDto, HabitTranslation ht) {
        ht.setDescription(htDto.getDescription());
        ht.setHabitItem(htDto.getHabitItem());
        ht.setName(htDto.getName());
    }

    @Test
    void deleteTest() {
        Habit habit = Habit.builder().id(1L).build();
        HabitVO habitVO = HabitVO.builder().id(1L).build();

        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(modelMapper.map(habit, HabitVO.class)).thenReturn(habitVO);

        managementHabitService.delete(1L);

        verify(habitRepo, times(1)).findById(1L);
        verify(userActionRepo, times(1)).deleteAllByHabitId(1L);
        verify(habitTranslationRepo, times(1)).deleteAllByHabit(habit);
        verify(habitAssignService, times(1)).deleteAllHabitAssignsByHabit(habitVO);
        verify(habitRepo, times(1)).delete(habit);
    }

    @Test
    void deleteAllTest() {
        Habit habit = new Habit();
        HabitVO habitVO = new HabitVO();
        List<Long> list = new ArrayList<>();
        list.add(1L);
        list.add(2L);
        list.add(3L);
        list.add(4L);
        list.add(5L);
        when(habitRepo.findById(anyLong())).thenReturn(Optional.of(habit));
        when(modelMapper.map(habit, HabitVO.class)).thenReturn(habitVO);
        doNothing().when(habitTranslationRepo).deleteAllByHabit(habit);
        doNothing().when(habitAssignService).deleteAllHabitAssignsByHabit(habitVO);
        doNothing().when(habitRepo).delete(habit);
        managementHabitService.deleteAll(list);
        verify(habitRepo, times(5)).delete(any(Habit.class));
        verify(habitTranslationRepo, times(5)).deleteAllByHabit(any(Habit.class));

    }

    @Test
    void successfulUploadImageForHabitTest() {
        HabitManagementDto habitManagementDto = ModelUtils.getHabitManagementDtoWithTranslation();
        MultipartFile imageFile = new MockMultipartFile("image.jpg", "some-image-content".getBytes());
        when(fileService.upload(imageFile)).thenReturn("image-url");

        managementHabitService.saveHabitAndTranslations(habitManagementDto, imageFile);
        assertEquals("https://example.com/sample-image.jpg", habitManagementDto.getImage());

        verify(fileService, times(1)).upload(imageFile);
    }

    @Test
    void saveHabitAndTranslationsWhenImageNullTest() {
        var languageDTO = getLanguageDTO();
        var language = getLanguage();
        var habitManagementDtoWithoutImage = getHabitManagementDtoWithoutImage();
        var habitManagementDtoWithDefaultImage = getHabitManagementDtoWithDefaultImage();
        var habit = getHabitWithDefaultImage();

        when(languageService.findByCode(LANGUAGE_CODE_EN)).thenReturn(languageDTO);
        when(modelMapper.map(languageDTO, Language.class)).thenReturn(language);

        when(habitRepo.save(habit)).thenReturn(habit);
        when(modelMapper.map(habit, HabitManagementDto.class)).thenReturn(habitManagementDtoWithDefaultImage);

        var result = managementHabitService.saveHabitAndTranslations(habitManagementDtoWithoutImage, null);

        assertEquals(AppConstant.DEFAULT_HABIT_IMAGE, result.getImage());

        verify(languageService).findByCode(LANGUAGE_CODE_EN);
        verify(modelMapper).map(languageDTO, Language.class);
        verify(habitRepo).save(habit);
        verify(modelMapper).map(habit, HabitManagementDto.class);
    }

    @Test
    void saveHabitAndTranslationsWhenImageEmptyTest() {
        var languageDTO = getLanguageDTO();
        var language = getLanguage();
        var habitManagementDtoWithoutImage = getHabitManagementDtoWithoutImage();
        var habitManagementDtoWithDefaultImage = getHabitManagementDtoWithDefaultImage();
        var habit = getHabitWithDefaultImage();

        MultipartFile emptyImage = mock(MultipartFile.class);

        when(emptyImage.isEmpty()).thenReturn(true);
        when(languageService.findByCode(LANGUAGE_CODE_EN)).thenReturn(languageDTO);
        when(modelMapper.map(languageDTO, Language.class)).thenReturn(language);

        when(habitRepo.save(habit)).thenReturn(habit);
        when(modelMapper.map(habit, HabitManagementDto.class)).thenReturn(habitManagementDtoWithDefaultImage);

        var result = managementHabitService.saveHabitAndTranslations(habitManagementDtoWithoutImage, emptyImage);

        assertEquals(AppConstant.DEFAULT_HABIT_IMAGE, result.getImage());

        verify(languageService).findByCode(LANGUAGE_CODE_EN);
        verify(modelMapper).map(languageDTO, Language.class);
        verify(habitRepo).save(habit);
        verify(modelMapper).map(habit, HabitManagementDto.class);
        verify(emptyImage).isEmpty();
    }

    @Test
    void switchIsDeletedStatusTest() {
        Habit habit = getHabit();
        habit.setIsDeleted(false);
        Boolean newStatus = true;

        when(habitRepo.findById(anyLong())).thenReturn(Optional.of(habit));

        managementHabitService.switchIsDeletedStatus(1L, newStatus);

        assertTrue(habit.getIsDeleted());
        verify(habitRepo).save(habit);
    }

    @Test
    void switchIsDeletedStatusToFalseTest() {
        Habit habit = getHabit();
        Boolean newStatus = false;
        habit.setIsDeleted(true);

        when(habitRepo.findById(anyLong())).thenReturn(Optional.of(habit));

        managementHabitService.switchIsDeletedStatus(1L, newStatus);

        assertFalse(habit.getIsDeleted());
        verify(habitRepo).save(habit);
    }

    @Test
    void switchIsDeletedStatusWhenHabitIsNotFoundTest() {
        Long habitId = 1L;
        Boolean newStatus = false;

        when(habitRepo.findById(habitId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> managementHabitService.switchIsDeletedStatus(habitId, newStatus));
        verify(habitRepo).findById(habitId);
    }

    @Test
    void switchIsCustomStatusTest() {
        Habit habit = getHabit();
        habit.setIsCustomHabit(false);
        Boolean newIsCustomStatus = true;

        when(habitRepo.findById(anyLong())).thenReturn(Optional.of(habit));

        managementHabitService.switchIsCustomStatus(1L, newIsCustomStatus);

        assertTrue(habit.getIsCustomHabit());
        verify(habitRepo).save(habit);
    }

    @Test
    void switchIsCustomStatusWhenHabitIsNotFoundTest() {
        Long habitId = 1L;
        Boolean newIsCustomStatus = false;

        when(habitRepo.findById(habitId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> managementHabitService.switchIsCustomStatus(habitId, newIsCustomStatus));
        verify(habitRepo).findById(habitId);
    }
}
