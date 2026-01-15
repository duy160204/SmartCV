package com.example.SmartCV.modules.admin.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.payment.domain.PaymentProvider;
import com.example.SmartCV.modules.payment.domain.PaymentStatus;
import com.example.SmartCV.modules.payment.domain.PaymentTransaction;
import com.example.SmartCV.modules.payment.repository.PaymentTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPaymentService {

    private final PaymentTransactionRepository paymentRepository;

    // ==================================================
    // LIST ALL – newest first
    // ==================================================
    public List<PaymentTransaction> findAll() {
        return paymentRepository
                .findAllByOrderByCreatedAtDesc();
    }

    // ==================================================
    // FILTER BY USER
    // ==================================================
    public List<PaymentTransaction> findByUserId(Long userId) {
        return paymentRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ==================================================
    // FILTER BY STATUS
    // ==================================================
    public List<PaymentTransaction> findByStatus(
            PaymentStatus status
    ) {
        return paymentRepository
                .findByStatusOrderByCreatedAtDesc(status);
    }

    // ==================================================
    // FILTER BY PROVIDER
    // ==================================================
    public List<PaymentTransaction> findByProvider(
            PaymentProvider provider
    ) {
        return paymentRepository
                .findByProviderOrderByCreatedAtDesc(provider);
    }

    // ==================================================
    // FILTER BY DATE RANGE
    // [from 00:00, to +1day 00:00)
    // ==================================================
    public List<PaymentTransaction> findByDateRange(
            LocalDate from,
            LocalDate to
    ) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        return paymentRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(
                        start,
                        end
                );
    }

    // ==================================================
    // USER + DATE RANGE
    // ==================================================
    public List<PaymentTransaction> findByUserAndDateRange(
            Long userId,
            LocalDate from,
            LocalDate to
    ) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        return paymentRepository
                .findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        userId,
                        start,
                        end
                );
    }
    public PaymentTransaction findById(Long id) {
    return paymentRepository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("Payment not found: " + id)
            );
}

}
