package greencity.config;

import com.google.maps.GeoApiContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleApiConfiguration {
    @Value("${greencity.authorization.googleApiKey}")
    private String googleApiKey;

    /**
     * Method create ApiContext.
     *
     * @return {@link GeoApiContext}
     */
    @Bean
    GeoApiContext context() {
        return new GeoApiContext.Builder().apiKey(googleApiKey).build();
    }
}
