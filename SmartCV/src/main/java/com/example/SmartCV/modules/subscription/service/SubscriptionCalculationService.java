package com.example.SmartCV.modules.subscription.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.example.SmartCV.modules.subscription.domain.PlanType;
import com.example.SmartCV.modules.subscription.domain.SubscriptionPeriod;
import com.example.SmartCV.modules.subscription.domain.UserSubscription;

@Service
public class SubscriptionCalculationService {

    /**
     * Tính start/end cho plan mới.
     * Admin không set ngày, system tự tính.
     */
    public SubscriptionPeriod calculatePeriod(UserSubscription current, PlanType newPlan) {

        LocalDate startDate;
        LocalDate endDate;

        if (current == null || current.isExpired()) {
            startDate = LocalDate.now();
        } else {
            // có thể chọn nối tiếp hoặc ghi đè – ở đây chọn ghi đè từ hôm nay
            startDate = LocalDate.now();
        }

        // mặc định 1 tháng, sau này payment truyền số tháng vào
        endDate = startDate.plusMonths(1);

        return new SubscriptionPeriod(startDate, endDate);
    }
}
