package com.example.SmartCV.modules.ai.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.ai.domain.AiUsage;
import com.example.SmartCV.modules.ai.repository.AiUsageRepository;

import lombok.RequiredArgsConstructor;

/**
 * AI Usage Tracking Service
 * 
 * Responsibilities:
 * - Increment usage count
 * - Provide current usage count
 * - Reset daily usage (handled by LocalDate.now() strategy)
 * 
 * Decision logic has been moved to AiGateway.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AiUsageService {

    private final AiUsageRepository aiUsageRepository;

    /**
     * Record a new AI request usage for the user
     */
    public void recordUsage(Long userId) {
        LocalDate today = LocalDate.now();

        AiUsage usage = aiUsageRepository
                .findByUserIdAndUsageDate(userId, today)
                .orElseGet(() -> AiUsage.builder()
                        .userId(userId)
                        .usageDate(today)
                        .requestCount(0)
                        .build());

        usage.incrementCount();
        aiUsageRepository.save(usage);
    }

    /**
     * Get current usage count for today
     */
    @Transactional(readOnly = true)
    public int getUsageCount(Long userId) {
        LocalDate today = LocalDate.now();

        return aiUsageRepository
                .findByUserIdAndUsageDate(userId, today)
                .map(AiUsage::getRequestCount)
                .orElse(0);
    }
}
