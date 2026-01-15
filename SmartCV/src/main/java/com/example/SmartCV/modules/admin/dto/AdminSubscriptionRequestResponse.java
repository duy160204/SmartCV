package com.example.SmartCV.modules.admin.dto;

import java.time.LocalDateTime;

import com.example.SmartCV.modules.admin.domain.AdminSubscriptionRequestStatus;
import com.example.SmartCV.modules.subscription.domain.PlanType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminSubscriptionRequestResponse {

    private Long id;
    private Long userId;
    private PlanType requestedPlan;
    private Integer months;
    private Long paymentId;
    private AdminSubscriptionRequestStatus status;
    private LocalDateTime createdAt;
}
