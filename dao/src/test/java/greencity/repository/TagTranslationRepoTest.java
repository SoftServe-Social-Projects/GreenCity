package greencity.repository;

import greencity.ModelUtils;
import greencity.entity.localization.TagTranslation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@Sql("classpath:sql/tags.sql")
class TagTranslationRepoTest {
    @Autowired
    private TagTranslationRepo tagTranslationRepo;

    private static final String UKRAINIAN_LANGUAGE = "ua";
    private static final String ENGLISH_LANGUAGE = "en";

    private static List<Long> convertTagTranslationsListToLongList(List<TagTranslation> tagTranslations) {
        return tagTranslations.stream().map(TagTranslation::getId).collect(Collectors.toList());
    }

    @Test
    void bulkDeleteByTagId() {
        List<Long> ids = Collections.singletonList(1L);

        tagTranslationRepo.bulkDeleteByTagId(ids);
        List<TagTranslation> remainingTags = tagTranslationRepo.findAll();

        assertEquals(6, remainingTags.size());
    }

    @Test
    void findAllEcoNewsTagsWithEnglishTest() {
        List<TagTranslation> actual = tagTranslationRepo.findAllEcoNewsTags(ENGLISH_LANGUAGE);
        List<TagTranslation> expected =
            Arrays.asList(ModelUtils.getTagTranslationsNews().get(1), ModelUtils.getTagTranslationsEducation().get(1),
                ModelUtils.getTagTranslationsAds().get(1));
        List<Long> expectedIds = convertTagTranslationsListToLongList(expected);
        List<Long> actualIds = convertTagTranslationsListToLongList(actual);

        assertEquals(3, actual.size());
        assertEquals(expectedIds, actualIds);
    }

    @Test
    void findAllEcoNewsTagsWithUkrainianTest() {
        List<TagTranslation> actual = tagTranslationRepo.findAllEcoNewsTags(UKRAINIAN_LANGUAGE);
        List<TagTranslation> expected =
            Arrays.asList(ModelUtils.getTagTranslationsNews().get(0), ModelUtils.getTagTranslationsEducation().get(0),
                ModelUtils.getTagTranslationsAds().get(0));
        List<Long> expectedIds = convertTagTranslationsListToLongList(expected);
        List<Long> actualIds = convertTagTranslationsListToLongList(actual);

        assertEquals(3, actual.size());
        assertEquals(expectedIds, actualIds);
    }
}