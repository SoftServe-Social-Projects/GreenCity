package greencity.service;

import greencity.constant.ErrorMessage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Setter
@Slf4j
@Service
public class OpenAIServiceImpl implements OpenAIService {
    @Value("${openai.api.key}")
    private String apiKey;
    @Value("${openai.api.url}")
    private String apiUrl;
    private final RestTemplate restTemplate;

    public OpenAIServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String makeRequest(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + apiKey);
        headers.add("Content-Type", "application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o-mini");
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));
        body.put("messages", messages);
        body.put("max_tokens", 450);
        body.put("temperature", 0.8);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {
                });

            return Optional.ofNullable(response)
                .map(ResponseEntity::getBody)
                .filter(responseBody -> responseBody.containsKey("choices"))
                .map(responseBody -> (List<Map<String, Object>>) responseBody.get("choices"))
                .filter(choices -> !choices.isEmpty())
                .map(choices -> choices.get(0))
                .map(choice -> (Map<String, Object>) choice.get("message"))
                .map(message -> (String) message.get("content"))
                .orElse(ErrorMessage.OPEN_AI_IS_NOT_RESPONDING);
        } catch (Exception e) {
            return ErrorMessage.OPEN_AI_IS_NOT_RESPONDING;
        }
    }
}
