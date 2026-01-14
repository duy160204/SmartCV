package com.example.SmartCV.modules.payment.domain;

import java.time.LocalDateTime;

import com.example.SmartCV.modules.subscription.domain.PlanType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "payment_transactions",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "transaction_code")
    },
    indexes = {
        @Index(name = "idx_payment_user", columnList = "user_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_txn_code", columnList = "transaction_code")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User thực hiện thanh toán
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Gói được mua (FREE / PRO / PREMIUM)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType plan;

    /**
     * Số tháng mua (1, 3, 6, 12...)
     */
    @Column(nullable = false)
    private Integer months;

    /**
     * Tổng tiền tại thời điểm mua (đơn vị nhỏ nhất: VND / cents)
     */
    @Column(nullable = false)
    private Long amount;

    /**
     * Cổng thanh toán: VNPAY / STRIPE / PAYPAL
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentProvider provider;

    /**
     * Trạng thái giao dịch
     * PENDING / SUCCESS / FAILED / CANCELED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    /**
     * Mã giao dịch từ gateway
     * - VNPay: vnp_TxnRef
     * - Stripe: session_id
     * - Paypal: order_id
     */
    @Column(name = "transaction_code", nullable = false, unique = true)
    private String transactionCode;

    /**
     * Thời điểm thanh toán thành công
     * (chỉ set khi status = SUCCESS)
     */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /**
     * Thời điểm tạo giao dịch
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Thời điểm cập nhật trạng thái gần nhất
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =========================
    // LIFECYCLE
    // =========================
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // =========================
    // HELPERS
    // =========================
    public boolean isSuccess() {
        return this.status == PaymentStatus.SUCCESS;
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }
}
