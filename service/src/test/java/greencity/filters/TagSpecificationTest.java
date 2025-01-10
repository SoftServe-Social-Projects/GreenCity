package greencity.filters;

import greencity.ModelUtils;
import greencity.dto.tag.TagViewDto;
import greencity.entity.Tag;
import greencity.entity.Tag_;
import greencity.entity.localization.TagTranslation;
import greencity.entity.localization.TagTranslation_;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagSpecificationTest {
    @Mock
    private ListJoin<Tag, TagTranslation> tagTagTranslationJoin;
    @Mock
    private Root<Tag> root;
    @Mock
    private CriteriaQuery<Tag> criteriaQuery;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private Predicate expected;
    @Mock
    private Path<Object> objectPath;
    @Mock
    private Path<Tag> tagPath;
    @Mock
    private Expression<String> as;
    TagSpecification tagSpecification;
    List<SearchCriteria> searchCriteriaList;
    TagViewDto tagViewDto;
    @Mock
    ListAttribute<Tag, TagTranslation> tagTranslationsList;
    @Mock
    SingularAttribute<TagTranslation, Tag> tag;
    @Mock
    SingularAttribute<TagTranslation, String> name;
    @Mock
    SingularAttribute<Tag, Long> id;

    @BeforeEach
    void init() {
        Tag_.tagTranslations = tagTranslationsList;
        TagTranslation_.tag = tag;
        TagTranslation_.name = name;
        Tag_.id = id;
        MockitoAnnotations.openMocks(this);
        tagViewDto = ModelUtils.getTagViewDto();
        searchCriteriaList = new ArrayList<>();
        searchCriteriaList.add(SearchCriteria.builder()
            .key("id")
            .type("id")
            .value(tagViewDto.getId())
            .build());
        searchCriteriaList.add(SearchCriteria.builder()
            .key("type")
            .type("type")
            .value(tagViewDto.getType())
            .build());
        searchCriteriaList.add(SearchCriteria.builder()
            .key("name")
            .type("name")
            .value(tagViewDto.getName())
            .build());
        tagSpecification = new TagSpecification(searchCriteriaList);
    }

    @Test
    void toPredicate() {
        SearchCriteria build = SearchCriteria.builder()
            .key("id")
            .type("id")
            .value(tagViewDto.getId())
            .build();
        SearchCriteria build1 = SearchCriteria.builder()
            .key("type")
            .type("type")
            .value(tagViewDto.getType())
            .build();
        SearchCriteria build2 = SearchCriteria.builder()
            .key("name")
            .type("name")
            .value(tagViewDto.getName())
            .build();
        when(criteriaBuilder.conjunction()).thenReturn(expected);
        when(criteriaQuery.distinct(true)).thenReturn(criteriaQuery);
        when(root.get(build.getKey())).thenReturn(objectPath);
        when(criteriaBuilder.equal(objectPath, build.getValue())).thenThrow(NumberFormatException.class);
        when(criteriaBuilder.conjunction()).thenReturn(expected);
        when(criteriaBuilder.disjunction()).thenReturn(expected);
        when(root.get(build1.getKey())).thenReturn(objectPath);
        when(objectPath.as(String.class)).thenReturn(this.as);
        when(criteriaBuilder.like(as, "%" + build1.getValue() + "%")).thenReturn(expected);
        when(criteriaBuilder.and(expected, expected)).thenReturn(expected);
        when(root.join(Tag_.tagTranslations)).thenReturn(tagTagTranslationJoin);
        when(tagTagTranslationJoin.get(TagTranslation_.NAME)).thenReturn(objectPath);
        when(criteriaBuilder.like(any(), eq("%" + build2.getValue() + "%"))).thenReturn(expected);
        when(tagTagTranslationJoin.get(TagTranslation_.tag)).thenReturn(tagPath);
        when(tagPath.get(Tag_.ID)).thenReturn(objectPath);
        when(root.get(Tag_.ID)).thenReturn(objectPath);
        when(criteriaBuilder.equal(objectPath, objectPath)).thenReturn(expected);
        when(criteriaBuilder.and(expected, expected)).thenReturn(expected);
        Predicate actual = tagSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);
        assertEquals(expected, actual);
    }
}