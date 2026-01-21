package com.example.SmartCV.modules.subscription.dto;

import com.example.SmartCV.modules.subscription.domain.PlanType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PlanDefinitionDTO {
    private Long id;
    private String code;
    private String name;
    private BigDecimal price;
    private String currency;
    private int durationMonths;
    private PlanType planType; // The functional tier (FREE, PRO, etc)
    private int maxSharePerMonth;
    private int publicLinkExpireDays;
    private String description;
    private boolean active;
    private java.util.List<String> features;
}
