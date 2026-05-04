package com.example.SmartCV.modules.ai.service;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import com.example.SmartCV.common.exception.BusinessException;
import com.example.SmartCV.modules.ai.client.AiProvider;
import com.example.SmartCV.modules.ai.client.AiProviderFactory;
import com.example.SmartCV.modules.ai.dto.UnifiedAiRequest;
import com.example.SmartCV.modules.ai.dto.UnifiedAiResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.SmartCV.modules.cv.dto.UnifiedCVDTO;

/**
 * Standardized AI Service - OpenAI ONLY
 * - Integrated with Resilience4j CircuitBreaker.
 * - Single point of failure handling.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final AiProviderFactory aiProviderFactory;
    private final ObjectMapper objectMapper;

    @CircuitBreaker(name = "openai", fallbackMethod = "fallbackOpenAi")
    public String chatWithCv(String cvContent, String userMessage, String level, String locale) {
        String localeRule = (locale != null && locale.equalsIgnoreCase("vi")) 
            ? "You MUST respond in Vietnamese." : "You MUST respond in English.";
            
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\n" + localeRule + "\n" + AiPrompts.buildLevelContext(level))
                .userMessage("=== CV CONTENT ===\n" + cvContent + "\n\n=== QUESTION ===\n" + userMessage)
                .build();
                
        return execute(request);
    }

    @CircuitBreaker(name = "openai", fallbackMethod = "fallbackOpenAi")
    public String chatWithCv(String cvContent, String userMessage) {
        return chatWithCv(cvContent, userMessage, null, "en");
    }

    @CircuitBreaker(name = "openai", fallbackMethod = "fallbackOpenAi")
    public String generateCvContent(String prompt, String templateConfigJson, String locale) {
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\n" + AiPrompts.GENERATE_CV_PROMPT)
                .userMessage("=== BACKGROUND ===\n" + prompt + "\n\n=== CONFIG ===\n" + templateConfigJson)
                .build();
                
        return validateJsonOutput(execute(request));
    }

    @CircuitBreaker(name = "openai", fallbackMethod = "fallbackOpenAi")
    public String improveText(String text, String instruction, String locale) {
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\n" + AiPrompts.IMPROVE_TEXT_PROMPT)
                .userMessage("=== INSTRUCTION ===\n" + instruction + "\n\n=== TEXT ===\n" + text)
                .build();
                
        return execute(request);
    }

    @CircuitBreaker(name = "openai", fallbackMethod = "fallbackOpenAi")
    public String buildTemplateFromImage(String imageUrl, String locale) {
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\n" + AiPrompts.BUILD_TEMPLATE_PROMPT)
                .userMessage("Convert image to template.")
                .imageUrl(imageUrl)
                .build();
                
        return execute(request);
    }

    private String execute(UnifiedAiRequest request) {
        AiProvider provider = aiProviderFactory.getPrimaryProvider();
        try {
            UnifiedAiResponse response = provider.chat(request);
            return response.getContent();
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.error("OpenAI Rate Limit (429) detected.");
                throw new BusinessException("OPENAI_RATE_LIMIT", HttpStatus.TOO_MANY_REQUESTS);
            }
            log.error("OpenAI HTTP Error {}: {}", e.getStatusCode(), e.getMessage());
            throw new BusinessException("OPENAI_ERROR", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("Generic OpenAI Execution Error: {}", e.getMessage());
            throw new BusinessException("OPENAI_ERROR", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private String fallbackOpenAi(Throwable t) {
        log.error("CIRCUIT OPEN/FAILURE detected for OpenAI: {}", t.getMessage());
        return "The AI assistant is temporarily unavailable. Please try again in a few minutes.";
    }

    private String validateJsonOutput(String rawJson) {
        try {
            String cleanedJson = rawJson.replaceAll("```json|```", "").trim();
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(cleanedJson);
            UnifiedCVDTO cleanDto = objectMapper.treeToValue(rootNode, UnifiedCVDTO.class);
            return objectMapper.writeValueAsString(cleanDto);
        } catch (Exception e) {
            log.error("AI Schema Validation Failed: {}", e.getMessage());
            throw new BusinessException("SCHEMA_VIOLATION", HttpStatus.BAD_REQUEST);
        }
    }
}
