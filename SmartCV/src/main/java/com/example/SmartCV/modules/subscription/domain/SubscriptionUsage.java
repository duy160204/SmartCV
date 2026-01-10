package com.example.SmartCV.modules.subscription.domain;

import java.time.LocalDateTime;

import com.example.SmartCV.modules.subscription.domain.PlanType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "subscription_usage",
    indexes = {
        @Index(name = "idx_usage_user", columnList = "user_id"),
        @Index(name = "idx_usage_cv", columnList = "cv_id"),
        @Index(name = "idx_usage_expire", columnList = "expire_at"),
        @Index(name = "idx_usage_period", columnList = "period")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "cv_id", nullable = false)
    private Long cvId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", nullable = false)
    private UsageType usageType;

    @Column(name = "share_uuid", unique = true, nullable = false)
    private String shareUuid;

    // format: 2026-01
    @Column(nullable = false, length = 7)
    private String period;

    @Column(name = "expire_at", nullable = false)
    private LocalDateTime expireAt;

    @Builder.Default
    @Column(name = "notified_before_expire", nullable = false)
    private boolean notifiedBeforeExpire = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
