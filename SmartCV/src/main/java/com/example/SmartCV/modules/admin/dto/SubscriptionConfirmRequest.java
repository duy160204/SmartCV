package com.example.SmartCV.modules.admin.dto;

import com.example.SmartCV.modules.subscription.domain.PlanType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionConfirmRequest {
    private Long userId;
    private PlanType newPlan;
    private boolean confirm;
    public int getMonths() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMonths'");
    }
}
