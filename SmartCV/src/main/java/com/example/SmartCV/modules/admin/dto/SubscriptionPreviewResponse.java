package com.example.SmartCV.modules.admin.dto;

import java.time.LocalDate;

import com.example.SmartCV.modules.subscription.domain.PlanType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionPreviewResponse {

    private Long userId;
    private PlanType oldPlan;
    private PlanType newPlan;
    private LocalDate startDate;
    private LocalDate endDate;
    private String message;
}
