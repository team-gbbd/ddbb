package com.ddbb.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiClient {

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=%s";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${google.api.key:}")
    private String googleApiKey;

    public String requestInsights(String prompt) {
        String apiKey = resolveApiKey();
        String requestUrl = GEMINI_URL.formatted(apiKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        try {
            String payload = objectMapper.writeValueAsString(body);
            HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, requestEntity, String.class);
            String responseBody = response.getBody();

            if (responseBody == null || responseBody.isBlank()) {
                throw new IllegalStateException("Gemini API returned empty body");
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode textNode = root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (textNode.isMissingNode() || textNode.isNull()) {
                throw new IllegalStateException("Unexpected Gemini API response: " + responseBody);
            }

            return textNode.asText();
        } catch (Exception e) {
            log.error("Failed to call Gemini API", e);
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    private String resolveApiKey() {
        if (googleApiKey != null && !googleApiKey.isBlank()) {
            return googleApiKey;
        }
        String envKey = System.getenv("GOOGLE_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey;
        }
        throw new IllegalStateException("Google API key is not configured. Set 'google.api.key' or GOOGLE_API_KEY env.");
    }
}
