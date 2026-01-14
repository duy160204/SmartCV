package com.example.SmartCV.modules.subscription.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.SmartCV.modules.subscription.domain.*;
import com.example.SmartCV.modules.subscription.repository.SubscriptionHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionHistoryService {

    private final SubscriptionHistoryRepository repository;

    public void saveHistory(
            Long userId,
            PlanType oldPlan,
            PlanType newPlan,
            SubscriptionChangeType changeType,
            ChangeReason reason,
            Long paymentId,
            Long confirmedByAdminId
    ) {
        SubscriptionHistory history = SubscriptionHistory.builder()
                .userId(userId)
                .oldPlan(oldPlan)
                .newPlan(newPlan)
                .changeType(changeType)
                .reason(reason)
                .paymentId(paymentId)
                .confirmedByAdminId(confirmedByAdminId)
                .changedAt(LocalDateTime.now())
                .build();

        repository.save(history);
    }
}
