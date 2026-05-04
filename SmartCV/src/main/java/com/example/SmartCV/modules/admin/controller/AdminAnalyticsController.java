package com.example.SmartCV.modules.admin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SmartCV.modules.admin.service.AdminAnalyticsService;
import com.example.SmartCV.modules.payment.domain.PaymentTransaction;
import com.example.SmartCV.modules.subscription.domain.SubscriptionHistory;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final AdminAnalyticsService analyticsService;

    @GetMapping("/payments/revenue-trend")
    public ResponseEntity<List<Map<String, Object>>> getRevenueTrend() {
        return ResponseEntity.ok(analyticsService.getPaymentRevenueTrend());
    }

    @GetMapping("/payments/recent")
    public ResponseEntity<List<PaymentTransaction>> getRecentPayments() {
        return ResponseEntity.ok(analyticsService.getRecentPayments());
    }

    @GetMapping("/subscriptions/distribution")
    public ResponseEntity<Map<String, Long>> getSubscriptionDistribution() {
        return ResponseEntity.ok(analyticsService.getSubscriptionDistribution());
    }

    @GetMapping("/subscriptions/recent")
    public ResponseEntity<List<SubscriptionHistory>> getRecentSubscriptions() {
        return ResponseEntity.ok(analyticsService.getRecentSubscriptions());
    }
}
