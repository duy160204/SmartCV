package com.example.SmartCV.modules.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SmartCV.modules.payment.domain.PaymentStatus;
import com.example.SmartCV.modules.payment.domain.PaymentTransaction;

@Repository
public interface PaymentTransactionRepository
        extends JpaRepository<PaymentTransaction, Long> {

    /**
     * Dùng cho callback từ VNPay / Stripe
     */
    Optional<PaymentTransaction> findByTransactionCode(String transactionCode);

    /**
     * Lấy tất cả giao dịch của user
     */
    List<PaymentTransaction> findByUserId(Long userId);

    /**
     * Lấy giao dịch theo trạng thái
     */
    List<PaymentTransaction> findByStatus(PaymentStatus status);

    /**
     * Lấy giao dịch mới nhất của user
     */
    Optional<PaymentTransaction>
        findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
