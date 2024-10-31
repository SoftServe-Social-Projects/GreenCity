package greencity.filters;

import greencity.dto.shoppinglistitem.ShoppingListItemDto;
import greencity.entity.ShoppingListItem;
import greencity.entity.ShoppingListItem_;
import greencity.entity.Translation;
import static greencity.entity.Translation_.content;
import greencity.entity.localization.ShoppingListItemTranslation;
import greencity.entity.localization.ShoppingListItemTranslation_;
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
class ShoppingListItemSpecificationTest {

    @Mock
    private CriteriaQuery<?> criteriaQueryMock;
    @Mock
    private CriteriaBuilder criteriaBuilderMock;
    @Mock
    private Predicate expected;
    @Mock
    private Root<ShoppingListItemTranslation> shoppingListItemTranslationRootMock;
    @Mock
    private Root<ShoppingListItem> shoppingListItemRootMock;
    @Mock
    private Path<Object> objectPath;
    @Mock
    private Path<String> stringPath;
    @Mock
    private Path<Long> longPathMock;
    @Mock
    private Path<ShoppingListItem> shoppingListItemPath;
    @Mock
    private SingularAttribute<ShoppingListItem, Long> id;
    @Mock
    private SingularAttribute<ShoppingListItemTranslation, ShoppingListItem> shoppingListItem;
    @Mock
    private SingularAttribute<Translation, String> contentAttr;

    private ShoppingListItemSpecification shoppingListItemSpecification;

    @Mock
    private List<SearchCriteria> searchCriteriaList;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        ShoppingListItemDto shoppingListItemDto = new ShoppingListItemDto();
        shoppingListItemDto.setId(1L);
        shoppingListItemDto.setText("content");
        searchCriteriaList = new ArrayList<>();
        searchCriteriaList.add(SearchCriteria.builder()
            .key("id")
            .type("id")
            .value(shoppingListItemDto.getId())
            .build());
        searchCriteriaList.add(SearchCriteria.builder()
            .key("content")
            .type("content")
            .value(shoppingListItemDto.getText())
            .build());
        ShoppingListItemTranslation_.shoppingListItem = shoppingListItem;
        ShoppingListItem_.id = id;
        content = contentAttr;
        shoppingListItemSpecification = new ShoppingListItemSpecification(searchCriteriaList);
    }

    @Test
    void toPredicate() {
        ShoppingListItemDto shoppingListItemDto = new ShoppingListItemDto();
        shoppingListItemDto.setId(1L);
        shoppingListItemDto.setText("content");
        SearchCriteria buildId = SearchCriteria.builder()
            .key("id")
            .type("id")
            .value(shoppingListItemDto.getId())
            .build();
        SearchCriteria buildContent = SearchCriteria.builder()
            .key("content")
            .type("content")
            .value(shoppingListItemDto.getText())
            .build();
        when(criteriaBuilderMock.conjunction()).thenReturn(expected);
        when(shoppingListItemRootMock.get(buildId.getKey())).thenReturn(objectPath);
        when(criteriaBuilderMock.equal(objectPath, buildId.getValue())).thenThrow(NumberFormatException.class);
        when(criteriaBuilderMock.conjunction()).thenReturn(expected);
        when(criteriaBuilderMock.disjunction()).thenReturn(expected);
        when(criteriaQueryMock.from(ShoppingListItemTranslation.class)).thenReturn(shoppingListItemTranslationRootMock);
        when(criteriaBuilderMock.conjunction()).thenReturn(expected);
        when(shoppingListItemTranslationRootMock.get(content)).thenReturn(stringPath);
        when(criteriaBuilderMock.like(stringPath, "%" + buildContent.getValue() + "%")).thenReturn(expected);
        when(shoppingListItemTranslationRootMock.get(ShoppingListItemTranslation_.shoppingListItem)).thenReturn(
            shoppingListItemPath);
        when(shoppingListItemPath.get(ShoppingListItem_.id)).thenReturn(longPathMock);
        when(shoppingListItemRootMock.get(ShoppingListItem_.id)).thenReturn(longPathMock);
        when(criteriaBuilderMock.equal(longPathMock, longPathMock)).thenReturn(expected);
        when(criteriaBuilderMock.and(expected, expected)).thenReturn(expected);
        Predicate predicate = shoppingListItemSpecification
            .toPredicate(shoppingListItemRootMock, criteriaQueryMock, criteriaBuilderMock);
        assertEquals(expected, predicate);
    }
}
