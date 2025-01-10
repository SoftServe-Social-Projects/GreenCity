package greencity.mapping;

import greencity.dto.econews.EcoNewsVO;
import greencity.dto.language.LanguageVO;
import greencity.dto.tag.TagTranslationVO;
import greencity.dto.tag.TagVO;
import greencity.dto.user.UserVO;
import greencity.entity.EcoNews;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class EcoNewsVOMapper extends AbstractConverter<EcoNews, EcoNewsVO> {
    @Override
    protected EcoNewsVO convert(EcoNews ecoNews) {
        return EcoNewsVO.builder()
            .id(ecoNews.getId())
            .author(UserVO.builder()
                .id(ecoNews.getAuthor().getId())
                .name(ecoNews.getAuthor().getName())
                .email(ecoNews.getAuthor().getEmail())
                .userStatus(ecoNews.getAuthor().getUserStatus())
                .role(ecoNews.getAuthor().getRole())
                .build())
            .creationDate(ecoNews.getCreationDate())
            .imagePath(ecoNews.getImagePath())
            .source(ecoNews.getSource())
            .text(ecoNews.getText())
            .title(ecoNews.getTitle())
            .tags(ecoNews.getTags().stream()
                .map(tag -> TagVO.builder()
                    .id(tag.getId())
                    .tagTranslations(tag.getTagTranslations().stream()
                        .map(tagTranslation -> TagTranslationVO.builder()
                            .name(tagTranslation.getName())
                            .id(tagTranslation.getId())
                            .languageVO(LanguageVO.builder()
                                .code(tagTranslation.getLanguage().getCode())
                                .id(tagTranslation.getId())
                                .build())
                            .build())
                        .collect(Collectors.toList()))
                    .build())
                .collect(Collectors.toList()))
            .usersLikedNews(ecoNews.getUsersLikedNews().stream()
                .map(user -> UserVO.builder()
                    .id(user.getId())
                    .build())
                .collect(Collectors.toSet()))
            .usersDislikedNews(ecoNews.getUsersDislikedNews().stream()
                .map(user -> UserVO.builder()
                    .id(user.getId())
                    .build())
                .collect(Collectors.toSet()))
            .build();
    }
}
