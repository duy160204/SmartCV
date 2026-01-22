package com.example.SmartCV.modules.ai.client;

import com.example.SmartCV.common.exception.BusinessException;
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
public class OpenAiClient implements AiProvider {

        private final RestTemplate restTemplate;

        @Value("${ai.openai.model}")
        private String model;

        @Value("${ai.openai.base-url}")
        private String baseUrl;

        public OpenAiClient(
                        @Value("${ai.openai.api-key}") String apiKey,
                        @Value("${ai.openai.base-url}") String baseUrl,
                        RestTemplateBuilder builder) {
                this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
                this.restTemplate = builder
                                .setConnectTimeout(Duration.ofSeconds(10))
                                .setReadTimeout(Duration.ofSeconds(30))
                                .defaultHeader("Authorization", "Bearer " + apiKey)
                                .build();
        }

        @Override
        public String chat(String prompt) {

                Map<String, Object> body = Map.of(
                                "model", model,
                                "messages", List.of(
                                                Map.of("role", "user", "content", prompt)),
                                "temperature", 0.5 // Lower temperature for more consistent, professional results
                );

                try {
                        // Using RestTemplate (Blocking I/O but safe in Servlet thread pool compared to
                        // block() in potentially reactive context)
                        // And we have configured timeouts.
                        Map response = restTemplate.postForObject(baseUrl + "/chat/completions", body, Map.class);

                        if (response == null || !response.containsKey("choices")) {
                                throw new RuntimeException("Invalid response from AI provider");
                        }

                        List<Map> choices = (List<Map>) response.get("choices");
                        if (choices.isEmpty()) {
                                return "";
                        }
                        Map message = (Map) choices.get(0).get("message");
                        return message.get("content").toString();

                } catch (Exception e) {
                        // Log error in caller or here
                        throw new BusinessException("AI Service Error: " + e.getMessage(),
                                        HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }
}
