package com.example.SmartCV.modules.subscription.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdatePlanRequest {
    private String name;

    @jakarta.validation.constraints.Min(0)
    private BigDecimal price;

    @jakarta.validation.constraints.Min(1)
    private int durationMonths;

    @jakarta.validation.constraints.Min(0)
    private int maxSharePerMonth;

    @jakarta.validation.constraints.Min(1)
    private int publicLinkExpireDays;

    private String description;
}
