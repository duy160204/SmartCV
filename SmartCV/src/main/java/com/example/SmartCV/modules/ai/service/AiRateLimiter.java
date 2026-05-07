package com.example.SmartCV.modules.ai.service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.SmartCV.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

@Service
public class AiRateLimiter {

    @Value("${app.ai.rate-limit-per-minute:10}")
    private double ratePerMinute;

    @Value("${app.ai.rate-limit-capacity:15}")
    private int capacity;

    private final ConcurrentHashMap<Long, BucketState> buckets = new ConcurrentHashMap<>();

    private static class BucketState {
        double tokens;
        long lastRefreshed;

        BucketState(double tokens, long lastRefreshed) {
            this.tokens = tokens;
            this.lastRefreshed = lastRefreshed;
        }
    }

    public void checkRateLimit(Long userId) {
        long now = Instant.now().getEpochSecond();
        double ratePerSecond = ratePerMinute / 60.0;

        BucketState state = buckets.computeIfAbsent(userId, k -> new BucketState(capacity, now));

        synchronized (state) {
            long delta = Math.max(0L, now - state.lastRefreshed);
            double filledTokens = Math.min(capacity, state.tokens + (delta * ratePerSecond));

            if (filledTokens >= 1.0) {
                state.tokens = filledTokens - 1.0;
                state.lastRefreshed = now;
            } else {
                state.tokens = filledTokens;
                state.lastRefreshed = now;
                throw new BusinessException("AI_RATE_LIMIT_EXCEEDED", HttpStatus.TOO_MANY_REQUESTS);
            }
        }

        // Periodically cleanup map to prevent OOM
        if (buckets.size() > 2000) {
            buckets.entrySet().removeIf(entry -> (now - entry.getValue().lastRefreshed) > 3600);
        }
    }
}
