package greencity.mapping;

import greencity.ModelUtils;
import greencity.constant.AppConstant;
import greencity.dto.econews.EcoNewsDto;
import greencity.entity.EcoNews;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;

import greencity.entity.localization.TagTranslation;
import greencity.service.CommentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class EcoNewsDtoMapperTest {
    @Mock
    CommentService commentService;
    @InjectMocks
    EcoNewsDtoMapper ecoNewsDtoMapper;

    @Test
    void convertTest() {
        EcoNews ecoNews = ModelUtils.getEcoNewsForMethodConvertTest();
        String defaultLanguage = AppConstant.DEFAULT_LANGUAGE_CODE;

        EcoNewsDto expected = EcoNewsDto.builder()
            .id(ecoNews.getId())
            .tagsUa(ecoNews.getTags().stream()
                .flatMap(t -> t.getTagTranslations().stream())
                .filter(t -> t.getLanguage().getCode().equals("ua"))
                .map(TagTranslation::getName)
                .collect(Collectors.toList()))
            .tags(ecoNews.getTags().stream()
                .flatMap(t -> t.getTagTranslations().stream())
                .filter(t -> t.getLanguage().getCode().equals(defaultLanguage))
                .map(TagTranslation::getName)
                .collect(Collectors.toList()))
            .countComments(commentService.countCommentsForEcoNews(ecoNews.getId()))
            .title(ecoNews.getTitle())
            .shortInfo(ecoNews.getShortInfo())
            .imagePath(ecoNews.getImagePath())
            .likes(ecoNews.getUsersLikedNews().size())
            .author(ModelUtils.getEcoNewsAuthorDto())
            .creationDate(ecoNews.getCreationDate())
            .content("text")
            .hidden(false)
            .build();

        assertEquals(expected, ecoNewsDtoMapper.convert(ecoNews));
    }
}
