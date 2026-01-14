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

        // ===== CASE 1: chưa có hoặc đã hết hạn =====
        if (current == null || current.isExpired()) {
            startDate = LocalDate.now();
        }

        // ===== CASE 2: gia hạn cùng plan =====
        else if (current.getPlan() == newPlan) {
            startDate = current.getEndDate() != null
                    ? current.getEndDate()
                    : LocalDate.now();
        }

        // ===== CASE 3: upgrade / downgrade plan =====
        else {
            // ghi đè từ hôm nay (an toàn – dễ kiểm soát)
            startDate = LocalDate.now();
        }

        LocalDate endDate = startDate.plusMonths(months);

        return new SubscriptionPeriod(startDate, endDate);
    }
}
