package com.example.SmartCV.modules.admin.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.payment.domain.PaymentStatus;
import com.example.SmartCV.modules.payment.domain.PaymentTransaction;
import com.example.SmartCV.modules.payment.repository.PaymentTransactionRepository;
import com.example.SmartCV.modules.subscription.domain.PlanType;
import com.example.SmartCV.modules.subscription.domain.SubscriptionHistory;
import com.example.SmartCV.modules.subscription.repository.SubscriptionHistoryRepository;
import com.example.SmartCV.modules.subscription.repository.UserSubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAnalyticsService {

    private final PaymentTransactionRepository paymentRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    public List<Map<String, Object>> getPaymentRevenueTrend() {
        LocalDateTime from = LocalDateTime.now().minusDays(30);
        LocalDateTime to = LocalDateTime.now();
        List<Object[]> results = paymentRepository.revenueByDay(PaymentStatus.SUCCESS, from, to);
        
        List<Map<String, Object>> trend = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", row[0].toString());
            map.put("value", row[1]);
            trend.add(map);
        }
        return trend;
    }

    public List<PaymentTransaction> getRecentPayments() {
        return paymentRepository.findTop5ByStatusOrderByCreatedAtDesc(PaymentStatus.SUCCESS);
    }

    public Map<String, Long> getSubscriptionDistribution() {
        long free = userSubscriptionRepository.countByPlan(PlanType.FREE);
        long pro = userSubscriptionRepository.countByPlan(PlanType.PRO);
        long premium = userSubscriptionRepository.countByPlan(PlanType.PREMIUM);
        
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("free", free);
        distribution.put("pro", pro);
        distribution.put("premium", premium);
        return distribution;
    }

    public List<SubscriptionHistory> getRecentSubscriptions() {
        return subscriptionHistoryRepository.findTop5ByOrderByChangedAtDesc();
    }
}
