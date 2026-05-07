package com.example.SmartCV.modules.subscription.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plan_features")
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
    @Column(name = "plan", nullable = false, columnDefinition = "VARCHAR(255)")
    private PlanType plan;

    @Column(name = "feature_code", nullable = false)
    private String featureCode;

    @Column(nullable = false)
    private boolean enabled;
}
