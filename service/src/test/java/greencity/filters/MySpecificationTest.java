package greencity.filters;

import greencity.ModelUtils;
import greencity.dto.econews.EcoNewsViewDto;
import greencity.dto.ratingstatistics.RatingStatisticsViewDto;
import greencity.dto.tag.TagViewDto;
import greencity.entity.EcoNews;
import greencity.entity.RatingStatistics_;
import greencity.entity.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.criteria.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySpecificationTest {
    @Mock
    private Root<Tag> root;
    @Mock
    private Root<EcoNews> newsRoot;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private Predicate expected;
    @Mock
    private Path<Object> objectPath;
    @Mock
    private Expression<String> as;
    @Mock
    private SearchCriteria searchCriteriaEmpty;
    TagSpecification tagSpecification;
    List<SearchCriteria> searchCriteriaList;
    TagViewDto tagViewDto;
    EcoNewsViewDto ecoNewsViewDto;
    EcoNewsSpecification ecoNewsSpecification;
    SearchCriteria searchCriteriaForAll;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        ecoNewsViewDto = ModelUtils.getEcoNewsViewDto();
        tagViewDto = ModelUtils.getTagViewDto();
        searchCriteriaList = new ArrayList<>();
        searchCriteriaList.add(searchCriteriaForAll = SearchCriteria.builder()
            .key("id")
            .type("id")
            .value(tagViewDto.getId())
            .build());
        tagSpecification = new TagSpecification(searchCriteriaList);
        ecoNewsSpecification = new EcoNewsSpecification(searchCriteriaList);
    }

    @Test
    void getDataRangePredicate() {
        RatingStatisticsViewDto ratingStatisticsViewDto =
            new RatingStatisticsViewDto("2", "COMMENT_OR_REPLY", "1", "", "2021-01-12", "2021-01-13", "", "50");
        SearchCriteria onlyThisMethod = SearchCriteria.builder()
            .key(RatingStatistics_.CREATE_DATE)
            .type("dateRange")
            .value(new String[] {ratingStatisticsViewDto.getStartDate(), ratingStatisticsViewDto.getEndDate()})
            .build();
        when(newsRoot.get(onlyThisMethod.getKey())).thenReturn(objectPath);
        when(criteriaBuilder.between(any(), any(ZonedDateTime.class), any(ZonedDateTime.class)))
            .thenThrow(DateTimeParseException.class).thenReturn(expected);
        when(criteriaBuilder.disjunction()).thenReturn(expected);
        Predicate actual = ecoNewsSpecification.getDataRangePredicate(newsRoot, criteriaBuilder, onlyThisMethod);
        assertEquals(expected, actual);
    }

    @Test
    void getNumericPredicate() {
        when(root.get(searchCriteriaForAll.getKey())).thenReturn(objectPath);
        when(criteriaBuilder.equal(objectPath, searchCriteriaForAll.getValue())).thenThrow(NumberFormatException.class)
            .thenReturn(expected);
        when(criteriaBuilder.disjunction()).thenReturn(expected);
        Predicate actual = tagSpecification.getNumericPredicate(root, criteriaBuilder, searchCriteriaForAll);
        assertEquals(expected, actual);
    }

    @Test
    void getStringPredicate() {
        when(newsRoot.get(searchCriteriaForAll.getKey())).thenReturn(objectPath);
        when(criteriaBuilder.like(any(), eq("%" + searchCriteriaForAll.getValue() + "%"))).thenReturn(expected);
        Predicate actual = ecoNewsSpecification.getStringPredicate(newsRoot, criteriaBuilder, searchCriteriaForAll);
        assertEquals(expected, actual);
    }

    @Test
    void getEnumPredicate() {
        when(root.get(searchCriteriaForAll.getKey())).thenReturn(objectPath);
        when(objectPath.as(String.class)).thenReturn(this.as);
        when(criteriaBuilder.like(as, "%" + searchCriteriaForAll.getValue() + "%")).thenReturn(expected);
        Predicate actual = tagSpecification.getEnumPredicate(root, criteriaBuilder, searchCriteriaForAll);
        assertEquals(expected, actual);
    }

    @Test
    void getAuthorPredicate() {
        when(newsRoot.get(searchCriteriaForAll.getKey())).thenReturn(objectPath);
        when(criteriaBuilder.like(any(), eq("%" + searchCriteriaForAll.getValue() + "%"))).thenReturn(expected);
        Predicate actual = ecoNewsSpecification.getAuthorPredicate(newsRoot, criteriaBuilder, searchCriteriaForAll);
        assertEquals(expected, actual);
    }

    @Test
    void getBooleanPredicate() {
        when(newsRoot.get(searchCriteriaForAll.getKey())).thenReturn(objectPath);
        String searchCriteriaValue = (String) searchCriteriaForAll.getValue();
        when(criteriaBuilder.equal(any(), eq(Boolean.parseBoolean(searchCriteriaValue)))).thenReturn(expected);
        Predicate actual = ecoNewsSpecification.getBooleanPredicate(newsRoot, criteriaBuilder, searchCriteriaForAll);
        assertEquals(expected, actual);
    }

    @Test
    void getBooleanPredicateEmptyValue() {
        when(searchCriteriaEmpty.getValue()).thenReturn("");
        when(criteriaBuilder.conjunction()).thenReturn(expected);
        Predicate actual = ecoNewsSpecification.getBooleanPredicate(newsRoot, criteriaBuilder, searchCriteriaEmpty);
        assertEquals(expected, actual);
    }
}