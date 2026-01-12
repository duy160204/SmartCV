package com.example.SmartCV.modules.admin.dto;

import java.time.LocalDate;

import com.example.SmartCV.modules.subscription.domain.PlanType;
import com.example.SmartCV.modules.subscription.domain.SubscriptionStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserDetailResponse {

    private Long id;
    private String email;

    private boolean verified;
    private boolean locked;

    private LocalDate createdAt;

    private PlanType plan;
    private SubscriptionStatus subscriptionStatus;

    private LocalDate startDate;
    private LocalDate endDate;
}
