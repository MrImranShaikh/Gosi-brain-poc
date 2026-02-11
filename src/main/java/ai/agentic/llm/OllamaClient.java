package ai.agentic.llm;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class OllamaClient implements LLMClient {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String model;

    public OllamaClient(String model) {
        this.model = model;
    }

    @Override
    public String generate(String prompt) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", model);
            payload.put("prompt", prompt);
            payload.put("stream", false);

            String requestBody = mapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Ollama HTTP error: " + response.body());
            }

            return extractResponse(response.body());

        } catch (Exception e) {
            throw new RuntimeException("LLM call failed", e);
        }
    }

    private String extractResponse(String body) {
        try {
            return mapper.readTree(body)
                    .get("response")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException("Invalid Ollama response: " + body, e);
        }
    }
}
