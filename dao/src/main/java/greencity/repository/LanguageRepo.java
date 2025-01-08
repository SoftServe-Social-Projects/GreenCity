package greencity.repository;

import greencity.entity.Language;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LanguageRepo extends JpaRepository<Language, Long> {
    /**
     * method, that returns {@link Language} by it's code.
     *
     * @param languageCode code of the language.
     * @return {@link Language} by it's code.
     */
    Optional<Language> findByCode(String languageCode);

    /**
     * method, that returns codes of all {@link Language}s.
     *
     * @return {@link List} of language code strings.
     */
    @Query("SELECT code FROM Language")
    List<String> findAllLanguageCodes();
}
