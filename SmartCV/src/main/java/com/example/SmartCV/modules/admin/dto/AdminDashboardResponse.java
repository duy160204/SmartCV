package com.example.SmartCV.modules.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardResponse {

    // =========================
    // USER
    // =========================
    private long totalUsers;
    private long verifiedUsers;
    private long lockedUsers;
    private long newUsers7Days;

    // =========================
    // CV
    // =========================
    private long totalCVs;
    private long publicCVs;

    // =========================
    // SUBSCRIPTION
    // =========================
    private long freeUsers;
    private long proUsers;
    private long premiumUsers;
    private long totalSubscriptionsActive;

    // =========================
    // TEMPLATE
    // =========================
    private long totalTemplates;
    private long activeTemplates;

    // =========================
    // PAYMENT (🔥 THÊM – KHÔNG PHÁ)
    // =========================

    /**
     * Tổng số payment (mọi trạng thái)
     */
    private long totalPayments;

    /**
     * Số payment SUCCESS
     */
    private long successPayments;

    /**
     * Tổng doanh thu (chỉ SUCCESS)
     */
    private long totalRevenue;

    /**
     * Số user đã trả tiền (distinct userId, SUCCESS)
     */
    private long paidUsers;
}
