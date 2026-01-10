package com.example.SmartCV.modules.subscription.domain;

import com.example.SmartCV.modules.subscription.domain.PlanType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plan_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private PlanType plan;

    // FREE = 1, PRO = 5, PREMIUM = 999
    @Column(name = "max_share_per_month", nullable = false)
    private int maxSharePerMonth;

    // số ngày link public tồn tại
    @Column(name = "public_link_expire_days", nullable = false)
    private int publicLinkExpireDays;
}
