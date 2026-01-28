package com.example.SmartCV.modules.ai.client;

import com.example.SmartCV.common.exception.BusinessException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.example.SmartCV.modules.ai.dto.UnifiedAiRequest;
import com.example.SmartCV.modules.ai.dto.UnifiedAiResponse;

@Component
@Primary
@ConditionalOnProperty(name = "ai.gemini.enabled", havingValue = "true", matchIfMissing = true)
public class GeminiAiProvider implements AiProvider {

    private final RestTemplate restTemplate;

    @Value("${ai.gemini.model:gemini-2.0-flash}")
    private String model;

    @Value("${ai.gemini.api-key}")
    private String apiKey;

    private static final String BASE_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1/models/%s:generateContent?key=%s";

    public GeminiAiProvider(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public UnifiedAiResponse chat(UnifiedAiRequest request) {
        String url = String.format(BASE_URL_TEMPLATE, model, apiKey);

        long startTime = System.currentTimeMillis();

        // Build Gemini Request Body
        // { "contents": [{ "parts": [{ "text": "prompt" }] }] }
        // Prepend system message to user message for Gemini as it supports it
        // differently in beta
        // For stability, we just prepend it to the text.
        String finalPrompt = request.getSystemMessage() + "\n\n" + request.getUserMessage();

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", finalPrompt)))));

        try {
            Map response = restTemplate.postForObject(url, body, Map.class);

            if (response == null || !response.containsKey("candidates")) {
                throw new BusinessException("AI Service Unavailable: Empty response", HttpStatus.SERVICE_UNAVAILABLE);
            }

            List<Map> candidates = (List<Map>) response.get("candidates");
            if (candidates.isEmpty()) {
                return new UnifiedAiResponse("");
            }

            Map firstCandidate = candidates.get(0);
            Map content = (Map) firstCandidate.get("content");
            List<Map> parts = (List<Map>) content.get("parts");

            if (parts.isEmpty()) {
                return new UnifiedAiResponse("");
            }

            String text = parts.get(0).get("text").toString();
            long latency = System.currentTimeMillis() - startTime;

            return UnifiedAiResponse.builder()
                    .content(text)
                    .provider(AiProviderType.GEMINI)
                    .latencyMs(latency)
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("AI Processing Failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public AiProviderType getType() {
        return AiProviderType.GEMINI;
    }
}
