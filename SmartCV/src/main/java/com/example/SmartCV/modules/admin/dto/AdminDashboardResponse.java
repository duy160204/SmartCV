package com.example.SmartCV.modules.admin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {

    private long totalUsers;
    private long verifiedUsers;
    private long lockedUsers;

    private long totalCVs;
    private long publicCVs;

    private long freeUsers;
    private long proUsers;
    private long premiumUsers;

    private long totalTemplates;
    private long activeTemplates;

    private long totalSubscriptionsActive;
}
