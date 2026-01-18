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
    @Column(nullable = false) // NO unique=true here! Multiple plans can be PRO.
    private PlanType plan; // Determines feature set (FREE, PRO, PREMIUM)

    @Column(nullable = false, unique = true)
    private String code; // e.g. "PRO_MONTHLY"

    @Column(nullable = false)
    private String name; // e.g. "Pro Monthly"

    @Column(nullable = false)
    private java.math.BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private String currency = "VND";

    @Column(nullable = false)
    private int durationMonths;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    private String description;

    // FREE = 1, PRO = 5, PREMIUM = 999
    @Column(name = "max_share_per_month", nullable = false)
    private int maxSharePerMonth;

    // số ngày link public tồn tại
    @Column(name = "public_link_expire_days", nullable = false)
    private int publicLinkExpireDays;

    public long getDurationDays() {
        return (long) durationMonths * 30;
    }
}
