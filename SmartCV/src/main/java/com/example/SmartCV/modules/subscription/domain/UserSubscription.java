package com.example.SmartCV.modules.subscription.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== OWNER =====
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // ===== PLAN INFO =====
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    // ===== PERIOD =====
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // ===== PAYMENT TRACE (NEW) =====

    /**
     * Payment SUCCESS cuối cùng kích hoạt gói này
     * Nullable để không phá admin-upgrade cũ
     */
    @Column(name = "last_payment_id")
    private Long lastPaymentId;

    /**
     * Admin xác nhận (nếu là admin thao tác hoặc manual confirm)
     */
    @Column(name = "confirmed_by_admin_id")
    private Long confirmedByAdminId;

    /**
     * Thời điểm admin xác nhận
     */
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    // ===== HELPERS (GIỮ NGUYÊN) =====

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE;
    }

    public boolean isExpired() {
        return status == SubscriptionStatus.EXPIRED
                || (endDate != null && endDate.isBefore(LocalDate.now()));
    }
}
