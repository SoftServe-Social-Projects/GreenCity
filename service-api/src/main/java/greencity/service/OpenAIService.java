package greencity.service;

/**
 * Interface for interacting with the OpenAI API. The purpose of this interface
 * is to send requests to the OpenAI service and receive responses.
 */
public interface OpenAIService {
    /**
     * Sends a request to the OpenAI API and returns the response as a string.
     *
     * @param request The request as a string to be sent to the OpenAI API.
     * @return The response from the service as a string.
     */
    String makeRequest(String request);
}
