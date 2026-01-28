package com.example.SmartCV.modules.ai.client;

import com.example.SmartCV.common.exception.BusinessException;
import com.example.SmartCV.modules.ai.dto.UnifiedAiRequest;
import com.example.SmartCV.modules.ai.dto.UnifiedAiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "ai.openai.enabled", havingValue = "true")
public class OpenAiProvider implements AiProvider {

        private final RestTemplate restTemplate;

        @Value("${ai.openai.model:gpt-3.5-turbo}")
        private String model;

        @Value("${ai.openai.base-url}")
        private String baseUrl;

        public OpenAiProvider(
                        @Value("${ai.openai.api-key}") String apiKey,
                        @Value("${ai.openai.base-url}") String baseUrl,
                        RestTemplateBuilder builder) {
                this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
                this.restTemplate = builder
                                .connectTimeout(Duration.ofSeconds(10))
                                .readTimeout(Duration.ofSeconds(30))
                                .defaultHeader("Authorization", "Bearer " + apiKey)
                                .build();
        }

        @Override
        public UnifiedAiResponse chat(UnifiedAiRequest request) {
                long startTime = System.currentTimeMillis();

                Map<String, Object> body = Map.of(
                                "model", model,
                                "messages", List.of(
                                                Map.of("role", "system", "content", request.getSystemMessage()),
                                                Map.of("role", "user", "content", request.getUserMessage())),
                                "temperature", 0.5);

                try {
                        Map response = restTemplate.postForObject(baseUrl + "/chat/completions", body, Map.class);

                        if (response == null || !response.containsKey("choices")) {
                                throw new RuntimeException("Invalid response from AI provider");
                        }

                        List<Map> choices = (List<Map>) response.get("choices");
                        if (choices.isEmpty()) {
                                return new UnifiedAiResponse("");
                        }
                        Map message = (Map) choices.get(0).get("message");
                        String text = message.get("content").toString();

                        long latency = System.currentTimeMillis() - startTime;

                        return UnifiedAiResponse.builder()
                                        .content(text)
                                        .provider(AiProviderType.OPENAI)
                                        .latencyMs(latency)
                                        .build();

                } catch (Exception e) {
                        throw new BusinessException("AI Service Error: " + e.getMessage(),
                                        HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }

        @Override
        public AiProviderType getType() {
                return AiProviderType.OPENAI;
        }
}
