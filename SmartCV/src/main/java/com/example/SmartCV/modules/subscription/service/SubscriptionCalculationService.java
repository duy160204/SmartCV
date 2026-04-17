package com.example.SmartCV.modules.subscription.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.example.SmartCV.modules.subscription.domain.PlanType;
import com.example.SmartCV.modules.subscription.domain.SubscriptionPeriod;
import com.example.SmartCV.modules.subscription.domain.UserSubscription;

@Service
public class SubscriptionCalculationService {

    /**
     * Tính chu kỳ subscription dựa trên:
     * - Subscription hiện tại
     * - Gói mới
     * - Số tháng mua (từ payment)
     */
    public SubscriptionPeriod calculatePeriod(
            UserSubscription current,
            PlanType newPlan,
            int months
    ) {
        if (months <= 0) {
            throw new IllegalArgumentException("Months must be > 0");
        }

        LocalDate startDate;

        // Start from current endDate if it's in the future, else start from now.
        // This ensures "No Data Loss" by stacking time regardless of plan change,
        // while granting benefits immediately (startDate is handled by upsert logic if needed).
        // For simple logic, we always calculate endDate based on the furthest point.
        startDate = (current != null && current.getEndDate() != null && current.getEndDate().isAfter(LocalDate.now()))
                ? current.getEndDate()
                : LocalDate.now();

        LocalDate endDate = startDate.plusMonths(months);

        return new SubscriptionPeriod(startDate, endDate);
    }
}
