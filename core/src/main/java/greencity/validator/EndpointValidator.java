package greencity.validator;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class EndpointValidator {
    @Getter
    private static List<String> validEndpoints = List.of();

    private EndpointValidator() {
    }

    @Value("${valid.endpoints}")
    public void setValidEndpoints(List<String> validEndpointList) {
        validEndpoints = validEndpointList;
    }

    private static boolean isValidEndpoint(String endpointTemplate, String actualUrl) {
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

    private boolean hasExtraCharacters(String url) {
        return !validEndpoints.contains(url);
    }

    public static boolean checkUrl(String url) {
        EndpointValidator endpointValidator = new EndpointValidator();
        if (endpointValidator.hasExtraCharacters(url)) {
            for (String validEndpoint : validEndpoints) {
                if (isValidEndpoint(validEndpoint, url)) {
                    return true;
                }
            }
            return false;
        }

        for (String validEndpoint : validEndpoints) {
            if (isValidEndpoint(validEndpoint, url)) {
                return true;
            }
        }
        return false;
    }
}
