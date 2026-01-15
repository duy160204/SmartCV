package com.example.SmartCV.modules.admin.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminRevenueDailyDTO {

    private LocalDate date;
    private Long totalAmount;
}
