package com.example.SmartCV.modules.ai.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.SmartCV.common.exception.BusinessException;
import com.example.SmartCV.modules.subscription.domain.PlanDefinition;
import com.example.SmartCV.modules.subscription.domain.PlanType;
import com.example.SmartCV.modules.subscription.domain.UserSubscription;
import com.example.SmartCV.modules.subscription.repository.PlanDefinitionRepository;
import com.example.SmartCV.modules.subscription.repository.UserSubscriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

/**
 * Clean AI Gateway - SINGLE SOURCE OF TRUTH
 * - Enforces ALL AI Quota & Rate Limit Policies.
 * - Centralized decision logic for User, Admin, and System.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiGateway {

    private final AiRateLimiter aiRateLimiter;
    private final AiUsageService aiUsageService;
    private final AiService aiService;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PlanDefinitionRepository planDefinitionRepository;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    @org.springframework.beans.factory.annotation.Value("${app.ai.free-plan-daily-limit:50}")
    private int freePlanDailyLimit;

    /**
     * Submit Async AI Analysis Job
     */
    public void submitAiAnalysisJob(String jobId, Long userId, String cvContent, String prompt) {
        enforcePolicies(userId);

        java.util.Map<String, String> payload = new java.util.HashMap<>();
        payload.put("jobId", jobId);
        payload.put("userId", String.valueOf(userId));
        payload.put("cvContent", cvContent);
        payload.put("prompt", prompt);

        redisTemplate.opsForStream().add(com.example.SmartCV.config.RedisStreamConfig.STREAM_AI_REQUEST, payload);
        log.info("Submitted async AI job {} to Redis Stream", jobId);
    }

    /**
     * Helper to create Redis Stream Group
     */
    public void createStreamGroup(String streamKey, String groupName) {
        try {
            redisTemplate.opsForStream().createGroup(streamKey, groupName);
        } catch (org.springframework.data.redis.RedisSystemException e) {
            // Usually means group already exists
            log.debug("Stream group {} already exists for {}", groupName, streamKey);
        }
    }

    public String chatWithCv(Long userId, String cvContent, String userMessage, String level, String locale) {
        enforcePolicies(userId);
        return aiService.chatWithCv(cvContent, userMessage, level, locale);
    }

    public String generateCvContent(Long userId, String prompt, String templateConfigJson, String locale) {
        enforcePolicies(userId);
        return aiService.generateCvContent(prompt, templateConfigJson, locale);
    }

    public String improveText(Long userId, String text, String instruction, String locale) {
        enforcePolicies(userId);
        return aiService.improveText(text, instruction, locale);
    }

    public String buildTemplateFromImage(Long userId, String imageUrl, String locale) {
        enforcePolicies(userId);
        return aiService.buildTemplateFromImage(imageUrl, locale);
    }

    /**
     * Unified Policy Enforcement
     */
    private void enforcePolicies(Long userId) {
        // 1. ADMIN OVERRIDE - Global Bypass
        if (isAdmin()) {
            log.info("Admin Bypass: No AI limits enforced for user {}", userId);
            return;
        }

        // 2. SYSTEM / WORKER BYPASS (userId 0)
        if (userId == 0L) {
            log.info("System Task: Enforcement delegated to lower layers or ignored.");
            return; 
        }

        // 3. RESOLVE PLAN & LIMIT
        int dailyLimit = resolveDailyLimit(userId);

        // 4. ENFORCE DAILY QUOTA (DB)
        int currentUsage = aiUsageService.getUsageCount(userId);
        if (currentUsage >= dailyLimit) {
            log.warn("Daily Limit Reached: User {} (Usage: {}/{})", userId, currentUsage, dailyLimit);
            throw new BusinessException("AI_DAILY_LIMIT_EXCEEDED", HttpStatus.TOO_MANY_REQUESTS);
        }

        // 5. ENFORCE RATE LIMIT (REDIS)
        try {
            aiRateLimiter.checkRateLimit(userId);
        } catch (BusinessException e) {
            throw new BusinessException("AI_RATE_LIMIT_EXCEEDED", HttpStatus.TOO_MANY_REQUESTS);
        }

        // 6. RECORD USAGE
        aiUsageService.recordUsage(userId);
    }

    public boolean isUserAdmin() {
        return isAdmin();
    }

    public int getDailyLimit(Long userId) {
        return resolveDailyLimit(userId);
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }
        return false;
    }

    private int resolveDailyLimit(Long userId) {
        return userSubscriptionRepository.findByUserId(userId)
            .filter(UserSubscription::isActive)
            .flatMap(sub -> planDefinitionRepository.findFirstByPlanAndIsActiveTrueOrderByIdDesc(sub.getPlan()))
            .map(PlanDefinition::getMaxAiRequestsPerDay)
            .orElse(freePlanDailyLimit);
    }
}
