package com.example.SmartCV.modules.subscription.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.SmartCV.modules.subscription.domain.ChangeReason;
import com.example.SmartCV.modules.subscription.domain.PlanType;
import com.example.SmartCV.modules.subscription.domain.SubscriptionChangeType;
import com.example.SmartCV.modules.subscription.domain.SubscriptionHistory;
import com.example.SmartCV.modules.subscription.repository.SubscriptionHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionHistoryService {

    private final SubscriptionHistoryRepository historyRepository;

    public void saveHistory(
            Long userId,
            PlanType oldPlan,
            PlanType newPlan,
            SubscriptionChangeType changeType,
            ChangeReason reason
    ) {

        SubscriptionHistory history = SubscriptionHistory.builder()
                .userId(userId)
                .oldPlan(oldPlan)
                .newPlan(newPlan)
                .changeType(changeType)
                .reason(reason)
                .changedAt(LocalDateTime.now())
                .build();

        historyRepository.save(history);
    }
}
