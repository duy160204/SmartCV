package com.example.SmartCV.modules.subscription.dto;

import com.example.SmartCV.modules.subscription.domain.PlanType;
import com.example.SmartCV.modules.subscription.domain.SubscriptionStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MySubscriptionDTO {
    
    private Long userId;
    private String role;
    
    private PlanInfo plan;
    private SubscriptionInfo subscription;
    private UsageInfo usage;
    private LimitInfo limits;

    // Flat fields for 4.3 API REQUIREMENT
    private String planName;
    private int maxAiRequestsPerDay;
    private int usedToday;
    private int remainingToday;

    @Data
    @Builder
    public static class PlanInfo {
        private String code;
        private String name;
        private int maxAiRequestsPerDay;
    }

    @Data
    @Builder
    public static class SubscriptionInfo {
        private SubscriptionStatus status;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data
    @Builder
    public static class UsageInfo {
        private int usedToday;
        private int remaining;
        private LocalDate resetAt;
    }

    @Data
    @Builder
    public static class LimitInfo {
        private boolean isUnlimited;
        private int rateLimitPerMinute;
    }
}
