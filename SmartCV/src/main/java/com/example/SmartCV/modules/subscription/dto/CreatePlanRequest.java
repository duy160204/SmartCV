package com.example.SmartCV.modules.subscription.dto;

import com.example.SmartCV.modules.subscription.domain.PlanType;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreatePlanRequest {
    @jakarta.validation.constraints.NotBlank(message = "Code is required")
    private String code;

    @jakarta.validation.constraints.NotBlank(message = "Name is required")
    private String name;

    @jakarta.validation.constraints.NotNull(message = "Price is required")
    @jakarta.validation.constraints.Min(value = 0, message = "Price must be non-negative")
    private BigDecimal price;

    @jakarta.validation.constraints.Min(value = 1, message = "Duration must be at least 1 month")
    private int durationMonths;

    @jakarta.validation.constraints.NotNull(message = "Plan Type (Tier) is required")
    private PlanType planType;

    @jakarta.validation.constraints.Min(value = 0, message = "Max share per month cannot be negative")
    private int maxSharePerMonth;

    @jakarta.validation.constraints.Min(value = 1, message = "Public link expire days must be at least 1")
    private int publicLinkExpireDays;

    private String description;
}
