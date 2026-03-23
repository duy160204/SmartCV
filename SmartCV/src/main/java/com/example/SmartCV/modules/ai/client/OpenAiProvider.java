package com.example.SmartCV.modules.ai.client;

import com.example.SmartCV.common.exception.BusinessException;
import com.example.SmartCV.modules.ai.dto.UnifiedAiRequest;
import com.example.SmartCV.modules.ai.dto.UnifiedAiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "ai.openai.enabled", havingValue = "true")
public class OpenAiProvider implements AiProvider {

        private final RestClient restClient;
        private final String model;

        public OpenAiProvider(
                        @Value("${ai.openai.api-key:}") String apiKey,
                        @Value("${ai.openai.base-url:https://api.openai.com/v1}") String baseUrl,
                        @Value("${ai.openai.model:gpt-3.5-turbo}") String model,
                        RestClient.Builder builder) {

                this.model = model;

                // Ensure robust base URL formatting (strip trailing slash safely)
                String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

                // Modern timeout setting approach compatible with Spring Boot 3.2 RestClient
                SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                requestFactory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
                requestFactory.setReadTimeout((int) Duration.ofSeconds(30).toMillis());

                // Utilize robust, fluent RestClient
                this.restClient = builder
                                .baseUrl(cleanBaseUrl)
                                .defaultHeader("Authorization", "Bearer " + apiKey)
                                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .requestFactory(requestFactory)
                                .build();
        }

        @Override
        public UnifiedAiResponse chat(UnifiedAiRequest request) {
                long startTime = System.currentTimeMillis();

                Object userContent;
                if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
                        userContent = List.of(
                                        Map.of("type", "text", "text", request.getUserMessage()),
                                        Map.of("type", "image_url", "image_url", Map.of("url", request.getImageUrl())));
                } else {
                        userContent = request.getUserMessage();
                }

                Map<String, Object> body = Map.of(
                                "model", model,
                                "messages", List.of(
                                                Map.of("role", "system", "content", request.getSystemMessage()),
                                                Map.of("role", "user", "content", userContent)),
                                "temperature", 0.5);

                try {
                        Map response = restClient.post()
                                        .uri("/chat/completions")
                                        .body(body)
                                        .retrieve()
                                        .body(Map.class);

                        if (response == null || !response.containsKey("choices")) {
                                throw new BusinessException("Invalid response from AI provider",
                                                HttpStatus.BAD_GATEWAY);
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

                } catch (RestClientResponseException e) {
                        throw new BusinessException(
                                        "OpenAI API Error: " + e.getResponseBodyAsString(),
                                        HttpStatus.valueOf(e.getStatusCode().value()));
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
