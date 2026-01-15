package com.example.SmartCV.modules.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminRevenueMonthlyDTO {

    private int year;
    private int month;
    private Long totalAmount;
}
