package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.ratingstatistics.RatingStatisticsDto;
import greencity.dto.ratingstatistics.RatingStatisticsDtoForTables;
import greencity.dto.ratingstatistics.RatingStatisticsVO;
import greencity.dto.ratingstatistics.RatingStatisticsViewDto;
import greencity.dto.ratingstatistics.RatingPointsDto;
import greencity.dto.user.UserVO;
import greencity.entity.RatingPoints;
import greencity.entity.RatingStatistics;
import greencity.filters.RatingStatisticsSpecification;
import greencity.filters.SearchCriteria;
import greencity.repository.RatingStatisticsRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RatingStatisticsServiceImplTest {
    @Mock
    private RatingStatisticsRepo ratingStatisticsRepo;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private RatingStatisticsServiceImpl ratingStatisticsService;

    private final ZonedDateTime defaultTime = ZonedDateTime.now();

    private final Pageable pageable = PageRequest.of(3, 5);
    private final RatingPoints ratingPoints = RatingPoints.builder().id(1L).name("CREATE_NEWS").points(1).build();
    private final RatingPointsDto ratingPointsDto =
        RatingPointsDto.builder().id(1L).name("CREATE_NEWS").points(1).build();
    private final RatingStatisticsVO ratingStatisticsVO = RatingStatisticsVO
        .builder().id(1L).createDate(defaultTime).pointsChanged(1.0).rating(5.0)
        .ratingPoints(ratingPointsDto).build();

    private final RatingStatistics ratingStatistics = RatingStatistics
        .builder().id(1L).createDate(defaultTime).pointsChanged(1.0).rating(5.0)
        .ratingPoints(ratingPoints).build();

    private final RatingStatisticsDto ratingStatisticsDto = new RatingStatisticsDto(1L, defaultTime,
        ratingPointsDto,
        1.0f, 5.0f, UserVO.builder().build());

    private final Page<RatingStatistics> ratingStatisticsPage = Page.empty(pageable);

    private final List<RatingStatistics> ratingStatisticsList = Collections.singletonList(ratingStatistics);

    private final List<RatingStatisticsDto> ratingStatisticsDtoList = Collections.singletonList(ratingStatisticsDto);

    private SearchCriteria generateSearchCriteria(String key, String type) {
        return SearchCriteria.builder()
            .key(key)
            .type(type)
            .build();
    }

    @Test
    void save() {
        when(modelMapper.map(ratingStatisticsVO, RatingStatistics.class)).thenReturn(ratingStatistics);
        when(ratingStatisticsRepo.save(ratingStatistics)).thenReturn(ratingStatistics);
        when(modelMapper.map(ratingStatistics, RatingStatisticsVO.class)).thenReturn(ratingStatisticsVO);
        RatingStatisticsVO expected = ratingStatisticsService.save(ratingStatisticsVO);

        verify(ratingStatisticsRepo, times(1)).save(ratingStatistics);
        assertEquals(expected, ratingStatisticsVO);
    }

    @Test
    void getRatingStatisticsForManagementByPage() {
        when(ratingStatisticsRepo.findAll(pageable)).thenReturn(ratingStatisticsPage);
        PageableAdvancedDto<RatingStatisticsDtoForTables> expected = ratingStatisticsService
            .getRatingStatisticsForManagementByPage(pageable);
        PageableAdvancedDto<RatingStatisticsDtoForTables> actual = new PageableAdvancedDto<>(
            Collections.emptyList(), ratingStatisticsPage.getTotalElements(), 3,
            ratingStatisticsPage.getTotalPages(),
            ratingStatisticsPage.getNumber(), ratingStatisticsPage.hasPrevious(),
            ratingStatisticsPage.hasNext(),
            ratingStatisticsPage.isFirst(), ratingStatisticsPage.isLast());

        assertEquals(expected, actual);
    }

    @Test
    void getAllRatingStatistics() {
        when(ratingStatisticsRepo.findAll()).thenReturn(ratingStatisticsList);
        when(modelMapper.map(ratingStatistics, RatingStatisticsDto.class)).thenReturn(ratingStatisticsDto);

        List<RatingStatisticsDto> expected = ratingStatisticsService.getAllRatingStatistics();

        assertEquals(expected, ratingStatisticsDtoList);
    }

    @Test
    void getFilteredRatingStatisticsForExcel() {
        RatingStatisticsViewDto ratingStatisticsViewDto = RatingStatisticsViewDto.builder()
            .id("").eventName("").userId("").userEmail("").startDate("").endDate("")
            .pointsChanged("1").currentRating("").build();

        when(ratingStatisticsRepo.findAll(any(RatingStatisticsSpecification.class))).thenReturn(ratingStatisticsList);
        when(modelMapper.map(ratingStatistics, RatingStatisticsDto.class)).thenReturn(ratingStatisticsDto);

        List<RatingStatisticsDto> expected = ratingStatisticsService
            .getFilteredRatingStatisticsForExcel(ratingStatisticsViewDto);

        assertEquals(expected, ratingStatisticsDtoList);
    }

    @Test
    void getFilteredDataForManagementByPage() {
        RatingStatisticsViewDto ratingStatisticsViewDto = RatingStatisticsViewDto.builder()
            .id("").eventName("").userId("").userEmail("").startDate("").endDate("")
            .pointsChanged("").currentRating("").build();

        when(ratingStatisticsRepo.findAll(any(RatingStatisticsSpecification.class), eq(pageable)))
            .thenReturn(ratingStatisticsPage);

        PageableAdvancedDto<RatingStatisticsDtoForTables> expected = ratingStatisticsService
            .getFilteredDataForManagementByPage(pageable, ratingStatisticsViewDto);
        PageableAdvancedDto<RatingStatisticsDtoForTables> actual = new PageableAdvancedDto<>(
            Collections.emptyList(), ratingStatisticsPage.getTotalElements(), 3,
            ratingStatisticsPage.getTotalPages(),
            ratingStatisticsPage.getNumber(), ratingStatisticsPage.hasPrevious(), ratingStatisticsPage.hasNext(),
            ratingStatisticsPage.isFirst(), ratingStatisticsPage.isLast());

        assertEquals(expected, actual);
    }

    @Test
    void buildSearchCriteriaOnlyWithId() {
        RatingStatisticsViewDto ratingStatisticsViewDto = RatingStatisticsViewDto.builder()
            .id("1L").eventName("").userId("").userEmail("").startDate("").endDate("")
            .pointsChanged("").currentRating("").build();
        SearchCriteria searchCriteriaId = generateSearchCriteria("id", "id");
        searchCriteriaId.setValue(ratingStatisticsViewDto.getId());
        List<SearchCriteria> actual = List.of(searchCriteriaId);
        List<SearchCriteria> expected = ratingStatisticsService.buildSearchCriteria(ratingStatisticsViewDto);

        assertEquals(expected, actual);
    }

    @Test
    void buildSearchCriteriaWithIdAndEventNameAndUserIdAndUserEmail() {
        RatingStatisticsViewDto ratingStatisticsViewDto = RatingStatisticsViewDto.builder()
            .id("1L").eventName("Event Name").userId("2L").userEmail("email@gmail.com").startDate("").endDate("")
            .pointsChanged("").currentRating("").build();

        SearchCriteria searchCriteriaId = generateSearchCriteria("id", "id");
        searchCriteriaId.setValue(ratingStatisticsViewDto.getId());
        SearchCriteria searchCriteriaEventName = generateSearchCriteria("ratingPoints", "ratingPoints");
        searchCriteriaEventName.setValue(ratingStatisticsViewDto.getEventName());
        SearchCriteria searchCriteriaUserId = generateSearchCriteria("user", "userId");
        searchCriteriaUserId.setValue(ratingStatisticsViewDto.getUserId());
        SearchCriteria searchCriteriaUserEmail = generateSearchCriteria("user", "userMail");
        searchCriteriaUserEmail.setValue(ratingStatisticsViewDto.getUserEmail());

        List<SearchCriteria> actual = List.of(searchCriteriaId, searchCriteriaEventName,
            searchCriteriaUserId, searchCriteriaUserEmail);
        List<SearchCriteria> expected = ratingStatisticsService.buildSearchCriteria(ratingStatisticsViewDto);

        assertEquals(expected, actual);
    }

    @Test
    void buildSearchCriteriaWithStartDateAndEndDateAndPointsChangedAndCurrentRating() {
        RatingStatisticsViewDto ratingStatisticsViewDto = RatingStatisticsViewDto.builder()
            .id("").eventName("").userId("").userEmail("").startDate("2020-10-17").endDate("2020-10-20")
            .pointsChanged("10").currentRating("8").build();

        SearchCriteria searchCriteriaPointsChanged = generateSearchCriteria("pointsChanged", "pointsChanged");
        searchCriteriaPointsChanged.setValue(ratingStatisticsViewDto.getPointsChanged());
        SearchCriteria searchCriteriaCurrentRating = generateSearchCriteria("rating", "currentRating");
        searchCriteriaCurrentRating.setValue(ratingStatisticsViewDto.getCurrentRating());
        SearchCriteria searchCriteriaDateRange = generateSearchCriteria("createDate", "dateRange");
        searchCriteriaDateRange.setValue(new String[] {ratingStatisticsViewDto.getStartDate(),
            ratingStatisticsViewDto.getEndDate()});

        List<SearchCriteria> actual = List.of(searchCriteriaDateRange, searchCriteriaPointsChanged,
            searchCriteriaCurrentRating);
        List<SearchCriteria> expected = ratingStatisticsService.buildSearchCriteria(ratingStatisticsViewDto);

        assertEquals(expected.size(), actual.size());
        assertEquals(expected.getLast(), actual.getLast());
        assertEquals(expected.get(1), actual.get(1));
    }

    @Test
    void buildSearchCriteriaReturnEmptyList() {
        RatingStatisticsViewDto ratingStatisticsViewDto = RatingStatisticsViewDto.builder()
            .id("").eventName("").userId("").userEmail("").startDate("").endDate("")
            .pointsChanged("").currentRating("").build();

        List<SearchCriteria> expected = ratingStatisticsService.buildSearchCriteria(ratingStatisticsViewDto);

        assertEquals(0, expected.size());
    }
}
