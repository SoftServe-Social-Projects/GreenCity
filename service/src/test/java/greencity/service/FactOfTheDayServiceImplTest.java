package greencity.service;

import greencity.ModelUtils;
import greencity.dto.PageableDto;
import greencity.dto.factoftheday.FactOfTheDayDTO;
import greencity.dto.factoftheday.FactOfTheDayPostDTO;
import greencity.dto.factoftheday.FactOfTheDayTranslationDTO;
import greencity.dto.factoftheday.FactOfTheDayTranslationVO;
import greencity.dto.language.LanguageDTO;
import greencity.dto.tag.TagDto;
import greencity.entity.FactOfTheDay;
import greencity.entity.Language;
import greencity.entity.Tag;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.NotUpdatedException;
import greencity.repository.FactOfTheDayRepo;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static greencity.enums.TagType.FACT_OF_THE_DAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import greencity.repository.TagsRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class FactOfTheDayServiceImplTest {
    @Mock
    private ModelMapper modelMapper;

    @Mock
    private FactOfTheDayTranslationServiceImpl factOfTheDayTranslationService;

    @Mock
    private FactOfTheDayRepo factOfTheDayRepo;

    @Mock
    private LanguageService languageService;

    @Mock
    private FactOfTheDayService service;
    @Mock
    private TagsRepo tagsRepo;

    @InjectMocks
    private FactOfTheDayServiceImpl factOfTheDayService;

    @Test
    void getAllFactsOfTheDayTest() {
        int pageNumber = 0;
        int pageSize = 1;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<FactOfTheDay> factsOfTheDays = Collections.singletonList(ModelUtils.getFactOfTheDay());

        Page<FactOfTheDay> pageFacts = new PageImpl<>(factsOfTheDays,
            pageable, factsOfTheDays.size());

        List<FactOfTheDayDTO> dtoList = Collections.singletonList(
            ModelUtils.getFactOfTheDayDto());

        PageableDto<FactOfTheDayDTO> pageableDto = new PageableDto<>(dtoList, dtoList.size(), 0, 1);

        when(factOfTheDayRepo.findAll(pageable)).thenReturn(pageFacts);
        when(modelMapper.map(factsOfTheDays.getFirst(), FactOfTheDayDTO.class)).thenReturn(dtoList.getFirst());

        PageableDto<FactOfTheDayDTO> actual = factOfTheDayService.getAllFactsOfTheDay(pageable);
        assertEquals(pageableDto, actual);
    }

    @Test
    void getAllFactsOfTheDayFailed() {
        Pageable pageable = PageRequest.of(5, 5);
        when(factOfTheDayRepo.findAll(any(Pageable.class))).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> factOfTheDayService.getAllFactsOfTheDay(pageable));
    }

    @Test
    void saveFactOfTheDayAndTranslationsTest() {
        FactOfTheDayPostDTO factDtoPost = ModelUtils.getFactOfTheDayPostDto();
        Set<Tag> tagDtos = Set.of(ModelUtils.getTag());

        when(tagsRepo.findTagsById(List.of(25L))).thenReturn(tagDtos);

        FactOfTheDayPostDTO res = factOfTheDayService.saveFactOfTheDayAndTranslations(factDtoPost);
        verify(factOfTheDayRepo, times(1)).save(any(FactOfTheDay.class));
        verify(factOfTheDayTranslationService, times(1)).saveAll(anyList());

        assertEquals(factDtoPost, res);
    }

    @Test
    void saveFactOfTheDayAndTranslationsTestFailed() {
        FactOfTheDayPostDTO factDTO = ModelUtils.getFactOfTheDayPostDto();
        FactOfTheDay fact = ModelUtils.getFactOfTheDay();
        Set<Tag> tagDtos = Set.of(ModelUtils.getTag());

        when(tagsRepo.findTagsById(List.of(25L))).thenReturn(tagDtos);
        when(factOfTheDayRepo.save(fact)).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> factOfTheDayService.saveFactOfTheDayAndTranslations(factDTO));
        verify(factOfTheDayTranslationService, times(0)).saveAll(anyList());
    }

    @Test
    void updateFactOfTheDayAndTranslationsTest() {
        LanguageDTO languageDTO = ModelUtils.getLanguageDTO();
        FactOfTheDay dbFact = ModelUtils.getFactOfTheDay();
        Set<Tag> tagDtos = Set.of(ModelUtils.getTag());

        when(factOfTheDayRepo.findById(anyLong())).thenReturn(Optional.of(dbFact));
        when(modelMapper.map(dbFact.getFactOfTheDayTranslations().get(0), FactOfTheDayTranslationVO.class)).thenReturn(
            ModelUtils.getFactOfTheDayTranslationVO());
        when(languageService.findByCode("en")).thenReturn(languageDTO);
        when(factOfTheDayTranslationService.saveAll(anyList())).thenReturn(null);
        when(tagsRepo.findTagsById(List.of(25L))).thenReturn(tagDtos);

        FactOfTheDayPostDTO fact = ModelUtils.getFactOfTheDayPostDto();
        assertEquals(fact, factOfTheDayService.updateFactOfTheDayAndTranslations(fact));
        verify(factOfTheDayRepo, times(1)).findById(anyLong());
        verify(factOfTheDayTranslationService, times(1)).deleteAll(anyList());
        verify(languageService, times(1)).findByCode(anyString());
        verify(modelMapper, times(1)).map(languageDTO, Language.class);
        verify(factOfTheDayRepo, times(1)).save(any(FactOfTheDay.class));
        verify(factOfTheDayTranslationService, times(1)).saveAll(anyList());
        verify(modelMapper, times(1)).map(dbFact.getFactOfTheDayTranslations().get(0), FactOfTheDayTranslationVO.class);
    }

    @Test
    void updateFactOfTheDayAndTranslationsTestFailed() {
        FactOfTheDayPostDTO factDtoPost = ModelUtils.getFactOfTheDayPostDto();
        when(factOfTheDayRepo.findById(anyLong())).thenThrow(NotUpdatedException.class);

        assertThrows(NotUpdatedException.class,
            () -> factOfTheDayService.updateFactOfTheDayAndTranslations(factDtoPost));
    }

    @Test
    void searchByTest() {
        int pageNumber = 0;
        int pageSize = 1;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<FactOfTheDay> factsOfTheDays = Collections.singletonList(ModelUtils.getFactOfTheDay());

        Page<FactOfTheDay> pageFacts = new PageImpl<>(factsOfTheDays,
            pageable, factsOfTheDays.size());

        List<FactOfTheDayDTO> dtoList = Collections.singletonList(
            ModelUtils.getFactOfTheDayDto());

        PageableDto<FactOfTheDayDTO> pageableDto = new PageableDto<>(dtoList, dtoList.size(), 0, 1);

        when(factOfTheDayRepo.searchBy(pageable, "query")).thenReturn(pageFacts);
        when(modelMapper.map(factsOfTheDays.getFirst(), FactOfTheDayDTO.class)).thenReturn(dtoList.getFirst());

        PageableDto<FactOfTheDayDTO> actual = factOfTheDayService.searchBy(pageable, "query");
        assertEquals(pageableDto, actual);
    }

    @Test
    void searchByFailed() {

        int invalidNUmber = 10;
        int invalidSize = 10;
        Pageable pageable = PageRequest.of(invalidNUmber, invalidSize);
        when(factOfTheDayRepo.searchBy(pageable, "invalidQuery")).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> factOfTheDayService.searchBy(pageable, "invalidQuery"));
    }

    @Test
    void deleteAllFactOfTheDayAndTranslationsTest() {
        List<Long> idList = List.of(1L, 2L, 3L);
        FactOfTheDay fact = ModelUtils.getFactOfTheDay();
        FactOfTheDayTranslationVO factOfTheDayTranslationVO = ModelUtils.getFactOfTheDayTranslationVO();
        when(factOfTheDayRepo.findById(anyLong())).thenReturn(Optional.of(fact));
        when(modelMapper.map(fact.getFactOfTheDayTranslations().get(0), FactOfTheDayTranslationVO.class))
            .thenReturn(factOfTheDayTranslationVO);

        assertEquals(idList, factOfTheDayService.deleteAllFactOfTheDayAndTranslations(idList));
        verify(factOfTheDayRepo, times(3)).deleteById(anyLong());
        verify(factOfTheDayTranslationService, times(3)).deleteAll(anyList());
    }

    @Test
    void deleteFactOfTheDayAndTranslationsTest() {
        Long id = 1L;
        FactOfTheDay fact = ModelUtils.getFactOfTheDay();
        FactOfTheDayTranslationVO factOfTheDayTranslationVO = ModelUtils.getFactOfTheDayTranslationVO();
        when(factOfTheDayRepo.findById(anyLong())).thenReturn(Optional.of(fact));
        when(modelMapper.map(fact.getFactOfTheDayTranslations().get(0), FactOfTheDayTranslationVO.class))
            .thenReturn(factOfTheDayTranslationVO);

        assertEquals(id, factOfTheDayService.deleteFactOfTheDayAndTranslations(id));
        verify(factOfTheDayRepo, times(1)).deleteById(anyLong());
        verify(factOfTheDayTranslationService, times(1)).deleteAll(anyList());
    }

    @Test
    void deleteAllFactOfTheDayAndTranslationsTestFailed() {
        List<Long> longs = List.of(1L, 2L, 3L);
        when(factOfTheDayRepo.findById(anyLong())).thenThrow(NotUpdatedException.class);

        assertThrows(NotUpdatedException.class, () -> factOfTheDayService.deleteAllFactOfTheDayAndTranslations(longs));
    }

    @Test
    void deleteFactOfTheDayWithTranslationsTestFailed() {
        Long id = 1L;
        when(factOfTheDayRepo.findById(anyLong())).thenThrow(NotUpdatedException.class);

        assertThrows(NotUpdatedException.class, () -> factOfTheDayService.deleteFactOfTheDayAndTranslations(id));
    }

    @Test
    void getRandomFactOfTheDayByTags_success() {
        Set<Long> tagIds = Set.of(1L, 2L);
        FactOfTheDay factOfTheDay = ModelUtils.getFactOfTheDay();
        FactOfTheDayTranslationDTO expectedDto = ModelUtils.getFactOfTheDayTranslationDTO();

        when(factOfTheDayRepo.getRandomFactOfTheDay(tagIds)).thenReturn(Optional.of(factOfTheDay));
        when(modelMapper.map(factOfTheDay, FactOfTheDayTranslationDTO.class)).thenReturn(expectedDto);

        FactOfTheDayTranslationDTO result =
            factOfTheDayService.getRandomFactOfTheDayByTags(tagIds);

        assertEquals(expectedDto, result);
    }

    @Test
    void getRandomFactOfTheDayByLanguageAndTags_factNotFound() {
        Set<Long> tagIds = Set.of(1L, 2L);

        when(factOfTheDayRepo.getRandomFactOfTheDay(tagIds)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            factOfTheDayService.getRandomFactOfTheDayByTags(tagIds);
        });
    }

    @Test
    void getRandomGeneralFactOfTheDay_success() {
        List<Tag> tags = List.of(ModelUtils.getTag());
        Set<Long> tagIds = Set.of(1L);
        FactOfTheDay factOfTheDay = ModelUtils.getFactOfTheDay();
        FactOfTheDayTranslationDTO expectedDto = ModelUtils.getFactOfTheDayTranslationDTO();

        when(factOfTheDayRepo.getRandomFactOfTheDay(tagIds)).thenReturn(Optional.of(factOfTheDay));
        when(modelMapper.map(factOfTheDay, FactOfTheDayTranslationDTO.class)).thenReturn(expectedDto);
        when(tagsRepo.findTagsByType(FACT_OF_THE_DAY)).thenReturn(tags);
        when(factOfTheDayService.getRandomFactOfTheDayByTags(tagIds))
            .thenReturn(expectedDto);

        FactOfTheDayTranslationDTO result = factOfTheDayService.getRandomGeneralFactOfTheDay();

        assertEquals(expectedDto, result);
    }

    @Test
    void getRandomGeneralFactOfTheDay_noTagsFound() {
        when(tagsRepo.findTagsByType(FACT_OF_THE_DAY)).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class, () -> {
            factOfTheDayService.getRandomGeneralFactOfTheDay();
        });
    }

    @Test
    void getRandomFactOfTheDayForUser_success() {
        String userEmail = "user@example.com";
        Set<Long> tagIds = Set.of(1L);
        FactOfTheDay factOfTheDay = ModelUtils.getFactOfTheDay();
        FactOfTheDayTranslationDTO expectedDto = ModelUtils.getFactOfTheDayTranslationDTO();

        when(factOfTheDayRepo.getRandomFactOfTheDay(tagIds)).thenReturn(Optional.of(factOfTheDay));
        when(modelMapper.map(factOfTheDay, FactOfTheDayTranslationDTO.class)).thenReturn(expectedDto);
        when(tagsRepo.findTagsIdByUserHabitsInProgress(userEmail)).thenReturn(tagIds);
        when(factOfTheDayService.getRandomFactOfTheDayByTags(tagIds))
            .thenReturn(expectedDto);

        FactOfTheDayTranslationDTO result =
            factOfTheDayService.getRandomFactOfTheDayForUser(userEmail);

        assertEquals(expectedDto, result);
    }

    @Test
    void getRandomFactOfTheDayForUser_noUserTags() {
        String userEmail = "user@example.com";

        when(tagsRepo.findTagsIdByUserHabitsInProgress(userEmail)).thenReturn(Collections.emptySet());

        FactOfTheDayTranslationDTO result = factOfTheDayService.getRandomFactOfTheDayForUser(userEmail);

        assertNull(result);
    }

    @Test
    void getAllFactOfTheDayTags_success() {
        TagDto tagDto = ModelUtils.getTagDto();
        Set<TagDto> expectedTags = Set.of(tagDto);

        when(factOfTheDayRepo.findAllFactOfTheDayAndHabitTags()).thenReturn(expectedTags);

        Set<TagDto> result = factOfTheDayService.getAllFactOfTheDayTags();

        assertEquals(expectedTags, result);
    }

    @Test
    void getAllFactOfTheDayTags_noTagsFound() {
        when(factOfTheDayRepo.findAllFactOfTheDayAndHabitTags()).thenReturn(Collections.emptySet());

        Set<TagDto> result = factOfTheDayService.getAllFactOfTheDayTags();

        assertTrue(result.isEmpty());
    }
}