package com.example.SmartCV.modules.payment.controller;

import com.example.SmartCV.common.utils.UserPrincipal;
import com.example.SmartCV.modules.payment.domain.*;
import com.example.SmartCV.modules.payment.dto.CreatePaymentRequest;
import com.example.SmartCV.modules.payment.repository.PaymentTransactionRepository;
import com.example.SmartCV.modules.payment.service.VNPayClientService;

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

    private final com.example.SmartCV.modules.subscription.repository.PlanDefinitionRepository planDefinitionRepository;
    private final PaymentTransactionRepository paymentRepo;
    private final com.example.SmartCV.modules.payment.service.PaymentTransactionService paymentTransactionService;
    private final VNPayClientService vnpayClientService;

    @PostMapping
    public ResponseEntity<?> createPayment(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody CreatePaymentRequest request) {
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        if (request.getPlanCode() == null || request.getPlanCode().isEmpty()) {
            return ResponseEntity.badRequest().body("Plan code is required");
        }

        if (request.getProvider() == null) {
            return ResponseEntity.badRequest().body("Payment provider is required");
        }

        // LOOKUP PLAN
        var planDef = planDefinitionRepository.findByCode(request.getPlanCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid plan code: " + request.getPlanCode()));

        if (!planDef.isActive()) {
            return ResponseEntity.badRequest().body("Plan is currently inactive");
        }

        long amount = planDef.getPrice().longValue();

        PaymentTransaction tx = PaymentTransaction.builder()
                .userId(user.getId())
                .plan(planDef.getPlan()) // ENUM
                .months(planDef.getDurationMonths())
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
                        "transactionCode", tx.getTransactionCode()));
    }

    @GetMapping("/{transactionCode}")
    public ResponseEntity<?> getPaymentStatus(
            @PathVariable String transactionCode) {

        try {
            PaymentTransaction tx = paymentTransactionService.getByTransactionCode(transactionCode);

            return ResponseEntity.ok(Map.of(
                    "transactionCode", tx.getTransactionCode(),
                    "status", tx.getStatus(),
                    "amount", tx.getAmount(),
                    "plan", tx.getPlan(),
                    "createdAt", tx.getCreatedAt().toString()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
