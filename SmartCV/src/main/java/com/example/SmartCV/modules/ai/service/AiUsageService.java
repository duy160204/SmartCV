package com.example.SmartCV.modules.ai.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.ai.domain.AiUsage;
import com.example.SmartCV.modules.ai.repository.AiUsageRepository;

import lombok.RequiredArgsConstructor;

/**
 * AI Usage Control Service
 * 
 * Purpose: Prevent abuse through daily request limits
 * NOT plan-based - applies equally to all users
 * Future-proof: Can be extended with plan-based limits later
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AiUsageService {

    private final AiUsageRepository aiUsageRepository;

    @Value("${app.ai.max-requests-per-day:20}")
    private int maxRequestsPerDay;

    /**
     * Check if user can make AI request today
     * Throws exception if limit exceeded
     */
    public void checkAndRecordUsage(Long userId) {
        LocalDate today = LocalDate.now();

        AiUsage usage = aiUsageRepository
                .findByUserIdAndUsageDate(userId, today)
                .orElseGet(() -> createNewUsage(userId, today));

        if (usage.getRequestCount() >= maxRequestsPerDay) {
            throw new RuntimeException(
                    "You've reached today's AI usage limit (" + maxRequestsPerDay
                            + " requests). Please try again tomorrow.");
        }

        usage.incrementCount();
        aiUsageRepository.save(usage);
    }

    /**
     * Get remaining requests for today
     */
    @Transactional(readOnly = true)
    public int getRemainingRequests(Long userId) {
        LocalDate today = LocalDate.now();

        return aiUsageRepository
                .findByUserIdAndUsageDate(userId, today)
                .map(usage -> Math.max(0, maxRequestsPerDay - usage.getRequestCount()))
                .orElse(maxRequestsPerDay);
    }

    private AiUsage createNewUsage(Long userId, LocalDate date) {
        return AiUsage.builder()
                .userId(userId)
                .usageDate(date)
                .requestCount(0)
                .build();
    }
}
