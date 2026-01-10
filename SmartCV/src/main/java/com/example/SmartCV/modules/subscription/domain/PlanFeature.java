package com.example.SmartCV.modules.subscription.domain;

import com.example.SmartCV.modules.subscription.domain.PlanType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "plan_features",
    uniqueConstraints = @UniqueConstraint(columnNames = {"plan", "featureCode"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType plan;

    @Column(name = "feature_code", nullable = false)
    private String featureCode;

    @Column(nullable = false)
    private boolean enabled;
}
