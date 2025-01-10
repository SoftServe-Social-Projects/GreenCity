package greencity.config;

import greencity.exception.exceptions.FailedToObtainDatasourceTimezone;
import java.time.ZoneId;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/**
 * Configuration class that obtains metadata of the database.
 *
 * @author Yurii Koval
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DatasourceMetadata {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Gets zoneId of the database. WARNING: this method uses native query for
     * PostgreSQL. It may NOT WORK with another DBMS.
     *
     * @return zoneId instance of the database.
     */
    @Bean
    public ZoneId datasourceTimezone() {
        String zoneId = jdbcTemplate.queryForObject("SHOW TIMEZONE", String.class);
        if (zoneId == null) {
            String errorMessage = "Didn't manage to obtain datasource timezone!";
            log.error(errorMessage);
            throw new FailedToObtainDatasourceTimezone(errorMessage);
        }
        log.info("Obtained timezone of the database is {}", zoneId);
        return ZoneId.of(zoneId);
    }

    /**
     * Creates functions for database on application startup.
     *
     * @author Vasyl Zhovnir
     */
    @EventListener(ApplicationReadyEvent.class)
    public void createFunctions() {
        ResourceDatabasePopulator databasePopulator =
            new ResourceDatabasePopulator(true, true, "UTF-8");
        databasePopulator.addScript(
            new ClassPathResource("db/functions/fn_recommended_econews_by_opened_eco_news.sql"));
        databasePopulator.addScript(new ClassPathResource("db/functions/fn_searcheconews.sql"));
        databasePopulator.addScript(new ClassPathResource("db/functions/pg_buffercache_pages.sql"));
        databasePopulator.addScript(new ClassPathResource("db/functions/pg_stat_statements.sql"));
        databasePopulator.addScript(new ClassPathResource("db/functions/pg_stat_statements_reset.sql"));
        databasePopulator.addScript(new ClassPathResource("db/functions/fn_recommended_friends.sql"));
        databasePopulator.execute(Objects.requireNonNull(jdbcTemplate.getDataSource()));
    }
}
