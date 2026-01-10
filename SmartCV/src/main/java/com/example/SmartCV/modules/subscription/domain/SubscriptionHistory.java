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

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_plan")
    private PlanType oldPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_plan", nullable = false)
    private PlanType newPlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(nullable = false)
    private String reason; 
    // PAYMENT, ADMIN_UPDATE, EXPIRED, DOWNGRADE, SYSTEM

    @Column(name = "changed_at")
    private LocalDateTime changedAt;
}
