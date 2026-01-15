package com.example.SmartCV.modules.admin.dto;

import com.example.SmartCV.modules.subscription.domain.PlanType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionPreviewRequest {
    private Long userId;
    private PlanType newPlan;
    public int getMonths() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMonths'");
    }
}
