package greencity.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import greencity.dto.econews.EcoNewsViewDto;
import greencity.entity.*;
import greencity.entity.localization.TagTranslation;
import greencity.entity.localization.TagTranslation_;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.ListJoin;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EcoNewsSpecificationTest {

    @Mock
    private CriteriaBuilder criteriaBuilderMock;

    @Mock
    private CriteriaQuery criteriaQueryMock;

    @Mock
    private Root<EcoNews> ecoNewsRootMock;

    @Mock
    private Predicate predicateMock;

    @Mock
    private Path<Object> pathEcoNewsTitleMock;

    @Mock
    private Path<Object> pathEcoNewsTextMock;

    @Mock
    private Path<Object> pathEcoNewsAuthorMock;

    @Mock
    private Path<Object> pathEcoNewsAuthorNameMock;

    @Mock
    private Path<Object> pathEcoNewsCreationDateMock;

    @Mock
    private Path<Object> pathEcoNewsHiddenMock;

    @Mock
    private Path<Object> pathEcoNewsLikesMock;

    @Mock
    private Path<Object> pathEcoNewsDislikesMock;

    @Mock
    private Path<Object> pathEcoNewsIdMock;

    @Mock
    private Path<Object> pathEcoNewsDateRangeMock;

    @Mock
    private Predicate andTitlePredicate;

    @Mock
    private Predicate andTextPredicate;

    @Mock
    private Predicate andAuthorPredicate;

    @Mock
    private Predicate andTagPredicate;

    @Mock
    private Predicate andCreationDatePredicate;

    @Mock
    private Predicate andBooleanPredicate;

    @Mock
    private Predicate emptyPredicate;

    @Mock
    private Predicate andIdPredicate;

    @Mock
    private Predicate andDateRangePredicate;

    @Mock
    private ListAttribute<EcoNews, Tag> tags;

    @Mock
    private ListJoin<EcoNews, Tag> tagJoinMock;

    @Mock
    private ListAttribute<Tag, TagTranslation> tagTranslations;

    @Mock
    private ListJoin<Tag, TagTranslation> tagTranslationJoinMock;

    @Mock
    private SingularAttribute<TagTranslation, String> name;

    @Mock
    private Path<String> pathTagTranslationNameMock;

    @Mock
    private Expression<Integer> likesExpressionMock;

    @Mock
    private Expression<Integer> dislikesExpressionMock;

    @Mock
    private Expression<?> titleExpressionMock;

    private EcoNewsSpecification ecoNewsSpecification;

    private List<SearchCriteria> criteriaList;

    private Order likesOrderAsc;

    private Order titleOrderAsc;

    private Order dislikesOrderAsc;

    private List<Order> orderListAsc;

    private Order likesOrderDesc;

    private Order titleOrderDesc;

    private Order dislikesOrderDesc;

    private List<Order> orderListDesc;

    @BeforeEach
    void setUp() {
        EcoNewsViewDto ecoNewsViewDto =
            new EcoNewsViewDto("", "anyTitle", "anyAuthor", "weather", "2024-08-20", "", "News", "");

        criteriaList = new ArrayList<>();
        criteriaList.add(
            SearchCriteria.builder()
                .key(EcoNews_.TITLE)
                .type(EcoNews_.TITLE)
                .value(ecoNewsViewDto.getTitle())
                .build());
        criteriaList.add(
            SearchCriteria.builder()
                .key(EcoNews_.AUTHOR)
                .type(EcoNews_.AUTHOR)
                .value(ecoNewsViewDto.getAuthor())
                .build());
        criteriaList.add(
            SearchCriteria.builder()
                .key(EcoNews_.TEXT)
                .type(EcoNews_.TEXT)
                .value(ecoNewsViewDto.getText())
                .build());
        criteriaList.add(
            SearchCriteria.builder()
                .key(EcoNews_.CREATION_DATE)
                .type(EcoNews_.CREATION_DATE)
                .value(ecoNewsViewDto.getStartDate())
                .build());
        criteriaList.add(
            SearchCriteria.builder()
                .key(EcoNews_.TAGS)
                .type(EcoNews_.TAGS)
                .value(ecoNewsViewDto.getTags())
                .build());
        criteriaList.add(
            SearchCriteria.builder()
                .key(EcoNews_.HIDDEN)
                .type(EcoNews_.HIDDEN)
                .value("true")
                .build());

        EcoNews_.tags = tags;
        Tag_.tagTranslations = tagTranslations;
        TagTranslation_.name = name;

        ecoNewsSpecification = new EcoNewsSpecification(criteriaList);

        likesOrderAsc = new Order() {
            @Override
            public Order reverse() {
                return null;
            }

            @Override
            public boolean isAscending() {
                return true;
            }

            @Override
            public Expression<?> getExpression() {
                return likesExpressionMock;
            }
        };
        titleOrderAsc = new Order() {
            @Override
            public Order reverse() {
                return null;
            }

            @Override
            public boolean isAscending() {
                return true;
            }

            @Override
            public Expression<?> getExpression() {
                return titleExpressionMock;
            }
        };
        orderListAsc = new ArrayList<>();
        orderListAsc.add(likesOrderAsc);
        orderListAsc.add(titleOrderAsc);

        likesOrderDesc = new Order() {
            @Override
            public Order reverse() {
                return null;
            }

            @Override
            public boolean isAscending() {
                return false;
            }

            @Override
            public Expression<?> getExpression() {
                return likesExpressionMock;
            }
        };
        titleOrderDesc = new Order() {
            @Override
            public Order reverse() {
                return null;
            }

            @Override
            public boolean isAscending() {
                return false;
            }

            @Override
            public Expression<?> getExpression() {
                return titleExpressionMock;
            }
        };
        orderListDesc = new ArrayList<>();
        orderListDesc.add(likesOrderDesc);
        orderListDesc.add(titleOrderDesc);

        dislikesOrderAsc = new Order() {
            @Override
            public Order reverse() {
                return null;
            }

            @Override
            public boolean isAscending() {
                return true;
            }

            @Override
            public Expression<?> getExpression() {
                return dislikesExpressionMock;
            }
        };
        dislikesOrderDesc = new Order() {
            @Override
            public Order reverse() {
                return null;
            }

            @Override
            public boolean isAscending() {
                return false;
            }

            @Override
            public Expression<?> getExpression() {
                return dislikesExpressionMock;
            }
        };
    }

    @Test
    void toPredicate() {

        when(criteriaBuilderMock.conjunction()).thenReturn(predicateMock);

        when(ecoNewsRootMock.get(EcoNews_.TITLE)).thenReturn(pathEcoNewsTitleMock);

        when(criteriaBuilderMock.like(ecoNewsRootMock.get(EcoNews_.TITLE),
            "%" + criteriaList.getFirst().getValue() + "%"))
                .thenReturn(andTitlePredicate);

        when(criteriaBuilderMock.and(predicateMock, andTitlePredicate)).thenReturn(andTitlePredicate);

        when(ecoNewsRootMock.get(EcoNews_.AUTHOR)).thenReturn(pathEcoNewsAuthorMock);

        when(pathEcoNewsAuthorMock.get("name")).thenReturn(pathEcoNewsAuthorNameMock);

        when(criteriaBuilderMock
            .like(ecoNewsRootMock.get(EcoNews_.AUTHOR).get("name"), "%" + criteriaList.get(1).getValue() + "%"))
                .thenReturn(andAuthorPredicate);

        when(criteriaBuilderMock.and(andTitlePredicate, andAuthorPredicate)).thenReturn(andAuthorPredicate);

        when(ecoNewsRootMock.get(EcoNews_.TEXT)).thenReturn(pathEcoNewsTextMock);

        when(criteriaBuilderMock.like(ecoNewsRootMock.get(EcoNews_.TEXT), "%" + criteriaList.get(2).getValue() + "%"))
            .thenReturn(andTextPredicate);

        when(criteriaBuilderMock.and(andAuthorPredicate, andTextPredicate)).thenReturn(andTextPredicate);

        when(ecoNewsRootMock.get(EcoNews_.CREATION_DATE)).thenReturn(pathEcoNewsCreationDateMock);

        when(criteriaBuilderMock.between(any(), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(andCreationDatePredicate);

        when(criteriaBuilderMock.and(andTextPredicate, andCreationDatePredicate)).thenReturn(andCreationDatePredicate);

        when(ecoNewsRootMock.join(EcoNews_.tags)).thenReturn(tagJoinMock);

        when(tagJoinMock.join(Tag_.tagTranslations)).thenReturn(tagTranslationJoinMock);

        when(tagTranslationJoinMock.get(TagTranslation_.name)).thenReturn(pathTagTranslationNameMock);

        when(criteriaBuilderMock
            .like(pathTagTranslationNameMock.as(String.class), "%" + criteriaList.get(4).getValue() + "%"))
                .thenReturn(andTagPredicate);

        when(criteriaBuilderMock.and(List.of(andTagPredicate).toArray(new Predicate[0]))).thenReturn(andTagPredicate);

        when(criteriaBuilderMock.and(andCreationDatePredicate, andTagPredicate)).thenReturn(andTagPredicate);

        when(ecoNewsRootMock.get(EcoNews_.HIDDEN)).thenReturn(pathEcoNewsHiddenMock);

        when(criteriaBuilderMock.equal(any(), eq(true))).thenReturn(andBooleanPredicate);

        when(criteriaBuilderMock.and(andTagPredicate, andBooleanPredicate)).thenReturn(andBooleanPredicate);

        when(criteriaQueryMock.getOrderList()).thenReturn(List.of(dislikesOrderAsc));
        when(dislikesExpressionMock.toString()).thenReturn("dislikes");
        when(ecoNewsRootMock.get("usersDislikedNews")).thenReturn(pathEcoNewsDislikesMock);
        when(criteriaBuilderMock.size(any(Expression.class))).thenReturn(dislikesExpressionMock);
        when(criteriaBuilderMock.asc(dislikesExpressionMock)).thenReturn(dislikesOrderAsc);
        when(criteriaQueryMock.orderBy(List.of(dislikesOrderAsc))).thenReturn(criteriaQueryMock);

        ecoNewsSpecification.toPredicate(ecoNewsRootMock, criteriaQueryMock, criteriaBuilderMock);

        verify(ecoNewsRootMock, never()).get(EcoNews_.ID);
        verify(ecoNewsRootMock, never()).get(EcoNews_.IMAGE_PATH);
        verify(ecoNewsRootMock, never()).get(EcoNews_.SOURCE);
        verify(ecoNewsRootMock, never()).get("dateRange");
        verify(criteriaBuilderMock).and(predicateMock, andTitlePredicate);
        verify(criteriaBuilderMock).and(andTitlePredicate, andAuthorPredicate);
        verify(criteriaBuilderMock).and(andAuthorPredicate, andTextPredicate);
        verify(criteriaBuilderMock).and(andTextPredicate, andCreationDatePredicate);
        verify(criteriaBuilderMock).and(andCreationDatePredicate, andTagPredicate);
        verify(criteriaBuilderMock).and(andTagPredicate, andBooleanPredicate);
        verify(criteriaQueryMock).orderBy(List.of(dislikesOrderAsc));
    }

    @Test
    void toPredicateTagsEmpty() {
        List<SearchCriteria> emptyTagsList = List.of(SearchCriteria.builder()
            .key(EcoNews_.TAGS)
            .type(EcoNews_.TAGS)
            .value("")
            .build());
        EcoNewsSpecification specificationForEmpty = new EcoNewsSpecification(emptyTagsList);
        when(criteriaBuilderMock.conjunction()).thenReturn(emptyPredicate);
        when(criteriaBuilderMock.and(emptyPredicate, emptyPredicate)).thenReturn(emptyPredicate);
        Predicate actual = specificationForEmpty.toPredicate(ecoNewsRootMock, criteriaQueryMock, criteriaBuilderMock);
        assertEquals(emptyPredicate, actual);
        verify(criteriaBuilderMock).and(emptyPredicate, emptyPredicate);
    }

    @Test
    void toPredicateCreationDateEmpty() {
        List<SearchCriteria> emptyCreationDateList = List.of(SearchCriteria.builder()
            .key(EcoNews_.CREATION_DATE)
            .type(EcoNews_.CREATION_DATE)
            .value("")
            .build());
        EcoNewsSpecification specificationForEmpty = new EcoNewsSpecification(emptyCreationDateList);
        when(criteriaBuilderMock.conjunction()).thenReturn(emptyPredicate);
        when(criteriaBuilderMock.and(emptyPredicate, emptyPredicate)).thenReturn(emptyPredicate);
        Predicate actual = specificationForEmpty.toPredicate(ecoNewsRootMock, criteriaQueryMock, criteriaBuilderMock);
        assertEquals(emptyPredicate, actual);
        verify(criteriaBuilderMock).and(emptyPredicate, emptyPredicate);
    }

    @Test
    void toPredicateInvalidCreationDate() {
        List<SearchCriteria> invalidCreationDateList = List.of(SearchCriteria.builder()
            .key(EcoNews_.CREATION_DATE)
            .type(EcoNews_.CREATION_DATE)
            .value("invalidDate")
            .build());
        EcoNewsSpecification specificationForInvalidDate = new EcoNewsSpecification(invalidCreationDateList);
        when(criteriaBuilderMock.conjunction()).thenReturn(emptyPredicate);
        when(criteriaBuilderMock.disjunction()).thenReturn(emptyPredicate);
        when(criteriaBuilderMock.and(emptyPredicate, emptyPredicate)).thenReturn(emptyPredicate);
        Predicate actual =
            specificationForInvalidDate.toPredicate(ecoNewsRootMock, criteriaQueryMock, criteriaBuilderMock);
        assertEquals(emptyPredicate, actual);
        verify(criteriaBuilderMock).and(emptyPredicate, emptyPredicate);
    }

    @Test
    void toPredicateSortOrderAsc() {
        EcoNewsSpecification specificationForSortList = new EcoNewsSpecification(new ArrayList<>());
        when(criteriaBuilderMock.conjunction()).thenReturn(predicateMock);
        when(criteriaQueryMock.getOrderList()).thenReturn(orderListAsc);
        when(likesExpressionMock.toString()).thenReturn("likes");
        when(ecoNewsRootMock.get("usersLikedNews")).thenReturn(pathEcoNewsLikesMock);
        when(criteriaBuilderMock.size(any(Expression.class))).thenReturn(likesExpressionMock);
        when(criteriaBuilderMock.asc(likesExpressionMock)).thenReturn(likesOrderAsc);
        when(titleExpressionMock.toString()).thenReturn("title");
        when(ecoNewsRootMock.get(EcoNews_.TITLE)).thenReturn(pathEcoNewsTitleMock);
        when(criteriaBuilderMock.asc(pathEcoNewsTitleMock)).thenReturn(titleOrderAsc);
        when(criteriaQueryMock.orderBy(orderListAsc)).thenReturn(criteriaQueryMock);
        Predicate actual =
            specificationForSortList.toPredicate(ecoNewsRootMock, criteriaQueryMock, criteriaBuilderMock);
        assertEquals(predicateMock, actual);
        verify(criteriaBuilderMock).conjunction();
        verify(criteriaQueryMock).orderBy(orderListAsc);
    }

    @Test
    void toPredicateSortOrderDesc() {
        EcoNewsSpecification specificationForSortList = new EcoNewsSpecification(new ArrayList<>());
        when(criteriaBuilderMock.conjunction()).thenReturn(predicateMock);
        when(criteriaQueryMock.getOrderList()).thenReturn(orderListDesc);
        when(likesExpressionMock.toString()).thenReturn("likes");
        when(ecoNewsRootMock.get("usersLikedNews")).thenReturn(pathEcoNewsLikesMock);
        when(criteriaBuilderMock.size(any(Expression.class))).thenReturn(likesExpressionMock);
        when(criteriaBuilderMock.desc(likesExpressionMock)).thenReturn(likesOrderDesc);
        when(titleExpressionMock.toString()).thenReturn("title");
        when(ecoNewsRootMock.get(EcoNews_.TITLE)).thenReturn(pathEcoNewsTitleMock);
        when(criteriaBuilderMock.desc(pathEcoNewsTitleMock)).thenReturn(titleOrderDesc);
        when(criteriaQueryMock.orderBy(orderListDesc)).thenReturn(criteriaQueryMock);
        Predicate actual =
            specificationForSortList.toPredicate(ecoNewsRootMock, criteriaQueryMock, criteriaBuilderMock);
        assertEquals(predicateMock, actual);
        verify(criteriaBuilderMock).conjunction();
        verify(criteriaQueryMock).orderBy(orderListDesc);
    }

    @Test
    void toPredicateIdAndDateRangeSortOrderDislikesDesc() {
        List<SearchCriteria> searchCriteriaList = new ArrayList<>();
        searchCriteriaList.add(SearchCriteria.builder()
            .key(EcoNews_.ID)
            .type(EcoNews_.ID)
            .value("1")
            .build());
        searchCriteriaList.add(SearchCriteria.builder()
            .key("dateRange")
            .type("dateRange")
            .value(new String[] {"2023-09-01", "2023-09-12"})
            .build());
        EcoNewsSpecification specificationForSortList = new EcoNewsSpecification(searchCriteriaList);
        when(criteriaBuilderMock.conjunction()).thenReturn(predicateMock);
        when(ecoNewsRootMock.get(EcoNews_.ID)).thenReturn(pathEcoNewsIdMock);
        when(criteriaBuilderMock.equal(any(), anyString())).thenReturn(andIdPredicate);
        when(criteriaBuilderMock.and(predicateMock, andIdPredicate)).thenReturn(andIdPredicate);
        when(ecoNewsRootMock.get("dateRange")).thenReturn(pathEcoNewsDateRangeMock);
        when(criteriaBuilderMock.between(any(), any(ZonedDateTime.class), any(ZonedDateTime.class)))
            .thenReturn(andDateRangePredicate);
        when(criteriaBuilderMock.and(andIdPredicate, andDateRangePredicate)).thenReturn(andDateRangePredicate);

        when(criteriaQueryMock.getOrderList()).thenReturn(List.of(dislikesOrderDesc));
        when(dislikesExpressionMock.toString()).thenReturn("dislikes");
        when(ecoNewsRootMock.get("usersDislikedNews")).thenReturn(pathEcoNewsDislikesMock);
        when(criteriaBuilderMock.size(any(Expression.class))).thenReturn(dislikesExpressionMock);
        when(criteriaBuilderMock.desc(dislikesExpressionMock)).thenReturn(dislikesOrderDesc);
        when(criteriaQueryMock.orderBy(List.of(dislikesOrderDesc))).thenReturn(criteriaQueryMock);
        Predicate actual =
            specificationForSortList.toPredicate(ecoNewsRootMock, criteriaQueryMock, criteriaBuilderMock);
        assertEquals(andDateRangePredicate, actual);
        verify(criteriaBuilderMock).conjunction();
        verify(criteriaQueryMock).orderBy(List.of(dislikesOrderDesc));
    }

    @Test
    void testPredicateCreatorForUnknownType() {
        SearchCriteria criteria = new SearchCriteria("unknownType", "=", "value");
        List<SearchCriteria> searchCriteriaList = List.of(criteria);
        EcoNewsSpecification specification = new EcoNewsSpecification(searchCriteriaList);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<EcoNews> root = mock(Root.class);
        CriteriaQuery<?> cq = mock(CriteriaQuery.class);
        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction()).thenReturn(conjunction);

        Predicate predicate = specification.toPredicate(root, cq, cb);

        assertNotNull(predicate);
        assertEquals(conjunction, predicate);
        verify(cb).conjunction();
    }

    @Test
    void testEmptySearchCriteriaList() {
        List<SearchCriteria> searchCriteriaList = Collections.emptyList();
        EcoNewsSpecification specification = new EcoNewsSpecification(searchCriteriaList);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<EcoNews> root = mock(Root.class);
        CriteriaQuery<?> cq = mock(CriteriaQuery.class);
        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction()).thenReturn(conjunction);

        Predicate predicate = specification.toPredicate(root, cq, cb);

        assertNotNull(predicate);
        assertEquals(conjunction, predicate);
        verify(cb).conjunction();
    }
}