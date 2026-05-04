package com.example.SmartCV.modules.ai.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.SmartCV.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Clean AI Gateway
 * - Unified Entry Point for ALL AI Requests.
 * - Handles Auth & Rate Limiting at the edge.
 */
@Service
@RequiredArgsConstructor
public class AiGateway {

    private final AiRateLimiter aiRateLimiter;
    private final AiService aiService;

    public String chatWithCv(Long userId, String cvContent, String userMessage, String level, String locale) {
        enforceRateLimit(userId);
        return aiService.chatWithCv(cvContent, userMessage, level, locale);
    }

    public String generateCvContent(Long userId, String prompt, String templateConfigJson, String locale) {
        enforceRateLimit(userId);
        return aiService.generateCvContent(prompt, templateConfigJson, locale);
    }

    public String improveText(Long userId, String text, String instruction, String locale) {
        enforceRateLimit(userId);
        return aiService.improveText(text, instruction, locale);
    }

    public String buildTemplateFromImage(Long userId, String imageUrl, String locale) {
        enforceRateLimit(userId);
        return aiService.buildTemplateFromImage(imageUrl, locale);
    }

    private void enforceRateLimit(Long userId) {
        try {
            aiRateLimiter.checkRateLimit(userId);
        } catch (BusinessException e) {
            // Standardize rate limit error for Gateway
            throw new BusinessException("RATE_LIMIT_EXCEEDED", HttpStatus.TOO_MANY_REQUESTS);
        }
    }
}
