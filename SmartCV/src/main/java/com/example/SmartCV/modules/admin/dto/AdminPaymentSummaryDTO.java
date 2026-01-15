package com.example.SmartCV.modules.admin.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AdminPaymentSummaryDTO {

    private long totalPayments;      // số giao dịch SUCCESS
    private long totalPaidUsers;     // số user distinct
    private long totalRevenue;       // tổng tiền
}
