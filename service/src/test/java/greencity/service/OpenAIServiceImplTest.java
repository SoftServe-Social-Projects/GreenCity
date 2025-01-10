package greencity.service;

import greencity.constant.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAIServiceImplTest {
    @InjectMocks
    private OpenAIServiceImpl openAIService;

    @Mock
    private RestTemplate restTemplate;
    String apiKey = "mock-api-key";
    String ariUrl = "https://api.openai.com/v1/chat/completions";

    @BeforeEach
    void setUp() {
        openAIService.setApiKey(apiKey);
        openAIService.setApiUrl(ariUrl);
    }

    @Test
    void makeRequest_returnsResponseContent() {
        Map<String, Object> mockResponseBody = Map.of(
            "choices", List.of(
                Map.of("message", Map.of("content", "Hello, how can I help you?"))));

        when(restTemplate.exchange(
            eq(ariUrl),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, Object>>() {
            }))).thenReturn(new ResponseEntity<>(mockResponseBody, HttpStatus.OK));

        String result = openAIService.makeRequest("Say hello");

        assertEquals("Hello, how can I help you?", result);
    }

    @Test
    void makeRequest_returnsErrorMessage_whenResponseIsInvalid() {
        Map<String, Object> mockResponseBody = Map.of();

        when(restTemplate.exchange(
            eq(ariUrl),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, Object>>() {
            }))).thenReturn(new ResponseEntity<>(mockResponseBody, HttpStatus.OK));

        String result = openAIService.makeRequest("Say hello");

        assertEquals(ErrorMessage.OPEN_AI_IS_NOT_RESPONDING, result);
    }
}