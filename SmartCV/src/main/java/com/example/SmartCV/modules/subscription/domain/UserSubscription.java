package com.example.SmartCV.modules.subscription.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.SmartCV.modules.subscription.domain.PlanType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "user_subscriptions",
    indexes = {
        @Index(name = "idx_user_subscription_user", columnList = "user_id"),
        @Index(name = "idx_user_subscription_plan", columnList = "plan")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== Business helpers =====
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE
                && (endDate == null || !endDate.isBefore(LocalDate.now()));
    }

    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }
}
