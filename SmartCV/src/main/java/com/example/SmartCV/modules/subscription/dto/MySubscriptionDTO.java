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
    private PlanType plan;
    private SubscriptionStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
}
