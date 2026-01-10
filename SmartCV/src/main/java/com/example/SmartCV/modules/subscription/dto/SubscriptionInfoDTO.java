package com.example.SmartCV.modules.subscription.dto;

import com.example.SmartCV.modules.subscription.domain.PlanType;
import com.example.SmartCV.modules.subscription.domain.SubscriptionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscriptionInfoDTO {
    private PlanType plan;
    private SubscriptionStatus status;
    private String startDate;
    private String endDate;
}
