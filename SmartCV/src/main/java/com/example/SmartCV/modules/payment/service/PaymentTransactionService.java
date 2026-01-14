package com.example.SmartCV.modules.payment.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.payment.domain.PaymentProvider;
import com.example.SmartCV.modules.payment.domain.PaymentStatus;
import com.example.SmartCV.modules.payment.domain.PaymentTransaction;
import com.example.SmartCV.modules.payment.repository.PaymentTransactionRepository;
import com.example.SmartCV.modules.subscription.domain.PlanType;
import com.example.SmartCV.modules.subscription.repository.PlanDefinitionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PlanDefinitionRepository planDefinitionRepository;

    /**
     * Tạo giao dịch thanh toán (PENDING)
     */
    @Transactional
    public PaymentTransaction createTransaction(
            Long userId,
            PlanType plan,
            Integer months,
            PaymentProvider provider
    ) {
        // 1. Validate input
        if (months == null || months <= 0) {
            throw new IllegalArgumentException("Months must be greater than 0");
        }

        // 2. Validate plan có tồn tại
        planDefinitionRepository.findByPlan(plan)
                .orElseThrow(() -> new IllegalArgumentException("Plan not supported: " + plan));

        // 3. Tính tiền (rule: giá * số tháng)
        long amount = calculateAmount(plan, months);

        // 4. Tạo transaction code
        String transactionCode = generateTransactionCode();

        // 5. Build entity
        PaymentTransaction transaction = PaymentTransaction.builder()
                .userId(userId)
                .plan(plan)
                .months(months)
                .amount(amount)
                .provider(provider)
                .status(PaymentStatus.PENDING)
                .transactionCode(transactionCode)
                .createdAt(LocalDateTime.now())
                .build();

        return paymentTransactionRepository.save(transaction);
    }

    /**
     * Đánh dấu giao dịch thành công
     */
    @Transactional
    public PaymentTransaction markSuccess(String transactionCode) {
        PaymentTransaction tx = getByTransactionCode(transactionCode);

        if (tx.getStatus() == PaymentStatus.SUCCESS) {
            return tx; // idempotent
        }

        tx.setStatus(PaymentStatus.SUCCESS);
        tx.setPaidAt(LocalDateTime.now());

        return paymentTransactionRepository.save(tx);
    }

    /**
     * Đánh dấu giao dịch thất bại
     */
    @Transactional
    public PaymentTransaction markFailed(String transactionCode) {
        PaymentTransaction tx = getByTransactionCode(transactionCode);

        if (tx.getStatus() == PaymentStatus.FAILED) {
            return tx;
        }

        tx.setStatus(PaymentStatus.FAILED);
        return paymentTransactionRepository.save(tx);
    }

    /**
     * Lấy transaction theo code
     */
    public PaymentTransaction getByTransactionCode(String transactionCode) {
        return paymentTransactionRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment transaction not found: " + transactionCode
                ));
    }

    /**
     * ===== PRIVATE HELPERS =====
     */

    private long calculateAmount(PlanType plan, int months) {
        // Tạm hard-code, sau có thể đưa vào bảng pricing
        return switch (plan) {
            case PRO -> 99000L * months;
            case PREMIUM -> 199000L * months;
            default -> throw new IllegalArgumentException("Plan not purchasable: " + plan);
        };
    }

    private String generateTransactionCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

