package com.example.SmartCV.modules.admin.dto;

import com.example.SmartCV.modules.subscription.domain.PlanType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionConfirmRequest {
    @jakarta.validation.constraints.NotNull
    private Long userId;

    @jakarta.validation.constraints.NotNull
    private PlanType newPlan;

    @jakarta.validation.constraints.Min(1)
    private int durationMonths;

    private boolean confirm;
}
