package com.example.SmartCV.modules.payment.controller;

import com.example.SmartCV.common.utils.UserPrincipal;
import com.example.SmartCV.modules.payment.domain.*;
import com.example.SmartCV.modules.payment.dto.CreatePaymentRequest;
import com.example.SmartCV.modules.payment.repository.PaymentTransactionRepository;
import com.example.SmartCV.modules.payment.service.VNPayClientService;
import com.example.SmartCV.modules.subscription.domain.PlanType;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentTransactionRepository paymentRepo;
    private final VNPayClientService vnpayClientService;

    @PostMapping
    public ResponseEntity<?> createPayment(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody CreatePaymentRequest request
    ) {
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        if (request.getPlan() == null || request.getMonths() == null) {
            return ResponseEntity.badRequest().body("Plan and months are required");
        }

        if (request.getMonths() <= 0) {
            return ResponseEntity.badRequest().body("Months must be greater than 0");
        }

        if (request.getProvider() == null) {
            return ResponseEntity.badRequest().body("Payment provider is required");
        }

        long amount;
        try {
            amount = calculateAmount(request.getPlan(), request.getMonths());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        PaymentTransaction tx = PaymentTransaction.builder()
                .userId(user.getId())
                .plan(request.getPlan())
                .months(request.getMonths())
                .amount(amount)
                .provider(request.getProvider())
                .status(PaymentStatus.PENDING)
                .transactionCode(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepo.save(tx);

        // ========= Gateway routing =========
        String paymentUrl;
        switch (tx.getProvider()) {
            case VNPAY -> paymentUrl = vnpayClientService.buildPaymentUrl(tx);
            default -> {
                return ResponseEntity
                        .badRequest()
                        .body("Unsupported payment provider: " + tx.getProvider());
            }
        }

        return ResponseEntity.ok(
                Map.of(
                        "paymentUrl", paymentUrl,
                        "transactionCode", tx.getTransactionCode()
                )
        );
    }

    /* ================= PRICING ================= */

    private long calculateAmount(PlanType plan, int months) {
        return switch (plan) {
            case PRO -> 99_000L * months;
            case PREMIUM -> 199_000L * months;
            default -> throw new IllegalArgumentException("Unsupported plan: " + plan);
        };
    }
}
