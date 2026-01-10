package com.example.SmartCV.modules.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlanInfoDTO {
    private String code;        // FREE, PRO, PREMIUM
    private String name;        // Free Plan, Pro Plan...
    private int maxSharePerMonth;
    private boolean allowDownload;
    private int publicDays;
}
