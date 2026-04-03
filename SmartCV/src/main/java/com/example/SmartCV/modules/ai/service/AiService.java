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

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final AiProviderFactory aiProviderFactory;
    private final AiRateLimiter aiRateLimiter; 

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
    public String chatWithCv(String cvContent, String userMessage) {
        aiRateLimiter.checkRateLimit(getCurrentUserId());
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\nBạn là chuyên gia tuyển dụng...")
                .userMessage("=== NỘI DUNG CV ===\n" + cvContent + "\n\n=== CÂU HỎI CỦA NGƯỜI DÙNG ===\n" + userMessage)
                .build();
        return executeChat(aiProviderFactory.getPrimaryProvider(), request);
    }

    public String fallbackChatWithCv(String cvContent, String userMessage, Throwable t) {
        log.warn("Falling back to OpenAI for chatWithCv. Reason: {}", t.getMessage());
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\nBạn là chuyên gia tuyển dụng...")
                .userMessage("=== NỘI DUNG CV ===\n" + cvContent + "\n\n=== CÂU HỎI CỦA NGƯỜI DÙNG ===\n" + userMessage)
                .build();
        return executeChat(aiProviderFactory.getFallbackProvider(), request);
    }

    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackGenerateCvContent")
    @Retryable(include = { BusinessException.class, TimeoutException.class, HttpServerErrorException.class }, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public String generateCvContent(String prompt, String templateConfigJson) {
        aiRateLimiter.checkRateLimit(getCurrentUserId());
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\n" + AiPrompts.GENERATE_CV_PROMPT)
                .userMessage("=== USER BACKGROUND ===\n" + prompt + "\n\n=== SECTION CONFIG ===\n" + templateConfigJson)
                .build();
        return executeChat(aiProviderFactory.getPrimaryProvider(), request);
    }

    public String fallbackGenerateCvContent(String prompt, String templateConfigJson, Throwable t) {
        log.warn("Fallback to OpenAI for generateCvContent. Reason: {}", t.getMessage());
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\n" + AiPrompts.GENERATE_CV_PROMPT)
                .userMessage("=== USER BACKGROUND ===\n" + prompt + "\n\n=== SECTION CONFIG ===\n" + templateConfigJson)
                .build();
        return executeChat(aiProviderFactory.getFallbackProvider(), request);
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
}
