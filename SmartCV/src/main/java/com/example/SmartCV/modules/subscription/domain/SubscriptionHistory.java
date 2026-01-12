package com.example.SmartCV.modules.subscription.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscription_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User bị thay đổi subscription
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Plan cũ (có thể null nếu là SYSTEM_INIT)
    @Enumerated(EnumType.STRING)
    @Column(name = "old_plan")
    private PlanType oldPlan;

    // Plan mới
    @Enumerated(EnumType.STRING)
    @Column(name = "new_plan", nullable = false)
    private PlanType newPlan;

    // Loại thay đổi (ADMIN_UPDATE, PAYMENT_SUCCESS, EXPIRED, ...)
    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private SubscriptionChangeType changeType;

    // Nguồn thay đổi (ADMIN, PAYMENT, SYSTEM)
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private ChangeReason reason;

    // Thời điểm thay đổi
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
}
