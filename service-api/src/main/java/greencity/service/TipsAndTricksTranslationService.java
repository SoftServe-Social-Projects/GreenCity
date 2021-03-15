package greencity.service;

import greencity.dto.tipsandtricks.TextTranslationVO;
import greencity.dto.tipsandtricks.TitleTranslationVO;
import java.util.List;

public interface TipsAndTricksTranslationService {
    /**
     * Method saves all new {@link TitleTranslationVO}.
     *
     * @param titleTranslations {@link TitleTranslationVO}
     * @return list of {@link TitleTranslationVO}
     */
    List<TitleTranslationVO> saveTitleTranslations(List<TitleTranslationVO> titleTranslations);

    /**
     * Method saves all new {@link TextTranslationVO}.
     *
     * @param textTranslations {@link TextTranslationVO}
     * @return list of {@link TextTranslationVO}
     */
    List<TextTranslationVO> saveTextTranslations(List<TextTranslationVO> textTranslations);
}
