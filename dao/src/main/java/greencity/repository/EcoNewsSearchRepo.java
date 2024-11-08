package greencity.repository;

import greencity.entity.EcoNews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EcoNewsSearchRepo {
    /**
     * Method for search eco news by title,text,short info and tag name.
     *
     * @param searchingText - text criteria for searching.
     * @return all finding eco news, their tags and also count of finding eco news.
     */
    Page<EcoNews> find(Pageable pageable, String searchingText, Boolean isFavorite, Long userId);
}
