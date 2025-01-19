package greencity.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class EndpointValidator {
    private final List<String> validEndpoints;

    private static EndpointValidator instance;

    public EndpointValidator(@Value("${valid.endpoints}") List<String> validEndpoints) {
        this.validEndpoints = validEndpoints;
        instance = this;
    }

    public static boolean isValidEndpoint(String endpointTemplate, String actualUrl) {
        String[] templateParts = endpointTemplate.split("/");
        String[] urlParts = actualUrl.split("/");
        if (templateParts.length != urlParts.length) {
            return false;
        }
        for (int i = 0; i < templateParts.length; i++) {
            String templatePart = templateParts[i];
            String urlPart = urlParts[i];

            if (templatePart.startsWith("{") && templatePart.endsWith("}")) {
                if (!urlPart.matches("\\d+")) {
                    return false;
                }
            } else {
                if (!templatePart.equals(urlPart)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean hasExtraCharacters(String url) {
        return !instance.validEndpoints.contains(url);
    }

    public static boolean checkUrl(String url) {
        if (hasExtraCharacters(url)) {
            for (String validEndpoint : instance.validEndpoints) {
                if (isValidEndpoint(validEndpoint, url)) {
                    return true;
                }
            }
            return false;
        }

        for (String validEndpoint : instance.validEndpoints) {
            if (isValidEndpoint(validEndpoint, url)) {
                return true;
            }
        }
        return false;
    }
}
