package com.example.SmartCV.modules.admin.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

import com.example.SmartCV.modules.subscription.domain.PlanType;

@Entity
@Table(name = "admin_subscription_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSubscriptionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== USER =====
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // ===== PLAN =====
    @Enumerated(EnumType.STRING)
    @Column(name = "requested_plan", nullable = false)
    private PlanType requestedPlan;

    @Column(name = "months", nullable = false)
    private Integer months;

    // ===== PAYMENT TRACE =====
    @Column(name = "payment_id", nullable = false, unique = true)
    private Long paymentId;

    // ===== STATUS =====
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminSubscriptionRequestStatus status;

    // ===== ADMIN TRACE =====
    @Column(name = "previewed_by_admin_id")
    private Long previewedByAdminId;

    @Column(name = "confirmed_by_admin_id")
    private Long confirmedByAdminId;

    @Column(name = "previewed_at")
    private LocalDateTime previewedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    // ===== TIME =====
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
