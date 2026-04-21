package com.example.SmartCV.modules.ai.service;

import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import com.example.SmartCV.common.exception.BusinessException;
import org.springframework.web.client.HttpServerErrorException;
import java.util.concurrent.TimeoutException;
import com.example.SmartCV.common.utils.UserPrincipal;

import com.example.SmartCV.modules.ai.client.AiProvider;
import com.example.SmartCV.modules.ai.client.AiProviderFactory;
import com.example.SmartCV.modules.ai.dto.UnifiedAiRequest;
import com.example.SmartCV.modules.ai.dto.UnifiedAiResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.SmartCV.modules.cv.dto.UnifiedCVDTO;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final AiProviderFactory aiProviderFactory;
    private final AiRateLimiter aiRateLimiter; 
    private final ObjectMapper objectMapper;

    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
                return ((UserPrincipal) auth.getPrincipal()).getId();
            }
            return 0L; 
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * EXPLICIT EXCEPTION RESTRICTION:
     * Retries ONLY RateLimit Exceptions, Timeouts, and 500 Network crashes. 
     * Invalid user JSON or logic errors will correctly fall straight through without triggering duplicate loops.
     */
    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackChatWithCv")
    @Retryable(
        include = { BusinessException.class, TimeoutException.class, HttpServerErrorException.class },
        maxAttempts = 3, 
        backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public String chatWithCv(String cvContent, String userMessage, String level) {
        aiRateLimiter.checkRateLimit(getCurrentUserId());
        String systemMessage = AiPrompts.SAFETY_INSTRUCTIONS + "\nBạn là chuyên gia tuyển dụng..." + AiPrompts.buildLevelContext(level);
        
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(systemMessage)
                .userMessage("=== NỘI DUNG CV ===\n" + cvContent + "\n\n=== CÂU HỎI CỦA NGƯỜI DÙNG ===\n" + userMessage)
                .build();
        return executeChat(aiProviderFactory.getPrimaryProvider(), request);
    }

    public String chatWithCv(String cvContent, String userMessage) {
        return chatWithCv(cvContent, userMessage, null);
    }

    public String fallbackChatWithCv(String cvContent, String userMessage, String level, Throwable t) {
        log.warn("Falling back to OpenAI for chatWithCv. Reason: {}", t.getMessage());
        String systemMessage = AiPrompts.SAFETY_INSTRUCTIONS + "\nBạn là chuyên gia tuyển dụng..." + AiPrompts.buildLevelContext(level);
        
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(systemMessage)
                .userMessage("=== NỘI DUNG CV ===\n" + cvContent + "\n\n=== CÂU HỎI CỦA NGƯỜI DÙNG ===\n" + userMessage)
                .build();
        return executeChat(aiProviderFactory.getFallbackProvider(), request);
    }

    public String fallbackChatWithCv(String cvContent, String userMessage, Throwable t) {
        return fallbackChatWithCv(cvContent, userMessage, null, t);
    }

    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackGenerateCvContent")
    @Retryable(include = { BusinessException.class, TimeoutException.class, HttpServerErrorException.class }, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public String generateCvContent(String prompt, String templateConfigJson) {
        aiRateLimiter.checkRateLimit(getCurrentUserId());
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\n" + AiPrompts.GENERATE_CV_PROMPT)
                .userMessage("=== USER BACKGROUND ===\n" + prompt + "\n\n=== SECTION CONFIG ===\n" + templateConfigJson)
                .build();
        String responseContent = executeChat(aiProviderFactory.getPrimaryProvider(), request);
        return validateJsonOutput(responseContent);
    }

    public String fallbackGenerateCvContent(String prompt, String templateConfigJson, Throwable t) {
        log.warn("Fallback to OpenAI for generateCvContent. Reason: {}", t.getMessage());
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\n" + AiPrompts.GENERATE_CV_PROMPT)
                .userMessage("=== USER BACKGROUND ===\n" + prompt + "\n\n=== SECTION CONFIG ===\n" + templateConfigJson)
                .build();
        String responseContent = executeChat(aiProviderFactory.getFallbackProvider(), request);
        return validateJsonOutput(responseContent);
    }

    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackImproveText")
    @Retryable(include = { BusinessException.class, TimeoutException.class, HttpServerErrorException.class }, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public String improveText(String text, String instruction) {
        aiRateLimiter.checkRateLimit(getCurrentUserId());
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\n" + AiPrompts.IMPROVE_TEXT_PROMPT)
                .userMessage("=== INSTRUCTION ===\n" + instruction + "\n\n=== TEXT TO IMPROVE ===\n" + text)
                .build();
        return executeChat(aiProviderFactory.getPrimaryProvider(), request);
    }

    public String fallbackImproveText(String text, String instruction, Throwable t) {
        log.warn("Fallback to OpenAI for improveText. Reason: {}", t.getMessage());
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\n" + AiPrompts.IMPROVE_TEXT_PROMPT)
                .userMessage("=== INSTRUCTION ===\n" + instruction + "\n\n=== TEXT TO IMPROVE ===\n" + text)
                .build();
        return executeChat(aiProviderFactory.getFallbackProvider(), request);
    }

    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackBuildTemplateFromImage")
    @Retryable(include = { BusinessException.class, TimeoutException.class, HttpServerErrorException.class }, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public String buildTemplateFromImage(String imageUrl) {
        aiRateLimiter.checkRateLimit(getCurrentUserId());
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\n" + AiPrompts.BUILD_TEMPLATE_PROMPT)
                .userMessage("Convert the image according to the instructions.")
                .imageUrl(imageUrl)
                .build();
        return executeChat(aiProviderFactory.getPrimaryProvider(), request);
    }

    public String fallbackBuildTemplateFromImage(String imageUrl, Throwable t) {
        log.warn("Fallback to OpenAI for buildTemplateFromImage. Reason: {}", t.getMessage());
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\n" + AiPrompts.BUILD_TEMPLATE_PROMPT)
                .userMessage("Convert the image according to the instructions.")
                .imageUrl(imageUrl)
                .build();
        return executeChat(aiProviderFactory.getFallbackProvider(), request);
    }

    private String executeChat(AiProvider provider, UnifiedAiRequest request) {
        if (provider == null) {
            throw new RuntimeException("AI Provider not configured.");
        }
        UnifiedAiResponse response = provider.chat(request);
        log.info("AI Request processed by {} in {}ms", response.getProvider(), response.getLatencyMs());
        return response.getContent();
    }

    private String validateJsonOutput(String rawJson) {
        try {
            String cleanedJson = rawJson;
            if (cleanedJson.contains("```json")) {
                cleanedJson = cleanedJson.replaceAll("```json|```", "").trim();
            } else if (cleanedJson.contains("```")) {
                cleanedJson = cleanedJson.replaceAll("```", "").trim();
            }
            
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(cleanedJson);
            if (rootNode.isObject()) {
                com.fasterxml.jackson.databind.node.ObjectNode root = (com.fasterxml.jackson.databind.node.ObjectNode) rootNode;
                
                if (root.has("profile") && root.get("profile").isObject()) {
                    sanitizeObject((com.fasterxml.jackson.databind.node.ObjectNode) root.get("profile"), java.util.Arrays.asList(
                        "name", "title", "email", "phone", "website", "location", "summary", 
                        "photo", "gender", "birthday", "address", "extras"
                    ));
                }
                
                sanitizeArray(root, "experience", java.util.Arrays.asList("company", "position", "date", "description", "extras"));
                sanitizeArray(root, "skills", java.util.Arrays.asList("name", "level", "extras"));
                sanitizeArray(root, "projects", java.util.Arrays.asList("name", "role", "date", "description", "link", "extras"));
                sanitizeArray(root, "languages", java.util.Arrays.asList("language", "proficiency", "extras"));
                sanitizeArray(root, "certifications", java.util.Arrays.asList("name", "issuer", "date", "extras"));
                sanitizeArray(root, "awards", java.util.Arrays.asList("name", "issuer", "year", "extras"));
                sanitizeArray(root, "education", java.util.Arrays.asList("school", "degree", "major", "date", "extras"));
                sanitizeArray(root, "references", java.util.Arrays.asList("name", "position", "company", "contact", "extras"));
            }

            ObjectMapper strictMapper = objectMapper.copy();
            strictMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
            strictMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);
            
            UnifiedCVDTO cleanDto = strictMapper.treeToValue(rootNode, UnifiedCVDTO.class);
            return strictMapper.writeValueAsString(cleanDto);
        } catch (Exception e) {
            log.error("AI Schema Validation Failed: {}", e.getMessage());
            throw new BusinessException("SCHEMA_VIOLATION: Invalid AI JSON format. " + e.getMessage(), org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }

    private void sanitizeArray(com.fasterxml.jackson.databind.node.ObjectNode root, String fieldName, java.util.List<String> coreFields) {
        if (root.has(fieldName) && root.get(fieldName).isArray()) {
            for (com.fasterxml.jackson.databind.JsonNode item : root.get(fieldName)) {
                if (item.isObject()) {
                    sanitizeObject((com.fasterxml.jackson.databind.node.ObjectNode) item, coreFields);
                }
            }
        }
    }

    private void sanitizeObject(com.fasterxml.jackson.databind.node.ObjectNode obj, java.util.List<String> coreFields) {
        com.fasterxml.jackson.databind.node.ObjectNode extrasNode = null;
        if (obj.has("extras") && obj.get("extras").isObject()) {
            extrasNode = (com.fasterxml.jackson.databind.node.ObjectNode) obj.get("extras");
        }
        
        java.util.List<String> fieldNames = new java.util.ArrayList<>();
        obj.fieldNames().forEachRemaining(fieldNames::add);
        
        for (String fieldName : fieldNames) {
            if (!coreFields.contains(fieldName)) {
                if (extrasNode == null) {
                    extrasNode = obj.putObject("extras");
                }
                extrasNode.set(fieldName, obj.get(fieldName));
                obj.remove(fieldName);
            }
        }
    }
}
