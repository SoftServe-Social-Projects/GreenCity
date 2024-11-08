package greencity.filters;

import greencity.dto.todolistitem.ToDoListItemDto;
import greencity.entity.ToDoListItem;
import greencity.entity.ToDoListItem_;
import greencity.entity.Translation;
import static greencity.entity.Translation_.content;
import greencity.entity.localization.ToDoListItemTranslation;
import greencity.entity.localization.ToDoListItemTranslation_;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.SingularAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class ToDoListItemSpecificationTest {

    @Mock
    private CriteriaQuery<?> criteriaQueryMock;
    @Mock
    private CriteriaBuilder criteriaBuilderMock;
    @Mock
    private Predicate expected;
    @Mock
    private Root<ToDoListItemTranslation> toDoListItemTranslationRootMock;
    @Mock
    private Root<ToDoListItem> toDoListItemRootMock;
    @Mock
    private Path<Object> objectPath;
    @Mock
    private Path<String> stringPath;
    @Mock
    private Path<Long> longPathMock;
    @Mock
    private Path<ToDoListItem> toDoListItemPath;
    @Mock
    private SingularAttribute<ToDoListItem, Long> id;
    @Mock
    private SingularAttribute<ToDoListItemTranslation, ToDoListItem> toDoListItem;
    @Mock
    private SingularAttribute<Translation, String> contentAttr;

    private ToDoListItemSpecification toDoListItemSpecification;

    @Mock
    private List<SearchCriteria> searchCriteriaList;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        ToDoListItemDto toDoListItemDto = new ToDoListItemDto();
        toDoListItemDto.setId(1L);
        toDoListItemDto.setText("content");
        searchCriteriaList = new ArrayList<>();
        searchCriteriaList.add(SearchCriteria.builder()
            .key("id")
            .type("id")
            .value(toDoListItemDto.getId())
            .build());
        searchCriteriaList.add(SearchCriteria.builder()
            .key("content")
            .type("content")
            .value(toDoListItemDto.getText())
            .build());
        ToDoListItemTranslation_.toDoListItem = toDoListItem;
        ToDoListItem_.id = id;
        content = contentAttr;
        toDoListItemSpecification = new ToDoListItemSpecification(searchCriteriaList);
    }

    @Test
    void toPredicate() {
        ToDoListItemDto toDoListItemDto = new ToDoListItemDto();
        toDoListItemDto.setId(1L);
        toDoListItemDto.setText("content");
        SearchCriteria buildId = SearchCriteria.builder()
            .key("id")
            .type("id")
            .value(toDoListItemDto.getId())
            .build();
        SearchCriteria buildContent = SearchCriteria.builder()
            .key("content")
            .type("content")
            .value(toDoListItemDto.getText())
            .build();
        when(criteriaBuilderMock.conjunction()).thenReturn(expected);
        when(toDoListItemRootMock.get(buildId.getKey())).thenReturn(objectPath);
        when(criteriaBuilderMock.equal(objectPath, buildId.getValue())).thenThrow(NumberFormatException.class);
        when(criteriaBuilderMock.conjunction()).thenReturn(expected);
        when(criteriaBuilderMock.disjunction()).thenReturn(expected);
        when(criteriaQueryMock.from(ToDoListItemTranslation.class)).thenReturn(toDoListItemTranslationRootMock);
        when(criteriaBuilderMock.conjunction()).thenReturn(expected);
        when(toDoListItemTranslationRootMock.get(content)).thenReturn(stringPath);
        when(criteriaBuilderMock.like(stringPath, "%" + buildContent.getValue() + "%")).thenReturn(expected);
        when(toDoListItemTranslationRootMock.get(ToDoListItemTranslation_.toDoListItem)).thenReturn(
            toDoListItemPath);
        when(toDoListItemPath.get(ToDoListItem_.id)).thenReturn(longPathMock);
        when(toDoListItemRootMock.get(ToDoListItem_.id)).thenReturn(longPathMock);
        when(criteriaBuilderMock.equal(longPathMock, longPathMock)).thenReturn(expected);
        when(criteriaBuilderMock.and(expected, expected)).thenReturn(expected);
        Predicate predicate = toDoListItemSpecification
            .toPredicate(toDoListItemRootMock, criteriaQueryMock, criteriaBuilderMock);
        assertEquals(expected, predicate);
    }
}
